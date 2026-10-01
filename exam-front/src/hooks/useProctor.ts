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

/** 答题页 <video> 的固定 id：模板 ref 之外再留一条找元素的路 */
const VIDEO_ID = 'proctor-video';

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
  /** 摄像头是否打开（拿到设备流，页面据此显示小窗） */
  const cameraOpen = ref(false);
  /** 画面是否真的出来了（拿到流 ≠ 有画面，解码完才算，抓拍只在就绪后进行） */
  const cameraReady = ref(false);
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
  /** 考试窗口当前是否有焦点，用来识别「有焦点 → 没焦点」的变化沿 */
  let windowFocused = true;
  /**
   * 本次「离屏」是否已经记过一次
   *
   * <p>一次点击会引出好几个信号（visibilitychange / blur / 定时兜底，甚至 blur+focus+blur），
   * 节流挡不住相隔几秒的那些。所以只要还没确认「人回来了」，就绝不再记第二次。
   */
  let awayCounted = false;
  /** blur 后延迟复检的定时器，卸载时统一清掉 */
  const focusTimers = new Set<ReturnType<typeof setTimeout>>();

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

  /**
   * 记一次切屏
   *
   * <p>「切屏」不止切换标签页：切到别的软件窗口（微信、钉钉、另一个浏览器窗口）
   * 时页面并没有 hidden，只是浏览器窗口失去了焦点，同样属于离开考试画面，必须一起算。
   *
   * <p>去重靠两层：① awayCounted —— 同一段离屏只记一次，人没回来就不解锁；
   * ② 1.5 秒节流 —— 兜住「回来后又立刻切走」这种抖动。
   * 后端只按「类型 + 秒」去重，隔了几秒的两条会各算一次，所以这一层必须在前端堵住。
   */
  const markSwitch = (reason: string): void => {
    if (awayCounted) {
      return;
    }
    if (throttled(lastSwitchAt, 1500)) {
      return;
    }
    awayCounted = true;
    lastSwitchAt = Date.now();
    switchCount.value += 1;
    const max = rule.value?.switchScreen ?? 0;
    push('switch_screen', reason);
    // 切走前抓拍一张：监考端能确认「离开那一刻屏幕前是谁」
    if (cameraReady.value) {
      void capture('switch_screen');
    }
    if (max > 0) {
      warn(`已切屏 ${switchCount.value} 次，最多 ${max} 次，超过将自动交卷`);
    } else {
      warn('考试期间离开了页面，已被记录');
    }
  };

  /**
   * 焦点状态机：所有来源（事件 + 定时兜底）都收敛到这里
   *
   * <p>状态一律**现读**（`hasFocus() && !hidden`），事件只负责「什么时候读、以什么理由记」。
   * 原因：事件会骗人 —— Chrome 在 blur 事件里 `document.hasFocus()` 常常还是 true，
   * 切到别的窗口也可能只来 blur 不来 visibilitychange，甚至 blur 之后又补一个 focus。
   * 以前每个事件各自维护一份 windowFocused，于是同一次点击被判成两次「有焦点 → 没焦点」。
   */
  const syncFocus = (reason: string, onBack?: () => void): void => {
    const focused = document.hasFocus() && !document.hidden;
    if (focused) {
      const back = !windowFocused;
      windowFocused = true;
      // 人回来了，解锁下一次计数
      awayCounted = false;
      if (back) {
        onBack?.();
      }
      return;
    }
    const wasFocused = windowFocused;
    windowFocused = false;
    // 只在「有焦点 → 没焦点」这个变化沿上记一次
    if (wasFocused) {
      markSwitch(reason);
    }
  };

  /** blur 之后延迟复读几次：等焦点真正切走，读到的状态才可信 */
  const scheduleFocusCheck = (reason: string): void => {
    [0, 300, 1200].forEach((delay) => {
      const timer = setTimeout(() => {
        focusTimers.delete(timer);
        syncFocus(reason);
      }, delay);
      focusTimers.add(timer);
    });
  };

  /** 切换标签页 / 最小化：页面进入后台 */
  const onVisibilityChange = (): void => {
    syncFocus('切换了浏览器标签页或最小化窗口');
  };

  /**
   * 浏览器窗口失去焦点：切到其它软件或另一个浏览器窗口
   *
   * <p>这种场景下 document.hidden 仍是 false（页面「可见」），老逻辑只记了一条 blur，
   * 结果切到别的应用去查资料完全不计入切屏次数。
   */
  const onBlur = (): void => {
    // 事件当下如果焦点其实还在（点浏览器地址栏等），只落一条 info 流水，不计数
    if (document.hasFocus() && !document.hidden) {
      push('blur', '窗口短暂失去焦点');
    }
    scheduleFocusCheck('浏览器窗口失去焦点（切换到了其它窗口或应用）');
  };

  /** 焦点回到考试页面：顺带抓拍一张，监考端能看到「人回来了」 */
  const onFocus = (): void => {
    syncFocus('', () => {
      if (cameraReady.value) {
        void capture('resume');
      }
    });
  };

  /**
   * 焦点状态兜底检查
   *
   * <p>blur 事件不是每次都来：系统级切换（mac 调度中心 / 切换桌面）、全屏下 alt-tab、
   * 部分浏览器最小化时都可能丢事件。所以定时再看一眼真实状态补记，
   * 持续离屏由 awayCounted 兜住，不会被反复计数。
   */
  const checkFocus = (): void => {
    syncFocus('考试窗口失去焦点（可能切换到了其它应用）');
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

  /** 与模板 <video> 约定的 id：万一模板 ref 没绑上，还能按 id 兜底找到元素 */
  const findVideo = (): HTMLVideoElement | null => videoRef.value ?? (document.getElementById(VIDEO_ID) as HTMLVideoElement | null);

  /** 整帧平均亮度极低就是没出画面，这种图传上去没意义 */
  const isBlankFrame = (canvas: HTMLCanvasElement, ctx: CanvasRenderingContext2D): boolean => {
    try {
      const { data } = ctx.getImageData(0, 0, canvas.width, canvas.height);
      let sum = 0;
      let count = 0;
      // 隔点采样，够判断又不至于每次抓拍都遍历全部像素
      for (let i = 0; i < data.length; i += 16) {
        sum += data[i] + data[i + 1] + data[i + 2];
        count += 3;
      }
      return count > 0 && sum / count < 6;
    } catch {
      return false;
    }
  };

  /**
   * 抓拍一帧并上传
   *
   * <p>返回是否真的抓到一张：画面没解码出来（readyState/videoWidth 不满足）
   * 或整帧全黑时直接跳过等下一轮 —— 监考端收到一堆黑图比少几张更糟。
   */
  const capture = async (eventType = 'periodic'): Promise<boolean> => {
    const video = findVideo();
    if (!video || !cameraOpen.value || !sessionId.value) {
      return false;
    }
    if (video.readyState < 2 || video.videoWidth === 0 || video.videoHeight === 0) {
      return false;
    }
    try {
      const canvas = document.createElement('canvas');
      canvas.width = 320;
      // 按摄像头真实比例缩放，避免拉伸出黑边
      canvas.height = Math.max(1, Math.round((320 * video.videoHeight) / video.videoWidth));
      const ctx = canvas.getContext('2d');
      if (!ctx) {
        return false;
      }
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
      if (isBlankFrame(canvas, ctx)) {
        return false;
      }
      const blob = await new Promise<Blob | null>((resolve) => canvas.toBlob(resolve, 'image/jpeg', 0.7));
      if (!blob) {
        return false;
      }
      await uploadProctorSnapshot(sessionId.value, blob, eventType);
      cameraCount.value += 1;
      return true;
    } catch {
      // 抓拍失败不打断答题，下一轮还会再试
      return false;
    }
  };

  /** 入场抓拍：画面可能还没解码出来，重试几轮，抓到一张就停 */
  const captureWithRetry = async (eventType: string, rounds = 6): Promise<void> => {
    for (let i = 0; i < rounds; i++) {
      if (stopped || (await capture(eventType))) {
        return;
      }
      await new Promise((r) => setTimeout(r, 1000));
    }
  };

  /**
   * 等 <video> 真正挂载
   *
   * <p>监考卡片受 v-if 控制（会话ID、页面 loading 等），开考瞬间 DOM 往往还没渲染出来，
   * 直接取 ref 会是 null。这里轮询等待，超时交给调用方决定是否重试。
   */
  const waitVideo = async (timeoutMs = 10_000): Promise<HTMLVideoElement | null> => {
    const deadline = Date.now() + timeoutMs;
    while (Date.now() < deadline) {
      const el = findVideo();
      if (el) {
        videoRef.value = el;
        return el;
      }
      await new Promise((r) => setTimeout(r, 200));
    }
    return null;
  };

  const ensurePlaying = async (video: HTMLVideoElement): Promise<void> => {
    if (!video.paused) {
      return;
    }
    try {
      await video.play();
    } catch {
      // 自动播放被拦（少数浏览器要求用户手势）就等下一轮，不打断答题
    }
  };

  /** 等画面解码出来：拿到流只是第一步，readyState 没到 2 拍出来就是黑图 */
  const waitFrame = async (video: HTMLVideoElement, timeoutMs = 8_000): Promise<boolean> => {
    const deadline = Date.now() + timeoutMs;
    while (Date.now() < deadline) {
      if (video.readyState >= 2 && video.videoWidth > 0) {
        return true;
      }
      await ensurePlaying(video);
      await new Promise((r) => setTimeout(r, 200));
    }
    return false;
  };

  const startCaptureTimer = (): void => {
    if (cameraTimer) {
      return;
    }
    const interval = Math.max(15, rule.value?.cameraInterval ?? 60) * 1000;
    cameraTimer = setInterval(() => void capture('periodic'), interval);
  };

  /** 把设备流挂到 <video> 并开始抓拍，成功返回 true */
  const attachStream = async (video: HTMLVideoElement): Promise<boolean> => {
    if (video.srcObject !== stream) {
      video.srcObject = stream;
    }
    video.muted = true;
    video.playsInline = true;
    if (!(await waitFrame(video))) {
      return false;
    }
    cameraReady.value = true;
    cameraError.value = '';
    startCaptureTimer();
    // 入场先来一张，监考端能确认「这个人对得上号」
    void captureWithRetry('enter');
    return true;
  };

  /** 后台重试挂流：应对「开考时页面还没渲染完」这类时序问题，不再一棍子打死 */
  const retryAttach = async (rounds = 15): Promise<void> => {
    for (let i = 0; i < rounds; i++) {
      if (stopped) {
        return;
      }
      await new Promise((r) => setTimeout(r, 2_000));
      const video = findVideo();
      if (!video) {
        continue;
      }
      if (await attachStream(video)) {
        return;
      }
    }
    if (!cameraReady.value) {
      cameraError.value = '摄像头画面未就绪，请检查设备后刷新页面重试';
    }
  };

  const cameraErrorMessage = (e: unknown): string => {
    switch ((e as DOMException | undefined)?.name ?? '') {
      case 'NotAllowedError':
      case 'SecurityError':
        return '摄像头权限被拒绝，请在浏览器地址栏允许后重试';
      case 'NotFoundError':
      case 'DevicesNotFoundError':
        return '未检测到摄像头设备';
      case 'NotReadableError':
      case 'TrackStartError':
        return '摄像头被其它程序占用，请关闭后重试';
      case 'OverconstrainedError':
        return '摄像头不支持当前分辨率要求';
      default:
        return '未获取到摄像头，请检查权限与设备';
    }
  };

  /**
   * 摄像头保活：定时盯一眼画面还在不在
   *
   * <p>切标签页回来、浏览器自动暂停、DOM 重建都可能让画面断掉，
   * 只靠开考那一次挂流不够，断了要能自己接回去。
   */
  const keepCameraAlive = (): void => {
    const video = findVideo();
    if (!video || !cameraOpen.value) {
      return;
    }
    if (stream && video.srcObject !== stream) {
      video.srcObject = stream;
      video.muted = true;
    }
    if (video.paused) {
      void ensurePlaying(video);
    }
    const ready = video.readyState >= 2 && video.videoWidth > 0;
    if (!ready) {
      cameraReady.value = false;
      return;
    }
    if (!cameraReady.value) {
      cameraReady.value = true;
      cameraError.value = '';
      startCaptureTimer();
      void captureWithRetry('enter');
      return;
    }
    // 设备被拔掉或被系统回收：如实提示，别假装正常
    if (stream && stream.getVideoTracks().some((t) => t.readyState === 'ended')) {
      cameraError.value = '摄像头连接已断开，请检查设备';
    }
  };

  const openCamera = async (): Promise<void> => {
    if (!navigator.mediaDevices?.getUserMedia) {
      cameraError.value = '当前环境不支持摄像头（需通过 HTTPS 或 localhost 访问）';
      push('camera_deny', cameraError.value);
      return;
    }
    try {
      stream = await navigator.mediaDevices.getUserMedia({ video: { width: 640, height: 480 }, audio: false });
    } catch (e) {
      cameraError.value = cameraErrorMessage(e);
      push('camera_deny', cameraError.value);
      return;
    }
    // 先把小窗显示出来再挂流：display:none 的 <video> 在部分浏览器里不解码画面
    cameraOpen.value = true;
    cameraError.value = '';
    await nextTick();
    const video = await waitVideo();
    if (!video || !(await attachStream(video))) {
      cameraError.value = '正在等待摄像头画面…';
      void retryAttach();
    }
  };

  /* --------------------------------- 生命周期 --------------------------------- */

  const bind = (): void => {
    document.addEventListener('visibilitychange', onVisibilityChange);
    window.addEventListener('blur', onBlur);
    window.addEventListener('focus', onFocus);
    document.addEventListener('copy', onCopy, true);
    document.addEventListener('cut', onCut, true);
    document.addEventListener('paste', onPaste, true);
    document.addEventListener('contextmenu', onContextMenu, true);
    document.addEventListener('keydown', onKeyDown, true);
    document.addEventListener('fullscreenchange', onFullscreenChange);
    // 进页面时可能已经是全屏（比如从全屏的考试中心点进来），别把这当成一次退出
    wasFullscreen = inFullscreenNow();
    // 同理：考生可能是在别的窗口里打开试卷、页面自己加载完的，别把这当成一次切屏
    windowFocused = document.hasFocus() && !document.hidden;
    awayCounted = false;
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
    // 开发者工具、全屏状态、窗口焦点、摄像头画面一起查：四者都是「事件不一定来」的状态类检测
    devtoolTimer = setInterval(() => {
      checkDevtool();
      checkFullscreen();
      checkFocus();
      keepCameraAlive();
    }, 3_000);
  };

  const unbind = (): void => {
    document.removeEventListener('visibilitychange', onVisibilityChange);
    window.removeEventListener('blur', onBlur);
    window.removeEventListener('focus', onFocus);
    document.removeEventListener('copy', onCopy, true);
    document.removeEventListener('cut', onCut, true);
    document.removeEventListener('paste', onPaste, true);
    document.removeEventListener('contextmenu', onContextMenu, true);
    document.removeEventListener('keydown', onKeyDown, true);
    document.removeEventListener('fullscreenchange', onFullscreenChange);
    [flushTimer, heartbeatTimer, cameraTimer, devtoolTimer].forEach((t) => t && clearInterval(t));
    flushTimer = heartbeatTimer = cameraTimer = devtoolTimer = undefined;
    focusTimers.forEach((t) => clearTimeout(t));
    focusTimers.clear();
    stream?.getTracks().forEach((track) => track.stop());
    stream = null;
    cameraOpen.value = false;
    cameraReady.value = false;
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
    cameraReady,
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
