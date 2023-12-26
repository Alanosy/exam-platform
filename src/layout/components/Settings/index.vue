<template>
  <el-drawer v-model="showSettings" :with-header="false" direction="rtl" size="300px" close-on-click-modal>
    <h3 class="drawer-title">主题风格设置</h3>

    <div class="setting-drawer-block-checbox">
      <div class="setting-drawer-block-checbox-item" @click="handleTheme(SideThemeEnum.DARK)">
        <img src="@/assets/images/dark.svg" alt="dark" />
        <div v-if="sideTheme === 'theme-dark'" class="setting-drawer-block-checbox-selectIcon" style="display: block">
          <i aria-label="图标: check" class="anticon anticon-check">
            <svg viewBox="64 64 896 896" data-icon="check" width="1em" height="1em" :fill="theme" aria-hidden="true" focusable="false" class>
              <path
                d="M912 190h-69.9c-9.8 0-19.1 4.5-25.1 12.2L404.7 724.5 207 474a32 32 0 0 0-25.1-12.2H112c-6.7 0-10.4 7.7-6.3 12.9l273.9 347c12.8 16.2 37.4 16.2 50.3 0l488.4-618.9c4.1-5.1.4-12.8-6.3-12.8z"
              />
            </svg>
          </i>
        </div>
      </div>
      <div class="setting-drawer-block-checbox-item" @click="handleTheme(SideThemeEnum.LIGHT)">
        <img src="@/assets/images/light.svg" alt="light" />
        <div v-if="sideTheme === 'theme-light'" class="setting-drawer-block-checbox-selectIcon" style="display: block">
          <i aria-label="图标: check" class="anticon anticon-check">
            <svg viewBox="64 64 896 896" data-icon="check" width="1em" height="1em" :fill="theme" aria-hidden="true" focusable="false" class>
              <path
                d="M912 190h-69.9c-9.8 0-19.1 4.5-25.1 12.2L404.7 724.5 207 474a32 32 0 0 0-25.1-12.2H112c-6.7 0-10.4 7.7-6.3 12.9l273.9 347c12.8 16.2 37.4 16.2 50.3 0l488.4-618.9c4.1-5.1.4-12.8-6.3-12.8z"
              />
            </svg>
          </i>
        </div>
      </div>
    </div>
    <div class="drawer-item">
      <span>主题颜色</span>
      <span class="comp-style">
        <el-color-picker v-model="theme" :predefine="predefineColors" @change="themeChange" />
      </span>
    </div>
    <div class="drawer-item">
      <span>深色模式</span>
      <span class="comp-style">
        <el-switch v-model="isDark" class="drawer-switch" @change="toggleDark" />
      </span>
    </div>

    <el-divider />

    <h3 class="drawer-title">系统布局配置</h3>

    <div class="drawer-item">
      <span>开启 TopNav</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.topNav" class="drawer-switch" @change="topNavChange" />
      </span>
    </div>

    <div class="drawer-item">
      <span>开启 Tags-Views</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.tagsView" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>固定 Header</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.fixedHeader" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>显示 Logo</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.sidebarLogo" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>动态标题</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.dynamicTitle" class="drawer-switch" @change="dynamicTitleChange" />
      </span>
    </div>

    <el-divider />

    <el-button type="primary" plain icon="DocumentAdd" @click="saveSetting">保存配置</el-button>
    <el-button plain icon="Refresh" @click="resetSetting">重置配置</el-button>
  </el-drawer>
</template>

<script setup lang="ts">
import { useDynamicTitle } from '@/utils/dynamicTitle';
import useAppStore from '@/store/modules/app';
import useSettingsStore from '@/store/modules/settings';
import usePermissionStore from '@/store/modules/permission';
import { handleThemeStyle } from '@/utils/theme';
import { SideThemeEnum } from '@/enums/SideThemeEnum';
import defaultSettings from '@/settings';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const appStore = useAppStore();
const settingsStore = useSettingsStore();
const permissionStore = usePermissionStore();

const showSettings = ref(false);
const theme = ref(settingsStore.theme);
const sideTheme = ref(settingsStore.sideTheme);
const storeSettings = computed(() => settingsStore);
const predefineColors = ref(['#409EFF', '#ff4500', '#ff8c00', '#ffd700', '#90ee90', '#00ced1', '#1e90ff', '#c71585']);

// 是否暗黑模式
const isDark = useDark({
  storageKey: 'useDarkKey',
  valueDark: 'dark',
  valueLight: 'light'
});
// 匹配菜单颜色
watch(isDark, () => {
  if (isDark.value) {
    settingsStore.sideTheme = SideThemeEnum.DARK;
  } else {
    settingsStore.sideTheme = sideTheme.value;
  }
});
const toggleDark = () => useToggle(isDark);

const topNavChange = (val: any) => {
  if (!val) {
    appStore.toggleSideBarHide(false);
    permissionStore.setSidebarRouters(permissionStore.defaultRoutes as any);
  }
};

const dynamicTitleChange = () => {
  // 动态设置网页标题
  useDynamicTitle();
};

const themeChange = (val: string) => {
  settingsStore.theme = val;
  handleThemeStyle(val);
};
const handleTheme = (val: string) => {
  sideTheme.value = val;
  if (isDark.value && val === SideThemeEnum.LIGHT) {
    // 暗黑模式颜色不变
    settingsStore.sideTheme = SideThemeEnum.DARK;
    return;
  }
  settingsStore.sideTheme = val;
};
const saveSetting = () => {
  proxy?.$modal.loading('正在保存到本地，请稍候...');
  const settings = useStorage<LayoutSetting>('layout-setting', defaultSettings);
  settings.value.topNav = storeSettings.value.topNav;
  settings.value.tagsView = storeSettings.value.tagsView;
  settings.value.fixedHeader = storeSettings.value.fixedHeader;
  settings.value.sidebarLogo = storeSettings.value.sidebarLogo;
  settings.value.dynamicTitle = storeSettings.value.dynamicTitle;
  settings.value.sideTheme = storeSettings.value.sideTheme;
  settings.value.theme = storeSettings.value.theme;
  setTimeout(() => {
    proxy?.$modal.closeLoading();
  }, 1000);
};
const resetSetting = () => {
  proxy?.$modal.loading('正在清除设置缓存并刷新，请稍候...');
  useStorage<any>('layout-setting', null).value = null;
  setTimeout('window.location.reload()', 1000);
};
const openSetting = () => {
  showSettings.value = true;
};

defineExpose({
  openSetting
});
</script>

<style lang="scss" scoped>
.setting-drawer-title {
  margin-bottom: 12px;
  color: rgba(0, 0, 0, 0.85);
  line-height: 22px;
  font-weight: bold;
  .drawer-title {
    font-size: 14px;
  }
}
.setting-drawer-block-checbox {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-top: 10px;
  margin-bottom: 20px;

  .setting-drawer-block-checbox-item {
    position: relative;
    margin-right: 16px;
    border-radius: 2px;
    cursor: pointer;

    img {
      width: 48px;
      height: 48px;
    }

    .custom-img {
      width: 48px;
      height: 38px;
      border-radius: 5px;
      box-shadow: 1px 1px 2px #898484;
    }

    .setting-drawer-block-checbox-selectIcon {
      position: absolute;
      top: 0;
      right: 0;
      width: 100%;
      height: 100%;
      padding-top: 15px;
      padding-left: 24px;
      color: #1890ff;
      font-weight: 700;
      font-size: 14px;
    }
  }
}

.drawer-item {
  padding: 12px 0;
  font-size: 14px;

  .comp-style {
    float: right;
    margin: -3px 8px 0px 0px;
  }
}
</style>
