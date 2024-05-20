// 自定义国际化配置
import { createI18n } from 'vue-i18n';

import { LanguageEnum } from '@/enums/LanguageEnum';
import messages from '@intlify/unplugin-vue-i18n/messages';

/**
 * 获取当前语言
 * @returns zh-cn|en ...
 */
export const getLanguage = (): LanguageEnum => {
  const language = useStorage<LanguageEnum>('language', LanguageEnum.zh_CN);
  if (language.value) {
    return language.value;
  }
  return LanguageEnum.zh_CN;
};

const i18n = createI18n({
  globalInjection: true,
  allowComposition: true,
  legacy: false,
  locale: getLanguage(),
  messages
});

export default i18n;
