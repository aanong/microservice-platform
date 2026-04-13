# 微服务演示项目 (Microservice Platform)

这是一个基于 Spring Cloud Alibaba 体系构建的微服务电商演示项目，包含了用户服务、商品库存服务、订单服务以及 API 网关。

## 模块结构

- **`gateway-service`**: API 网关，负责请求路由和统一鉴权等功能。
- **`user-service`**: 用户服务，包括用户注册、登录、Token 验证和用户详情查询。
- **`stock-service`**: 商品库存服务，负责商品和分类的管理。
- **`order-service`**: 订单服务，涵盖购物车和订单的生命周期管理。
- **`common`**: 公共基础模块，提供所有业务服务共享的工具类、通用配置、缓存与消息中间件的支持。

## 技术栈

- Spring Boot 2.7.x
- Spring Cloud 2021.x / Spring Cloud Alibaba 2021.0.5.0
- MyBatis-Plus 3.5.5
- Redis (Spring Data Redis)
- Apache RocketMQ (用于事件发布和缓存同步)
- Seata 1.7.1 (分布式事务)
- MySQL
- Nacos (注册中心与配置中心)

## 核心架构设计

### 缓存与缓存同步机制

本项目采用公共模块统一定义了 Redis JSON 缓存工具类和缓存的 Canal (CanalFlatMessage 等) MQ 同步机制：

- `RedisJsonCacheHelper`: 提供统一样板以写入和读取 JSON 对象、JSON 列表。
- `AbstractCacheSyncConsumer`: 统一的 RocketMQ 缓存同步监听基类，处理 Canal 的 `UPDATE/DELETE/INSERT` 消息解析、幂等去重和延迟检测。各个服务只需继承该类并进行极少量的配置即可实现数据库变更同步到 Redis。
- `AbstractEventPublisher`: 抽象事件发布者。

## 组建本地环境

依赖的中间件主要包括 MySQL, Redis, RocketMQ, Nacos, Seata。可以使用项目 `docker` 目录下的 `docker-compose.yml` 快速启动相关基础支撑设施。

```bash
docker-compose up -d
```

## Observability

The project now includes a unified logging and trace baseline:

- `common` auto-configures JSON logs to `stdout`
- every HTTP request gets an `X-Trace-Id`
- gateway WebClient calls propagate the same trace id downstream
- RocketMQ publisher and consumer logs carry the same trace id when possible
- ELK stores structured logs and SkyWalking provides topology and trace views

Infra references:

- observability stack compose: [docker/infra/docker-compose.yml](docker/infra/docker-compose.yml)
- logstash pipeline: [docker/infra/logstash/pipeline/logstash.conf](docker/infra/logstash/pipeline/logstash.conf)
- setup notes: [docker/infra/observability/README.md](docker/infra/observability/README.md)

启动数据库服务并初始化后，按顺序启动Nacos，各业务微服务，最后启动 Gateway 即可访问。
