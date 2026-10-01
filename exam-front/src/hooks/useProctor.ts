import { nextTick, onBeforeUnmount, ref } from 'vue';
import { finishProctor, proctorHeartbeat, reportProctorEvents, startProctor, uploadProctorSnapshot } from '@/api/exam/proctor';
import type { ProctorEventBO, ProctorRule } from '@/api/exam/proctor/types';

/**
 * 防作弊采集
 *
 * <p>答题页只管「把可疑动作报上去」，计数、风险分级、要不要强制交卷都由后端算，
 * 前端改了也没用。整个采集做成尽力而为：任何一个环节出错（没摄像头、服务没起来、
 * 上报失败）都不能影响答题本身。
 *
 * @author ruoyi
 */
export interface UseProctorOptions {
  /** 答卷记录ID（考试ID要等试卷接口回来才知道，所以放在 start 里传） */
  recordId: string;
  /** 违规达到上限（如切屏超次）时回调，答题页据此强制交卷 */
  onExceed?: (reason: string) => void;
  /** 需要提示考生注意时的回调（切屏警告、退出全屏…） */
  onWarn?: (msg: string) => void;
}

/** 本地时间串，与后端 Date 解析格式保持一致 */
const nowText = (): string => {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
};

const deviceInfo = (): string => {
  const nav = window.navigator;
  return `${nav.platform} · ${window.screen.width}x${window.screen.height} · ${nav.language}`;
};

export function useProctor(options: UseProctorOptions) {
  /** 会话ID，空串表示防作弊没启用 */
  const sessionId = ref('');
  /** 本场生效的防作弊规则 */
  const rule = ref<ProctorRule | null>(null);
  const switchCount = ref(0);
  const pasteCount = ref(0);
  const exitFullscreenCount = ref(0);
  const cameraCount = ref(0);
  /** 摄像头是否真的打开（页面据此显示小窗） */
  const cameraOpen = ref(false);
  const cameraError = ref('');
  /** 页面里 <video> 的引用，抓拍从这里取画面 */
  const videoRef = ref<HTMLVideoElement | null>(null);
  /** 最近一次需要考生注意的提示 */
  const warning = ref('');

  let started = false;
  let stopped = false;
  let exceeded = false;
  let lastSwitchAt = 0;
  let lastDevtoolAt = 0;
  let lastMultitabAt = 0;
  let wasFullscreen = false;

  const queue: ProctorEventBO[] = [];
  let flushTimer: ReturnType<typeof setInterval> | undefined;
  let heartbeatTimer: ReturnType<typeof setInterval> | undefined;
  let cameraTimer: ReturnType<typeof setInterval> | undefined;
  let devtoolTimer: ReturnType<typeof setInterval> | undefined;
  let stream: MediaStream | null = null;
  let channel: BroadcastChannel | null = null;

  const warn = (msg: string): void => {
    warning.value = msg;
    options.onWarn?.(msg);
  };

  /** 入队一条事件，攒够或到点就发 */
  const push = (eventType: string, content?: string, extra?: string): void => {
    if (!started || stopped) {
      return;
    }
    queue.push({ eventType, content, extra, eventTime: nowText() });
    if (queue.length >= 5) {
      void flush();
    }
  };

  /** 同类事件在很短时间内只报一次，避免一次切屏被算成好几次 */
  const throttled = (lastAt: number, gap: number): boolean => Date.now() - lastAt < gap;

  /**
   * 用服务端计数覆盖本地计数
   *
   * <p>页面刷新后本地计数是从 0 重新开始的，而真实次数在服务端会话上。
   * 开考时用会话里的值初始化，之后每次上报 / 心跳再校准一次，
   * 否则考生一刷新就看到「切屏 0 次」，像是被清零了。
   */
  const syncCounts = (data?: {
    switchCount?: number;
    pasteCount?: number;
    exitFullscreenCount?: number;
    cameraCount?: number;
    exceed?: boolean;
    exceedReason?: string;
  }): void => {
    if (!data) {
      return;
    }
    switchCount.value = data.switchCount ?? switchCount.value;
    pasteCount.value = data.pasteCount ?? pasteCount.value;
    exitFullscreenCount.value = data.exitFullscreenCount ?? exitFullscreenCount.value;
    cameraCount.value = data.cameraCount ?? cameraCount.value;
    if (data.exceed && !exceeded) {
      exceeded = true;
      warn(data.exceedReason || '违规达到上限，系统已自动交卷');
      options.onExceed?.(data.exceedReason || '违规达到上限，系统已自动交卷');
    }
  };

  /** 把攒下的事件发出去，并用服务端返回的真实计数校准本地显示 */
  const flush = async (): Promise<void> => {
    if (!sessionId.value || queue.length === 0) {
      return;
    }
    const events = queue.splice(0, queue.length);
    try {
      const res = await reportProctorEvents(sessionId.value, events);
      syncCounts(res.data);
    } catch {
      // 上报失败不能打断答题：事件丢了就丢了，下一次心跳还会带上新的
    }
  };

  /* ------------------------------- 各类行为采集 ------------------------------- */

  /** 切屏 / 离屏：visibilitychange 与 blur 常常同时触发，1.5 秒内只算一次 */
  const markSwitch = (): void => {
    if (throttled(lastSwitchAt, 1500)) {
      return;
    }
    lastSwitchAt = Date.now();
    switchCount.value += 1;
    const max = rule.value?.switchScreen ?? 0;
    push('switch_screen', '离开了考试页面');
    if (max > 0) {
      warn(`已切屏 ${switchCount.value} 次，最多 ${max} 次，超过将自动交卷`);
    } else {
      warn('考试期间离开了页面，已被记录');
    }
  };

  const onVisibilityChange = (): void => {
    if (document.hidden) {
      markSwitch();
    }
  };

  const onBlur = (): void => {
    // 页面整个被切走时 visibilitychange 已经记过一次，这里只记「窗口失焦但页面还看得见」
    if (document.hidden) {
      markSwitch();
    } else {
      push('blur', '窗口失去焦点');
    }
  };

  const clipboardText = (e: ClipboardEvent): string => (e.clipboardData?.getData('text') ?? '').slice(0, 200);

  const onCopy = (e: ClipboardEvent): void => {
    push('copy', clipboardText(e));
    if (rule.value?.copyPaste === 1) {
      e.preventDefault();
      warn('本次考试禁止复制');
    }
  };

  const onCut = (e: ClipboardEvent): void => {
    push('cut', clipboardText(e));
    if (rule.value?.copyPaste === 1) {
      e.preventDefault();
      warn('本次考试禁止剪切');
    }
  };

  const onPaste = (e: ClipboardEvent): void => {
    const text = clipboardText(e);
    pasteCount.value += 1;
    push('paste', text || '剪贴板内容（非文本）');
    if (rule.value?.copyPaste === 1) {
      e.preventDefault();
      warn('本次考试禁止粘贴');
      return;
    }
    const max = rule.value?.maxPaste ?? 0;
    if (max > 0 && pasteCount.value > max) {
      warn(`粘贴次数已达 ${pasteCount.value} 次，超过限制将自动交卷`);
    }
  };

  const onContextMenu = (e: MouseEvent): void => {
    push('contextmenu', '');
    if (rule.value?.copyPaste === 1) {
      e.preventDefault();
    }
  };

  /** 开发者工具相关快捷键：只记录并提醒，不强制交卷 */
  const onKeyDown = (e: KeyboardEvent): void => {
    const key = e.key?.toLowerCase();
    const hit =
      key === 'f12' ||
      (e.ctrlKey && e.shiftKey && ['i', 'j', 'c'].includes(key)) ||
      (e.metaKey && e.altKey && ['i', 'j', 'c'].includes(key)) ||
      (e.ctrlKey && key === 'u') ||
      (e.ctrlKey && e.shiftKey && key === 's');
    if (!hit) {
      return;
    }
    if (throttled(lastDevtoolAt, 30_000)) {
      return;
    }
    lastDevtoolAt = Date.now();
    push('devtool', `按下 ${e.key}`);
    warn('考试期间请勿打开开发者工具');
  };

  /** 打开开发者工具后窗口可用尺寸会明显变小，按尺寸差二次判断 */
  const checkDevtool = (): void => {
    if (throttled(lastDevtoolAt, 30_000)) {
      return;
    }
    const widthGap = window.outerWidth - window.innerWidth;
    const heightGap = window.outerHeight - window.innerHeight;
    if (widthGap > 160 || heightGap > 160) {
      lastDevtoolAt = Date.now();
      push('devtool', `窗口尺寸异常 ${widthGap}x${heightGap}`);
      warn('检测到疑似开发者工具，考试期间请勿使用');
    }
  };

  /** 同账号在多个标签页打开同一场答卷：互相打个招呼就知道了 */
  const setupMultitab = (): void => {
    if (rule.value?.multitab === 0 || typeof BroadcastChannel === 'undefined') {
      return;
    }
    try {
      channel = new BroadcastChannel(`proctor_${options.recordId}`);
      channel.onmessage = (e: MessageEvent) => {
        if (throttled(lastMultitabAt, 30_000)) {
          return;
        }
        if (e.data === 'ping') {
          channel?.postMessage('pong');
          return;
        }
        if (e.data === 'pong') {
          lastMultitabAt = Date.now();
          push('multitab', '同一场考试在多个标签页中打开');
          warn('检测到同一场考试在多个标签页中打开，请勿多开');
        }
      };
      channel.postMessage('ping');
    } catch {
      // 浏览器不支持就算了，多开检测本来就是附加项
    }
  };

  /** 兼容内核前缀：Safari 只有 webkitFullscreenElement */
  const inFullscreenNow = (): boolean => {
    const doc = document as Document & { webkitFullscreenElement?: Element | null };
    return !!doc.fullscreenElement || !!doc.webkitFullscreenElement;
  };

  /** 记一次「退出全屏」：只有真的进去过才记，避免没进过全屏的人被反复计数 */
  const markExitFullscreen = (reason: string): void => {
    if (!wasFullscreen) {
      return;
    }
    wasFullscreen = false;
    exitFullscreenCount.value += 1;
    push('exit_fullscreen', reason);
    const max = rule.value?.maxExitFullscreen ?? 0;
    warn(max > 0 ? `已退出全屏 ${exitFullscreenCount.value} 次，最多 ${max} 次` : '考试要求全屏作答，请重新进入全屏');
  };

  const onFullscreenChange = (): void => {
    if (inFullscreenNow()) {
      wasFullscreen = true;
      push('enter_fullscreen', '进入全屏');
      return;
    }
    markExitFullscreen('退出了全屏');
  };

  /**
   * 全屏状态兜底检查
   *
   * <p>只靠 fullscreenchange 会漏：F11 是浏览器级全屏，压根不触发这个事件；
   * Esc 退出时偶尔也收不到。所以定时看一眼真实状态，退出过就补记一次。
   */
  const checkFullscreen = (): void => {
    if (rule.value?.fullScreen !== 1) {
      return;
    }
    if (inFullscreenNow()) {
      wasFullscreen = true;
      return;
    }
    markExitFullscreen('检测到未处于全屏状态');
  };

  /** 浏览器要求全屏必须由用户手势触发，所以提供按钮让人点一下 */
  const enterFullscreen = async (): Promise<void> => {
    try {
      const el = document.documentElement as HTMLElement & { webkitRequestFullscreen?: () => Promise<void> };
      if (el.requestFullscreen) {
        await el.requestFullscreen();
      } else if (el.webkitRequestFullscreen) {
        await el.webkitRequestFullscreen();
      }
      // 进入成功与否交给 fullscreenchange / 定时检查去认，这里不乐观置位
    } catch {
      warn('当前浏览器不允许自动进入全屏，请手动按 F11 后不要再退出');
    }
  };

  /* --------------------------------- 摄像头 --------------------------------- */

  const capture = async (eventType = 'periodic'): Promise<void> => {
    const video = videoRef.value;
    if (!video || !cameraOpen.value || !sessionId.value) {
      return;
    }
    // 画面还没真正出来就抓，会得到一张纯黑图，不如跳过等下一轮
    if (video.readyState < 2 || video.videoWidth === 0 || video.videoHeight === 0) {
      return;
    }
    try {
      const canvas = document.createElement('canvas');
      // 按摄像头真实比例缩放，避免拉伸出黑边
      canvas.width = 320;
      canvas.height = Math.round((320 * video.videoHeight) / video.videoWidth);
      const ctx = canvas.getContext('2d');
      if (!ctx) {
        return;
      }
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
      const blob = await new Promise<Blob | null>((resolve) => canvas.toBlob(resolve, 'image/jpeg', 0.7));
      if (!blob) {
        return;
      }
      await uploadProctorSnapshot(sessionId.value, blob, eventType);
      cameraCount.value += 1;
    } catch {
      // 抓拍失败不打断答题，下一轮还会再试
    }
  };

  /**
   * 等 <video> 挂载
   *
   * <p>监考卡片是 v-if="proctorId"，拿到会话 ID 后 DOM 要等下一轮渲染才有这个元素，
   * 直接取 ref 会是 null，画面永远挂不上去（表现就是小窗全黑、抓拍也是黑的）。
   */
  const waitVideo = async (): Promise<HTMLVideoElement | null> => {
    for (let i = 0; i < 30; i++) {
      if (videoRef.value) {
        return videoRef.value;
      }
      await new Promise((r) => setTimeout(r, 100));
    }
    return null;
  };

  const openCamera = async (): Promise<void> => {
    try {
      stream = await navigator.mediaDevices.getUserMedia({ video: { width: 320, height: 240 }, audio: false });
    } catch {
      cameraError.value = '未获取到摄像头，请检查权限与设备';
      push('camera_deny', cameraError.value);
      return;
    }
    // 先把小窗显示出来再挂流：display:none 的 <video> 在部分浏览器里不解码画面，抓出来是纯黑
    cameraOpen.value = true;
    cameraError.value = '';
    await nextTick();
    const video = videoRef.value ?? (await waitVideo());
    if (!video) {
      cameraError.value = '摄像头画面未就绪，请刷新页面重试';
      return;
    }
    video.srcObject = stream;
    video.muted = true;
    // 等元数据就绪再 play，否则第一帧还没解码就开始抓拍
    await new Promise<void>((resolve) => {
      const timer = setTimeout(() => {
        video.removeEventListener('loadedmetadata', onReady);
        resolve();
      }, 3000);
      function onReady(): void {
        clearTimeout(timer);
        video.removeEventListener('loadedmetadata', onReady);
        resolve();
      }
      video.addEventListener('loadedmetadata', onReady);
    });
    try {
      await video.play();
    } catch {
      // 静音自动播放一般都能过，过不了就等用户交互，不影响抓拍
    }
    const interval = Math.max(15, rule.value?.cameraInterval ?? 60) * 1000;
    cameraTimer = setInterval(() => void capture('periodic'), interval);
    // 入场先来一张，监考端能确认「这个人对得上号」。
    // 画面可能还没解码出来，所以重试几轮，拿到一张就停（抓不到会被 capture 自己跳过）
    void (async () => {
      for (let i = 0; i < 5; i++) {
        await new Promise((r) => setTimeout(r, 1000));
        const before = cameraCount.value;
        await capture('enter');
        if (cameraCount.value > before) {
          return;
        }
      }
    })();
  };

  /* --------------------------------- 生命周期 --------------------------------- */

  const bind = (): void => {
    document.addEventListener('visibilitychange', onVisibilityChange);
    window.addEventListener('blur', onBlur);
    document.addEventListener('copy', onCopy, true);
    document.addEventListener('cut', onCut, true);
    document.addEventListener('paste', onPaste, true);
    document.addEventListener('contextmenu', onContextMenu, true);
    document.addEventListener('keydown', onKeyDown, true);
    document.addEventListener('fullscreenchange', onFullscreenChange);
    // 进页面时可能已经是全屏（比如从全屏的考试中心点进来），别把这当成一次退出
    wasFullscreen = inFullscreenNow();
    flushTimer = setInterval(() => void flush(), 5_000);
    heartbeatTimer = setInterval(() => {
      if (!sessionId.value || stopped) {
        return;
      }
      // 心跳顺带把服务端计数带回来：本地计数可能因为刷新页面从 0 重新起算
      void proctorHeartbeat(sessionId.value)
        .then((res) => syncCounts(res.data))
        .catch(() => undefined);
    }, 30_000);
    // 开发者工具与全屏状态一起查：两者都是「事件不一定来」的状态类检测
    devtoolTimer = setInterval(() => {
      checkDevtool();
      checkFullscreen();
    }, 3_000);
  };

  const unbind = (): void => {
    document.removeEventListener('visibilitychange', onVisibilityChange);
    window.removeEventListener('blur', onBlur);
    document.removeEventListener('copy', onCopy, true);
    document.removeEventListener('cut', onCut, true);
    document.removeEventListener('paste', onPaste, true);
    document.removeEventListener('contextmenu', onContextMenu, true);
    document.removeEventListener('keydown', onKeyDown, true);
    document.removeEventListener('fullscreenchange', onFullscreenChange);
    [flushTimer, heartbeatTimer, cameraTimer, devtoolTimer].forEach((t) => t && clearInterval(t));
    flushTimer = heartbeatTimer = cameraTimer = devtoolTimer = undefined;
    stream?.getTracks().forEach((track) => track.stop());
    stream = null;
    cameraOpen.value = false;
    channel?.close();
    channel = null;
    if (document.fullscreenElement) {
      void document.exitFullscreen().catch(() => undefined);
    }
  };

  /**
   * 开启监考：防作弊服务不可用时静默降级，绝不能挡住答题
   *
   * @param examId 考试ID，试卷接口回来才拿得到
   */
  const start = async (examId: string): Promise<void> => {
    try {
      const res = await startProctor({ examId, recordId: options.recordId, device: deviceInfo() });
      sessionId.value = String(res.data?.id ?? '');
      rule.value = res.data?.rule ?? null;
      if (!sessionId.value) {
        return;
      }
      // 续答 / 刷新页面时会话是复用的，先把服务端已有的计数拿回来，
      // 否则本地从 0 起算，考生会以为自己之前的记录被清零了
      syncCounts(res.data);
      started = true;
      bind();
      setupMultitab();
      if (rule.value?.camera === 1) {
        await openCamera();
      }
      if (rule.value?.fullScreen === 1) {
        // 自动进全屏通常会被浏览器拦，提示里给个按钮让人点
        warn('本场考试要求全屏作答，请点击下方「进入全屏」');
      }
    } catch {
      // 没拿到会话就当这场没开防作弊，照常答题
      sessionId.value = '';
    }
  };

  /**
   * 结束采集
   *
   * @param status 传 submitted / force_submit 时顺带通知后端结束会话
   */
  const stop = async (status?: string): Promise<void> => {
    if (!started) {
      return;
    }
    stopped = true;
    await flush();
    unbind();
    if (status) {
      try {
        await finishProctor(options.recordId, status);
      } catch {
        // 后端交卷时也会通知一次，这里失败无所谓
      }
    }
  };

  onBeforeUnmount(() => {
    void stop();
  });

  return {
    sessionId,
    rule,
    switchCount,
    pasteCount,
    exitFullscreenCount,
    cameraCount,
    cameraOpen,
    cameraError,
    videoRef,
    warning,
    start,
    stop,
    flush,
    enterFullscreen,
    capture
  };
}
