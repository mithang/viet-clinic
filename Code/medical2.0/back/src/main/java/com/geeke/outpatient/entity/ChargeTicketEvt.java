package com.geeke.outpatient.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * Chi tiết đơn thuốcEntity
 * @author txl
 * @version 2022-06-07
 */
@Data
public class ChargeTicketEvt implements Serializable {

	private static final long serialVersionUID = 1014474470772900060L;
	//Phòng khámTên
	private String clinicName;
	//Họ tên
	private String name;
	//Giới tính
	private String gender;
	//Tuổi
	private String age;
	//Số phiếu thu phí
	private String code;
	//Thu phíLớploại
    private String type;
	//Hạng mục thu phí
	private String project;
	//Người thu phí
    private String people;
    //Ngày khám bệnh
    private String date;
	//khám bệnhKhoa
	private String office;
	//Thu phíSố tiền
	private String fee;
	//phương thức thanh toánSố tiền
	private String feeType;
	//Lời nhắc thân thiện
	private String tip;
	//Đăng ký khámsố thứ tự
	private String num;
}