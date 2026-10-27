import request from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/sys/dictType')

// Chuẩn xuất dữ liệuCRUDGiao diện（Giữ tương thích ngược）
export const getDictTypeById = baseApi.getById
export const listDictTypePage = baseApi.listPage
export const listDictTypeAll = baseApi.listAll
export const saveDictType = baseApi.save
export const deleteDictType = baseApi.delete
export const bulkInsertDictType = baseApi.bulkInsert
export const bulkUpdateDictType = baseApi.bulkUpdate
export const bulkDeleteDictType = baseApi.bulkDelete

// Giao diện tùy chỉnh
export const importDictType = (dictType) =>
    request({
        url: '/sys/dictType/importJson',
        method: 'post',
        data: dictType
    })
