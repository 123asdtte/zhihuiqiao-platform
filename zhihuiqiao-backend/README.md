# 智汇桥后端服务（zhihuiqiao-backend）

智汇桥——AI 驱动的产学研用一体化智慧协同系统后端服务，基于 Spring Boot 3.x 构建，提供用户认证、科研撮合、资源流转、教学辅助、消息通知、系统管理等 RESTful API，并支持 WebSocket 实时推送。

---

## 技术栈

| 技术 | 版本/说明 |
| --- | --- |
| JDK | 17 |
| Spring Boot | 3.1.6 |
| Spring Security | JWT 认证 |
| MyBatis-Plus | 3.5.6 |
| MySQL | 8.x |
| Redis | 缓存 / 会话 |
| WebSocket | 实时通知推送 |
| Maven | 构建工具 |

---

## 项目结构

```
zhihuiqiao-backend
├── src/main/java/com/zhihuiqiao
│   ├── annotation          # 自定义注解（如操作日志注解）
│   ├── common              # 通用结果封装、异常处理等
│   ├── config              # Spring Boot 配置类
│   ├── controller          # RESTful API 控制器
│   ├── dto                 # 数据传输对象
│   ├── entity              # 数据库实体类
│   ├── mapper              # MyBatis-Plus Mapper 接口
│   ├── security            # Spring Security / JWT 相关
│   ├── service             # 业务逻辑层
│   ├── vo                  # 视图对象
│   └── websocket           # WebSocket 通知处理器
├── src/main/resources
│   ├── application.yml     # 主配置文件
│   ├── db                  # 数据库初始化脚本
│   └── mapper              # MyBatis XML 映射文件
└── pom.xml
```

---

## 环境要求

1. JDK 17 及以上
2. Maven 3.8 及以上
3. MySQL 8.0 及以上
4. Redis 6.0 及以上（本地开发可用默认端口 6379）

---

## 数据库准备

1. 创建数据库：

```sql
CREATE DATABASE zhihuiqiao
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
```

2. 修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zhihuiqiao?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 你的密码
```

3. 首次启动时，可将 `spring.sql.init.mode` 设置为 `always`，让框架自动执行 `db/schema.sql` 和 `db/data.sql` 初始化表结构和基础数据。初始化完成后建议改回 `never`，避免重复插入：

```yaml
spring:
  sql:
    init:
      mode: always   # 首次启动使用，之后改为 never
```

---

## 配置文件说明

核心配置位于 `src/main/resources/application.yml`：

| 配置项 | 说明 | 默认值 |
| --- | --- | --- |
| `server.port` | 后端服务端口 | 8081 |
| `server.servlet.encoding.charset` | 响应编码 | UTF-8 |
| `spring.datasource.url` | MySQL 连接地址 | 见配置文件 |
| `spring.data.redis.*` | Redis 连接配置 | localhost:6379 |
| `spring.servlet.multipart.max-file-size` | 单文件上传大小限制 | 10MB |
| `jwt.secret` | JWT 签名密钥 | 务必在生产环境替换 |
| `jwt.expiration` | Token 有效期（毫秒） | 86400000（1天） |

**生产环境注意：**
- 务必将 `jwt.secret` 替换为高强度随机字符串。
- 关闭 `org.springframework.security` 的 debug 日志。
- 配置 HTTPS 并关闭 WebSocket 的 `setAllowedOrigins("*")`。

---

## 启动方式

### 本地开发启动

```bash
# 进入后端目录
cd zhihuiqiao-backend

# 安装依赖并启动（跳过测试，加快启动速度）
mvn -B -DskipTests spring-boot:run
```

服务启动后访问：
- API 根地址：`http://localhost:8081`
- Swagger / Knife4j 文档：`http://localhost:8081/doc.html`

### 打包构建

```bash
mvn -B -DskipTests clean package
```

构建产物位于 `target/zhihuiqiao-backend-1.0.0-SNAPSHOT.jar`。

### 运行 jar 包

```bash
java -jar target/zhihuiqiao-backend-1.0.0-SNAPSHOT.jar
```

---

## 默认测试账号

数据库初始化脚本 `db/data.sql` 中已内置部分测试账号，例如：

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| admin | 123456 | 管理员 |
| student01 | 123456 | 学生 |
| teacher01 | 123456 | 教师 |
| enterprise01 | 123456 | 企业用户 |

> 实际密码以数据库初始化脚本或运行时的默认实现为准。

---

## 主要模块说明

| 模块 | 接口前缀 | 说明 |
| --- | --- | --- |
| 认证模块 | `/auth/**` | 登录、注册 |
| 科研撮合 | `/api/research/**` | 科研项目、企业需求、项目申请、科研画像 |
| 资源流转 | `/api/resource/**` | 闲置资源、资源预约、流转记录 |
| 教学辅助 | `/api/learning/**` | 学习资源、学习中心 |
| 消息通知 | `/api/notifications/**` | 通知列表、未读数量、标记已读 |
| 系统管理 | `/api/admin/**` | 用户管理、内容审核、数据看板、操作日志 |
| 实时通知 | `/ws/notification` | WebSocket 端点，用于推送实时通知 |

---

## WebSocket 实时通知

后端通过 `/ws/notification` 端点向客户端推送实时通知。连接时需在 query 参数或请求头中携带 JWT Token：

```
ws://localhost:8081/ws/notification?token=你的JWT_TOKEN
```

触发推送的业务场景包括：
- 学生提交项目加入申请 → 通知项目发布人
- 项目申请被审核 → 通知申请人
- 用户提交资源预约 → 通知资源所有者
- 资源预约被审核 → 通知预约人

---

## 常见问题

1. **端口 8081 被占用**
   ```bash
   netstat -ano | findstr :8081
   taskkill /PID <PID> /F
   ```

2. **MySQL 连接失败**
   - 确认 MySQL 服务已启动。
   - 确认用户名、密码、数据库名正确。
   - 首次连接建议添加 `allowPublicKeyRetrieval=true`。

3. **Redis 连接失败**
   - 确认 Redis 服务已启动，默认端口 6379。
   - 如 Redis 设置了密码，请在 `application.yml` 中配置 `spring.data.redis.password`。

4. **中文乱码**
   - 确认数据库、表、连接串均使用 UTF-8 / utf8mb4。
   - 后端 `server.servlet.encoding` 已强制使用 UTF-8。
