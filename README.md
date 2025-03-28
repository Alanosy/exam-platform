# online-exam-system-backend ｜ 通用在线考试系统

#### 友情提示

> 1. **配套前端项目**：[前端项目地址](https://github.com/Alanosy/online-exam-system-frontend)

#### 介绍

本项目致力于打造一款通用的在线考试系统，此项目为**后端项目，还有配套前端**，在上面有可以点击跳转过去。本项目主要优点是开发、部署简单快捷、界面设计友好、代码结构清晰，功能完整。

本项目还在更新中，欢迎小伙伴一起开发一起进步,具体可以参考下方参与贡献

如遇到Bug可以在issue中提出，会仔细回答各位

如果遇到部署问题，也可以加入下方的QQ群，会给大家解答，QQ群文件中放有该项目的配套资料

**=>如果各位喜欢，麻烦各位大佬点点Star<=**

##### 温馨提示

1. **=>登录页面可以登录三个角色<=**，分别为学生、教师和管理员,不同账号显示不用用户界面，具体账号登录管理员查看用户管理
2. **=>加入班级请点击头像打开个人信息中加入<=**，班级口令请看教师或管理端班级口令查找
3. 数据库Sql文件在lib文件夹中

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

2. 导入IDEA，配置好maven、java版本（需要java8版本）

3. 配置application-dev.yml，配置好mysql和redis。（注意：最好使用Mysql5.7版本）

4. 如果需要上传图片需要配置阿里云OSS或者本地自己部署Minio服务然后在配置文件中配置

5. 配置好后直接点运行就可以使用了，如果运行不起来，可以加入QQ群提问

#### 开发环境

| 工具  | 版本号   | 下载                                                         |
| ----- | -------- | ------------------------------------------------------------ |
| JDK   | **8**    | https://www.oracle.com/cn/java/technologies/downloads/#java8 |
| MySQL | **5.7**  | https://dev.mysql.com/downloads/mysql/5.7.html               |
| Redis | **5**    | https://redis.io/download                                    |
| Nginx | **1.22** | http://nginx.org/en/download.html                            |

#### 相关文档

<table>
    <tr>
        <td>功能结构图</td>
        <td>技术栈</td>
    </tr>
  <tr>
        <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/%E5%8A%9F%E8%83%BD%E7%BB%93%E6%9E%84%E5%9B%BE.drawio.png"/></td>
        <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/技术栈.png"/></td>
    </tr>
  <tr>
        <td>ER图</td>
        <td>连接池</td>
    </tr>
  <tr>
        <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/ER图.png"/></td>
        <td><img src="https://github.com/Alanosy/online-exam-system-backend/blob/master/img/连接池.png"/></td>
    </tr>
</table>


#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

#### 联系方式

QQ群：1034380536  <=可以先加这个

微信：fignet

邮箱：fignet@163.com

## 许可证

[Apache License 2.0](https://github.com/macrozheng/mall/blob/master/LICENSE)

Copyright (c) 2018-2024 macrozheng
