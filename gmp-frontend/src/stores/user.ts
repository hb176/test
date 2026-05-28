import { defineStore } from 'pinia'
import { login, logout, getUserInfo, getUserMenus } from '@/api/auth'
import { setToken, removeToken, getToken, setRefreshToken, setTokenExpiresAt, setTokenExpiresIn } from '@/utils/auth'
import router from '@/router'

/**
 * 用户状态管理 - Pinia
 * 存储当前登录用户信息、Token、权限列表
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userId: null as number | null,
    username: '',
    displayName: '',
    avatar: '',
    roles: [] as string[],
    permissions: [] as string[],
    deptId: null as number | null,
    deptName: '',
    menus: [] as any[],
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    hasPermission: (state) => (perm: string) => state.permissions.includes(perm),
  },

  actions: {
    /** 用户登录 */
    async loginAction(username: string, password: string, captchaKey: string, captchaCode: string) {
      const res = await login({ username, password, captchaKey, captchaCode })
      const { accessToken, refreshToken, expiresIn } = res.data
      setToken(accessToken)
      setRefreshToken(refreshToken)
      setTokenExpiresAt(expiresIn)
      setTokenExpiresIn(expiresIn)
      this.token = accessToken
      try {
        await this.fetchUserInfo()
      } catch (_) {
        // fetchUserInfo 失败不阻塞登录，用户信息为空但 token 有效
      }
      router.push('/dashboard')
    },

    /** 获取当前用户信息 */
    async fetchUserInfo() {
      const res = await getUserInfo()
      const user = res.data
      this.userId = user.userId
      this.username = user.username
      this.displayName = user.displayName
      this.roles = user.roles || []
      this.permissions = user.permissions || []
      this.deptId = user.deptId
      // 加载菜单权限
      try {
        const menuRes = await getUserMenus()
        this.menus = menuRes.data || []
      } catch (_) {
        this.menus = []
      }
    },

    /** 登出 */
    async logoutAction() {
      try { await logout() } catch (_) { /* ignore */ }
      removeToken()
      this.token = ''
      router.push('/login')
    },
  },
})
