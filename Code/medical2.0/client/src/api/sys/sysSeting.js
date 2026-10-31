import request from '@/utils/request'

// Hiển thị lại ảnh theo id ảnh
export const getFileById = (id) =>
    request({
        url: '/sys/sysSeting/getFile/' + id,
        method: 'get'
    })
export const getFiled = (id) =>
    request({
        url:'/sys/sysSeting/getFiled/'+id,
        method:'get'
    })
export const getFirmList = (loginName, password) =>
    request({
        url: '/auth/getUserTenant',
        method: 'post',
        data: { loginName, password }
    })


export const listSysSetingAll = (search) =>
    request({
        url: '/sys/sysSeting/listAll',
        method: 'post',
        data: search
    })


export const saveSysSeting = (sysSeting) =>
    request({
        url: '/sys/sysSeting/saveWithFile',
        method: 'post',
        data: sysSeting
    })

export const deleteSysSeting = (sysSeting) =>
    request({
        url: '/sys/sysSeting/delete',
        method: 'post',
        data: sysSeting
    })

export const getPhotoById = (id) =>
    request({
        url:'/sys/sysSeting/getPhoto/'+id,
        method:'get'
    })

