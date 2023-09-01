import { defineStore } from 'pinia';
import defaultSettings from '@/settings';
import { SettingTypeEnum } from '@/enums/SettingTypeEnum';
import { useDynamicTitle } from '@/utils/dynamicTitle';
import { Ref } from 'vue';

export const useSettingsStore = defineStore('setting', () => {
  const storageSetting = JSON.parse(localStorage.getItem('layout-setting') || '{}');

  const prop: { [key: string]: Ref<any> } = {
    title: ref<string>(''),
    theme: ref<string>(storageSetting.theme || defaultSettings.theme),
    sideTheme: ref<string>(storageSetting.sideTheme || defaultSettings.sideTheme),
    showSettings: ref<boolean>(storageSetting.showSettings || defaultSettings.showSettings),
    topNav: ref<boolean>(storageSetting.topNav === undefined ? defaultSettings.topNav : storageSetting.topNav),
    tagsView: ref<boolean>(storageSetting.tagsView === undefined ? defaultSettings.tagsView : storageSetting.tagsView),
    fixedHeader: ref<boolean>(storageSetting.fixedHeader === undefined ? defaultSettings.fixedHeader : storageSetting.fixedHeader),
    sidebarLogo: ref<boolean>(storageSetting.sidebarLogo === undefined ? defaultSettings.sidebarLogo : storageSetting.sidebarLogo),
    dynamicTitle: ref<boolean>(storageSetting.dynamicTitle === undefined ? defaultSettings.dynamicTitle : storageSetting.dynamicTitle),
    animationEnable: ref<boolean>(storageSetting.animationEnable === undefined ? defaultSettings.animationEnable : storageSetting.animationEnable),
    dark: ref<boolean>(storageSetting.dark || defaultSettings.dark)
  };

  const { title, theme, sideTheme, showSettings, topNav, tagsView, fixedHeader, sidebarLogo, dynamicTitle, animationEnable, dark } = prop;

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
