<template>
</template>
<script>
  import Cookies from 'js-cookie'
  export default {
    name: 'Base',
    data() {
      return {
        existDataView: false,
        reportView: null
      }
    },
    watch: {
      existDataView(val, oldValue) {
        if( val !== oldValue && val ) {
          this.reportView = () => import('./Report')
        }
      }
    },
    methods: {
      // Kiểm tra đệ quy xem dataviewer của báo cáo đã tải xong chưa
      wactchDataView () {
        if(window["vue-dataviewer"]) {
          this.existDataView = true
        } else {
          setTimeout(this.wactchDataView, 50)
        }
      }
    },
    mounted() {
      // Cookies.set('Authorization', sessionStorage.getItem('token'))
      // Tránh việc vì cùng tên miền (cổng có thể khác nhau) mà dùng chung một cookie 'Authorization', cần định nghĩa tách biệt cho cả hai
      Cookies.set('x-token', sessionStorage.getItem('token'))

      this.wactchDataView()
    }
  }
</script>
