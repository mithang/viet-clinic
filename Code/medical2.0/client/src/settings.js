/**
 * Cài đặt giao diện hệ thống
 */
module.exports = {
  // Tên hiển thị mặc định của hệ thống (bị ghi đè bởi cấu hình
  // sys_seting.sysAbbrname tải từ backend sau khi đăng nhập)
  title: 'Phòng khám đám mây',
  /**
   * @type {string} up-down-dark | up-down-light | left-right-dark | left-right-light
   * @description Phong cách chủ đề style
   */
  style: 'up-down',
  /**
   * @type {string} #018cb7
   * @description Màu chủ đề theme
   */
  theme: '#018cb7',
  /**
   * @type {string} #018cb7
   * @description Màu header
   */
  headerColor: '#018cb7',
  /**
   * @type {string} #2f4050
   * @description Màu sidebar
   */
  sidebarColor: '#2f4050',
  /**
   * @type {string}
   * @description Màu nền vùng làm việc
   */
  backgroundColor: '',
  /**
   * @type {string} default | medium | small | mini
   * @description Kích thước bố cục
   */
  size: 'mini',
  /**
   * @type {boolean} true | false
   * @description Có cần tagsView không
   */
  showTagsView: false,
  /**
   * @type {boolean} true | false
   * @description Có cần cố định header không
   */
  fixedHeader: true,
  
  /**
   * @type {boolean} true | false
   * @description Có hiển thị không logo
   */
  showLogo: true
}
