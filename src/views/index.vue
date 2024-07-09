<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :sm="24" :lg="12" style="padding-left: 20px">
        <h2>RuoYi-Vue-Plus多租户管理系统</h2>
        <p>
          RuoYi-Vue-Plus 是基于 RuoYi-Vue 针对 分布式集群 场景升级(不兼容原框架)
          <br />
          * 前端开发框架 Vue3、TS、Element Plus<br />
          * 后端开发框架 Spring Boot<br />
          * 容器框架 Undertow 基于 Netty 的高性能容器<br />
          * 权限认证框架 Sa-Token 支持多终端认证系统<br />
          * 关系数据库 MySQL 适配 8.X 最低 5.7<br />
          * 缓存数据库 Redis 适配 6.X 最低 4.X<br />
          * 数据库框架 Mybatis-Plus 快速 CRUD 增加开发效率<br />
          * 数据库框架 p6spy 更强劲的 SQL 分析<br />
          * 多数据源框架 dynamic-datasource 支持主从与多种类数据库异构<br />
          * 序列化框架 Jackson 统一使用 jackson 高效可靠<br />
          * Redis客户端 Redisson 性能强劲、API丰富<br />
          * 分布式限流 Redisson 全局、请求IP、集群ID 多种限流<br />
          * 分布式锁 Lock4j 注解锁、工具锁 多种多样<br />
          * 分布式幂等 Lock4j 基于分布式锁实现<br />
          * 分布式链路追踪 SkyWalking 支持链路追踪、网格分析、度量聚合、可视化<br />
          * 分布式任务调度 SnailJob 高性能 高可靠 易扩展<br />
          * 文件存储 Minio 本地存储<br />
          * 文件存储 七牛、阿里、腾讯 云存储<br />
          * 监控框架 SpringBoot-Admin 全方位服务监控<br />
          * 校验框架 Validation 增强接口安全性 严谨性<br />
          * Excel框架 Alibaba EasyExcel 性能优异 扩展性强<br />
          * 文档框架 SpringDoc、javadoc 无注解零入侵基于java注释<br />
          * 工具类框架 Hutool、Lombok 减少代码冗余 增加安全性<br />
          * 代码生成器 适配MP、SpringDoc规范化代码 一键生成前后端代码<br />
          * 部署方式 Docker 容器编排 一键部署业务集群<br />
          * 国际化 SpringMessage Spring标准国际化方案<br />
        </p>
        <p><b>当前版本:</b> <span>v5.2.1</span></p>
        <p>
          <el-tag type="danger">&yen;免费开源</el-tag>
        </p>
        <p>
          <el-button type="primary" icon="Cloudy" plain @click="goTarget('https://gitee.com/dromara/RuoYi-Vue-Plus')">访问码云</el-button>
          <el-button type="primary" icon="Cloudy" plain @click="goTarget('https://github.com/dromara/RuoYi-Vue-Plus')">访问GitHub</el-button>
          <el-button type="primary" icon="Cloudy" plain @click="goTarget('https://plus-doc.dromara.org/#/ruoyi-vue-plus/changlog')"
            >更新日志</el-button
          >
        </p>
      </el-col>

      <el-col :sm="24" :lg="12" style="padding-left: 20px">
        <h2>RuoYi-Cloud-Plus多租户微服务管理系统</h2>
        <p>
          RuoYi-Cloud-Plus 微服务通用权限管理系统 重写 RuoYi-Cloud 全方位升级(不兼容原框架)
          <br />
          * 前端开发框架 Vue3、TS、Element UI<br />
          * 后端开发框架 Spring Boot<br />
          * 微服务开发框架 Spring Cloud、Spring Cloud Alibaba<br />
          * 容器框架 Undertow 基于 XNIO 的高性能容器<br />
          * 权限认证框架 Sa-Token、Jwt 支持多终端认证系统<br />
          * 关系数据库 MySQL 适配 8.X 最低 5.7<br />
          * 关系数据库 Oracle 适配 11g 12c<br />
          * 关系数据库 PostgreSQL 适配 13 14<br />
          * 关系数据库 SQLServer 适配 2017 2019<br />
          * 缓存数据库 Redis 适配 6.X 最低 5.X<br />
          * 分布式注册中心 Alibaba Nacos 采用2.X 基于GRPC通信高性能<br />
          * 分布式配置中心 Alibaba Nacos 采用2.X 基于GRPC通信高性能<br />
          * 服务网关 Spring Cloud Gateway 响应式高性能网关<br />
          * 负载均衡 Spring Cloud Loadbalancer 负载均衡处理<br />
          * RPC远程调用 Apache Dubbo 原生态使用体验、高性能<br />
          * 分布式限流熔断 Alibaba Sentinel 无侵入、高扩展<br />
          * 分布式事务 Alibaba Seata 无侵入、高扩展 支持 四种模式<br />
          * 分布式消息队列 Apache Kafka 高性能高速度<br />
          * 分布式消息队列 Apache RocketMQ 高可用功能多样<br />
          * 分布式消息队列 RabbitMQ 支持各种扩展插件功能多样性<br />
          * 分布式搜索引擎 ElasticSearch 业界知名<br />
          * 分布式链路追踪 Apache SkyWalking 链路追踪、网格分析、度量聚合、可视化<br />
          * 分布式日志中心 ELK 业界成熟解决方案<br />
          * 分布式监控 Prometheus、Grafana 全方位性能监控<br />
          * 其余与 Vue 版本一致<br />
        </p>
        <p><b>当前版本:</b> <span>v2.2.0</span></p>
        <p>
          <el-tag type="danger">&yen;免费开源</el-tag>
        </p>
        <p>
          <el-button type="primary" icon="Cloudy" plain @click="goTarget('https://gitee.com/dromara/RuoYi-Cloud-Plus')">访问码云</el-button>
          <el-button type="primary" icon="Cloudy" plain @click="goTarget('https://github.com/dromara/RuoYi-Cloud-Plus')">访问GitHub</el-button>
          <el-button type="primary" icon="Cloudy" plain @click="goTarget('https://plus-doc.dromara.org/#/ruoyi-cloud-plus/changlog')"
            >更新日志</el-button
          >
        </p>
      </el-col>
    </el-row>
    <el-divider />
  </div>
</template>

<script setup name="Index" lang="ts">
const goTarget = (url: string) => {
  window.open(url, '__blank');
};
</script>

<style scoped lang="scss">
.home {
  blockquote {
    padding: 10px 20px;
    margin: 0 0 20px;
    font-size: 17.5px;
    border-left: 5px solid #eee;
  }
  hr {
    margin-top: 20px;
    margin-bottom: 20px;
    border: 0;
    border-top: 1px solid #eee;
  }
  .col-item {
    margin-bottom: 20px;
  }

  ul {
    padding: 0;
    margin: 0;
  }

  font-family: 'open sans', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  font-size: 13px;
  color: #676a6c;
  overflow-x: hidden;

  ul {
    list-style-type: none;
  }

  h4 {
    margin-top: 0px;
  }

  h2 {
    margin-top: 10px;
    font-size: 26px;
    font-weight: 100;
  }

  p {
    margin-top: 10px;

    b {
      font-weight: 700;
    }
  }

  .update-log {
    ol {
      display: block;
      list-style-type: decimal;
      margin-block-start: 1em;
      margin-block-end: 1em;
      margin-inline-start: 0;
      margin-inline-end: 0;
      padding-inline-start: 40px;
    }
  }
}
</style>
