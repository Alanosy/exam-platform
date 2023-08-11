import UnoCss from 'unocss/vite';
import { presetUno, presetAttributify, presetIcons } from 'unocss';

export default () => {
  return UnoCss({
    presets: [presetUno(), presetAttributify(), presetIcons()],
    // rules: [['search', {}]],
    shortcuts: {
      'panel-title':
        'pb-[5px] font-sans leading-[1.1] font-medium text-base text-[#6379bb] border-b border-b-solid border-[var(--el-border-color-light)] mb-5 mt-0'
    },
    hmrTopLevelAwait: false // unocss默认是true，低版本浏览器是不支持的，启动后会报错
  });
};
