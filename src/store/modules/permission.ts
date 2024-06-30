import { defineStore } from 'pinia';
import router, { constantRoutes, dynamicRoutes } from '@/router';
import store from '@/store';
import { getRouters } from '@/api/menu';
import auth from '@/plugins/auth';
import { RouteRecordRaw } from 'vue-router';

import Layout from '@/layout/index.vue';
import ParentView from '@/components/ParentView/index.vue';
import InnerLink from '@/layout/components/InnerLink/index.vue';

import { createCustomNameComponent } from '@/utils/createCustomNameComponent';

// 匹配views里面所有的.vue文件
const modules = import.meta.glob('./../../views/**/*.vue');
export const usePermissionStore = defineStore('permission', () => {
  const routes = ref<RouteRecordRaw[]>([]);
  const addRoutes = ref<RouteRecordRaw[]>([]);
  const defaultRoutes = ref<RouteRecordRaw[]>([]);
  const topbarRouters = ref<RouteRecordRaw[]>([]);
  const sidebarRouters = ref<RouteRecordRaw[]>([]);

  const getRoutes = (): RouteRecordRaw[] => {
    return routes.value;
  };
  const getSidebarRoutes = (): RouteRecordRaw[] => {
    return sidebarRouters.value;
  };
  const getTopbarRoutes = (): RouteRecordRaw[] => {
    return topbarRouters.value;
  };

  const setRoutes = (newRoutes: RouteRecordRaw[]): void => {
    addRoutes.value = newRoutes;
    routes.value = constantRoutes.concat(newRoutes);
  };
  const setDefaultRoutes = (routes: RouteRecordRaw[]): void => {
    defaultRoutes.value = constantRoutes.concat(routes);
  };
  const setTopbarRoutes = (routes: RouteRecordRaw[]): void => {
    topbarRouters.value = routes;
  };
  const setSidebarRouters = (routes: RouteRecordRaw[]): void => {
    sidebarRouters.value = routes;
  };
  const generateRoutes = async (): Promise<RouteRecordRaw[]> => {
    const res = await getRouters();
    const { data } = res;
    const sdata = JSON.parse(JSON.stringify(data));
    const rdata = JSON.parse(JSON.stringify(data));
    const defaultData = JSON.parse(JSON.stringify(data));
    const sidebarRoutes = filterAsyncRouter(sdata);
    const rewriteRoutes = filterAsyncRouter(rdata, undefined, true);
    const defaultRoutes = filterAsyncRouter(defaultData);
    const asyncRoutes = filterDynamicRoutes(dynamicRoutes);
    asyncRoutes.forEach((route) => {
      router.addRoute(route);
    });
    setRoutes(rewriteRoutes);
    setSidebarRouters(constantRoutes.concat(sidebarRoutes));
    setDefaultRoutes(sidebarRoutes);
    setTopbarRoutes(defaultRoutes);
    // 路由name重复检查
    duplicateRouteChecker(asyncRoutes, sidebarRoutes);
    return new Promise<RouteRecordRaw[]>((resolve) => resolve(rewriteRoutes));
  };

  /**
   * 遍历后台传来的路由字符串，转换为组件对象
   * @param asyncRouterMap 后台传来的路由字符串
   * @param lastRouter 上一级路由
   * @param type 是否是重写路由
   */
  const filterAsyncRouter = (asyncRouterMap: RouteRecordRaw[], lastRouter?: RouteRecordRaw, type = false): RouteRecordRaw[] => {
    return asyncRouterMap.filter((route) => {
      if (type && route.children) {
        route.children = filterChildren(route.children, undefined);
      }
      // Layout ParentView 组件特殊处理
      if (route.component?.toString() === 'Layout') {
        route.component = Layout;
      } else if (route.component?.toString() === 'ParentView') {
        route.component = ParentView;
      } else if (route.component?.toString() === 'InnerLink') {
        route.component = InnerLink;
      } else {
        route.component = loadView(route.component, route.name as string);
      }
      if (route.children != null && route.children && route.children.length) {
        route.children = filterAsyncRouter(route.children, route, type);
      } else {
        delete route.children;
        delete route.redirect;
      }
      return true;
    });
  };
  const filterChildren = (childrenMap: RouteRecordRaw[], lastRouter?: RouteRecordRaw): RouteRecordRaw[] => {
    let children: RouteRecordRaw[] = [];
    childrenMap.forEach((el) => {
      if (el.children && el.children.length) {
        if (el.component?.toString() === 'ParentView' && !lastRouter) {
          el.children.forEach((c) => {
            c.path = el.path + '/' + c.path;
            if (c.children && c.children.length) {
              children = children.concat(filterChildren(c.children, c));
              return;
            }
            children.push(c);
          });
          return;
        }
      }
      if (lastRouter) {
        el.path = lastRouter.path + '/' + el.path;
        if (el.children && el.children.length) {
          children = children.concat(filterChildren(el.children, el));
          return;
        }
      }
      children = children.concat(el);
    });
    return children;
  };
  return {
    routes,
    topbarRouters,
    sidebarRouters,
    defaultRoutes,

    getRoutes,
    getSidebarRoutes,
    getTopbarRoutes,

    setRoutes,
    generateRoutes,
    setSidebarRouters
  };
});

// 动态路由遍历，验证是否具备权限
export const filterDynamicRoutes = (routes: RouteRecordRaw[]) => {
  const res: RouteRecordRaw[] = [];
  routes.forEach((route) => {
    if (route.permissions) {
      if (auth.hasPermiOr(route.permissions)) {
        res.push(route);
      }
    } else if (route.roles) {
      if (auth.hasRoleOr(route.roles)) {
        res.push(route);
      }
    }
  });
  return res;
};

export const loadView = (view: any, name: string) => {
  let res;
  for (const path in modules) {
    const dir = path.split('views/')[1].split('.vue')[0];
    if (dir === view) {
      res = createCustomNameComponent(modules[path], { name });
    }
  }
  return res;
};

// 非setup
export const usePermissionStoreHook = () => {
  return usePermissionStore(store);
};

interface Route {
  name?: string | symbol;
  path: string;
  children?: Route[];
}

/**
 * 检查路由name是否重复
 * @param localRoutes 本地路由
 * @param routes 动态路由
 */
function duplicateRouteChecker(localRoutes: Route[], routes: Route[]) {
  // 展平
  function flatRoutes(routes: Route[]) {
    const res: Route[] = [];
    routes.forEach((route) => {
      if (route.children) {
        res.push(...flatRoutes(route.children));
      } else {
        res.push(route);
      }
    });
    return res;
  }

  const allRoutes = flatRoutes([...localRoutes, ...routes]);

  const nameList: string[] = [];
  allRoutes.forEach((route) => {
    const name = route.name.toString();
    if (name && nameList.includes(name)) {
      const message = `路由名称: [${name}] 重复, 会造成 404`;
      console.error(message);
      ElNotification({
        title: '路由名称重复',
        message,
        type: 'error'
      });
      return;
    }
    nameList.push(route.name.toString());
  });
}

export default usePermissionStore;
