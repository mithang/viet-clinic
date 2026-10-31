// import Cookies from 'js-cookie'

const TokenKey = 'token'
const CurrentUserKey = 'currentUser'
const RoutersKey = 'routers'
const SysSettingKey = 'sysSetting'
const DataPermisionsKey = 'dataPermisions'
const PersonalThemeKey = 'personalTheme'
const CurrentCompanyKey = 'currentCompany'
const CompanysKey='companys'
// Xóa toàn bộ dữ liệu cục bộ
export function clearLocalData() {
  sessionStorage.clear()
}

// Lấy token
export function getLocalToken() {
  return sessionStorage.getItem(TokenKey)
}
// Lưu token
export function setLocalToken(token) {
  sessionStorage.setItem(TokenKey, token)
}
// Xóa token
export function removeLocalToken() {
  sessionStorage.removeItem(TokenKey)
}

// Lấy người dùng đã đăng nhập
export function getLocalCurrentUser() {
  return JSON.parse(sessionStorage.getItem(CurrentUserKey))
}

// Lưu người dùng đã đăng nhập
export function setLocalCurrentUser(currentUser) {
  sessionStorage.setItem(CurrentUserKey, JSON.stringify(currentUser))
}
// Lấy tất cả công ty của người dùng
export function getLocalCompanys() {
  return JSON.parse(sessionStorage.getItem(CompanysKey))
}
// Lưu tất cả công ty của người dùng
export function setLocalCompanys(Companys) {
  sessionStorage.setItem(CompanysKey, JSON.stringify(Companys))
}

// Lấy route của người dùng đã đăng nhập
export function getLocalRouters() {
  return JSON.parse(sessionStorage.getItem(RoutersKey))
}
// Lưu route của người dùng đã đăng nhập
export function setLocalRouters(routers) {
  sessionStorage.setItem(RoutersKey, JSON.stringify(routers))
}

// Lấy cài đặt hệ thống
export function getLocalSysSetting() {
  return JSON.parse(sessionStorage.getItem(SysSettingKey))
}
// Lưu cài đặt hệ thống
export function setLocalSysSetting(settings) {
  sessionStorage.setItem(SysSettingKey, JSON.stringify(settings))
}


// Lấy quyền dữ liệu của người dùng đã đăng nhập
export function getLocalDataPermisions() {
  return JSON.parse(sessionStorage.getItem(DataPermisionsKey))
}
// Lưu quyền dữ liệu của người dùng đã đăng nhập
export function setLocalDataPermisions(dataPermission) {
  sessionStorage.setItem(DataPermisionsKey, JSON.stringify(dataPermission))
}

// Lấy chủ đề của người dùng đã đăng nhập
export function getLocalPersonalTheme() {
  return JSON.parse(sessionStorage.getItem(PersonalThemeKey))
}
// Lưu chủ đề của người dùng đã đăng nhập
export function setLocalPersonalTheme(personalTheme) {
  sessionStorage.setItem(PersonalThemeKey, JSON.stringify(personalTheme))
}

// Lấy công ty hiện tại của người dùng đã đăng nhập
export function getLocalCurrentCompany() {
  return JSON.parse(sessionStorage.getItem(CurrentCompanyKey))
}
// Lưu công ty hiện tại của người dùng đã đăng nhập
export function setLocalCurrentCompany(currentCompany) {
  sessionStorage.setItem(CurrentCompanyKey, JSON.stringify(currentCompany))
}