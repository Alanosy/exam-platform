<template>
  <el-drawer v-model="showSettings" :with-header="false" direction="rtl" size="300px" close-on-click-modal>
    <h3 class="drawer-title">菜单导航设置</h3>
    <div class="nav-wrap">
      <el-tooltip content="左侧菜单" placement="bottom">
        <div
          class="item left"
          @click="handleNavType(NavTypeEnum.LEFT)"
          :style="{ '--theme': theme }"
          :class="{ activeItem: navType == NavTypeEnum.LEFT }"
        >
          <b></b><b></b>
        </div>
      </el-tooltip>

      <el-tooltip content="混合菜单" placement="bottom">
        <div
          class="item mix"
          @click="handleNavType(NavTypeEnum.MIX)"
          :style="{ '--theme': theme }"
          :class="{ activeItem: navType == NavTypeEnum.MIX }"
        >
          <b></b><b></b>
        </div>
      </el-tooltip>
      <el-tooltip content="顶部菜单" placement="bottom">
        <div
          class="item top"
          @click="handleNavType(NavTypeEnum.TOP)"
          :style="{ '--theme': theme }"
          :class="{ activeItem: navType == NavTypeEnum.TOP }"
        >
          <b></b><b></b>
        </div>
      </el-tooltip>
    </div>

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
    <div class="drawer-item">
      <span>页面圆角</span>
      <span class="comp-style">
        <el-slider v-model="radiusBase" :min="0" :max="32" :step="2" style="width: 120px" @change="radiusBaseChange" />
      </span>
    </div>

    <el-divider />

    <h3 class="drawer-title">系统布局配置</h3>

    <div class="drawer-item">
      <span>开启 Tags-Views</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.tagsView" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>显示页签图标</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.tagsIcon" :disabled="!settingsStore.tagsView" class="drawer-switch" />
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
import { useAppStore } from '@/store/modules/app';
import { useSettingsStore } from '@/store/modules/settings';
import { usePermissionStore } from '@/store/modules/permission';
import { handleThemeStyle } from '@/utils/theme';
import { SideThemeEnum } from '@/enums/SideThemeEnum';
import { NavTypeEnum } from '@/enums/NavTypeEnum';
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
const navType = ref(settingsStore.navType);
const radiusBase = ref(settingsStore.radiusBase);
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

/** 菜单导航设置 */
watch(
  () => navType,
  (val: string) => {
    if (val.value === NavTypeEnum.TOP) {
      appStore.toggleSideBarHide(true);
      permissionStore.setSidebarRouters(permissionStore.defaultRoutes as any);
    } else if (val.value === NavTypeEnum.LEFT) {
      appStore.toggleSideBarHide(false);
      permissionStore.setSidebarRouters(permissionStore.defaultRoutes as any);
    } else if (val.value === NavTypeEnum.MIX) {
      appStore.toggleSideBarHide(false);
    }
  },
  { immediate: true, deep: true }
);

const handleNavType = (val: NavTypeEnum) => {
  settingsStore.navType = val;
  navType.value = val;
};

const dynamicTitleChange = () => {
  // 动态设置网页标题
  useDynamicTitle();
};

const themeChange = (val: string) => {
  settingsStore.theme = val;
  handleThemeStyle(val);
};
const radiusBaseChange = (val: number) => {
  settingsStore.radiusBase = val;
  const el = document.documentElement;
  el.style.setProperty('--app-radius-base', `${val}px`);
  el.style.setProperty('--app-radius-sm', `${Math.round(val * 0.6)}px`);
  el.style.setProperty('--app-radius-md', `${val}px`);
  el.style.setProperty('--app-radius-lg', `${Math.round(val * 1.4)}px`);
  el.style.setProperty('--el-border-radius-base', `${val}px`);
  el.style.setProperty('--el-border-radius-small', `${Math.round(val * 0.6)}px`);
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
  settings.value.tagsView = storeSettings.value.tagsView;
  settings.value.tagsIcon = storeSettings.value.tagsIcon;
  settings.value.fixedHeader = storeSettings.value.fixedHeader;
  settings.value.sidebarLogo = storeSettings.value.sidebarLogo;
  settings.value.dynamicTitle = storeSettings.value.dynamicTitle;
  settings.value.sideTheme = storeSettings.value.sideTheme;
  settings.value.theme = storeSettings.value.theme;
  settings.value.navType = storeSettings.value.navType;
  settings.value.radiusBase = storeSettings.value.radiusBase;
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

onMounted(() => {
  radiusBaseChange(storeSettings.value.radiusBase);
});

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

// 导航模式
.nav-wrap {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-top: 10px;
  margin-bottom: 20px;

  .activeItem {
    border: 2px solid #{'var(--theme)'} !important;
  }

  .item {
    position: relative;
    margin-right: 16px;
    cursor: pointer;
    width: 56px;
    height: 48px;
    border-radius: 4px;
    background: #f0f2f5;
    border: 2px solid transparent;
  }

  .left {
    b:first-child {
      display: block;
      height: 30%;
      background: #fff;
    }
    b:last-child {
      width: 30%;
      background: #1b2a47;
      position: absolute;
      height: 100%;
      top: 0;
      border-radius: 4px 0 0 4px;
    }
  }
  .mix {
    b:first-child {
      border-radius: 4px 4px 0 0;
      display: block;
      height: 30%;
      background: #1b2a47;
    }
    b:last-child {
      width: 30%;
      background: #1b2a47;
      position: absolute;
      height: 70%;
      border-radius: 0 0 0 4px;
    }
  }
  .top {
    b:first-child {
      display: block;
      height: 30%;
      background: #1b2a47;
      border-radius: 4px 4px 0 0;
    }
  }
}
</style>
