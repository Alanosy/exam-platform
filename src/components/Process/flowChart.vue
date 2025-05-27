<template>
  <div>
    <div :style="'height:' + height">
      <iframe :src="iframeUrl" style="width: 100%; height: 100%" />
    </div>
  </div>
</template>

<script setup lang="ts">
import '@logicflow/core/lib/style/index.css';
import { getToken } from '@/utils/auth';

// Props 定义方式变化
const props = defineProps({
  insId: {
    type: [String, Number],
    default: null
  }
});

const height = document.documentElement.clientHeight - 94.5 + 'px';
const iframeUrl = ref('');
const baseUrl = import.meta.env.VITE_APP_BASE_API;

onMounted(async () => {
  const url = baseUrl + `/warm-flow-ui/index.html?id=${props.insId}&type=FlowChart`;
  iframeUrl.value = url + '&Authorization=Bearer ' + getToken() + '&clientid=' + import.meta.env.VITE_APP_CLIENT_ID;
});
</script>
