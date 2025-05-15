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
import type { LanguageType } from '@/lang';

export {};

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    // 全局方法声明
    $modal: typeof modal;
    $tab: typeof tab;
    $download: typeof download;
    $auth: typeof auth;
    $cache: typeof cache;
    animate: typeof animate;
    /**
     * i18n $t方法支持ts类型提示
     * @param key i18n key
     */
    $t(key: ObjKeysToUnion<LanguageType>): string;

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

/**
 * { a: 1, b: { ba: { baa: 1, bab: 2 }, bb: 2} } ---> a | b.ba.baa | b.ba.bab | b.bb
 * https://juejin.cn/post/7280062870670606397
 */
export type ObjKeysToUnion<T, P extends string = ''> = T extends object
  ? {
      [K in keyof T]: ObjKeysToUnion<T[K], P extends '' ? `${K & string}` : `${P}.${K & string}`>;
    }[keyof T]
  : P;
