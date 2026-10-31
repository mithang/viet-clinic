/**
 * API client yêu cầu chuẩn
 * Cung cấp hai axios instance:
 * - service: Thời gian chờ mặc định 30 giây, dùng cho API request thông thường
 * - serviceLong: Thời gian chờ 5 phút, dùng cho thao tác hàng loạt, xuất dữ liệu, v.v.
 */
import axios from 'axios'
import { getLocalToken, removeLocalToken, clearLocalData } from '@/utils/auth'
import { handleSessionExpired } from '@/utils/sessionExpired'

// Tạo axios instance - thời gian chờ mặc định 30 giây
const service = axios.create({
    baseURL: process.env.BASE_API,
    timeout: 30000
})

// Tạo instance thời gian chờ dài - dùng cho thao tác hàng loạt, xuất dữ liệu, v.v., thời gian chờ 5 phút
const serviceLongInstance = axios.create({
    baseURL: process.env.BASE_API,
    timeout: 300000
})

// Request interceptor (bộ chặn) - dùng chung
const requestInterceptor = config => {
    if (getLocalToken()) {
        config.headers['X-Token'] = getLocalToken()
    }
    return config
}

const requestInterceptorError = error => {
    console.error(error)
    return Promise.reject(error)
}

// Response interceptor (bộ chặn) - dùng chung
const responseInterceptor = response => {
    if (response.headers["content-disposition"]) {
        let res = {
            data: response.data,
            headers: response.headers["content-disposition"]
        }
        return res
    } else {
        // Thống nhất chuyển code thành số, khắc phục vấn đề backend trả về chuỗi "100" mà frontend dùng === 100 để so sánh
        if (response.data && response.data.code !== undefined) {
            response.data.code = Number(response.data.code)
        }

        const data = response.data

        // Tự động xử lý hết phiên (code 20011): nhắc nhở và chuyển hướng đều được khử trùng toàn cục, tránh nhiều request đồng thời hiện nhiều thông báo
        if (data && data.code === 20011) {
            clearLocalData()
            handleSessionExpired()
            return Promise.reject(data)
        }

        // Cảnh báo và lỗi nghiệp vụ do component tự xử lý (qua showMessage/outputError)
        // Interceptor chỉ phụ trách xử lý hết phiên, tránh nhắc trùng với tầng component

        return data
    }
}

const responseInterceptorError = error => {
    console.error(error.response ? error.response : error)
    const respData = error.response && error.response.data ? error.response.data : null
    let errorData = {
        type: 'error',
        code: error.response && error.response.status ? error.response.status : '1',
        msg: 'Lỗi không xác định',
        data: respData || error
    }

    // Ưu tiên sử dụng thông tin lỗi nghiệp vụ do backend trả về
    if (respData) {
        if (respData.msg) {
            errorData.msg = respData.msg
        } else if (respData.message) {
            errorData.msg = respData.message
        }
    } else if (error.message) {
        errorData.msg = error.message
    }

    // Xử lý 401 Lỗi (Chưa được cấp quyền/Chưa đăng nhập)
    if (error.response && error.response.status === 401) {
        clearLocalData()
        handleSessionExpired()
    } else {
    // Lỗi mạng và các lỗi phi nghiệp vụ cũng tự động nhắc
        ELEMENT.Message({
            showClose: true,
            message: errorData.msg,
            type: 'error',
            duration: 5000
        })
    }

    return Promise.reject(errorData)
}

// Áp dụng interceptor cho cả hai instance
service.interceptors.request.use(requestInterceptor, requestInterceptorError)
service.interceptors.response.use(responseInterceptor, responseInterceptorError)

serviceLongInstance.interceptors.request.use(requestInterceptor, requestInterceptorError)
serviceLongInstance.interceptors.response.use(responseInterceptor, responseInterceptorError)

// Đồng nhất với service mặc định: interceptor đã hiện thông báo, bắt tại đây rejection và trả về error body, tránh che màn hình dev
export const serviceLong = (config) => serviceLongInstance(config).catch((errorData) => errorData)

// Interceptor đã thống nhất hiện thông báo lỗi; bắt tại đây rejection và trả nguyên error body,
// tránh Promise rejection chưa được bắt kích hoạt lỗi che màn hình của webpack-dev-server, dẫn đến crash trang
const safeRequest = (config) => service(config).catch((errorData) => errorData)

export default safeRequest
