# 科研项目删除功能实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现科研项目的安全删除功能：发布者可删除自己的项目，管理员可删除任何项目，删除时校验状态与关联申请记录，并提供「我的项目」管理页面和项目详情页删除入口。

**Architecture:** 后端在 `ResearchController` 暴露 `DELETE /api/research/project/{id}`，由 `ResearchProjectServiceImpl` 集中处理权限、状态、关联数据校验及级联删除；前端新增 `MyProjects.vue` 管理页面，并在 `ProjectDetail.vue` 增加发布者/管理员可见的删除按钮，通过二次确认后调用接口。

**Tech Stack:** Spring Boot 3 + MyBatis-Plus / Vue 3 + Element Plus + Pinia + Vue Router

---

## 文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/service/ResearchProjectService.java` | 修改 | 接口新增 `deleteProject` 方法 |
| `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/service/impl/ResearchProjectServiceImpl.java` | 修改 | 实现删除业务逻辑 |
| `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/controller/ResearchController.java` | 修改 | 新增 DELETE 接口 |
| `zhihuiqiao-frontend/src/api/research.ts` | 修改 | 新增 `deleteProject` API 封装 |
| `zhihuiqiao-frontend/src/views/research/MyProjects.vue` | 创建 | 我的项目管理列表 |
| `zhihuiqiao-frontend/src/views/research/ProjectDetail.vue` | 修改 | 增加删除按钮与二次确认 |
| `zhihuiqiao-frontend/src/router/index.ts` | 修改 | 注册 `/app/research/my-projects` 路由 |
| `zhihuiqiao-frontend/src/layout/MainLayout.vue` | 修改 | 科研撮合菜单增加「我的项目」入口 |

---

## Task 1: 后端 Service 接口定义

**Files:**
- Modify: `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/service/ResearchProjectService.java`

- [ ] **Step 1: 在接口中新增删除方法**

```java
    /**
     * 删除科研项目
     *
     * @param id          项目ID
     * @param currentUserId 当前登录用户ID
     * @param currentRole   当前登录用户角色
     * @return 删除是否成功
     */
    boolean deleteProject(Long id, Long currentUserId, String currentRole);
```

- [ ] **Step 2: 保存文件**

---

## Task 2: 后端 Service 删除逻辑实现

**Files:**
- Modify: `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/service/impl/ResearchProjectServiceImpl.java`

- [ ] **Step 1: 注入 ProjectApplicationService**

在类顶部添加依赖：

```java
import com.zhihuiqiao.service.ProjectApplicationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
```

通过构造器注入：

```java
private final ProjectApplicationService projectApplicationService;

public ResearchProjectServiceImpl(ProjectApplicationService projectApplicationService) {
    this.projectApplicationService = projectApplicationService;
}
```

或若使用 Lombok `@RequiredArgsConstructor`，确保类上有 `@Service` 与 `@RequiredArgsConstructor`，并将 `ProjectApplicationService` 声明为 `final` 字段：

```java
private final ProjectApplicationService projectApplicationService;
```

- [ ] **Step 2: 实现 deleteProject 方法**

```java
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProject(Long id, Long currentUserId, String currentRole) {
        // 1. 查询项目是否存在
        ResearchProject project = getById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        // 2. 权限校验：仅发布者本人或管理员可删除
        if (!"admin".equals(currentRole) && !project.getPublisherId().equals(currentUserId)) {
            throw new RuntimeException("无权删除该项目");
        }

        // 3. 状态校验：仅允许删除 draft / pending_audit / recruiting
        String status = project.getStatus();
        if (!StringUtils.hasText(status) ||
                !("draft".equals(status) || "pending_audit".equals(status) || "recruiting".equals(status))) {
            throw new RuntimeException("当前状态的项目不允许删除");
        }

        // 4. 关联数据校验：存在非 rejected 的申请记录时禁止删除
        LambdaQueryWrapper<ProjectApplication> applicationWrapper = new LambdaQueryWrapper<>();
        applicationWrapper.eq(ProjectApplication::getProjectId, id)
                .ne(ProjectApplication::getStatus, "rejected");
        long activeApplicationCount = projectApplicationService.count(applicationWrapper);
        if (activeApplicationCount > 0) {
            throw new RuntimeException("该项目已有申请记录，无法删除");
        }

        // 5. 级联删除该项目的所有申请记录（包含 rejected）
        LambdaQueryWrapper<ProjectApplication> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ProjectApplication::getProjectId, id);
        projectApplicationService.remove(deleteWrapper);

        // 6. 删除项目
        return removeById(id);
    }
```

- [ ] **Step 3: 保存文件**

---

## Task 3: 后端 Controller 删除接口

**Files:**
- Modify: `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/controller/ResearchController.java`

- [ ] **Step 1: 在科研项目相关方法后新增 DELETE 接口**

建议插入位置：在 `updateProjectStatus` 方法之后，项目申请相关代码之前。

```java
    @OperationLogAnnotation(module = "科研撮合", operation = "删除科研项目")
    @Operation(summary = "删除科研项目")
    @DeleteMapping("/project/{id}")
    public Result<Boolean> deleteProject(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        try {
            boolean success = researchProjectService.deleteProject(id, currentUserId, currentRole);
            return Result.success(success);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
```

- [ ] **Step 2: 保存文件**

- [ ] **Step 3: 编译验证后端**

Run: `mvn clean compile -DskipTests`
Expected: BUILD SUCCESS

---

## Task 4: 后端删除接口 API 测试

**Files:**
- Test helper: `test_api_helper.ps1`（已存在）

- [ ] **Step 1: 用教师账号发布一个测试项目**

Run:
```powershell
. .\test_api_helper.ps1
$teacherToken = Get-Token 'teacher01' '123456'
$body = '{"projectName":"delete-test-project","projectDescription":"for delete test","projectType":"基础研究","researchFields":"AI","maxMembers":5}'
$res = Invoke-Api POST '/api/research/project' $teacherToken $body
$res.content
```
Expected: 返回 `{ code: 200, data: <projectId> }`

- [ ] **Step 2: 学生提交申请，验证存在申请时无法删除**

Run:
```powershell
$studentToken = Get-Token 'student01' '123456'
$projectId = <上一步返回的 projectId>
$applyBody = "{\"projectId\":$projectId,\"applyReason\":\"apply for test\"}"
Invoke-Api POST '/api/research/application' $studentToken $applyBody
$delRes = Invoke-Api DELETE "/api/research/project/$projectId" $teacherToken
$delRes.content
```
Expected: `{ code: 500, message: "该项目已有申请记录，无法删除" }`

- [ ] **Step 3: 拒绝申请后再删除**

先查询该项目的申请ID：
```powershell
$apps = Invoke-Api GET "/api/research/project/$projectId/applications" $teacherToken
$apps.content.data[0].id
```

拒绝申请：
```powershell
$appId = <申请ID>
Invoke-Api PUT "/api/research/application/$appId/audit" $teacherToken "{status:'rejected'}"
```

再次删除：
```powershell
$delRes2 = Invoke-Api DELETE "/api/research/project/$projectId" $teacherToken
$delRes2.content
```
Expected: `{ code: 200, data: true }`

- [ ] **Step 4: 验证删除后查询返回 null**

```powershell
$getRes = Invoke-Api GET "/api/research/project/$projectId" $teacherToken
$getRes.content.data
```
Expected: `data` 为 null

- [ ] **Step 5: 验证越权删除**

用 student01 删除一个教师发布的项目：
```powershell
# 先创建一个教师项目
$pub = Invoke-Api POST '/api/research/project' $teacherToken '{"projectName":"unauthorized-delete-test","projectDescription":"x","projectType":"基础研究","maxMembers":3}'
$pid = $pub.content.data
$unauth = Invoke-Api DELETE "/api/research/project/$pid" $studentToken
$unauth.content
```
Expected: `{ code: 500, message: "无权删除该项目" }`

---

## Task 5: 前端 API 封装

**Files:**
- Modify: `zhihuiqiao-frontend/src/api/research.ts`

- [ ] **Step 1: 在「科研项目」代码段中新增 deleteProject 函数**

插入位置：在 `updateProjectStatus` 函数之后。

```typescript
/**
 * 删除科研项目
 * @param id 项目ID
 */
export function deleteProject(id: number | string) {
  return request({
    url: `/api/research/project/${id}`,
    method: 'delete'
  })
}
```

- [ ] **Step 2: 保存文件**

---

## Task 6: 新增「我的项目」管理页面

**Files:**
- Create: `zhihuiqiao-frontend/src/views/research/MyProjects.vue`

- [ ] **Step 1: 创建页面文件**

```vue
<template>
  <div class="my-projects-page zh-page">
    <div class="page-header-section">
      <div class="page-header-content">
        <h1 class="zh-page-title">我的项目</h1>
        <p class="zh-page-subtitle">管理您发布的科研项目</p>
      </div>
      <el-button type="primary" class="publish-btn" @click="router.push('/app/research/project/publish')">
        <el-icon><Plus /></el-icon>
        发布项目
      </el-button>
    </div>

    <div class="filter-bar">
      <el-select v-model="searchForm.status" placeholder="全部状态" clearable @change="handleSearch">
        <el-option label="草稿" value="draft" />
        <el-option label="待审核" value="pending_audit" />
        <el-option label="招募中" value="recruiting" />
        <el-option label="进行中" value="ongoing" />
        <el-option label="已完成" value="completed" />
        <el-option label="已关闭" value="closed" />
      </el-select>
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索项目名称"
        clearable
        style="width: 260px"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button text @click="handleReset">重置筛选</el-button>
    </div>

    <div v-loading="loading" class="project-table-wrapper">
      <el-table :data="projectList" style="width: 100%" border>
        <el-table-column prop="projectName" label="项目名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="projectType" label="项目类型" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="浏览量" width="90" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goToDetail(row.id)">查看</el-button>
            <el-button link type="danger" :disabled="!canDelete(row.status)" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="pagination.total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <div v-if="projectList.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无项目" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getProjectList, deleteProject } from '@/api/research'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const projectList = ref<any[]>([])
const searchForm = reactive({
  keyword: '',
  status: ''
})
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const deletableStatuses = ['draft', 'pending_audit', 'recruiting']

function statusText(status: string) {
  const map: Record<string, string> = {
    draft: '草稿',
    pending_audit: '待审核',
    recruiting: '招募中',
    ongoing: '进行中',
    completed: '已完成',
    closed: '已关闭'
  }
  return map[status] || status
}

function statusType(status: string) {
  const map: Record<string, any> = {
    draft: 'info',
    pending_audit: 'warning',
    recruiting: 'success',
    ongoing: 'primary',
    completed: '',
    closed: 'danger'
  }
  return map[status] || 'info'
}

function canDelete(status: string) {
  return deletableStatuses.includes(status)
}

async function loadProjectList() {
  loading.value = true
  try {
    const res: any = await getProjectList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    })
    // 前端过滤当前用户发布的项目（后端列表接口默认只返回 recruiting/ongoing，需要新增我的项目专用接口时按此扩展）
    const records = (res.data?.records || []).filter((item: any) => item.publisherId === userStore.userInfo?.id)
    projectList.value = records
    pagination.total = records.length
  } catch (error) {
    ElMessage.error('加载项目列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadProjectList()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = ''
  handleSearch()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadProjectList()
}

function handlePageChange(page: number) {
  pagination.pageNum = page
  loadProjectList()
}

function goToDetail(id: number) {
  router.push(`/app/research/projects/${id}`)
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(
      `确定要删除项目「${row.projectName}」吗？删除后将无法恢复，且相关申请记录也会被清除。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res: any = await deleteProject(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadProjectList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    if (error === 'cancel' || error?.message === 'cancel') return
    ElMessage.error(error?.response?.data?.message || '删除失败')
  }
}

onMounted(() => {
  loadProjectList()
})
</script>

<style scoped lang="scss">
.page-header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: var(--zh-space-6);
}

.publish-btn {
  background: var(--zh-accent) !important;
  border-color: var(--zh-accent) !important;
  color: #fff !important;
  font-weight: 600;

  &:hover {
    background: var(--zh-accent-light) !important;
    border-color: var(--zh-accent-light) !important;
  }
}

.filter-bar {
  display: flex;
  gap: var(--zh-space-3);
  align-items: center;
  margin-bottom: var(--zh-space-6);
}

.project-table-wrapper {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-5);
  box-shadow: var(--zh-shadow);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--zh-space-6);
}

.empty-state {
  padding: var(--zh-space-16) 0;
}
</style>
```

- [ ] **Step 2: 保存文件**

---

## Task 7: 为「我的项目」提供后端专用查询接口（可选但建议）

**说明：** 由于 `ProjectList.vue` 使用的 `/api/research/project/list` 默认只返回 `recruiting` 和 `ongoing` 状态的项目，且未按发布者过滤，新建「我的项目」页面时需要更精确的数据。

**方案选择：**
- **方案 A（推荐）：** 新增 `GET /api/research/project/my` 接口，返回当前用户发布的全部状态项目，支持 keyword/status 筛选与分页。
- **方案 B（简单）：** 前端直接调用现有 `/api/research/project/list?status=`，但仅能查询 `recruiting/ongoing`，且总数会不准确。

本计划采用方案 A。

**Files:**
- Modify: `zhihuiqiao-backend/src/main/java/com/zhihuiqiao/controller/ResearchController.java`
- Modify: `zhihuiqiao-frontend/src/api/research.ts`
- Modify: `zhihuiqiao-frontend/src/views/research/MyProjects.vue`

- [ ] **Step 1: 后端新增我的项目查询接口**

在 `ResearchController` 中 `listProjects` 方法之后插入：

```java
    @Operation(summary = "查询我发布的科研项目")
    @GetMapping("/project/my")
    public Result<Page<ResearchProject>> listMyProjects(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        Long currentUserId = getCurrentUserId();
        Page<ResearchProject> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ResearchProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResearchProject::getPublisherId, currentUserId)
                .orderByDesc(ResearchProject::getCreateTime);

        if (StringUtils.hasText(status)) {
            wrapper.eq(ResearchProject::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ResearchProject::getProjectName, keyword);
        }

        return Result.success(researchProjectService.page(page, wrapper));
    }
```

- [ ] **Step 2: 前端新增 myProjects API**

在 `research.ts` 中新增：

```typescript
/**
 * 查询我发布的科研项目
 * @param params 查询参数
 */
export function getMyProjects(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: string
}) {
  return request({
    url: '/api/research/project/my',
    method: 'get',
    params
  })
}
```

- [ ] **Step 3: 修改 MyProjects.vue 使用新接口**

将 `loadProjectList` 中的 `getProjectList` 调用替换为 `getMyProjects`，并移除前端 filter 逻辑：

```typescript
import { getMyProjects, deleteProject } from '@/api/research'

async function loadProjectList() {
  loading.value = true
  try {
    const res: any = await getMyProjects({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    })
    projectList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载项目列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}
```

- [ ] **Step 4: 保存所有修改文件**

---

## Task 8: 项目详情页增加删除入口

**Files:**
- Modify: `zhihuiqiao-frontend/src/views/research/ProjectDetail.vue`

- [ ] **Step 1: 引入 deleteProject API 和 ElMessageBox**

```typescript
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProjectDetail, applyProject, getProjectApplications, auditApplication, deleteProject } from '@/api/research'
```

- [ ] **Step 2: 新增计算属性判断当前用户是否可删除该项目**

```typescript
/**
 * 是否可删除项目
 */
const canDeleteProject = computed(() => {
  if (!project.value) return false
  if (userStore.isAdmin) return true
  return project.value.publisherId === userStore.userInfo?.id
})
```

- [ ] **Step 3: 在操作栏增加删除按钮**

在 `detail-actions-bar` 中的「查看申请」按钮之前插入：

```vue
            <el-button
              v-if="canDeleteProject"
              size="large"
              type="danger"
              plain
              @click="handleDeleteProject"
            >
              删除项目
            </el-button>
```

- [ ] **Step 4: 新增删除处理方法**

```typescript
/**
 * 删除项目
 */
async function handleDeleteProject() {
  if (!project.value) return
  try {
    await ElMessageBox.confirm(
      `确定要删除项目「${project.value.projectName}」吗？删除后将无法恢复，且相关申请记录也会被清除。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res: any = await deleteProject(project.value.id)
    if (res.code === 200) {
      ElMessage.success('项目删除成功')
      router.push('/app/research/projects')
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    if (error === 'cancel' || error?.message === 'cancel') return
    ElMessage.error(error?.response?.data?.message || '删除失败')
  }
}
```

- [ ] **Step 5: 保存文件**

---

## Task 9: 注册路由

**Files:**
- Modify: `zhihuiqiao-frontend/src/router/index.ts`

- [ ] **Step 1: 在科研项目路由中新增「我的项目」**

在 `/app/research/applications` 路由之后插入：

```typescript
      {
        path: 'research/my-projects',
        name: 'MyProjects',
        component: () => import('@/views/research/MyProjects.vue'),
        meta: { title: '我的项目', roles: ['teacher', 'enterprise', 'admin'] }
      },
```

- [ ] **Step 2: 保存文件**

---

## Task 10: 添加侧边栏菜单入口

**Files:**
- Modify: `zhihuiqiao-frontend/src/layout/MainLayout.vue`

- [ ] **Step 1: 在「科研撮合」子菜单中增加「我的项目」**

在「我的申请」之后插入：

```vue
            <el-menu-item v-if="userStore.isTeacher || userStore.isEnterprise || userStore.isAdmin" index="/app/research/my-projects">我的项目</el-menu-item>
```

或者将条件加在 `el-sub-menu` 内部仅对该菜单项生效。

- [ ] **Step 2: 保存文件**

---

## Task 11: 前端构建与浏览器验证

- [ ] **Step 1: 构建前端**

Run: `npm run build`
Expected: build succeeded

- [ ] **Step 2: 浏览器端到端验证（使用 browser_use 子代理）**

测试脚本：
1. 用 `teacher01 / 123456` 登录
2. 进入「科研撮合」→「我的项目」菜单，确认页面加载
3. 若列表为空，先发布一个测试项目
4. 在项目详情页确认「删除项目」按钮可见
5. 用学生账号申请加入该项目
6. 切回教师账号，进入我的项目/项目详情，点击删除
7. 确认提示「该项目已有申请记录，无法删除」
8. 拒绝学生申请后再次删除，确认删除成功
9. 管理员账号登录，验证可删除其他用户发布的项目

- [ ] **Step 3: 后端接口回归测试**

Run: `mvn clean package -DskipTests`
Expected: BUILD SUCCESS

---

## Self-Review Checklist

- [ ] **Spec coverage:** 所有设计文档中的要求（权限、状态、关联数据、级联删除、前端入口）均有对应任务。
- [ ] **Placeholder scan:** 无 TBD/TODO，所有代码块完整。
- [ ] **Type consistency:** `deleteProject` 方法名、参数类型、状态常量前后一致。
- [ ] **Scope check:** 本计划仅覆盖科研项目删除，不涉及企业需求/资源/学习资源。
