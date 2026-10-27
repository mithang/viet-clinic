package com.geeke.medicareutils.domain.respo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Output_1101 {

    private BaseInfo baseinfo; // cơ bảnThông tin
    private List<InsuInfo> insuinfo; // tham gia bảo hiểmThông tinDanh sách
    private List<IdetInfo> idetinfo; // thânphầnThông tinDanh sách

    @Data
    public static class BaseInfo {
        private String psn_no; // Mã nhân viên
        private String psn_cert_type; // Loại giấy tờ nhân viên
        private String certno; // Số giấy tờ
        private String psn_name; // Tên nhân viên
        private String gend; // Giới tính
        private String naty; // Dân tộc
        private String brdy; // Ngày sinh
        private int age; // Tuổi
        private String expContent; // Trườngmở rộng
    }

    @Data
    public static class InsuInfo {
        private BigDecimal balc; // Số dư
        private String insutype; // Loại hình bảo hiểm
        private String psn_type; // Loại nhân viên
        private String psn_insu_stas; // nhân viêntham gia bảo hiểmTrạng thái
        private String psn_insu_date; // Cá nhântham gia bảo hiểmNgày
        private String paus_insu_date; // tạmdừngtham gia bảo hiểmNgày
        private String cvlserv_flag; // Cờ công chức
        private String insuplc_admdvs; // Vùng BHYT nơi tham gia bảo hiểm
        private String emp_name; // Tên đơn vị
    }

    @Data
    public static class IdetInfo {
        private String psn_idet_type; // nhân viênthânphầnLớploại
        private String psn_type_lv; // Loại nhân viên v.v. cấp
        private String memo; // Ghi chú
        private String begntime; // Thời gian bắt đầu
        private String endtime; // Thời gian kết thúc
    }

}
