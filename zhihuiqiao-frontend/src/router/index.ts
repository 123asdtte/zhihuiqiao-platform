import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layout/AuthLayout.vue'),
    meta: { requiresAuth: false },
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
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: { title: '个人中心' }
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

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  document.title = `${to.meta.title || '智汇桥'} - 智汇桥`

  if (to.meta.requiresAuth === false) {
    next()
    return
  }

  if (!userStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  const requiredRoles = to.meta.roles as string[] | undefined
  if (requiredRoles && userStore.roleType && !requiredRoles.includes(userStore.roleType)) {
    next({ path: '/app/home' })
    return
  }

  next()
})

export default router
