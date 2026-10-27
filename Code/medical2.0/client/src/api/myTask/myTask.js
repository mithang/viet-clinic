import request from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/myTask/myTask')

// Chuẩn xuất dữ liệuCRUDGiao diện（Giữ tương thích ngược）
export const getMyTaskById = baseApi.getById
export const listMyTaskPage = baseApi.listPage
export const listMyTaskAll = baseApi.listAll
export const saveMyTask = baseApi.save

// Giao diện tùy chỉnh
export const updateMyTask = (myTask) =>
    request({
        url: '/myTask/myTask/update',
        method: 'post',
        data: myTask
    })
