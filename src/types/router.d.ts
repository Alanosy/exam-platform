import { RouteRecordRaw } from 'vue-router';

declare module 'vue-router' {
  type RouteOption = {
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

  interface _RouteLocationBase {
    children?: RouteOption[];
  }

  interface RouteLocationOptions {
    fullPath?: string;
  }

  interface TagView extends Partial<_RouteLocationBase> {
    title?: string;
    meta?: {
      link?: string;
      title?: string;
      affix?: boolean;
      noCache?: boolean;
    };
  }
}
