import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/admin/role')

// Chuẩn xuất dữ liệuCRUDGiao diện（Giữ tương thích ngược）
export const getRoleById = baseApi.getById
export const listRolePage = baseApi.listPage
export const listRoleAll = baseApi.listAll
export const saveRole = baseApi.save
export const deleteRole = baseApi.delete
export const bulkInsertRole = baseApi.bulkInsert
export const bulkUpdateRole = baseApi.bulkUpdate
export const bulkDeleteRole = baseApi.bulkDelete
