package com.geeke.medicareutils.domain.respo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Chi phíChi tiếtXuất
 */
@Data
public class MdFeeDetail {
    private String feedetlSn; // Chi phíChi tiếtsố thứ tự
    private BigDecimal detItemFeeSumamt; // Chi tiếthạng mụcChi phítổngsố tiền
    private BigDecimal cnt; // Số lượng
    private BigDecimal pric; // Đơn giá
    private BigDecimal pricUplmtAmt; // chỉ địnhgiátrêngiới hạnSố tiền
    private BigDecimal selfpayProp; // tự thangso vớiví dụ
    private BigDecimal fulamtOwnpayAmt; // toàn bộtựphíSố tiền
    private BigDecimal overlmtAmt; // vượtgiới hạngiáSố tiền
    private BigDecimal preselfpayAmt; // trước dòngtự thangSố tiền
    private BigDecimal inscpScpAmt; // Số tiền phù hợp phạm vi chính sách
    private String chrgitmLv; // Hạng mục thu phí v.v. cấp
    private String medChrgitmType; // y tếHạng mục thu phíLớploại
    private String basMednFlag; // Cờ thuốc cơ bản
    private String hiNegoDrugFlag; // BHYT谈判Thuốccờ
    private String chldMedcFlag; // 儿童dùngthuốccờ
    private String listSpItemFlag; // Cờ hạng mục đặc biệt trong danh mục
    private String lmtUsedFlag; // giới hạnsử dụngcờ
    private String drtReimFlag; // 直báocờ
    private String memo; // Ghi chú
    private String expContent; // Trườngmở rộng
}
