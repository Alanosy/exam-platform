<template>
  <div>
    <template v-for="(item, index) in options">
      <template v-if="values.includes(item.value)">
        <span v-if="item.elTagType == 'default' || item.elTagType == ''" :key="item.value" :index="index" :class="item.elTagClass">
          {{ item.label + " " }}
        </span>
        <el-tag
          v-else
          :disable-transitions="true"
          :key="item.value + ''"
          :index="index"
          :type="item.elTagType === 'primary' ? '' : item.elTagType"
          :class="item.elTagClass"
        >
          {{ item.label + " " }}
        </el-tag>
      </template>
    </template>
    <template v-if="unmatch && showValue">
      {{ unmatchArray }}
    </template>
  </div>
</template>

<script setup lang="ts">
import { PropType } from 'vue';


const props = defineProps({
  // 数据
  options: {
    type: Array as PropType<DictDataOption[]>,
    default: null,
  },
  // 当前的值
  value: [Number, String, Array] as PropType<number | string | Array<number | string>>,
  // 当未找到匹配的数据时，显示value
  showValue: {
    type: Boolean as PropType<boolean>,
    default: true,
  },
});

const values = computed(() => {
  if (props.value !== null && typeof props.value !== "undefined") {
    return Array.isArray(props.value) ? props.value : [String(props.value)];
  } else {
    return [];
  }
});

const unmatch = computed(() => {
  if (props.value !== null && typeof props.value !== "undefined") {
    // 传入值为非数组
    if (!Array.isArray(props.value)) {
      if (props.options.some((v) => v.value == props.value)) {
        return false;
      }
      return true;
    }
    return true;
  }
  // 没有value不显示
  return false;
});

const unmatchArray = computed(() => {
// 记录未匹配的项
  const itemUnmatchArray: Array<string | number> = [];
  if (props.value !== null && typeof props.value !== "undefined") {
    // 传入值为非数组
    if (!Array.isArray(props.value)) {
      itemUnmatchArray.push(props.value);
    } else {
      // 传入值为Array
      props.value.forEach((item) => {
        if (!props.options.some((v) => v.value == item)) {
          itemUnmatchArray.push(item);
        }
      });
    }
  }
  // 没有value不显示
  return handleArray(itemUnmatchArray);
});

const handleArray = (array: Array<string | number>) => {
  if (array.length === 0) return "";
  return array.reduce((pre, cur) => {
    return pre + " " + cur;
  });
}
</script>

<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}
</style>
