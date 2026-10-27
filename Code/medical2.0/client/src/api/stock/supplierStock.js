import request from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/stock/supplierStock')

// Tiêu chuẩnCRUDGiao diện
export const getSupplierStockById = baseApi.getById
export const listSupplierStockPage = baseApi.listPage
export const listSupplierStockAll = baseApi.listAll
export const saveSupplierStock = baseApi.save
export const deleteSupplierStock = baseApi.delete
export const bulkInsertSupplierStock = baseApi.bulkInsert
export const bulkUpdateSupplierStock = baseApi.bulkUpdate
export const bulkDeleteSupplierStock = baseApi.bulkDelete

// Giao diện tùy chỉnh
export const getSupplierStockBySid = (sid) =>
    request({
        url: '/stock/supplierStock/stock/' + sid,
        method: 'get'
    })

export const saveSupplierStockList = (storageEvt) =>
    request({
        url: '/stock/supplierStock/inStorage',
        method: 'post',
        data: storageEvt
    })

export const updateStockStockList = (storageEvt) =>
    request({
        url: '/stock/supplierStock/updateStock',
        method: 'post',
        data: storageEvt
    })

export const saveSupplierStockListV1 = (storageEvt) =>
    request({
        url: '/stock/supplierStock/inStorageByCompany',
        method: 'post',
        data: storageEvt
    })
