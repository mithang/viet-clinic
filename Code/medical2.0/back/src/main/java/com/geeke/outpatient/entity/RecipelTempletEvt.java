package com.geeke.outpatient.entity;

import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.Stuff;
import com.geeke.sys.entity.DictItem;
import com.geeke.treatment.entity.CostItem;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Chi tiết đơn thuốcEntity
 * @author txl
 * @version 2022-06-07
 */
@Data
public class RecipelTempletEvt implements Serializable {

	private static final long serialVersionUID = 1014474470772900077L;
	//Bệnh việnTên
	private String clinicName;
	//Đơn thuốcký
    private String recipelSign = "Đơn thuốcgiấy";
	//Tiểu loại đơn thuốc
    private String smallType;
    //Khoa
    private String office;
    //Số đơn thuốc
    private String code;
	//Họ tên
	private String name;
	//Giới tính
	private String gender;
    //lập đơnNgày
    private String billDate;
	//Tuổi
	private String age;
	//Loại phí
	private String feeType;
	//Ngày phát thuốc
    private String dispenseDate;
    //Phát thuốc
    private String dispense = "Phát thuốc: ";
    //Số CMND/CCCD
    private String idCard;
    //Chẩn đoán lâm sàng
    private String diagnose;
	//Thuốcđịnh danh
	private String stuffSign = " RP:";
	//Y lệnh
	private String entrust;
	// trong thuốccấu hình
    private String chineseMedicineUse;
    //Bác sĩ
    private String doctor;
    //Số tiền
    private String fee;
    //Dược sĩ
    private String apothecary;
    //Duyệt
    private String check;
    //Đối chiếu
    private String checkAgain ="Đối chiếu: ";
    //Phát thuốc
    private String dispensing;
    //Dược sĩ/士(Pha chế)
    private String deploy;
    //Lưu ý
    private String medicalRemark;
    //black
    private String black;
    //Khám lần đầu/Tái khám
    private String treatType;
}