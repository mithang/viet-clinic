/**
 * Sanitize tối thiểu cho HTML bài viết (không thêm dependency).
 * Dùng DOMParser của trình duyệt, chỉ giữ lại các thẻ định dạng văn bản
 * thông thường của Quill và các thuộc tính an toàn; mọi thứ khác bị gỡ:
 * - Bỏ hẳn: script, style, iframe, object, embed, form, input, button,
 *   link, meta, title, base, svg, math và mọi thẻ không trong allowlist
 *   (script/style bị bỏ cả nội dung, thẻ còn lại chỉ bị bóc vỏ, giữ text).
 * - Bỏ mọi attribute bắt đầu bằng "on" (onclick, onerror, ...), mọi
 *   attribute style/class (tránh CSS injection, đánh đổi: mất căn lề/màu
 *   chữ của Quill nhưng giữ nguyên chữ và cấu trúc đoạn/danh sách).
 * - Link chỉ giữ href http/https/mailto; ảnh chỉ giữ src http/https hoặc
 *   data:image (ảnh nhúng của Quill). javascript:, data:text/html, vbscript:
 *   đều bị gỡ attribute.
 * Kết quả vẫn dùng với v-html nhưng đã qua lọc, nên nội dung bài viết do
 * người khác soạn không còn chạy được script khi người đọc/xét duyệt mở ra.
 */
const ALLOWED_TAGS = new Set([
  'P', 'BR', 'B', 'STRONG', 'I', 'EM', 'U', 'S', 'SUB', 'SUP',
  'UL', 'OL', 'LI', 'DIV', 'SPAN',
  'H1', 'H2', 'H3', 'H4', 'H5', 'H6',
  'BLOCKQUOTE', 'PRE', 'CODE', 'HR',
  'A', 'IMG'
])

const DROP_ENTIRELY = new Set([
  'SCRIPT', 'STYLE', 'IFRAME', 'OBJECT', 'EMBED', 'FORM', 'INPUT',
  'BUTTON', 'LINK', 'META', 'TITLE', 'BASE', 'SVG', 'MATH', 'TEXTAREA',
  'SELECT', 'AUDIO', 'VIDEO', 'SOURCE', 'TRACK'
])

function isSafeUrl(url, allowDataImage) {
  if (!url) return false
  const value = url.trim().toLowerCase()
  if (value.startsWith('http://') || value.startsWith('https://') || value.startsWith('mailto:')) return true
  if (allowDataImage && value.startsWith('data:image/')) return true
  return false
}

function cleanNode(node) {
  const children = Array.from(node.childNodes)
  children.forEach((child) => {
    if (child.nodeType === Node.COMMENT_NODE) {
      child.remove()
      return
    }
    if (child.nodeType !== Node.ELEMENT_NODE) return
    const tag = child.tagName
    if (DROP_ENTIRELY.has(tag)) {
      child.remove()
      return
    }
    if (!ALLOWED_TAGS.has(tag)) {
      // Bóc vỏ: giữ lại các node con, bỏ thẻ lạ.
      cleanNode(child)
      while (child.firstChild) {
        node.insertBefore(child.firstChild, child)
      }
      child.remove()
      return
    }
    // Thẻ được giữ: chỉ giữ attribute an toàn, gỡ toàn bộ attribute khác.
    const kept = {}
    if (tag === 'A' && isSafeUrl(child.getAttribute('href'), false)) {
      kept.href = child.getAttribute('href')
    }
    if (tag === 'A' && child.getAttribute('title')) {
      kept.title = child.getAttribute('title')
    }
    if (tag === 'IMG' && isSafeUrl(child.getAttribute('src'), true)) {
      kept.src = child.getAttribute('src')
    }
    if (tag === 'IMG' && child.getAttribute('alt')) {
      kept.alt = child.getAttribute('alt')
    }
    Array.from(child.attributes).forEach((attr) => child.removeAttribute(attr.name))
    Object.keys(kept).forEach((name) => child.setAttribute(name, kept[name]))
    cleanNode(child)
  })
}

export function sanitizeHtml(html) {
  if (!html) return ''
  if (typeof DOMParser === 'undefined') return ''
  const doc = new DOMParser().parseFromString(html, 'text/html')
  cleanNode(doc.body)
  return doc.body.innerHTML
}

export default sanitizeHtml
