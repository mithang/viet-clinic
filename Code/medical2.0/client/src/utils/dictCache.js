/**
 * 字典缓存工具
 * 通过字典类型 code（而非雪花 ID）Lấy mục từ điển，消除前端硬编码 ID
 */
import { listDictItemByCode } from '@/api/sys/dictItem'

// 所有字典类型 code 常量
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
  // 会员
  MEMBER_TYPE: 'memberType',
  // Hệ thống
  ACTION_TYPE: 'actionType',
}

// 内存缓存
const cache = new Map()

/**
 * Theo loại từ điển code 获取字典项列表（带内存缓存）
 * @param {string} code Loại từ điển code，như DICT_CODE.GENDER
 * @returns {Promise<Array>} 字典项列表
 */
export async function getDictItemsByCode(code) {
  if (cache.has(code)) return cache.get(code)
  try {
    const res = await listDictItemByCode(code)
    const items = res.data || []
    cache.set(code, items)
    return items
  } catch (e) {
    console.error('加载字典失败:', code, e)
    return []
  }
}

/**
 * 批量预加载字典（用于页面初始化）
 * @param {string[]} codes Loại từ điển code Mảng
 */
export async function preloadDicts(codes) {
  await Promise.all(codes.map(code => getDictItemsByCode(code)))
}

/**
 * 清除指定或全部字典缓存
 * @param {string} [code] 不传则清除全部
 */
export function clearDictCache(code) {
  if (code) cache.delete(code)
  else cache.clear()
}
