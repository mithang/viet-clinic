// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import '@/icons'
const Vue = window.Vue
import App from './App'
import router from './router'
import store from './store'
import moment from "moment"
import axios from 'axios'

// Element UI Đã qua CDN tại index.html 中加载，Mặc định size là mini
// Chuyển locale Element UI sang tiếng Việt (G-P1-01): bản CDN mặc định
// tiếng Trung nên mọi bảng trống hiện "暂无数据", phân trang hiện "共 0 条".
// ELEMENT.locale có sẵn trên bản UMD của Element UI.
import elementViLocale from './utils/element-vi-locale'
if (typeof window !== 'undefined' && window.ELEMENT && window.ELEMENT.locale) {
  window.ELEMENT.locale(elementViLocale)
}

//引入 Liên kết ba cấp tỉnh/thành phố/quận/huyện
import Distpicker from 'v-distpicker'


Vue.prototype.$moment = moment
Vue.prototype.$axios = axios

//全局使用省市区三级联动
Vue.component('v-distpicker', Distpicker)

//引入汉字转拼英码
import pinyin from 'js-pinyin'
pinyin.setOptions({checkPolyphone: false, charCase: 0})

// Element UI 主题变量已通过 CDN Đang tải，无需额外导入
//import * as filters from './utils/filters'
import filters from './utils/filters'

import NumberInput from './components/NumberInput'
Vue.use(NumberInput)

import './utils/global'
import VueCropper from 'vue-cropper'
Vue.use(VueCropper)

Vue.config.productionTip = false

// 强制设置 Element UI 尺寸为 mini（确保在 Vue 实例创建后生效）
Vue.prototype.$ELEMENT = { size: 'mini', zIndex: 3000 }

// 全局兜底方法：部分定制组件（như medicalOutpatientRecord）既没有 extends BaseUI，
// 也未引入 listViewMixin，却在请求 .catch 中调用 this.outputError / this.showMessage，
// 会话过期时便会抛 “outputError is not a function”。此处提供安全兜底实现，
// 组件自身或继承（BaseUI）的同名方法优先级更高，不会覆盖既有逻辑。
Vue.mixin({
  methods: {
    outputError(error) {
      // Phiên hết hạn đã được request.js bộ chặn thống nhất nhắc và chuyển hướng，避免重复弹窗
      if (error && error.code === 20011) return
      console.error(error && error.response ? error.response : error)
      if (this.$message) {
        this.$message({
          showClose: true,
          message: 'Đã xảy ra lỗi，Vui lòng nhấnF12Xem nhật ký trình duyệt。',
          type: 'error'
        })
      }
    },
    showMessage(msgData) {
      if (!msgData) return
      // 会话过期由拦截器统一处理，跳过以免重复提示
      if (msgData.code === 20011) return
      const tip =
        msgData.type === 'error' && msgData.data
          ? '， Vui lòng nhấnF12Xem nhật ký trình duyệt。'
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


/* 注入全局filter*/
Object.keys(filters).forEach(key => {
  Vue.filter(key, filters[key])
})

// 报表
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

// 解决 ResizeObserver 循环报错（ElementUI 必加）不需要可注释
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
