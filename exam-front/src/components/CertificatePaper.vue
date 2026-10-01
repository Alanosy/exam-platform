<template>
  <div class="cert-paper cert-print-area" :class="{ 'is-vertical': vertical }" :style="paperStyle">
    <img v-if="bgUrl" class="cert-bg" :src="bgUrl" alt="" />
    <div class="cert-inner">
      <div class="cert-title">{{ title || '证书标题' }}</div>
      <div v-if="subtitle" class="cert-subtitle">{{ subtitle }}</div>

      <div class="cert-holder">{{ holder || '考生姓名' }}</div>
      <div class="cert-content">{{ content || '（证书正文，支持换行）' }}</div>

      <div class="cert-foot">
        <div class="cert-foot-left">
          <div class="cert-meta">证书编号：{{ certNo || '待颁发' }}</div>
          <div v-if="expireDate" class="cert-meta">有效期至：{{ expireDate }}</div>
        </div>
        <div class="cert-foot-right">
          <div class="cert-issuer">{{ issuer || '发证机构' }}</div>
          <div class="cert-meta">{{ issueDate || '-' }}</div>
        </div>
      </div>

      <img v-if="sealUrl" class="cert-seal" :src="sealUrl" alt="印章" />
      <div v-else-if="sealPlaceholder" class="cert-seal-placeholder">印章</div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 证书纸张（预览 / 查看 / 打印共用）
 *
 * <p>模板编辑时的预览和考生看到的正式证书是同一套版式，
 * 所以抽成组件：编辑页传模板 + 示例值，查看页传已颁发的记录，渲染逻辑只有一份。
 */
const props = withDefaults(
  defineProps<{
    title?: string;
    subtitle?: string;
    content?: string;
    issuer?: string;
    bgColor?: string;
    bgUrl?: string;
    sealUrl?: string;
    /** 0横版 1竖版 */
    orientation?: string;
    holder?: string;
    certNo?: string;
    issueDate?: string;
    expireDate?: string;
    /** 预览态：没有印章时画一个占位圈，让排版看起来完整 */
    sealPlaceholder?: boolean;
  }>(),
  {
    bgColor: '#fdfaf3'
  }
);

const vertical = computed(() => props.orientation === '1');

const paperStyle = computed(() => ({
  background: props.bgUrl ? `url(${props.bgUrl}) center / cover no-repeat` : props.bgColor || '#fdfaf3'
}));
</script>

<style scoped lang="scss">
.cert-paper {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  border: 1px solid #d9c9a3;
  border-radius: 6px;
  box-shadow: 0 2px 12px rgb(0 0 0 / 8%);
  overflow: hidden;
  color: #4a3b1f;

  &.is-vertical {
    aspect-ratio: 3 / 4;
  }
}

.cert-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cert-inner {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  padding: 6% 8%;
  box-sizing: border-box;
  text-align: center;
}

.cert-title {
  font-size: clamp(18px, 3.2vw, 34px);
  font-weight: 700;
  letter-spacing: 6px;
}

.cert-subtitle {
  margin-top: 6px;
  font-size: 12px;
  letter-spacing: 2px;
  color: #9a8654;
}

.cert-holder {
  margin-top: 4%;
  font-size: clamp(16px, 2.4vw, 26px);
  font-weight: 600;
  border-bottom: 1px solid #c9b78c;
  padding: 0 12px 4px;
  min-width: 40%;
}

.cert-content {
  margin-top: 3%;
  flex: 1;
  font-size: clamp(12px, 1.4vw, 15px);
  line-height: 1.9;
  white-space: pre-wrap;
  text-align: left;
  width: 100%;
}

.cert-foot {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  width: 100%;
  margin-top: 3%;
}

.cert-foot-left {
  text-align: left;
}

.cert-foot-right {
  text-align: right;
}

.cert-issuer {
  font-size: 14px;
  font-weight: 600;
}

.cert-meta {
  font-size: 12px;
  color: #8a7a55;
}

.cert-seal {
  position: absolute;
  right: 8%;
  bottom: 14%;
  width: 18%;
  opacity: 0.9;
}

.cert-seal-placeholder {
  position: absolute;
  right: 8%;
  bottom: 14%;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border: 2px dashed #c0413b;
  border-radius: 50%;
  color: #c0413b;
  font-size: 13px;
  opacity: 0.5;
}
</style>

<style lang="scss">
/* 打印证书：只打证书纸，页面其它内容全部隐掉 */
@media print {
  body * {
    visibility: hidden;
  }

  .cert-print-area,
  .cert-print-area * {
    visibility: visible;
  }

  .cert-print-area {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    box-shadow: none;
  }
}
</style>
