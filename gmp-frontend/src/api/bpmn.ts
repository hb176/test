import axios from 'axios'
import { getToken } from '@/utils/auth'

// BPMN 设计器 API 使用独立的请求实例
// 因为后端返回的 code 是字符串 "100"/"101"，不是数字 200
const bpmnRequest = axios.create({
  baseURL: '',
  timeout: 30000
})

// 请求拦截器：添加 token
bpmnRequest.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：处理 BPMN API 的特殊响应格式
bpmnRequest.interceptors.response.use(
  (response) => {
    const res = response.data
    // BPMN API 返回 code: "100" 表示成功
    if (res.code === '100' || res.code === 100) {
      return res
    }
    // 其他情况视为失败
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => Promise.reject(error)
)

// ==================== 模型 CRUD ====================

/** 保存 BPMN 模型（含自动部署） */
export function saveBpmnModel(data: any) {
  return bpmnRequest.post('/flow/bpmnDesigner/prod/api/saveBpmnModel', data)
}

/** 根据模型 ID 加载 BPMN XML */
export function getBpmnByModelId(modelId: string) {
  return bpmnRequest.get(`/flow/bpmnDesigner/prod/api/getBpmnByModelId/${modelId}`)
}

/** 根据模型 Key 加载 BPMN XML */
export function getBpmnByModelKey(modelKey: string) {
  return bpmnRequest.get(`/flow/bpmnDesigner/prod/api/getBpmnByModelKey/${modelKey}`)
}

/** 校验 BPMN XML 合法性 */
export function validateBpmnModel(data: any) {
  return bpmnRequest.post('/flow/bpmnDesigner/prod/api/validateBpmnModel', data)
}

// ==================== 发布/停用 ====================

/** 发布模型 */
export function publishBpmn(modelId: string) {
  return bpmnRequest.post(`/flow/bpmnDesigner/prod/api/publishBpmn/${modelId}`)
}

/** 停用模型 */
export function stopBpmn(modelId: string) {
  return bpmnRequest.post(`/flow/bpmnDesigner/prod/api/stopBpmn/${modelId}`)
}

// ==================== 监听器 ====================

/** 获取监听器列表（含参数） */
export function getListenersAndParams(data?: any) {
  return bpmnRequest.post('/flow/bpmnDesigner/prod/api/getListenersAndParams', data || {})
}

// ==================== 组织架构 ====================

/** 获取部门树 */
export function getOrgTree() {
  return bpmnRequest.get('/flow/bpmnDesigner/prod/api/getOrgTree')
}

/** 分页查询人员 */
export function getPersonalPagerModel(data: any) {
  return bpmnRequest.post('/flow/bpmnDesigner/prod/api/getPersonalPagerModel', data)
}

/** 分页查询角色 */
export function getRolePagerModel(data: any) {
  return bpmnRequest.post('/flow/bpmnDesigner/prod/api/getRolePagerModel', data)
}

/** 按角色查询人员 */
export function getPersonalsByRole(data: any) {
  return bpmnRequest.post('/flow/bpmnDesigner/prod/api/getPersonalsByRole', data)
}

// ==================== 表单 ====================

/** 分页查询自定义表单 */
export function getCustomFormPagerModel(data: any) {
  return bpmnRequest.post('/flow/bpmnDesigner/prod/api/getCustomFormPagerModel', data)
}

// ==================== 基础数据 ====================

/** 获取分类列表 */
export function getCategories(data?: any) {
  return bpmnRequest.post('/flow/bpmnDesigner/prod/api/getCategories', data || {})
}

/** 获取函数列表 */
export function getFunctionVariableVos() {
  return bpmnRequest.get('/flow/bpmnDesigner/prod/api/getFunctionVariableVos')
}
