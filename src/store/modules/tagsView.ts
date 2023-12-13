import { TagView, RouteRecordNormalized } from 'vue-router';

export const useTagsViewStore = defineStore('tagsView', () => {
  const visitedViews = ref<TagView[]>([]);
  const cachedViews = ref<string[]>([]);
  const iframeViews = ref<TagView[]>([]);

  const addView = (view: TagView) => {
    addVisitedView(view);
    addCachedView(view);
  };

  const addIframeView = (view: TagView): void => {
    if (iframeViews.value.some((v: TagView) => v.path === view.path)) return;
    iframeViews.value.push(
      Object.assign({}, view, {
        title: view.meta?.title || 'no-name'
      })
    );
  };
  const delIframeView = (view: TagView): Promise<TagView[]> => {
    return new Promise((resolve) => {
      iframeViews.value = iframeViews.value.filter((item: TagView) => item.path !== view.path);
      resolve([...iframeViews.value]);
    });
  };
  const addVisitedView = (view: TagView): void => {
    if (visitedViews.value.some((v: TagView) => v.path === view.path)) return;
    visitedViews.value.push(
      Object.assign({}, view, {
        title: view.meta?.title || 'no-name'
      })
    );
  };
  const delView = (view: TagView): Promise<{ visitedViews: TagView[]; cachedViews: string[] }> => {
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

  const delVisitedView = (view: TagView): Promise<TagView[]> => {
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
  const delCachedView = (view?: TagView): Promise<string[]> => {
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
  const delOthersViews = (view: TagView): Promise<{ visitedViews: TagView[]; cachedViews: string[] }> => {
    return new Promise((resolve) => {
      delOthersVisitedViews(view);
      delOthersCachedViews(view);
      resolve({
        visitedViews: [...visitedViews.value],
        cachedViews: [...cachedViews.value]
      });
    });
  };

  const delOthersVisitedViews = (view: TagView): Promise<TagView[]> => {
    return new Promise((resolve) => {
      visitedViews.value = visitedViews.value.filter((v: TagView) => {
        return v.meta?.affix || v.path === view.path;
      });
      resolve([...visitedViews.value]);
    });
  };
  const delOthersCachedViews = (view: TagView): Promise<string[]> => {
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

  const delAllViews = (): Promise<{ visitedViews: TagView[]; cachedViews: string[] }> => {
    return new Promise((resolve) => {
      delAllVisitedViews();
      delAllCachedViews();
      resolve({
        visitedViews: [...visitedViews.value],
        cachedViews: [...cachedViews.value]
      });
    });
  };
  const delAllVisitedViews = (): Promise<TagView[]> => {
    return new Promise((resolve) => {
      visitedViews.value = visitedViews.value.filter((tag: TagView) => tag.meta?.affix);
      resolve([...visitedViews.value]);
    });
  };

  const delAllCachedViews = (): Promise<string[]> => {
    return new Promise((resolve) => {
      cachedViews.value = [];
      resolve([...cachedViews.value]);
    });
  };

  const updateVisitedView = (view: TagView | RouteLocationNormalized): void => {
    for (let v of visitedViews.value) {
      if (v.path === view.path) {
        v = Object.assign(v, view);
        break;
      }
    }
  };
  const delRightTags = (view: TagView): Promise<TagView[]> => {
    return new Promise((resolve) => {
      const index = visitedViews.value.findIndex((v: TagView) => v.path === view.path);
      if (index === -1) {
        return;
      }
      visitedViews.value = visitedViews.value.filter((item: TagView, idx: number) => {
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
  const delLeftTags = (view: TagView): Promise<TagView[]> => {
    return new Promise((resolve) => {
      const index = visitedViews.value.findIndex((v: TagView) => v.path === view.path);
      if (index === -1) {
        return;
      }
      visitedViews.value = visitedViews.value.filter((item: TagView, idx: number) => {
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

  const addCachedView = (view: TagView | RouteLocationNormalized): void => {
    const viewName = view.name as string;
    if (!viewName) return;
    if (cachedViews.value.includes(viewName)) return;
    if (!view.meta?.noCache) {
      cachedViews.value.push(viewName);
    }
  };

  const isDynamicRoute = (view: any): boolean => {
    // 检查匹配的路由记录中是否有动态段
    return view.matched.some((m: RouteRecordNormalized) => m.path.includes(':'));
  };

  return {
    visitedViews,
    cachedViews,
    iframeViews,
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
