import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/gen/genTableColumn')

// Chuẩn xuất dữ liệuCRUDGiao diện (Giữ tương thích ngược)
export const getGenTableColumnById = baseApi.getById
export const listGenTableColumnPage = baseApi.listPage
export const listGenTableColumnAll = baseApi.listAll
export const saveGenTableColumn = baseApi.save
export const deleteGenTableColumn = baseApi.delete
