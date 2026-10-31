import request from '@/utils/request'
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

// Khởi động bộ hẹn giờ
export const startTask = (scheduleJob) =>
  request({
    url: '/schedule/dynamicTask/startTask',
    method: 'post',
    data: scheduleJob
  })

// Khởi động bộ hẹn giờ
export const endTask = (scheduleJob) =>
  request({
    url: '/schedule/dynamicTask/endTask',
    method: 'post',
    data: scheduleJob
  })

// Khởi động lại bộ hẹn giờ
export const restartTask = (scheduleJob) =>
  request({
    url: '/schedule/dynamicTask/restartTask',
    method: 'post',
    data: scheduleJob
  })
