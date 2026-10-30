package com.geeke.common.constants;

/**
 * nghiệp vụTrạng thái/Loạithườnglượngđịnh nghĩa
 *  và  sys_dict_item bảngTrong value TrườngNhất quán
 *
 * Xóa bỏmã trong phân tánphát hành của 魔phương phápchuỗi(Như  "amountStatus_1", "tollType_0"  v.v. )
 * Nếutừ điểnGiá trịphátsinhthay đổi, chỉcầnSửanàyTệp
 */
public final class BizConstants {

    private BizConstants() {}

    // ========== Đăng ký khámTrạng thái registrationStatus ==========
    /** Chờ khám */
    public static final String REG_STATUS_PENDING = "registrationStatus_0";
    /** Đãkhám bệnh */
    public static final String REG_STATUS_VISITED = "registrationStatus_1";
    /** hủy số khám */
    public static final String REG_STATUS_REFUNDED = "registrationStatus_2";
    /** chờkýĐến */
    public static final String REG_STATUS_PENDING_CHECKIN = "registrationStatus_3";
    /** Đãhết hiệu lực */
    public static final String REG_STATUS_FEE_REFUNDED = "registrationStatus_4";
    /** chờ phútkhám */
    public static final String REG_STATUS_PENDING_TRIAGE = "registrationStatus_5";

    // ========== Trạng thái thu phí amountStatus ==========
    /** ChưaThu phí */
    public static final String AMOUNT_STATUS_UNPAID = "amountStatus_0";
    /** Đã thu phí */
    public static final String AMOUNT_STATUS_PAID = "amountStatus_1";
    /** Đã hoàn phí */
    public static final String AMOUNT_STATUS_REFUNDED = "amountStatus_2";

    // ========== Loại thu phí tollType ==========
    /** Đơn thuốc tây */
    public static final String TOLL_TYPE_WESTERN = "tollType_0";
    /** Đơn thuốc đông y */
    public static final String TOLL_TYPE_CHINESE = "tollType_1";
    /** Truyền dịch */
    public static final String TOLL_TYPE_INFUSION = "tollType_2";
    /** Điều trị */
    public static final String TOLL_TYPE_TREATMENT = "tollType_3";
    /** Xét nghiệm và kiểm tra */
    public static final String TOLL_TYPE_EXAMINATION = "tollType_4";
    /** Phí đăng ký khám */
    public static final String TOLL_TYPE_REGISTRATION = "tollType_5";

    // ========== Đơn thuốcLoại recipelType ==========
    /** Đơn thuốc tây */
    public static final String RECIPEL_TYPE_WESTERN = "recipelType_0";
    /** Đơn thuốc đông y */
    public static final String RECIPEL_TYPE_CHINESE = "recipelType_1";
    /** Đơn truyền dịch */
    public static final String RECIPEL_TYPE_INFUSION = "recipelType_2";
    /** khácĐơn thuốc */
    public static final String RECIPEL_TYPE_OTHER = "recipelType_3";
    /**  trong thànhthuốcĐơn thuốc */
    public static final String RECIPEL_TYPE_PATENT = "recipelType_4";
    /** ngoàiphốiĐơn thuốc */
    public static final String RECIPEL_TYPE_EXTERNAL = "recipelType_5";

    // ========== khám bệnhLoại treatType ==========
    /** thông thườngngoại trú */
    public static final String TREAT_TYPE_NORMAL = "treatType_0";
    /** Tái khám */
    public static final String TREAT_TYPE_FOLLOWUP = "treatType_1";

    // ========== Thuốc/Loại vật tư stuffType ==========
    /** Thuốc */
    public static final String STUFF_TYPE_DRUG = "stuffType_0";
    /** Vật tư */
    public static final String STUFF_TYPE_MATERIAL = "stuffType_1";
    /** Hạng mục khám chữa bệnh */
    public static final String STUFF_TYPE_COST_ITEM = "stuffType_2";

    // ========== ngoại trúLoại medicalType ==========
    /** Thuốc tây */
    public static final String MEDICAL_TYPE_WESTERN = "medicalType_0";
    /** Dược liệu đông y */
    public static final String MEDICAL_TYPE_HERBAL = "medicalType_1";
    /** Thuốc thành phẩm đông y */
    public static final String MEDICAL_TYPE_PATENT = "medicalType_2";

    // ========== điều trịhạng mụcLoại treatmentItemType ==========
    /** Xét nghiệm */
    public static final String TREATMENT_ITEM_TYPE_LAB = "treatmentItemType_0";
    /** Kiểm tra */
    public static final String TREATMENT_ITEM_TYPE_EXAM = "treatmentItemType_1";

    // ========== Giới tính gender ==========
    /** Nam */
    public static final String GENDER_MALE = "gender_0";
    /** Nữ */
    public static final String GENDER_FEMALE = "gender_1";

    // ========== Vai tròMã ==========
    /** Quản lýnhân viênVai tròMã */
    public static final String ROLE_ADMIN = "admin";

    // ========== phương thức thanh toán payType ==========
    /** tiền mặtthanh toán */
    public static final String PAY_TYPE_CASH = "payType_0";
    /** thanh toánbảothanh toán */
    public static final String PAY_TYPE_ALIPAY = "payType_1";
    /** WeChatthanh toán */
    public static final String PAY_TYPE_WECHAT = "payType_2";
    /** ngân hàng dòngthẻthanh toán */
    public static final String PAY_TYPE_BANK_CARD = "payType_3";
    /** BHYTthanh toán — GIỮ HẰNG SỐ CHO DỮ LIỆU LỊCH SỬ, nhưng giá trị này
     *  đã bị ẩn khỏi dict payType (payment_dict_vi.sql) vì hệ thống chưa
     *  có quyết toán BHYT Việt Nam; không dùng cho thu phí mới. */
    public static final String PAY_TYPE_MEDICAL = "payType_4";
    /** Chuyển khoản ngân hàng (Việt Nam, thêm 2026-10-30) */
    public static final String PAY_TYPE_BANK_TRANSFER = "payType_5";
    /** Ví điện tử Việt Nam — chỉ ghi nhận hình thức, chưa tích hợp cổng
     *  thanh toán hay đối soát tự động (thêm 2026-10-30) */
    public static final String PAY_TYPE_EWALLET = "payType_6";

    // ========== Bệnh truyền nhiễmLoại infectType ==========
    /** khôngBệnh truyền nhiễm */
    public static final String INFECT_TYPE_NONE = "infectType_0";

    // ========== Đăng ký khámđếnnguồn registrationSource ==========
    /** Khám lần đầu */
    public static final String REG_SOURCE_NORMAL = "registrationSource_0";
    /** Tiếp nhận nhanh */
    public static final String REG_SOURCE_QUICK = "registrationSource_2";
    /** Thu phí bán lẻ */
    public static final String REG_SOURCE_RETAIL = "registrationSource_3";

    // ========== Nhà cung cấpnhập khoTrạng thái xét duyệt supplierStorageExamineStatus ==========
    /** xét duyệtQua */
    public static final String SUPPLIER_STORAGE_EXAMINE_PASS = "supplierStorageExamineStatus_0";
    /** Đãhủy */
    public static final String SUPPLIER_STORAGE_EXAMINE_VOIDED = "supplierStorageExamineStatus_2";
    /** Chờ xét duyệt */
    public static final String SUPPLIER_STORAGE_EXAMINE_PENDING = "supplierStorageExamineStatus_3";

    // ========== Loại từ điểnID(sys_dict_type.id) ==========
    /** Loại thuốc */
    public static final String DICT_TYPE_DRUG_TYPE = "1004078055755374603";
    /** Thuốctính chất */
    public static final String DICT_TYPE_DRUG_NATURE = "1004078055755374607";
    /** Đơn vị liều lượng */
    public static final String DICT_TYPE_DOSAGE_UNIT = "1004406758192578588";
    /** Đơn vị bào chế */
    public static final String DICT_TYPE_PREPARATION_UNIT = "1004406758192578593";
    /** Quy cách đóng gói */
    public static final String DICT_TYPE_PACK_SPEC = "1004406758192578597";
    /** Loại vật tư */
    public static final String DICT_TYPE_STUFF_TYPE = "1004462867645374476";
    /** Cơ sởLoại */
    public static final String DICT_TYPE_INSTITUTION_TYPE = "998465736089977631";
    /** Cơ sởĐơn vị */
    public static final String DICT_TYPE_INSTITUTION_UNIT = "999976636865404934";

    // ========== nghiệp vụthực thểID(Cầnsautiếp tụcsửa là độngcấu hình) ==========
    /** Mặc địnhNhà cung cấpID(bản việnNhà cung cấp) */
    public static final String DEFAULT_SUPPLIER_ID = "2098053487889309725";
    /** nhập khoxét duyệtQuamục từ điểnID */
    public static final String DICT_ITEM_STORAGE_EXAMINE_PASS = "1005787933775863930";
}
