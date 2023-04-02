declare type DefaultSettings = {
  /**
   * 网页标题
   */
  title?: string;

  /**
   * 侧边栏主题 theme-dark | theme-light
   */
  sideTheme?: string;

  /**
   * 是否显示系统布局设置
   */
  showSettings?: boolean;

  /**
   * 是否显示顶部导航
   */
  topNav?: boolean;

  /**
   * 是否显示多标签导航
   */
  tagsView?: boolean;
  /**
   * 是否固定头部
   */
  fixedHeader?: boolean;
  /**
   * 是否显示侧边栏Logo
   */
  sidebarLogo?: boolean;
  /**
   * 导航栏布局
   */
  layout?: string;
  /**
   * 主题模式
   */
  theme?: string;

  /**
   * 布局大小
   */
  size?: string;

  /**
   * 语言
   */
  language?: string;

  /**
   * 是否显示动态标题
   */
  dynamicTitle?: boolean;
  /**
   * 是否启用动画效果
   */
  animationEnable?: boolean;
  /**
   *  是否启用暗黑模式
   *
   * true:暗黑模式
   * false: 明亮模式
   */
  dark?: boolean;

  errorLog?: string;
};
