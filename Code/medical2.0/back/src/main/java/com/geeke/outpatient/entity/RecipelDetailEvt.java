package com.geeke.outpatient.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Chi tiết đơn thuốcEntity
 * @author txl
 * @version 2022-06-07
 */
@Data
public class RecipelDetailEvt implements Serializable {

	private static final long serialVersionUID = 1014474470772900078L;
	//Tên
	private String name;
	//Quy cách
    private String norms;
	//Số lượng
	private String num;
	//Đơn giá
    private String price;
    //Số ngày
    private String days;
    //dùngphương phápđịnh danh
    private String useText = "Cách dùng:";
	//tần suất lần
	private String frequency;
	//Cách dùng
	private String use;
	//Ghi chú
	private String remarks;
	//Têntiêu đề
	private String nameTitle = "Tên";
	//Số lượngtiêu đề
	private String numTitle = "Số lượng";
	//Đơn vịtiêu đề
	private String unitTitle = "Đơn vị";
	//đơn giátiêu đề
	private String priceTitle = "Đơn giá";
	//Ghi chútiêu đề
	private String remarksTitle = "Ghi chú";
	//Phí vật tư
	private String stuffPriceName = "Phí vật tư";
	//Phí vật tưSố tiền
	private String stuffPrice;
}