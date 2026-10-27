/**
 * API工厂函数
 * 生成标准CRUDGiao diện，消除API层重复代码
 *
 * Cách sử dụng：
 * // api/stock/supplier.js
 * import { createCrudApi } from '@/utils/apiFactory'
 *
 * const baseApi = createCrudApi('/stock/supplier')
 *
 * // 添加自定义接口
 * export const customMethod = (data) => request({ url: '/stock/supplier/custom', method: 'post', data })
 *
 * // 导出所有接口
 * export default {
 *   ...baseApi,
 *   customMethod
 * }
 */
import request from './request'

/**
 * 创建标准CRUD API
 * @param {string} baseUrl - APIĐường dẫn cơ sở
 * @returns {Object} 包含标准CRUD方法的对象
 */
export function createCrudApi(baseUrl) {
  return {
    /**
     * TheoID获取实体
     * @param {string} id - Thực thểID
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
     * 保存实体（新增或更新）
     * @param {Object} data - Dữ liệu thực thể
     */
    save: (data) => request({
      url: `${baseUrl}/save`,
      method: 'post',
      data
    }),

    /**
     * Xóa thực thể
     * @param {Object} data - Dữ liệu thực thể
     */
    delete: (data) => request({
      url: `${baseUrl}/delete`,
      method: 'post',
      data
    }),

    /**
     * 批量新增
     * @param {Array} data - Mảng thực thể
     */
    bulkInsert: (data) => request({
      url: `${baseUrl}/bulkInsert`,
      method: 'post',
      data
    }),

    /**
     * 批量更新
     * @param {Array} data - Mảng thực thể
     */
    bulkUpdate: (data) => request({
      url: `${baseUrl}/bulkUpdate`,
      method: 'post',
      data
    }),

    /**
     * 批量删除
     * @param {Array} data - Mảng thực thể
     */
    bulkDelete: (data) => request({
      url: `${baseUrl}/bulkDelete`,
      method: 'post',
      data
    })
  }
}

/**
 * 创建带分页的查询API
 * @param {string} baseUrl - APIĐường dẫn cơ sở
 * @returns {Object} 包含分页查询方法的对象
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
 * 创建自定义请求方法
 * @param {string} url - Yêu cầuURL
 * @param {string} method - 请求方法
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
