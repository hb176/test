import request from '@/utils/request'

// ========== 表单定义 ==========
export function getFormDefinitionList(params: any) {
  return request.get('/form/definition/page', { params })
}
export function getFormDefinitionById(id: number) {
  return request.get(`/form/definition/${id}`)
}
export function getFormDefinitionByKey(formKey: string) {
  return request.get(`/form/definition/key/${formKey}`)
}
export function createFormDefinition(data: any) {
  return request.post('/form/definition', data)
}
export function updateFormDefinition(id: number, data: any) {
  return request.put(`/form/definition/${id}`, data)
}
export function publishFormDefinition(id: number) {
  return request.put(`/form/definition/${id}/publish`)
}
export function archiveFormDefinition(id: number) {
  return request.put(`/form/definition/${id}/archive`)
}

// ========== 表单数据 ==========
export function getFormDataPage(params: any) {
  return request.get('/form/data/page', { params })
}
export function getFormDataById(id: number) {
  return request.get(`/form/data/${id}`)
}
export function createFormData(data: any) {
  return request.post('/form/data', data)
}
export function updateFormData(id: number, data: any) {
  return request.put(`/form/data/${id}`, data)
}
export function submitFormData(id: number) {
  return request.put(`/form/data/${id}/submit`)
}
