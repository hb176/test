/**
 * Token管理工具 - 存取JWT Token到localStorage
 */
const TOKEN_KEY = 'GMP_TOKEN'
const REFRESH_TOKEN_KEY = 'GMP_REFRESH_TOKEN'
const TOKEN_EXPIRES_KEY = 'GMP_TOKEN_EXPIRES_AT'
const TOKEN_EXPIRES_IN_KEY = 'GMP_TOKEN_EXPIRES_IN'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(TOKEN_EXPIRES_KEY)
  localStorage.removeItem(TOKEN_EXPIRES_IN_KEY)
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function setRefreshToken(token: string): void {
  localStorage.setItem(REFRESH_TOKEN_KEY, token)
}

/** 记录token过期时间戳（毫秒） */
export function setTokenExpiresAt(expiresIn: number): void {
  const expiresAt = Date.now() + expiresIn * 1000
  localStorage.setItem(TOKEN_EXPIRES_KEY, String(expiresAt))
}

/** 获取token过期时间戳（毫秒），无记录返回null */
export function getTokenExpiresAt(): number | null {
  const v = localStorage.getItem(TOKEN_EXPIRES_KEY)
  return v ? Number(v) : null
}

/** token是否在指定秒数内过期 */
export function isTokenExpiringSoon(withinSeconds: number): boolean {
  const expiresAt = getTokenExpiresAt()
  if (!expiresAt) return false
  return Date.now() > expiresAt - withinSeconds * 1000
}

/** 记录token有效时长（秒），用于不操作超时计时 */
export function setTokenExpiresIn(seconds: number): void {
  localStorage.setItem(TOKEN_EXPIRES_IN_KEY, String(seconds))
}

/** 获取token有效时长（秒），默认1800（30分钟） */
export function getTokenExpiresIn(): number {
  const v = localStorage.getItem(TOKEN_EXPIRES_IN_KEY)
  return v ? Number(v) : 1800
}
