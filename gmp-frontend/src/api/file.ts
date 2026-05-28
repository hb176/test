import request from '@/utils/request'

export function uploadFile(file: File, businessType?: string, businessId?: number) {
  const formData = new FormData()
  formData.append('file', file)
  if (businessType) formData.append('businessType', businessType)
  if (businessId) formData.append('businessId', String(businessId))
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 300000,
  })
}

export function getFilesByRecord(recordId: number) {
  return request.get(`/file/by-record/${recordId}`)
}

export function deleteFileRecord(id: number) {
  return request.delete(`/file/record/${id}`)
}

export function downloadFile(bucketName: string, objectName: string, fileName?: string) {
  const params = fileName ? `?fileName=${encodeURIComponent(fileName)}` : ''
  window.open(`/api/file/download/${bucketName}/${objectName}${params}`, '_blank')
}
