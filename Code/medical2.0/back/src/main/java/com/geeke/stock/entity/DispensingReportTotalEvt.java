package com.geeke.stock.entity;

import com.geeke.common.data.Page;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Chi tiết phát thuốcEntity
 * @author txl
 * @version 2022-08-11
 */
@Data
public class DispensingReportTotalEvt {

	private static final long serialVersionUID = 1025152944882663512L;

	//Số lượngTổng cộng
	private String numberAmount;
    //giá nhậptổng giáTổng cộng
    private String bidTotalAmount;
	//bán hàngtổng giáTổng cộng
    private String priceTotalAmount;
    //lợi nhuậnTổng cộng
    private String profitAmount;
}