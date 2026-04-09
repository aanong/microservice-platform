# Canal -> RocketMQ Cache Sync

This project expects Canal adapter to publish normalized cache sync events to RocketMQ topics:

- `cache-sync-stock`
- `cache-sync-order`
- `cache-sync-user`

Payload contract (`CacheSyncEvent`):

```json
{
  "eventId": "gtid-or-uuid",
  "db": "microservice_mall",
  "table": "order_main",
  "opType": "UPDATE",
  "pk": {"id": 1001},
  "before": {"id": 1001, "user_id": 2001, "order_status": "PENDING_PAY"},
  "after": {"id": 1001, "user_id": 2001, "order_status": "PAID"},
  "ts": 1710000000000,
  "sourcePos": "mysql-bin.000123:45678"
}
```

Recommended sharding key: primary key value (`id` / `order_id`) to keep per-entity ordering.
