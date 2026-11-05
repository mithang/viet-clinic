import { createCrudApi } from '@/utils/apiFactory'

const baseApi = createCrudApi('/schedule/scheduleJob')

export const getScheduleJobById = baseApi.getById
export const listScheduleJobPage = baseApi.listPage
export const listScheduleJobAll = baseApi.listAll
export const saveScheduleJob = baseApi.save
export const deleteScheduleJob = baseApi.delete
export const bulkInsertScheduleJob = baseApi.bulkInsert
export const bulkUpdateScheduleJob = baseApi.bulkUpdate
export const bulkDeleteScheduleJob = baseApi.bulkDelete
