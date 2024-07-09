<template>
  <div v-loading="loading" class="social-callback"></div>
</template>

<script setup lang="ts">
import { login, callback } from '@/api/login';
import { setToken, getToken } from '@/utils/auth';
import { LoginData } from '@/api/types';

const route = useRoute();
const loading = ref(true);

/**
 * 接收Route传递的参数
 * @param {Object} route.query.
 */
const code = route.query.code as string;
const state = route.query.state as string;
const source = route.query.source as string;
const stateJson = JSON.parse(atob(state));
const tenantId = (stateJson.tenantId as string) ? (stateJson.tenantId as string) : '000000';
const domain = stateJson.domain as string;

const processResponse = async (res: any) => {
  if (res.code !== 200) {
    throw new Error(res.msg);
  }
  if (res.data !== null) {
    setToken(res.data.access_token);
  }
  ElMessage.success(res.msg);
  setTimeout(() => {
    location.href = import.meta.env.VITE_APP_CONTEXT_PATH + 'index';
  }, 2000);
};

const handleError = (error: any) => {
  ElMessage.error(error.message);
  setTimeout(() => {
    location.href = import.meta.env.VITE_APP_CONTEXT_PATH + 'index';
  }, 2000);
};

const callbackByCode = async (data: LoginData) => {
  try {
    const res = await callback(data);
    await processResponse(res);
    loading.value = false;
  } catch (error) {
    handleError(error);
  }
};

const loginByCode = async (data: LoginData) => {
  try {
    const res = await login(data);
    await processResponse(res);
    loading.value = false;
  } catch (error) {
    handleError(error);
  }
};

const init = async () => {
  // 如果域名不相等 则重定向处理
  let host = window.location.host;
  if (domain !== host) {
    let urlFull = new URL(window.location.href);
    urlFull.host = domain;
    window.location.href = urlFull.toString();
    return;
  }

  const data: LoginData = {
    socialCode: code,
    socialState: state,
    tenantId: tenantId,
    source: source,
    clientId: import.meta.env.VITE_APP_CLIENT_ID,
    grantType: 'social'
  };

  if (!getToken()) {
    await loginByCode(data);
  } else {
    await callbackByCode(data);
  }
};

onMounted(() => {
  nextTick(() => {
    init();
  });
});
</script>
