import request from '@/utils/request'

// ========== QMS ==========
export function createDeviation(data: any) { return request.post('/qms/deviation', data) }
export function getDeviationPage(params: any) { return request.get('/qms/deviation/page', { params }) }
export function createCapa(data: any) { return request.post('/qms/capa', data) }
export function getCapaPage(params: any) { return request.get('/qms/capa/page', { params }) }
export function createChange(data: any) { return request.post('/qms/change', data) }
export function getAuditPage(params: any) { return request.get('/qms/audit/page', { params }) }

// ========== DMS 文件管理 ==========
export function getDocumentPage(params: any) { return request.get('/dms/document/page', { params }) }
export function getDocument(id: number) { return request.get(`/dms/document/${id}`) }
export function createDocument(data: any) { return request.post('/dms/document', data) }
export function updateDocument(id: number, data: any) { return request.put(`/dms/document/${id}`, data) }
export function submitForReview(id: number) { return request.post(`/dms/document/${id}/submit-review`) }
export function obsoleteDocument(id: number, data?: any) { return request.put(`/dms/document/${id}/obsolete`, data || {}) }
export function reReviewDocument(id: number) { return request.post(`/dms/document/${id}/re-review`) }
export function getVersionHistory(id: number) { return request.get(`/dms/document/${id}/versions`) }

// ========== TMS 培训管理 ==========
export function getCoursePage(params: any) { return request.get('/tms/course/page', { params }) }
export function getCourse(id: number) { return request.get(`/tms/course/${id}`) }
export function createCourse(data: any) { return request.post('/tms/course', data) }
export function getPlanPage(params: any) { return request.get('/tms/plan/page', { params }) }
export function getPlan(id: number) { return request.get(`/tms/plan/${id}`) }
export function createTrainingPlan(data: any) { return request.post('/tms/plan', data) }
export function getTrainingRecords(params: any) { return request.get('/tms/record/page', { params }) }
export function createTrainingRecord(data: any) { return request.post('/tms/record', data) }
export function evaluateTraining(id: number, data?: any) { return request.post(`/tms/record/${id}/evaluate`, data || {}) }
export function getUserCertifications(userId: number) { return request.get(`/tms/certification/user/${userId}`) }
export function getExpiringCertifications(days?: number) { return request.get('/tms/certification/expiring', { params: { days: days || 30 } }) }
export function updateCourse(id: number, data: any) { return request.put(`/tms/course/${id}`, data) }
export function deleteCourse(id: number) { return request.delete(`/tms/course/${id}`) }
export function updatePlan(id: number, data: any) { return request.put(`/tms/plan/${id}`, data) }
export function deletePlan(id: number) { return request.delete(`/tms/plan/${id}`) }

// ========== TMS 培训分配 ==========
export function assignTraining(data: { courseId: number; userIds: number[]; userNames: string[] }) { return request.post('/tms/assignment', data) }
export function getAssignmentsByCourse(courseId: number) { return request.get(`/tms/assignment/course/${courseId}`) }
export function getAssignmentsByUser(userId: number) { return request.get(`/tms/assignment/user/${userId}`) }
export function completeAssignment(id: number, score?: number) { return request.put(`/tms/assignment/${id}/complete`, { score }) }
export function getCourseStats(courseId: number) { return request.get(`/tms/assignment/course/${courseId}/stats`) }
export function getExpiringAssignments(days?: number) { return request.get('/tms/assignment/expiring', { params: { days: days || 30 } }) }

// ========== 消息中心 ==========
export function getUnreadCount() { return request.get('/system/message/unread-count') }
export function getMessages(params: any) { return request.get('/system/message/list', { params }) }
export function markAsRead(id: number) { return request.put(`/system/message/${id}/read`) }
export function markAllAsRead() { return request.put('/system/message/read-all') }
export function deleteMessage(id: number) { return request.delete(`/system/message/${id}`) }

// ========== 定时任务 ==========
export function getScheduledTasks(params?: any) { return request.get('/system/scheduled-task/list', { params }) }
export function enableTask(id: number) { return request.put(`/system/scheduled-task/${id}/enable`) }
export function disableTask(id: number) { return request.put(`/system/scheduled-task/${id}/disable`) }
export function executeTaskNow(id: number) { return request.post(`/system/scheduled-task/${id}/execute`) }

// ========== QRS ==========
export function getApqrPage(params: any) { return request.get('/qrs/apqr/page', { params }) }
export function getQualityTrend(productId: number, year: number) { return request.get(`/qrs/trend/${productId}`, { params: { year } }) }
export function getDashboard() { return request.get('/qrs/dashboard') }
