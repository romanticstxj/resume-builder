import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi, logoutApi } from '@/api/auth'

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

// 优先读 localStorage，其次 sessionStorage
function getStoredItem(key) {
  return localStorage.getItem(key) || sessionStorage.getItem(key)
}

export const useUserStore = defineStore('user', () => {
  const token = ref(getStoredItem('token') || '')
  const refreshToken = ref(getStoredItem('refreshToken') || '')
  const refreshTokenExpiresAt = ref(Number(getStoredItem('refreshTokenExpiresAt') || '0'))
  const userInfo = ref(JSON.parse(getStoredItem('userInfo') || 'null'))

  const login = async (loginData, rememberMe = true) => {
    const res = await loginApi(loginData)
    const storage = rememberMe ? localStorage : sessionStorage
    // 切换存储时清除另一个存储中的旧数据
    const otherStorage = rememberMe ? sessionStorage : localStorage
    ;['token', 'refreshToken', 'refreshTokenExpiresAt', 'userInfo'].forEach(k => otherStorage.removeItem(k))

    token.value = res.token
    refreshToken.value = res.refreshToken
    const expiresAt = Date.now() + 7 * 24 * 3600 * 1000
    refreshTokenExpiresAt.value = expiresAt
    userInfo.value = res.userInfo
    storage.setItem('token', res.token)
    storage.setItem('refreshToken', res.refreshToken)
    storage.setItem('refreshTokenExpiresAt', String(expiresAt))
    storage.setItem('userInfo', JSON.stringify(res.userInfo))
  }

  const register = async (registerData) => {
    await registerApi(registerData)
  }

  const logout = async () => {
    try {
      if (refreshToken.value) await logoutApi(refreshToken.value)
    } catch (_) { /* 忽略网络错误，本地照常清除 */ }
    token.value = ''
    refreshToken.value = ''
    refreshTokenExpiresAt.value = 0
    userInfo.value = null
    ;['token', 'refreshToken', 'refreshTokenExpiresAt', 'userInfo'].forEach(k => {
      localStorage.removeItem(k)
      sessionStorage.removeItem(k)
    })
    window.location.href = '/login'
  }

  // 写入当前活跃的存储（localStorage 优先，否则 sessionStorage）
  function activeStorage() {
    return localStorage.getItem('token') ? localStorage : sessionStorage
  }

  const setToken = (t) => {
    token.value = t
    activeStorage().setItem('token', t)
  }

  const setRefreshToken = (t, expiresAt) => {
    refreshToken.value = t
    activeStorage().setItem('refreshToken', t)
    if (expiresAt) {
      refreshTokenExpiresAt.value = expiresAt
      activeStorage().setItem('refreshTokenExpiresAt', String(expiresAt))
    }
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    activeStorage().setItem('userInfo', JSON.stringify(info))
  }

  return {
    token,
    refreshToken,
    refreshTokenExpiresAt,
    userInfo,
    login,
    register,
    logout,
    setToken,
    setRefreshToken,
    setUserInfo
  }
})
