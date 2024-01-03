import modal from '@/plugins/modal';
import tab from '@/plugins/tab';
import download from '@/plugins/download';
import auth from '@/plugins/auth';
import cache from '@/plugins/cache';
import animate from '@/animate';
import { useDict } from '@/utils/dict';
import { handleTree, addDateRange, selectDictLabel, selectDictLabels, parseTime } from '@/utils/ruoyi';
import { getConfigKey, updateConfigByKey } from '@/api/system/config';
import { download as rd } from '@/utils/request';

export {};

declare module 'vue' {
  interface ComponentCustomProperties {
    // 全局方法声明
    $modal: typeof modal;
    $tab: typeof tab;
    $download: typeof download;
    $auth: typeof auth;
    $cache: typeof cache;
    animate: typeof animate;

    useDict: typeof useDict;
    addDateRange: typeof addDateRange;
    download: typeof rd;
    handleTree: typeof handleTree;
    getConfigKey: typeof getConfigKey;
    updateConfigByKey: typeof updateConfigByKey;
    selectDictLabel: typeof selectDictLabel;
    selectDictLabels: typeof selectDictLabels;
    parseTime: typeof parseTime;
  }
}

declare module 'vform3-builds' {
  const content: any;
  export = content;
}
