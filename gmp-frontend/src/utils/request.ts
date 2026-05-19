import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken, getRefreshToken, setToken, setRefreshToken, setTokenExpiresAt, setTokenExpiresIn, isTokenExpiringSoon } from './auth'
import router from '@/router'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

/** 正在进行的刷新请求（用于并发请求排队等待） */
let refreshPromise: Promise<string> | null = null

/** 静默刷新token，返回新的accessToken */
async function refreshAccessToken(): Promise<string> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) throw new Error('无RefreshToken')
  const res = await axios.post('/api/auth/refresh-token', { refreshToken })
  if (res.data.code !== 200) throw new Error('刷新失败')
  const { accessToken, refreshToken: newRefreshToken, expiresIn } = res.data.data
  setToken(accessToken)
  if (newRefreshToken) setRefreshToken(newRefreshToken)
  if (expiresIn) {
    setTokenExpiresAt(expiresIn)
    setTokenExpiresIn(expiresIn)
  }
  return accessToken
}

// 请求拦截器
service.interceptors.request.use(
  async (config: any) => {
    // 登录和刷新请求不处理，避免死循环
    const isAuthRequest = config.url?.includes('/auth/login') || config.url?.includes('/auth/refresh-token')
    if (!isAuthRequest && isTokenExpiringSoon(300)) {
      // 复用同一个刷新Promise，并发请求排队等待
      if (!refreshPromise) {
        refreshPromise = refreshAccessToken().finally(() => { refreshPromise = null })
      }
      try {
        const newToken = await refreshPromise
        config.headers.Authorization = `Bearer ${newToken}`
        return config
      } catch {
        removeToken()
        router.push('/login')
        return Promise.reject(new Error('Token已过期，请重新登录'))
      }
    }

    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (response.config.responseType === 'blob') return response

    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  async (error) => {
    const originalRequest = error.config
    // 401时尝试静默刷新一次，成功则重试原请求
    if (error.response?.status === 401 && !originalRequest._retried && getRefreshToken()) {
      originalRequest._retried = true
      try {
        if (!refreshPromise) {
          refreshPromise = refreshAccessToken().finally(() => { refreshPromise = null })
        }
        const newToken = await refreshPromise
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return service(originalRequest)
      } catch {
        // 刷新也失败，跳转登录
      }
    }
    if (error.response?.status === 401) {
      removeToken()
      router.push('/login')
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service
