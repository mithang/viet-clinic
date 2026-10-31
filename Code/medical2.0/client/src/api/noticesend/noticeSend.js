import request from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/noticesend/noticeSend')

// Chuẩn xuất dữ liệuCRUDGiao diện (Giữ tương thích ngược)
export const getNoticeSendById = baseApi.getById
export const listNoticeSendPage = baseApi.listPage
export const listNoticeSendAll = baseApi.listAll
export const saveNoticeSend = baseApi.save
export const deleteNoticeSend = baseApi.delete
export const bulkInsertNoticeSend = baseApi.bulkInsert
export const bulkUpdateNoticeSend = baseApi.bulkUpdate
export const bulkDeleteNoticeSend = baseApi.bulkDelete

// Giao diện tùy chỉnh
export const getCompanyTree = (code) =>
    request({
        url: '/noticesend/noticeSend/tree/' + code,
        method: 'get'
    })
