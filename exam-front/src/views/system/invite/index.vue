<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="考试ID" prop="examId">
              <el-input v-model="queryParams.examId" placeholder="请输入考试ID" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="邀请账号：手机号/邮箱" prop="inviteAccount">
              <el-input v-model="queryParams.inviteAccount" placeholder="请输入邀请账号：手机号/邮箱" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="邀请发送时间" prop="inviteTime">
              <el-date-picker clearable
                v-model="queryParams.inviteTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择邀请发送时间"
              />
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:invite:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:invite:edit']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:invite:remove']">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:invite:export']">导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="inviteList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="主键ID" align="center" prop="id" v-if="true" />
        <el-table-column label="考试ID" align="center" prop="examId" />
        <el-table-column label="邀请账号：手机号/邮箱" align="center" prop="inviteAccount" />
        <el-table-column label="sms短信 / email邮件" align="center" prop="inviteType" />
        <el-table-column label="send已发送 / accept已进入考试 / expire已过期" align="center" prop="inviteStatus" />
        <el-table-column label="邀请发送时间" align="center" prop="inviteTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.inviteTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:invite:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:invite:remove']"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
    <!-- 添加或修改考试邀请记录对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" append-to-body>
      <el-form ref="inviteFormRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="考试ID" prop="examId">
          <el-input v-model="form.examId" placeholder="请输入考试ID" />
        </el-form-item>
        <el-form-item label="邀请账号：手机号/邮箱" prop="inviteAccount">
          <el-input v-model="form.inviteAccount" placeholder="请输入邀请账号：手机号/邮箱" />
        </el-form-item>
        <el-form-item label="邀请发送时间" prop="inviteTime">
          <el-date-picker clearable
            v-model="form.inviteTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择邀请发送时间">
          </el-date-picker>
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

<script setup name="Invite" lang="ts">
import { listInvite, getInvite, delInvite, addInvite, updateInvite } from '@/api/system/invite';
import { InviteVO, InviteQuery, InviteForm } from '@/api/system/invite/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const inviteList = ref<InviteVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const inviteFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: InviteForm = {
  id: undefined,
  examId: undefined,
  inviteAccount: undefined,
  inviteType: undefined,
  inviteStatus: undefined,
  inviteTime: undefined,
}
const data = reactive<PageData<InviteForm, InviteQuery>>({
  form: {...initFormData},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    examId: undefined,
    inviteAccount: undefined,
    inviteType: undefined,
    inviteStatus: undefined,
    inviteTime: undefined,
    params: {
    }
  },
  rules: {
    id: [
      { required: true, message: "主键ID不能为空", trigger: "blur" }
    ],
    examId: [
      { required: true, message: "考试ID不能为空", trigger: "blur" }
    ],
    inviteAccount: [
      { required: true, message: "邀请账号：手机号/邮箱不能为空", trigger: "blur" }
    ],
    inviteType: [
      { required: true, message: "sms短信 / email邮件不能为空", trigger: "change" }
    ],
    inviteStatus: [
      { required: true, message: "send已发送 / accept已进入考试 / expire已过期不能为空", trigger: "change" }
    ],
    inviteTime: [
      { required: true, message: "邀请发送时间不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询考试邀请记录列表 */
const getList = async () => {
  loading.value = true;
  const res = await listInvite(queryParams.value);
  inviteList.value = res.rows;
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
  inviteFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: InviteVO[]) => {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = "添加考试邀请记录";
}

/** 修改按钮操作 */
const handleUpdate = async (row?: InviteVO) => {
  reset();
  const _id = row?.id || ids.value[0]
  const res = await getInvite(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = "修改考试邀请记录";
}

/** 提交按钮 */
const submitForm = () => {
  inviteFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateInvite(form.value).finally(() =>  buttonLoading.value = false);
      } else {
        await addInvite(form.value).finally(() =>  buttonLoading.value = false);
      }
      proxy?.$modal.msgSuccess("操作成功");
      dialog.visible = false;
      await getList();
    }
  });
}

/** 删除按钮操作 */
const handleDelete = async (row?: InviteVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除考试邀请记录编号为"' + _ids + '"的数据项？').finally(() => loading.value = false);
  await delInvite(_ids);
  proxy?.$modal.msgSuccess("删除成功");
  await getList();
}

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download('system/invite/export', {
    ...queryParams.value
  }, `invite_${new Date().getTime()}.xlsx`)
}

onMounted(() => {
  getList();
});
</script>
