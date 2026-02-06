<template>
  <t-layout class="layout">
    <t-aside class="sidebar">
      <div class="logo">简历制作工具</div>
      <t-menu
        :value="activeMenu"
        theme="light"
        @change="handleMenuChange"
      >
        <t-menu-item value="resumes">
          <template #icon><t-icon name="home" /></template>
          我的简历
        </t-menu-item>
        <t-menu-item value="templates">
          <template #icon><t-icon name="file-copy" /></template>
          模板市场
        </t-menu-item>
        <t-menu-item value="template-list">
          <template #icon><t-icon name="setting" /></template>
          模板管理
        </t-menu-item>
        <t-menu-item value="ai-generate">
          <template #icon><t-icon name="robot" /></template>
          AI 智能生成
        </t-menu-item>
      </t-menu>
    </t-aside>
    <t-layout class="main-layout">
      <t-header class="header">
        <div class="header-left">
          <h2>{{ pageTitle }}</h2>
        </div>
        <div class="header-right">
          <t-dropdown :options="userMenu" @click="handleUserMenuClick">
            <div class="user-info">
              <t-avatar size="32">{{ userInfo?.username?.charAt(0) || 'U' }}</t-avatar>
              <span>{{ userInfo?.username || '用户' }}</span>
              <t-icon name="chevron-down" />
            </div>
          </t-dropdown>
        </div>
      </t-header>
      <t-content class="content">
        <router-view />
      </t-content>
    </t-layout>
  </t-layout>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { MessagePlugin } from 'tdesign-vue-next'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/resumes')) return 'resumes'
  if (path.startsWith('/templates')) return 'templates'
  if (path.startsWith('/template')) return 'template-list'
  if (path.startsWith('/ai')) return 'ai-generate'
  return ''
})

const pageTitle = computed(() => {
  const routeMap = {
    resumes: '我的简历',
    templates: '模板市场',
    'template-list': '模板管理',
    'ai-generate': 'AI 智能生成'
  }
  return routeMap[activeMenu.value] || ''
})

const userMenu = [
  { content: '退出登录', value: 'logout' }
]

const handleMenuChange = (value) => {
  const routeMap = {
    resumes: '/resumes',
    templates: '/templates',
    'template-list': '/template/list',
    'ai-generate': '/ai/generate'
  }
  if (routeMap[value]) {
    router.push(routeMap[value])
  }
}

const handleUserMenuClick = ({ value }) => {
  if (value === 'logout') {
    userStore.logout()
    MessagePlugin.success('退出登录成功')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.sidebar {
  width: 240px;
  background: #fff;
  border-right: 1px solid var(--td-border-level-1-color);
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  color: var(--td-brand-color);
  border-bottom: 1px solid var(--td-border-level-1-color);
}

.main-layout {
  flex: 1;
}

.header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid var(--td-border-level-1-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-left h2 {
  font-size: 20px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: var(--td-bg-color-container-hover);
}

.content {
  padding: 24px;
  background-color: #f5f7fa;
  overflow-y: auto;
}
</style>
