import modal from '@/plugins/modal';
import tab from '@/plugins/tab';
import { useDict } from '@/utils/dict';
import { addDateRange, handleTree, selectDictLabel, selectDictLabels, parseTime } from '@/utils/ruoyi';
import { getConfigKey, updateConfigByKey } from '@/api/system/config';
import { download as download1 } from '@/utils/request';
import download from '@/plugins/download';
import animate from '@/animate';

declare module 'vue' {
  export interface ComponentCustomProperties {
    // 全局方法声明
    $modal: typeof modal;
    $tab: typeof tab;
    $download: typeof download;
    animate: typeof animate;

    useDict: typeof useDict;
    addDateRange: typeof addDateRange;
    download: typeof download1;
    handleTree: typeof handleTree;
    getConfigKey: typeof getConfigKey;
    updateConfigByKey: typeof updateConfigByKey;
    selectDictLabel: typeof selectDictLabel;
    selectDictLabels: typeof selectDictLabels;
    parseTime: typeof parseTime;
  }
}
