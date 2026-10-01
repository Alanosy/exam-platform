<template>
  <div class="p-[16px]">
    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <!-- ------------------------------ 证书模板 ------------------------------ -->
        <el-tab-pane label="证书模板" name="template">
          <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
            <el-form-item label="关键字" prop="keyword">
              <el-input v-model="queryParams.keyword" placeholder="证书名称 / 编码" clearable style="width: 200px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
                <el-option label="启用" value="0" />
                <el-option label="停用" value="1" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <el-row :gutter="10" class="mb-[10px]">
            <el-col :span="1.5">
              <el-button v-hasPermi="['exam:cert:add']" type="primary" plain icon="Plus" @click="openForm()">新增</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                v-hasPermi="['exam:cert:edit']"
                type="success"
                plain
                icon="Edit"
                :disabled="selectedId === undefined"
                @click="openForm(selectedId)"
              >
                修改
              </el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                v-hasPermi="['exam:cert:remove']"
                type="danger"
                plain
                icon="Delete"
                :disabled="selectedId === undefined"
                @click="handleDelete"
              >
                删除
              </el-button>
            </el-col>
          </el-row>

          <el-table v-loading="loading" :data="list" border stripe @current-change="onCurrentChange" highlight-current-row>
            <el-table-column label="证书名称" prop="certName" min-width="160" show-overflow-tooltip />
            <el-table-column label="编码" prop="certCode" width="130" show-overflow-tooltip />
            <el-table-column label="标题" prop="title" min-width="140" show-overflow-tooltip />
            <el-table-column label="版式" align="center" width="80">
              <template #default="{ row }">{{ row.orientation === '1' ? '竖版' : '横版' }}</template>
            </el-table-column>
            <el-table-column label="有效期" align="center" width="120">
              <template #default="{ row }">
                {{ row.validType === '1' ? `${row.validDays ?? 0} 天` : '永久有效' }}
              </template>
            </el-table-column>
            <el-table-column label="已颁发" align="center" width="90" prop="issueCount" />
            <el-table-column label="状态" align="center" width="90">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.status)" size="small" effect="plain">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" align="center" width="150">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" icon="Edit" @click="openForm(row.id)">修改</el-button>
                <el-button link type="primary" icon="Tickets" @click="openRecords(row)">颁发记录</el-button>
              </template>
            </el-table-column>
          </el-table>

          <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
          <el-empty v-if="!loading && total === 0" description="还没有证书模板，先建一个再去考试里挂上" />
        </el-tab-pane>

        <!-- ------------------------------ 颁发记录 ------------------------------ -->
        <el-tab-pane label="颁发记录" name="record">
          <el-form :model="recordQuery" :inline="true" label-width="68px">
            <el-form-item label="证书">
              <el-select v-model="recordQuery.certId" placeholder="全部证书" clearable filterable style="width: 180px">
                <el-option v-for="item in certOptions" :key="item.id" :label="item.certName" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="关键字">
              <el-input v-model="recordQuery.keyword" placeholder="姓名 / 账号 / 证书编号" clearable style="width: 200px" @keyup.enter="getRecordList" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="recordQuery.status" placeholder="全部" clearable style="width: 120px">
                <el-option label="有效" value="0" />
                <el-option label="已吊销" value="1" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="getRecordList">搜索</el-button>
              <el-button icon="Refresh" @click="resetRecordQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <el-row :gutter="10" class="mb-[10px]">
            <el-col :span="1.5">
              <el-button v-hasPermi="['exam:cert:issue']" type="warning" plain icon="Upload" @click="issueVisible = true">手工补发</el-button>
            </el-col>
          </el-row>

          <el-table v-loading="recordLoading" :data="recordList" border stripe>
            <el-table-column label="证书编号" prop="certNo" width="180" show-overflow-tooltip />
            <el-table-column label="考生" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">{{ row.nickName || row.account || '-' }}</template>
            </el-table-column>
            <el-table-column label="考试" prop="examName" min-width="150" show-overflow-tooltip />
            <el-table-column label="得分" align="center" width="110">
              <template #default="{ row }">{{ row.score ?? 0 }} / {{ row.totalScore ?? 0 }}</template>
            </el-table-column>
            <el-table-column label="证书" prop="certName" min-width="130" show-overflow-tooltip />
            <el-table-column label="颁发方式" align="center" width="100">
              <template #default="{ row }">
                <el-tag :type="row.issueType === '1' ? 'warning' : 'success'" size="small" effect="plain">
                  {{ row.issueType === '1' ? '手工补发' : '及格自动' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="颁发时间" align="center" width="150">
              <template #default="{ row }">{{ formatTime(row.issueTime) }}</template>
            </el-table-column>
            <el-table-column label="有效期至" align="center" width="110">
              <template #default="{ row }">{{ row.expireTime ? formatDate(row.expireTime) : '永久' }}</template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === '1' ? 'danger' : 'success'" size="small" effect="plain">
                  {{ row.status === '1' ? '已吊销' : '有效' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" icon="View" @click="openDetail(row)">查看</el-button>
                <el-button
                  v-if="row.status !== '1'"
                  v-hasPermi="['exam:cert:issue']"
                  link
                  type="danger"
                  icon="CircleClose"
                  @click="handleRevoke(row)"
                >
                  吊销
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <pagination v-show="recordTotal > 0" v-model:page="recordQuery.pageNum" v-model:limit="recordQuery.pageSize" :total="recordTotal" @pagination="getRecordList" />
          <el-empty v-if="!recordLoading && recordTotal === 0" description="还没有颁发过证书" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ------------------------------ 模板表单 ------------------------------ -->
    <el-dialog v-model="formVisible" :title="form.id ? '修改证书模板' : '新增证书模板'" width="1000px" append-to-body destroy-on-close>
      <el-row :gutter="20">
        <el-col :span="14">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="证书名称" prop="certName">
                  <el-input v-model="form.certName" placeholder="管理用名称，如：前端认证证书" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="证书编码" prop="certCode">
                  <el-input v-model="form.certCode" placeholder="对外标识，可留空" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="大标题" prop="title">
                  <el-input v-model="form.title" placeholder="证书正面大字，如：结业证书" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="副标题" prop="subtitle">
                  <el-input v-model="form.subtitle" placeholder="标题下方小字，可留空" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="正文模板" prop="content">
              <el-input v-model="form.content" type="textarea" :rows="5" placeholder="如：在《{examName}》考试中取得 {score} 分，特发此证。" />
              <div class="form-tip">
                占位符：{nickName} 姓名 · {account} 账号 · {examName} 考试 · {score} 得分 · {totalScore} 总分 ·
                {passScore} 及格分 · {certNo} 编号 · {issueDate} 颁发日期 · {expireDate} 有效期
              </div>
            </el-form-item>

            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="发证机构">
                  <el-input v-model="form.issuer" placeholder="证书落款，如：XX 学院" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="版式">
                  <el-radio-group v-model="form.orientation">
                    <el-radio value="0">横版</el-radio>
                    <el-radio value="1">竖版</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="印章">
                  <image-upload v-model="form.sealOssId" :limit="1" :file-size="5" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="背景图">
                  <image-upload v-model="form.bgOssId" :limit="1" :file-size="10" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="背景色">
                  <el-color-picker v-model="form.bgColor" />
                  <span class="form-tip ml-2">未上传背景图时生效</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="有效期">
                  <el-radio-group v-model="form.validType">
                    <el-radio value="0">永久</el-radio>
                    <el-radio value="1">按天</el-radio>
                  </el-radio-group>
                  <el-input-number
                    v-if="form.validType === '1'"
                    v-model="form.validDays"
                    :min="1"
                    :max="3650"
                    :precision="0"
                    controls-position="right"
                    class="ml-2 w-[110px]"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio value="0">启用</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
              <span class="form-tip ml-2">停用后不再自动颁发，已发出的证书不受影响</span>
            </el-form-item>

            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit />
            </el-form-item>
          </el-form>
        </el-col>

        <el-col :span="10">
          <div class="preview-title">实时预览（示例数据）</div>
          <certificate-paper
            :title="form.title"
            :subtitle="form.subtitle"
            :content="previewContent"
            :issuer="form.issuer"
            :bg-color="form.bgColor"
            :bg-url="previewBgUrl"
            :seal-url="previewSealUrl"
            :orientation="form.orientation"
            holder="张三"
            cert-no="CERT20261002000001"
            issue-date="2026-10-02"
            :expire-date="form.validType === '1' ? `示例：${form.validDays ?? 0} 天后` : '长期有效'"
            seal-placeholder
          />
        </el-col>
      </el-row>

      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- ------------------------------ 证书查看 ------------------------------ -->
    <el-dialog v-model="detailVisible" title="证书详情" width="900px" append-to-body>
      <div v-if="current">
        <certificate-paper
          :title="current.title"
          :subtitle="current.subtitle"
          :content="current.content"
          :issuer="current.issuer"
          :bg-color="current.bgColor"
          :bg-url="current.bgUrl"
          :seal-url="current.sealUrl"
          :orientation="current.orientation"
          :holder="current.nickName || current.account"
          :cert-no="current.certNo"
          :issue-date="formatDate(current.issueTime)"
          :expire-date="current.expireTime ? formatDate(current.expireTime) : '长期有效'"
        />
        <el-descriptions :column="3" border class="mt-[16px]" size="small">
          <el-descriptions-item label="考生">{{ current.nickName || current.account || '-' }}</el-descriptions-item>
          <el-descriptions-item label="考试">{{ current.examName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="得分">{{ current.score ?? 0 }} / {{ current.totalScore ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="证书编号">{{ current.certNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="颁发时间">{{ formatTime(current.issueTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="current.status === '1' ? 'danger' : 'success'" size="small" effect="plain">
              {{ current.status === '1' ? '已吊销' : '有效' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="current.status === '1'" label="吊销原因" :span="3">
            {{ current.revokeReason || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" icon="Printer" @click="printCert">打印证书</el-button>
      </template>
    </el-dialog>

    <!-- ------------------------------ 手工补发 ------------------------------ -->
    <el-dialog v-model="issueVisible" title="手工补发证书" width="520px" append-to-body>
      <el-form :model="issueForm" label-width="100px">
        <el-form-item label="答卷ID" required>
          <el-input v-model="issueForm.recordId" placeholder="在考试记录 / 阅卷列表里能看到" />
        </el-form-item>
        <el-form-item label="考试ID">
          <el-input v-model="issueForm.examId" placeholder="留空时按答卷自带的考试" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="issueForm.remark" placeholder="如：证书服务故障补发" />
        </el-form-item>
      </el-form>
      <div class="form-tip">
        成绩由后端按答卷ID去查，这里不用填分数：考生没及格会被直接拒绝。
      </div>
      <template #footer>
        <el-button @click="issueVisible = false">取消</el-button>
        <el-button type="primary" :loading="issuing" @click="submitIssue">确定补发</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="SystemCert">
import ImageUpload from '@/components/ImageUpload/index.vue';
import CertificatePaper from '@/components/CertificatePaper.vue';
import {
  addCertificate,
  delCertificate,
  getCertificate,
  issueCertificate,
  listCertificateOptions,
  listCertificateRecords,
  listCertificates,
  revokeCertificate,
  updateCertificate
} from '@/api/exam/cert';
import type { CertificateBO, CertificateOptionVO, CertificateQuery, CertificateRecordVO, CertificateVO, CertIssueBO } from '@/api/exam/cert/types';

type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger';

const STATUS_TAG: Record<string, TagType> = { '0': 'success', '1': 'info' };

const statusTag = (status?: string): TagType => STATUS_TAG[status ?? '0'] ?? 'info';
const statusText = (status?: string): string => (status === '1' ? '停用' : '启用');

const pad = (n: number): string => String(n).padStart(2, '0');

const formatTime = (value?: string): string => {
  if (!value) return '-';
  const date = new Date(String(value).replace(/-/g, '/'));
  if (Number.isNaN(date.getTime())) return value;
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const formatDate = (value?: string): string => {
  if (!value) return '-';
  const date = new Date(String(value).replace(/-/g, '/'));
  if (Number.isNaN(date.getTime())) return value;
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
};

/* --------------------------------- 证书模板 --------------------------------- */

const loading = ref(false);
const total = ref(0);
const list = ref<CertificateVO[]>([]);
const certOptions = ref<CertificateOptionVO[]>([]);
const selectedId = ref<string | undefined>(undefined);
const queryFormRef = ref<ElFormInstance>();
const queryParams = ref<CertificateQuery>({ pageNum: 1, pageSize: 10 });

const getList = async (): Promise<void> => {
  loading.value = true;
  try {
    // 响应拦截器已经把 res 解包成业务体（rows/total 在同一层），这里按 any 取，跟监考中心一致
    const res: any = await listCertificates(queryParams.value);
    list.value = res?.rows ?? [];
    total.value = res.total ?? 0;
  } catch {
    list.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const onCurrentChange = (row?: CertificateVO): void => {
  selectedId.value = row?.id;
};

const handleQuery = (): void => {
  queryParams.value.pageNum = 1;
  getList();
};

const resetQuery = (): void => {
  queryFormRef.value?.resetFields();
  queryParams.value = { pageNum: 1, pageSize: 10 };
  getList();
};

/** 证书下拉：模板页签的颁发记录筛选要用 */
const loadOptions = async (): Promise<void> => {
  try {
    const res = await listCertificateOptions();
    certOptions.value = res.data ?? [];
  } catch {
    certOptions.value = [];
  }
};

/* ---------------------------------- 表单 ---------------------------------- */

const formVisible = ref(false);
const submitting = ref(false);
const formRef = ref<ElFormInstance>();
const form = reactive<CertificateBO>({
  orientation: '0',
  validType: '0',
  validDays: 365,
  status: '0',
  bgColor: '#fdfaf3'
});

const rules = {
  certName: [{ required: true, message: '证书名称不能为空', trigger: 'blur' }],
  title: [{ required: true, message: '证书大标题不能为空', trigger: 'blur' }]
};

/** 预览用的示例值，跟后端 render() 的占位符一致 */
const SAMPLE: Record<string, string> = {
  '{nickName}': '张三',
  '{account}': 'zhangsan',
  '{examName}': '前端工程师认证考试',
  '{score}': '85',
  '{totalScore}': '100',
  '{passScore}': '60',
  '{certNo}': 'CERT20261002000001',
  '{issueDate}': '2026-10-02',
  '{expireDate}': '长期有效'
};

const previewContent = computed((): string => {
  let text = form.content ?? '';
  Object.keys(SAMPLE).forEach((key) => {
    text = text.split(key).join(SAMPLE[key]);
  });
  return text;
});

/**
 * 刚上传的图片拿到的是 ossId，没有访问地址，
 * 这里拿详情接口回填的 url 做预览；拿不到就先不显示背景，不影响保存。
 */
const previewSealUrl = ref('');
const previewBgUrl = ref('');

const openForm = async (id?: string): Promise<void> => {
  Object.assign(form, {
    id: undefined,
    certName: '',
    certCode: '',
    title: '',
    subtitle: '',
    content: '',
    issuer: '',
    sealOssId: '',
    bgOssId: '',
    bgColor: '#fdfaf3',
    orientation: '0',
    validType: '0',
    validDays: 365,
    status: '0',
    remark: ''
  });
  previewSealUrl.value = '';
  previewBgUrl.value = '';
  if (id) {
    const res = await getCertificate(id);
    Object.assign(form, res.data ?? {});
    previewSealUrl.value = res.data?.sealUrl ?? '';
    previewBgUrl.value = res.data?.bgUrl ?? '';
  }
  formVisible.value = true;
};

const submitForm = async (): Promise<void> => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  submitting.value = true;
  try {
    if (form.id) {
      await updateCertificate(form);
    } else {
      await addCertificate(form);
    }
    formVisible.value = false;
    await getList();
    await loadOptions();
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async (): Promise<void> => {
  if (!selectedId.value) return;
  await ElMessageBox.confirm('删除后已颁发的证书不受影响（内容已快照），确定删除该模板吗？', '提示', { type: 'warning' });
  await delCertificate(selectedId.value);
  selectedId.value = undefined;
  await getList();
  await loadOptions();
};

/* -------------------------------- 颁发记录 -------------------------------- */

const recordLoading = ref(false);
const recordTotal = ref(0);
const recordList = ref<CertificateRecordVO[]>([]);
const recordQuery = ref<{ pageNum: number; pageSize: number; certId?: string; examId?: string; keyword?: string; status?: string }>({
  pageNum: 1,
  pageSize: 10
});

const getRecordList = async (): Promise<void> => {
  recordLoading.value = true;
  try {
    const res: any = await listCertificateRecords(recordQuery.value);
    recordList.value = res?.rows ?? [];
    recordTotal.value = res.total ?? 0;
  } catch {
    recordList.value = [];
    recordTotal.value = 0;
  } finally {
    recordLoading.value = false;
  }
};

const resetRecordQuery = (): void => {
  recordQuery.value = { pageNum: 1, pageSize: 10 };
  getRecordList();
};

/** 从模板行点「颁发记录」进来时，直接把筛选切到那张证书 */
const openRecords = (row: CertificateVO): void => {
  recordQuery.value = { pageNum: 1, pageSize: 10, certId: row.id };
  activeTab.value = 'record';
  getRecordList();
};

const detailVisible = ref(false);
const current = ref<CertificateRecordVO | undefined>(undefined);

const openDetail = (row: CertificateRecordVO): void => {
  current.value = row;
  detailVisible.value = true;
};

const printCert = (): void => {
  window.print();
};

const handleRevoke = async (row: CertificateRecordVO): Promise<void> => {
  const { value } = await ElMessageBox.prompt('填写吊销原因（如：考试作弊、成绩作废）', '吊销证书', {
    inputPlaceholder: '请输入原因',
    inputValidator: (v: string) => (v && v.trim().length > 0 ? true : '请填写吊销原因')
  }).catch(() => ({ value: undefined as string | undefined }));
  if (!value) return;
  await revokeCertificate(String(row.id), value);
  await getRecordList();
};

/* -------------------------------- 手工补发 -------------------------------- */

const issueVisible = ref(false);
const issuing = ref(false);
const issueForm = reactive<CertIssueBO>({ recordId: '', examId: '', remark: '' });

const submitIssue = async (): Promise<void> => {
  if (!issueForm.recordId) {
    ElMessage.warning('请填写答卷ID');
    return;
  }
  issuing.value = true;
  try {
    await issueCertificate(issueForm);
    ElMessage.success('补发成功');
    issueVisible.value = false;
    await getRecordList();
  } finally {
    issuing.value = false;
  }
};

/* ---------------------------------- 初始化 ---------------------------------- */

const activeTab = ref('template');

// 切到记录页签时才拉数据，避免两个列表一起请求
watch(activeTab, (tab) => {
  if (tab === 'record' && recordList.value.length === 0) {
    getRecordList();
  }
});

onMounted(async () => {
  await getList();
  await loadOptions();
});
</script>

<style scoped lang="scss">
.form-tip {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.6;
  color: #909399;
}

.preview-title {
  margin-bottom: 10px;
  font-size: 13px;
  color: #606266;
}
</style>
