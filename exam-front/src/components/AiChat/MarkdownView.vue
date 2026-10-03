<template>
  <div class="md-view" v-html="html"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';

/**
 * Markdown 渲染
 *
 * 两个刻意的选择：
 * 1. html: false —— AI 输出的内容不可信，禁掉原始 HTML 避免 XSS，
 *    代码块走 highlight.js 自己转义，不把 innerHTML 的口子开给模型。
 * 2. breaks: true —— 聊天场景里模型经常用单换行分句，不开这个会糊成一坨。
 */
const props = defineProps<{ content: string }>();

const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  highlight(code: string, lang: string) {
    const escaped = md.utils.escapeHtml(code);
    if (lang && hljs.getLanguage(lang)) {
      try {
        const highlighted = hljs.highlight(code, { language: lang, ignoreIllegals: true }).value;
        return `<pre class="md-code"><code class="language-${lang}">${highlighted}</code></pre>`;
      } catch {
        return `<pre class="md-code"><code>${escaped}</code></pre>`;
      }
    }
    return `<pre class="md-code"><code>${escaped}</code></pre>`;
  }
});

const html = computed(() => md.render(props.content || ''));
</script>

<style scoped lang="scss">
.md-view {
  font-size: 13px;
  line-height: 1.7;
  color: var(--el-text-color-primary);
  word-break: break-word;

  :deep(p) {
    margin: 0 0 8px;
  }

  :deep(p:last-child) {
    margin-bottom: 0;
  }

  :deep(h1),
  :deep(h2),
  :deep(h3),
  :deep(h4) {
    margin: 12px 0 6px;
    font-weight: 600;
    line-height: 1.4;
  }

  :deep(h3) {
    font-size: 14px;
  }

  :deep(ul),
  :deep(ol) {
    margin: 4px 0 8px;
    padding-left: 18px;
  }

  :deep(li) {
    margin: 2px 0;
  }

  :deep(blockquote) {
    margin: 8px 0;
    padding: 6px 10px;
    color: var(--el-text-color-secondary);
    background: var(--el-fill-color-light);
    border-left: 3px solid var(--el-color-primary-light-5);
    border-radius: 0 4px 4px 0;
  }

  :deep(code) {
    padding: 1px 4px;
    font-size: 12px;
    background: var(--el-fill-color-light);
    border-radius: 3px;
  }

  :deep(pre.md-code) {
    margin: 8px 0;
    padding: 10px 12px;
    overflow-x: auto;
    background: #f6f8fa;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 6px;

    code {
      padding: 0;
      background: transparent;
    }
  }

  :deep(table) {
    width: 100%;
    margin: 8px 0;
    font-size: 12px;
    border-collapse: collapse;
  }

  :deep(th),
  :deep(td) {
    padding: 6px 8px;
    text-align: left;
    border: 1px solid var(--el-border-color-lighter);
  }

  :deep(th) {
    background: var(--el-fill-color-light);
    font-weight: 600;
  }

  :deep(a) {
    color: var(--el-color-primary);
    text-decoration: none;
  }

  :deep(hr) {
    margin: 10px 0;
    border: none;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}
</style>
