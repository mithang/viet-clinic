<template>
  <div  style="text-align:left" >
    listvàformLớp thành phần cơ sở của cửa sổ，Xử lý thống nhất thông tin lỗi bất thường。
  </div>
</template>
<script>
import { getLocalDataPermisions } from '@/utils/auth'
export default {
  name: 'BaseUI',
  data() {
    return {
      currentUser: window.currentUser,  // 单前登录用户
      loadcount: 0,   // loading加载次数
      loading: false,  // 加载标志，用与显示加载遮罩

      isAlert: false   // 已经弹出alert对话标志
    }
  },
  methods: {
    setLoad() {
      this.loadcount++
      this.loading = true
    },
    resetLoad() {
      this.loadcount--
      if(this.loadcount <= 0) {
        this.loadcount = 0
        this.loading = false
      }
    },
    getAttrValue(entity, attr, javaType) {
      let pos = attr.indexOf('.')
      if(pos === -1) {
        if(javaType === "java.math.BigDecimal") {  // 金额类型，保留两位小数，Phân tách hàng nghìn
          return  (entity[attr] || 0).toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,')
        }
        return entity[attr]
      }
      return this.getAttrValue(entity[attr.substr(0, pos)], attr.substr(pos + 1), javaType)
    },
    // 构造更多的组合查询条件 Định dạng （ （ groupOne）  groupLogic （ groupTwo ））
    compositeCondition(moreParm, isFirst) {
      moreParm = moreParm || this.moreParm  // this.moreParm 是主界面的组合挑选条件
      let firstLogic = isFirst ? '' : 'AND'
      let conditions = []
      let groupOne = []
      let groupTwo = []

      if(moreParm.groupOne) {
        moreParm.groupOne.forEach((item, index) => {
          if(item.column && item.queryType && item.value && (!(item.value instanceof Array) || (item.value instanceof Array && item.value.length > 0))) {
            if(groupOne.length === 0) {
              groupOne.push({logic: '', columnName: item.column.name, queryType: item.queryType, value: item.value})
            } else {
              groupOne.push({logic: item.logic, columnName: item.column.name, queryType: item.queryType, value: item.value})
            }
          }
        })
      }
      if(moreParm.groupTwo) {
        moreParm.groupTwo.forEach((item, index) => {
          if(item.column && item.queryType && item.value && (!(item.value instanceof Array) || (item.value instanceof Array && item.value.length > 0))) {
            if(groupTwo.length === 0) {
              groupTwo.push({logic: '', columnName: item.column.name, queryType: item.queryType, value: item.value})
            } else {
              groupTwo.push({logic: item.logic, columnName: item.column.name, queryType: item.queryType, value: item.value})
            }
          }
        })
      }

      if(groupOne.length > 0 && groupTwo.length > 0) {
        conditions.push({logic: firstLogic, queryType: '('})

        conditions.push({logic: '', queryType: '('})
        conditions = conditions.concat(groupOne)
        conditions.push({logic: '', queryType: ')'})

        conditions.push({logic: moreParm.groupLogic, queryType: '('})
        conditions = conditions.concat(groupTwo)
        conditions.push({logic: '', queryType: ')'})

        conditions.push({logic: '', queryType: ')'})
      } else if(groupOne.length > 0) {
        conditions.push({logic: firstLogic, queryType: '('})
        conditions = conditions.concat(groupOne)
        conditions.push({logic: '', queryType: ')'})
      } else if(groupTwo.length > 0) {
        conditions.push({logic: firstLogic, queryType: '('})
        conditions = conditions.concat(groupTwo)
        conditions.push({logic: '', queryType: ')'})
      }

      return conditions
    },
    pushDataPermissions(params, routerId, mataId) {
      let items = [];
      let permissions = getLocalDataPermisions()
      let thePermissions = permissions.filter((permission)=>{return permission.routerId === routerId && permission.metaId === mataId });
      if(thePermissions.length <= 0){
        return params
      }

      items.push({logic: 'AND', queryType: '('})
      let isFirst = true
      for (let permission of thePermissions) {
        let moreParm = JSON.parse(permission.conditions)
        let conditions = this.compositeCondition(moreParm, isFirst)
        if(conditions.length > 0) {
          if(isFirst) {
            items = items.concat(conditions)
          } else {
            items.push({logic: 'OR', queryType: ''})
            items = items.concat(conditions)
          }

          isFirst = false
        }

      }
      items.push({logic: '', queryType: ')'})

      params.push.apply(params, items)

      return params
    },
    outputError(error) {
      this.resetLoad()
      // Phiên hết hạn đã được request.js bộ chặn thống nhất nhắc và chuyển hướng（全局去重），此处不再弹窗
      if (error && error.code === 20011) {
        return
      }
      console.error(error.response ? error.response : error.message)
      this.$alert('Đã xảy ra lỗi，Vui lòng nhấnF12Xem nhật ký trình duyệt。', 'Nhắc nhở', {
        type: 'error'
      })
    },
    showMessage(msgData) {
      // Phiên hết hạn đã được request.js bộ chặn thống nhất nhắc và chuyển hướng（全局去重），
      // 此处只做收尾（Đóng hộp thoại），不再弹窗、不再重复跳转
      if (msgData && msgData.code === 20011) {
        if (this.dialogProps) {
          this.dialogProps.visible = false
        }
        return
      }
      let tip = ''
      if(msgData.type === 'error' && msgData.data) {
        console.error(msgData.data)
        tip = '， Vui lòng nhấnF12Xem nhật ký trình duyệt。'
      }

      if (msgData.type === 'warning' || msgData.type === 'error') {
        if(!this.isAlert) {
          this.isAlert = true
          this.$alert(msgData.msg + tip, msgData.type === 'warning' ? 'Cảnh báo' : 'Lỗi', {
            type: msgData.type,
            callback: action => {
              if(this.loadcount <= 0) {
                this.isAlert = false
              }
            }
          })
        }
      } else {
        this.$message({
          showClose: true,
          message: msgData.msg + tip,
          type: msgData.type
        })
      }
    }
  },
  watch: {
    loadcount: function(val, oldVal) {
      if(val === 0) {
        this.isAlert = false
      }
    }
  }
}
</script>
