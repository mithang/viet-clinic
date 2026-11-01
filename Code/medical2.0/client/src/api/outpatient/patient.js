import { createCrudApi } from '@/utils/apiFactory'
import request from '@/utils/request'

const baseApi = createCrudApi('/outpatient/patient')

// G3: tra trùng bệnh nhân theo SĐT / CMND-CCCD cho form đăng ký.
// Chỉ đọc, không tạo, không merge — lễ tân chọn rõ ràng hoặc tạo mới.
export const lookupDuplicates = (data) => request({
  url: '/outpatient/patient/lookupDuplicates',
  method: 'post',
  data
})

export const getPatientById = baseApi.getById
export const listPatientPage = baseApi.listPage
export const listPatientAll = baseApi.listAll
export const savePatient = baseApi.save
export const deletePatient = baseApi.delete
export const bulkInsertPatient = baseApi.bulkInsert
export const bulkUpdatePatient = baseApi.bulkUpdate
export const bulkDeletePatient = baseApi.bulkDelete
