# MEMORY.md — exam-front 项目长期笔记

## 项目概况
- RuoYi-Vue-Plus 5.X 的 plus-ui 前端（Vue3.5 + TS + Element Plus 2.13 + Vite 7 + UnoCSS + Pinia + vxe-table）
- 后端代理：`VITE_APP_BASE_API` → `http://localhost:8080`
- 当前分支 `5.X`，业务方向是**在线考试/题库系统**
- 核心业务模块在 `src/api/system` 与 `src/views/system` 下：`bank`(题库) `question`(试题) `option`(试题选项) `tag` `tagRel` `media`

## 重要约定
- `vue-tsc --noEmit` 在本仓库**长期有存量报错**（DictTag / layout Settings / monitor 日期选择等一二十处），
  不是新增代码引入的。改完代码后请用 `grep -E "<你改的模块>"` 过滤自己的文件确认干净即可，不要试图修全仓库。
- ESLint 规则以 prettier 为主，改动后统一 `npx eslint --fix <files>` 再提交。
- `PageQuery` 的 `pageNum` / `pageSize` 是**必填**，分页查询参数不能只传 `pageSize`。
- 组件 `Editor`（Quill 封装）没有被全局注册，使用处需显式 `import Editor from '@/components/Editor/index.vue'`。
- 富文本判空不能直接 `required`，要剥掉 HTML 标签后判空（参考 `edit.vue` 的 `stripHtml`）。

## 试题模块设计约定（2026-09-28 重构）
- 编辑页是**独立整页** `src/views/system/question/edit.vue`（路由 `QuestionEdit`，hidden 常量路由），
  不使用弹窗；列表页 `index.vue` 只负责列表 + 跳转。
- 试题与选项**嵌套一次性提交**：`question.options` 数组 + `answer` JSON 串。
- **对错不落 option 表**，统一由 `question.answer` 的 JSON 结构描述。
- 题型的全部行为开关集中在 `src/views/system/question/questionMeta.ts`，加新题型只改这个文件。
- answer JSON 结构约定：
  - 选项型：`{ rightKeys: ['A','C'] }`
  - 填空：`{ blanks: [{ answers: ['张三'] }] }`
  - 简答/论述/文件上传：`{ answer: '<p>富文本</p>' }`
  - 代码题：`{ language: 'java', answer: '...', remark: '...' }`
  - 匹配题：`{ pairs: [{ left: 'CPU', right: '中央处理器' }] }`
- 列表页「试题管理」菜单路由地址是 **`/tool/question`**（2026-09-29 修正，之前误配成 `/system/question`）。
  同步改三处：edit.vue 的 `QUESTION_LIST_PATH`、router/index.ts 的编辑页 `path`（`/tool/question/edit/:questionId?`）
  与 `activeMenu`。注意：菜单挂在 tool 下，但视图文件仍在 `src/views/system/question/`（后端菜单 component 指向这里）。
- 编辑页返回走 `goBack()`：有历史来源页则 `proxy.$tab.closePage(currentRoute)` + `router.back()`，
  无历史则 `proxy.$tab.closeOpenPage({ path: QUESTION_LIST_PATH })`，保证标签页不残留。
