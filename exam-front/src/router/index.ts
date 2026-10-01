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
  },
  {
    // 监考中心：发布者查看自己考试里考生的切屏 / 复制粘贴 / 摄像头抓拍等防作弊记录
    // 后台菜单（path=proctor）会生成 /proctor，这里再注册一份 /system/proctor，
    // 方便从考试管理列表直接带 examId 跳进来
    path: '/system/proctor',
    component: Layout,
    hidden: true,
    redirect: 'noRedirect',
    children: [
      {
        path: '',
        component: () => import('@/views/system/proctor/index.vue'),
        name: 'SystemProctor',
        meta: { title: '监考中心', noCache: true }
      }
    ]
  },
  {
    // 证书管理：维护证书模板、查看与吊销已颁发的证书
    // 后台菜单（path=cert）会生成 /cert，这里再注册一份 /system/cert
    path: '/system/cert',
    component: Layout,
    hidden: true,
    redirect: 'noRedirect',
    children: [
      {
        path: '',
        component: () => import('@/views/system/cert/index.vue'),
        name: 'SystemCert',
        meta: { title: '证书管理', noCache: true }
      }
    ]
  },
  {
    // 阅卷管理：先按考试看有哪些卷要阅，再点进去看作答用户，最后逐题打分
    path: '/system/mark',
    component: Layout,
    hidden: true,
    redirect: 'noRedirect',
    children: [
      {
        path: '',
        component: () => import('@/views/system/mark/index.vue'),
        name: 'SystemMark',
        meta: { title: '阅卷管理', noCache: true }
      },
      {
        // 某场考试下的答卷列表
        path: 'record',
        component: () => import('@/views/system/mark/record.vue'),
        name: 'SystemMarkRecord',
        meta: { title: '答卷列表', activeMenu: '/system/mark', noCache: true }
      },
      {
        // 逐题阅卷打分
        path: 'marking',
        component: () => import('@/views/system/mark/marking.vue'),
        name: 'SystemMarking',
        meta: { title: '阅卷打分', activeMenu: '/system/mark', noCache: true }
      }
    ]
  },
  {
    // 考生通过公开链接加入考试：/exam/join/{joinCode}
    // 不带 Layout，独立整页展示；未登录时由 permission.ts 的路由守卫拦到 /login?redirect=...
    path: '/exam/join/:code',
    component: () => import('@/views/exam/join/index.vue'),
    name: 'ExamJoin',
    hidden: true,
    meta: { title: '加入考试', noCache: true }
  },
  {
    // 考试中心：查看自己参加的所有考试
    path: '/exam/center',
    component: Layout,
    hidden: true,
    redirect: 'noRedirect',
    children: [
      {
        path: '',
        component: () => import('@/views/exam/center/index.vue'),
        name: 'ExamCenter',
        meta: { title: '考试中心', noCache: true }
      }
    ]
  },
  {
    // 开考前的考试说明确认页
    path: '/exam/brief/:examId',
    component: () => import('@/views/exam/brief/index.vue'),
    name: 'ExamBrief',
    hidden: true,
    meta: { title: '考试须知', noCache: true }
  },
  {
    // 答题页：全屏专注，交卷后同页切换到成绩视图
    path: '/exam/answer/:recordId',
    component: () => import('@/views/exam/answer/index.vue'),
    name: 'ExamAnswer',
    hidden: true,
    meta: { title: '在线答题', noCache: true }
  },
  {
    // 成绩页：单独进入某份答卷的成绩（答题页交卷后也在本页展示成绩）
    path: '/exam/result/:recordId',
    component: () => import('@/views/exam/answer/index.vue'),
    name: 'ExamResult',
    hidden: true,
    meta: { title: '考试成绩', noCache: true }
  },
  {
    // 考试记录：我参加过的每一次考试，一行一次答卷
    path: '/exam/records',
    component: Layout,
    hidden: true,
    redirect: 'noRedirect',
    children: [
      {
        path: '',
        component: () => import('@/views/exam/records/index.vue'),
        name: 'ExamRecords',
        meta: { title: '考试记录', noCache: true }
      }
    ]
  },
  {
    // 我的证书：考试及格后自动颁发的证书，可查看与打印
    path: '/exam/certs',
    component: Layout,
    hidden: true,
    redirect: 'noRedirect',
    children: [
      {
        path: '',
        component: () => import('@/views/exam/certs/index.vue'),
        name: 'ExamMyCerts',
        meta: { title: '我的证书', noCache: true }
      }
    ]
  },
  {
    // 答题记录详情：逐题看自己的作答、正确答案、解析与得分
    path: '/exam/record/:recordId',
    component: () => import('@/views/exam/records/detail.vue'),
    name: 'ExamRecordDetail',
    hidden: true,
    meta: { title: '答题记录', noCache: true }
  },
  {
    // 错题本：先按考试记录分页统计，点进去才看该场考试的错题明细
    path: '/exam/wrong',
    component: Layout,
    hidden: true,
    redirect: 'noRedirect',
    children: [
      {
        path: '',
        component: () => import('@/views/exam/wrong/index.vue'),
        name: 'ExamWrongBook',
        meta: { title: '错题本', noCache: true }
      },
      {
        // 错题明细：某一场考试 / 某一份练习下的错题列表，挂在 Layout 下才有侧边栏与导航
        path: 'detail',
        component: () => import('@/views/exam/wrong/detail.vue'),
        name: 'ExamWrongDetail',
        meta: { title: '错题明细', noCache: true }
      }
    ]
  },
  {
    // 重刷错题：逐题作答、即时判对错、看答案解析
    path: '/exam/wrong/practice',
    component: () => import('@/views/exam/wrong/practice.vue'),
    name: 'ExamWrongPractice',
    hidden: true,
    meta: { title: '重刷错题', noCache: true }
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
