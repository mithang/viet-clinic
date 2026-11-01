import request from '@/utils/request'

// Hàng chờ yêu cầu đặt lịch online (G2). Intake là endpoint công khai
// (không cần tài khoản); các endpoint còn lại cần phiên lễ tân và
// fail-closed với tài khoản 0 quyền ở backend.
export const submitBookingRequest = (data) => request({
  url: '/booking/requests/intake',
  method: 'post',
  data
})

export const listPendingBookingRequests = (params) => request({
  url: '/booking/requests/pending',
  method: 'get',
  params
})

export const listPriorBookingRequests = (params) => request({
  url: '/booking/requests/prior',
  method: 'get',
  params
})

export const confirmBookingRequest = (id) => request({
  url: `/booking/requests/${id}/confirm`,
  method: 'post'
})

export const rejectBookingRequest = (id, reason) => request({
  url: `/booking/requests/${id}/reject`,
  method: 'post',
  data: { reason }
})
