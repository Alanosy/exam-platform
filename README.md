# online-exam-system-backend

#### 友情提示

> 1. **接口文档**：[在线接口文档](https://apifox.com/apidoc/shared-a61e857a-187a-4b5f-972e-f0f0f6b89a7f) 。
> 2. **配套前端项目**：[前端项目地址](https://github.com/Alanosy/online-exam-system-frontend)

#### 介绍

本项目致力于打造一款通用的校园在线考试系统，此项目为后端项目，还有配套前端，在上面有可以点击跳转过去

由大学生开发，本项目还在初期开发中，欢迎小伙伴一起开发一起进步,具体可以参考下方参与贡献，有问题的话可以在issue中提问，会仔细回答各位

**如果各位喜欢，麻烦各位大佬点点Star**

#### 功能介绍

* 用户管理
* 班级管理
* 试卷中心
* 刷题中心
* 考试记录
* 错题本
* 考试管理
* 题库管理
* 试题管理
* 证书管理
* 我的证书
* 成绩分析
* 阅卷管理
* 公告管理
* 切屏检测
* 证书生成

### 技术选型

#### 后端技术

| 技术             | 说明             | 官网                                           |
| ---------------- | ---------------- | ---------------------------------------------- |
| SpringBoot       | Web应用开发框架  | https://spring.io/projects/spring-boot         |
| SpringSecurity   | 认证和授权框架   | https://spring.io/projects/spring-security     |
| MyBatisPlus      | ORM框架          | http://www.mybatis.org/mybatis-3/zh/index.html |
| MyBatisGenerator | 数据层代码生成器 | http://www.mybatis.org/generator/index.html    |
| Redis            | 内存数据存储     | https://redis.io/                              |
| Druid            | 数据库连接池     | https://github.com/alibaba/druid               |
| OSS              | 对象存储         | https://github.com/aliyun/aliyun-oss-java-sdk  |
| MinIO            | 对象存储         | https://github.com/minio/minio                 |
| JWT              | JWT登录支持      | https://github.com/jwtk/jjwt                   |
| Lombok           | Java语言增强库   | https://github.com/rzwitserloot/lombok         |
| Hutool           | Java工具类库     | https://github.com/looly/hutool                |

### 开发工具

| 工具         | 说明                | 官网                                                  |
| ------------ | ------------------- | ----------------------------------------------------- |
| IDEA         | 开发IDE             | https://www.jetbrains.com/idea/download               |
| RedisDesktop | redis客户端连接工具 | https://github.com/qishibo/AnotherRedisDesktopManager |
| SwitchHosts  | 本地host管理        | https://oldj.github.io/SwitchHosts/                   |
| Navicat      | 数据库连接工具      | http://www.formysql.com/xiazai.html                   |
| Xmind        | 思维导图设计工具    | http://www.edrawsoft.cn/mindmaster                    |
| Draw         | 流程图绘制工具      | https://draw.io                                       |
| Apifox       | API接口调试工具     | http://apifox.com                                     |
| Typora       | Markdown编辑器      | https://typora.io/                                    |

### 开发环境

| 工具  | 版本号 | 下载                                                         |
| ----- | ------ | ------------------------------------------------------------ |
| JDK   | 17     | https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html |
| MySQL | 5.7    | https://www.mysql.com/                                       |
| Redis | 7.0    | https://redis.io/download                                    |
| Nginx | 1.22   | http://nginx.org/en/download.html                            |



#### 系统架构

##### 功能结构图

![image-20240722113039659](https://alantypora.oss-cn-chengdu.aliyuncs.com/image-20240722113039659.png)

##### ER图

![ER图](https://alantypora.oss-cn-chengdu.aliyuncs.com/ER%E5%9B%BE.png)

#### 架构图

![架构图](https://alantypora.oss-cn-chengdu.aliyuncs.com/%E6%9E%B6%E6%9E%84%E5%9B%BE.png)

#### 缓存

![缓存作用模型.drawio](https://alantypora.oss-cn-chengdu.aliyuncs.com/%E7%BC%93%E5%AD%98%E4%BD%9C%E7%94%A8%E6%A8%A1%E5%9E%8B.drawio.png)



![分页缓存.drawio](https://alantypora.oss-cn-chengdu.aliyuncs.com/%E5%8D%95%E6%9D%A1%E8%AE%B0%E5%BD%95%E7%BC%93%E5%AD%98.drawio.png)





![redis分片集群.drawio](https://alantypora.oss-cn-chengdu.aliyuncs.com/redis%E5%88%86%E7%89%87%E9%9B%86%E7%BE%A4.drawio.png)

#### 主从分离

![主从分离.drawio](https://alantypora.oss-cn-chengdu.aliyuncs.com/%E4%B8%BB%E4%BB%8E%E5%88%86%E7%A6%BB.drawio.png)



#### 连接池

![连接池](https://alantypora.oss-cn-chengdu.aliyuncs.com/%E8%BF%9E%E6%8E%A5%E6%B1%A0.png)

#### 技术栈

![技术栈](https://alantypora.oss-cn-chengdu.aliyuncs.com/%E6%8A%80%E6%9C%AF%E6%A0%88.png)



#### 项目展示![截屏2024-06-01 下午7.59.40](https://alantypora.oss-cn-chengdu.aliyuncs.com/%E6%88%AA%E5%B1%8F2024-06-01%20%E4%B8%8B%E5%8D%887.59.40.png)

![截屏2024-06-01 下午7.59.28](https://alantypora.oss-cn-chengdu.aliyuncs.com/%E6%88%AA%E5%B1%8F2024-06-01%20%E4%B8%8B%E5%8D%887.59.28.png)

![截屏2024-06-01 下午7.57.15](https://alantypora.oss-cn-chengdu.aliyuncs.com/%E6%88%AA%E5%B1%8F2024-06-01%20%E4%B8%8B%E5%8D%887.59.14.png)



#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

## 许可证

[Apache License 2.0](https://github.com/macrozheng/mall/blob/master/LICENSE)

Copyright (c) 2018-2024 macrozheng



<picture>
  <source
    media="(prefers-color-scheme: dark)"
    srcset="
      https://api.star-history.com/svg?repos=Alanosy/online-exam-system-backend&type=Date&theme=dark
    "
  />
  <source
    media="(prefers-color-scheme: light)"
    srcset="
      https://api.star-history.com/svg?repos=Alanosy/online-exam-system-backend&type=Date
    "
  />
  <img
    alt="Star History Chart"
    src="https://api.star-history.com/svg?repos=Alanosy/online-exam-system-backend&type=Date"
  />
</picture>
