/**
 * Bảng ánh xạ giá trị cũ lưu bằng tiếng Trung sang tiếng Việt.
 *
 * Vì sao cần file này: một số giá trị enum/trạng thái đã được ghi vào cơ sở dữ liệu
 * bằng tiếng Trung từ trước khi bản địa hoá. Không thể đổi thẳng chuỗi trong code,
 * vì nếu đổi mà chưa migration DB thì phép so sánh sẽ không còn khớp và chức năng
 * im lặng ngừng hoạt động.
 *
 * Cách dùng:
 *   normalizeLegacy(v)        -> đổi giá trị cũ sang tiếng Việt để hiển thị
 *   sameLegacy(a, b)          -> so sánh chấp nhận CẢ giá trị cũ và mới
 *   matchesAnyLegacy(v, list) -> kiểm tra v thuộc một trong các giá trị cho trước
 *
 * Nhờ đó code chạy đúng trước VÀ sau khi chạy DB/migration_v4_enum_vi.sql.
 * Khi migration đã chạy xong trên mọi môi trường, có thể xoá bảng LEGACY_VALUES
 * và thay các lời gọi sameLegacy() bằng so sánh chuỗi thường.
 */
export const LEGACY_VALUES = {
  // Loại nghề nghiệp (sys_dict / user_ext.job_type)
  '药师': 'Dược sĩ',

  // Giới tính (dict gender: gender_0 / gender_1)
  '男': 'Nam',
  '女': 'Nữ',

  // Loại điều trị (treat_type)
  '初诊': 'Khám lần đầu',
  '复诊': 'Tái khám',

  // Cách dùng thuốc đông y (dict chinese_use)
  '水冲': 'Uống với nước',

  // Tên hạng mục phí (business data)
  '煎药费': 'Phí sắc thuốc',

  // Trạng thái đồng bộ (zstbzt)
  '诊所已同步': 'Đã đồng bộ',

  // Trạng thái tác vụ định kỳ (Quartz job status)
  '执行中': 'Đang chạy',
  '已暂停': 'Đã tạm dừng',
  '待执行': 'Chờ chạy',
  '已完成': 'Đã hoàn thành',
  '已停止': 'Đã dừng'
}

/**
 * Đổi một giá trị cũ sang tiếng Việt. Giá trị không nằm trong bảng thì giữ nguyên.
 * @param {*} value giá trị cần chuẩn hoá
 * @returns {*} giá trị đã chuẩn hoá
 */
export function normalizeLegacy (value) {
  if (typeof value !== 'string') {
    return value
  }
  const trimmed = value.trim()
  return Object.prototype.hasOwnProperty.call(LEGACY_VALUES, trimmed)
    ? LEGACY_VALUES[trimmed]
    : value
}

/**
 * So sánh hai giá trị sau khi đã chuẩn hoá, nên khớp được cả trước và sau migration.
 * @param {*} actual giá trị thực tế (có thể là giá trị cũ)
 * @param {*} expected giá trị mong đợi (có thể là giá trị cũ hoặc mới)
 * @returns {boolean}
 */
export function sameLegacy (actual, expected) {
  return normalizeLegacy(actual) === normalizeLegacy(expected)
}

/**
 * Kiểm tra giá trị có thuộc danh sách cho trước hay không (đã chuẩn hoá cả hai phía).
 * @param {*} actual giá trị thực tế
 * @param {Array} candidates danh sách giá trị mong đợi
 * @returns {boolean}
 */
export function matchesAnyLegacy (actual, candidates) {
  const normalized = normalizeLegacy(actual)
  return candidates.some(candidate => normalizeLegacy(candidate) === normalized)
}
