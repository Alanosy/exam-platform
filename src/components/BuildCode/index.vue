<!-- 代码构建 -->
<script setup lang="ts">

const props = defineProps({
  showBtn: {
    type: Boolean,
    default: false
  },
  formJson: {
    type: Object,
    default: undefined
  }
})
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const buildRef = ref();
const emits = defineEmits(['reJson', 'saveDesign']);



//获取表单json
const getJson = () => {
  const formJson = JSON.stringify(buildRef.value.getFormJson())
  const fieldJson = JSON.stringify(buildRef.value.getFieldWidgets())
  let data = {
    formJson, fieldJson
  }
  emits("saveDesign", data)
}

onMounted(() => {
  if (props.formJson) {
    buildRef.value.setFormJson(props.formJson)
  }
})
</script>

<template>
  <div>
    <v-form-designer
      class="build"
      ref="buildRef"
      :designer-config="{ importJsonButton: true, exportJsonButton: true, exportCodeButton: true, generateSFCButton: true, formTemplates: true }"
    >
      <template #customToolButtons v-if="showBtn">
        <el-button link type="primary" icon="Select" @click="getJson">保存</el-button>
      </template>
    </v-form-designer>
  </div>
</template>

<style lang="scss">
.build {
  margin: 0 !important;
  overflow-y: auto !important;

  & header.main-header {
    display: none;
  }

  & .right-toolbar-con {
    text-align: right !important;
  }
}
</style>
