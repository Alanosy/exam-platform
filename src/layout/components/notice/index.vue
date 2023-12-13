<template>
  <div v-loading="state.loading" class="layout-navbars-breadcrumb-user-news">
    <div class="head-box">
      <div class="head-box-title">通知公告</div>
      <div class="head-box-btn" @click="readAll">全部已读</div>
    </div>
    <div v-loading="state.loading" class="content-box">
      <template v-if="newsList.length > 0">
        <div v-for="(v, k) in newsList" :key="k" class="content-box-item" @click="onNewsClick(k)">
          <div class="item-conten">
            <div>{{ v.message }}</div>
            <div class="content-box-msg"></div>
            <div class="content-box-time">{{ v.time }}</div>
          </div>
          <!-- 已读/未读 -->
          <span v-if="v.read" class="el-tag el-tag--success el-tag--mini read">已读</span>
          <span v-else class="el-tag el-tag--danger el-tag--mini read">未读</span>
        </div>
      </template>
      <el-empty v-else :description="'消息为空'"></el-empty>
    </div>
    <div v-if="newsList.length > 0" class="foot-box" @click="onGoToGiteeClick">前往gitee</div>
  </div>
</template>

<script setup lang="ts" name="layoutBreadcrumbUserNews">
import { storeToRefs } from 'pinia';
import useNoticeStore from '@/store/modules/notice';

const noticeStore = storeToRefs(useNoticeStore());
const { readAll } = useNoticeStore();

// 定义变量内容
const state = reactive({
  loading: false
});
const newsList = ref([]) as any;

/**
 * 初始化数据
 * @returns
 */
const getTableData = async () => {
  state.loading = true;
  newsList.value = noticeStore.state.value.notices;
  state.loading = false;
};

//点击消息，写入已读
const onNewsClick = (item: any) => {
  newsList.value[item].read = true;
  //并且写入pinia
  noticeStore.state.value.notices = newsList.value;
};

// 前往通知中心点击
const onGoToGiteeClick = () => {
  window.open('https://gitee.com/dromara/RuoYi-Vue-Plus/tree/5.X/');
};

onMounted(() => {
  nextTick(() => {
    getTableData();
  });
});
</script>

<style scoped lang="scss">
.layout-navbars-breadcrumb-user-news {
  .head-box {
    display: flex;
    border-bottom: 1px solid var(--el-border-color-lighter);
    box-sizing: border-box;
    color: var(--el-text-color-primary);
    justify-content: space-between;
    height: 35px;
    align-items: center;
    .head-box-btn {
      color: var(--el-color-primary);
      font-size: 13px;
      cursor: pointer;
      opacity: 0.8;
      &:hover {
        opacity: 1;
      }
    }
  }
  .content-box {
    height: 300px;
    overflow: auto;
    font-size: 13px;
    .content-box-item {
      padding-top: 12px;
      display: flex;
      &:last-of-type {
        padding-bottom: 12px;
      }
      .content-box-msg {
        color: var(--el-text-color-secondary);
        margin-top: 5px;
        margin-bottom: 5px;
      }
      .content-box-time {
        color: var(--el-text-color-secondary);
      }
      .item-conten {
        width: 100%;
        display: flex;
        flex-direction: column;
      }
    }
  }
  .foot-box {
    height: 35px;
    color: var(--el-color-primary);
    font-size: 13px;
    cursor: pointer;
    opacity: 0.8;
    display: flex;
    align-items: center;
    justify-content: center;
    border-top: 1px solid var(--el-border-color-lighter);
    &:hover {
      opacity: 1;
    }
  }
  :deep(.el-empty__description p) {
    font-size: 13px;
  }
}
</style>
