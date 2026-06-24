# 智汇桥后端服务

智汇桥——AI 驱动的产学研用一体化智慧协同系统后端服务。

## 一、项目简介

本项目为「智汇桥」平台提供 RESTful API 接口，支撑四大核心业务模块：

- **科研撮合**：科研项目发布、申请、审核，企业需求发布，科研画像管理
- **资源流转**：闲置资源发布、预约、审批、流转记录
- **教学辅助**：学习资源发布、学习记录、收藏
- **系统管理**：用户管理、内容审核、数据看板、消息通知

## 二、技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.1.6 | 后端主框架 |
| JDK | 17 | Java 运行环境 |
| MyBatis-Plus | 3.5.6 | ORM 框架 |
| Spring Security | 6.x | 认证与权限控制 |
| JWT | 0.12.3 | Token 生成与校验 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.x | 缓存与会话存储 |
| Knife4j | 4.4.0 | API 文档 |
| Maven | 3.8+ | 项目构建工具 |
| Lombok | 1.18+ | 简化实体类代码 |
| Hutool | 5.8.23 | 工具类库 |

## 三、开发环境准备

### 3.1 必需软件

- JDK 17（已配置 `JAVA_HOME`）
- Maven 3.8+
- MySQL 8.0+
- Redis 7.x（可选，当前版本缓存使用较少，但配置中已预留）
- IntelliJ IDEA（推荐）或 Eclipse

### 3.2 数据库初始化

1. 创建数据库：

```sql
CREATE DATABASE zhihuiqiao
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
```

2. 执行建表脚本：

```bash
# 方式一：在 MySQL 客户端中执行
source src/main/resources/db/schema.sql
source src/main/resources/db/data.sql

# 方式二：首次启动时自动初始化（application.yml 中设置 sql.init.mode=always）
```

> 注意：首次启动后，建议将 `application.yml` 中的 `spring.sql.init.mode` 改为 `never`，避免重复插入测试数据。

### 3.3 配置文件

核心配置位于 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zhihuiqiao?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123456
```

请根据本地 MySQL 账号密码修改对应配置。

## 四、项目启动

### 4.1 方式一：IDEA 直接运行

1. 使用 IDEA 打开 `zhihuiqiao-backend` 目录
2. 等待 Maven 依赖加载完成
3. 运行 `com.zhihuiqiao.ZhihuiqiaoApplication` 主类
4. 控制台出现 `Started ZhihuiqiaoApplication` 即启动成功

### 4.2 方式二：Maven 命令行

```bash
# 编译并运行
mvn spring-boot:run

# 或先打包再运行
mvn clean package -DskipTests
java -jar target/zhihuiqiao-backend-1.0.0-SNAPSHOT.jar
```

### 4.3 验证启动

服务默认运行在 `http://localhost:8081`

- 健康检查：`GET http://localhost:8081/actuator/health`（如启用 actuator）
- 接口文档：`http://localhost:8081/doc.html`
- 登录接口：`POST http://localhost:8081/auth/login`

## 五、项目结构

```
zhihuiqiao-backend/
├── src/main/java/com/zhihuiqiao/
│   ├── ZhihuiqiaoApplication.java     # 启动类
│   ├── common/                         # 公共模块
│   │   ├── Result.java                 # 统一响应结果
│   │   ├── ResultCode.java             # 响应码枚举
│   │   ├── BusinessException.java      # 业务异常
│   │   └── GlobalExceptionHandler.java # 全局异常处理
│   ├── config/                         # 配置类
│   │   ├── Knife4jConfig.java          # Knife4j 文档配置
│   │   └── MyBatisPlusConfig.java      # MyBatis-Plus 配置
│   ├── constant/                       # 常量枚举
│   │   ├── RoleType.java               # 角色类型
│   │   └── UserStatus.java             # 用户状态
│   ├── controller/                     # 控制器层
│   │   ├── AuthController.java         # 认证接口
│   │   ├── AdminController.java        # 管理后台接口
│   │   ├── DashboardController.java    # 数据看板接口
│   │   ├── ResearchController.java     # 科研撮合接口
│   │   ├── ResourceController.java     # 资源流转接口
│   │   ├── LearningController.java     # 教学辅助接口
│   │   └── NotificationController.java # 消息通知接口
│   ├── dto/                            # 请求参数 DTO
│   ├── entity/                         # 数据库实体
│   ├── mapper/                         # 数据访问层
│   ├── service/                        # 业务逻辑层
│   │   └── impl/                       # 实现类
│   ├── security/                       # 安全认证
│   │   ├── JwtUtil.java                # JWT 工具类
│   │   ├── JwtAuthenticationFilter.java # JWT 过滤器
│   │   └── SecurityConfig.java         # Security 配置
│   └── vo/                             # 视图对象
├── src/main/resources/
│   ├── application.yml                 # 主配置文件
│   ├── db/
│   │   ├── schema.sql                  # 建表脚本
│   │   └── data.sql                    # 初始化数据
│   └── mapper/                         # MyBatis XML（如需要）
└── pom.xml                             # Maven 配置
```

## 六、接口说明

### 6.1 接口前缀

- 认证接口：`/auth/**`（无需 Token）
- 业务接口：`/api/**`（需要 Token）

### 6.2 认证方式

登录成功后，接口请求需在 Header 中携带：

```http
Authorization: Bearer {token}
```

### 6.3 测试账号

初始化数据已预置以下账号，密码均为 `123456`：

| 账号 | 角色 | 说明 |
|------|------|------|
| admin | 管理员 | 拥有最高权限 |
| student01 | 学生 | 可浏览项目、申请项目、预约资源 |
| teacher01 | 教师 | 可发布项目/资源、审核申请/预约 |
| enterprise01 | 企业 | 可发布企业需求 |

## 七、核心模块接口速查

### 7.1 科研撮合模块

| 功能 | 方法 | 路径 |
|------|------|------|
| 发布科研项目 | POST | /api/research/project |
| 项目列表 | GET | /api/research/project/list |
| 项目详情 | GET | /api/research/project/{id} |
| 申请加入项目 | POST | /api/research/application |
| 我的申请 | GET | /api/research/application/my |
| 审核申请 | PUT | /api/research/application/{id}/audit |
| 发布企业需求 | POST | /api/research/demand |
| 企业需求列表 | GET | /api/research/demand/list |
| 科研画像 | GET/POST | /api/research/profile |

### 7.2 资源流转模块

| 功能 | 方法 | 路径 |
|------|------|------|
| 发布闲置资源 | POST | /api/resource |
| 资源列表 | GET | /api/resource/list |
| 资源详情 | GET | /api/resource/{id} |
| 预约资源 | POST | /api/resource/booking |
| 我的预约 | GET | /api/resource/booking/my |
| 审批预约 | PUT | /api/resource/booking/{id}/audit |

### 7.3 教学辅助模块

| 功能 | 方法 | 路径 |
|------|------|------|
| 发布学习资源 | POST | /api/learning/resource |
| 学习资源列表 | GET | /api/learning/resource/list |
| 学习资源详情 | GET | /api/learning/resource/{id} |
| 记录学习进度 | POST | /api/learning/record |
| 学习中心 | GET | /api/learning/record/my |

### 7.4 系统管理模块

| 功能 | 方法 | 路径 |
|------|------|------|
| 用户列表 | GET | /api/admin/users |
| 更新用户 | PUT | /api/admin/users/{id} |
| 重置密码 | PUT | /api/admin/users/{id}/reset-password |
| 内容审核 | PUT | /api/admin/audit/{type}/{id} |
| 数据看板 | GET | /api/dashboard/statistics |
| 消息通知 | GET/PUT | /api/notifications |

## 八、API 文档

项目集成 Knife4j，启动后访问：

```
http://localhost:8081/doc.html
```

在文档页面可以查看所有接口详情、在线调试并导出 API 文档。

## 九、注意事项

1. **跨域问题**：当前已配置 CORS，允许前端开发环境（`localhost:5173`、`localhost:5174`、`localhost:5175`）访问。
2. **JWT 密钥**：生产环境请修改 `application.yml` 中的 `jwt.secret`。
3. **文件上传**：当前版本图片/头像使用 URL 方式，文件上传接口预留了 multipart 配置，后续可扩展。
4. **Redis**：当前缓存使用较少，但已配置连接，可按需扩展会话缓存、热点数据缓存等。

## 十、常见问题

### Q1: 启动报错数据库连接失败

请检查：
- MySQL 服务是否已启动
- `application.yml` 中的数据库地址、用户名、密码是否正确
- 数据库 `zhihuiqiao` 是否已创建

### Q2: 登录接口返回 401

请检查：
- 请求路径是否正确（`/auth/login`）
- Content-Type 是否为 `application/json`
- 账号密码是否使用测试账号

### Q3: 接口文档无法打开

请检查：
- 后端服务是否已正常启动
- 访问地址是否为 `http://localhost:8081/doc.html`
- 是否存在端口冲突
