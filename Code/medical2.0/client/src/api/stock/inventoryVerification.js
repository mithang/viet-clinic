import request from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/stock/inventoryVerification')

// Chuẩn xuất dữ liệuCRUDGiao diện（Giữ tương thích ngược）
export const getInventoryVerificationById = baseApi.getById
export const listInventoryVerificationPage = baseApi.listPage
export const listInventoryVerificationAll = baseApi.listAll
export const deleteInventoryVerification = baseApi.delete
export const bulkInsertInventoryVerification = baseApi.bulkInsert
export const bulkUpdateInventoryVerification = baseApi.bulkUpdate
export const bulkDeleteInventoryVerification = baseApi.bulkDelete

// Giao diện tùy chỉnh
export const saveInventoryVerification = (type, variety) =>
    request({
        url: '/stock/inventoryVerification/save',
        method: 'post',
        params: { type, variety }
    })

export const accomplishInventoryVerification = (inventoryVerifications) =>
    request({
        url: '/stock/inventoryVerification/accomplishInventoryVerification',
        method: 'post',
        data: inventoryVerifications
    })
