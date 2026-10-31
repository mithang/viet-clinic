import request from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/stock/supplierOutbound')

// Chuẩn xuất dữ liệuCRUDGiao diện (Giữ tương thích ngược)
export const getSupplierOutbound = baseApi.getById
export const listSupplierOutbound = baseApi.listPage
export const saveSupplierOutbound = (data) =>
    request({
        url: '/stock/supplierOutbound/saveEvt',
        method: 'post',
        data
    })

// Giao diện tùy chỉnh
export const cancelSupplierOutbound = (id) =>
    request({
        url: '/stock/supplierOutbound/cancel/' + id,
        method: 'get'
    })

export const examineSupplierOutbound = (id) =>
    request({
        url: '/stock/supplierOutbound/examine/' + id,
        method: 'get'
    })
