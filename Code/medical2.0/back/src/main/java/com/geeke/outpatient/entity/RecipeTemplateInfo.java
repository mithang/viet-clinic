package com.geeke.outpatient.entity;

import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.sys.entity.DictItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class RecipeTemplateInfo extends DataEntity<RecipeTemplateInfo> {
    private String id;
    private Company company;
    private Recipetemplate recipetemplate;
    private String dosage;		// Liều lượng
    private DictItem recipelUse;      // Cách dùng
    private DictItem frequency;      // Tần suất và liều dùng
    private DictItem takeFrequency;      // dùngdùngtần suất lần
    private String singleDosage;		// Liều dùng mỗi lần
    private BigDecimal fee;		// Đơn thuốctổng giá
    private String remaks;     //Thông tin ghi chú
    private DictItem smallType;   //Tiểu loại đơn thuốc
    private String entrust;     //Y lệnh
//    private String createBy;   //Người tạo
//    private Date createDate;   //Thời gian tạo
//    private String updateBy;   //Sửangười
//    private Date updateDate;   //SửaThời gian
   // private String delFlag;   //Xóacờ
    private int seq;         //thứ tự
}
