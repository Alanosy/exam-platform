<template>
  <div v-if="!item.hidden">
    <template v-if="hasOneShowingChild(item, item.children) && (!onlyOneChild.children || onlyOneChild.noShowingChildren) && !item.alwaysShow">
      <app-link v-if="onlyOneChild.meta" :to="resolvePath(onlyOneChild.path, onlyOneChild.query)">
        <el-menu-item :index="resolvePath(onlyOneChild.path)" :class="{ 'submenu-title-noDropdown': !isNest }">
          <svg-icon :icon-class="onlyOneChild.meta.icon || (item.meta && item.meta.icon)" />
          <template #title>
            <span class="menu-title" :title="hasTitle(onlyOneChild.meta.title)">{{ onlyOneChild.meta.title }}</span>
          </template>
        </el-menu-item>
      </app-link>
    </template>

    <el-sub-menu v-else ref="subMenu" :index="resolvePath(item.path)" teleported>
      <template v-if="item.meta" #title>
        <svg-icon :icon-class="item.meta ? item.meta.icon : '' " />
        <span class="menu-title" :title="hasTitle(item.meta?.title)">{{ item.meta?.title }}</span>
      </template>

      <sidebar-item
        v-for="(child) in item.children as RouteOption[]"
        :key="child.path"
        :is-nest="true"
        :item="child"
        :base-path="resolvePath(child.path)"
        class="nest-menu"
      />
    </el-sub-menu>
  </div>
</template>

<script setup lang="ts">
import { isExternal } from '@/utils/validate'
import AppLink from './Link.vue'
import { getNormalPath } from '@/utils/ruoyi'
import { RouteOption } from "vue-router";

const props = defineProps({
    // route object
    item: {
        type: Object as PropType<RouteOption>,
        required: true
    },
    isNest: {
        type: Boolean,
        default: false
    },
    basePath: {
        type: String,
        default: ''
    }
})

const onlyOneChild = ref<any>({});

const hasOneShowingChild = (parent: RouteOption, children?:RouteOption[]) => {
    if (!children) {
        children = [];
    }
    const showingChildren = children.filter(item => {
        if (item.hidden) {
            return false
        } else {
            // Temp set(will be used if only has one showing child)
            onlyOneChild.value = item
            return true
        }
    })

    // When there is only one child router, the child router is displayed by default
    if (showingChildren.length === 1) {
        return true
    }

    // Show parent if there are no child router to display
    if (showingChildren.length === 0) {
        onlyOneChild.value = { ...parent, path: '', noShowingChildren: true }
        if (parent.name === '2222') {
          console.log(onlyOneChild.value)
        }
        return true
    }


    return false
};

const resolvePath = (routePath:string, routeQuery?:string): any => {
    if (isExternal(routePath)) {
        return routePath
    }
    if (isExternal(props.basePath)) {
        return props.basePath
    }
    if (routeQuery) {
        let query = JSON.parse(routeQuery);
        return { path: getNormalPath(props.basePath + '/' + routePath), query: query }
    }
    return getNormalPath(props.basePath + '/' + routePath)
}

const hasTitle = (title: string | undefined): string => {
    if(!title || title.length <= 5) {
        return "";
    }
    return title;
}
</script>
