<template>
  <div>
    <svg-icon icon-class="question" @click="goto" />
  </div>
</template>

<script setup lang="ts">
const url = ref('https://doc.alan.org.cn');

function goto() {
  window.open(url.value);
}
</script>
