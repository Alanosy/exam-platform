<template>
  <div v-loading="loading" class="social-login"></div>
</template>

<script setup lang="ts">
import {socialLogin} from '@/api/login';
import {setToken} from '@/utils/auth';

const route = useRoute();
const router = useRouter();


/**
 * 接收Route传递的参数
 * @param {Object} route.query.
 */
const code = route.query.code;
const state = route.query.state;
const source = route.query.source as string;
const loading = ref(true);
await socialLogin(source, code, state)
  .then(async (res) => {
    if (res.code !== 200) {
      ElMessage.error(res.msg);
      router.go(-2);
      return;
    }
    loading.value = false;
    setToken(res.msg);
    ElMessage.success('登录成功');
    router.go(-2);
  })
  .catch(() => {
    loading.value = false;
  });
</script>
