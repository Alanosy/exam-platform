# exam-platform ｜ 「砚考」在线考试系统

#### 友情提示

> 1. 文档地址: [exam-doc](https://doc.alan.org.cn)
> 2. 如果本项目对你有帮助，欢迎点个 ⭐ Star 支持一下，非常感谢！
> 3. 项目处于持续开发中，部分功能仍在完善，遇到问题欢迎提 Issue

#### 项目介绍

[![GitHub](https://img.shields.io/github/stars/Alanosy/exam-platform?style=social&label=Github%20Stars)
![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://gitee.com/alanosy/exam-platform/LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-blue.svg)]()
[![JDK-17](https://img.shields.io/badge/JDK-17-green.svg)]()
[![JDK-21](https://img.shields.io/badge/JDK-21-green.svg)]()

「砚考」是一款开箱即用的在线考试系统，覆盖 **出题 → 组卷 → 考试 → 监考 → 阅卷 → 统计 → 发证 → 错题巩固** 的完整闭环，支持学生、教师、管理员三种角色。

本仓库采用**单仓多模块（monorepo）**结构：

- `exam-back/` —— 微服务后端，基于 [RuoYi-Cloud-Plus](https://gitee.com/dromara/RuoYi-Cloud-Plus) `2.6.2`
- `exam-front/` —— 前端，基于 Vue3 + TypeScript + Element Plus + Vite

后端把考试业务拆成 10 个可独立部署的微服务（考试、题库、试卷、答题、阅卷、统计、证书、练习、监考、AI），服务间经 Nacos 注册发现、Dubbo 调用，网关统一鉴权限流。

##### 它把考试里几件麻烦事做实了

- **组卷不用一道道挑** —— 除手工选题外，可按「题库 + 题型 + 难度 + 分值」配一条规则随机抽题，规则能保存复用，候选不足时提前提示
- **防作弊不是摆设** —— 切屏离屏、窗口失焦、复制剪切粘贴、右键、退出全屏、多标签页、开发者工具等 13 类行为全程记录并计数，摄像头定时抓拍留证；达到上限时**由服务端判定强制交卷**，改前端绕不过去，同时对考生透明展示规则与已用次数
- **判分不用干等** —— 客观题交卷即自动出分（正确 / 半对 / 错误三态），主观题按「考试 → 答卷 → 逐题」三级人工阅卷，每次改分留审计日志
- **考完立刻有数** —— 分数段分布、逐题正确率 / 难度 / 区分度、选项分布与易错项、知识点掌握度与薄弱分级；汇总表每 10 分钟定时重算兜底，也可手动重算
- **结果能沉淀** —— 及格自动颁发证书（模板支持占位符、印章、背景图、横竖版与有效期），答错和半对的题自动进入错题本，可反复重练、记笔记、标记掌握
- **答题不怕掉线** —— 逐题自动保存、中途退出可断点续答、倒计时归零自动交卷（前端主动提交 + 后端定时兜底双保险），重复交卷幂等

##### 关于 AI

AI 能力正在接入中：Python Agent（`ruoyi-exam-agent`）已完成出题、阅卷、推荐、试卷分析四类能力，Java 侧网关与模型配置表已就绪，**接线工作进行中**，当前 AI 阅卷会返回「未接入」。

****

#### 功能介绍

| 功能       | 功能描述 |
| ---------- | -------- |
| 题库管理   | 题库的增删改查与导出，支持分类树归类、可见性（私有 / 公开）与草稿 / 正常 / 归档三态，可在题库内直接挂载和管理试题 |
| 试题管理   | 支持单选、多选、判断、填空、简答、论述、代码、文件上传、匹配共 9 种题型，含富文本题干与解析、难度、默认分值；提供 Excel 模板导入（整批校验，失败整批回滚并逐行提示）与导出 |
| 试卷管理   | 手工选题与按规则随机抽题两种组卷方式，规则可保存复用、候选不足时给出提示；可配总分与及格分、题目 / 选项乱序、自动判分与人工阅卷开关、部分得分与答错扣分 |
| 考试管理   | 正式 / 练习两类考试，可配时间窗、限时、迟到入场、重考次数、答案展示时机；考生准入支持**白名单（按部门选人）** 与**公开链接（加入码 + 密码 + 有效期）** 两种方式；含 9 项防作弊规则与证书模板绑定，支持保存并一键发布，并提供考试情况看板与导出 |
| 阅卷管理   | 客观题交卷即自动判分（区分正确 / 半对 / 错误三态），主观题按「考试 → 答卷 → 逐题」三级人工阅卷并记录改分日志；双评仲裁与 AI 预评已预留接口但尚未接入 |
| 考试统计   | 基于预计算汇总表并提供每 10 分钟定时重算兜底，输出大盘概览、分数段分布、考生明细与排名、逐题正确率 / 难度 / 区分度、选项分布与易错项、知识点掌握度与薄弱分级，支持手动重算与作废答卷不入统 |
| 考试记录   | 考生查看自己参加过的每一场考试与答卷，可按考试、类型、状态、是否及格筛选，并逐题回看自己的作答、正确答案、解析与得分 |
| 错题本     | 交卷后答错与半对的题目自动进入错题本，支持按来源分组查看、重练并即时判分看解析、记笔记、标记已掌握、移出与恢复，并保留每次重做的历史 |
| 错题管理   | 管理端错题维护页面尚未实现，当前错题能力集中在考生端错题本；基于题库 / 章节的刷题练习仍在规划中 |
| 我的证书   | 考试及格后自动颁发（含主观题的卷子待阅卷完成后再发，避免分数不一致），证书墙展示有效性标识（有效 / 已过期 / 已吊销）并支持打印；模板由管理端配置占位符、印章、背景、横竖版与有效期 |
| 监考中心   | 准实时监控（10 秒静默轮询）查看自己考试的监考会话、风险概览、事件流水与摄像头抓拍，考生进入答题页后自动产生记录 |
| 防作弊服务 | 检测切屏离屏、窗口失焦、复制 / 剪切 / 粘贴、右键、进出全屏、摄像头抓拍与拒权、开发者工具、多标签页等 13 类行为；规则由考试单独配置，达到上限时**由服务端判定强制交卷**（前端无法绕过），并向考生透明展示规则与计数 |
| AI服务     | 开发中：Java 网关与 AI 模型配置已就绪，Python Agent 已实现出题、阅卷、推荐、试卷分析四类能力，**但尚未与 Java 侧接线**，AI 阅卷目前返回「未接入」 |



#### 项目展示

<table>
    <tr>
        <td><img src="http://bucket.alan.org.cn/blog/2026/10/02/15-22-35-c0e20243086e1f31b51949fd262cd167-5b1234.png"/></td>
        <td><img src="http://bucket.alan.org.cn/blog/2026/10/02/16-54-13-8c24c3c3b11b2c4beb21e4b752418b61-55bd73.png"/></td>
      	<td><img src="http://bucket.alan.org.cn/blog/2026/10/02/16-56-09-38f4c6ee221536f6a158c42b27c5d99c-347b46.png"/></td>
    </tr>
  <tr>
    <td><img src="http://bucket.alan.org.cn/blog/2026/10/02/17-26-29-120c709884c644ecff863fae9109b945-c76290.png"/></td>
    <td><img src="http://bucket.alan.org.cn/blog/2026/10/02/17-25-22-aef85c1edcdfae86bf9e94103fd9ecaa-06b4cf.png"/></td>
    <td><img src="http://bucket.alan.org.cn/blog/2026/10/02/17-24-50-42d47142c854b1612a8b4c3545e11aa5-46de92.png"/></td>
  </tr>
</table>


#### 仓库结构

```
exam-platform/
├── exam-back/       # 微服务后端（RuoYi-Cloud-Plus，开发中）
├── exam-front/      # 前端（Vue3 + TS + Element Plus，开发中）
├── .gitignore
├── AGENTS.md
├── LICENSE
└── README.md
```

#### 开发环境

| 工具  | 版本    | 下载                                                         |
| ----- | ------- | ------------------------------------------------------------ |
| JDK   | 17 / 21 | https://www.oracle.com/cn/java/technologies/downloads/        |
| MySQL | 8       | https://dev.mysql.com/downloads/mysql/                       |
| Redis | 7       | https://redis.io/download                                    |
| Nacos | 2.x     | https://nacos.io/                                            |
| Node  | 18+     | https://nodejs.org/                                          |

#### 软件架构图

![Plus部署架构图](http://bucket.alan.org.cn/blog/2026/10/02/16-37-21-1a99c97a84b9df30782568688df1d715-3c95d5.png "Plus部署架构图.png")

#### 技术架构

| 层级 | 位置           | 架构                       | 技术栈                                                       |
| :--- | :------------- | :------------------------- | :----------------------------------------------------------- |
| 后端 | `exam-back/`   | 微服务（RuoYi-Cloud-Plus） | Spring Boot 3.x + Spring Cloud + Nacos + Dubbo + Sa-Token + MyBatis-Plus + MySQL + Redis |
| 前端 | `exam-front/`  | 单页应用（SPA）            | Vue3 + TypeScript + Element Plus + Vite + Pinia               |

##### 后端（exam-back）

- 基于 [RuoYi-Cloud-Plus](https://gitee.com/dromara/RuoYi-Cloud-Plus) `2.6.2` 的微服务后端
- 微服务模块划分：考试管理（exam-manage）、题库服务（exam-question）、试卷服务（exam-paper）、答题服务（exam-answer）、阅卷服务（exam-mark）、统计服务（exam-stat）、证书服务（exam-cert）、练习服务（exam-practice）、防作弊服务（exam-proctor）、AI 服务（exam-ai）
- 基础设施：Nacos（注册/配置中心）、Spring Cloud Gateway（网关）、Dubbo（RPC）、Sentinel（限流熔断）、Seata（分布式事务）、Redis、MySQL
- AI 能力：`ruoyi-agent/ruoyi-exam-agent`（Python Agent）提供出题、阅卷、推荐、试卷分析等能力，经 Java 侧 AI 网关对接

##### 前端（exam-front）

- 基于 Vue3 + TypeScript + Element Plus + Vite，状态管理用 Pinia
- 按模块组织 `api` / `views` / `store`，菜单与按钮权限由后端驱动
- ⚠️ **注意**：项目仍在开发中，部分功能尚未完善

#### 联系方式

QQ群：群1:1034380536、群2:1098802068

微信公众号：程序员阿祥

邮箱：fignet@163.com

#### 最后

本项目还在开发当中，存在着 bug 还请谅解，也希望你加入到我们一起开发该项目

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

## 许可证

[Apache License 2.0](https://github.com/macrozheng/mall/blob/master/LICENSE)

Copyright (c) 2018-2024 macrozheng
