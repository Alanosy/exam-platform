import { RouteLocationNormalized } from 'vue-router';

export const useTagsViewStore = defineStore('tagsView', () => {
  const visitedViews = ref<RouteLocationNormalized[]>([]);
  const cachedViews = ref<string[]>([]);
  const iframeViews = ref<RouteLocationNormalized[]>([]);

  const getVisitedViews = (): RouteLocationNormalized[] => {
    return visitedViews.value;
  };
  const getIframeViews = (): RouteLocationNormalized[] => {
    return iframeViews.value;
  };
  const getCachedViews = (): string[] => {
    return cachedViews.value;
  };

  const addView = (view: RouteLocationNormalized) => {
    addVisitedView(view);
    addCachedView(view);
  };

  const addIframeView = (view: RouteLocationNormalized): void => {
    if (iframeViews.value.some((v: RouteLocationNormalized) => v.path === view.path)) return;
    iframeViews.value.push(
      Object.assign({}, view, {
        title: view.meta?.title || 'no-name'
      })
    );
  };
  const delIframeView = (view: RouteLocationNormalized): Promise<RouteLocationNormalized[]> => {
    return new Promise((resolve) => {
      iframeViews.value = iframeViews.value.filter((item: RouteLocationNormalized) => item.path !== view.path);
      resolve([...iframeViews.value]);
    });
  };
  const addVisitedView = (view: RouteLocationNormalized): void => {
    if (visitedViews.value.some((v: RouteLocationNormalized) => v.path === view.path)) return;
    visitedViews.value.push(
      Object.assign({}, view, {
        title: view.meta?.title || 'no-name'
      })
    );
  };
  const delView = (
    view: RouteLocationNormalized
  ): Promise<{
    visitedViews: RouteLocationNormalized[];
    cachedViews: string[];
  }> => {
    return new Promise((resolve) => {
      delVisitedView(view);
      if (!isDynamicRoute(view)) {
        delCachedView(view);
      }
      resolve({
        visitedViews: [...visitedViews.value],
        cachedViews: [...cachedViews.value]
      });
    });
  };

  const delVisitedView = (view: RouteLocationNormalized): Promise<RouteLocationNormalized[]> => {
    return new Promise((resolve) => {
      for (const [i, v] of visitedViews.value.entries()) {
        if (v.path === view.path) {
          visitedViews.value.splice(i, 1);
          break;
        }
      }
      resolve([...visitedViews.value]);
    });
  };
  const delCachedView = (view?: RouteLocationNormalized): Promise<string[]> => {
    let viewName = '';
    if (view) {
      viewName = view.name as string;
    }
    return new Promise((resolve) => {
      const index = cachedViews.value.indexOf(viewName);
      index > -1 && cachedViews.value.splice(index, 1);
      resolve([...cachedViews.value]);
    });
  };
  const delOthersViews = (
    view: RouteLocationNormalized
  ): Promise<{
    visitedViews: RouteLocationNormalized[];
    cachedViews: string[];
  }> => {
    return new Promise((resolve) => {
      delOthersVisitedViews(view);
      delOthersCachedViews(view);
      resolve({
        visitedViews: [...visitedViews.value],
        cachedViews: [...cachedViews.value]
      });
    });
  };

  const delOthersVisitedViews = (view: RouteLocationNormalized): Promise<RouteLocationNormalized[]> => {
    return new Promise((resolve) => {
      visitedViews.value = visitedViews.value.filter((v: RouteLocationNormalized) => {
        return v.meta?.affix || v.path === view.path;
      });
      resolve([...visitedViews.value]);
    });
  };
  const delOthersCachedViews = (view: RouteLocationNormalized): Promise<string[]> => {
    const viewName = view.name as string;
    return new Promise((resolve) => {
      const index = cachedViews.value.indexOf(viewName);
      if (index > -1) {
        cachedViews.value = cachedViews.value.slice(index, index + 1);
      } else {
        cachedViews.value = [];
      }
      resolve([...cachedViews.value]);
    });
  };

  const delAllViews = (): Promise<{ visitedViews: RouteLocationNormalized[]; cachedViews: string[] }> => {
    return new Promise((resolve) => {
      delAllVisitedViews();
      delAllCachedViews();
      resolve({
        visitedViews: [...visitedViews.value],
        cachedViews: [...cachedViews.value]
      });
    });
  };
  const delAllVisitedViews = (): Promise<RouteLocationNormalized[]> => {
    return new Promise((resolve) => {
      visitedViews.value = visitedViews.value.filter((tag: RouteLocationNormalized) => tag.meta?.affix);
      resolve([...visitedViews.value]);
    });
  };

  const delAllCachedViews = (): Promise<string[]> => {
    return new Promise((resolve) => {
      cachedViews.value = [];
      resolve([...cachedViews.value]);
    });
  };

  const updateVisitedView = (view: RouteLocationNormalized): void => {
    for (let v of visitedViews.value) {
      if (v.path === view.path) {
        v = Object.assign(v, view);
        break;
      }
    }
  };
  const delRightTags = (view: RouteLocationNormalized): Promise<RouteLocationNormalized[]> => {
    return new Promise((resolve) => {
      const index = visitedViews.value.findIndex((v: RouteLocationNormalized) => v.path === view.path);
      if (index === -1) {
        return;
      }
      visitedViews.value = visitedViews.value.filter((item: RouteLocationNormalized, idx: number) => {
        if (idx <= index || (item.meta && item.meta.affix)) {
          return true;
        }
        const i = cachedViews.value.indexOf(item.name as string);
        if (i > -1) {
          cachedViews.value.splice(i, 1);
        }
        return false;
      });
      resolve([...visitedViews.value]);
    });
  };
  const delLeftTags = (view: RouteLocationNormalized): Promise<RouteLocationNormalized[]> => {
    return new Promise((resolve) => {
      const index = visitedViews.value.findIndex((v: RouteLocationNormalized) => v.path === view.path);
      if (index === -1) {
        return;
      }
      visitedViews.value = visitedViews.value.filter((item: RouteLocationNormalized, idx: number) => {
        if (idx >= index || (item.meta && item.meta.affix)) {
          return true;
        }
        const i = cachedViews.value.indexOf(item.name as string);
        if (i > -1) {
          cachedViews.value.splice(i, 1);
        }
        return false;
      });
      resolve([...visitedViews.value]);
    });
  };

  const addCachedView = (view: RouteLocationNormalized): void => {
    const viewName = view.name as string;
    if (!viewName) return;
    if (cachedViews.value.includes(viewName)) return;
    if (!view.meta?.noCache) {
      cachedViews.value.push(viewName);
    }
  };

  const isDynamicRoute = (view: RouteLocationNormalized): boolean => {
    // 检查匹配的路由记录中是否有动态段
    return view.matched.some((m) => m.path.includes(':'));
  };

  return {
    visitedViews,
    cachedViews,
    iframeViews,

    getVisitedViews,
    getIframeViews,
    getCachedViews,

    addVisitedView,
    addCachedView,
    delVisitedView,
    delCachedView,
    updateVisitedView,
    addView,
    delView,
    delAllViews,
    delAllVisitedViews,
    delAllCachedViews,
    delOthersViews,
    delRightTags,
    delLeftTags,
    addIframeView,
    delIframeView
  };
});
export default useTagsViewStore;
