import { RouteRecordRaw } from 'vue-router';

declare module 'vue-router' {
  declare type RouteOption = {
    hidden?: boolean;
    permissions?: string[];
    roles?: string[];
    component?: any;
    children?: RouteOption[];
    alwaysShow?: boolean;
    parentPath?: string;
    meta?: {
      title: string;
      icon: string;
    };
    query?: string;
  } & RouteRecordRaw;

  declare interface _RouteLocationBase {
    children?: RouteOption[];
  }

  declare interface RouteLocationOptions {
    fullPath?: string;
  }

  declare interface TagView extends Partial<_RouteLocationBase> {
    title?: string;
    meta?: {
      link?: string;
      title?: string;
      affix?: boolean;
      noCache?: boolean;
    };
  }
}
