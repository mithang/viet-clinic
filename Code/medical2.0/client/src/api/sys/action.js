import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/sys/action')

// Chuẩn xuất dữ liệuCRUDGiao diện（Giữ tương thích ngược）
export const getActionById = baseApi.getById
export const listActionPage = baseApi.listPage
export const listActionAll = baseApi.listAll
export const saveAction = baseApi.save
export const deleteAction = baseApi.delete
