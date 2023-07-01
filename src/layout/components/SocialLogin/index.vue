<template>
  <div v-loading="loading" class="social-login"></div>
</template>

<script setup lang="ts">
import { socialLogin } from '@/api/login';
import { setToken } from '@/utils/auth';
import Cookies from 'js-cookie';
import { getToken } from '@/utils/auth';
import router from '@/router';

const route = useRoute();
const loading = ref(true);


/**
 * 接收Route传递的参数
 * @param {Object} route.query.
 */
const code = route.query.code;
const state = route.query.state;
const source = route.query.source as string;
const tenantId = Cookies.get("tenantId") ? Cookies.get("tenantId") as string : '000000';

/**
 * 通过code获取token
 * @param {string} source
 * @param {string} code
 * @param {string} state
 */
await socialLogin(source, tenantId, code, state)
  .then(async (res) => {
    if (res.code !== 200) {
      ElMessage.error(res.msg);
      location.href = import.meta.env.VITE_APP_CONTEXT_PATH + 'index';
      return;
    }
    loading.value = false;
    setToken(res.data.access_token)
    ElMessage.success(res.msg);
    location.href = import.meta.env.VITE_APP_CONTEXT_PATH + 'index';
  })
  .catch(() => {
    loading.value = false;
  });
</script>
