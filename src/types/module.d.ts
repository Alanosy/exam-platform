import type modal from '@/plugins/modal';
import type tab from '@/plugins/tab';
import type download from '@/plugins/download';
import type auth from '@/plugins/auth';
import type cache from '@/plugins/cache';
import type animate from '@/animate';
import type { useDict } from '@/utils/dict';
import type { addDateRange, handleTree, selectDictLabel, selectDictLabels, parseTime } from '@/utils/ruoyi';
import type { getConfigKey, updateConfigByKey } from '@/api/system/config';
import type { download as rd } from '@/utils/request';

declare module '@vue/runtime-core' {
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
