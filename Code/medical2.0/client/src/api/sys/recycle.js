// lys 2019/12/12  biên soạn thủ công
import request from '@/utils/request'

export const restoreAction = (action) =>
    request({
        url: '/sys/recycle/restor',
        method: 'post',
        data: action
    })
    
