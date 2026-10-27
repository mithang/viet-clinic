package com.geeke.outpatient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * Bảng thông tin BHYT của bệnh nhân
 * @TableName patient_md_data
 */
@TableName(value ="patient_md_data")
@Data
public class PatientMdData implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;


    /**
     * nhóm织id
     */
    private Long OrganizeId;

    /**
     * Bệnh nhânid
     */
    private Long patientId;

    /**
     * Mã nhân viên
     */
    private String psnNo;

    /**
     * Loại giấy tờ
     */
    private String psnCertType;

    /**
     * Số giấy tờ
     */
    private String certno;

    /**
     * Tên nhân viên
     */
    private String psnName;

    /**
     * Giới tính
     */
    private String gend;

    /**
     * Dân tộc
     */
    private String naty;

    /**
     * Ngày sinh
     */
    private String brdy;

    /**
     * Tuổi
     */
    private Integer age;

    /**
     * Trườngmở rộng
     */
    private String expcontent;

    /**
     * Số dư
     */
    private BigDecimal balc;

    /**
     * Loại hình bảo hiểm
     */
    private String insutype;

    /**
     * Loại nhân viên
     */
    private String psnType;

    /**
     * nhân viêntham gia bảo hiểmTrạng thái
     */
    private String psnInsuStas;

    /**
     * Cá nhântham gia bảo hiểmNgày
     */
    private String psnInsuDate;

    /**
     * tạmdừngtham gia bảo hiểmNgày
     */
    private String pausInsuDate;

    /**
     * Cờ công chức
     */
    private String cvlservFlag;

    /**
     * Vùng BHYT nơi tham gia bảo hiểm
     */
    private String insuplcAdmdvs;

    /**
     * Tên đơn vị
     */
    private String empName;

    /**
     * chế độKiểm traLoại
     */
    private String trtChkType;

    /**
     * thanh toán từ quỹLoại
     */
    private String fundPayType;

    /**
     * Cờ hưởng chế độ khoản quỹ
     */
    private String trtEnjymntFlag;

    /**
     * nhân viênchế độNgày bắt đầu
     */
    private Date trtBegndate;

    /**
     * ngườigốcchế độNgày kết thúc
     */
    private Date trtEnddate;

    /**
     * chế độKiểm traKết quả
     */
    private String trtChkRslt;

    /**
     * Trườngmở rộng
     */
    private String trtExpcontent;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}