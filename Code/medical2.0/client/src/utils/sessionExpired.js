/**
 * Xử lý thống nhất khi hết phiên (dùng chung cho request.js / requestReport.js)
 * Giải quyết hai vấn đề:
 * 1. Khi trang có nhiều yêu cầu đồng thời cùng hết phiên, mỗi yêu cầu đều hiện một lần thông báo "Đăng nhập đã hết hạn" → thông báo đi qua cửa sổ thời gian toàn cục để chống lặp, chỉ hiện một lần
 * 2. Đồng thời kích hoạt nhiều lần router.replace('/login') → NavigationDuplicated là ngoại lệ chưa bắt, việc chuyển trang được chống lặp và nuốt ngoại lệ điều hướng
 */
import router from '@/router'

// Chống lặp thông báo: trong cửa sổ thời gian (mặc định 3 giây) chỉ hiện một lần
let lastMessageTime = 0
const MESSAGE_DEDUPE_WINDOW = 3000

// Chống lặp chuyển trang: đang chuyển trang thì không kích hoạt lại
let isRedirecting = false

export function handleSessionExpired() {
    const now = Date.now()
    if (now - lastMessageTime > MESSAGE_DEDUPE_WINDOW) {
        lastMessageTime = now
        ELEMENT.Message({
            message: 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại',
            type: 'warning',
            duration: 2000
        })
    }

    // Đã ở trang đăng nhập thì không chuyển trang lại
    if (router.currentRoute.path === '/login') return
    if (isRedirecting) return
    isRedirecting = true
    router
        .replace('/login')
        .catch(() => {}) // Bỏ qua ngoại lệ điều hướng như NavigationDuplicated
        .finally(() => {
            isRedirecting = false
        })
}
