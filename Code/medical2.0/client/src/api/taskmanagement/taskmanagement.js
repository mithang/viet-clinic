import request from '@/utils/request'

// Thêm mới công việc
export const inserttask = (search) =>
    request({
        // url: '/taskmanagement/taskmanage/inserttask',
        url: '/taskmanagement/taskmanage/save',
        method: 'post',
        data: search
    })
    
export const storageTask = (search) =>
    request({
        url: '/taskmanagement/taskmanage/storage',
        method: 'post',
        data: search
    })

export const release = (task) =>
    request({
        url: '/taskmanagement/taskmanage/release',
        method: 'post',
        data: task
    })

export const deleteTask = (task) =>
    request({
        url: '/taskmanagement/taskmanage/delete',
        method: 'post',
        data: task
    })

    
// Lấy dữ liệu công việc
export const selecttasklist = (search) =>
    request({
        url: '/taskmanagement/taskmanage/selecttasklist',
        method: 'post',
        data: search
    })

    // Lấy dữ liệu người thực hiện
export const getusertree = (search) =>
request({
    url: '/taskmanagement/taskmanage/getusertree',
    method: 'post',
    data: search
})
// Lấy tình hình phản hồi
export const getfeedbacktable = (search) =>
request({
    url: '/taskmanagement/taskmanage/getfeedbacktable',
    method: 'post',
    data: search
})
// Duyệt công việc
export const updateaudit = (search) =>
request({
    url: '/taskmanagement/taskmanage/updateaudit',
    method: 'post',
    data: search
})