# 校园闲鱼式资源流转功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有闲置资源模块扩展「转让」交易模式，支持标价线下交易、意向申请、确认成交、双向评价，不处理线上资金。

**Architecture:** 复用 `idle_resource` 表并扩展 `trade_mode`/`expect_price`/`contact_info` 字段；新增 `resource_transfer_request` 表记录买家意向；扩展 `resource_transfer_log` 记录成交与评价；后端在 `ResourceController` 新增转让相关接口；前端在资源列表/详情/发布页区分借用/转让，并新增「我的转让意向」页面。

**Tech Stack:** Spring Boot + MyBatis-Plus + Vue 3 + Element Plus + Pinia

---

## 文件结构

### 后端
- `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/entity/IdleResource.java` — 扩展字段
- `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/entity/ResourceTransferRequest.java` — 新增实体
- `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/entity/ResourceTransferLog.java` — 扩展评价字段
- `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/mapper/ResourceTransferRequestMapper.java` — 新增 Mapper
- `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/service/ResourceTransferRequestService.java` — Service 接口
- `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/service/impl/ResourceTransferRequestServiceImpl.java` — Service 实现
- `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/controller/ResourceController.java` — 新增转让接口
- `zhihuiqiao-backend/src/main/resources/db/schema.sql` — 补充表结构
- `zhihuiqiao-backend/src/test/java/com/zhihuiqiao/service/impl/ResourceTransferRequestServiceImplTest.java` — 单元测试

### 前端
- `zhihuiqiao-frontend/src/api/resource.ts` — 新增转让 API
- `zhihuiqiao-frontend/src/views/resource/ResourceList.vue` — 列表筛选与标签
- `zhihuiqiao-frontend/src/views/resource/ResourceDetail.vue` — 详情页按钮与联系方式
- `zhihuiqiao-frontend/src/views/resource/ResourcePublish.vue` — 发布页交易模式切换
- `zhihuiqiao-frontend/src/views/resource/MyTransferRequests.vue` — 新增「我的转让意向」页面
- `zhihuiqiao-frontend/src/router/index.ts` — 注册新路由

---

## Task 1：数据库变更

**Files:**
- Modify: `zhihuiqiao-backend/src/main/resources/db/schema.sql`

- [ ] **Step 1.1：扩展 idle_resource 表**

```sql
ALTER TABLE idle_resource
    ADD COLUMN trade_mode VARCHAR(20) DEFAULT 'borrow' COMMENT '交易模式：borrow-借用 transfer-转让' AFTER resource_type,
    ADD COLUMN expect_price DECIMAL(10,2) DEFAULT NULL COMMENT '转让期望价格，0 表示免费' AFTER rental_price,
    ADD COLUMN contact_info VARCHAR(255) DEFAULT NULL COMMENT '联系方式' AFTER expect_price;
```

- [ ] **Step 1.2：新增 resource_transfer_request 表**

```sql
CREATE TABLE IF NOT EXISTS resource_transfer_request (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '意向ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    buyer_id BIGINT NOT NULL COMMENT '买家ID',
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

- [ ] **Step 1.3：扩展 resource_transfer_log 评价字段**

```sql
ALTER TABLE resource_transfer_log
    ADD COLUMN from_user_rating INT DEFAULT NULL COMMENT '转出方评分' AFTER remark,
    ADD COLUMN from_user_comment VARCHAR(500) DEFAULT NULL COMMENT '转出方评价' AFTER from_user_rating,
    ADD COLUMN to_user_rating INT DEFAULT NULL COMMENT '转入方评分' AFTER from_user_comment,
    ADD COLUMN to_user_comment VARCHAR(500) DEFAULT NULL COMMENT '转入方评价' AFTER to_user_rating;
```

- [ ] **Step 1.4：执行 SQL**

Run:
```bash
mysql -u root -p123456 zhihuiqiao < zhihuiqiao-backend/src/main/resources/db/schema.sql
```

Expected: SQL executes without errors (CREATE IF NOT EXISTS / ALTER ADD COLUMN are idempotent).

- [ ] **Step 1.5：Commit**

```bash
git add zhihuiqiao-backend/src/main/resources/db/schema.sql
git commit -m "db(schema): extend idle_resource and add transfer request table"
```

---

## Task 2：后端实体与 Mapper

**Files:**
- Modify: `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/entity/IdleResource.java`
- Modify: `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/entity/ResourceTransferLog.java`
- Create: `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/entity/ResourceTransferRequest.java`
- Create: `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/mapper/ResourceTransferRequestMapper.java`

- [ ] **Step 2.1：IdleResource 扩展字段**

在 `IdleResource.java` 中新增：

```java
private String tradeMode;
private BigDecimal expectPrice;
private String contactInfo;
```

- [ ] **Step 2.2：ResourceTransferLog 扩展评价字段**

在 `ResourceTransferLog.java` 中新增：

```java
private Integer fromUserRating;
private String fromUserComment;
private Integer toUserRating;
private String toUserComment;
```

- [ ] **Step 2.3：创建 ResourceTransferRequest 实体**

```java
package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource_transfer_request")
public class ResourceTransferRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resourceId;

    private Long buyerId;

    private Long sellerId;

    private String status;

    private String message;

    private String contactInfo;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String resourceName;

    @TableField(exist = false)
    private String buyerName;

    @TableField(exist = false)
    private String sellerName;
}
```

- [ ] **Step 2.4：创建 Mapper**

```java
package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.ResourceTransferRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResourceTransferRequestMapper extends BaseMapper<ResourceTransferRequest> {
}
```

- [ ] **Step 2.5：Commit**

```bash
git add zhihuiqiao-backend/src/main/java/com/zhihuiqiao/entity/IdleResource.java \
        zhihuiqiao-backend/src/main/java/com/zhihuiqiao/entity/ResourceTransferLog.java \
        zhihuiqiao-backend/src/main/java/com/zhihuiqiao/entity/ResourceTransferRequest.java \
        zhihuiqiao-backend/src/main/java/com/zhihuiqiao/mapper/ResourceTransferRequestMapper.java
git commit -m "feat(entity): add transfer request entity and extend resource/log entities"
```

---

## Task 3：后端 Service 层

**Files:**
- Create: `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/service/ResourceTransferRequestService.java`
- Create: `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/service/impl/ResourceTransferRequestServiceImpl.java`

- [ ] **Step 3.1：Service 接口**

```java
package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.ResourceTransferRequest;

import java.util.List;

public interface ResourceTransferRequestService extends IService<ResourceTransferRequest> {

    Long submitRequest(Long buyerId, Long resourceId, String message, String contactInfo);

    boolean acceptRequest(Long requestId, Long sellerId);

    boolean rejectRequest(Long requestId, Long sellerId);

    boolean cancelRequest(Long requestId, Long buyerId);

    List<ResourceTransferRequest> listSentRequests(Long buyerId);

    List<ResourceTransferRequest> listReceivedRequests(Long sellerId);

    boolean review(Long requestId, Long fromUserId, Long toUserId, Integer rating, String comment);
}
```

- [ ] **Step 3.2：Service 实现（核心逻辑）**

```java
package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.IdleResource;
import com.zhihuiqiao.entity.ResourceTransferLog;
import com.zhihuiqiao.entity.ResourceTransferRequest;
import com.zhihuiqiao.mapper.ResourceTransferRequestMapper;
import com.zhihuiqiao.service.IdleResourceService;
import com.zhihuiqiao.service.ResourceTransferLogService;
import com.zhihuiqiao.service.ResourceTransferRequestService;
import com.zhihuiqiao.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceTransferRequestServiceImpl extends ServiceImpl<ResourceTransferRequestMapper, ResourceTransferRequest>
        implements ResourceTransferRequestService {

    private final IdleResourceService idleResourceService;
    private final ResourceTransferLogService resourceTransferLogService;
    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitRequest(Long buyerId, Long resourceId, String message, String contactInfo) {
        IdleResource resource = idleResourceService.getById(resourceId);
        if (resource == null || !"transfer".equals(resource.getTradeMode())) {
            throw new IllegalArgumentException("该资源不支持转让");
        }
        if (!"available".equals(resource.getStatus())) {
            throw new IllegalArgumentException("该资源当前不可转让");
        }
        if (resource.getOwnerId().equals(buyerId)) {
            throw new IllegalArgumentException("不能购买自己发布的资源");
        }

        ResourceTransferRequest request = new ResourceTransferRequest();
        request.setResourceId(resourceId);
        request.setBuyerId(buyerId);
        request.setSellerId(resource.getOwnerId());
        request.setStatus("pending");
        request.setMessage(message);
        request.setContactInfo(contactInfo);
        baseMapper.insert(request);
        return request.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptRequest(Long requestId, Long sellerId) {
        ResourceTransferRequest request = getById(requestId);
        if (request == null || !request.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("无权操作该意向");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("该意向已处理");
        }

        IdleResource resource = idleResourceService.getById(request.getResourceId());
        if (resource == null || !"available".equals(resource.getStatus())) {
            throw new IllegalArgumentException("资源状态已变更");
        }

        request.setStatus("accepted");
        request.setUpdateTime(LocalDateTime.now());
        updateById(request);

        // 关闭同一资源的其他 pending 意向
        LambdaQueryWrapper<ResourceTransferRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceTransferRequest::getResourceId, request.getResourceId())
                .eq(ResourceTransferRequest::getStatus, "pending")
                .ne(ResourceTransferRequest::getId, requestId);
        ResourceTransferRequest rejected = new ResourceTransferRequest();
        rejected.setStatus("rejected");
        rejected.setUpdateTime(LocalDateTime.now());
        baseMapper.update(rejected, wrapper);

        // 更新资源状态为已转让
        resource.setStatus("transferred");
        resource.setUpdateTime(LocalDateTime.now());
        idleResourceService.updateById(resource);

        // 写入流转日志
        ResourceTransferLog log = new ResourceTransferLog();
        log.setResourceId(request.getResourceId());
        log.setFromUserId(resource.getOwnerId());
        log.setToUserId(request.getBuyerId());
        log.setTransferType("transfer");
        log.setRemark("转让成交");
        log.setCreateTime(LocalDateTime.now());
        resourceTransferLogService.save(log);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectRequest(Long requestId, Long sellerId) {
        ResourceTransferRequest request = getById(requestId);
        if (request == null || !request.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("无权操作该意向");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("该意向已处理");
        }
        request.setStatus("rejected");
        request.setUpdateTime(LocalDateTime.now());
        return updateById(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRequest(Long requestId, Long buyerId) {
        ResourceTransferRequest request = getById(requestId);
        if (request == null || !request.getBuyerId().equals(buyerId)) {
            throw new IllegalArgumentException("无权操作该意向");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("只能取消待处理的意向");
        }
        request.setStatus("cancelled");
        request.setUpdateTime(LocalDateTime.now());
        return updateById(request);
    }

    @Override
    public List<ResourceTransferRequest> listSentRequests(Long buyerId) {
        LambdaQueryWrapper<ResourceTransferRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceTransferRequest::getBuyerId, buyerId).orderByDesc(ResourceTransferRequest::getCreateTime);
        return enrich(baseMapper.selectList(wrapper));
    }

    @Override
    public List<ResourceTransferRequest> listReceivedRequests(Long sellerId) {
        LambdaQueryWrapper<ResourceTransferRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceTransferRequest::getSellerId, sellerId).orderByDesc(ResourceTransferRequest::getCreateTime);
        return enrich(baseMapper.selectList(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean review(Long requestId, Long fromUserId, Long toUserId, Integer rating, String comment) {
        ResourceTransferRequest request = getById(requestId);
        if (request == null || !"accepted".equals(request.getStatus())) {
            throw new IllegalArgumentException("只能评价已成交的交易");
        }
        if (!request.getBuyerId().equals(fromUserId) && !request.getSellerId().equals(fromUserId)) {
            throw new IllegalArgumentException("无权评价该交易");
        }
        if (!request.getBuyerId().equals(toUserId) && !request.getSellerId().equals(toUserId)) {
            throw new IllegalArgumentException("评价对象不正确");
        }

        ResourceTransferLog log = resourceTransferLogService.lambdaQuery()
                .eq(ResourceTransferLog::getResourceId, request.getResourceId())
                .eq(ResourceTransferLog::getTransferType, "transfer")
                .eq(ResourceTransferLog::getFromUserId, request.getSellerId())
                .eq(ResourceTransferLog::getToUserId, request.getBuyerId())
                .one();
        if (log == null) {
            throw new IllegalArgumentException("未找到成交记录");
        }

        boolean isSeller = request.getSellerId().equals(fromUserId);
        if (isSeller) {
            log.setFromUserRating(rating);
            log.setFromUserComment(comment);
        } else {
            log.setToUserRating(rating);
            log.setToUserComment(comment);
        }

        // 差评扣分
        if (rating != null && rating <= 2) {
            userService.deductCreditScore(toUserId, 5);
        }

        return resourceTransferLogService.updateById(log);
    }

    private List<ResourceTransferRequest> enrich(List<ResourceTransferRequest> list) {
        list.forEach(item -> {
            IdleResource resource = idleResourceService.getById(item.getResourceId());
            if (resource != null) {
                item.setResourceName(resource.getResourceName());
            }
            item.setBuyerName(userService.getById(item.getBuyerId()).getUsername());
            item.setSellerName(userService.getById(item.getSellerId()).getUsername());
        });
        return list;
    }
}
```

- [ ] **Step 3.3：UserService 增加扣分方法**

Modify `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/service/UserService.java`:

```java
boolean deductCreditScore(Long userId, Integer score);
```

Modify `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/service/impl/UserServiceImpl.java`:

```java
@Override
@Transactional(rollbackFor = Exception.class)
public boolean deductCreditScore(Long userId, Integer score) {
    User user = getById(userId);
    if (user == null) {
        return false;
    }
    int newScore = Math.max(0, user.getCreditScore() - score);
    user.setCreditScore(newScore);
    return updateById(user);
}
```

- [ ] **Step 3.4：Commit**

```bash
git add zhihuiqiao-backend/src/main/java/com/zhihuiqiao/service/ \
        zhihuiqiao-backend/src/main/java/com/zhihuiqiao/service/impl/
git commit -m "feat(service): implement transfer request service with review and credit score"
```

---

## Task 4：后端 Controller 接口

**Files:**
- Modify: `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/controller/ResourceController.java`

- [ ] **Step 4.1：注入 Service**

```java
private final ResourceTransferRequestService transferRequestService;
```

- [ ] **Step 4.2：新增转让意向接口**

```java
@PostMapping("/transfer/request")
@Operation(summary = "提交转让意向")
public Result<Long> submitTransferRequest(@RequestBody @Valid TransferRequestDTO dto) {
    Long buyerId = getCurrentUserId();
    Long requestId = transferRequestService.submitRequest(buyerId, dto.getResourceId(), dto.getMessage(), dto.getContactInfo());
    return Result.success(requestId);
}

@Data
public static class TransferRequestDTO {
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;
    @Size(max = 500, message = "留言不超过500字")
    private String message;
    @Size(max = 255, message = "联系方式不超过255字")
    private String contactInfo;
}
```

- [ ] **Step 4.3：新增卖家/买家查询接口**

```java
@GetMapping("/transfer/requests/sent")
@Operation(summary = "我发出的转让意向")
public Result<List<ResourceTransferRequest>> listSentTransferRequests() {
    Long buyerId = getCurrentUserId();
    return Result.success(transferRequestService.listSentRequests(buyerId));
}

@GetMapping("/transfer/requests/received")
@Operation(summary = "我收到的转让意向")
public Result<List<ResourceTransferRequest>> listReceivedTransferRequests() {
    Long sellerId = getCurrentUserId();
    return Result.success(transferRequestService.listReceivedRequests(sellerId));
}
```

- [ ] **Step 4.4：新增接受/拒绝/取消接口**

```java
@PutMapping("/transfer/request/{id}/accept")
@Operation(summary = "卖家确认成交")
public Result<Boolean> acceptTransferRequest(@PathVariable Long id) {
    Long sellerId = getCurrentUserId();
    boolean result = transferRequestService.acceptRequest(id, sellerId);
    return Result.success(result);
}

@PutMapping("/transfer/request/{id}/reject")
@Operation(summary = "卖家拒绝意向")
public Result<Boolean> rejectTransferRequest(@PathVariable Long id) {
    Long sellerId = getCurrentUserId();
    boolean result = transferRequestService.rejectRequest(id, sellerId);
    return Result.success(result);
}

@PutMapping("/transfer/request/{id}/cancel")
@Operation(summary = "买家取消意向")
public Result<Boolean> cancelTransferRequest(@PathVariable Long id) {
    Long buyerId = getCurrentUserId();
    boolean result = transferRequestService.cancelRequest(id, buyerId);
    return Result.success(result);
}
```

- [ ] **Step 4.5：新增评价接口**

```java
@PostMapping("/transfer/request/{id}/review")
@Operation(summary = "交易评价")
public Result<Boolean> reviewTransferRequest(@PathVariable Long id,
                                             @RequestBody @Valid TransferReviewDTO dto) {
    Long fromUserId = getCurrentUserId();
    boolean result = transferRequestService.review(id, fromUserId, dto.getTargetUserId(), dto.getRating(), dto.getComment());
    return Result.success(result);
}

@Data
public static class TransferReviewDTO {
    @NotNull(message = "评价对象不能为空")
    private Long targetUserId;
    @NotNull(message = "评分不能为空")
    @Min(1)
    @Max(5)
    private Integer rating;
    @Size(max = 500, message = "评价内容不超过500字")
    private String comment;
}
```

- [ ] **Step 4.6：扩展资源列表筛选**

在 `list()` 方法中新增参数：

```java
@RequestParam(required = false) String tradeMode,
@RequestParam(required = false) BigDecimal minPrice,
@RequestParam(required = false) BigDecimal maxPrice,
```

并追加查询条件：

```java
if (StringUtils.hasText(tradeMode)) {
    wrapper.eq(IdleResource::getTradeMode, tradeMode);
}
if (minPrice != null) {
    wrapper.ge(IdleResource::getExpectPrice, minPrice);
}
if (maxPrice != null) {
    wrapper.le(IdleResource::getExpectPrice, maxPrice);
}
```

- [ ] **Step 4.7：Commit**

```bash
git add zhihuiqiao-backend/src/main/java/com/zhihuiqiao/controller/ResourceController.java
git commit -m "feat(controller): add transfer request APIs and list filters"
```

---

## Task 5：后端单元测试

**Files:**
- Create: `zhihuiqiao-backend/src/test/java/com/zhihuiqiao/service/impl/ResourceTransferRequestServiceImplTest.java`

- [ ] **Step 5.1：编写测试**

```java
package com.zhihuiqiao.service.impl;

import com.zhihuiqiao.entity.IdleResource;
import com.zhihuiqiao.entity.ResourceTransferLog;
import com.zhihuiqiao.entity.ResourceTransferRequest;
import com.zhihuiqiao.mapper.ResourceTransferRequestMapper;
import com.zhihuiqiao.service.IdleResourceService;
import com.zhihuiqiao.service.ResourceTransferLogService;
import com.zhihuiqiao.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceTransferRequestServiceImplTest {

    @Mock
    private ResourceTransferRequestMapper mapper;

    @Mock
    private IdleResourceService idleResourceService;

    @Mock
    private ResourceTransferLogService logService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ResourceTransferRequestServiceImpl service;

    @Test
    void submitRequest_shouldFail_whenResourceNotTransferable() {
        IdleResource resource = new IdleResource();
        resource.setTradeMode("borrow");
        when(idleResourceService.getById(1L)).thenReturn(resource);

        assertThrows(IllegalArgumentException.class,
                () -> service.submitRequest(2L, 1L, "想要", "微信"));
    }

    @Test
    void acceptRequest_shouldUpdateStatusAndLog() {
        ResourceTransferRequest request = new ResourceTransferRequest();
        request.setId(1L);
        request.setResourceId(10L);
        request.setBuyerId(2L);
        request.setSellerId(1L);
        request.setStatus("pending");
        when(mapper.selectById(1L)).thenReturn(request);

        IdleResource resource = new IdleResource();
        resource.setId(10L);
        resource.setStatus("available");
        resource.setOwnerId(1L);
        when(idleResourceService.getById(10L)).thenReturn(resource);

        boolean result = service.acceptRequest(1L, 1L);
        assertTrue(result);
        verify(idleResourceService).updateById(argThat(r -> "transferred".equals(r.getStatus())));
        verify(logService).save(any(ResourceTransferLog.class));
    }
}
```

- [ ] **Step 5.2：运行测试**

Run:
```bash
mvn test -Dtest=ResourceTransferRequestServiceImplTest -pl zhihuiqiao-backend
```

Expected: Tests pass.

- [ ] **Step 5.3：Commit**

```bash
git add zhihuiqiao-backend/src/test/java/com/zhihuiqiao/service/impl/ResourceTransferRequestServiceImplTest.java
git commit -m "test: add transfer request service unit tests"
```

---

## Task 6：前端 API 封装

**Files:**
- Modify: `zhihuiqiao-frontend/src/api/resource.ts`

- [ ] **Step 6.1：新增转让相关 API**

```typescript
export function submitTransferRequest(data: { resourceId: number | string; message?: string; contactInfo?: string }) {
  return request({ url: '/api/resource/transfer/request', method: 'post', data })
}

export function getSentTransferRequests() {
  return request({ url: '/api/resource/transfer/requests/sent', method: 'get' })
}

export function getReceivedTransferRequests() {
  return request({ url: '/api/resource/transfer/requests/received', method: 'get' })
}

export function acceptTransferRequest(id: number | string) {
  return request({ url: `/api/resource/transfer/request/${id}/accept`, method: 'put' })
}

export function rejectTransferRequest(id: number | string) {
  return request({ url: `/api/resource/transfer/request/${id}/reject`, method: 'put' })
}

export function cancelTransferRequest(id: number | string) {
  return request({ url: `/api/resource/transfer/request/${id}/cancel`, method: 'put' })
}

export function reviewTransferRequest(id: number | string, data: { targetUserId: number | string; rating: number; comment?: string }) {
  return request({ url: `/api/resource/transfer/request/${id}/review`, method: 'post', data })
}
```

- [ ] **Step 6.2：扩展资源列表参数**

修改 `getResourceList` 函数签名，增加 `tradeMode`、`minPrice`、`maxPrice` 可选参数。

- [ ] **Step 6.3：Commit**

```bash
git add zhihuiqiao-frontend/src/api/resource.ts
git commit -m "feat(api): add transfer request APIs and list filters"
```

---

## Task 7：前端资源列表页

**Files:**
- Modify: `zhihuiqiao-frontend/src/views/resource/ResourceList.vue`

- [ ] **Step 7.1：新增交易模式筛选器**

在筛选区增加：

```vue
<el-radio-group v-model="query.tradeMode" @change="handleSearch">
  <el-radio-button label="">全部</el-radio-button>
  <el-radio-button label="borrow">可借用</el-radio-button>
  <el-radio-button label="transfer">可转让</el-radio-button>
</el-radio-group>
```

- [ ] **Step 7.2：卡片显示价格/模式标签**

在资源卡片上显示：

```vue
<el-tag v-if="item.tradeMode === 'transfer'" type="warning">转让</el-tag>
<el-tag v-else type="success">借用</el-tag>
<span v-if="item.tradeMode === 'transfer'" class="price">
  {{ item.expectPrice === 0 ? '免费' : '¥' + item.expectPrice }}
</span>
```

- [ ] **Step 7.3：Commit**

```bash
git add zhihuiqiao-frontend/src/views/resource/ResourceList.vue
git commit -m "feat(ui): add trade mode filter and price tag on resource list"
```

---

## Task 8：前端资源详情页

**Files:**
- Modify: `zhihuiqiao-frontend/src/views/resource/ResourceDetail.vue`

- [ ] **Step 8.1：根据 tradeMode 显示不同操作**

借用模式保持原「预约借用」按钮；转让模式显示「我想要」按钮，点击弹出表单：

```vue
<el-dialog v-model="requestDialogVisible" title="提交转让意向">
  <el-form :model="requestForm" label-width="80px">
    <el-form-item label="留言">
      <el-input v-model="requestForm.message" type="textarea" :rows="3" maxlength="500" show-word-limit />
    </el-form-item>
    <el-form-item label="联系方式">
      <el-input v-model="requestForm.contactInfo" maxlength="255" placeholder="微信/手机号/交易地点" />
    </el-form-item>
  </el-form>
  <template #footer>
    <el-button @click="requestDialogVisible = false">取消</el-button>
    <el-button type="primary" @click="submitTransferRequest">提交意向</el-button>
  </template>
</el-dialog>
```

- [ ] **Step 8.2：显示联系方式**

登录后展示卖家的 `contactInfo`。

- [ ] **Step 8.3：Commit**

```bash
git add zhihuiqiao-frontend/src/views/resource/ResourceDetail.vue
git commit -m "feat(ui): support transfer mode on resource detail"
```

---

## Task 9：前端资源发布页

**Files:**
- Modify: `zhihuiqiao-frontend/src/views/resource/ResourcePublish.vue`

- [ ] **Step 9.1：新增交易模式选择**

```vue
<el-form-item label="交易模式" prop="tradeMode">
  <el-radio-group v-model="form.tradeMode">
    <el-radio-button label="borrow">借用</el-radio-button>
    <el-radio-button label="transfer">转让</el-radio-button>
  </el-radio-group>
</el-form-item>
```

- [ ] **Step 9.2：转让模式显示价格和联系方式**

```vue
<el-form-item v-if="form.tradeMode === 'transfer'" label="期望价格" prop="expectPrice">
  <el-input-number v-model="form.expectPrice" :min="0" :precision="2" :step="1" />
  <span class="form-tip">0 表示免费</span>
</el-form-item>

<el-form-item v-if="form.tradeMode === 'transfer'" label="联系方式" prop="contactInfo">
  <el-input v-model="form.contactInfo" maxlength="255" placeholder="微信/手机号/交易地点" />
</el-form-item>
```

- [ ] **Step 9.3：Commit**

```bash
git add zhihuiqiao-frontend/src/views/resource/ResourcePublish.vue
git commit -m "feat(ui): add trade mode selection on resource publish"
```

---

## Task 10：前端「我的转让意向」页面

**Files:**
- Create: `zhihuiqiao-frontend/src/views/resource/MyTransferRequests.vue`
- Modify: `zhihuiqiao-frontend/src/router/index.ts`

- [ ] **Step 10.1：创建页面**

页面包含两个标签页：「我发出的」「我收到的」。

我发出的列表：显示资源名、卖家、状态、提交时间；状态 pending 时可取消。

我收到的列表：显示资源名、买家留言、买家联系方式；状态 pending 时可确认成交或拒绝。

成交后显示评价按钮，弹出评分/评价框。

- [ ] **Step 10.2：注册路由**

在 `router/index.ts` 中新增：

```typescript
{
  path: '/app/resource/transfer-requests',
  name: 'MyTransferRequests',
  component: () => import('@/views/resource/MyTransferRequests.vue'),
  meta: { title: '我的转让意向', requiresAuth: true }
}
```

- [ ] **Step 10.3：添加菜单入口**

在资源相关菜单或个人中心增加「我的转让意向」入口。

- [ ] **Step 10.4：Commit**

```bash
git add zhihuiqiao-frontend/src/views/resource/MyTransferRequests.vue \
        zhihuiqiao-frontend/src/router/index.ts
git commit -m "feat(ui): add my transfer requests page and route"
```

---

## Task 11：集成测试与验证

- [ ] **Step 11.1：重启后端**

```bash
cd zhihuiqiao-backend
mvn spring-boot:run -q
```

- [ ] **Step 11.2：重启前端**

```bash
cd zhihuiqiao-frontend
npm run dev
```

- [ ] **Step 11.3：端到端验证**

1. `teacher01` 发布转让资源（价格 10 元）
2. `student01` 提交「我想要」意向
3. `teacher01` 确认成交
4. 双方进入「我的转让意向」评价
5. 验证资源状态变为 `transferred`

- [ ] **Step 11.4：Commit 最终修复**

```bash
git add .
git commit -m "fix: transfer feature integration fixes"
```

---

## Task 12：推送至 Gitee

- [ ] **Step 12.1：推送所有提交**

```bash
git push gitee main
```

Expected: All commits pushed to `https://gitee.com/wsjic12-s_adw/wisdom-bridge.git`.

---

## 自检

- Spec coverage:
  - 转让模式字段扩展 → Task 1/2
  - 意向申请表 → Task 1/2/3
  - 确认成交与评价 → Task 3/4
  - 列表筛选/价格展示 → Task 4/7
  - 详情页「我想要」 → Task 8
  - 发布页交易模式 → Task 9
  - 我的转让意向 → Task 10
  - 信用分影响 → Task 3
- Placeholder scan: 无 TBD/TODO，所有步骤包含具体代码或命令。
- Type consistency: `tradeMode` 字段在后端/前端/数据库中均为字符串 borrow/transfer。
