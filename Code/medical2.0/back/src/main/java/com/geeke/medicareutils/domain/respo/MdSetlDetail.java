package com.geeke.medicareutils.domain.respo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MdSetlDetail {
    private String fundPayType; // thanh toán từ quỹLoại
    private BigDecimal inscpScpAmt; // Số tiền phù hợp phạm vi chính sách
    private BigDecimal crtPayblAmt; // này lầncó thểthanh toángiới hạnsố tiềnSố tiền
    private BigDecimal fundPayAmt; // thanh toán từ quỹSố tiền
    private String fundPayTypeName; // thanh toán từ quỹLoạiTên
    private String setlProcInfo; // Quyết toánquaquy trìnhThông tin
}
