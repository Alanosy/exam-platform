<template>
  <div>
    <template v-for="(item, index) in options">
      <template v-if="values.includes(item.value)">
        <span v-if="(item.elTagType == 'default' || item.elTagType == '') && (item.elTagClass == '' || item.elTagClass == null)"
              :key="item.value" :index="index" :class="item.elTagClass">
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
import { propTypes } from '@/utils/propTypes';


const props = defineProps({
  // 数据
  options: {
    type: Array as PropType<DictDataOption[]>,
    default: null,
  },
  // 当前的值
  value: [Number, String, Array] as PropType<number | string | Array<number | string>>,
  // 当未找到匹配的数据时，显示value
  showValue: propTypes.bool.def(true),
  separator: propTypes.string.def(","),
});
const values = computed(() => {
  if (props.value === '' || props.value === null || typeof props.value === "undefined") return []
  return Array.isArray(props.value) ? props.value.map(item => '' + item) : String(props.value).split(props.separator);
});

const unmatch = computed(() => {
  if (props.options?.length == 0 || props.value === '' || props.value === null || typeof props.value === "undefined") return false
  // 传入值为非数组
  values.value.forEach(item => {
    if (!props.options.some(v => v.value === item)) {
      return true // 如果有未匹配项，将标志设置为true
    }
  })
  return false // 返回标志的值
});

const unmatchArray = computed(() => {
// 记录未匹配的项
  const itemUnmatchArray: Array<string | number> = [];
  if (props.value !== '' && props.value !== null && typeof props.value !== "undefined") {
    values.value.forEach(item => {
      if (!props.options.some(v => v.value === item)) {
        itemUnmatchArray.push(item);
      }
    })
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
