/**
 * Công cụ tải Blob
 * Xử lý thống nhất tải file stream do backend trả về
 */

/**
 * Phân tích tên file từ header content-disposition
 * @param {string} disposition - Giá trị header content-disposition
 * @returns {string|null} Tên file đã phân tích
 */
export function parseFilename(disposition) {
  if (!disposition) return null
  // Ưu tiên thử định dạng filename*=UTF-8'' (RFC 5987)
  const utf8Match = disposition.match(/filename\*=UTF-8''(.+)/i)
  if (utf8Match) {
    return decodeURIComponent(utf8Match[1])
  }
  // Fallback về định dạng filename="xxx" hoặc filename=xxx
  const match = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/i)
  if (match) {
    let filename = match[1].replace(/['"]/g, '')
    try {
      filename = decodeURI(filename)
    } catch (e) {
      // Giải mã thất bại thì dùng giá trị gốc
    }
    return filename
  }
  return null
}

/**
 * Tải blob response
 * @param {Object} response - Đối tượng response axios (responseType: 'blob')
 * @param {string} [defaultFilename] - Tên file mặc định (khi headers không có tên file)
 */
export function downloadBlob(response, defaultFilename) {
  const headers = response.headers
  // content-disposition có thể ở vị trí khác nhau
  const disposition = headers['content-disposition'] || headers
  const filename = parseFilename(typeof disposition === 'string' ? disposition : '') || defaultFilename || 'Tải tệp xuống'

  const blob = new Blob([response.data], { type: 'application/octet-stream' })
  const url = window.URL.createObjectURL(blob)

  const link = document.createElement('a')
  link.style.display = 'none'
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()

  // Dọn dẹp, tránh rò rỉ bộ nhớ
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
