import request from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/stock/supplierStorage')

// Chuẩn xuất dữ liệuCRUDGiao diện（Giữ tương thích ngược）
export const getSupplierStorageById = baseApi.getById
export const listSupplierStoragePage = baseApi.listPage
export const listSupplierStorageAll = baseApi.listAll
export const saveSupplierStorage = baseApi.save
export const deleteSupplierStorage = baseApi.delete
export const bulkInsertSupplierStorage = baseApi.bulkInsert
export const bulkUpdateSupplierStorage = baseApi.bulkUpdate
export const bulkDeleteSupplierStorage = baseApi.bulkDelete

// Giao diện tùy chỉnh
export const bulkUpdateSupplierStorage1 = (supplierStorages) =>
    request({
        url: '/stock/supplierStorage/cancel',
        method: 'post',
        data: supplierStorages
    })

export const bulkInSupplierStorage = (supplierStorages) =>
    request({
        url: '/stock/supplierStock/auditStorage',
        method: 'post',
        data: supplierStorages
    })

export const listByCode = (search) =>
    request({
        url: '/stock/supplierStorage/listByCode',
        method: 'get',
        params: search
    })
