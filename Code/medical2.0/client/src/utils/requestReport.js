/**
 * 报表专用HTTP客户端
 * 用于报表模块的APIYêu cầu，Sử dụng Authorization 头而非 X-Token
 */
import axios from 'axios'
import { getLocalToken, removeLocalToken } from '@/utils/auth'
import { handleSessionExpired } from '@/utils/sessionExpired'

// TạoaxiosVí dụ thực tế
const service = axios.create({
    baseURL: process.env.REPORT_SERVER_URL,
    timeout: 40000
})

// requestBộ chặn (interceptor)
service.interceptors.request.use(config => {
    if (getLocalToken()) {
        config.headers['Authorization'] = getLocalToken()
    }
    return config
}, error => {
    return Promise.reject(error)
})

// responseBộ chặn (interceptor)
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

        // Xử lý 401 Lỗi（Chưa được cấp quyền/Chưa đăng nhập）— và request.js 共用全局去重的会话过期处理
        if (error.response && error.response.status === 401) {
            removeLocalToken()
            handleSessionExpired()
        }

        return Promise.reject(errorData)
    })

export default service
