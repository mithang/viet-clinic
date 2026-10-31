package com.geeke.outpatient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * Bảng danh mục y tế
 * @TableName pres_drug
 */
@TableName(value ="pres_drug")
@Data
public class PresDrug implements Serializable {
    /**
     * Mã danh mục y tế
     */
    @TableId
    private String Id;


    private String medlistcodg;

    /**
     * quốc giaThuốcmã số
     */
    private String natdrugno;

    /**
     * Tên chung
     */
    private String genname;

    /**
     * Tên hàng hóa
     */
    private String prodname;

    /**
     * đăng kýtên
     */
    private String regname;

    /**
     * danh mụcLớploại (101-Thuốc tây, 102-Thuốc đông y, 103-tựbào chế, 104-Dân tộcthuốc)
     */
    private String listtype;

    /**
     * danh mụcLớpTên khácgọi là
     */
    private String listtypename;

    /**
     * Quy cáchTên
     */
    private String specname;

    /**
     * Nhà sản xuất
     */
    private String prdrname;

    /**
     * Số phê duyệt
     */
    private String aprvno;

    /**
     * liềukiểuTên
     */
    private String dosformname;

    /**
     * Nhỏ nhấtĐơn vị đóng gói
     */
    private String minpacunt;

    /**
     * Nhỏ nhấtSố lượng đóng gói
     */
    private String minpaccnt;

    /**
     * Nhỏ nhấtĐơn vị bào chế
     */
    private String minprepunt;

    /**
     * Mã vùng điều phối
     */
    private String poolareano;

    /**
     * điều phốiQuậnTên
     */
    private String poolareaname;

    /**
     * Có hay khônghai kênhcờ (0 -  không , 1 -  có )
     */
    private String dualchnlflag;

    /**
     * Thời gian bắt đầu (Định dạng: yyyy-MM-dd HH:mm:ss)
     */
    private Date begntime;

    /**
     * Thời gian kết thúc (Định dạng: yyyy-MM-dd HH:mm:ss)
     */
    private Date endtime;


    /**
     * Thời gian kết thúc (Định dạng: yyyy-MM-dd HH:mm:ss)
     */
    private Float drugprice;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}