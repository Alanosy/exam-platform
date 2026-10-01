<template>
  <div class="p-[16px]">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">我的证书</span>
          <el-button plain icon="Refresh" @click="getList">刷新</el-button>
        </div>
      </template>

      <div v-loading="loading">
        <el-row v-if="list.length > 0" :gutter="16">
          <el-col v-for="item in list" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6" class="mb-[16px]">
            <div class="cert-card" @click="openDetail(item)">
              <div class="cert-card-paper">
                <certificate-paper
                  :title="item.title"
                  :subtitle="item.subtitle"
                  :content="item.content"
                  :issuer="item.issuer"
                  :bg-color="item.bgColor"
                  :bg-url="item.bgUrl"
                  :seal-url="item.sealUrl"
                  :orientation="item.orientation"
                  :holder="item.nickName || item.account"
                  :cert-no="item.certNo"
                  :issue-date="formatDate(item.issueTime)"
                  :expire-date="item.expireTime ? formatDate(item.expireTime) : '长期有效'"
                />
              </div>
              <div class="cert-card-info">
                <div class="cert-card-name">
                  <el-tag v-if="item.status === '1'" type="danger" size="small" effect="plain">已吊销</el-tag>
                  <el-tag v-else-if="isExpired(item)" type="info" size="small" effect="plain">已过期</el-tag>
                  <el-tag v-else type="success" size="small" effect="plain">有效</el-tag>
                  <span class="ml-1">{{ item.examName || '考试' }}</span>
                </div>
                <div class="cert-card-meta">得分 {{ item.score ?? 0 }} / {{ item.totalScore ?? 0 }}</div>
                <div class="cert-card-meta">颁发于 {{ formatTime(item.issueTime) }}</div>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-empty v-else-if="!loading" description="还没有获得证书，考试及格后会自动颁发" />
      </div>
    </el-card>

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
          <el-descriptions-item label="证书编号">{{ current.certNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="考试">{{ current.examName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="得分">{{ current.score ?? 0 }} / {{ current.totalScore ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="颁发时间">{{ formatTime(current.issueTime) }}</el-descriptions-item>
          <el-descriptions-item label="有效期至">{{ current.expireTime ? formatDate(current.expireTime) : '永久有效' }}</el-descriptions-item>
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
  </div>
</template>

<script setup lang="ts" name="ExamMyCerts">
import CertificatePaper from '@/components/CertificatePaper.vue';
import { listMyCertificates } from '@/api/exam/cert';
import type { CertificateRecordVO } from '@/api/exam/cert/types';

const loading = ref(true);
const list = ref<CertificateRecordVO[]>([]);
const detailVisible = ref(false);
const current = ref<CertificateRecordVO | undefined>(undefined);

const pad = (n: number): string => String(n).padStart(2, '0');

const toDate = (value?: string): Date | null => {
  if (!value) return null;
  const date = new Date(String(value).replace(/-/g, '/'));
  return Number.isNaN(date.getTime()) ? null : date;
};

const formatTime = (value?: string): string => {
  const date = toDate(value);
  if (!date) return '-';
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const formatDate = (value?: string): string => {
  const date = toDate(value);
  if (!date) return '-';
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
};

/** 过期是「算」出来的：库里只存失效时间，不额外维护状态列 */
const isExpired = (item: CertificateRecordVO): boolean => {
  if (item.status === '1') return false;
  const expire = toDate(item.expireTime);
  return expire !== null && expire.getTime() < Date.now();
};

const openDetail = (item: CertificateRecordVO): void => {
  current.value = item;
  detailVisible.value = true;
};

/** 打印样式在 CertificatePaper 里：@media print 只保留证书纸本身 */
const printCert = (): void => {
  window.print();
};

const getList = async (): Promise<void> => {
  loading.value = true;
  try {
    const res = await listMyCertificates();
    list.value = res.data ?? [];
  } catch {
    list.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(getList);
</script>

<style scoped lang="scss">
.cert-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 4px 16px rgb(0 0 0 / 10%);
  }
}

.cert-card-paper {
  padding: 10px;
  background: #fafafa;
}

.cert-card-info {
  padding: 10px 12px;
}

.cert-card-name {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cert-card-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
</style>
