import request from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/gen/genTable')

// Chuẩn xuất dữ liệuCRUDGiao diện（Giữ tương thích ngược）
export const getGenTableById = baseApi.getById
export const listGenTablePage = baseApi.listPage
export const listGenTableAll = baseApi.listAll
export const saveGenTable = baseApi.save
export const deleteGenTable = baseApi.delete
export const bulkInsertGenTable = baseApi.bulkInsert
export const bulkUpdateGenTable = baseApi.bulkUpdate
export const bulkDeleteGenTable = baseApi.bulkDelete

// Giao diện tùy chỉnh
export const importGenTable = (genTable) =>
    request({
        url: '/gen/genTable/importJson',
        method: 'post',
        data: genTable
    })
