import { createCrudApi } from '@/utils/apiFactory'

// 使用工厂函数生成标准CRUDGiao diện
const baseApi = createCrudApi('/stock/supplier')

// 导出所有接口（Giữ tương thích ngược）
export const getSupplierById = baseApi.getById
export const listSupplierPage = baseApi.listPage
export const listSupplierAll = baseApi.listAll
export const saveSupplier = baseApi.save
export const deleteSupplier = baseApi.delete
export const bulkInsertSupplier = baseApi.bulkInsert
export const bulkUpdateSupplier = baseApi.bulkUpdate
export const bulkDeleteSupplier = baseApi.bulkDelete
