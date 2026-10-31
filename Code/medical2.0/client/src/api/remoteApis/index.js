/**
 * Lớp gọi API thống nhất cho HIS phòng khám/Khám chữa bệnh từ xa
 *
 * Tất cả API bên ngoài bắt đầu bằng apis/ đều được gọi qua module này, tránh hard-code URL và lặp boilerplate trong view.
 * Cách gọi: import { treatedApplyInfo, getPatient, ... } from '@/api/remoteApis'
 */
import axios from 'axios'

const APP_ID = 'Oh_Newtouch_Clinic'

/**
 * Tạo hàm yêu cầu API từ xa
 * @param {string} url - Đường dẫn API (như 'apis/RemoteTreated/TreatedApplyInfo')
 * @returns {function} Hàm yêu cầu, nhận tham số (data, organizeId, token)
 */
function createRemoteApi(url) {
  return (data, organizeId, token) =>
    axios.post(url, {
      Data: data,
      OrganizeId: organizeId,
      AppId: APP_ID,
      Timestamp: new Date()
    }, {
      headers: {
        Authorization: 'Bearer ' + token
      }
    })
}

// ========== Khám chữa bệnh từ xa ==========

/** Truy vấn thông tin yêu cầu hội chẩn */
export const treatedApplyInfo = createRemoteApi('apis/RemoteTreated/TreatedApplyInfo')

/** Yêu cầu hội chẩn từ xa (chỉ chuyển tuyến) */
export const remoteTreatedApplyZfOnly = createRemoteApi('apis/RemoteTreated/RemoteTreatedApplyZfOnly')

/** Yêu cầu họp bác sĩ */
export const doctorMeetingApply = createRemoteApi('apis/RemoteTreated/DoctorMeetingApply')

/** Yêu cầu người dùng tham gia cuộc họp */
export const userJoinMeetingApply = createRemoteApi('apis/RemoteTreated/UserJoinMeetingApply')

/** Huỷ yêu cầu hội chẩn */
export const treatedApplyCancel = createRemoteApi('apis/RemoteTreated/TreatedApplyCancel')

// ========== Bệnh nhân ==========

/** Truy vấn thông tin bệnh nhân */
export const getPatient = createRemoteApi('apis/Patient/GetPatient')

/** Bệnh nhân tự đăng ký khám */
export const patientSelfReg = createRemoteApi('apis/Patient/PatientSelfReg')

/** Thêm địa chỉ bệnh nhân */
export const patientAddressAdd = createRemoteApi('apis/patient/PatientAddressAdd')

/** Cập nhật địa chỉ bệnh nhân */
export const patientAddressUpdate = createRemoteApi('apis/patient/PatientAddressUpdate')

/** Truy vấn địa chỉ đơn hàng của bệnh nhân */
export const patientOrderAddressQuery = createRemoteApi('apis/patient/PatientOrderAddressQuery')

// ========== Ngoại trú ==========

/** Truy vấn hoá đơn ngoại trú chưa thanh toán */
export const outpBillUnpaidByMzh = createRemoteApi('apis/Outpatient/OutpBillUnpaidByMzh')

/** Chi tiết hạng mục hoá đơn ngoại trú */
export const outpBillItemDetail = createRemoteApi('apis/Outpatient/OutpBillItemDetail')

// ========== Đăng ký khám/Hẹn khám ==========

/** Lấy danh sách cơ sở */
export const getOrgList = createRemoteApi('apis/SysOrg/GetOrgList')

/** Lấy khoa ngoại trú */
export const getOutpDepartment = createRemoteApi('apis/Booking/GetOutpDepartment')

/** Lấy bác sĩ ngoại trú */
export const getOutpDoctor = createRemoteApi('apis/Booking/GetOutpDoctor')

// ========== Thanh toán ==========

/** Truy vấn đơn hàng */
export const orderQuery = createRemoteApi('apis/PayOrder/OrderQuery')

/** Tạo đơn hàng hoá đơn */
export const billOrderCreate = createRemoteApi('apis/PayOrder/BillOrderCreate')

/** Yêu cầu khoá hoá đơn */
export const billOrderLockApply = createRemoteApi('apis/PayOrder/BillOrderLockApply')

/** Thanh toán hoá đơn */
export const billOrderPay = createRemoteApi('apis/PayOrder/BillOrderPay')
