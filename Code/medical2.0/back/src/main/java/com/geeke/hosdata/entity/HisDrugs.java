package com.geeke.hosdata.entity;

import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.sys.entity.DictItem;
import lombok.Data;

import java.io.Serializable;

//bản việnhisThuốc
@Data
public class HisDrugs extends DataEntity<HisDrugs> implements Serializable {

    private String goodsName;		// Tên thuốc
    private String companyId;      // Phòng khámid
    private String brandName;		// hàng hóaTên khác
    private String type;      // Loại thuốc  01: Thuốc tây  03: Thuốc đông y  0002000066 :Thuốc thành phẩm đông y
    private String code;		// Mã thuốc
    private String source;		// Nguồn thuốc
    private String factory;		// sinhsảnĐơn vị
    private String pinyinCode; // Pinyin
    private String price;      //Giá
    private String dosis;     // Liều lượng
    private String dosisUnit; //Đơn vị liều lượng
    private String preparation; //Chế phẩm
    private String preparationUnit; //Đơn vị bào chế
    private String pack; //Đơn vị đóng gói
    private String standardCode; //Số đăng ký thuốc quốc gia
    private String insuranceCode; //Mã BHYT
    private String status; //Trạng thái






}
