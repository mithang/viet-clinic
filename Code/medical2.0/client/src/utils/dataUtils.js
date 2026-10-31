/**
 * Hàm công cụ phân tích dữ liệu
 * Quản lý tập trung logic phân tích định dạng dữ liệu mong manh
 */

/**
 * Trích xuất phần giá trị số từ giá trị phức hợp
 * Ví dụ: "BID_2" → 2, "tollType_0" → "0"
 * @param {string} value - Chuỗi giá trị phức hợp (như "BID_2")
 * @param {string} separator - Dấu phân cách (mặc định "_")
 * @param {*} defaultValue - Giá trị mặc định khi phân tích thất bại
 * @returns {string} Phần sau dấu phân cách
 */
export function extractSuffix(value, separator = '_', defaultValue = '') {
  if (!value || typeof value !== 'string') return defaultValue;
  const parts = value.split(separator);
  return parts.length > 1 ? parts[parts.length - 1] : defaultValue;
}

/**
 * Trích xuất phần tiền tố từ giá trị phức hợp
 * Ví dụ: "BID_2" → "BID", "tollType_0" → "tollType"
 * @param {string} value - Chuỗi giá trị phức hợp
 * @param {string} separator - Dấu phân cách (mặc định "_")
 * @param {*} defaultValue - Giá trị mặc định khi phân tích thất bại
 * @returns {string} Phần trước dấu phân cách
 */
export function extractPrefix(value, separator = '_', defaultValue = '') {
  if (!value || typeof value !== 'string') return defaultValue;
  const parts = value.split(separator);
  return parts.length > 1 ? parts[0] : defaultValue;
}

/**
 * Trích xuất số lần từ giá trị tần suất
 * Ví dụ: "BID_2" → 2, "QD_1" → 1
 * @param {string} frequencyValue - Giá trị tần suất
 * @returns {number} Số lần tần suất
 */
export function getFrequencyCount(frequencyValue) {
  const suffix = extractSuffix(frequencyValue, '_', '1');
  const num = parseInt(suffix, 10);
  return isNaN(num) ? 1 : num;
}

/**
 * Nối chuỗi quy cách thuốc
 * Ví dụ: "10mg*12viên/hộp"
 * @param {object} drug - Đối tượng thuốc
 * @returns {string} Chuỗi quy cách
 */
export function formatDrugSpec(drug) {
  if (!drug) return '';
  const dosis = drug.dosis || '';
  const dosisUnit = drug.dosisUnit ? (typeof drug.dosisUnit === 'object' ? drug.dosisUnit.name : drug.dosisUnit) : '';
  const preparation = drug.preparation || '';
  const preparationUnit = drug.preparationUnit ? (typeof drug.preparationUnit === 'object' ? drug.preparationUnit.name : drug.preparationUnit) : '';
  const pack = drug.pack ? (typeof drug.pack === 'object' ? drug.pack.name : drug.pack) : '';
  return `${dosis}${dosisUnit}*${preparation}${preparationUnit}/${pack}`;
}
