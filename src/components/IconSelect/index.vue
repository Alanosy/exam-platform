<template>
  <div class="relative" :style="{ width: width }">
    <el-input v-model="modelValue" readonly @click="visible = !visible" placeholder="点击选择图标">
      <template #prepend>
        <svg-icon :icon-class="modelValue as string" />
      </template>
    </el-input>

    <el-popover shadow="none" :visible="visible" placement="bottom-end" trigger="click" :width="450">
      <template #reference>
        <div @click="visible = !visible" class="cursor-pointer text-[#999] absolute right-[10px] top-0 height-[32px] leading-[32px]">
          <i-ep-caret-top v-show="visible"></i-ep-caret-top>
          <i-ep-caret-bottom v-show="!visible"></i-ep-caret-bottom>
        </div>
      </template>

      <el-input class="p-2" v-model="filterValue" placeholder="搜索图标" clearable @input="filterIcons" />

      <el-scrollbar height="w-[200px]">
        <ul class="icon-list">
          <el-tooltip v-for="(iconName, index) in iconNames" :key="index" :content="iconName" placement="bottom" effect="light">
            <li :class="['icon-item', {active: modelValue == iconName}]" @click="selectedIcon(iconName)">
              <svg-icon color="var(--el-text-color-regular)" :icon-class="iconName" />
            </li>
          </el-tooltip>
        </ul>
      </el-scrollbar>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import icons from '@/components/IconSelect/requireIcons';
import { propTypes } from '@/utils/propTypes';

const props = defineProps({
  modelValue: propTypes.string.isRequired,
  width: propTypes.string.def('400px')
});

const emit = defineEmits(['update:modelValue']);
const visible = ref(false);
const { modelValue, width } = toRefs(props);
const iconNames = ref<string[]>(icons);

const filterValue = ref('');

/**
 * 筛选图标
 */
const filterIcons = () => {
  if (filterValue.value) {
    iconNames.value = icons.filter(iconName =>
      iconName.includes(filterValue.value)
    );
  } else {
    iconNames.value = icons;
  }
}
/**
 * 选择图标
 * @param iconName 选择的图标名称
 */
const selectedIcon = (iconName: string) => {
  emit('update:modelValue', iconName);
  visible.value = false;
}
</script>

<style scoped lang="scss">
.el-scrollbar {
  max-height: calc(50vh - 100px)!important;
  overflow-y: auto;
}
.el-divider--horizontal {
  margin: 10px auto !important;
}
.icon-list {
  display: flex;
  flex-wrap: wrap;
  padding-left: 10px;
  margin-top: 10px;

  .icon-item {
    cursor: pointer;
    width: 10%;
    margin: 0 10px 10px 0;
    padding: 5px;
    display: flex;
    flex-direction: column;
    justify-items: center;
    align-items: center;
    border: 1px solid #ccc;
    &:hover {
      border-color: var(--el-color-primary);
      color: var(--el-color-primary);
      transition: all 0.2s;
      transform: scaleX(1.1);
    }
  }
  .active {
      border-color: var(--el-color-primary);
      color: var(--el-color-primary);
    }
}
</style>
