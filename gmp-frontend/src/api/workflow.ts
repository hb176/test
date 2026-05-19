import request from '@/utils/request'

// ========== 流程定义 ==========
/** 流程定义列表 */
export function getDefinitionList(params: any) {
  return request.get('/workflow/definition/page', { params })
}
/** 部署新流程 */
export function deployDefinition(data: any) {
  return request.post('/workflow/definition/deploy', data)
}
/** 挂起/激活流程定义 */
export function suspendDefinition(id: number) {
  return request.put(`/workflow/definition/${id}/suspend`)
}
export function activateDefinition(id: number) {
  return request.put(`/workflow/definition/${id}/activate`)
}
export function deleteDefinition(id: string) {
  return request.delete(`/workflow/definition/${id}`)
}

// ========== 流程实例 ==========
/** 启动流程 */
export function startProcess(data: any) {
  return request.post('/workflow/instance/start', data)
}
/** 我的发起 */
export function getMyStarted(params: any) {
  return request.get('/workflow/instance/my-started', { params })
}
/** 流程实例详情（含审批轨迹、流程图进度） */
export function getInstanceDetail(id: number) {
  return request.get(`/workflow/instance/${id}`)
}
/** 撤回 */
export function withdrawProcess(id: number) {
  return request.put(`/workflow/instance/${id}/withdraw`)
}
/** 催办 */
export function urgeProcess(id: number) {
  return request.post(`/workflow/instance/${id}/urge`)
}
/** 终止 */
export function terminateProcess(id: number, reason: string) {
  return request.put(`/workflow/instance/${id}/terminate`, { reason })
}

// ========== 任务管理 ==========
/** 我的待办 */
export function getTaskTodo(params: any) {
  return request.get('/workflow/task/todo', { params })
}
/** 我的已办 */
export function getTaskDone(params: any) {
  return request.get('/workflow/task/done', { params })
}
/** 可认领任务 */
export function getClaimableTasks(params: any) {
  return request.get('/workflow/task/claimable', { params })
}
/** 待办数量 */
export function getTodoCount() {
  return request.get('/workflow/task/todo-count')
}
/** 认领任务 */
export function claimTask(taskId: string) {
  return request.post(`/workflow/task/${taskId}/claim`)
}
/** 办理任务（通过/驳回） */
export function completeTask(taskId: string, data: any) {
  return request.post(`/workflow/task/${taskId}/complete`, data)
}
/** 委派 */
export function delegateTask(taskId: string, delegateUserId: string, comment: string) {
  return request.post(`/workflow/task/${taskId}/delegate`, { delegateUserId, comment })
}
/** 转办 */
export function transferTask(taskId: string, transferUserId: string, comment: string) {
  return request.post(`/workflow/task/${taskId}/transfer`, { transferUserId, comment })
}
/** 加签 */
export function addSign(taskId: string, addAssignee: string, comment: string) {
  return request.post(`/workflow/task/${taskId}/add-sign`, { addAssignee, comment })
}

// ========== 审批轨迹 ==========
/** 获取流程审批轨迹 */
export function getProcessTrace(instanceId: number) {
  return request.get(`/workflow/history/trace/${instanceId}`)
}
