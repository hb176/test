import request from '@/utils/request'

// ========== 用户管理 ==========
export function getUserPage(params: any) { return request.get('/system/user/page', { params }) }
export function addUser(data: any) { return request.post('/system/user', data) }
export function updateUser(id: number, data: any) { return request.put(`/system/user/${id}`, data) }
export function deleteUser(id: number) { return request.delete(`/system/user/${id}`) }
export function resetPassword(id: number, password: string) { return request.put(`/system/user/${id}/reset-password`, { password }) }

// ========== 角色管理 ==========
export function getRoleList() { return request.get('/system/role/list') }
export function getRoleById(id: number) { return request.get(`/system/role/${id}`) }
export function addRole(data: any) { return request.post('/system/role', data) }
export function updateRole(id: number, data: any) { return request.put(`/system/role/${id}`, data) }
export function deleteRole(id: number) { return request.delete(`/system/role/${id}`) }
export function getRoleMenus(roleId: number) { return request.get(`/system/menu/role/${roleId}`) }

// ========== 菜单管理 ==========
export function getMenuTree() { return request.get('/system/menu/tree') }
export function saveRoleMenus(roleId: number, menuIds: number[]) { return request.put(`/system/menu/role/${roleId}`, menuIds) }

// ========== 部门管理 ==========
export function getDeptTree() { return request.get('/system/dept/tree') }
export function getDeptChildren(parentId: number) { return request.get(`/system/dept/children/${parentId}`) }
export function getDeptById(id: number) { return request.get(`/system/dept/${id}`) }
export function addDept(data: any) { return request.post('/system/dept', data) }
export function updateDept(id: number, data: any) { return request.put(`/system/dept/${id}`, data) }
export function deleteDept(id: number) { return request.delete(`/system/dept/${id}`) }

// ========== 字典管理 ==========
export function getDictByCode(code: string) { return request.get(`/system/dict/${code}`) }
export function refreshDictCache() { return request.post('/system/dict/refresh-cache') }

// ========== 系统配置 ==========
export function getConfigList() { return request.get('/system/config/list') }
export function updateConfig(data: any) { return request.put('/system/config', data) }

// ========== 操作日志 ==========
export function getLogPage(params: any) { return request.get('/system/log/page', { params }) }
