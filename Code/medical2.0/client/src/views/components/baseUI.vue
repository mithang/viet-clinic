<template>
  <div  style="text-align:left" >
    Lớp thành phần cơ sở của cửa sổ list và form, xử lý thống nhất thông tin lỗi bất thường.
  </div>
</template>
<script>
import { getLocalDataPermisions, getLocalRouters } from '@/utils/auth'
export default {
  name: 'BaseUI',
  data() {
    return {
      currentUser: window.currentUser,  // Người dùng đăng nhập hiện tại
      loadcount: 0,   // Số lần loading
      loading: false,  // Cờ loading, dùng để hiển thị lớp phủ loading

      isAlert: false   // Cờ đã hiển thị hộp thoại alert
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
        if(javaType === "java.math.BigDecimal") {
          // Kiểu tiền tệ: 2 chữ số thập phân, phân tách hàng nghìn theo
          // chuẩn Việt Nam (vd 1234567.89 -> 1.234.567,89)
          const parts = (entity[attr] || 0).toFixed(2).split('.')
          const intPart = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, '.')
          return intPart + ',' + parts[1]
        }
        return entity[attr]
      }
      return this.getAttrValue(entity[attr.substr(0, pos)], attr.substr(pos + 1), javaType)
    },
    // Xây dựng điều kiện truy vấn tổ hợp thêm, Định dạng ( ( groupOne)groupLogic ( groupTwo ))
    compositeCondition(moreParm, isFirst) {
      moreParm = moreParm || this.moreParm  // this.moreParm là điều kiện lọc tổ hợp của giao diện chính
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
    // RouterId của route hiện tại, kèm fallback khi route không có meta
    // (vd mở trực tiếp bằng hash tới route chưa được đăng ký từ login:
    // $route.meta.routerId là undefined). Fallback tra session routers
    // theo path hiện tại hoặc $route.name (code). Trả null khi không tra
    // được — nơi gọi phải fail-closed (bỏ qua lệnh gọi quyền/cột) thay vì
    // gọi backend với tham số thiếu rồi hiện dialog lỗi 400.
    resolveRouterId() {
      const metaId = this.$route && this.$route.meta && this.$route.meta.routerId
      if (metaId) {
        return metaId
      }
      try {
        const routers = getLocalRouters() || []
        const path = (this.$route && this.$route.path || '').replace(/^\//, '')
        if (path) {
          const byUrl = routers.find((r) => r.url === path)
          if (byUrl && byUrl.id) {
            return byUrl.id
          }
        }
        if (this.$route && this.$route.name) {
          const byCode = routers.find((r) => r.code === this.$route.name)
          if (byCode && byCode.id) {
            return byCode.id
          }
        }
      } catch (e) {
        // Bỏ qua, trả null bên dưới
      }
      return null
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
      // Phiên hết hạn đã được request.js bộ chặn thống nhất nhắc và chuyển hướng (loại bỏ trùng lặp toàn cục), không hiển thị dialog nữa
      if (error && error.code === 20011) {
        return
      }
      console.error(error.response ? error.response : error.message)
      this.$alert('Đã xảy ra lỗi. Vui lòng nhấn F12 để xem nhật ký trình duyệt.', 'Nhắc nhở', {
        type: 'error'
      })
    },
    showMessage(msgData) {
      // Phiên hết hạn đã được request.js bộ chặn thống nhất nhắc và chuyển hướng (loại bỏ trùng lặp toàn cục),
      // chỉ làm khâu cuối (đóng hộp thoại), không hiển thị dialog nữa, không chuyển hướng lặp lại
      if (msgData && msgData.code === 20011) {
        if (this.dialogProps) {
          this.dialogProps.visible = false
        }
        return
      }
      let tip = ''
      if(msgData.type === 'error' && msgData.data) {
        console.error(msgData.data)
        tip = '. Vui lòng nhấn F12 để xem nhật ký trình duyệt.'
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
