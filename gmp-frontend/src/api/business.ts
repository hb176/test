import request from '@/utils/request'

// ========== QMS ==========
export function createDeviation(data: any) { return request.post('/qms/deviation', data) }
export function getDeviationPage(params: any) { return request.get('/qms/deviation/page', { params }) }
export function createCapa(data: any) { return request.post('/qms/capa', data) }
export function getCapaPage(params: any) { return request.get('/qms/capa/page', { params }) }
export function createChange(data: any) { return request.post('/qms/change', data) }
export function getAuditPage(params: any) { return request.get('/qms/audit/page', { params }) }

// ========== DMS ==========
export function getDocumentPage(params: any) { return request.get('/dms/document/page', { params }) }
export function createDocument(data: any) { return request.post('/dms/document', data) }
export function submitForReview(id: number) { return request.post(`/dms/document/${id}/submit-review`) }

// ========== TMS ==========
export function getCoursePage(params: any) { return request.get('/tms/course/page', { params }) }
export function getPlanPage(params: any) { return request.get('/tms/plan/page', { params }) }

// ========== QRS ==========
export function getApqrPage(params: any) { return request.get('/qrs/apqr/page', { params }) }
export function getQualityTrend(productId: number, year: number) { return request.get(`/qrs/trend/${productId}`, { params: { year } }) }
export function getDashboard() { return request.get('/qrs/dashboard') }
