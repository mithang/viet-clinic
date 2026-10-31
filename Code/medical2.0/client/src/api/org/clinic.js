import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/org/clinic')

// Chuẩn xuất dữ liệuCRUDGiao diện (Giữ tương thích ngược)
export const getClinicById = baseApi.getById
export const listClinicPage = baseApi.listPage
export const listClinicAll = baseApi.listAll
export const saveClinic = baseApi.save
export const deleteClinic = baseApi.delete
export const bulkInsertClinic = baseApi.bulkInsert
export const bulkUpdateClinic = baseApi.bulkUpdate
export const bulkDeleteClinic = baseApi.bulkDelete
