# exam-platform 项目约定（长期）

## 技术栈

RuoYi-Cloud-Plus 2.6.2 微服务（Spring Boot 3 + Dubbo + MyBatis-Plus + 多租户） + Vue3/TS/Element Plus/UnoCSS 前端。
后端 `exam-back/`，前端 `exam-front/`。数据库分库：`ry-exam`（主库）与 `ry-exam-answer`（答题库），跨库一律走 Dubbo。

## 用户硬性约束

- 不改动公共模块（ruoyi-common / ruoyi-api 公共部分等）。
- 不改已存在的表结构（需要时另出 update 脚本，且幂等）。
- **不自动执行 `mvn` 编译和 `git push`**，需要时提示用户自己跑。

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

## 其它已确认的坑

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
