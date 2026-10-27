package com.geeke.admin.entity;

import com.geeke.common.persistence.DataEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Người dùngmở rộngEntity
 * @author ch
 * @version 2021-08-25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserExt extends DataEntity<UserExt>{

    private static final long serialVersionUID = 1L;
    private String userId;		// Người dùngid
    private String companyId;		// Phòng khámid
    private String photoId;		// Tải lênảnhid
    private String nation;		// Dân tộc
    private String sex;			// Giới tính
    private String birthday;		// sinhNgày
    private String isDuty;		// Có hay khôngtạichức vụ(0 không 1 có )
    private String creditType;		// Loại giấy tờ
    private String creditNum;		// Số giấy tờ
    private String post;		// chức vụvị
    private String office;		// Tên khoa
    private String officeCode;		// Mã khoa
    private String jobType;		// Nghề nghiệpLoại
    private String description;		// Mô tảThông tin
    private String startWorkTime;		// Phòng khámBắt đầucông việclàmThời gian
    private String endWorkTime;		// Phòng khámThời gian kết thúc
    //Trường BHYT
     private String pracPsnCode;//nhân viên hành nghềMã

}
