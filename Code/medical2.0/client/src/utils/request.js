/**
 * Tiêu chuẩnAPI请求客户端
 * 提供两个axiosVí dụ thực tế：
 * - service: Mặc định30秒超时，用于常规APIYêu cầu
 * - serviceLong: 5分钟超时，Dùng cho thao tác hàng loạt、导出等
 */
import axios from 'axios'
import { getLocalToken, removeLocalToken, clearLocalData } from '@/utils/auth'
import { handleSessionExpired } from '@/utils/sessionExpired'

// Tạo axios Ví dụ thực tế - 默认超时30giây
const service = axios.create({
    baseURL: process.env.BASE_API,
    timeout: 30000
})

// 创建长超时实例 - Dùng cho thao tác hàng loạt、导出等，超时5Phút
const serviceLongInstance = axios.create({
    baseURL: process.env.BASE_API,
    timeout: 300000
})

// requestBộ chặn (interceptor) - Dùng chung
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

// responseBộ chặn (interceptor) - Dùng chung
const responseInterceptor = response => {
    if (response.headers["content-disposition"]) {
        let res = {
            data: response.data,
            headers: response.headers["content-disposition"]
        }
        return res
    } else {
        // 统一将 code 转为数字，解决后端返回字符串 "100" 而前端用 === 100 比较的问题
        if (response.data && response.data.code !== undefined) {
            response.data.code = Number(response.data.code)
        }

        const data = response.data

        // 自动处理会话过期（code 20011）：提示与跳转均全局去重，避免并发请求弹多个提示
        if (data && data.code === 20011) {
            clearLocalData()
            handleSessionExpired()
            return Promise.reject(data)
        }

        // 业务警告和错误由组件自行处理（Qua showMessage/outputError）
        // 拦截器只负责会话过期处理，避免与组件层重复提示

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

    // 优先使用后端返回的业务错误信息
    if (respData) {
        if (respData.msg) {
            errorData.msg = respData.msg
        } else if (respData.message) {
            errorData.msg = respData.message
        }
    } else if (error.message) {
        errorData.msg = error.message
    }

    // Xử lý 401 Lỗi（Chưa được cấp quyền/Chưa đăng nhập）
    if (error.response && error.response.status === 401) {
        clearLocalData()
        handleSessionExpired()
    } else {
        // 网络错误等非业务错误也自动提示
        ELEMENT.Message({
            showClose: true,
            message: errorData.msg,
            type: 'error',
            duration: 5000
        })
    }

    return Promise.reject(errorData)
}

// 应用拦截器到两个实例
service.interceptors.request.use(requestInterceptor, requestInterceptorError)
service.interceptors.response.use(responseInterceptor, responseInterceptorError)

serviceLongInstance.interceptors.request.use(requestInterceptor, requestInterceptorError)
serviceLongInstance.interceptors.response.use(responseInterceptor, responseInterceptorError)

// 与默认 service 一致：拦截器已弹提示，Bắt tại đây rejection 并返回错误体，Tránh dev 遮罩
export const serviceLong = (config) => serviceLongInstance(config).catch((errorData) => errorData)

// 拦截器已统一弹出错误提示；Bắt tại đây rejection 并原样返回错误体，
// 避免未捕获的 Promise rejection 触发 webpack-dev-server 错误遮罩、导致页面崩溃
const safeRequest = (config) => service(config).catch((errorData) => errorData)

export default safeRequest
