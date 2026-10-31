// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import '@/icons'
const Vue = window.Vue
import App from './App'
import router from './router'
import store from './store'
import moment from "moment"
import axios from 'axios'

// Element UI đã qua CDN tải tại index.html, mặc định size là mini
// Chuyển locale Element UI sang tiếng Việt (G-P1-01): bản CDN mặc định
// tiếng Trung nên mọi bảng trống hiện "không có dữ liệu", phân trang hiện "tổng 0 bản ghi".
// ELEMENT.locale có sẵn trên bản UMD của Element UI.
import elementViLocale from './utils/element-vi-locale'
if (typeof window !== 'undefined' && window.ELEMENT && window.ELEMENT.locale) {
  window.ELEMENT.locale(elementViLocale)
}

// Import liên kết ba cấp tỉnh/thành phố/quận/huyện
import Distpicker from 'v-distpicker'


Vue.prototype.$moment = moment
Vue.prototype.$axios = axios

// Toàn cục sử dụng liên kết ba cấp tỉnh/thành phố/quận/huyện
Vue.component('v-distpicker', Distpicker)

// Import thư viện chuyển Hán tự sang bính âm
import pinyin from 'js-pinyin'
pinyin.setOptions({checkPolyphone: false, charCase: 0})

// Biến chủ đề Element UI đã qua CDN đang tải, không cần import thêm
//import * as filters from './utils/filters'
import filters from './utils/filters'

import NumberInput from './components/NumberInput'
Vue.use(NumberInput)

import './utils/global'
import VueCropper from 'vue-cropper'
Vue.use(VueCropper)

Vue.config.productionTip = false

// Buộc đặt kích thước Element UI thành mini (đảm bảo hiệu lực sau khi tạo Vue instance)
Vue.prototype.$ELEMENT = { size: 'mini', zIndex: 3000 }

// Phương thức dự phòng toàn cục: một số component tùy chỉnh (như medicalOutpatientRecord) không extends BaseUI,
// cũng không import listViewMixin, nhưng trong .catch lại gọi this.outputError / this.showMessage,
// khi hết phiên sẽ ném "outputError is not a function". Tại đây cung cấp triển khai dự phòng an toàn,
// phương thức cùng tên của component hoặc kế thừa (BaseUI) có ưu tiên cao hơn, không ghi đè logic đã có.
Vue.mixin({
  methods: {
    outputError(error) {
      // Phiên hết hạn đã được request.js bộ chặn thống nhất nhắc và chuyển hướng, tránh lặp popup
      if (error && error.code === 20011) return
      console.error(error && error.response ? error.response : error)
      if (this.$message) {
        this.$message({
          showClose: true,
          message: 'Đã xảy ra lỗi. Vui lòng nhấn F12 để xem nhật ký trình duyệt.',
          type: 'error'
        })
      }
    },
    showMessage(msgData) {
      if (!msgData) return
      // Hết phiên do interceptor xử lý thống nhất, bỏ qua để tránh nhắc lại
      if (msgData.code === 20011) return
      const tip =
        msgData.type === 'error' && msgData.data
          ? '. Vui lòng nhấn F12 để xem nhật ký trình duyệt.'
          : ''
      if (this.$message) {
        this.$message({
          showClose: true,
          message: (msgData.msg || 'Thao tác thất bại') + tip,
          type: msgData.type || 'error'
        })
      }
    }
  }
})


/* Inject filter toàn cục */
Object.keys(filters).forEach(key => {
  Vue.filter(key, filters[key])
})

// Báo cáo
import i18n from '@/views/report/lang'
Vue.config.devtools = process.env.NODE_ENV !== 'production';

/* eslint-disable no-new */
let vm = new Vue({
  el: '#app',
  i18n,
  store,
  router,
  template: '<App/>',
  components: { App }
})

export default vm

// Khắc phục lỗi vòng ResizeObserver (ElementUI bắt buộc thêm), không cần thì có thể comment
const debounce = (func, wait) => {
  let timeout;
  return function (...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), wait);
  };
};
const resizeObserver = window.ResizeObserver;
window.ResizeObserver = class ResizeObserver extends resizeObserver {
  constructor(callback) {
    super(debounce(callback, 50));
  }
};
