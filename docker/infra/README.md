# 基础组件安装（国内免登录镜像优先）

本目录用于一键启动本项目基础中间件：
- MySQL 8.0（启用 ROW binlog，供 Canal 订阅）
- Redis 7.2
- Nacos 2.2.3
- RocketMQ 5.1.4（NameServer + Broker + Dashboard）
- Seata Server 1.6.1
- Canal Server 1.1.7（RocketMQ 输出，多 Topic 路由）

## 1. 启动
在 `docker/infra` 目录执行：

```bash
docker compose up -d
```

## 2. 检查
```bash
docker compose ps
```

RocketMQ Dashboard: `http://<宿主机IP>:8080`
Nacos: `http://<宿主机IP>:30048/nacos`

## 3. 初始化 RocketMQ Topic
首次启动后，执行：

```bash
docker exec -it rmq-broker sh -c "sh mqadmin updateTopic -n rmq-namesrv:9876 -c DefaultCluster -t cache-sync-stock -r 4 -w 4"
docker exec -it rmq-broker sh -c "sh mqadmin updateTopic -n rmq-namesrv:9876 -c DefaultCluster -t cache-sync-order -r 4 -w 4"
docker exec -it rmq-broker sh -c "sh mqadmin updateTopic -n rmq-namesrv:9876 -c DefaultCluster -t cache-sync-user -r 4 -w 4"
docker exec -it rmq-broker sh -c "sh mqadmin updateTopic -n rmq-namesrv:9876 -c DefaultCluster -t cache-sync-default -r 4 -w 4"
```

## 4. 导入业务库表
`mysql/init/001_init.sql` 只创建基础库与 canal 用户权限。

业务表请导入项目自带 SQL：
- `stock-service/src/main/resources/sql/schema.sql`
- `order-service/src/main/resources/sql/order_schema.sql`
- `user-service/src/main/resources/sql/user_schema.sql`

## 5. 与当前项目默认端口对齐
- MySQL: `3306`
- Redis: `30379`（密码 `$NMQ#vqc`）
- Nacos: `30048`
- RocketMQ NameServer: `9876`
- Seata: `8091`
- Canal: `11111`

## 6. 停止
```bash
docker compose down
```
