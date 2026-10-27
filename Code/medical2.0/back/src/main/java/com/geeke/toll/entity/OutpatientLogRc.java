package com.geeke.toll.entity;

import lombok.Data;

import java.util.Date;

@Data
public class OutpatientLogRc {
    private int limit;
    private int offset;
    private String companyId;
    private String kssj;
    private String jssj;
    /** ID */
    private String id;

    /** Tên bệnh nhân */
    private String patientName;

    /**Tên phụ huynh */
    private String patriarchName;

    /** Giới tính */
    private String sex;

    /** Tuổi bệnh nhân */
    private String age;

    /** Dân tộc */
    private String nation;

    /** Nghề nghiệp */
    private String occupation;

    /** Địa chỉ chi tiết */
    private String address;

    /** Ngày khám bệnh */
    private Date visitDate;

    /** ban đầu/Tái khám */
    private String initialVisit;

    /** Huyết áp */
    private String bloodPressure;

    /** Triệu chứng lâm sàng */
    private String symptom;

    /** Sốt, nhiệt độ cơ thể tăng */
    private String fever;

    /** Tiền sử dịch tễ */
    private String epidemicDisease;

    /** Chẩn đoán */
    private String diagnosis;

    /** Bệnh truyền nhiễm */
    private String infect;

    /** Tình hình xử lý */
    private String handle;

    /** Số giấy tờ hiệu lực */
    private String certificate;

    /** Đơn vị công tác */
    private String unit;

    /** Chữ ký bác sĩ */
    private String signature;

    /** Cờ hộ nghèo */
    private String poverty;

    /** Số điện thoại liên hệ */
    private String telephone;

    /** Ngày phát bệnh */
    private Date morbidityDate;

    /** Kết quả dương tính trong phòng thí nghiệm */
    private String positiveResult;

    /** Giáo dục sức khỏe cá thể hóa */
    private String healthEducation;

    /**Trạng thái cơ sở(1: Đăng nhập tenant)*/
    private String jgzt;

    /** Cơ sởid(Đơn vị thuêid)*/
    private String jgid;
}
