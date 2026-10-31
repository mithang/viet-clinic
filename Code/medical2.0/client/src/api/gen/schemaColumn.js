import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/gen/schemaColumn')

// Chuẩn xuất dữ liệuCRUDGiao diện (Giữ tương thích ngược)
export const getSchemaColumnById = baseApi.getById
export const listSchemaColumnPage = baseApi.listPage
export const listSchemaColumnAll = baseApi.listAll
export const saveSchemaColumn = baseApi.save
export const deleteSchemaColumn = baseApi.delete
