// 自定义国际化配置
import { createI18n } from 'vue-i18n';

// 本地语言包
import enUSLocale from './en_US';
import zhCNLocale from './zh_CN';
import Cookies from 'js-cookie';

const messages = {
  zh_CN: {
    ...zhCNLocale
  },
  en_US: {
    ...enUSLocale
  }
};

/**
 * 获取当前系统使用语言字符串
 * @returns zh-cn|en ...
 */
export const getLanguage = () => {
  // 本地缓存获取
  let language = Cookies.get('language');
  if (language) {
    return language;
  }
  // 浏览器使用语言
  language = navigator.language.toLowerCase();
  const locales = Object.keys(messages);
  for (const locale of locales) {
    if (language.indexOf(locale) > -1) {
      return locale;
    }
  }
  return 'zh_CN';
};

const i18n = createI18n({
  legacy: false,
  locale: getLanguage(),
  messages
});

export default i18n;
