## 平台简介

## 平台简介

- 本仓库为前端技术栈 [Vue3](https://v3.cn.vuejs.org) + [Element Plus](https://element-plus.org/zh-CN) + [Vite](https://cn.vitejs.dev) 版本。
- 配套后端代码仓库地址
- [RuoYi-Vue-Plus 5.X(注意版本号)](https://gitee.com/dromara/RuoYi-Vue-Plus)
- [RuoYi-Cloud-Plus 2.X(注意版本号)](https://gitee.com/dromara/RuoYi-Cloud-Plus)

## 前端运行

```bash
# 克隆项目
git clone https://gitee.com/JavaLionLi/plus-ui.git

# 安装依赖
npm install --registry=https://registry.npmmirror.com

# 启动服务
npm run dev

# 构建测试环境 yarn build:stage
# 构建生产环境 yarn build:prod
# 前端访问地址 http://localhost:80
```

## 本框架与 RuoYi 的业务差异

| 业务         | 功能说明                                                        | 本框架 | RuoYi                          |
| ------------ | --------------------------------------------------------------- | ------ | ------------------------------ |
| 租户管理     | 系统内租户的管理 如:租户套餐、过期时间、用户数量、企业信息等    | 支持   | 无                             |
| 租户套餐管理 | 系统内租户所能使用的套餐管理 如:套餐内所包含的菜单等            | 支持   | 无                             |
| 用户管理     | 用户的管理配置 如:新增用户、分配用户所属部门、角色、岗位等      | 支持   | 支持                           |
| 部门管理     | 配置系统组织机构（公司、部门、小组） 树结构展现支持数据权限     | 支持   | 支持                           |
| 岗位管理     | 配置系统用户所属担任职务                                        | 支持   | 支持                           |
| 菜单管理     | 配置系统菜单、操作权限、按钮权限标识等                          | 支持   | 支持                           |
| 角色管理     | 角色菜单权限分配、设置角色按机构进行数据范围权限划分            | 支持   | 支持                           |
| 字典管理     | 对系统中经常使用的一些较为固定的数据进行维护                    | 支持   | 支持                           |
| 参数管理     | 对系统动态配置常用参数                                          | 支持   | 支持                           |
| 通知公告     | 系统通知公告信息发布维护                                        | 支持   | 支持                           |
| 操作日志     | 系统正常操作日志记录和查询 系统异常信息日志记录和查询           | 支持   | 支持                           |
| 登录日志     | 系统登录日志记录查询包含登录异常                                | 支持   | 支持                           |
| 文件管理     | 系统文件展示、上传、下载、删除等管理                            | 支持   | 无                             |
| 文件配置管理 | 系统文件上传、下载所需要的配置信息动态添加、修改、删除等管理    | 支持   | 无                             |
| 在线用户管理 | 已登录系统的在线用户信息监控与强制踢出操作                      | 支持   | 支持                           |
| 定时任务     | 运行报表、任务管理(添加、修改、删除)、日志管理、执行器管理等    | 支持   | 仅支持任务与日志管理           |
| 代码生成     | 多数据源前后端代码的生成（java、html、xml、sql）支持 CRUD 下载  | 支持   | 仅支持单数据源                 |
| 系统接口     | 根据业务代码自动生成相关的 api 接口文档                         | 支持   | 支持                           |
| 服务监控     | 监视集群系统 CPU、内存、磁盘、堆栈、在线日志、Spring 相关配置等 | 支持   | 仅支持单机 CPU、内存、磁盘监控 |
| 缓存监控     | 对系统的缓存信息查询，命令统计等。                              | 支持   | 支持                           |
| 在线构建器   | 拖动表单元素生成相应的 HTML 代码。                              | 支持   | 支持                           |
| 使用案例     | 系统的一些功能案例                                              | 支持   | 不支持                         |

## 演示图

- 本仓库为前端技术栈 [Vue3](https://v3.cn.vuejs.org) + [Element Plus](https://element-plus.org/zh-CN) + [Vite](https://cn.vitejs.dev) 版本。
- 配套后端代码仓库地址
- [RuoYi-Vue-Plus 5.X(注意版本号)](https://gitee.com/dromara/RuoYi-Vue-Plus)
- [RuoYi-Cloud-Plus 2.X(注意版本号)](https://gitee.com/dromara/RuoYi-Cloud-Plus)

## 前端运行

```bash
# 克隆项目
git clone https://gitee.com/JavaLionLi/plus-ui.git

# 安装依赖
npm install --registry=https://registry.npmmirror.com

# 启动服务
npm run dev

# 构建测试环境 yarn build:stage
# 构建生产环境 yarn build:prod
# 前端访问地址 http://localhost:80
```

## 后端改造

参考后端代码内 `ruoyi-gen/resources/vm/vue/v3/readme.txt` 说明

## 内置功能

1. 租户管理：配置系统租户，支持 SaaS 场景下的多租户功能。
2. 用户管理：用户是系统操作者，该功能主要完成系统用户配置。
3. 部门管理：配置系统组织机构（公司、部门、小组），树结构展现支持数据权限。
4. 岗位管理：配置系统用户所属担任职务。
5. 菜单管理：配置系统菜单，操作权限，按钮权限标识等。
6. 角色管理：角色菜单权限分配、设置角色按机构进行数据范围权限划分。
7. 字典管理：对系统中经常使用的一些较为固定的数据进行维护。
8. 参数管理：对系统动态配置常用参数。
9. 通知公告：系统通知公告信息发布维护。
10. 操作日志：系统正常操作日志记录和查询；系统异常信息日志记录和查询。
11. 登录日志：系统登录日志记录查询包含登录异常。
12. 在线用户：当前系统中活跃用户状态监控。
13. 定时任务：在线（添加、修改、删除)任务调度包含执行结果日志。
14. 代码生成：前后端代码的生成（java、html、xml、sql）支持 CRUD 下载 。
15. 系统接口：根据业务代码自动生成相关的 api 接口文档。
16. 服务监控：监视当前系统 CPU、内存、磁盘、堆栈等相关信息。
17. 缓存监控：对系统的缓存信息查询，命令统计等。
18. 在线构建器：拖动表单元素生成相应的 HTML 代码。

## 演示图

<table>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/cd1f90be5f2684f4560c9519c0f2a232ee8.jpg"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/1cbcf0e6f257c7d3a063c0e3f2ff989e4b3.jpg"/></td>
    </tr>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-8074972883b5ba0622e13246738ebba237a.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-9f88719cdfca9af2e58b352a20e23d43b12.png"/></td>
    </tr>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-39bf2584ec3a529b0d5a3b70d15c9b37646.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-936ec82d1f4872e1bc980927654b6007307.png"/></td>
    </tr>
	<tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-b2d62ceb95d2dd9b3fbe157bb70d26001e9.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-d67451d308b7a79ad6819723396f7c3d77a.png"/></td>
    </tr>	 
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/5e8c387724954459291aafd5eb52b456f53.jpg"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/644e78da53c2e92a95dfda4f76e6d117c4b.jpg"/></td>
    </tr>
	<tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-8370a0d02977eebf6dbf854c8450293c937.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-49003ed83f60f633e7153609a53a2b644f7.png"/></td>
    </tr>
	<tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-d4fe726319ece268d4746602c39cffc0621.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-c195234bbcd30be6927f037a6755e6ab69c.png"/></td>
    </tr>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/b6115bc8c31de52951982e509930b20684a.jpg"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-5e4daac0bb59612c5038448acbcef235e3a.png"/></td>
    </tr>
</table>
