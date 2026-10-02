# exam-platform 项目约定（长期）

## 技术栈与硬约束

RuoYi-Cloud-Plus 2.6.2 微服务（Spring Boot 3 + Dubbo + MyBatis-Plus + 多租户）+ Vue3/TS/Element Plus 前端。
后端 `exam-back/`，前端 `exam-front/`；分库 `ry-exam`（主库）／`ry-exam-answer`（答题库），跨库一律走 Dubbo。

- 不改公共模块（ruoyi-common / ruoyi-api 公共部分）；不改已有表结构（另出幂等 update 脚本）。
- **不自动跑 `mvn`、不 `git push`**；**每批改动后 `git add` + `commit`（不 push）**。
- 数据库 DDL 变更只写脚本放 `exam-back/script/sql/update/`，不直接执行。

## 后端坑

- **mapstruct-plus**：`MapstructUtils.convert` 无兜底，找不到转换器直接抛 `ConvertException`。
  同模块 VO 加 `@AutoMapper(target = 实体.class)`；跨模块 Remote*Vo 在 `domain/convert/` 写
  `@Mapper(...SPRING) interface XxxConvert extends BaseMapper<实体, RemoteXxxVo>`。
  **Remote*Vo 字段类型必须与实体完全一致**（Long↔Boolean 会编译失败，0/1 开关统一 Long）。
- `@AutoMapper` 类的 extends 目标不存在 → 注解处理器 NPE（栈里看不到业务类名），优先怀疑 extends 写错。
- `BaseEntity` 包是 `org.dromara.common.mybatis.core.domain.BaseEntity`；它 extends commons-lang3
  StringUtils，所以 `isBlank/defaultIfBlank` 可用。`org.dromara.common.core.utils.StringUtils` **没有** `containsIgnoreCase`。
- 继承 `TenantEntity` 时不含 delFlag，需自己声明 `private Long delFlag`。
- `@TableLogic` 自动带 `is_deleted=0`，查已删行要手写 SQL（先 `restore()`）。
- `lqw.ne(field,val)` 对 NULL 无效 → `and(w -> w.isNull(f).or().ne(f,val))`。
- 跨服务 BO 的 `tenantId` 必须是 **String**（`TenantHelper.getTenantId()` 返回 String），写 Long 编译失败。
- MP 3.5.5：`LambdaQueryWrapper.select(String...)` 不存在，聚合用 `QueryWrapper.select(true, List.of(...))`+groupBy；
  `selectMaps` 的 key 受驼峰配置影响，`examId`/`exam_id` 两种都兜。
- `TableDataInfo.build(List, IPage)` 是假分页重载；三元里混 `new Page<>()` 会泛型推断失败。
  手工内存分页：`new Page<>(pageNum, pageSize, total)` → `setRecords` → `TableDataInfo.build(page)`。
- **导出 `@ExcelProperty` + `ExcelIgnoreUnannotated` + `ExcelUtil.exportExcel(list, "名", Vo.class, response)`。
  五参重载不存在**（第5参是下拉框不是表头）；异构多 sheet 只能走 Consumer 版：
  `ExcelUtil.exportExcel(Object.class, os, w -> w.write(new ArrayList<>(rows),
  ExcelWriterWrapper.sheetBuilder(i,"sheet名").head(XxxVo.class).build()))`，
  且这个重载**不自动写响应头**，要自己 `FileUtils.setAttachmentResponseHeader` + setContentType。
- **给管理端实时页面供数不要读 `stat_exam_*` 汇总表**（那是定时任务预计算的，`StatExamServiceImpl.overview`
  不会自动兜底重算，没跑过就是空）。「谁参考了 / 多少分」这类直接实时查，走
  `RemoteExamAnswerService.listRecordsByExam` + `RemoteMarkService.listPendingRecordIds`。
- `sys_menu` 在 **ry-cloud** 库；`exam_proctor_*` / `exam_certificate*` 在 ry-exam。
  `sys_dict_*` **没有** visible/status 列。

## 前端坑

- **禁止对任何 ID 做 `Number()`/`parseInt()`**（雪花 19 位丢精度），全程字符串透传，校验用 `/^\d+$/`。
- **`async` 函数带默认参数不能直接当事件处理器**（MouseEvent 会传进第一形参）→ `@click="() => loadList()"`。
- 考生端链接必须由 `src/utils/joinLink.ts` 的 `buildJoinLink()` 生成（防 `//` 双斜杠）。
- `el-tag`/`el-button` 的 `type` 只接受字面量联合 → 用 `Record<string, TagType>` 辅助函数取色。
- 分页取数：`const res: any = await xxx(); res?.rows ?? []`（拦截器已解包）。
- `watch(() => x.y, cb)` 必须写在 `const x` 之后（Vue 3.5 同步跑 getter，TDZ 报错）；`computed` 可后置。
- `v-if` 里的 ref 赋值后不能立刻取，先 `await nextTick()`；`display:none` 的 video 不解码（抓拍全黑）。
- 依赖 DOM 的副作用要等 `loading=false` + `nextTick()` 之后再启动。
- `document.hidden` 管不到切到其它应用，判离屏用 `document.hasFocus()`；状态类检测（全屏/焦点）要定时轮询兜底。
- 本地计数必须能被服务端周期纠正（心跳回传），否则刷新即「清零」。
- 检查手段：eslint 需放后台（>120s）；`vite build` 在沙箱会被超时打断，改用 `vue-tsc --noEmit`。

## 考试时间口径（ExamRecordServiceImpl）

- `isNotStarted`（提前 10s）/ `latestEntryTime`（开始+迟到分钟+60s 缓冲）/ `isLate`，别直接比 startTime。
- 入场窗口以 `latestEntryTime` 为准；前端乐观放开**严格不早于后端**、且**由时间实时算出**（不存内存标记）。
- **入场规则只管首次进场**：`startExam` 里「已有 answering 记录 → 返回 recordId」必须排在最前。
- 前端倒计时走 `src/hooks/useServerClock.ts`（`ExamCenterVo.serverTime`），轮询一律静默 `loadXxx(true)`。
- 状态提示一句话，具体时间交给倒计时（`src/utils/tip.ts` 的 `plainTip()`）。

## 路由：菜单生成地址怎么算（判断 404 用）

`SysMenu.getRouterPath()`：parent_id=0 且 type=M 且非外链 → `"/"+path`；
parent_id=0 且 type=C 且 is_frame=1（即 `isMenuFrame()`）→ 返回 `"/"`，
**真正地址落在 buildMenus 塞进 children 的那个 path 上**。
例：菜单 `考试统计` path=`stat` → 实际访问 `/stat`，**不是** `/system/stat`。

- 下钻详情页（不是一级功能入口）一律在 `router/index.ts` 注册 hidden 静态路由，
  别指望菜单：菜单只能盖到入口那一层。
- `meta.activeMenu` 必须写**菜单实际生成的地址**，写错就高亮不到侧边栏
  （`/system/exam/edit` 的 `activeMenu='/system/exam'` 就是错的，菜单生成的是 `/exam`）。
- 新增页面前先核对「页面里跳转写的路径」与「菜单/静态路由实际路径」是否对得上，
  两边各写一套是 404 的主要来源。

## 业务模块速查

- **证书 ruoyi-exam-cert**（9218，`/cert/**`）：`exam_certificate` 模板 + `exam_certificate_record` 快照颁发记录。
  颁发两处：`doSubmit`（无主观题及格）+ `writeBackMark(finished=true)`，异常只 warn。
- **防作弊 ruoyi-exam-proctor**（9219，`/proctor/**`）：session/event/snapshot 三表，规则只来自
  `exam.anti_cheat_config`；判定权在服务端，**阈值 > 0 才强制交卷**；`doSubmit` 调 `finishSession` 闭环。
- **考生准入 `exam.participant_type`**：white 白名单（默认）/ public 链接。
  链接方式写 `exam_invite` 不开 exam_record；白名单方式用 `exam_user`（按用户存，部门只是选人维度），
  接口 `GET/PUT /exam/{id}/whiteUsers`。
  **新增来源必须同时改 `listMyCenter()` 与 `startExam()` 两处**。
- **统计 ruoyi-exam-stat**（9216，`/stat/**`，原先是空壳）：只做「拼数据」——
  考试侧用轻量实体 `StatExam` 直读 exam 表（不引考试模块实体，避免服务焊死），
  答卷侧走 `RemoteExamAnswerService`（statRecords / trendSubmit / rankByExam）。
  首页总览唯一入口 `GET /stat/home/overview`，`@SaCheckPermission("system:exam:list")`；
  **答卷侧的量必须允许降级**（答题服务挂了降为 0，不能连带打不开首页）。
  `update_exam_stat.sql` 里那批 `stat_*` 汇总表是后续考试分析用的，首页暂时不读它们。
- **首页 `views/index.vue`** 是业务 Dashboard，按 `permissions` 是否含 `system:exam:list`
  分管理视角 / 考生视角；考生侧只复用已有接口（我的考试 / 错题 / 我的证书），不新增权限。
  底部固定「系统能力矩阵 + 项目定位说明」；「大屏模式」= screenfull 全屏根容器 +
  `.is-screen` 深色科技风，Esc 退出靠 `screenfull.on('change')` 同步配色。
  注意 `useUserStore()` **没有 name**，只有 nickname。
  **大屏里的交互不能依赖 teleport 到 body 的弹层**：全屏时浏览器只渲染被全屏的那个元素，
  el-dropdown / el-message / el-dialog 的弹层全在 body 下，**全屏后根本不显示**（曾经把
  「退出大屏」放进下拉菜单，结果进了大屏就出不来）。切换类操作一律做成内联按钮，
  并补一颗 `position: fixed` 的悬浮退出按钮（内容长，滚下去看不到顶部）。
  `screenfull.request/exit` 必须包 try/catch，await 抛错会让 `screenMode` 卡在 true。
- 控制器统一写 `@RequestMapping({"", "/xxx"})` 兼容网关 StripPrefix。

## 本地数据库（只读）

`127.0.0.1:3306` root/ruoyi123，**无 mysql 客户端**，用
`/Users/alan/.workbuddy/binaries/python/envs/default/bin/python` + pymysql。
库：ry-exam（主库）／ry-exam-answer（仅 exam_record、exam_answer）／ry-cloud（sys_*、sys_menu）。
写操作先报 SQL 经用户确认；查询带 `tenant_id` 与 `del_flag = 0`。详见 `AGENTS.md`。
