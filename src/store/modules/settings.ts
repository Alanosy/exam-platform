import { defineStore } from 'pinia';
import defaultSettings from '@/settings';
import { SettingTypeEnum } from '@/enums/SettingTypeEnum';
import { useDynamicTitle } from '@/utils/dynamicTitle';
export const useSettingsStore = defineStore('setting', () => {
  const storageSetting = JSON.parse(localStorage.getItem('layout-setting') || '{}');
  const title = ref<string>(defaultSettings.title);
  const theme = ref<string>(storageSetting.theme || defaultSettings.theme);
  const sideTheme = ref<string>(storageSetting.sideTheme || defaultSettings.sideTheme);
  const showSettings = ref<boolean>(storageSetting.showSettings || defaultSettings.showSettings);
  const topNav = ref<boolean>(storageSetting.topNav === undefined ? defaultSettings.topNav : storageSetting.topNav);
  const tagsView = ref<boolean>(storageSetting.tagsView === undefined ? defaultSettings.tagsView : storageSetting.tagsView);
  const fixedHeader = ref<boolean>(storageSetting.fixedHeader === undefined ? defaultSettings.fixedHeader : storageSetting.fixedHeader);
  const sidebarLogo = ref<boolean>(storageSetting.sidebarLogo === undefined ? defaultSettings.sidebarLogo : storageSetting.sidebarLogo);
  const dynamicTitle = ref<boolean>(storageSetting.dynamicTitle === undefined ? defaultSettings.dynamicTitle : storageSetting.dynamicTitle);
  const animationEnable = ref<boolean>(
    storageSetting.animationEnable === undefined ? defaultSettings.animationEnable : storageSetting.animationEnable
  );
  const dark = ref<boolean>(storageSetting.dark || defaultSettings.dark);

  const prop: { [key: string]: Ref<any> } = {
    theme,
    sideTheme,
    showSettings,
    topNav,
    tagsView,
    fixedHeader,
    sidebarLogo,
    dynamicTitle,
    animationEnable,
    dark
  };

  // actions
  const changeSetting = (param: { key: SettingTypeEnum; value: any }) => {
    const { key, value } = param;
    if (key in prop) {
      prop[key].value = value;
    }
  };
  const setTitle = (value: string) => {
    title.value = value;
    useDynamicTitle();
  };
  return {
    title,
    theme,
    sideTheme,
    showSettings,
    topNav,
    tagsView,
    fixedHeader,
    sidebarLogo,
    dynamicTitle,
    animationEnable,
    dark,
    changeSetting,
    setTitle
  };
});

export default useSettingsStore;
