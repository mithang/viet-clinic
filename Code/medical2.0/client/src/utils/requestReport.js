/**
 * HTTP client riêng cho báo cáo
 * Dùng cho yêu cầu API của module báo cáo, sử dụng header Authorization thay vì X-Token
 */
import axios from 'axios'
import { getLocalToken, removeLocalToken } from '@/utils/auth'
import { handleSessionExpired } from '@/utils/sessionExpired'

// Tạo đối tượng axios thực tế
const service = axios.create({
    baseURL: process.env.REPORT_SERVER_URL,
    timeout: 40000
})

// Bộ chặn (interceptor) request
service.interceptors.request.use(config => {
    if (getLocalToken()) {
        config.headers['Authorization'] = getLocalToken()
    }
    return config
}, error => {
    return Promise.reject(error)
})

// Bộ chặn (interceptor) response
service.interceptors.response.use(
    response => {
        return response.data
    },
    error => {
        let errorData = {
            type: 'error',
            code: error.response && error.response.status ? error.response.status : '1',
            msg: error.message ? error.message : 'Lỗi không xác định',
            data: error.response && error.response.data ? error.response.data : error
        }

        // Xử lý lỗi 401 (chưa được cấp quyền/chưa đăng nhập) — dùng chung cách xử lý hết phiên có chống lặp toàn cục với request.js
        if (error.response && error.response.status === 401) {
            removeLocalToken()
            handleSessionExpired()
        }

        return Promise.reject(errorData)
    })

export default service
