<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="试题ID" prop="questionId">
              <el-input v-model="queryParams.questionId" placeholder="请输入试题ID" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="资源访问地址" prop="mediaUrl">
              <el-input v-model="queryParams.mediaUrl" placeholder="请输入资源访问地址MinIO" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="原始文件名" prop="mediaName">
              <el-input v-model="queryParams.mediaName" placeholder="请输入原始文件名" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="展示顺序" prop="sort">
              <el-input v-model="queryParams.sort" placeholder="请输入展示顺序" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="never">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:media:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:media:edit']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:media:remove']">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:media:export']">导出</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="info" plain icon="DeleteFilled" @click="handleCleanUnused" v-hasPermi="['system:media:remove']">清理孤儿附件</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="mediaList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="主键ID" align="center" prop="id" v-if="true" />
        <el-table-column label="试题ID" align="center" prop="questionId" />
        <el-table-column label="媒体类型" align="center" prop="mediaType" />
        <el-table-column label="资源访问地址" align="center" prop="mediaUrl" />
        <el-table-column label="原始文件名" align="center" prop="mediaName" />
        <el-table-column label="展示顺序" align="center" prop="sort" />
        <el-table-column label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:media:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:media:remove']"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
    <!-- 添加或修改试题多媒体附件对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" append-to-body>
      <el-form ref="mediaFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="试题ID" prop="questionId">
          <el-input v-model="form.questionId" placeholder="请输入试题ID" />
        </el-form-item>
        <el-form-item label="资源访问地址" prop="mediaUrl">
            <el-input v-model="form.mediaUrl" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="原始文件名" prop="mediaName">
          <el-input v-model="form.mediaName" placeholder="请输入原始文件名" />
        </el-form-item>
        <el-form-item label="展示顺序" prop="sort">
          <el-input v-model="form.sort" placeholder="请输入展示顺序" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Media" lang="ts">
import { listMedia, getMedia, delMedia, addMedia, updateMedia, cleanUnusedMedia } from '@/api/system/media';
import { MediaVO, MediaQuery, MediaForm } from '@/api/system/media/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const mediaList = ref<MediaVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const mediaFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: MediaForm = {
  id: undefined,
  questionId: undefined,
  mediaType: undefined,
  mediaUrl: undefined,
  mediaName: undefined,
  sort: undefined,
}
const data = reactive<PageData<MediaForm, MediaQuery>>({
  form: {...initFormData},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    questionId: undefined,
    mediaType: undefined,
    mediaUrl: undefined,
    mediaName: undefined,
    sort: undefined,
    params: {
    }
  },
  rules: {
    id: [
      { required: true, message: "主键ID不能为空", trigger: "blur" }
    ],
    questionId: [
      { required: true, message: "试题ID不能为空", trigger: "blur" }
    ],
    mediaType: [
      { required: true, message: "media_type:image图片,audio音频,video视频不能为空", trigger: "change" }
    ],
    mediaUrl: [
      { required: true, message: "资源访问地址MinIO不能为空", trigger: "blur" }
    ],
    mediaName: [
      { required: true, message: "原始文件名不能为空", trigger: "blur" }
    ],
    sort: [
      { required: true, message: "展示顺序不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询试题多媒体附件列表 */
const getList = async () => {
  loading.value = true;
  const res = await listMedia(queryParams.value);
  mediaList.value = res.rows;
  total.value = res.total;
  loading.value = false;
}

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
}

/** 表单重置 */
const reset = () => {
  form.value = {...initFormData};
  mediaFormRef.value?.resetFields();
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
}

/** 多选框选中数据 */
const handleSelectionChange = (selection: MediaVO[]) => {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = "添加试题多媒体附件";
}

/** 修改按钮操作 */
const handleUpdate = async (row?: MediaVO) => {
  reset();
  const _id = row?.id || ids.value[0]
  const res = await getMedia(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = "修改试题多媒体附件";
}

/** 提交按钮 */
const submitForm = () => {
  mediaFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateMedia(form.value).finally(() =>  buttonLoading.value = false);
      } else {
        await addMedia(form.value).finally(() =>  buttonLoading.value = false);
      }
      proxy?.$modal.msgSuccess("操作成功");
      dialog.visible = false;
      await getList();
    }
  });
}

/** 删除按钮操作 */
const handleDelete = async (row?: MediaVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除试题多媒体附件编号为"' + _ids + '"的数据项？').finally(() => loading.value = false);
  await delMedia(_ids);
  proxy?.$modal.msgSuccess("删除成功");
  await getList();
}

/** 清理孤儿附件（上传后长时间没落到任何试题上的文件） */
const handleCleanUnused = async () => {
  await proxy?.$modal.confirm('确认清理 24 小时前上传、且至今未归属任何试题的附件？文件会从对象存储删除，不可恢复。');
  const res = await cleanUnusedMedia(24);
  proxy?.$modal.msgSuccess(`清理完成，共 ${res.data ?? 0} 条`);
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  // 网关只转发 /question/** ，system 前缀会打到 system 服务上导致 404
  proxy?.download('question/media/export', {
    ...queryParams.value
  }, `media_${new Date().getTime()}.xlsx`)
}

onMounted(() => {
  getList();
});
</script>
