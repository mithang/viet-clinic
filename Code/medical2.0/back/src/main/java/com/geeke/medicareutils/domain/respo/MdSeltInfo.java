package com.geeke.medicareutils.domain.respo;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class MdSeltInfo {
    private String mdtrtId; // Khám bệnh ID
    private String setlId; // Quyết toán ID
    private String psnNo; // Mã nhân viên
    private String psnName; // Tên nhân viên
    private String psnCertType; // Loại giấy tờ nhân viên
    private String certNo; // Số giấy tờ
    private String gend; // Giới tính
    private String naty; // Dân tộc
    private String brdy; // Ngày sinh
    private BigDecimal age; // Tuổi
    private String insutype; // Loại hình bảo hiểm
    private String psnType; // Loại nhân viên
    private String cvlservFlag; // Cờ công chức
    private String setlTime; // Quyết toánThời gian
    private String mdtrtCertType; // Loại giấy tờ khám bệnh
    private String medType; // Loại hình y tế
    private BigDecimal medfeeSumamt; // y tếphítổngsố tiền
    private BigDecimal fulamtOwnpayAmt; // toàn bộtựphíSố tiền
    private BigDecimal overlmtSelfpay; // Vượt giá giới hạn thì tự trảChi phí
    private BigDecimal preselfpayAmt; // trước dòngtự thangSố tiền
    private BigDecimal inscpScpAmt; // Số tiền phù hợp phạm vi chính sách
    private BigDecimal actPayDedc; // Mức khởi điểm thanh toán thực tế
    private BigDecimal hifpPay; // Quỹ điều phối BHYT cơ bảnChi tiêu
    private BigDecimal poolPropSelfpay; // Tỷ lệ thanh toán của quỹ điều phối BHYT cơ bản
    private BigDecimal cvlservPay; // Quỹ trợ cấp y tế cho công chứcChi tiêu
    private BigDecimal hifesPay; // Quỹ BHYT bổ sung của doanh nghiệpChi tiêu
    private BigDecimal hifmiPay; // Quỹ bảo hiểm bệnh nặng cho dân cưChi tiêu
    private BigDecimal hifobPay; // chức vụcông việclớnsố tiềny tếChi phíbùgiúpcơ sởquỹChi tiêu
    private BigDecimal mafPay; // y tếcứugiúpcơ sởquỹChi tiêu
    private BigDecimal othPay; // khácChi tiêu
    private BigDecimal fundPaySumAmt; // thanh toán từ quỹtổngsố tiền
    private BigDecimal psnPartAmt; // Cá nhânâmgánhtổngSố tiền
    private BigDecimal acctPay; // Cá nhântài khoảnChi tiêu
    private BigDecimal psnCashPay; // Cá nhântiền mặtChi tiêu
    private BigDecimal hospPartAmt; // Bệnh việnâmgánhSố tiền
    private BigDecimal balc; // Số dư
    private BigDecimal acctMulaidPay; // Cá nhântài khoảnTổng trợ giúpthanh toánSố tiền
    private String medinsSetlId; // y dượcCơ sởQuyết toán ID
    private String clrOptins; // thanh toánqualàmCơ sở
    private String clrWay; // thanh toáncách
    private String clrType; // thanh toánLớploại
}
