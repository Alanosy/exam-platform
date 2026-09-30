# exam-platform ｜ 在线考试系统

#### 友情提示

> 1. **项目体验地址**：[项目体验地址](http://exam.alan.org.cn)
>    1. 管理员账号:admin 密码:123456
>    2. 教师账号:teacher 密码:123456
>    3. 学生账号:student 密码:123456

#### 介绍

本项目致力于打造一款通用的在线考试系统，涵盖**考试管理、题库管理、试卷中心、刷题练习、成绩分析、AI 阅卷、证书管理**等核心功能，支持学生、教师、管理员三种角色。

当前仓库已升级为**单仓多模块（monorepo）**结构，同时包含旧版单体应用与新版微服务应用两套代码，便于历史追溯与并行开发。

**=>如果各位喜欢，麻烦各位大佬点点Star<=**

****

#### 版本说明 / 项目升级情况

本项目经历了一次架构升级，目前仓库中同时保留了新旧两个版本：

| 版本 | 目录 | 状态 | 架构 | 技术栈 |
| :--- | :--- | :--- | :--- | :--- |
| **旧版** | `old-exam/` | 已上线，功能完整 | 单体应用（Spring Boot） | Spring Boot 2.x + MyBatis-Plus + Druid + Fastjson + EasyExcel |
| **新版** | `exam-back/` + `exam-front/` | 🚧 开发中 | 微服务（RuoYi-Cloud-Plus） | 后端：Spring Boot 3.x + Spring Cloud + Nacos + Dubbo + Sa-Token + MyBatis-Plus<br/>前端：Vue3 + TypeScript + Element Plus + Vite + Pinia |

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

****

#### 功能介绍

旧版已实现以下功能：

用户管理、班级管理、试卷中心、刷题中心、考试记录、错题本、考试管理、题库管理、试题管理、证书管理、我的证书、成绩分析、阅卷管理、公告管理、切屏检测、证书生成

最近新功能：

讨论功能、AI 阅卷、题库分类管理、成绩分析查看用户试卷、考试选题与抽题

#### 项目展示

<table>
    <tr>
        <td><img src="http://bucket.alan.org.cn/blog/2026/04/10/01-04-41-80771fc50a6fa5472b1ce441721b9b33-dacd41.png"/></td>
        <td><img src="http://bucket.alan.org.cn/blog/2026/04/10/01-07-38-0aaab4c596d622c45935de321f16abc6-0d82a8.png"/></td>
      	<td><img src="http://bucket.alan.org.cn/blog/2026/04/10/01-04-54-8df2f72d9681a6279de6527250d62751-c22b74.png"/></td>
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

#### 相关文档

<table>
    <tr>
        <td>功能结构图</td>
        <td>技术栈</td>
    </tr>
  <tr>
        <td><img src="http://bucket.alan.org.cn/blog/2026/04/10/00-58-06-0307a664f6cd8a3bd076a07c0a7b9193-6e0051.png"/></td>
        <td><img src="http://bucket.alan.org.cn/blog/2026/04/10/00-59-57-7a167ec462964f16eb815f17041568f4-8855a8.png"/></td>
    </tr>
  <tr>
        <td>ER图</td>
        <td>连接池</td>
    </tr>
  <tr>
        <td><img src="http://bucket.alan.org.cn/blog/2026/04/10/01-00-11-fb042558424b8161d92a43fce7a4c672-a4da5a.png"/></td>
        <td><img src="http://bucket.alan.org.cn/blog/2026/04/10/01-03-28-5068cf8ac437f500bf7da410975774d1-c2c277.png"/></td>
    </tr>
</table>

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

#### 联系方式

QQ群：群1:1034380536 （已满）、群2:1098802068

微信：fignet

微信群：请从公众号获得

邮箱：fignet@163.com

#### 最新联系方式

微信公众号关注「程序员阿祥」，回复「考试系统交流群」获取最新在线交流群

#### 其他服务（有偿）

博主提供代部署服务(50rmb)

提供讲解服务等等

#### 最后

本项目还在开发当中，存在着 bug 还请谅解，也希望你加入到我们一起开发该项目

新版正在基于 RuoYi-Cloud-Plus 微服务框架进行重构升级

## 许可证

[Apache License 2.0](https://github.com/macrozheng/mall/blob/master/LICENSE)

Copyright (c) 2018-2024 macrozheng
