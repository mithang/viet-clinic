package com.geeke.toll.entity;

import com.geeke.common.data.Page;
import com.geeke.outpatient.entity.RecipelInfoEvt;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * LC
 * tổng hợptổngbáo cáoratham
 */
@Data
public class TollVo implements Serializable {

    private static final long serialVersionUID = 1024846610635145503L;

    private Page page;
    private BigDecimal amountReceivableTotal;		// phải thu(trả)Số tiềnTổng cộng
    private BigDecimal amountReceivedTotal;		// thực thuSố tiềnTổng cộng
    private BigDecimal cashTotal;		// tiền mặtthanh toánTổng cộng
    private BigDecimal weChatTotal;		// WeChatthanh toánTổng cộng
    private BigDecimal alipayTotal;		// thanh toánbảothanh toánTổng cộng
    private BigDecimal bankCardPayTotal;		// ngân hàng dòngthẻthanh toánTổng cộng
    private BigDecimal ybTotal;		// BHYTthanh toánTổng cộng (chỉ dữ liệu lịch sử)
    private BigDecimal transferTotal;		// Chuyển khoản ngân hàngTổng cộng (payType_5, thêm 2026-10-30)
    private BigDecimal ewalletTotal;		// Ví điện tửTổng cộng (payType_6, thêm 2026-10-30)
}
