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
    // token 存在但已过期：强制登出并提示
    if (isTokenExpired(userStore.token)) {
      console.warn('[Auth] Token expired on navigation, forcing logout')
      userStore.logout()
      MessagePlugin.warning('登录已过期，请重新登录')
      next('/login')
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
