# exam-platform ｜ 「砚考」在线考试系统

#### 友情提示

> 1. 本项目已升级为微服务版本
> 2. 旧项目已迁移到old-exam文件夹中，**旧项目体验地址**：[项目体验地址](http://exam.alan.org.cn)
>    1. 管理员账号:admin 密码:123456
>    2. 教师账号:teacher 密码:123456
>    3. 学生账号:student 密码:123456
> 3. 文档地址: [exam-doc](https://doc.alan.org.cn)

#### 介绍

[![GitHub](https://img.shields.io/github/stars/Alanosy/exam-platform?style=social&label=Github%20Stars)
![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://gitee.com/alanosy/exam-platform/LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-blue.svg)]()
[![JDK-17](https://img.shields.io/badge/JDK-17-green.svg)]()
[![JDK-21](https://img.shields.io/badge/JDK-21-green.svg)]()
本项目致力于打造一款通用的在线考试系统，涵盖**考试管理、题库管理、试卷中心、刷题练习、成绩分析、AI 阅卷、证书管理**等核心功能，支持学生、教师、管理员三种角色。

当前仓库已升级为**单仓多模块（monorepo）**结构，同时包含旧版单体应用与新版微服务应用两套代码，便于历史追溯与并行开发。

**=>如果各位喜欢，麻烦各位大佬点点Star<=**

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
</table>

#### 仓库结构

```
exam-platform/
├── old-exam/        # 旧版：单体 Spring Boot 考试系统后端（已上线）
├── exam-back/       # 新版：微服务后端（RuoYi-Cloud-Plus，开发中）
├── exam-front/      # 新版：前端（Vue3 + TS + Element Plus，开发中）
├── .gitignore
├── LICENSE
└── README.md
```

#### 开发环境

| 工具  | 旧版 | 新版 | 下载                                                         |
| ----- | ---- | ---- | ------------------------------------------------------------ |
| JDK   | 17   | 17 / 21 | https://www.oracle.com/cn/java/technologies/downloads/ |
| MySQL | 8    | 8    | https://dev.mysql.com/downloads/mysql/                       |
| Redis | 7    | 7    | https://redis.io/download                                    |
| Nacos | -    | 2.x  | https://nacos.io/                                            |
| Node  | -    | 18+  | https://nodejs.org/                                          |

#### 软件架构图

![Plus部署架构图](http://bucket.alan.org.cn/blog/2026/10/02/16-37-21-1a99c97a84b9df30782568688df1d715-3c95d5.png "Plus部署架构图.png")

#### 版本说明 / 项目升级情况

本项目经历了一次架构升级，目前仓库中同时保留了新旧两个版本：

| 版本     | 目录                         | 状态             | 架构                       | 技术栈                                                       |
| :------- | :--------------------------- | :--------------- | :------------------------- | :----------------------------------------------------------- |
| **旧版** | `old-exam/`                  | 已上线，功能完整 | 单体应用（Spring Boot）    | Spring Boot 2.x + MyBatis-Plus + Druid + Fastjson + EasyExcel |
| **新版** | `exam-back/` + `exam-front/` | 🚧 开发中         | 微服务（RuoYi-Cloud-Plus） | 后端：Spring Boot 3.x + Spring Cloud + Nacos + Dubbo + Sa-Token + MyBatis-Plus<br/>前端：Vue3 + TypeScript + Element Plus + Vite + Pinia |

> **升级背景**：旧版基于 Spring Boot 单体架构开发，随着业务增长，在扩展性、可维护性方面遇到瓶颈。新版基于开源框架 [RuoYi-Cloud-Plus](https://gitee.com/dromara/RuoYi-Cloud-Plus) 进行重构，采用微服务架构，将考试、试卷、题库、阅卷、统计等业务拆分为独立服务，便于独立部署与水平扩展。

##### 旧版（old-exam）

- 路径：`old-exam/`
- 单体 Spring Boot 应用，功能完整，已上线运行
- 包含：用户/班级/试卷/题库/考试/阅卷/证书/统计/公告/讨论/AI 阅卷等模块

##### 新版（exam-back + exam-front）

- 后端：`exam-back/` —— 基于 [RuoYi-Cloud-Plus](https://gitee.com/dromara/RuoYi-Cloud-Plus) `2.6.2` 的微服务后端
- 前端：`exam-front/` —— 基于 Vue3 + TypeScript + Element Plus + Vite 的前端
- 微服务模块划分：考试管理（exam-manage）、试卷服务（exam-paper）、答题服务（exam-answer）、阅卷服务（exam-mark）、统计服务（exam-stat）、证书服务（exam-cert）、AI 服务（exam-ai）等
- 基础设施：Nacos（注册/配置中心）、Spring Cloud Gateway（网关）、Dubbo（RPC）、Sentinel（限流熔断）、Seata（分布式事务）、Redis、MySQL
- ⚠️ **注意**：新版仍在开发中，部分功能尚未完善

#### 联系方式

QQ群：群1:1034380536、群2:1098802068

微信公众号：程序员阿祥

邮箱：fignet@163.com

#### 最后

本项目还在开发当中，存在着 bug 还请谅解，也希望你加入到我们一起开发该项目

新版正在基于 RuoYi-Cloud-Plus 微服务框架进行重构升级

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

## 许可证

[Apache License 2.0](https://github.com/macrozheng/mall/blob/master/LICENSE)

Copyright (c) 2018-2024 macrozheng
