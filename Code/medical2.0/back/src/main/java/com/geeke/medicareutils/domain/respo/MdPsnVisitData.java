package com.geeke.medicareutils.domain.respo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * nhân viênkhám bệnhThông tin
 */
@Data
public class MdPsnVisitData {

        /** Khám bệnh ID */
        private String mdtrtId;

        /** Mã nhân viên */
        private String psnNo;

        /** Loại giấy tờ nhân viên */
        private String psnCertType;

        /** Số giấy tờ */
        private String certno;

        /** Tên nhân viên */
        private String psnName;

        /** Giới tính */
        private String gend;

        /** Dân tộc */
        private String naty;

        /** Ngày sinh */
        private LocalDate brdy;

        /** Tuổi */
        private int age;

        /** người liên hệTên */
        private String conerName;

        /** Số điện thoại liên hệ */
        private String tel;

        /** Loại hình bảo hiểm */
        private String insutype;

        /** Loại nhân viên */
        private String psnType;

        /** y tếcứugiúpĐối tượngcờ */
        private String mafPsnFlag;

        /** Cờ công chức */
        private String cvlservFlag;

        /** linhsốngthìnghiệp vụcờ */
        private String flxempeFlag;

        /** mớitrẻ sơ sinhcờ */
        private String nwbFlag;

        /** tham gia bảo hiểmCơ sởBHYTQuậnkhấu */
        private String insuOptins;

        /** Tên đơn vị */
        private String empName;

        /** Thời gian bắt đầu */
        private LocalDateTime begntime;

        /** Thời gian kết thúc */
        private LocalDateTime endtime;

        /** Loại giấy tờ khám bệnh */
        private String mdtrtCertType;

        /** Loại hình y tế */
        private String medType;

        /** vượtNămđộNhập việncờ */
        private String arsYearIptFlag;

        /** trước dòngthanh toáncờ */
        private String prePayFlag;

        /** Nhập viện/Số khám ngoại trú */
        private String iptOtpNo;

        /** bệnh ánsố */
        private String medrcdno;

        /** chínhđiều trịBác sĩMã */
        private String atddrNo;

        /** chẩn đoán chínhBác sĩTên */
        private String chfpdrName;

        /** nhập việnKhoaMã */
        private String admDeptCodg;

        /** nhập việnTên khoa */
        private String admDeptName;

        /** nhập việngiườngvị */
        private String admBed;

        /** Nhập việnchínhMã chẩn đoán */
        private String dscgMaindiagCode;

        /** Nhập việnchínhTên chẩn đoán */
        private String dscgMaindiagName;

        /** xuất việnKhoaMã */
        private String dscgDeptCodg;

        /** xuất việnTên khoa */
        private String dscgDeptName;

        /** xuất việngiườngvị */
        private String dscgBed;

        /** xuất việncách */
        private String dscgWay;

        /** Mô tả chính về tình trạng bệnh */
        private String mainCondDscr;

        /** Mã loại bệnh */
        private String diseCodg;

        /** Tên loại bệnh */
        private String diseName;

        /** Phẫu thuậtthao tácmã */
        private String oprnOprtCode;

        /** Phẫu thuậtthao tácTên */
        private String oprnOprtName;

        /** ngoại trúThông tin chẩn đoán */
        private String otpDiagInfo;

        /** tạiviệnTrạng thái */
        private String inhospStas;

        /** chếtvongNgày */
        private java.util.Date dieDate;

        /** Nhập viện ngàysố */
        private int iptDays;

        /** Số giấy chứng nhận dịch vụ kế hoạch hóa gia đình */
        private String fpscNo;

        /** sinhsinhLớploại */
        private String matnType;

        /** kế hoạchsinhsinhPhẫu thuậtLớploại */
        private String birctrlType;

        /** muộnsinhcờ */
        private String latechbFlag;

        /** thaituầnsố */
        private int gesoVal;

        /** thai lần */
        private int fetts;

        /** thaitrẻsố */
        private int fetusCnt;

        /** sớmsảncờ */
        private String pretFlag;

        /** kế hoạchsinhsinhPhẫu thuật hoặc sinhsinhNgày */
        private LocalDateTime birctrlMatnDate;

        /** Cờ kèm biến chứng */
        private String copFlag;

        /** Người thực hiện ID */
        private String opterId;

        /** Tên người thực hiện */
        private String opterName;

        /** qualàmThời gian */
        private java.util.Date optTime;

        /** Ghi chú */
        private String memo;


}
