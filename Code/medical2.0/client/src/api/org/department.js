import request from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/org/department')

// Chuẩn xuất dữ liệuCRUDGiao diện (Giữ tương thích ngược)
export const getDepartmentById = baseApi.getById
export const listDepartmentPage = baseApi.listPage
export const listDepartmentAll = baseApi.listAll
export const saveDepartment = baseApi.save
export const deleteDepartment = baseApi.delete

// Giao diện tùy chỉnh
export const treeDepartment = (search) =>
    request({
        url: '/org/department/tree',
        method: 'post',
        data: search
    })
