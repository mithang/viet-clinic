import { createCrudApi } from '@/utils/apiFactory'
import request from '@/utils/request'

const baseApi = createCrudApi('/sys/dictItem')

// Chuẩn xuất dữ liệuCRUDGiao diện（Giữ tương thích ngược）
export const getDictItemById = baseApi.getById
export const listDictItemPage = baseApi.listPage
export const listDictItemAll = baseApi.listAll
export const saveDictItem = baseApi.save
export const deleteDictItem = baseApi.delete

// Thêm mới：Theo loại từ điển code Lấy mục từ điển（推荐使用，替代硬编码雪花 ID）
export const listDictItemByCode = (code) =>
  request({ url: '/sys/dictItem/listByCode/' + code, method: 'get' })
