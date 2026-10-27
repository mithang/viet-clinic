import request from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/outpatient/remoteDiagnosisTreatment')

// Chuẩn xuất dữ liệuCRUDGiao diện（Giữ tương thích ngược）
export const getDiagnosisById = baseApi.getById
export const listDiagnosisPage = baseApi.listPage
export const listDiagnosisAll = baseApi.listAll
export const saveDiagnosis = baseApi.save
export const deleteDiagnosis = baseApi.delete
export const bulkInsertDiagnosis = baseApi.bulkInsert
export const bulkUpdateDiagnosis = baseApi.bulkUpdate
export const bulkDeleteDiagnosis = baseApi.bulkDelete

// Giao diện tùy chỉnh
export const getRegistrationId = (registrationId) =>
    request({
        url: '/outpatient/remoteDiagnosisTreatment/registrationId/' + registrationId,
        method: 'get'
    })

export const modifiedState = (patient) =>
    request({
        url: '/outpatient/remoteDiagnosisTreatment/modifiedState',
        method: 'post',
        data: patient
    })

export const chargeState = (patient) =>
    request({
        url: '/outpatient/remoteDiagnosisTreatment/chargeState',
        method: 'post',
        data: patient
    })
