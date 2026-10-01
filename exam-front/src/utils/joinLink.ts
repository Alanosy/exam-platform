/**
 * 考生端加入考试链接的拼接
 *
 * <p>背景：`VITE_APP_CONTEXT_PATH` 表示部署时的应用访问路径，本地默认是 `'/'`，
 * 而加入页路由又是 `/exam/join/`（两头都带斜杠），直接字符串拼接就会得到
 * `http://localhost//exam/join/xxxx` 这种双斜杠链接，浏览器会当成站点根目录外的
 * 绝对路径，路由匹配不上直接白屏。同理，contextPath 写成 `/admin/`（带尾斜杠）
 * 也会多一个斜杠。这里统一 h 在拼接时压掉多余斜杠。
 */

/** 考生端加入考试的页面路径，与路由 `/exam/join/:code` 保持一致 */
const JOIN_PATH = '/exam/join/';

/**
 * 拼接可直接发给考生的完整加入链接
 *
 * @param joinCode 考试的加入码，为空时返回空串（未生成链接 / 白名单考试）
 */
export const buildJoinLink = (joinCode?: string | null): string => {
  if (!joinCode) {
    return '';
  }
  const contextPath = import.meta.env.VITE_APP_CONTEXT_PATH || '/';
  // 只压 path 段，不要动 origin 里的 http://
  const path = `${contextPath}/${JOIN_PATH}${joinCode}`.replace(/\/{2,}/g, '/');
  return `${window.location.origin}${path}`;
};
