<template>
  <div :class="{ 'hidden': hidden }" class="pagination-container">
    <el-pagination
      :background="background"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :layout="layout"
      :page-sizes="pageSizes"
      :pager-count="pagerCount"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script lang="ts">
export default {
    name: 'Pagination'
}
</script>

<script setup lang="ts">
import { scrollTo } from '@/utils/scroll-to'
import { propTypes } from "@/utils/propTypes";

const props = defineProps({
    total: propTypes.number,
    page: propTypes.number.def(1),
    limit: propTypes.number.def(20),
    pageSizes: {
      type: Array as PropType<number[]>,
      default: () => [10, 20, 30, 50]
    },
    // 移动端页码按钮的数量端默认值5
    pagerCount: propTypes.number.def(document.body.clientWidth < 992 ? 5 : 7),
    layout: propTypes.string.def('total, sizes, prev, pager, next, jumper'),
    background: propTypes.bool.def(true),
    autoScroll: propTypes.bool.def(true),
    hidden: propTypes.bool.def(false),
    float: propTypes.string.def('right')
})

const emit = defineEmits(['update:page', 'update:limit', 'pagination']);
const currentPage = computed({
    get() {
        return props.page
    },
    set(val) {
        emit('update:page', val)
    }
})
const pageSize = computed({
    get() {
        return props.limit
    },
    set(val){
        emit('update:limit', val)
    }
})
function handleSizeChange(val: number) {
    if (currentPage.value * val > props.total) {
        currentPage.value = 1
    }
    emit('pagination', { page: currentPage.value, limit: val })
    if (props.autoScroll) {
        scrollTo(0, 800)
    }
}
function handleCurrentChange(val: number) {
    emit('pagination', { page: val, limit: pageSize.value })
    if (props.autoScroll) {
        scrollTo(0, 800)
    }
}
</script>

<style lang="scss" scoped>
.pagination-container {
  padding: 32px 16px;
  .el-pagination{
    float: v-bind(float);
  }
}
.pagination-container.hidden {
  display: none;
}
</style>
