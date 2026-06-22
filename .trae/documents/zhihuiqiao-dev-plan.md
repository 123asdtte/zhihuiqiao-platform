# 「智汇桥」完整开发计划

## 项目概述

**项目名称**：智汇桥——AI驱动的产学研用一体化智慧协同系统

**项目定位**：连接教师、学生、实验室与校企资源，打造师-生-机协同的智慧校园中心

**三大核心模块**：
1. 科研项目智能撮合
2. 校园闲置资源流转
3. 教学辅助个性化支持

**技术栈**：
- 前端：Vue 3.4 + Element Plus 2.7 + Pinia + Vite 5 + Vue Router
- 后端：Spring Boot 3.2 + JDK 17 + MyBatis-Plus 3.5 + Spring Security + JWT + Knife4j
- 数据库：MySQL 8.0 + Redis 7

**四类用户角色**：学生、教师、企业、管理员

**12张核心数据表**：sys_user、sys_role、sys_user_role、researcher_profile、research_project、project_application、enterprise_demand、idle_resource、resource_booking、resource_transfer_log、learning_resource、learning_record

---

## 开发步骤总览（共10步）

| 步骤 | 名称 | 涉及内容 |
|------|------|---------|
| 第1步 | 前端项目骨架搭建 | Vue 3 初始化、Element Plus、Pinia、Vue Router、Axios 封装、布局组件 |
| 第2步 | 后端项目骨架搭建 | Spring Boot 初始化、MySQL 建表、MyBatis-Plus 配置、JWT 认证 |
| 第3步 | 用户模块（前端+后端） | 登录/注册/个人中心页面 + 用户 CRUD 接口 + 前后端联调 |
| 第4步 | 科研撮合模块（后端） | 科研画像、项目发布/列表/详情、申请加入/审核、企业需求 |
| 第5步 | 科研撮合模块（前端） | 项目列表页、项目详情页、企业需求列表页 + 联调 |
| 第6步 | 资源流转模块（后端） | 闲置资源 CRUD、资源预约、流转记录 |
| 第7步 | 资源流转模块（前端） | 资源列表页、资源详情与预约页 + 联调 |
| 第8步 | 教学辅助模块（前后端） | 学习资源 CRUD、学习记录 + 学习资源列表页、学习中心页 |
| 第9步 | 首页与管理后台（前后端） | 首页数据看板 + 管理员用户管理、内容审核、数据看板 |
| 第10步 | 模拟数据与收尾 | 生成模拟数据、全流程测试、Bug 修复 |

---

## 第1步：前端项目骨架搭建（当前待执行）

### 目标
搭建完整的 Vue 3 前端项目骨架，包含所有基础设施和页面路由配置，为后续功能开发打好基础。

### 具体任务

#### 1.1 项目初始化
- 使用 Vite 5 创建 Vue 3 + TypeScript 项目（项目名：`zhihuiqiao-frontend`）
- 安装核心依赖：Element Plus 2.7、Pinia、Vue Router、Axios

#### 1.2 Axios 请求封装（`src/utils/request.ts`）
- 创建 Axios 实例，配置 baseURL
- 请求拦截器：自动添加 Token 到 Header
- 响应拦截器：统一错误处理、Token 过期（401）自动跳转登录页
- 使用 Element Plus 的 ElMessage 统一错误提示

#### 1.3 Pinia 状态管理（`src/stores/`）
- `user.ts`：用户状态（token、userInfo、login/logout actions）
- `app.ts`：应用状态（侧边栏折叠、面包屑等）

#### 1.4 Vue Router 路由配置（`src/router/index.ts`）
- 路由表定义，覆盖所有页面：
  - `/login` - 登录页
  - `/register` - 注册页
  - `/` - 首页
  - `/research/projects` - 科研项目列表
  - `/research/projects/:id` - 项目详情
  - `/research/demands` - 企业需求列表
  - `/resource/list` - 资源列表
  - `/resource/:id` - 资源详情与预约
  - `/learning/resources` - 学习资源列表
  - `/learning/center` - 学习中心
  - `/user/profile` - 个人中心
  - `/admin/users` - 用户管理（管理员）
  - `/admin/audit` - 内容审核（管理员）
  - `/admin/dashboard` - 数据看板（管理员）
- 路由守卫：未登录自动跳转 `/login`（登录/注册页除外）
- 基于角色的路由权限控制

#### 1.5 布局组件（`src/layout/`）
- `MainLayout.vue`：主布局，包含：
  - 顶部导航栏（平台 Logo + 名称「智汇桥」+ 菜单导航 + 用户头像/下拉菜单）
  - 侧边栏（根据角色显示不同菜单项）
  - 内容区域（`<router-view>`）
- `AuthLayout.vue`：认证布局（登录/注册页面专用，简洁居中布局）

#### 1.6 页面占位文件
为所有路由创建占位 `.vue` 文件，包含基本标题和布局，方便后续逐步填充：
- `views/login/LoginView.vue`
- `views/register/RegisterView.vue`
- `views/home/HomeView.vue`
- `views/research/ProjectList.vue`
- `views/research/ProjectDetail.vue`
- `views/research/DemandList.vue`
- `views/resource/ResourceList.vue`
- `views/resource/ResourceDetail.vue`
- `views/learning/ResourceList.vue`
- `views/learning/LearningCenter.vue`
- `views/user/ProfileView.vue`
- `views/admin/UserManage.vue`
- `views/admin/AuditManage.vue`
- `views/admin/Dashboard.vue`

#### 1.7 Element Plus 主题与全局配置
- 配置 Element Plus 按需引入
- 设置中文语言包
- 配置全局样式变量（主题色等）

### 验收标准
- `npm run dev` 能正常启动开发服务器
- 访问各路由路径能看到对应占位页面
- 导航栏菜单切换正常工作
- 路由守卫：未登录访问受保护页面自动跳转登录页
- 无 TypeScript 类型错误

### 产出目录结构
```
zhihuiqiao-frontend/
├── src/
│   ├── api/              # 接口定义（后续模块逐步添加）
│   ├── components/       # 公共组件
│   ├── layout/           # 布局组件
│   │   ├── MainLayout.vue
│   │   └── AuthLayout.vue
│   ├── router/           # 路由配置
│   │   └── index.ts
│   ├── stores/           # Pinia 状态管理
│   │   ├── user.ts
│   │   └── app.ts
│   ├── styles/           # 全局样式
│   │   └── index.scss
│   ├── utils/            # 工具函数
│   │   └── request.ts    # Axios 封装
│   ├── views/            # 页面
│   │   ├── login/
│   │   ├── register/
│   │   ├── home/
│   │   ├── research/
│   │   ├── resource/
│   │   ├── learning/
│   │   ├── user/
│   │   └── admin/
│   ├── App.vue
│   └── main.ts
├── index.html
├── vite.config.ts
├── tsconfig.json
└── package.json
```

---

## 第2步：后端项目骨架搭建

### 目标
搭建 Spring Boot 3 后端项目，完成数据库建表、MyBatis-Plus 配置、JWT 认证和统一响应格式。

### 具体任务
- Spring Boot 3.2 + JDK 17 项目初始化
- `application.yml` 配置（MySQL、Redis、MyBatis-Plus、Knife4j）
- 执行12张表的建表 SQL 脚本
- 初始化角色数据（学生/教师/企业/管理员）
- MyBatis-Plus 配置（分页插件、逻辑删除、自动填充）
- 统一响应类 `Result<T>`
- 全局异常处理器
- JWT 工具类 + 认证过滤器 + Security 配置
- CORS 跨域配置
- 用户实体类 + Mapper + Service + Controller
- Knife4j 接口文档配置

---

## 第3步：用户模块（前后端联调）

### 具体任务
- **前端**：登录页（表单+角色选择+校验）、注册页（完整表单校验）、个人主页
- **后端**：注册、登录、获取用户信息、更新用户信息、修改密码接口
- **联调**：登录注册全流程跑通、Token 存储与自动携带、Axios 拦截器验证

---

## 第4步：科研撮合模块（后端）

### 具体任务
- 科研画像 Entity/Mapper/Service/Controller
- 科研项目 CRUD（发布/列表/详情/状态更新）
- 项目申请流程（提交申请/列表查询/审核通过或拒绝）
- 企业需求 CRUD（发布/列表/匹配）
- MySQL ngram 全文检索

---

## 第5步：科研撮合模块（前端 + 联调）

### 具体任务
- 科研项目列表页（搜索+筛选+卡片列表+分页）
- 项目详情页（完整信息+申请加入弹窗）
- 企业需求列表页
- 前后端接口联调

---

## 第6步：资源流转模块（后端）

### 具体任务
- 闲置资源 CRUD
- 资源预约（提交/审批/归还）
- 流转记录管理

---

## 第7步：资源流转模块（前端 + 联调）

### 具体任务
- 资源列表页（分类筛选+卡片展示）
- 资源详情与预约页（日历选时段+预约提交）
- 前后端接口联调

---

## 第8步：教学辅助模块（前后端）

### 具体任务
- 后端：学习资源 CRUD、学习记录管理
- 前端：学习资源列表页、学习中心页（进度/收藏/继续学习）
- 前后端联调

---

## 第9步：首页与管理后台

### 具体任务
- 首页：轮播Banner + 数据看板（项目数/资源数/用户数）+ 最新动态
- 管理后台：用户管理（审核/禁用/角色分配）、内容审核、数据统计看板

---

## 第10步：模拟数据与收尾

### 具体任务
- 生成科研人员、设备、企业需求等模拟数据
- 全流程功能测试
- Bug 修复与 UI 优化
- 编写项目总结
