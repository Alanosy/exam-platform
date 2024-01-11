import VueI18nPlugin from '@intlify/unplugin-vue-i18n/vite';
export default (path: any) => {
  return VueI18nPlugin({
    include: [path.resolve(__dirname, '../../src/lang/**.json')]
  });
};
