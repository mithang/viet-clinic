import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/registration/registration')

// Chuẩn xuất dữ liệuCRUDGiao diện（Giữ tương thích ngược）
export const getRegistrationById = baseApi.getById
export const listRegistrationPage = baseApi.listPage
export const listRegistrationAll = baseApi.listAll
export const saveRegistration = baseApi.save
export const deleteRegistration = baseApi.delete
export const bulkInsertRegistration = baseApi.bulkInsert
export const bulkUpdateRegistration = baseApi.bulkUpdate
export const bulkDeleteRegistration = baseApi.bulkDelete
