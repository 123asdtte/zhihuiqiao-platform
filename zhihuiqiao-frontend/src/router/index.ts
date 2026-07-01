import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layout/AuthLayout.vue'),
    meta: { requiresAuth: false },
    redirect: '/login',
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/views/login/LoginView.vue'),
        meta: { title: '登录', requiresAuth: false }
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('@/views/register/RegisterView.vue'),
        meta: { title: '注册', requiresAuth: false }
      }
    ]
  },
  {
    path: '/app',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/app/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/HomeView.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'research/projects',
        name: 'ProjectList',
        component: () => import('@/views/research/ProjectList.vue'),
        meta: { title: '科研项目列表', roles: ['student', 'teacher', 'enterprise', 'admin'] }
      },
      {
        path: 'research/projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/research/ProjectDetail.vue'),
        meta: { title: '项目详情', roles: ['student', 'teacher', 'enterprise', 'admin'] }
      },
      {
        path: 'research/demands',
        name: 'DemandList',
        component: () => import('@/views/research/DemandList.vue'),
        meta: { title: '企业需求列表', roles: ['student', 'teacher', 'enterprise', 'admin'] }
      },
      {
        path: 'research/demands/:id',
        name: 'DemandDetail',
        component: () => import('@/views/research/DemandDetail.vue'),
        meta: { title: '企业需求详情', roles: ['student', 'teacher', 'enterprise', 'admin'] }
      },
      {
        path: 'research/applications',
        name: 'MyApplications',
        component: () => import('@/views/research/MyApplications.vue'),
        meta: { title: '项目申请', roles: ['student', 'admin'] }
      },
      {
        path: 'research/my-projects',
        name: 'MyProjects',
        component: () => import('@/views/research/MyProjects.vue'),
        meta: { title: '我的项目', roles: ['student', 'teacher', 'admin'] }
      },
      {
        path: 'research/project/publish',
        name: 'ProjectPublish',
        component: () => import('@/views/research/ProjectPublish.vue'),
        meta: { title: '发布科研项目', roles: ['student', 'teacher', 'admin'] }
      },
      {
        path: 'research/demand/publish',
        name: 'DemandPublish',
        component: () => import('@/views/research/DemandPublish.vue'),
        meta: { title: '发布企业需求', roles: ['enterprise', 'admin'] }
      },
      {
        path: 'resource/list',
        name: 'ResourceList',
        component: () => import('@/views/resource/ResourceList.vue'),
        meta: { title: '资源列表', roles: ['student', 'teacher', 'admin'] }
      },
      {
        path: 'resource/:id',
        name: 'ResourceDetail',
        component: () => import('@/views/resource/ResourceDetail.vue'),
        meta: { title: '资源详情', roles: ['student', 'teacher', 'admin'] }
      },
      {
        path: 'resource/publish',
        name: 'ResourcePublish',
        component: () => import('@/views/resource/ResourcePublish.vue'),
        meta: { title: '发布闲置资源', roles: ['student', 'teacher', 'admin'] }
      },

      {
        path: 'resource/transfer-requests',
        name: 'MyTransferRequests',
        component: () => import('@/views/resource/MyTransferRequests.vue'),
        meta: { title: '我的转让意向', roles: ['student', 'teacher', 'admin'] }
      },
      {
        path: 'resource/my-resources',
        name: 'MyResources',
        component: () => import('@/views/resource/MyResources.vue'),
        meta: { title: '我的资源', roles: ['student', 'teacher', 'admin'] }
      },
      {
        path: 'learning/resources',
        name: 'LearningResources',
        component: () => import('@/views/learning/ResourceList.vue'),
        meta: { title: '学习资源', roles: ['student', 'teacher', 'admin'] }
      },
      {
        path: 'learning/center',
        name: 'LearningCenter',
        component: () => import('@/views/learning/LearningCenter.vue'),
        meta: { title: '学习中心', roles: ['student', 'admin'] }
      },
      {
        path: 'learning/detail/:id',
        name: 'LearningResourceDetail',
        component: () => import('@/views/learning/ResourceDetail.vue'),
        meta: { title: '学习资源详情', roles: ['student', 'teacher', 'admin'] }
      },
      {
        path: 'learning/publish',
        name: 'LearningResourcePublish',
        component: () => import('@/views/learning/ResourcePublish.vue'),
        meta: { title: '发布学习资源', roles: ['teacher', 'admin'] }
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'search',
        name: 'GlobalSearch',
        component: () => import('@/views/search/GlobalSearch.vue'),
        meta: { title: '全局搜索' }
      },
      {
        path: 'notifications',
        name: 'NotificationCenter',
        component: () => import('@/views/notification/NotificationCenter.vue'),
        meta: { title: '消息通知' }
      },
      {
        path: 'admin/users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', roles: ['admin'] }
      },
      {
        path: 'admin/audit',
        name: 'AuditManage',
        component: () => import('@/views/admin/AuditManage.vue'),
        meta: { title: '内容审核', roles: ['admin'] }
      },
      {
        path: 'admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据看板', roles: ['admin'] }
      },
      {
        path: 'admin/operation-logs',
        name: 'OperationLog',
        component: () => import('@/views/admin/OperationLog.vue'),
        meta: { title: '操作日志', roles: ['admin'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  document.title = `${to.meta.title || '智汇桥'} - 智汇桥`

  // 无需认证的页面直接放行
  if (to.meta.requiresAuth === false) {
    return true
  }

  // 未登录用户重定向到登录页
  if (!userStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 校验角色权限
  const requiredRoles = to.meta.roles as string[] | undefined
  if (requiredRoles && userStore.roleType && !requiredRoles.includes(userStore.roleType)) {
    return { path: '/app/home' }
  }

  return true
})

export default router
