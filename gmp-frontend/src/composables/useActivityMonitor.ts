import { onMounted, onUnmounted } from 'vue'
import { ElMessageBox, ElNotification } from 'element-plus'
import { getTokenExpiresIn, removeToken, setTokenExpiresAt, setTokenExpiresIn } from '@/utils/auth'
import { refreshToken } from '@/api/auth'
import { getTokenExpireConfig } from '@/api/auth'
import router from '@/router'

/**
 * 用户不操作自动退出
 * 监听鼠标、键盘、滚动等事件，超时后跳转登录页
 * Token 过期前 60 秒弹出提醒，可点击续期
 */
export function useActivityMonitor() {
  const events = ['mousedown', 'mousemove', 'keydown', 'scroll', 'touchstart']
  let expireTimer: ReturnType<typeof setTimeout> | null = null
  let warnTimer: ReturnType<typeof setTimeout> | null = null

  function extendSession() {
    // 从服务端读取当前配置的过期时长并刷新token
    getTokenExpireConfig().then((res: any) => {
      const minutes = res.data?.expireMinutes || 30
      refreshToken(localStorage.getItem('GMP_REFRESH_TOKEN') || '').then((r: any) => {
        const { accessToken, refreshToken: newRefreshToken, expiresIn } = r.data
        localStorage.setItem('GMP_TOKEN', accessToken)
        if (newRefreshToken) localStorage.setItem('GMP_REFRESH_TOKEN', newRefreshToken)
        const seconds = expiresIn || minutes * 60
        setTokenExpiresAt(seconds)
        setTokenExpiresIn(seconds)
        clearAllTimers()
        startTimers()
        ElNotification.success({ title: '会话已续期', message: `Token 已延长 ${minutes} 分钟`, duration: 3000 })
      }).catch(() => {
        // 刷新失败，不做处理
      })
    }).catch(() => { /* ignore */ })
  }

  function showExpireWarning() {
    ElNotification({
      title: '登录即将过期',
      message: '您的登录状态将在 1 分钟后过期，请及时续期',
      type: 'warning',
      duration: 0,
      position: 'top-right',
      customClass: 'expire-warning-notify',
      onClose: () => { /* closed */ },
    } as any)
    ElMessageBox.confirm(
      '您的登录状态将在 1 分钟后过期，是否续期？',
      '登录即将过期',
      { confirmButtonText: '续期', cancelButtonText: '退出登录', type: 'warning', closeOnClickModal: false }
    ).then(() => {
      extendSession()
    }).catch(() => {
      removeToken()
      router.push('/login')
    })
  }

  function onExpired() {
    removeToken()
    router.push('/login')
  }

  function clearAllTimers() {
    if (expireTimer) { clearTimeout(expireTimer); expireTimer = null }
    if (warnTimer) { clearTimeout(warnTimer); warnTimer = null }
  }

  function startTimers() {
    const expiresIn = getTokenExpiresIn()
    const warnSeconds = Math.max(expiresIn - 60, 1)
    // 过期前60秒提醒
    warnTimer = setTimeout(showExpireWarning, warnSeconds * 1000)
    // 到期退出
    expireTimer = setTimeout(onExpired, expiresIn * 1000)
  }

  function onActivity() {
    clearAllTimers()
    startTimers()
  }

  onMounted(() => {
    startTimers()
    events.forEach(e => window.addEventListener(e, onActivity, { passive: true }))
  })

  onUnmounted(() => {
    clearAllTimers()
    events.forEach(e => window.removeEventListener(e, onActivity))
  })
}
