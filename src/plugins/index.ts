import modal from './modal';
import tab from './tab';
import download from './download';
import cache from './cache';
import auth from './auth';

import { App } from 'vue';

export default function installPlugin(app: App) {
  // 页签操作
  app.config.globalProperties.$tab = tab;

  // 模态框对象
  app.config.globalProperties.$modal = modal;

  // 缓存对象
  app.config.globalProperties.$cache = cache;

  // 下载文件
  app.config.globalProperties.$download = download;

  // 认证对象
  app.config.globalProperties.$auth = auth;
}
