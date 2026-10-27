import request from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/outpatient/recipelDetail')

// Chuẩn xuất dữ liệuCRUDGiao diện（Giữ tương thích ngược）
export const getRecipelDetailById = baseApi.getById
export const listRecipelDetailPage = baseApi.listPage
export const listRecipelDetailAll = baseApi.listAll
export const saveRecipelDetail = baseApi.save
export const deleteRecipelDetail = baseApi.delete
export const bulkInsertRecipelDetail = baseApi.bulkInsert
export const bulkUpdateRecipelDetail = baseApi.bulkUpdate
export const bulkDeleteRecipelDetail = baseApi.bulkDelete

// Giao diện tùy chỉnh
export const getByRecipelInfoId = (recipelInfoId) =>
    request({
        url: '/outpatient/recipelDetail/getByRecipelInfoId/' + recipelInfoId,
        method: 'get'
    })
