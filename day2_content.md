<title>6月16日项目开发日志</title>

# 「智汇桥」——AI驱动的产学研用一体化智慧协同系统



## 开发日志文档（Day 2）

### 一、项目基本信息

| 项目名称 | 智汇桥——AI驱动的产学研用一体化智慧协同系统 |
|-|-|
| 项目定位 | 连接教师、学生、实验室与校企资源，打造师-生-机协同的智慧校园中心 |
| 核心模块 | 科研项目智能撮合、校园闲置资源流转、教学辅助个性化支持 |
| 技术路线 | Spring Boot 3 + Vue 3 + MySQL 8.0 + Redis |
| 开发周期 | 2026年6月—9月 |
| 文档日期 | 2026年6月16日 |

### 二、今日工作内容

#### 2.1 技术选型确定

| 层级 | 技术选型 | 说明 |
|-|-|-|
| 后端框架 | Spring Boot 3.2.x | 主流Java开发框架 |
| JDK | JDK 17 | LTS长期支持版本 |
| ORM | MyBatis-Plus 3.5.x | 简化数据访问层开发 |
| 权限认证 | Spring Security + JWT | 用户认证与权限控制 |
| API文档 | Knife4j | 自动生成接口文档 |
| 前端框架 | Vue 3.4.x | 主流前端框架 |
| UI组件库 | Element Plus 2.7.x | 企业级后台UI组件库 |
| 状态管理 | Pinia | Vue 3官方推荐 |
| 构建工具 | Vite 5.x | 快速构建与热更新 |
| 关系数据库 | MySQL 8.0.x | 核心业务数据存储 |
| 缓存数据库 | Redis 7.x | 会话管理、数据缓存 |



#### 2.2 数据库设计（共12张表）

![](https://internal-api-drive-stream.feishu.cn/space/api/box/stream/download/authcode/?code=OGMwZDQxZmZlMjY0NjljOWM1NTYyM2RiMzFiZWM2ZjVfYzIxZDU4Yzc5YmFhMWQ1MzAyZTJhN2ZlOTFiNGUzZTRfSUQ6NzY1NDAxMzQ0NTg5NzEyODkwOF8xNzgyMjYxMjEyOjE3ODIyNjQ4MTJfVjM)

<figure view-type="Preview"><source href="https://internal-api-drive-stream.feishu.cn/space/api/box/stream/download/authcode/?code=MDhmNGExMjhmMzQ0OGU2YWE5ZmU1NmIxNjA3YTM4N2FfMzg5NGRmN2UyNmM5MzFmNWUzMzE0ZTZkN2ZmZDYwMmJfSUQ6NzY1NDAxMzY3NjU3OTU3MjcyNF8xNzgyMjYxMjEyOjE3ODIyNjQ4MTJfVjM" token="OnzWb1VbgoyO1xx9HEnccqnFnyc"/></figure>

#### 2.3 项目结构搭建



**后端结构（Spring Boot）**

```Plain Text
zhi-hui-qiao-backend/
├── src/main/java/com/zhihuiqiao/
│   ├── ZhiHuiQiaoApplication.java
│   ├── common/          # 公共模块（配置、常量、异常、工具类）
│   ├── modules/
│   │   ├── user/        # 用户模块（controller/service/mapper/entity/dto）
│   │   ├── research/    # 科研撮合模块
│   │   ├── resource/    # 资源流转模块
│   │   └── learning/    # 教学辅助模块
│   └── security/        # 认证授权（JWT）
├── src/main/resources/
│   ├── application.yml
│   └── mapper/
└── pom.xml
```



**前端结构（Vue 3）**

```Plain Text
zhi-hui-qiao-frontend/
├── src/
│   ├── api/             # 接口定义
│   ├── components/      # 公共组件
│   ├── layout/          # 布局组件
│   ├── router/          # 路由配置
│   ├── stores/          # Pinia状态管理
│   ├── utils/           # 工具函数（Axios封装）
│   └── views/           # 页面
│       ├── home/
│       ├── research/    # 科研撮合页面
│       ├── resource/    # 资源流转页面
│       ├── learning/    # 教学辅助页面
│       ├── user/        # 登录/注册/个人中心
│       └── admin/       # 管理后台
├── package.json
└── vite.config.js
```





#### 2.4 API接口设计（核心接口）



| 模块 | 接口 | 方法 | 路径 |
|-|-|-|-|
| 用户 | 注册 | POST | /api/user/register |
| 用户 | 登录 | POST | /api/user/login |
| 用户 | 获取信息 | GET | /api/user/info |
| 科研 | 发布项目 | POST | /api/research/project |
| 科研 | 项目列表 | GET | /api/research/project/list |
| 科研 | 申请加入 | POST | /api/research/project/apply |
| 科研 | 发布企业需求 | POST | /api/research/demand |
| 资源 | 发布闲置资源 | POST | /api/resource/publish |
| 资源 | 资源列表 | GET | /api/resource/list |
| 资源 | 预约资源 | POST | /api/resource/booking |
| 教学 | 发布学习资源 | POST | /api/learning/resource |
| 教学 | 学习资源列表 | GET | /api/learning/resource/list |
| 教学 | 记录学习进度 | POST | /api/learning/record |





### 三、明日工作计划（2026年6月17日）



| 序号 | 工作内容 | 产出 |
|-|-|-|
| 1 | 搭建后端环境，配置application.yml | 可运行的后端项目 |
| 2 | 执行SQL建表脚本 | 12张表创建完成 |
| 3 | 编写用户模块代码（实体类/Mapper/Service/Controller） | 用户模块完成 |
| 4 | 编写JWT认证拦截器 | 认证功能完成 |
| 5 | 搭建前端环境，配置路由 | 可运行的前端项目 |
| 6 | 实现登录注册页面 | 登录注册完成 |





**记录人**：罗智峰，杨博文

**日期**：2026年6月16日