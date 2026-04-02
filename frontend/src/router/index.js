import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore, isTokenExpired } from '@/stores/user'
import { MessagePlugin } from 'tdesign-vue-next'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/Login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/auth/github/callback',
    name: 'GitHubCallback',
    component: () => import('@/pages/Login/GitHubCallback.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/pages/Register/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/resumes'
      },
      {
        path: 'resumes',
        name: 'Resumes',
        component: () => import('@/pages/Resume/List.vue')
      },
      {
        path: 'resumes/create',
        name: 'ResumeCreate',
        component: () => import('@/pages/Resume/Editor.vue')
      },
      {
        path: 'resumes/:id/edit',
        name: 'ResumeEdit',
        component: () => import('@/pages/Resume/Editor.vue')
      },
      {
        path: 'templates',
        name: 'Templates',
        component: () => import('@/pages/Template/Market.vue')
      },
      {
        path: 'template/list',
        name: 'TemplateList',
        component: () => import('@/pages/Template/List.vue')
      },
      {
        path: 'template/create',
        name: 'TemplateCreate',
        component: () => import('@/pages/Template/Edit.vue')
      },
      {
        path: 'template/edit/:id',
        name: 'TemplateEdit',
        component: () => import('@/pages/Template/Edit.vue')
      },
      {
        path: 'template/preview/:id',
        name: 'TemplatePreview',
        component: () => import('@/pages/Template/Preview.vue')
      },
      {
        path: 'ai/generate',
        name: 'AiGenerate',
        component: () => import('@/pages/AI/Generate.vue')
      },
      {
        path: 'parse-tasks',
        name: 'ParseTasks',
        component: () => import('@/pages/ParseTask/List.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth) {
    if (!userStore.token) {
      next('/login')
      return
    }
    // access token 过期时，检查 refresh token 是否还有效
    // 如果 refresh token 也没有或过期，才强制登出
    // 注意：实际刷新由 axios 拦截器在第一个 API 请求时自动处理
    // 路由守卫只负责"完全没有凭证"的情况
    if (isTokenExpired(userStore.token)) {
      const rtExpired = !userStore.refreshToken || userStore.refreshTokenExpiresAt < Date.now()
      if (rtExpired) {
        // refresh token 也过期，清除本地状态并跳登录
        console.warn('[Auth] Both tokens expired on navigation, forcing logout')
        userStore.token = ''
        userStore.refreshToken = ''
        userStore.userInfo = null
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userInfo')
        MessagePlugin.warning('登录已过期，请重新登录')
        next('/login')
        return
      }
      // access token 过期但 refresh token 有效，放行
      // 页面加载后第一个 API 请求会触发 axios 拦截器自动刷新
      next()
      return
    }
  }

  if ((to.path === '/login' || to.path === '/register') && userStore.token && !isTokenExpired(userStore.token)) {
    next('/')
    return
  }

  next()
})

export default router
