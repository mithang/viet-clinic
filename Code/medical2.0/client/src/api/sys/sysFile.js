import request, { serviceLong } from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/sys/sysFile')

// Chuẩn xuất dữ liệuCRUDGiao diện（Giữ tương thích ngược）
export const listSysFilePage = baseApi.listPage
export const listSysFileAll = baseApi.listAll
export const saveSysFile = baseApi.save
export const deleteSysFile = baseApi.delete
export const bulkInsertSysFile = baseApi.bulkInsert
export const bulkUpdateSysFile = baseApi.bulkUpdate
export const bulkDeleteSysFile = baseApi.bulkDelete

// Giao diện tùy chỉnh（带特殊responseType）
export const getSysFileById = (id) =>
    serviceLong({
        url: '/sys/sysFile/' + id,
        method: 'get',
        responseType: 'blob'
    })

export const fileUploadById = (fileId) =>
    serviceLong({
        url: '/sys/fileContent/export/' + fileId,
        method: 'get',
        type: 'blob',
        responseType: 'arraybuffer'
    })
