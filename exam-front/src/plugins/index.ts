import modal from './modal';
import tab from './tab';
import download from './download';
import cache from './cache';
import auth from './auth';
// 预设动画
import animate from '@/animate';

import { download as dl } from '@/utils/request';
import { useDict } from '@/utils/dict';
import { getConfigKey, updateConfigByKey } from '@/api/system/config';
import { parseTime, addDateRange, handleTree, selectDictLabel, selectDictLabels } from '@/utils/ruoyi';

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

  // 全局方法挂载
  app.config.globalProperties.useDict = useDict;
  app.config.globalProperties.getConfigKey = getConfigKey;
  app.config.globalProperties.updateConfigByKey = updateConfigByKey;
  app.config.globalProperties.download = dl;
  app.config.globalProperties.parseTime = parseTime;
  app.config.globalProperties.handleTree = handleTree;
  app.config.globalProperties.addDateRange = addDateRange;
  app.config.globalProperties.selectDictLabel = selectDictLabel;
  app.config.globalProperties.selectDictLabels = selectDictLabels;
  app.config.globalProperties.animate = animate;
}
