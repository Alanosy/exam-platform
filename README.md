# online-exam-system-backend ｜ 通用在线考试系统

#### 友情提示

> 1. **接口文档**：[在线接口文档](https://apifox.com/apidoc/shared-a61e857a-187a-4b5f-972e-f0f0f6b89a7f) 。
> 2. **体验地址**：[点击此处跳转体验网站](http://47.109.94.143/#/login) 。 账号:admin 密码:123456
> 3. **配套前端项目**：[前端项目地址](https://github.com/Alanosy/online-exam-system-frontend)

#### 介绍

本项目致力于打造一款通用的在线考试系统，此项目为**后端项目**，**还有配套前端**，在上面有可以点击跳转过去。本项目主要优点是开发、部署简单快捷、界面设计友好、代码结构清晰。

本项目还在初期开发中，欢迎小伙伴一起开发一起进步,具体可以参考下方参与贡献，有问题的话可以在issue中提问，会仔细回答各位

**如果各位喜欢，麻烦各位大佬点点Star**

##### 温馨提示

1. 登录页面可以登录三个角色，分别为学生、教师和管理员,不同账号显示不用用户界面，具体账号登录管理员查看用户管理
2. **加入班级请点击头像打开个人信息中加入**，班级口令请看教师或管理端班级口令查找

****

#### 功能介绍

本项目包含以下功能

用户管理、班级管理、试卷中心、刷题中心、考试记录、错题本、考试管理、题库管理、试题管理、证书管理、我的证书、成绩分析、阅卷管理、公告管理、切屏检测、证书生成

#### 项目展示

<table>
    <tr>
        <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/管理端.png"/></td>
        <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/教师端.png"/></td>
      	<td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/学生端.png"/></td>
    </tr>
</table>

#### 安装

1. 拉取项目

   ``` bash
   git clone https://github.com/Alanosy/online-exam-system-backend.git
   ```

2. 导入IDEA，配置好maven、java版本（需要java17版本）

3. 配置application-dev.yml，配置好mysql和redis。（注意：mysql需要5.7版本，8.0有only_full_group_by 模式有些地方会报异常）

4. 直接点运行就可以使用了

#### 开发环境

| 工具  | 版本号 | 下载                                                       |
| ----- | ------ | ---------------------------------------------------------- |
| JDK   | 17     | https://www.oracle.com/java/technologies/downloads/#java17 |
| MySQL | 5.7    | https://dev.mysql.com/downloads/mysql/5.7.html             |
| Redis | 7.0    | https://redis.io/download                                  |
| Nginx | 1.22   | http://nginx.org/en/download.html                          |

#### 相关文档

<table>
    <tr>
        <td>功能结构图</td>
        <td>架构图</td>
    </tr>
  <tr>
        <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/%E5%8A%9F%E8%83%BD%E7%BB%93%E6%9E%84%E5%9B%BE.drawio.png"/></td>
        <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/架构图.png"/></td>
    </tr>
  <tr>
        <td>ER图</td>
        <td>技术栈</td>
    </tr>
  <tr>
        <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/ER图.png"/></td>
        <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/技术栈.png"/></td>
    </tr>
    <tr>
        <td>主从分离</td>
        <td>Redis集群</td>
    </tr>
  <tr>
        <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/主从分离.drawio.png"/></td>
        <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/redis分片集群.drawio.png"/></td>
    </tr>
  <tr>
        <td>缓存模型</td>
        <td>单条记录缓存</td>
    </tr>
  <tr>
        <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/缓存作用模型.drawio.png"/></td>
        <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/单条记录缓存.drawio.png"/></td>
    </tr>
   <tr>
        <td>连接池</td>
        <td></td>
    </tr>
  <tr>
    <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/连接池.png"/></td>
     <td></td>
  </tr>
</table>

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

#### 联系方式

微信：fignet

QQ群：1034380536

邮箱：fignet@163.com

## 许可证

[Apache License 2.0](https://github.com/macrozheng/mall/blob/master/LICENSE)

Copyright (c) 2018-2024 macrozheng
