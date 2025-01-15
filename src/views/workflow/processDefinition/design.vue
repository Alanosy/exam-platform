<template>
  <div ref="container" class="container">
    <iframe ref="iframe" :src="iframeUrl" frameborder="0" :width="sidebarStatusWidth()" height="100%"></iframe>
  </div>
</template>

<script setup name="WarmFlow">

const { proxy } = getCurrentInstance();
import { onMounted } from 'vue';
import { getToken } from '@/utils/auth';

const sidebarStatus = useStorage('sidebarStatus', '1');

console.log(sidebarStatus.value)

const sidebarStatusWidth = () => {
  if (sidebarStatus.value === '0') {
    return "115%";
  }
  return "104%";
}

// definitionId为需要查询的流程定义id，
// disabled为是否可编辑, 例如：查看的时候不可编辑，不可保存
const iframeUrl = ref('');
const baseUrl = import.meta.env.VITE_APP_BASE_API;
const iframeLoaded = () => {
  // iframe监听组件内设计器保存事件
  window.onmessage = (event) => {
    switch (event.data.method) {
      case 'close':
        close();
        break;
    }
  };
};
const open = async (definitionId, disabled) => {
  let url = baseUrl + `/warm-flow-ui/index.html?id=${definitionId}&disabled=${disabled}`;
  iframeUrl.value = url + '&Authorization=Bearer ' + getToken() + '&clientid=' + import.meta.env.VITE_APP_CLIENT_ID;
};
/** 关闭按钮 */
function close() {
  const obj = { path: '/workflow/processDefinition' };
  proxy.$tab.closeOpenPage(obj);
}

onMounted(() => {
  iframeLoaded();
  open(proxy.$route.query.definitionId, proxy.$route.query.disabled);
});
/**
 * 对外暴露子组件方法
 */
defineExpose({
  open
});
</script>

<style scoped>
.container {
  width: 100%;
  height: calc(100vh - 84px);
}
</style>
