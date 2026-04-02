<template>
  <div class="login-container">
    <div class="login-card">
      <div class="logo-section">
        <h1>AI 简历制作工具</h1>
        <p>智能生成，轻松创建专业简历</p>
      </div>
      
      <t-form ref="formRef" :data="formData" :rules="rules" label-width="0" @submit="handleLogin">
        <t-form-item name="email">
          <t-input
            v-model="formData.email"
            placeholder="请输入邮箱"
            size="large"
            clearable
          >
            <template #prefix-icon>
              <t-icon name="mail" />
            </template>
          </t-input>
        </t-form-item>
        
        <t-form-item name="password">
          <t-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            clearable
            @keyup.enter="handleLogin"
          >
            <template #prefix-icon>
              <t-icon name="lock-on" />
            </template>
          </t-input>
        </t-form-item>
        
        <t-form-item>
          <div class="remember-row">
            <t-checkbox v-model="rememberMe">记住我</t-checkbox>
          </div>
        </t-form-item>

        <t-form-item>
          <t-button
            type="primary"
            size="large"
            theme="primary"
            :loading="loading"
            block
            native-type="submit"
          >
            登录
          </t-button>
        </t-form-item>
      </t-form>
      
      <div class="footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>

      <div class="divider">
        <span>或</span>
      </div>

      <t-button
        block
        size="large"
        theme="default"
        variant="outline"
        class="github-btn"
        :loading="githubLoading"
        @click="handleGithubLogin"
      >
        <template #icon>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" style="margin-right:4px">
            <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z"/>
          </svg>
        </template>
        使用 GitHub 登录
      </t-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getGithubClientId } from '@/api/auth'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const githubLoading = ref(false)

const formData = ref({
  email: '',
  password: ''
})

const rememberMe = ref(true)

const rules = {
  email: [
    { required: true, message: '请输入邮箱', type: 'error' },
    { email: true, message: '请输入正确的邮箱格式', type: 'error' }
  ],
  password: [
    { required: true, message: '请输入密码', type: 'error' },
    { min: 6, message: '密码长度不能少于6位', type: 'error' }
  ]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate()
  if (valid !== true) return

  loading.value = true
  try {
    await userStore.login(formData.value, rememberMe.value)
    MessagePlugin.success('登录成功')
    router.push('/')
  } catch (error) {
    MessagePlugin.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const handleGithubLogin = async () => {
  githubLoading.value = true
  try {
    const clientId = await getGithubClientId()
    const redirectUri = `${window.location.origin}/auth/github/callback`
    const scope = 'read:user user:email'
    window.location.href = `https://github.com/login/oauth/authorize?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&scope=${encodeURIComponent(scope)}`
  } catch (e) {
    MessagePlugin.error('GitHub 登录初始化失败')
    githubLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 12px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

.logo-section {
  text-align: center;
  margin-bottom: 40px;
}

.logo-section h1 {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.logo-section p {
  font-size: 14px;
  color: #666;
}

.remember-row {
  width: 100%;
  display: flex;
  align-items: center;
}

.footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
}

.footer a {
  color: var(--td-brand-color);
  text-decoration: none;
  margin-left: 4px;
}

.footer a:hover {
  text-decoration: underline;
}

.divider {
  display: flex;
  align-items: center;
  margin: 20px 0 16px;
  color: #ccc;
  font-size: 13px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #eee;
}

.divider span {
  padding: 0 12px;
}

.github-btn {
  color: #24292e;
  border-color: #d0d7de;
}

.github-btn:hover {
  background: #f6f8fa;
}
</style>
