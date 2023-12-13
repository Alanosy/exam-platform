<template>
  <!-- 代码构建 -->
  <div>
    <v-form-designer
      ref="buildRef"
      class="build"
      :designer-config="{ importJsonButton: true, exportJsonButton: true, exportCodeButton: true, generateSFCButton: true, formTemplates: true }"
    >
      <template v-if="showBtn" #customToolButtons>
        <el-button link type="primary" icon="Select" @click="getJson">保存</el-button>
      </template>
    </v-form-designer>
  </div>
</template>

<script setup lang="ts">
interface Props {
  showBtn: boolean;
  formJson: any;
}

const props = withDefaults(defineProps<Props>(), {
  showBtn: true,
  formJson: ''
});

const buildRef = ref();
const emits = defineEmits(['reJson', 'saveDesign']);

//获取表单json
const getJson = () => {
  const formJson = JSON.stringify(buildRef.value.getFormJson());
  const fieldJson = JSON.stringify(buildRef.value.getFieldWidgets());
  let data = {
    formJson,
    fieldJson
  };
  emits('saveDesign', data);
};

onMounted(() => {
  if (props.formJson) {
    buildRef.value.setFormJson(props.formJson);
  }
});
</script>

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
