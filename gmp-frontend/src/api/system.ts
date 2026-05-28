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
export function getDictPage(params: any) { return request.get('/system/dict/page', { params }) }
export function getDictByCode(code: string) { return request.get(`/system/dict/${code}`) }
export function createDict(data: any) { return request.post('/system/dict', data) }
export function updateDict(data: any) { return request.put('/system/dict', data) }
export function deleteDict(id: number) { return request.delete(`/system/dict/${id}`) }
export function refreshDictCache() { return request.post('/system/dict/refresh-cache') }

// ========== 系统配置 ==========
export function getConfigList() { return request.get('/system/config/list') }
export function getConfigListByCategory(category: string) { return request.get('/system/config/list-by-category', { params: { category } }) }
export function updateConfig(data: any) { return request.put('/system/config', data) }
export function updatePasswordPolicy(data: any) { return request.put('/system/config/password-policy', data) }

// ========== 操作日志 ==========
export function getLogPage(params: any) { return request.get('/system/log/page', { params }) }

// ========== 消息模板 ==========
export function getTemplateList(params?: any) { return request.get('/system/message-template/list', { params }) }
export function getTemplateById(id: number) { return request.get(`/system/message-template/${id}`) }
export function createTemplate(data: any) { return request.post('/system/message-template', data) }
export function updateTemplate(id: number, data: any) { return request.put(`/system/message-template/${id}`, data) }
export function deleteTemplate(id: number) { return request.delete(`/system/message-template/${id}`) }

// ========== 定时任务 ==========
export function getTaskList(params?: any) { return request.get('/system/scheduled-task/list', { params }) }
export function getTaskById(id: number) { return request.get(`/system/scheduled-task/${id}`) }
export function createTask(data: any) { return request.post('/system/scheduled-task', data) }
export function updateTask(id: number, data: any) { return request.put(`/system/scheduled-task/${id}`, data) }
export function deleteTask(id: number) { return request.delete(`/system/scheduled-task/${id}`) }
export function enableTask(id: number) { return request.put(`/system/scheduled-task/${id}/enable`) }
export function disableTask(id: number) { return request.put(`/system/scheduled-task/${id}/disable`) }
export function executeTaskNow(id: number) { return request.post(`/system/scheduled-task/${id}/execute`) }
export function getTaskLogs(taskId: number, params?: any) { return request.get(`/system/scheduled-task/${taskId}/logs`, { params }) }

// ========== 电子签名 ==========
export function saveSignature(data: any) { return request.post('/system/signature', data) }
export function getSignatureByTaskId(taskId: string) { return request.get(`/system/signature/task/${taskId}`) }
export function validateCron(expression: string) { return request.get('/system/scheduled-task/validate-cron', { params: { expression } }) }
