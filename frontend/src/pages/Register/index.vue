<template>
  <div class="register-container">
    <div class="register-card">
      <div class="logo-section">
        <h1>注册账号</h1>
        <p>创建您的账号，开始制作专业简历</p>
      </div>
      
      <t-form ref="formRef" :data="formData" :rules="rules" label-width="0" @submit="handleRegister">
        <t-form-item name="username">
          <t-input
            v-model="formData.username"
            placeholder="请输入用户名"
            size="large"
            clearable
          >
            <template #prefix-icon>
              <t-icon name="user" />
            </template>
          </t-input>
        </t-form-item>
        
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
            placeholder="请输入密码(6-20位)"
            size="large"
            clearable
          >
            <template #prefix-icon>
              <t-icon name="lock-on" />
            </template>
          </t-input>
        </t-form-item>
        
        <t-form-item name="confirmPassword">
          <t-input
            v-model="formData.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            clearable
            @keyup.enter="handleRegister"
          >
            <template #prefix-icon>
              <t-icon name="lock-on" />
            </template>
          </t-input>
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
            注册
          </t-button>
        </t-form-item>
      </t-form>
      
      <div class="footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const formData = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (val) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      if (val !== formData.value.password) {
        resolve({ result: false, message: '两次输入的密码不一致' })
      } else {
        resolve({ result: true })
      }
    })
  })
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', type: 'error' },
    { min: 3, max: 50, message: '用户名长度为3-50字符', type: 'error' }
  ],
  email: [
    { required: true, message: '请输入邮箱', type: 'error' },
    { email: true, message: '请输入正确的邮箱格式', type: 'error' }
  ],
  password: [
    { required: true, message: '请输入密码', type: 'error' },
    { min: 6, max: 20, message: '密码长度为6-20字符', type: 'error' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', type: 'error' },
    { validator: validateConfirmPassword, type: 'error' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate()
  if (valid !== true) return

  loading.value = true
  try {
    await userStore.register({
      username: formData.value.username,
      email: formData.value.email,
      password: formData.value.password
    })
    MessagePlugin.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    MessagePlugin.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
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
</style>
