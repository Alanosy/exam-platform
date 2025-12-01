/**
 * 开发者工具保护
 * 检测开发者工具是否打开，如果打开则循环执行 debugger 阻止调试
 */

// 检测开发者工具是否打开
function detectDevTools(): boolean {
  try {
    // 方法1: 检测窗口尺寸差异（最可靠的方法）
    const widthThreshold = 160; // 开发者工具最小宽度
    const heightThreshold = 160; // 开发者工具最小高度

    const widthDiff = window.outerWidth - window.innerWidth;
    const heightDiff = window.outerHeight - window.innerHeight;

    if (widthDiff > widthThreshold || heightDiff > heightThreshold) {
      return true;
    }

    // 方法2: 检测 debugger 执行时间（最准确的方法）
    const start = performance.now();
    debugger; // 这个 debugger 用于检测，不会被移除
    const end = performance.now();
    const timeDiff = end - start;

    // 如果 debugger 被跳过（开发者工具关闭），时间差会很小（通常 < 1ms）
    // 如果 debugger 暂停（开发者工具打开），时间差会很大（通常 > 100ms）
    // 降低阈值以提高检测灵敏度
    if (timeDiff > 10) {
      return true;
    }

    // 方法3: 检测控制台对象
    let devtoolsDetected = false;
    const element = document.createElement('div');
    Object.defineProperty(element, 'id', {
      get: function () {
        devtoolsDetected = true;
        return '';
      }
    });

    // 使用 console 来触发 getter（仅在开发者工具打开时）
    try {
      console.log(element);
      console.clear();
    } catch (e) {
      // 忽略错误
    }

    if (devtoolsDetected) {
      return true;
    }

    // 方法4: 检测控制台是否被重写（开发者工具打开时）
    const devtoolsRegex = /./;
    // @ts-expect-error - 动态添加属性
    devtoolsRegex.toString = function () {
      // @ts-expect-error - 动态添加属性
      this.opened = true;
    };
    console.log('%c', devtoolsRegex);
    // @ts-expect-error - 检查动态添加的属性
    if (devtoolsRegex.opened) {
      return true;
    }
  } catch (e) {
    // 如果检测过程中出错，默认返回 false
    return false;
  }

  return false;
}

// 开发者工具保护主函数
export function initDevToolsProtection(): void {
  // 可以通过环境变量控制是否启用
  // 生产环境默认启用，开发环境可以通过 VITE_ENABLE_ANTI_DEBUG=true 来启用测试
  const isProduction = import.meta.env.MODE === 'production';
  const enableAntiDebug = import.meta.env.VITE_ENABLE_ANTI_DEBUG === 'true' || isProduction;

  if (!enableAntiDebug) {
    return;
  }

  let devToolsOpen = false;
  let debuggerInterval: number | null = null;

  // 立即执行一次检测
  const initialCheck = detectDevTools();
  if (initialCheck) {
    devToolsOpen = true;
    debuggerInterval = window.setInterval(() => {
      debugger; // 循环执行 debugger
    }, 50); // 更频繁的 debugger，每 50ms 一次
  }

  // 循环检测开发者工具（更频繁的检测）
  const checkInterval = setInterval(() => {
    const isOpen = detectDevTools();

    if (isOpen && !devToolsOpen) {
      // 开发者工具刚打开
      devToolsOpen = true;

      // 开始循环执行 debugger（更频繁）
      if (debuggerInterval === null) {
        debuggerInterval = window.setInterval(() => {
          debugger; // 循环执行 debugger
        }, 50); // 每 50ms 执行一次，更激进
      }
    } else if (!isOpen && devToolsOpen) {
      // 开发者工具关闭了
      devToolsOpen = false;

      // 停止循环 debugger
      if (debuggerInterval !== null) {
        clearInterval(debuggerInterval);
        debuggerInterval = null;
      }
    }
  }, 500); // 每 500ms 检测一次，更频繁

  // 页面卸载时清理
  window.addEventListener('beforeunload', () => {
    clearInterval(checkInterval);
    if (debuggerInterval !== null) {
      clearInterval(debuggerInterval);
    }
  });

  // 额外的检测：监听窗口大小变化
  let lastWidth = window.innerWidth;
  let lastHeight = window.innerHeight;

  window.addEventListener('resize', () => {
    const currentWidth = window.innerWidth;
    const currentHeight = window.innerHeight;

    // 如果窗口尺寸变化很大，可能是开发者工具打开/关闭
    if (Math.abs(currentWidth - lastWidth) > 200 || Math.abs(currentHeight - lastHeight) > 200) {
      // 重新检测
      const isOpen = detectDevTools();
      if (isOpen && !devToolsOpen) {
        devToolsOpen = true;
        if (debuggerInterval === null) {
          debuggerInterval = window.setInterval(() => {
            debugger; // 循环执行 debugger
          }, 50); // 更频繁的 debugger
        }
      }
    }

    lastWidth = currentWidth;
    lastHeight = currentHeight;
  });
}

