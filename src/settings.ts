import { LanguageEnum } from '@/enums/LanguageEnum';

const setting: DefaultSettings = {
  /**
   * 网页标题
   */
  title: import.meta.env.VITE_APP_TITLE,

  theme: '#409EFF',

  /**
   * 侧边栏主题 深色主题theme-dark，浅色主题theme-light
   */
  sideTheme: 'theme-dark',
  /**
   * 是否系统布局配置
   */
  showSettings: true,

  /**
   * 是否显示顶部导航
   */
  topNav: false,

  /**
   * 是否显示 tagsView
   */
  tagsView: true,

  /**
   * 显示页签图标
   */
  tagsIcon: false,

  /**
   * 是否固定头部
   */
  fixedHeader: false,

  /**
   * 是否显示logo
   */
  sidebarLogo: true,

  /**
   * 是否显示动态标题
   */
  dynamicTitle: false,

  /**
   * 是否开启动画 开启随机 关闭渐进渐出
   */
  animationEnable: false,

  /**
   * 是否暗黑模式
   */
  dark: false,

  /**
   * 默认语言
   */
  language: LanguageEnum.zh_CN,

  /**
   * 默认大小
   */
  size: 'default',

  /**
   * 默认布局
   */
  layout: ''
};
export default setting;
