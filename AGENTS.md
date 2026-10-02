# AGENTS.md - AI Agent 执行规范
> 本文件仅用于指导 AI Agent 在当前仓库执行编码任务，人类开发者请阅读 README.md

## 1. 项目概述

exam-platform 是一个在线考试系统，采用 monorepo 结构，包含三个部分：

| 目录 | 说明 | 是否允许 AI 操作 |
| :--- | :--- | :--- |
| `old-exam/` | 旧版单体 Spring Boot 考试系统（已上线，归档保留） | ❌ **禁止读取和修改** |
| `exam-back/` | 新版微服务后端，基于 [RuoYi-Cloud-Plus](https://gitee.com/dromara/RuoYi-Cloud-Plus) 2.6.2 | ✅ 允许 |
| `exam-front/` | 新版前端，基于 Vue3 + TypeScript + Element Plus + Vite | ✅ 允许 |

**核心规则：AI Agent 只允许在 `exam-back/` 和 `exam-front/` 目录下工作，禁止读取、修改 `old-exam/` 下的任何文件。** 如需了解旧版逻辑，请通过 README.md 或询问用户，不要进入 `old-exam/` 目录。

技术栈：
- 后端：Spring Boot 3.x + Spring Cloud + Nacos + Dubbo + Sa-Token + MyBatis-Plus + MySQL + Redis
- 前端：Vue 3.5 + TypeScript + Element Plus + Vite + Pinia

---

## 2. Agent 角色与权限

你是本项目的辅助开发助手，**仅负责 exam-back 和 exam-front 的开发**。

✅ 允许：
- 阅读 `exam-back/`、`exam-front/` 下的源码、配置、SQL 脚本
- 查看 git 提交记录
- 在 `exam-back/`、`exam-front/` 内创建/修改代码文件，遵循项目编码规范
- 执行编译检查、单元测试命令
- 梳理模块逻辑，生成接口文档
- 每次执行完，执行git commit提交到本地

❌ 禁止：
- **读取或修改 `old-exam/` 目录下的任何文件**（旧版代码，已归档）
- 未经确认直接修改数据库 DDL、核心配置文件（如 Nacos 配置、gateway 路由）
- 重构大量核心业务代码（超过 3 个文件必须先汇报方案）
- 直接推送代码到远程仓库，禁止执行 `git push`
- 删除任何文件，除非明确收到用户指令
- 修改 `AGENTS.md` / `README.md` 除非用户明确要求
- 新增第三方依赖，必须先询问用户

---

## 3. 目录结构理解（必须熟读）

### 3.1 后端 exam-back/（RuoYi-Cloud-Plus 微服务）

```
exam-back/
├── ruoyi-api/                  # 对外 API 接口定义（Dubbo 接口、DTO、VO）
│   └── ruoyi-api-exam-{模块}/  # 各考试模块的 API
├── ruoyi-modules/              # 业务模块实现
│   └── ruoyi-exam-{模块}/      # 考试业务模块
│       └── src/main/java/org/dromara/exam/{模块}/
│           ├── controller/     # 接口层（REST API）
│           ├── service/        # 业务接口
│           │   └── impl/       # 业务实现
│           ├── mapper/         # MyBatis-Plus Mapper
│           ├── domain/         # 实体 / BO / VO / convert
│           └── dubbo/          # Dubbo 服务实现（供其他服务调用）
├── ruoyi-common/               # 公共模块（工具、常量、基础组件）
├── ruoyi-gateway/              # 网关
├── ruoyi-auth/                 # 认证服务
├── ruoyi-visual/               # 监控（nacos、monitor）
├── ruoyi-agent/                # AI Agent 相关
├── script/                     # 部署脚本、SQL 脚本
└── docs/                       # 文档
```

#### ruoyi-modules/ 模块清单（描述来自各模块 pom.xml）

**考试业务模块（exam-*）：**

| 模块 | pom.xml 描述 | 说明 |
| :--- | :--- | :--- |
| `ruoyi-exam-manage` | 考试管理服务 | 考试创建、发布、编排等核心管理 |
| `ruoyi-exam-question` | 题库服务 | 题库与试题的管理 |
| `ruoyi-exam-paper` | 试卷服务 | 试卷组卷与管理 |
| `ruoyi-exam-answer` | 答题服务 | 考生答题、交卷逻辑 |
| `ruoyi-exam-mark` | 阅卷服务 | 客观题自动阅卷、主观题人工/AI 阅卷 |
| `ruoyi-exam-stat` | 考试统计服务 | 成绩、正确率等统计分析 |
| `ruoyi-exam-cert` | 证书服务 | 证书生成与管理 |
| `ruoyi-exam-practice` | 练习服务 | 刷题、练习功能 |
| `ruoyi-exam-proctor` | 防作弊服务 | 监考、切屏检测等防作弊 |
| `ruoyi-exam-ai` | AI 服务（Java 网关层） | AI 能力的 Java 网关层封装 |

**框架基础模块（RuoYi-Cloud-Plus 自带）：**

| 模块 | pom.xml 描述 | 说明 |
| :--- | :--- | :--- |
| `ruoyi-system` | 系统模块 | 用户、角色、菜单、部门、字典等 |
| `ruoyi-resource` | 资源服务 | OSS 文件存储、短信、邮件等 |
| `ruoyi-gen` | 代码生成 | 前后端代码生成器 |
| `ruoyi-job` | 任务调度模块 | 定时任务调度 |
| `ruoyi-workflow` | 工作流模块 | 工作流审批引擎 |

### 3.2 前端 exam-front/（Vue3 + TS）

```
exam-front/
└── src/
    ├── api/            # 接口请求封装（按模块分目录）
    ├── views/          # 页面视图（exam、system、monitor、workflow 等）
    ├── components/     # 公共组件
    ├── store/          # Pinia 状态管理
    ├── router/         # 路由
    ├── layout/         # 布局组件
    ├── utils/          # 工具函数
    ├── plugins/        # 插件
    ├── directive/      # 自定义指令
    ├── enums/          # 枚举
    ├── hooks/          # 组合式函数
    ├── lang/           # 国际化
    ├── assets/         # 静态资源
    └── types/          # TypeScript 类型声明
```

---

## 4. 编码规范（强制遵守）

### 后端（exam-back）
1. 包名：`org.dromara.exam.{模块}`，遵循 RuoYi-Cloud-Plus 规范
2. 命名：Java 大驼峰类名、小驼峰方法/变量；数据库字段下划线，对应实体驼峰
3. 分层：controller → service → mapper，跨服务调用走 Dubbo（在 `dubbo/` 包实现接口）
4. 注释：核心业务逻辑必须写注释；getter/setter 无需注释
5. 异常：使用框架统一异常 `ServiceException`，不要新建全局异常类
6. SQL：禁止写 `select *`；分页使用 MyBatis-Plus `Page` + `TableDataInfo`
7. 事务：`@Transactional(rollbackFor = Exception.class)`
8. 日志：使用 `@Slf4j`，关键操作记录 info 日志

### 前端（exam-front）
1. 命名：组件大驼峰，变量/函数小驼峰，文件名与组件名一致
2. 接口：统一在 `src/api/{模块}/` 下封装，类型定义在同目录 `types.ts`
3. 状态：使用 Pinia（`src/store/modules/`），不要在组件里乱放全局状态
4. 样式：使用 scoped 样式，公共样式放 `src/assets/styles/`
5. 注释：复杂逻辑、工具函数必须写注释

---

## 5. 任务处理流程（Agent 必须按这个步骤执行）

1. **理解需求** → 先确认需求边界，不清楚主动提问
2. **阅读代码** → 只在 `exam-back/`、`exam-front/` 内定位关联类/文件，梳理现有逻辑
3. **输出方案** → 告知要修改哪些文件，等待用户确认
4. **修改代码** → 同步补充必要的单元测试/类型定义
5. **验证** → 本地编译/测试验证，输出改动清单
6. **生成 commit 描述** → **不自动提交**，由用户决定

---

## 6. 命令白名单 & 黑名单

✅ 允许执行：
- `mvn compile`、`mvn test`、`mvn -pl {模块} compile`
- `npm run dev`、`npm run build`、`npm run lint`
- `git status`、`git diff`、`git log`、`git commit`
- 本地 MySQL 只读查询（见第 10 节）。**注意：本机没有装 `mysql` 客户端，
  只能用 `/Users/alan/.workbuddy/binaries/python/envs/default/bin/python` + `pymysql` 查询**

❌ 禁止执行：
- `git push`、`rm -rf`、`drop table`、`truncate`
- **未经用户明确确认的 `INSERT` / `UPDATE` / `DELETE` / `DROP` / `ALTER`**
- `curl` 访问外网、修改系统环境变量
- 进入或操作 `old-exam/` 目录的任何命令

---

## 7. 冲突与边界规则

- **old-exam/ 是禁区**：任何情况下都不要读取或修改该目录，即使它看起来与当前问题相关
- 如果发现现有代码逻辑矛盾、技术债务，先告知用户，不要擅自重构
- 改动如果涉及多模块联动（如同时改 exam-back 和 exam-front），先画简单逻辑说明
- 遇到数据库变更，优先生成 SQL 脚本放到 `exam-back/script/sql/`，不直接执行
- 当需求模糊时，停止编码，向用户提问澄清，不要自行猜测

---

## 8. 输出格式约定

- 代码块：完整可编译代码，不要省略关键导入
- 文件改动：每次修改后列出 `文件路径：改动简述`
- 方案汇报：精简，不要废话，不编造不存在的类/方法
- 禁止输出 `old-exam/` 下的任何文件路径

---

## 9. 额外项目特有规则

- 后端统一返回体：`R<T>`（来自 `ruoyi-common-core`）
- 后端分页返回：`TableDataInfo<T>`
- 接口需要权限注解：`@SaCheckPermission("exam:xxx:list")`
- 跨服务调用使用 Dubbo `@DubboReference`，不要直接 HTTP 调用其他微服务
- 前端请求统一走 `src/utils/request.ts` 封装的 axios 实例
- 不引入新的第三方依赖，新增依赖必须先询问用户

---

## 10. 本地数据库访问（允许，默认只读）

**允许连接本地 MySQL 辅助开发**：查表结构、核对字段、验证数据、定位线上/本地问题都可以直接查，
不用再向用户要连接信息。

### 10.1 连接信息

| 项 | 值 |
| :--- | :--- |
| host | `127.0.0.1` |
| port | `3306` |
| user | `root` |
| password | `ruoyi123` |

### 10.2 查询方式

**本机没有安装 `mysql` 命令行客户端**，不要尝试 `mysql -u...`。统一用：

```bash
/Users/alan/.workbuddy/binaries/python/envs/default/bin/python -c "
import pymysql
c = pymysql.connect(host='127.0.0.1', port=3306, user='root', password='ruoyi123', database='ry-exam')
cur = c.cursor()
cur.execute('select ...')
for r in cur.fetchall(): print(r)
"
```

（`pymysql` 已装在该 venv 里，别用系统 python，也别往全局装包。）

### 10.3 数据库清单与归属

| 数据库 | 用途 | 主要表 |
| :--- | :--- | :--- |
| `ry-exam` | **考试业务主库**（考试、题库、试卷、阅卷、防作弊、证书、白名单） | `exam`, `exam_user`, `exam_invite`, `exam_question`, `question`, `question_bank`, `question_option`, `paper`, `paper_question`, `exam_mark_*`, `exam_proctor_*`, `exam_certificate*` |
| `ry-exam-answer` | **答题库**（考生答卷，独立分库） | `exam_record`, `exam_answer` |
| `ry-cloud` | 系统/框架库（用户、角色、菜单、部门、字典、租户、OSS） | `sys_user`, `sys_role`, `sys_menu`, `sys_dept`, `sys_dict_type`, `sys_dict_data`, `sys_tenant`, `sys_oss*`, `gen_table*` |
| `ry-config` | Nacos 配置中心库 | `config_info`, `his_config_info` 等 |
| `ry-job` | 定时任务调度库（snail-job） | `sj_job`, `sj_job_task`, `sj_retry*` |
| `ry-seata` | 分布式事务库 | `global_table`, `branch_table`, `lock_table` |
| `ry-workflow` | 工作流库 | `flow_definition`, `flow_instance`, `flow_task` 等 |

> ⚠️ **`ry-exam` 与 `ry-exam-answer` 是两个库**，跨库一律走 Dubbo 调用，禁止写跨库 JOIN SQL。
> ⚠️ **`sys_menu` 在 `ry-cloud`**，不在 `ry-exam`。

### 10.4 边界（必须遵守）

- **默认只读**：`SELECT` / `SHOW` / `DESC` / `information_schema` 查询随意用。
- **写操作必须先说明再执行**：任何 `INSERT` / `UPDATE` / `DELETE` / `ALTER` / `DROP` / `TRUNCATE`
  都要先告诉用户「要改哪个库哪张表、改什么、为什么」，得到明确确认后再执行。
  （用户为了调试主动要求造数据/修数据除外，但仍需逐条报出 SQL。）
- **不改已有表结构**：结构变更一律生成幂等脚本放 `exam-back/script/sql/update/`，
  由用户自己执行；确需直连执行必须先确认。
- **注意多租户**：业务表基本都带 `tenant_id`，查询统计时**记得带租户条件**，
  否则会把所有租户的数据混在一起。
- **注意逻辑删除**：业务表普遍有 `del_flag`（0 未删 / 2 已删），查询要带 `del_flag = 0`，
  否则会统计到已删数据。
- **查不到表先看库**：`exam_*` 开头的表不一定都在 `ry-exam`，
  `exam_record` / `exam_answer` 在 `ry-exam-answer`；`exam_proctor_*` / `exam_certificate*` 在 `ry-exam`。
- 排查数据问题时，优先用数据库核实真实值，别只靠代码推测。
