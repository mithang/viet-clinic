// import Vue from 'vue'
// import VueI18n from 'vue-i18n'
import Cookies from 'js-cookie'
// import elementEnLocale from 'element-ui/lib/locale/lang/en' // element-ui lang
// import elementZhLocale from 'element-ui/lib/locale/lang/zh-CN'// element-ui lang
// import elementTWLocale from 'element-ui/lib/locale/lang/zh-TW'// element-ui lang

import enLocale from './en'
import zhLocale from './zh'
import twLocale from './tw'
import viLocale from './vi'
import elementViLocale from '@/utils/element-vi-locale'

import fuZh from 'fit2cloud-ui/src/locale/lang/zh-CN' // Đang tải nội dung fit2cloud

import fuEn from 'fit2cloud-ui/src/locale/lang/en_US' // Đang tải nội dung fit2cloud

const Vue = window.Vue
const VueI18n = window.VueI18n
Vue.use(VueI18n)

const messages = {
  en_US: {
    ...enLocale,
    ...ELEMENT.elementEnLocale,
    ...fuEn
  },
  zh_CN: {
    ...zhLocale,
    ...ELEMENT.elementZhLocale,
    ...fuZh
  },
  zh_TW: {
    ...twLocale,
    ...ELEMENT.elementTWLocale
  },
  // Tiếng Việt là ngôn ngữ mặc định của hệ thống.
  // fit2cloud-ui chưa có bản vi, nên trải fuEn trước để lấp các khoá còn thiếu;
  // viLocale trải sau cùng để bản dịch tiếng Việt luôn thắng.
  vi_VN: {
    ...fuEn,
    ...elementViLocale,
    ...viLocale,
    // Gộp sâu nhóm 'fu' để không mất khoá chỉ có trong fuEn
    fu: {
      ...((fuEn && fuEn.fu) || {}),
      ...((viLocale && viLocale.fu) || {})
    }
  }
}
export function getLanguage() {
  const chooseLanguage = Cookies.get('language')
  if (chooseLanguage) return chooseLanguage

  // if has not choose language
  const language = (navigator.language || navigator.browserLanguage || 'vi').toLowerCase()
  const locales = Object.keys(messages)
  for (const locale of locales) {
    // Chuẩn hoá vi_VN -> vi-vn để so khớp với navigator.language
    if (language.indexOf(locale.toLowerCase().replace('_', '-')) > -1) {
      return locale
    }
  }
  // Người dùng Việt Nam nhưng trình duyệt trả về biến thể khác (vi, vi-VN)
  if (language.indexOf('vi') === 0) {
    return 'vi_VN'
  }
  return 'vi_VN'
}
const i18n = new VueI18n({
  // set locale
  // options: en | zh | vi
  locale: getLanguage(),
  // set locale messages
  messages
})

export default i18n
