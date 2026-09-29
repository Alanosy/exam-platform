import { createWebHistory, createRouter, RouteRecordRaw } from 'vue-router';
/* Layout */
import Layout from '@/layout/index.vue';

/**
 * Note: 路由配置项
 *
 * hidden: true                     // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true                 // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 *                                  // 只有一个时，会将那个子路由当做根路由显示在侧边栏--如引导页面
 *                                  // 若你想不管路由下面的 children 声明的个数都显示你的根路由
 *                                  // 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，一直显示根路由
 * redirect: noRedirect             // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'               // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * query: '{"id": 1, "name": "ry"}' // 访问路由的默认传递参数
 * roles: ['admin', 'common']       // 访问路由的角色权限
 * permissions: ['a:a:a', 'b:b:b']  // 访问路由的菜单权限
 * meta : {
    noCache: true                   // 如果设置为true，则不会被 <keep-alive> 缓存(默认 false)
    title: 'title'                  // 设置该路由在侧边栏和面包屑中展示的名字
    icon: 'svg-name'                // 设置该路由的图标，对应路径src/assets/icons/svg
    breadcrumb: false               // 如果设置为false，则不会在breadcrumb面包屑中显示
    activeMenu: '/system/user'      // 当路由设置了该属性，则会高亮相对应的侧边栏。
  }
 */

// 公共路由
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/social-callback',
    hidden: true,
    component: () => import('@/layout/components/SocialCallback/index.vue')
  },
  {
    path: '/login',
    component: () => import('@/views/login.vue'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register.vue'),
    hidden: true
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/error/404.vue'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401.vue'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: '/index',
        component: () => import('@/views/index.vue'),
        name: 'Index',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index.vue'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },
  {
    // 试题编辑整页（新增 / 修改复用同一个页面）
    // activeMenu 指向后台「试题管理」菜单的路由地址，需要与实际菜单保持一致
    path: '/system/question/edit/:questionId?',
    component: Layout,
    hidden: true,
    redirect: 'noRedirect',
    children: [
      {
        path: '',
        component: () => import('@/views/system/question/edit.vue'),
        name: 'QuestionEdit',
        // noCache：试题编辑页不进 keep-alive 缓存。
        // 该路由是动态路由，关闭页签时缓存不会被清理，复用实例会导致「点新增还残留上一道题的内容」。
        meta: { title: '试题编辑', activeMenu: '/system/question', noCache: true }
      }
    ]
  },
  {
    // 试卷组卷整页（新增 / 编辑复用同一个页面）
    // activeMenu 指向后台「试卷管理」菜单的路由地址，需要与实际菜单保持一致
    path: '/system/paper/edit/:paperId?',
    component: Layout,
    hidden: true,
    redirect: 'noRedirect',
    children: [
      {
        path: '',
        component: () => import('@/views/system/paper/edit/index.vue'),
        name: 'PaperEdit',
        // noCache：不进 keep-alive 缓存，避免新增时残留上一份试卷的录入内容
        meta: { title: '试卷组卷', activeMenu: '/system/paper', noCache: true }
      }
    ]
  },
  {
    // 考试配置整页（新增 / 编辑复用同一个页面）
    // activeMenu 指向后台「考试管理」菜单的路由地址，需要与实际菜单保持一致
    path: '/system/exam/edit/:examId?',
    component: Layout,
    hidden: true,
    redirect: 'noRedirect',
    children: [
      {
        path: '',
        component: () => import('@/views/system/exam/edit/index.vue'),
        name: 'ExamEdit',
        // noCache：不进 keep-alive 缓存，避免新增时残留上一场考试的配置
        meta: { title: '考试配置', activeMenu: '/system/exam', noCache: true }
      }
    ]
  }
];

// 动态路由，基于用户权限动态去加载
export const dynamicRoutes: RouteRecordRaw[] = [];

/**
 * 创建路由
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.VITE_APP_CONTEXT_PATH),
  routes: constantRoutes,
  // 刷新时，滚动条位置还原
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }
    return { top: 0 };
  }
});

export default router;
