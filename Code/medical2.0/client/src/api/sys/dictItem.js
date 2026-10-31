import { createCrudApi } from '@/utils/apiFactory'
import request from '@/utils/request'

const baseApi = createCrudApi('/sys/dictItem')

// Giao diện CRUD xuất dữ liệu chuẩn (giữ tương thích ngược)
export const getDictItemById = baseApi.getById
export const listDictItemPage = baseApi.listPage
export const listDictItemAll = baseApi.listAll
export const saveDictItem = baseApi.save
export const deleteDictItem = baseApi.delete

// Thêm mới: lấy danh sách mục từ điển theo mã loại từ điển (khuyến nghị dùng, thay cho hard-code ID snowflake)
export const listDictItemByCode = (code) =>
  request({ url: '/sys/dictItem/listByCode/' + code, method: 'get' })
