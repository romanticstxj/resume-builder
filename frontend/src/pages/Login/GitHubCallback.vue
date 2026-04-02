<template>
  <div class="callback-container">
    <t-loading size="large" text="正在登录中..." />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { githubCallback } from '@/api/auth'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

onMounted(async () => {
  const code = route.query.code
  if (!code) {
    MessagePlugin.error('GitHub 授权失败')
    router.push('/login')
    return
  }
  try {
    const res = await githubCallback(code)
    // res 已被 axios 拦截器解包为 { token, userInfo }
    userStore.setToken(res.token)
    if (res.refreshToken) {
      const expiresAt = Date.now() + 7 * 24 * 3600 * 1000
      userStore.setRefreshToken(res.refreshToken, expiresAt)
    }
    userStore.setUserInfo(res.userInfo)
    MessagePlugin.success('登录成功')
    router.push('/')
  } catch (e) {
    MessagePlugin.error('GitHub 登录失败，请重试')
    router.push('/login')
  }
})
</script>

<style scoped>
.callback-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
