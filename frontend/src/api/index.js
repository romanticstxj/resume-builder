import axios from 'axios'
import { MessagePlugin } from 'tdesign-vue-next'
import { useUserStore } from '@/stores/user'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 防止并发 401 触发多次 refresh
let isRefreshing = false
let pendingQueue = [] // [{ resolve, reject }]

function processQueue(error, token = null) {
  pendingQueue.forEach(({ resolve, reject }) => {
    error ? reject(error) : resolve(token)
  })
  pendingQueue = []
}

service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const responseType = response.config?.responseType
    if (responseType === 'blob' || responseType === 'text') {
      return response.data
    }
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    } else {
      MessagePlugin.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
  },
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      const userStore = useUserStore()
      const storedRefreshToken = userStore.refreshToken

      // 没有 refresh token，直接登出
      if (!storedRefreshToken) {
        userStore.logout()
        MessagePlugin.warning('登录已过期，请重新登录')
        window.location.href = '/login'
        return Promise.reject(error)
      }

      // 已在刷新中，把请求加入队列等待
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return service(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        // 直接用 axios 避免走拦截器循环
        const res = await axios.post(`/api/auth/refresh?refreshToken=${storedRefreshToken}`)
        const { token: newToken, refreshToken: newRefreshToken } = res.data.data

        userStore.setToken(newToken)
        if (newRefreshToken) {
          const newExpiresAt = Date.now() + 7 * 24 * 3600 * 1000
          userStore.setRefreshToken(newRefreshToken, newExpiresAt)
        }

        processQueue(null, newToken)
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return service(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        userStore.logout()
        MessagePlugin.warning('登录已过期，请重新登录')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    if (error.response?.status !== 401) {
      MessagePlugin.error(error.response?.data?.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default service
