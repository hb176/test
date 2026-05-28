import request from '@/utils/request'

/** 登录 */
export function login(data: { username: string; password: string; captchaKey?: string; captchaCode?: string }) {
  return request.post('/auth/login', data)
}
/** 登出 */
export function logout() {
  return request.post('/auth/logout')
}
/** 刷新Token */
export function refreshToken(refreshToken: string) {
  return request.post('/auth/refresh-token', { refreshToken })
}
/** 获取当前用户信息 */
export function getUserInfo() {
  return request.get('/auth/user-info')
}
/** 获取当前用户的菜单权限 */
export function getUserMenus() {
  return request.get('/auth/user-menus')
}
/** 获取Token过期配置 */
export function getTokenExpireConfig() {
  return request.get('/auth/token-expire-config')
}
/** 更新Token过期配置 */
export function updateTokenExpireConfig(expireMinutes: number) {
  return request.put('/auth/token-expire-config', { expireMinutes })
}
