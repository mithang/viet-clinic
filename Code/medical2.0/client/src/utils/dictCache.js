/**
 * Tiện ích bộ nhớ đệm từ điển
 * Lấy mục từ điển theo mã loại từ điển (thay vì ID snowflake), loại bỏ hard-code ID ở frontend
 */
import { listDictItemByCode } from '@/api/sys/dictItem'

// Hằng số mã của tất cả loại từ điển
export const DICT_CODE = {
  // Thông tin cơ bản
  GENDER: 'gender',
  WITH_PATIENT_NEXUS: 'withPatientNexus',
  CARD_TYPE: 'cardType',
  INFECT_TYPE: 'infectType',
  // Đăng ký khám
  REGISTRATION_STATUS: 'registrationStatus',
  REGISTRATION_SOURCE: 'registrationSource',
  TREAT_TYPE: 'treatType',
  MED_TYPE: 'medType',
  // Thu phí
  PAY_TYPE: 'payType',
  TOLL_TYPE: 'tollType',
  AMOUNT_STATUS: 'amountStatus',
  // Đơn thuốc
  RECIPEL_TYPE: 'recipelType',
  RECIPEL_SMALL_TYPE: 'recipelSmallType',
  CHINESE_MEDICINE_RECIPEL_USE: 'chineseMedicineRecipelUse',
  CHINESE_MEDICINE_RECIPEL_FREQUENCY: 'chineseMedicineRecipelFrequency',
  CHINESE_MEDICINE_RECIPEL_TAKE_FREQUENCY: 'chineseMedicineRecipelTakeFrequency',
  CHINESE_MEDICINE_USE: 'chineseMedicineUse',
  WESTERN_MEDICINE_USE: 'westernMedicineUse',
  RECIPEL_DETAIL_FREQUENCY: 'recipelDetailFrequency',
  RECIPEL_DETAIL_DAYS: 'recipelDetailDays',
  INFUSE_USE: 'infuseUse',
  SKIN_TEST: 'skinTest',
  // Thuốc/Vật tư
  MEDICAL_TYPE: 'medicalType',
  MEDICAL_NATURE: 'medicalNature',
  MEDICAL_DOSIS_UNIT: 'medicalDosisUnit',
  MEDICAL_PREPARATION_UNIT: 'medicalPreparationUnit',
  MEDICAL_PACK_UNIT: 'medicalPackUnit',
  STUFF_TYPE: 'stuffType',
  // Hạng mục khám chữa bệnh
  TREATMENT_ITEM_TYPE: 'treatmentItemType',
  TREATMENT_ITEM_UNIT: 'treatmentItemUnit',
  // Tồn kho
  SUPPLIER_STORAGE_EXAMINE_STATUS: 'supplierStorageExamineStatus',
  SUPPLIER_OUTBOUND_STATUS: 'supplierOutboundStatus',
  OUTBOUND_METHOD: 'outboundMethod',
  // Thành viên
  MEMBER_TYPE: 'memberType',
  // Hệ thống
  ACTION_TYPE: 'actionType',
}

// Bộ nhớ đệm trong RAM
const cache = new Map()

/**
 * Lấy danh sách mục từ điển theo mã loại từ điển (có bộ nhớ đệm)
 * @param {string} code Mã loại từ điển, như DICT_CODE.GENDER
 * @returns {Promise<Array>} Danh sách mục từ điển
 */
export async function getDictItemsByCode(code) {
  if (cache.has(code)) return cache.get(code)
  try {
    const res = await listDictItemByCode(code)
    const items = res.data || []
    cache.set(code, items)
    return items
  } catch (e) {
    console.error('Tải từ điển thất bại:', code, e)
    return []
  }
}

/**
 * Nạp trước từ điển hàng loạt (dùng khi khởi tạo trang)
 * @param {string[]} codes Mảng mã loại từ điển
 */
export async function preloadDicts(codes) {
  await Promise.all(codes.map(code => getDictItemsByCode(code)))
}

/**
 * Xoá bộ nhớ đệm từ điển được chỉ định hoặc toàn bộ
 * @param {string} [code] Không truyền thì xoá toàn bộ
 */
export function clearDictCache(code) {
  if (code) cache.delete(code)
  else cache.clear()
}
