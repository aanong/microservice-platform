# 微服务演示项目 (Microservice Platform)

这是一个基于 **Spring Cloud Alibaba** 体系构建的微服务电商演示项目，涵盖用户认证、商品库存、订单履约、店铺管理等完整业务链路，并集成了分布式事务、缓存同步、消息事件发布及统一可观测性能力。

---

## 模块结构

| 模块 | 说明 |
|---|---|
| `gateway-service` | API 网关，负责请求路由、Token 校验与下游请求转发 |
| `user-service` | 用户服务，提供注册、登录、Token 验签接口 |
| `stock-service` | 商品库存服务，管理品牌、分类、SPU/SKU 及商品搜索 |
| `order-service` | 订单服务，覆盖购物车、订单、支付、退款、物流、优惠券、促销活动全流程 |
| `shop-service` | 店铺服务，提供用户侧店铺申请及商户侧店铺查询 |
| `common` | 公共基础模块，包含实体类、缓存工具、MQ 抽象、统一响应模型、全局异常处理 |
| `common-logging-spring-boot-starter` | 日志与链路追踪自动装配 Starter，所有业务服务引入后即可获得结构化日志和 TraceId 传播能力 |

---

## 技术栈

| 类别 | 组件 |
|---|---|
| 基础框架 | Spring Boot 2.7.x |
| 微服务 | Spring Cloud 2021.x / Spring Cloud Alibaba 2021.0.5.0 |
| 服务注册/配置 | Nacos |
| ORM | MyBatis-Plus 3.5.5 |
| 缓存 | Redis (Spring Data Redis) |
| 消息队列 | Apache RocketMQ |
| 分布式事务 | Seata 1.7.1 |
| 数据库 | MySQL |
| 可观测性 | ELK (Elasticsearch + Logstash + Kibana) + SkyWalking |

---

## 核心架构设计

### 1. 统一响应模型

所有接口的返回值统一封装在 `com.literature.common.core.model.ApiResponse<T>` 中。

```java
// 成功响应
ApiResponse.success(data);        // code = "0000"

// 错误响应
ApiResponse.error(code, message); // 使用 ErrorCode 常量
```

**内置错误码（`ErrorCode`）：**

| 常量 | 值 | 语义 |
|---|---|---|
| `SUCCESS` | `0000` | 成功 |
| `PARAM_ERROR` | `4001` | 参数错误 |
| `NOT_FOUND` | `4004` | 资源不存在 |
| `SYSTEM_ERROR` | `5000` | 系统内部错误 |

分页数据额外封装为 `PageResponse<T>` 后再嵌套进 `ApiResponse`。

---

### 2. 网关鉴权机制

`gateway-service` 内置 `AuthGlobalFilter`（Order = -100），对订单域路径（`/api/order/**`、`/api/cart/**`、`/api/orders/**`、`/api/payments/**`、`/api/coupons/**`、`/api/refunds/**`、`/api/logistics/**`）执行 Token 校验：

1. 提取请求头 `Authorization: Bearer <token>`。
2. 通过 WebClient 调用 `user-service` 内部接口 `/api/user/internal/validate` 验证 Token。
3. 校验通过后，将 `userId` 以 `X-User-Id` 请求头注入下游，实现用户身份传播。

---

### 3. 缓存与缓存同步机制

`common` 模块统一提供 Redis 缓存工具和基于 Canal + RocketMQ 的缓存同步机制：

- **`RedisJsonCacheHelper`**：统一封装 JSON 对象/列表的读写操作。
- **`AbstractCacheSyncConsumer`**：RocketMQ 缓存同步监听基类，处理 Canal `INSERT/UPDATE/DELETE` 消息的解析、幂等去重和延迟检测。各业务服务（`StockCacheSyncConsumer`、`OrderCacheSyncConsumer`、`UserCacheSyncConsumer`）只需继承并做少量配置即可完成数据库变更到 Redis 的自动同步。
- **`AbstractEventPublisher`**：抽象事件发布者，集成 `enabled` 开关与异常容错逻辑，业务侧通过继承实现各自的事件（如 `OrderEventPublisher#publishPaidEvent`、`ProductEventPublisher`）。

---

### 4. 可观测性（Observability）

`common-logging-spring-boot-starter` 为所有服务提供统一的日志与链路追踪基线，**引入依赖后零配置自动生效**：

| 能力 | 说明 |
|---|---|
| 结构化 JSON 日志 | 日志输出到 `stdout`，供 Logstash 采集 |
| HTTP 链路 TraceId | `ServletTraceLoggingFilter` 为每个请求生成/传播 `X-Trace-Id` |
| 响应头回写 | `X-Trace-Id` 随响应头返回，方便客户端问题定位 |
| WebClient 传播 | `TraceWebClientFilter` 将 TraceId 注入下游 WebClient 调用 |
| RocketMQ 传播 | 生产者/消费者日志携带同一 TraceId |
| 慢请求告警 | 请求耗时超过阈值时自动输出 `WARN` 级别日志 |

**可观测性基础设施配置：**

- Compose 文件：[docker/infra/docker-compose.yml](docker/infra/docker-compose.yml)
- Logstash Pipeline：[docker/infra/logstash/pipeline/logstash.conf](docker/infra/logstash/pipeline/logstash.conf)
- 搭建说明：[docker/infra/observability/README.md](docker/infra/observability/README.md)

---

## API 路由一览

### user-service

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/user/register` | 用户注册 |
| POST | `/api/user/login` | 用户登录，返回 Token |
| GET | `/api/user/internal/validate` | Token 验签（内部接口，供 Gateway 调用） |

### stock-service

| 方法 | 路径 | 说明 |
|---|---|---|
| GET/POST/PUT/DELETE | `/api/stock/categories/**` | 分类管理（树形结构） |
| GET/POST/PUT/DELETE | `/api/stock/brands/**` | 品牌管理 |
| GET/POST/PUT/DELETE | `/api/stock/products/**` | 商品管理 |
| GET/POST/PUT/DELETE | `/api/stock/spu/**` | SPU 管理（含规格） |
| GET/POST/PUT/DELETE | `/api/stock/sku/**` | SKU 管理（价格/库存/状态） |
| GET | `/api/stock/search/**` | 商品搜索 |

### order-service

| 方法 | 路径 | 说明 |
|---|---|---|
| GET/POST/DELETE | `/api/app/cart/**` | 购物车管理 |
| GET/POST | `/api/app/orders/**` | 订单创建与查询 |
| POST | `/api/app/payments/**` | 模拟支付 |
| POST | `/api/app/refunds/**` | 发起退款 |
| GET/POST | `/api/app/logistics/**` | 物流查询与轨迹追踪 |
| GET/POST | `/api/app/coupons/**` | 领取和查询优惠券 |
| GET/POST | `/api/merchant/promotions/**` | 促销活动创建与查询 |

### shop-service

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/app/shops/apply` | 用户申请开店（状态初始为 `PENDING`） |
| GET | `/api/merchant/shop` | 商户查询自己的店铺信息（需 `shopId` 请求头） |

---

## 本地环境搭建

### 前置依赖

- Docker & Docker Compose
- JDK 17+
- Maven 3.8+

### 启动基础中间件

使用项目 `docker` 目录下的 Compose 文件一键启动 MySQL、Redis、RocketMQ、Nacos、Seata：

```bash
docker-compose -f docker/docker-compose.yml up -d
```

启动可观测性组件（ELK + SkyWalking）：

```bash
docker-compose -f docker/infra/docker-compose.yml up -d
```

### 服务启动顺序

1. **Nacos**（注册中心 & 配置中心）
2. **user-service**
3. **stock-service**
4. **order-service**
5. **shop-service**
6. **gateway-service**（最后启动）

> **数据库初始化**：启动前请确保已执行各服务 `resources` 目录下的 SQL 脚本完成建表。

### 默认端口

| 服务 | 端口 |
|---|---|
| gateway-service | 8080 |
| user-service | 8081 |
| stock-service | 8082 |
| order-service | 8083 |
| shop-service | 8084 |
| Nacos | 8848 |
| RocketMQ Dashboard | 9876 |

---

## 异常处理规范

全局异常由 `com.literature.common.core.exception.GlobalExceptionHandler` 统一拦截处理，业务代码中通过抛出 `BizException` 传递错误语义，**不在业务层随意 catch 并吞掉异常**。

```java
// 示例：资源不存在时
throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
```

---

## 开发规范速查

- **响应封装**：所有 Controller 返回值必须使用 `ApiResponse<T>`，禁止裸返回实体。
- **分页**：分页数据使用 `PageResponse<T>` 嵌套进 `ApiResponse`。
- **依赖注入**：优先使用构造函数注入。
- **公共逻辑**：通用工具、实体、异常等放在 `common` 模块；日志/链路追踪能力放在 `common-logging-spring-boot-starter`。
- **Java 版本**：优先使用 Java 17+ 特性（`record`、`switch expressions` 等）。
