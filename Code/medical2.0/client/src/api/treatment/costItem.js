import request from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/treatment/costItem')

// Giao diện CRUD tiêu chuẩn
export const getCostItemById = baseApi.getById
export const listCostItemPage = baseApi.listPage
export const listCostItemAll = baseApi.listAll
export const saveCostItem = (data) =>
    request({
        url: '/treatment/costItem/saveDto',
        method: 'post',
        data
    })
export const deleteCostItem = baseApi.delete
export const bulkInsertCostItem = baseApi.bulkInsert
export const bulkUpdateCostItem = baseApi.bulkUpdate
export const bulkDeleteCostItem = baseApi.bulkDelete

// Giao diện tùy chỉnh
// Thông tin hạng mục khám chữa bệnh của đơn vị thuê cấp trên
export const listCostItemPageByParent = search =>
  request({
    url: "/treatment/costItem/listByInstitution",
    method: "post",
    data: search
  });

// Lấy toàn bộ hạng mục khám chữa bệnh bản bệnh viện
export const listCostItemPageByInstitutionAll = search =>
  request({
    url: "/hosdata/HosCollectData/getHosInstitutions",
    method: "post",
    data: search
  });

// Chọn hạng mục khám chữa bệnh của đơn vị thuê để đồng bộ về phòng khám
export const listCostItemPageByInstitution = search =>
  request({
    url: "/treatment/costItem/syncToClinic",
    method: "post",
    data: search
  });

// Chọn hạng mục khám chữa bệnh bản bệnh viện để đồng bộ
export const listCostItemPageByInstitutionSync = search =>
  request({
    url: "/hosdata/HosCollectData/HisInstitutionsToClinic",
    method: "post",
    data: search
  });
