# exam-platform 项目约定（长期）

## 技术栈

RuoYi-Cloud-Plus 2.6.2 微服务（Spring Boot 3 + Dubbo + MyBatis-Plus + 多租户） + Vue3/TS/Element Plus/UnoCSS 前端。
后端 `exam-back/`，前端 `exam-front/`。数据库分库：`ry-exam`（主库）与 `ry-exam-answer`（答题库），跨库一律走 Dubbo。

## 用户硬性约束

- 不改动公共模块（ruoyi-common / ruoyi-api 公共部分等）。
- 不改已存在的表结构（需要时另出 update 脚本，且幂等）。
- **不自动执行 `mvn` 编译和 `git push`**，需要时提示用户自己跑。
- **每完成一批改动就 `git add` + `git commit`（只提交不 push）**（2026-10-01 起用户明确要求）。
  起因：`exam-front/src/api` 与 `views` 被误删，因长期「不自动提交」导致当天全部工作无法找回。

## mapstruct-plus 转换器（踩过坑，务必遵守）

`MapstructUtils.convert(src, XxxVo.class)` **没有兜底**，找不到转换器直接抛
`io.github.linpeilie.ConvertException: cannot find converter from A to B`。转换器只能靠生成：

- 同模块 VO：VO 上加 `@AutoMapper(target = 实体.class)`（参照 `ExamVo` / `QuestionVo`），
  生成 `XxxToYyyVoMapper`。
- 跨模块 Remote*Vo（VO 在 ruoyi-api-*，实体在业务模块）：在业务模块
  `domain/convert/` 写 `@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
  unmappedTargetPolicy = ReportingPolicy.IGNORE) interface XxxConvert extends BaseMapper<实体, RemoteXxxVo>`。
  已有 `ExamConvert` / `PaperConvert` / `QuestionConvert` / `ExamRecordConvert` / `ExamAnswerConvert`。
- 排查：看 `ruoyi-xxx/target/generated-sources/annotations` 有没有生成 `XxxToYyyMapper.java`。

## Vue 3.5 的 watch 会同步执行 getter（TDZ 坑）

`<script setup>` 里 `watch(() => x.y, cb)` **必须写在 `const x = ...` 之后**：
即便没有 `immediate`，Vue 创建 watch 时也会同步跑一次 getter 取初值
（`@vue/reactivity` `watch()`：`immediate ? job(true) : oldValue = effect.run()`），
变量还在 TDZ 就抛 `ReferenceError: Cannot access 'x' before initialization`。
`computed` 是惰性的可以后置；普通函数体里引用后置变量也没事（运行时才调）。
本项目在 `views/system/exam/edit/index.vue` 上踩过一次（考试编辑页白屏）。

## 考试时间口径（迟到 / 入场）

判定一律走 `ExamRecordServiceImpl` 的三个方法，别再直接比 `startTime`：

- `isNotStarted(now, exam)`：留 `EARLY_GRACE_SECONDS = 10` 秒提前量（抵消考生机器与服务端时钟差）。
- `latestEntryTime(exam)`：开始时间 + 迟到分钟（`allowLate=1` 才算）+ `ENTRY_GRACE_SECONDS = 60` 秒缓冲。
  **「不允许迟到」管的是晚到几分钟的人，不是晚一秒就把守时的人踢出去。**
- `isLate(now, exam)`：超过最晚入场时间才算迟到。
- `ExamCenterVo` 带 `serverTime`（前端倒计时的准绳，别信本地时钟）与 `latestEntryTime`。
- 前端 `views/exam/center/index.vue`：未开始显示倒计时，到点先乐观放开按钮再静默刷列表。

## 其它已确认的坑

- **`async` 函数带默认参数后不能直接当事件处理器**：`@click="loadList"` 会把 MouseEvent
  传进第一个形参（如 `loadList(silent = false)`），要写 `@click="() => loadList()"`。
- **考生端链接必须由 `src/utils/joinLink.ts` 的 `buildJoinLink()` 生成**：
  `VITE_APP_CONTEXT_PATH` 本地是 `'/'`，和 `/exam/join/` 直接字符串拼接会得到
  `http://host//exam/join/xxx` 双斜杠链接（contextPath 带尾斜杠同理）。工具内部会
  把 path 段的连续斜杠压成一个。禁止再手写 `${location.origin}${VITE_APP_CONTEXT_PATH}` 拼接。

- 实体继承 `TenantEntity` 时 `BaseEntity` **不含 delFlag**，需自己声明 `private Long delFlag`。
- `@TableLogic` 会让 `selectOne` / `updateById` 自动带 `is_deleted=0`，要找已删行必须手写 SQL
  （租户条件仍由拦截器追加）；且更新前要先 `restore()`。
- `lqw.ne(field, val)` 对 NULL 无效，要写 `and(w -> w.isNull(f).or().ne(f, val))`。
- **前端禁止对任何 ID 做 `Number()` / `parseInt()`**：雪花ID 19 位超过
  `Number.MAX_SAFE_INTEGER`，一转就丢精度（2105488649030184961 → 2105488649030185000）。
  后端 `JacksonConfig` 已注册 `BigNumberSerializer`（Long/BigInteger → 字符串），
  前端全程字符串透传，校验用 `/^\d+$/`。
- `sys_dict_type` / `sys_dict_data` **没有** `visible` / `status` 列（那是 `sys_menu` 的）。
- `org.dromara.common.core.utils.StringUtils` **没有** `containsIgnoreCase`。
- `TableDataInfo.build(List<T>, IPage<T>)` 是「假分页」重载；三元里混 `new Page<>()` 与
  `pageQuery.build()` 泛型推断会失败，先声明成局部变量。
- 前端跑 eslint 超过 120s，要放后台；`vite build` 在沙箱里会被 `CODEBUDDY_BROKER_TIMEOUT` 打断
  （环境限制，不是代码问题），改用 `vue-tsc --noEmit` 做类型检查。

## 字典约定

考试域下拉统一走 RuoYi 字典：种子数据在 `script/sql/update/update_exam_dict.sql`（幂等），
前端统一用 `src/hooks/useExamDicts.ts`（字典优先 + 代码兜底）与内置 `<dict-tag>`。
多租户下字典 tenant_id 要跟随 `sys_yes_no` 的租户。
