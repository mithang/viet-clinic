/**
 * Tiện ích bộ nhớ đệm thông tin người dùng
 * Tránh phân tích JSON trong sessionStorage nhiều lần
 */

let currentUserCache = null
let currentCompanyCache = null

/**
 * Lấy thông tin người dùng hiện tại (có bộ nhớ đệm)
 * @returns {Object} Thông tin người dùng
 */
export function getCurrentUser() {
    if (!currentUserCache) {
        const userStr = sessionStorage.getItem('currentUser')
        if (userStr) {
            try {
                currentUserCache = JSON.parse(userStr)
            } catch (e) {
                console.error('Phân tích thông tin người dùng thất bại:', e)
                currentUserCache = {}
            }
        }
    }
    return currentUserCache || {}
}

/**
 * Lấy thông tin đơn vị hiện tại (có bộ nhớ đệm)
 * @returns {Object} Thông tin đơn vị
 */
export function getCurrentCompany() {
    if (!currentCompanyCache) {
        const companyStr = sessionStorage.getItem('currentCompany')
        if (companyStr) {
            try {
                currentCompanyCache = JSON.parse(companyStr)
            } catch (e) {
                console.error('Phân tích thông tin đơn vị thất bại:', e)
                currentCompanyCache = {}
            }
        }
    }
    return currentCompanyCache || {}
}

/**
 * Lấy ID người dùng hiện tại
 * @returns {string} ID người dùng
 */
export function getCurrentUserId() {
    return getCurrentUser().id || ''
}

/**
 * Lấy ID đơn vị hiện tại
 * @returns {string} ID đơn vị
 */
export function getCurrentCompanyId() {
    return getCurrentCompany().id || ''
}

