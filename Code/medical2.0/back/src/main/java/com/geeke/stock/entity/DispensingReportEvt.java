package com.geeke.stock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.outpatient.entity.RecipelInfo;
import com.geeke.outpatient.entity.Registration;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Chi tiết phát thuốcEntity
 * @author txl
 * @version 2022-08-11
 */
@Data
public class DispensingReportEvt {

	private static final long serialVersionUID = 1025152944882663511L;

	//Đơn thuốcđơnsố
	private String recipelCode;
	//Loại
    private String type;
	//Có hay khôngbán lẻ
    private String isUnpackSell;
    //Tên
    private String name;
    //Quy cách
    private String norms;
    //Số lượng
    private String number;
    //Chế phẩm
    private String preparation;
    //Đơn vị
    private String unit;
    //Đơn vị đóng gói
    private String pack;
    //giá nhậpđơn giá
    private BigDecimal bid;
    //giá nhậpđóng góiGiá
    private BigDecimal packageBid;
    //giá nhậptổng giá
    private BigDecimal bidTotal;
    //bán hàngđơn giá
    private BigDecimal price;
    //bán hàngđóng góigiá
    private BigDecimal packagePrice;
    //bán hàngtổng giá
    private BigDecimal priceTotal;
    //利润
    private BigDecimal profit;
    //Nhà sản xuất
    private String factory;
    //Số lô sản xuất
    private String batchNo;
    //hết hiệu lựcNgày
    private Date endDate;
    //Ngày phát thuốc
    private Date dispensingDate;

    private Company company;
}