# 校园闲鱼式资源流转功能设计文档

## 1. 背景与目标

在现有「闲置资源」借用闭环（预约 → 审批 → 使用 → 归还）基础上，扩展「转让/出售」交易模式，使平台覆盖校园二手交易场景（教材、电子产品、生活用品等）。

**目标**：在不涉及线上支付的前提下，提供信息发布、意向表达、线下交割、平台确认、双向评价的轻量交易能力。

## 2. 需求约束

- 交易模式：标价但线下交易，平台不处理资金。
- 资源类型：放宽到校园生活类，包括教材、电子产品、生活用品等。
- 沟通方式：仅展示联系方式，不做内置私信/议价。
- 用户范围：仅允许本校学生使用，复用现有实名认证体系。
- 信任机制：复用现有信用分 + 交易完成后双方互评。
- 集成方式：扩展现有「闲置资源」模块，不新建独立频道。

## 3. 方案选择

采用**方案 A：扩展现有闲置资源模块**。

理由：
- 复用 `idle_resource`、`resource_transfer_log`、信用分、通知体系，开发量最小。
- 与当前「闲置资源」菜单和页面保持一致，用户体验连贯。
- 可在 1 周内完成 MVP 并验证。

## 4. 数据模型

### 4.1 idle_resource 表扩展

新增/复用字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| trade_mode | VARCHAR(20) | `borrow`（借用） / `transfer`（转让） |
| expect_price | DECIMAL(10,2) | 期望价格，转让时必填；0 表示免费 |
| contact_info | VARCHAR(255) | 联系方式（微信/手机号/当面交易地点） |
| original_price | DECIMAL(10,2) | 原价/原价标签，展示用 |

**状态说明**：
- `available`：可预约/可购买
- `rented`：借用中（仅 borrow 模式）
- `transferred`：已转让（仅 transfer 模式）
- `unavailable`：已下架

### 4.2 新增 resource_transfer_request 表

记录买家的转让意向。

```sql
CREATE TABLE IF NOT EXISTS resource_transfer_request (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '意向ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    buyer_id BIGINT NOT NULL COMMENT '意向买家ID',
    seller_id BIGINT NOT NULL COMMENT '卖家ID',
    status VARCHAR(50) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/accepted/rejected/cancelled',
    message VARCHAR(500) DEFAULT NULL COMMENT '买家留言',
    contact_info VARCHAR(255) DEFAULT NULL COMMENT '买家联系方式',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_resource_buyer (resource_id, buyer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源转让意向表';
```

### 4.3 resource_transfer_log 表扩展

现有 `resource_transfer_log` 已支持 `transfer_type`（borrow/return/transfer）。本次新增转让成交记录时 `transfer_type='transfer'`，并补充评价字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| from_user_rating | INT | 卖家对买家评分 1-5 |
| from_user_comment | VARCHAR(500) | 卖家对买家评价 |
| to_user_rating | INT | 买家对卖家评分 1-5 |
| to_user_comment | VARCHAR(500) | 买家对卖家评价 |

## 5. 状态流转

### 5.1 转让资源发布

```
用户发布资源 → 选择 trade_mode=transfer → 填写 price/contact_info
→ 状态为 available
```

### 5.2 转让交易闭环

```
买家点击「我想要」 → 提交意向（resource_transfer_request.pending）
→ 卖家收到通知 → 查看意向列表 → 点击「确认成交」
→ 资源状态变为 transferred → 写入 resource_transfer_log(transfer)
→ 双方收到成交通知 → 线下交割完成后双方互评
→ 评价写入 resource_transfer_log，更新对方信用分
```

### 5.3 借用资源状态（保持不变）

```
available → pending → approved → ongoing → return_request → return_confirmed → available
```

## 6. 后端接口

### 6.1 资源发布

扩展现有 `POST /api/resource/publish`，请求体增加：

```json
{
  "resourceName": "线性代数教材",
  "resourceType": "book",
  "tradeMode": "transfer",
  "expectPrice": 15.00,
  "originalPrice": 45.00,
  "contactInfo": "微信：xxx，图书馆门口交易",
  "description": "九成新，有笔记",
  "location": "图书馆"
}
```

### 6.2 资源列表与筛选

扩展现有 `GET /api/resource/list`，新增参数：

- `tradeMode`：borrow / transfer（不传则全部）
- `minPrice`、`maxPrice`：价格区间（仅 transfer 有效）

列表返回增加 `tradeMode`、`expectPrice` 字段。

### 6.3 转让意向接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `POST /api/resource/transfer/request` | POST | 买家提交转让意向 |
| `GET /api/resource/transfer/requests/sent` | GET | 买家查看自己发出的意向 |
| `GET /api/resource/transfer/requests/received` | GET | 卖家查看收到的意向 |
| `PUT /api/resource/transfer/request/{id}/accept` | PUT | 卖家确认成交 |
| `PUT /api/resource/transfer/request/{id}/reject` | PUT | 卖家拒绝意向 |
| `PUT /api/resource/transfer/request/{id}/cancel` | PUT | 买家取消意向 |

### 6.4 评价接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `POST /api/resource/transfer/{logId}/review` | POST | 交易完成后一方对另一方评价 |

请求体：

```json
{
  "targetUserId": 5,
  "rating": 5,
  "comment": "交易顺利，书本很新"
}
```

## 7. 前端改动

### 7.1 资源列表页（ResourceList.vue）

- 新增筛选器：「全部 / 可借用 / 可转让」
- 转让资源卡片显示价格、交易方式标签
- 点击进入详情页

### 7.2 资源详情页（ResourceDetail.vue）

- `tradeMode=borrow`：保持现有「预约借用」按钮
- `tradeMode=transfer`：显示「我想要」按钮
- 卖家视角：显示「收到的意向」入口
- 展示联系方式（登录后可见）

### 7.3 新增「我的转让意向」页面

- 我发出的意向（状态跟踪）
- 我收到的意向（确认/拒绝）

### 7.4 资源发布页

- 新增「交易模式」单选：借用 / 转让
- 选择转让时显示价格、联系方式输入框

## 8. 信用分与通知

### 8.1 信用分影响

- 成交后差评（rating <= 2）扣减信用分 5 分
- 恶意取消已接受的意向扣减信用分 10 分
- 信用分低于 60 分禁止发布转让资源

### 8.2 通知事件

- `transfer_request_received`：卖家收到新的转让意向
- `transfer_request_accepted`：买家意向被确认
- `transfer_request_rejected`：买家意向被拒绝
- `transfer_review_invite`：成交后提示双方评价

## 9. 本校学生限制

复用现有用户体系的 `role_type` 与邮箱/学号认证：

- 仅 `student`、`teacher`、`admin` 角色可发布与交易
- `enterprise` 角色不参与校园二手交易
- 后续可扩展为仅通过 `.edu.cn` 邮箱认证的用户发布

## 10. 安全与合规

- 不接入线上支付，规避支付合规风险
- 联系方式仅对已通过意向/成交的双方展示，减少骚扰
- 支持举报功能（后续迭代），当前先提供下架接口

## 11. 后续可扩展

- 接入担保/仲裁
- 引入线上支付（需评估合规）
- 增加资源热度排序、卖家主页
- 引入 AI 定价建议
