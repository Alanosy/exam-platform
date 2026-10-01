import { computed, onBeforeUnmount, onMounted, ref } from 'vue';

/**
 * 服务端时钟
 *
 * <p>考试能不能开始由服务端说了算，而考生电脑的时钟可能快也可能慢：
 * 按本地时间算倒计时，要么按钮早亮（点进去被后端一句「考试尚未开始」打回来），
 * 要么按钮晚亮（明明到点了还得干等）。
 * 这里用接口返回的 serverTime 校准出「服务端现在几点」，
 * 前端的倒计时 / 到点判定统统以它为准，与后端保持同一口径。
 */

/** 校准值缓存：换个页面重新挂载时先用上一次的，不用等接口回来才知道准绳 */
const OFFSET_KEY = 'exam-server-offset';

/** 把后端给的时间串转成时间戳，解析不出来返回 NaN */
export const toTs = (value?: string): number => {
  if (!value) return Number.NaN;
  const text = String(value).trim();
  // 后端给的是本地时间串（yyyy-MM-dd HH:mm:ss），按本地时区解析，别当成 UTC
  const ts = new Date(text.includes('T') ? text : text.replace(' ', 'T')).getTime();
  return Number.isNaN(ts) ? Number.NaN : ts;
};

/** 毫秒倒计时的展示文案：超过一天带天数，否则 hh:mm:ss */
export const formatCountdown = (millis: number): string => {
  const left = Math.floor(millis / 1000);
  const pad = (n: number) => String(n).padStart(2, '0');
  const days = Math.floor(left / 86400);
  const clock = `${pad(Math.floor((left % 86400) / 3600))}:${pad(Math.floor((left % 3600) / 60))}:${pad(left % 60)}`;
  return days > 0 ? `${days} 天 ${clock}` : clock;
};

export function useServerClock() {
  const serverOffset = ref(Number(sessionStorage.getItem(OFFSET_KEY)) || 0);
  /** 本地时钟读数，每秒走一次，驱动倒计时 */
  const nowTs = ref(Date.now());
  /** 校准后的「服务端现在」 */
  const serverNow = computed(() => nowTs.value + serverOffset.value);

  let timer: ReturnType<typeof setInterval> | undefined;

  onMounted(() => {
    timer = setInterval(() => {
      nowTs.value = Date.now();
    }, 1000);
  });

  onBeforeUnmount(() => {
    if (timer) clearInterval(timer);
  });

  /**
   * 用一次接口往返校准时钟
   *
   * @param serverTime 接口返回的 serverTime
   * @param sentAt     请求发出前的本地时间戳，用来扣掉一半的网络往返
   */
  const syncServerTime = (serverTime?: string, sentAt?: number): void => {
    const serverTs = toTs(serverTime);
    if (Number.isNaN(serverTs)) {
      return;
    }
    const receivedAt = Date.now();
    const roundTrip = Number.isFinite(sentAt) ? (receivedAt - (sentAt as number)) / 2 : 0;
    serverOffset.value = serverTs + roundTrip - receivedAt;
    nowTs.value = receivedAt;
    sessionStorage.setItem(OFFSET_KEY, String(serverOffset.value));
  };

  return { serverNow, serverOffset, syncServerTime };
}
