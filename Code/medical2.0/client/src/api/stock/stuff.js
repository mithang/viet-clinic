import request from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/stock/stuff')

// Giao diện CRUD tiêu chuẩn
export const getStuffById = baseApi.getById
export const listStuffPage = baseApi.listPage
export const listStuffAll = baseApi.listAll
export const saveStuff = baseApi.save
export const deleteStuff = baseApi.delete
export const bulkInsertStuff = baseApi.bulkInsert
export const bulkUpdateStuff = baseApi.bulkUpdate
export const bulkDeleteStuff = baseApi.bulkDelete

// Giao diện tùy chỉnh
export const listSyncStuffPage = search =>
  request({
    url: "/stock/stuff/listByCompany",
    method: "post",
    data: search
  });

// Lấy danh sách vật tư của đơn vị thuê cấp trên
export const listParentStuffPage = search =>
  request({
    url: "/stock/stuff/listByInstitution",
    method: "post",
    data: search
  });

// Lấy danh sách vật tư bản bệnh viện
export const listInstitutionStuffPage = search =>
  request({
    url: "/hosdata/HosCollectData/getHosMaterials",
    method: "post",
    data: search
  });

// Chọn vật tư của đơn vị thuê để đồng bộ về phòng khám
export const syncStuff = search =>
  request({
    url: "/stock/stuff/syncToClinic",
    method: "post",
    data: search
  });

// Chọn vật tư của phòng bệnh viện để đồng bộ
export const syncInstitutionStuff = search =>
  request({
    url: "/hosdata/HosCollectData/HisMaterialsToClinic",
    method: "post",
    data: search
  });

export const listDrug = search =>
  request({
    url: "/stock/drug/list",
    method: "post",
    data: search
  });

export const inventory = search =>
  request({
    url: "/stock/stuff/inventory",
    method: "post",
    data: search
  });

export const listStuffPageForMedical = search =>
  request({
    url: "/stock/stuff/listForMedical",
    method: "post",
    data: search
  });

export const updateAllIndate = stuff =>
  request({
    url: "/stock/stuff/updateAllIndate",
    method: "post",
    data: stuff
  });

export const updateAllInventory = stuff =>
  request({
    url: "/stock/stuff/updateAllInventory",
    method: "post",
    data: stuff
  });

export const uploadExcel = formData =>
  request({
    url: "/stock/stuff/uploadExcel",
    method: "post",
    data: formData
  });

// Danh sách phát thuốc bản bệnh viện
export const listInstitutionDispensingPage = search =>
  request({
    url: "/hosdata/HosDepartmentStorage/getHosDrugsStorage",
    method: "post",
    data: search
  });

// Đồng bộ phát thuốc bản bệnh viện
export const syncInstitutionDispensing = search =>
  request({
    url: "/hosdata/HosDepartmentStorage/auditHosDrugsStorage",
    method: "post",
    data: search
  });
