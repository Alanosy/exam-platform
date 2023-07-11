<template>
  <transition-group name="fade-transform" mode="out-in">
    <inner-link
      v-for="(item, index) in tagsViewStore.iframeViews"
      :key="item.path"
      :iframeId="'iframe' + index"
      v-show="route.path === item.path"
      :src="item.meta ? item.meta.link : ''"
    ></inner-link>
  </transition-group>
</template>

<script setup lang="ts">
import InnerLink from "../InnerLink/index.vue";
import useTagsViewStore from '@/store/modules/tagsView';

const route = useRoute();
const tagsViewStore = useTagsViewStore()
</script>
