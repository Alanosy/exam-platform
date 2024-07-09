<template>
  <div>
    <el-table :data="auths" style="width: 100%; height: 100%; font-size: 14px">
      <el-table-column label="序号" width="50" type="index" />
      <el-table-column label="绑定账号平台" width="140" align="center" prop="source" show-overflow-tooltip />
      <el-table-column label="头像" width="120" align="center" prop="avatar">
        <template #default="scope">
          <img :src="scope.row.avatar" style="width: 45px; height: 45px" />
        </template>
      </el-table-column>
      <el-table-column label="系统账号" width="180" align="center" prop="userName" :show-overflow-tooltip="true" />
      <el-table-column label="绑定时间" width="180" align="center" prop="createTime" />
      <el-table-column label="操作" width="80" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button size="small" type="text" @click="unlockAuth(scope.row)">解绑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div id="git-user-binding">
      <h4 class="provider-desc">你可以绑定以下第三方帐号</h4>
      <div id="authlist" class="user-bind">
        <a class="third-app" href="#" title="使用 微信 账号授权登录" @click="authUrl('wechat')">
          <div class="git-other-login-icon">
            <svg-icon icon-class="wechat" />
          </div>
          <span class="app-name">WeiXin</span>
        </a>
        <a class="third-app" href="#" title="使用 MaxKey 账号授权登录" @click="authUrl('maxkey')">
          <div class="git-other-login-icon">
            <svg-icon icon-class="maxkey" />
          </div>
          <span class="app-name">MaxKey</span>
        </a>
        <a class="third-app" href="#" title="使用 TopIam 账号授权登录" @click="authUrl('topiam')">
          <div class="git-other-login-icon">
            <svg-icon icon-class="topiam" />
          </div>
          <span class="app-name">TopIam</span>
        </a>
        <a class="third-app" href="#" title="使用 Gitee 账号授权登录" @click="authUrl('gitee')">
          <div class="git-other-login-icon">
            <svg-icon icon-class="gitee" />
          </div>
          <span class="app-name">Gitee</span>
        </a>
        <a class="third-app" href="#" title="使用 GitHub 账号授权登录" @click="authUrl('github')">
          <div class="git-other-login-icon">
            <svg-icon icon-class="github" />
          </div>
          <span class="app-name">Github</span>
        </a>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { authUnlock, authBinding } from '@/api/system/social/auth';
import { propTypes } from '@/utils/propTypes';
import useUserStore from '@/store/modules/user';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const props = defineProps({
  auths: propTypes.any.isRequired
});
const auths = computed(() => props.auths);

const unlockAuth = (row: any) => {
  ElMessageBox.confirm('您确定要解除"' + row.source + '"的账号绑定吗？')
    .then(() => {
      return authUnlock(row.id);
    })
    .then((res: any) => {
      if (res.code === 200) {
        proxy?.$modal.msgSuccess('解绑成功');
        proxy?.$tab.refreshPage();
      } else {
        proxy?.$modal.msgError(res.msg);
      }
    })
    .catch(() => {});
};

const authUrl = (source: string) => {
  authBinding(source, useUserStore().tenantId).then((res: any) => {
    if (res.code === 200) {
      window.location.href = res.data;
    } else {
      proxy?.$modal.msgError(res.msg);
    }
  });
};
</script>

<style type="text/css">
.user-bind .third-app {
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-orient: vertical;
  -webkit-box-direction: normal;
  -ms-flex-direction: column;
  flex-direction: column;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  min-width: 80px;
  float: left;
}

.user-bind {
  font-size: 1rem;
  text-align: start;
  height: 50px;
  margin-top: 10px;
}

.git-other-login-icon > img {
  height: 32px;
}

a {
  text-decoration: none;
  cursor: pointer;
  color: #005980;
}

.provider-desc {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol',
    'Liberation Sans', 'PingFang SC', 'Microsoft YaHei', 'Hiragino Sans GB', 'Wenquanyi Micro Hei', 'WenQuanYi Zen Hei', 'ST Heiti', SimHei, SimSun,
    'WenQuanYi Zen Hei Sharp', sans-serif;
  font-size: 1.071rem;
}

td > img {
  height: 20px;
  width: 20px;
  display: inline-block;
  border-radius: 50%;
  margin-right: 5px;
}
</style>
