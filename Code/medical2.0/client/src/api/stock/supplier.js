import { createCrudApi } from '@/utils/apiFactory'

// Dùng hàm factory để sinh giao diện CRUD chuẩn
const baseApi = createCrudApi('/stock/supplier')

// Xuất tất cả API (giữ tương thích ngược)
export const getSupplierById = baseApi.getById
export const listSupplierPage = baseApi.listPage
export const listSupplierAll = baseApi.listAll
export const saveSupplier = baseApi.save
export const deleteSupplier = baseApi.delete
export const bulkInsertSupplier = baseApi.bulkInsert
export const bulkUpdateSupplier = baseApi.bulkUpdate
export const bulkDeleteSupplier = baseApi.bulkDelete
