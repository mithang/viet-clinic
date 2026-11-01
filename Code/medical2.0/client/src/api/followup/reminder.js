import { createCrudApi } from '@/utils/apiFactory'
import request from '@/utils/request'

const baseApi = createCrudApi('/followup/reminders')

export const getReminderById = baseApi.getById
export const listReminderPage = baseApi.listPage
export const listReminderAll = baseApi.listAll
export const saveReminder = baseApi.save
export const deleteReminder = baseApi.delete

export const listDueReminders = (params) => request({
  url: '/followup/reminders/due',
  method: 'get',
  params
})

export const acknowledgeReminder = (id) => request({
  url: `/followup/reminders/${id}/acknowledge`,
  method: 'post'
})

export const runDailyReminderJob = () => request({
  url: '/followup/reminders/runDailyJob',
  method: 'post'
})
