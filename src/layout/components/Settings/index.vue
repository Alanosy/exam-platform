<template>
  <el-drawer v-model="showSettings" :withHeader="false" direction="rtl" size="300px" close-on-click-modal>
    <h3 class="drawer-title">主题风格设置</h3>

    <div class="setting-drawer-block-checbox">
      <div class="setting-drawer-block-checbox-item" @click="handleTheme(SideThemeEnum.DARK)">
        <img src="@/assets/images/dark.svg" alt="dark" />
        <div v-if="sideTheme === 'theme-dark'" class="setting-drawer-block-checbox-selectIcon" style="display: block;">
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
        <div v-if="sideTheme === 'theme-light'" class="setting-drawer-block-checbox-selectIcon" style="display: block;">
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
        <el-switch v-model="isDark" @change="toggleDark" class="drawer-switch" />
      </span>
    </div>

    <el-divider />

    <h3 class="drawer-title">系统布局配置</h3>

    <div class="drawer-item">
      <span>开启 TopNav</span>
      <span class="comp-style">
        <el-switch v-model="topNav" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>开启 Tags-Views</span>
      <span class="comp-style">
        <el-switch v-model="tagsView" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>固定 Header</span>
      <span class="comp-style">
        <el-switch v-model="fixedHeader" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>显示 Logo</span>
      <span class="comp-style">
        <el-switch v-model="sidebarLogo" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>动态标题</span>
      <span class="comp-style">
        <el-switch v-model="dynamicTitle" class="drawer-switch" />
      </span>
    </div>

    <el-divider />

    <el-button type="primary" plain icon="DocumentAdd" @click="saveSetting">保存配置</el-button>
    <el-button plain icon="Refresh" @click="resetSetting">重置配置</el-button>
  </el-drawer>
</template>

<script setup lang="ts">
import { useDynamicTitle } from '@/utils/dynamicTitle'
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'
import { handleThemeStyle } from '@/utils/theme'
import { ComponentInternalInstance } from "vue";
import { SettingTypeEnum } from "@/enums/SettingTypeEnum";
import { SideThemeEnum } from "@/enums/SideThemeEnum";

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const appStore = useAppStore()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()


const showSettings = ref(false);
const theme = ref(settingsStore.theme);
const sideTheme = ref(settingsStore.sideTheme);
const storeSettings = computed(() => settingsStore);
const predefineColors = ref(["#409EFF", "#ff4500", "#ff8c00", "#ffd700", "#90ee90", "#00ced1", "#1e90ff", "#c71585"]);

// 是否暗黑模式
const isDark = useDark({
  storageKey: 'useDarkKey',
  valueDark: 'dark',
  valueLight: 'light',
});
watch(isDark, ()=> {
  if (isDark.value) {
    settingsStore.changeSetting({ key: SettingTypeEnum.SIDE_THEME, value: SideThemeEnum.DARK })
  } else {
    settingsStore.changeSetting({ key: SettingTypeEnum.SIDE_THEME, value: sideTheme.value })
  }
})
const toggleDark = () => useToggle(isDark);

/** 是否需要topNav */
const topNav = computed({
    get: () => storeSettings.value.topNav,
    set: (val) => {
        settingsStore.changeSetting({ key: SettingTypeEnum.TOP_NAV, value: val })
        if (!val) {
            appStore.toggleSideBarHide(false);
            permissionStore.setSidebarRouters(permissionStore.defaultRoutes);
        }
    }
})
/** 是否需要tagview */
const tagsView = computed({
    get: () => storeSettings.value.tagsView,
    set: (val) => {
        settingsStore.changeSetting({ key: SettingTypeEnum.TAGS_VIEW, value: val })
    }
})
/**是否需要固定头部 */
const fixedHeader = computed({
    get: () => storeSettings.value.fixedHeader,
    set: (val) => {
        settingsStore.changeSetting({ key: SettingTypeEnum.FIXED_HEADER, value: val })
    }
})
/**是否需要侧边栏的logo */
const sidebarLogo = computed({
    get: () => storeSettings.value.sidebarLogo,
    set: (val) => {
        settingsStore.changeSetting({ key: SettingTypeEnum.SIDEBAR_LOGO, value: val })
    }
})
/**是否需要侧边栏的动态网页的title */
const dynamicTitle = computed({
    get: () => storeSettings.value.dynamicTitle,
    set: (val) => {
        settingsStore.changeSetting({ key: SettingTypeEnum.DYNAMIC_TITLE, value: val })
        // 动态设置网页标题
        useDynamicTitle()
    }
})

const themeChange = (val: string | null) => {
    settingsStore.changeSetting({ key: SettingTypeEnum.THEME, value: val })
    theme.value = val;
    if (val) {
        handleThemeStyle(val);
    }
}
const handleTheme = (val: string) => {
    sideTheme.value = val;
    if (isDark.value && val === SideThemeEnum.LIGHT) {
      // 暗黑模式颜色不变
      settingsStore.changeSetting({ key: SettingTypeEnum.SIDE_THEME, value: SideThemeEnum.DARK })
      return
    }
    settingsStore.changeSetting({ key: SettingTypeEnum.SIDE_THEME, value: val })
}
const saveSetting = () => {
    proxy?.$modal.loading("正在保存到本地，请稍候...");
    let layoutSetting = {
        "topNav": storeSettings.value.topNav,
        "tagsView": storeSettings.value.tagsView,
        "fixedHeader": storeSettings.value.fixedHeader,
        "sidebarLogo": storeSettings.value.sidebarLogo,
        "dynamicTitle": storeSettings.value.dynamicTitle,
        "sideTheme": storeSettings.value.sideTheme,
        "theme": storeSettings.value.theme
    };
    localStorage.setItem("layout-setting", JSON.stringify(layoutSetting));
    setTimeout(() => {proxy?.$modal.closeLoading()}, 1000)
}
const resetSetting = () => {
    proxy?.$modal.loading("正在清除设置缓存并刷新，请稍候...");
    localStorage.removeItem("layout-setting")
    setTimeout("window.location.reload()", 1000)
}
const openSetting = () => {
    showSettings.value = true;
}

defineExpose({
    openSetting,
})
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
