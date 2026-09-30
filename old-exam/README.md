# online-exam-system-backend ｜ 通用在线考试系统

#### 友情提示

> 1. **项目体验地址**：[项目体验地址](http://exam.alan.org.cn)  
>    1. 管理员账号:admin 密码:123456
>    2. 教师账号:teacher 密码:123456
>    3. 教师账号:student 密码:123456
> 2. **配套前端项目**：[前端项目地址](https://github.com/Alanosy/online-exam-system-frontend)

#### 介绍

本项目致力于打造一款通用的在线考试系统，此项目为**后端项目，还有配套前端**，在上面有可以点击跳转过去。本项目主要优点是开发、部署简单快捷、界面设计友好、代码结构清晰，功能完整。

本项目还在更新中，欢迎小伙伴一起开发一起进步,具体可以参考下方参与贡献

如遇到Bug可以在issue中提出，会仔细回答各位

如果遇到部署问题，也可以在最下面找到联系方式，会给大家解答，也有项目的配套资料可以免费领取，包含ER图、设计资料、nginx反向代理配置文件、演讲ppt等全套资料

**=>如果各位喜欢，麻烦各位大佬点点Star<=**

##### 温馨提示

1. **=>登录页面可以登录三个角色<=**，分别为学生、教师和管理员,不同账号显示不用用户界面，具体账号登录管理员查看用户管理，默认密码都是123456
2. **=>加入班级请点击头像打开个人信息中加入<=**，班级口令请看教师或管理端班级口令查找
3. 数据库Sql文件在lib文件夹中

****

#### 功能介绍

本项目包含以下功能

用户管理、班级管理、试卷中心、刷题中心、考试记录、错题本、考试管理、题库管理、试题管理、证书管理、我的证书、成绩分析、阅卷管理、公告管理、切屏检测、证书生成

最近新功能：

讨论功能，Ai阅卷、题库添加分类管理、成绩分析添加查看用户试卷、添加考试可以直接选题和抽题

#### 项目展示

<table>
    <tr>
        <td><img src="http://bucket.alan.org.cn/blog/2026/04/10/01-04-41-80771fc50a6fa5472b1ce441721b9b33-dacd41.png"/></td>
        <td><img src="http://bucket.alan.org.cn/blog/2026/04/10/01-07-38-0aaab4c596d622c45935de321f16abc6-0d82a8.png"/></td>
      	<td><img src="http://bucket.alan.org.cn/blog/2026/04/10/01-04-54-8df2f72d9681a6279de6527250d62751-c22b74.png"/></td>
    </tr>
</table>


#### 安装

1. 拉取项目

   ``` bash
   git clone https://github.com/Alanosy/online-exam-system-backend.git
   ```

2. 导入IDEA，配置好maven、java版本（需要java17版本）

3. 配置application-dev.yml，配置好mysql和redis。（注意：最好使用Mysql5.7版本）

4. 如果需要上传图片需要配置阿里云OSS或者本地自己部署Minio服务然后在配置文件中配置

5. 配置好后直接点运行就可以使用了，如果运行不起来，可以在下方👇找到联系方式提问

#### 详细的部署教程

详细部署请关注微信公众号：「程序员阿祥」，回复「考试系统部署教程」获取，包含环境的详细部署，下载地址+步骤截图等等，超详细

<img src="http://bucket.alan.org.cn/blog/2026/04/10/00-49-07-36ff4c5cbd94a3e12e16101f244b9d11-def072.png" alt="image-20260410003958744" style="zoom:50%;" />

#### 开发环境

| 工具  | 版本号   | 下载                                                         |
| ----- | -------- | ------------------------------------------------------------ |
| JDK   | **17**   | https://www.oracle.com/cn/java/technologies/downloads/#java8 |
| MySQL | **8**    | https://dev.mysql.com/downloads/mysql/5.7.html               |
| Redis | **7**    | https://redis.io/download                                    |
| Nginx | **1.22** | http://nginx.org/en/download.html                            |

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

本项目还在开发当中，存在着bug还请谅解，也希望你加入到我们一起开发该项目

该项目正在升级，重新架构并引入Agent

## 许可证

[Apache License 2.0](https://github.com/macrozheng/mall/blob/master/LICENSE)

Copyright (c) 2018-2024 macrozheng
