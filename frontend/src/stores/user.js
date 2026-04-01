import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'

/**
 * 解析 JWT payload，不做签名验证（签名由后端负责）
 * 返回 payload 对象，解析失败返回 null
 */
function parseJwtPayload(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(base64))
  } catch {
    return null
  }
}

/**
 * 检查 token 是否已过期（本地时间判断，精度足够）
 */
export function isTokenExpired(token) {
  if (!token) return true
  const payload = parseJwtPayload(token)
  if (!payload?.exp) return true
  // exp 是秒级时间戳
  return Date.now() / 1000 >= payload.exp
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const login = async (loginData) => {
    const res = await loginApi(loginData)
    token.value = res.token
    userInfo.value = res.userInfo
    localStorage.setItem('token', res.token)
    localStorage.setItem('userInfo', JSON.stringify(res.userInfo))
  }

  const register = async (registerData) => {
    await registerApi(registerData)
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    login,
    register,
    logout
  }
})
