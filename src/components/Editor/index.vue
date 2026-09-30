<template>
  <div>
    <el-upload
      v-if="type === 'url'"
      :action="upload.url"
      :before-upload="handleBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      class="editor-img-uploader"
      name="file"
      :show-file-list="false"
      :headers="upload.headers"
    >
      <i ref="uploadRef"></i>
    </el-upload>
  </div>
  <div class="editor">
    <quill-editor
      ref="quillEditorRef"
      content-type="html"
      :options="options"
      :style="styles"
      @ready="handleEditorReady"
      @text-change="handleTextChange"
    />
  </div>
</template>

<script setup lang="ts">
import '@vueup/vue-quill/dist/vue-quill.snow.css';

import { QuillEditor, Quill } from '@vueup/vue-quill';
import { propTypes } from '@/utils/propTypes';
import { globalHeaders } from '@/utils/request';

const emit = defineEmits(['update:modelValue']);

const props = defineProps({
  /* 编辑器的内容 */
  modelValue: propTypes.string,
  /* 高度 */
  height: propTypes.number.def(400),
  /* 最小高度 */
  minHeight: propTypes.number.def(400),
  /* 只读 */
  readOnly: propTypes.bool.def(false),
  /* 上传文件大小限制(MB) */
  fileSize: propTypes.number.def(5),
  /* 插入图片时的默认最大高度(px)，超出则按原始比例等比缩小；0 表示不限制 */
  imageMaxHeight: propTypes.number.def(100),
  /* 类型（base64格式、url格式） */
  type: propTypes.string.def('url'),
  /* 精简工具栏，用于行内、小面积录入（如试题选项） */
  simple: propTypes.bool.def(false),
  /* 占位提示 */
  placeholder: propTypes.string.def('请输入内容')
});

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const upload = reactive<UploadOption>({
  headers: globalHeaders(),
  url: import.meta.env.VITE_APP_BASE_API + '/resource/oss/upload'
});
const quillEditorRef = ref();
const uploadRef = ref<HTMLDivElement>();

// 完整工具栏
const TOOLBAR_FULL = [
  ['bold', 'italic', 'underline', 'strike'], // 加粗 斜体 下划线 删除线
  ['blockquote', 'code-block'], // 引用  代码块
  [{ list: 'ordered' }, { list: 'bullet' }], // 有序、无序列表
  [{ indent: '-1' }, { indent: '+1' }], // 缩进
  [{ size: ['small', false, 'large', 'huge'] }], // 字体大小
  [{ header: [1, 2, 3, 4, 5, 6, false] }], // 标题
  [{ color: [] }, { background: [] }], // 字体颜色、字体背景颜色
  [{ align: [] }], // 对齐方式
  ['clean'], // 清除文本格式
  ['link', 'image', 'video'] // 链接、图片、视频
];

// 精简工具栏，保留富文本录入最常用的能力
const TOOLBAR_SIMPLE = [
  ['bold', 'italic', 'underline', 'strike'],
  ['code-block'],
  [{ list: 'ordered' }, { list: 'bullet' }],
  [{ size: ['small', false, 'large', 'huge'] }],
  [{ color: [] }, { background: [] }],
  ['clean'],
  ['link', 'image']
];

const options = ref<any>({
  theme: 'snow',
  bounds: document.body,
  debug: 'warn',
  modules: {
    // 工具栏配置
    toolbar: {
      container: props.simple ? TOOLBAR_SIMPLE : TOOLBAR_FULL,
      handlers: {
        image: (value: boolean) => {
          if (value) {
            // 调用element图片上传
            uploadRef.value.click();
          } else {
            Quill.format('image', true);
          }
        }
      }
    }
  },
  placeholder: props.placeholder,
  readOnly: props.readOnly
});

const styles = computed(() => {
  const style: any = {};
  if (props.minHeight) {
    style.minHeight = `${props.minHeight}px`;
  }
  if (props.height) {
    style.height = `${props.height}px`;
  }
  return style;
});

/** 取 quill 实例；quill 是异步实例化的，未就绪时返回 null */
const getQuill = (): any => {
  try {
    return (toRaw(quillEditorRef.value) as any)?.getQuill?.() ?? null;
  } catch {
    return null;
  }
};

/** 空内容归一化，避免 '' 与 '<p><br></p>' 互相判不等导致反复写入 */
const normalizeHtml = (html?: string): string => {
  const value = (html ?? '').trim();
  return !value || value === '<p></p>' || value === '<p><br></p>' || value === '<br>' ? '' : value;
};

/**
 * 手动同步 ql-blank
 *
 * quill 只在 text-change 时切换 ql-blank，外部静默写入内容不会触发该逻辑，
 * 于是会出现「内容已经回显，placeholder 还挂在那」的现象，这里兜底校正。
 */
const syncPlaceholder = () => {
  const quill = getQuill();
  const root = quill?.root as HTMLElement | undefined;
  if (!quill || !root) return;
  const hasText = (quill.getText() ?? '').replace(/[\u200b\ufeff]/g, '').trim().length > 0;
  const hasEmbed = !!root.querySelector('img, video, audio, iframe');
  root.classList.toggle('ql-blank', !hasText && !hasEmbed);
};

/**
 * 写入内容：必须走 quill 的文档模型（clipboard.convert + setContents）
 *
 * 直接给 root.innerHTML 赋值只会改 DOM，quill 内部的 delta 仍然是空的，
 * 表现就是内容看得见、placeholder 消不掉、光标点不进去、键盘输不进去。
 */
const applyContent = (html?: string) => {
  const quill = getQuill();
  if (!quill) return;
  // 内容一致时不用重复写入；但 DOM 被清空（quill 初始化时会清一次）时要补写，否则编辑器点不进去
  if (normalizeHtml(quill.root.innerHTML) === normalizeHtml(html) && quill.root.firstChild) {
    syncPlaceholder();
    return;
  }
  try {
    // clipboard.convert 把 html 解析成 delta，setContents 再走 quill 的文档模型落到编辑器里
    quill.setContents(quill.clipboard.convert(html ?? ''), 'silent');
  } catch {
    // 模型写入失败时至少保证内容可见
    quill.root.innerHTML = html ?? '';
  }
  syncPlaceholder();
};

/** 编辑器就绪后补写一次内容（初始化时 quill 可能还没实例化） */
const handleEditorReady = () => {
  applyContent(props.modelValue);
  // 只读状态由 options 决定，这里确保与当前 prop 一致
  getQuill()?.enable(!props.readOnly);
};

/** 用户输入时把最新 html 同步给父级的 v-model */
const handleTextChange = () => {
  emit('update:modelValue', getQuill()?.root.innerHTML ?? '');
  syncPlaceholder();
};

watch(
  () => props.modelValue,
  (v) => applyContent(v as string)
);

// options 里的 readOnly 只在初始化时读取一次，后续变化需要手动生效
watch(
  () => props.readOnly,
  (v) => getQuill()?.enable(!v)
);

onMounted(() => {
  // 兜底：ready 事件早于值就绪时再补一次
  nextTick(() => applyContent(props.modelValue));
});

/** 读原图真实尺寸，用于等比换算；取不到（跨域/格式异常）时按不缩放处理 */
const loadImageSize = (url: string): Promise<{ width: number; height: number } | null> =>
  new Promise((resolve) => {
    const img = new Image();
    img.onload = () => resolve({ width: img.naturalWidth, height: img.naturalHeight });
    img.onerror = () => resolve(null);
    img.src = url;
  });

/**
 * 插入图片并按默认最大宽度等比缩放
 *
 * quill 的 image blot 支持 width / height 属性，通过 formatText 写进去会落到文档模型里，
 * 随 root.innerHTML 一起保存；只在原图比上限宽时才缩，小图不会被放大。
 */
const insertImage = async (url: string) => {
  const quill = toRaw(quillEditorRef.value).getQuill();
  // 获取光标位置
  const length = quill.selection.savedRange.index;
  // 插入图片，res为服务器返回的图片链接地址
  quill.insertEmbed(length, 'image', url);

  // 原图比上限高时才缩，小图不会被放大；宽度按原始比例换算，避免拉伸变形
  const size = await loadImageSize(url);
  if (size && props.imageMaxHeight > 0 && size.height > props.imageMaxHeight) {
    const height = props.imageMaxHeight;
    const width = Math.max(1, Math.round((size.width * height) / size.height));
    quill.formatText(length, 1, 'width', String(width), 'user');
    quill.formatText(length, 1, 'height', String(height), 'user');
  }
  // 调整光标到最后
  quill.setSelection(length + 1);
};

// 图片上传成功：把返回的访问地址直接嵌进富文本，URL 随 HTML 一起保存
const handleUploadSuccess = async (res: any) => {
  if (res.code === 200) {
    await insertImage(res.data.url);
    proxy?.$modal.closeLoading();
  } else {
    proxy?.$modal.msgError('图片插入失败');
    proxy?.$modal.closeLoading();
  }
};

// 图片上传前拦截
const handleBeforeUpload = (file: any) => {
  const type = ['image/jpeg', 'image/jpg', 'image/png', 'image/svg'];
  const isJPG = type.includes(file.type);
  //检验文件格式
  if (!isJPG) {
    proxy?.$modal.msgError(`图片格式错误!`);
    return false;
  }
  // 校检文件大小
  if (props.fileSize) {
    const isLt = file.size / 1024 / 1024 < props.fileSize;
    if (!isLt) {
      proxy?.$modal.msgError(`上传文件大小不能超过 ${props.fileSize} MB!`);
      return false;
    }
  }
  proxy?.$modal.loading('正在上传文件，请稍候...');
  return true;
};

// 图片失败拦截
const handleUploadError = (err: any) => {
  proxy?.$modal.msgError('上传文件失败');
};
</script>

<style>
.editor-img-uploader {
  display: none;
}
/* 编辑器内的图片宽度不超过编辑区，高度按比例自适应，避免原图把版面撑破 */
.ql-editor img {
  max-width: 100%;
  height: auto;
  cursor: pointer;
}
.editor,
.ql-toolbar {
  white-space: pre-wrap !important;
  line-height: normal !important;
}
.quill-img {
  display: none;
}
.ql-snow .ql-tooltip[data-mode='link']::before {
  content: '请输入链接地址:';
}
.ql-snow .ql-tooltip.ql-editing a.ql-action::after {
  border-right: 0;
  content: '保存';
  padding-right: 0;
}
.ql-snow .ql-tooltip[data-mode='video']::before {
  content: '请输入视频地址:';
}
.ql-snow .ql-picker.ql-size .ql-picker-label::before,
.ql-snow .ql-picker.ql-size .ql-picker-item::before {
  content: '14px';
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value='small']::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value='small']::before {
  content: '10px';
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value='large']::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value='large']::before {
  content: '18px';
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value='huge']::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value='huge']::before {
  content: '32px';
}
.ql-snow .ql-picker.ql-header .ql-picker-label::before,
.ql-snow .ql-picker.ql-header .ql-picker-item::before {
  content: '文本';
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value='1']::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value='1']::before {
  content: '标题1';
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value='2']::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value='2']::before {
  content: '标题2';
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value='3']::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value='3']::before {
  content: '标题3';
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value='4']::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value='4']::before {
  content: '标题4';
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value='5']::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value='5']::before {
  content: '标题5';
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value='6']::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value='6']::before {
  content: '标题6';
}
.ql-snow .ql-picker.ql-font .ql-picker-label::before,
.ql-snow .ql-picker.ql-font .ql-picker-item::before {
  content: '标准字体';
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value='serif']::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value='serif']::before {
  content: '衬线字体';
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value='monospace']::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value='monospace']::before {
  content: '等宽字体';
}
</style>
