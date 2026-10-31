import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/gen/schemaTable')

// Chuẩn xuất dữ liệuCRUDGiao diện (Giữ tương thích ngược)
export const getSchemaTableById = baseApi.getById
export const listSchemaTablePage = baseApi.listPage
export const listSchemaTableAll = baseApi.listAll
export const saveSchemaTable = baseApi.save
export const deleteSchemaTable = baseApi.delete
