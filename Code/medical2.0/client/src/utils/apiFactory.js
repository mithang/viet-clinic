/**
 * Hàm factory API
 * Sinh giao diện CRUD chuẩn, loại bỏ code trùng lặp tầng API
 *
 * Cách sử dụng: 
 * // api/stock/supplier.js
 * import { createCrudApi } from '@/utils/apiFactory'
 *
 * const baseApi = createCrudApi('/stock/supplier')
 *
 * // Thêm interface tùy chỉnh
 * export const customMethod = (data) => request({ url: '/stock/supplier/custom', method: 'post', data })
 *
 * // Xuất tất cả interface
 * export default {
 *   ...baseApi,
 *   customMethod
 * }
 */
import request from './request'

/**
 * Tạo CRUD API chuẩn
 * @param {string} baseUrl - Đường dẫn cơ sở API
 * @returns {Object} Đối tượng chứa phương thức CRUD chuẩn
 */
export function createCrudApi(baseUrl) {
  return {
    /**
     * Lấy entity theo ID
     * @param {string} id - ID entity
     */
    getById: (id) => request({
      url: `${baseUrl}/${id}`,
      method: 'get'
    }),

    /**
     * Truy vấn phân trang
     * @param {Object} data - Tham số truy vấn
     */
    listPage: (data) => request({
      url: `${baseUrl}/list`,
      method: 'post',
      data
    }),

    /**
     * Truy vấn tất cả dữ liệu
     * @param {Object} data - Tham số truy vấn
     */
    listAll: (data) => request({
      url: `${baseUrl}/listAll`,
      method: 'post',
      data
    }),

    /**
 * Lưu entity (thêm mới hoặc cập nhật)
     * @param {Object} data - Dữ liệu entity
     */
    save: (data) => request({
      url: `${baseUrl}/save`,
      method: 'post',
      data
    }),

    /**
     * Xóa entity
     * @param {Object} data - Dữ liệu entity
     */
    delete: (data) => request({
      url: `${baseUrl}/delete`,
      method: 'post',
      data
    }),

    /**
     * Thêm hàng loạt
     * @param {Array} data - Mảng entity
     */
    bulkInsert: (data) => request({
      url: `${baseUrl}/bulkInsert`,
      method: 'post',
      data
    }),

    /**
     * Cập nhật hàng loạt
     * @param {Array} data - Mảng entity
     */
    bulkUpdate: (data) => request({
      url: `${baseUrl}/bulkUpdate`,
      method: 'post',
      data
    }),

    /**
     * Xóa hàng loạt
     * @param {Array} data - Mảng entity
     */
    bulkDelete: (data) => request({
      url: `${baseUrl}/bulkDelete`,
      method: 'post',
      data
    })
  }
}

/**
 * Tạo API truy vấn có phân trang
 * @param {string} baseUrl - Đường dẫn cơ sở API
 * @returns {Object} Đối tượng chứa phương thức truy vấn phân trang
 */
export function createPagedApi(baseUrl) {
  return {
    /**
     * Truy vấn phân trang
     * @param {Object} data - Tham số truy vấn
     */
    listPage: (data) => request({
      url: `${baseUrl}/list`,
      method: 'post',
      data
    }),

    /**
     * Truy vấn tất cả dữ liệu
     * @param {Object} data - Tham số truy vấn
     */
    listAll: (data) => request({
      url: `${baseUrl}/listAll`,
      method: 'post',
      data
    })
  }
}

/**
 * Tạo phương thức yêu cầu tùy chỉnh
 * @param {string} url - URL yêu cầu
 * @param {string} method - Phương thức yêu cầu
 * @returns {Function} Hàm yêu cầu
 */
export function createRequest(url, method = 'post') {
  return (data) => request({
    url,
    method,
    data
  })
}

export default {
  createCrudApi,
  createPagedApi,
  createRequest
}
