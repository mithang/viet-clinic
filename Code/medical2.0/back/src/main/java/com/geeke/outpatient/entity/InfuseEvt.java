package com.geeke.outpatient.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * Chi tiết đơn thuốcEntity
 * @author txl
 * @version 2022-06-07
 */
@Data
public class InfuseEvt implements Serializable {

	private static final long serialVersionUID = 1014474470772900079L;
	//Tên
	private String name;
	//Liều dùng mỗi lần
    private String singleDosage;
	//Số ngày
	private String days;
	//Tổng cộngSố lượng
	private String totalNum;
	//Test da
	private String skinTest;
	//Đơn giá
	private String price;
	//Phí vật tư
	private String stuffPriceName;
	//Phí vật tưSố tiền
	private String stuffPrice;
}