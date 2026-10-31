
/**
* Hai vòng lặp để xây dựng cây: dữ liệu list thông qua id và parentId cấu thành cây
*/
export function buildTree(list) {
  const map = {}
  const trees = []
  list.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  list.forEach(item => {
    if (item.parentId === '0' || !item.parentId) {
      trees.push(map[item.id])
    } else if (map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    }
  })
  return trees
}

// Giữ tên cũ để đảm bảo tương thích ngược
export const bulidTree = buildTree

export function isLightOrDark(rgbColor) {
  let currentRgb = rgbColor.replace("rgb(", "").replace(")", "");
  let currentRgbArr = currentRgb.split(',');
  let grayLevel =  currentRgbArr[0] * 0.299 + currentRgbArr[1] * 0.587 + currentRgbArr[2] * 0.114;
  return grayLevel >= 192
}

/**
 * Lấy ngày cách đây N ngày
 * @param {number} days Số ngày
 * @returns {Date} Đối tượng Date của N ngày trước
 */
export function daysAgo(days) {
  return new Date(Date.now() - days * 86400000)
}

/**
 * Định dạng ngày thành chuỗi YYYY-MM-DD HH:mm:ss
 * @param {Date} date Đối tượng ngày
 * @returns {string} Chuỗi ngày đã định dạng
 */
export function formatDateTime(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}:${s}`
}

/**
 * Công cụ định dạng BigNumber (phân tách hàng nghìn, giữ 2 chữ số thập phân)
 * Cần dự án đã import BigNumber.js
 * @param {BigNumber} BigNumber Hàm khởi tạo BigNumber
 * @returns {function} Hàm định dạng
 */
export function createBigNumFormatter(BigNumber) {
  return function bigNum(num) {
    if (num || num === '0') {
      return new BigNumber(num).toFormat(2)
    }
    return ''
  }
}
