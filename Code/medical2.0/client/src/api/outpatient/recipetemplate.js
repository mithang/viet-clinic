import request from '@/utils/request'
import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/outpatient/recipetemplate')

// Chuẩn xuất dữ liệuCRUDGiao diện (Giữ tương thích ngược)
export const getRecipetemplateById = baseApi.getById
export const listRecipetemplatePage = baseApi.listPage
export const listRecipetemplateAll = baseApi.listAll
export const saveRecipetemplate = baseApi.save
export const deleteRecipetemplate = baseApi.delete
export const bulkInsertRecipetemplate = baseApi.bulkInsert
export const bulkUpdateRecipetemplate = baseApi.bulkUpdate
export const bulkDeleteRecipetemplate = baseApi.bulkDelete

// Giao diện tùy chỉnh
export const allSave = (recipetemplate) =>
    request({
        url: '/outpatient/recipetemplate/allSave',
        method: 'post',
        data: recipetemplate
    })
