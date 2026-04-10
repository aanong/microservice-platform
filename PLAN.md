### 商品域重构计划（多级分类 + 品牌 + SPU/SKU + 规格自动生成 + ES 搜索）

#### Summary
将当前单表 `mall_product` 升级为标准电商模型：  
- 分类支持最多 3 级并带分类图。  
- 新增品牌（含品牌图）。  
- 商品拆分为 `SPU`（商品抽象）+ `SKU`（可售规格）。  
- 规格组合由系统自动生成（完整笛卡尔积）。  
- 搜索首期聚焦 SKU 商品检索（ES），并提供增量同步和全量重建。

#### Key Changes
1. **数据模型重构**
- 分类：`mall_category` 新增 `parent_id/level/image_url`（最多 3 级）。
- 品牌：新增 `mall_brand`（`name/code/logo_url/status/sort/remark`）。
- SPU：新增 `mall_spu`（`spu_code/name/category_id/brand_id/main_image_url/detail_images/description/status`）。
- 规格模板：新增 `mall_spu_spec`（规格名，如颜色/尺寸）与 `mall_spu_spec_value`（规格值）。
- SKU：新增 `mall_sku`（`sku_code/spu_id/spec_signature/spec_json/sale_price/stock/status/main_image_url`）。
- 兼容迁移：`mall_product` 逐步下线，订单与购物车改为引用 `sku_id`（保留 `spu_id` 快照字段便于统计）。

2. **编码自动生成方案（系统生成）**
- `spu_code` 规则：`SPU + yyyyMMdd + 6位序号`（按日递增，Redis/MySQL 序列皆可）。
- `sku_code` 规则：`SKU + spu_code尾段 + 4位组合序号`。
- 防冲突策略：DB 唯一索引 + 失败重试（最多 N 次）。
- 幂等策略：同一 SPU 同一 `spec_signature` 只能存在一个 SKU（唯一索引）。

3. **规格自动生成方案（完整笛卡尔积）**
- 输入：规格维度与值（例：颜色[黑/白]，容量[128G/256G]）。
- 生成流程：
  1. 对规格维度按 `sort` 排序并标准化；
  2. 计算笛卡尔积得到全部组合；
  3. 生成 `spec_signature`（如 `颜色:黑|容量:128G`）；
  4. 与现有 SKU 做 diff：新增缺失组合、保留已有组合、将失效组合置 `status=DISABLED`（不硬删）。
- 默认字段策略：新生成 SKU 继承 SPU 默认主图/状态，价格库存默认 0，需运营补全后上架。

4. **服务与接口改造（stock-service）**
- 新增品牌接口：`/api/brands`。
- 分类接口增强：支持 `parentId`、树形查询。
- 新增 SPU 接口：`/api/spus`（create/update/detail/list）。
- 新增 SKU 接口：`/api/skus`（detail/list/updatePrice/updateStock/updateStatus）。
- 新增规格生成接口：`POST /api/spus/{spuId}/skus/generate`（按当前规格自动重建组合）。
- 商品查询主入口逐步从 `/api/products` 迁移为 `SPU + SKU` 组合返回。

5. **订单侧联动改造（order-service）**
- 购物车入参 `productId` 改为 `skuId`；购物车表加 `sku_id/spu_id/spec_json` 快照字段。
- 下单、扣减库存、退款回补全部按 `sku_id`。
- 优惠券秒杀商品从 `seckill_product_id` 迁移为 `seckill_sku_id`（或同时保留 `spu_id` 扩展口径）。
- 过渡期兼容：旧字段保留一版，读写双轨后再清理。

6. **ES 搜索（首期 SKU）**
- 建索引 `mall_sku_search`：`skuId/spuId/spuName/skuCode/categoryId/categoryPath/brandId/brandName/specs/price/stock/status/mainImageUrl/updateTime`。
- 搜索接口：`/api/products/search` 返回可售 SKU 列表，支持 `keyword/categoryId/brandId/specFilters/priceRange/sort/page`。
- 同步机制：
  - SKU/SPU 变更后异步 upsert/delete ES；
  - 分类/品牌改名触发关联文档批量重建；
  - 提供全量重建任务接口与脚本。

#### Test Plan
- 分类：1~3 级创建成功，4 级失败；删除有子分类/引用失败。
- 规格生成：给定 2x2 规格自动生成 4 个 SKU；重复生成不产生重复 SKU。
- 编码：高并发创建 SPU/SKU 时编码唯一且连续可追踪。
- 订单链路：加购/下单/扣库存/退款全部按 `sku_id` 正常。
- 搜索：关键词 + 品牌 + 分类 + 规格过滤命中正确；SKU 上下架后检索结果同步。
- 回归：旧接口在兼容期可读，迁移后数据一致性校验通过。

#### Assumptions
- 已确认：按 **SKU 下单与扣库存**、编码 **系统自动生成**、规格组合 **完整笛卡尔积自动生成**。
- 分类层级固定最多 3 级；图片首期先存 URL 字段（`detail_images` 为 JSON URL 数组）。
- ES 首期仅做商品（SKU）搜索，分类/品牌走数据库查询。
