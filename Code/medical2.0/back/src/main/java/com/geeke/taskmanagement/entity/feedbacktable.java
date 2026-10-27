package com.geeke.taskmanagement.entity;

import lombok.Data;

import java.util.Date;

@Data
public class feedbacktable {
    private String id;
    /** Mã tenant */
    private String companyId ;
    /** Cờ xóa;(0: Bình thường; 1: Xóa) */
    private String delFlag ;
    /** Người tạo */
    private String createdBy ;
    /** Thời gian tạo */
    private Date createdTime ;
    /** Người cập nhật */
    private String updatedBy ;
    /** Thời gian cập nhật */
    private Date updatedTime ;
    private String taskmanagementid;
    private String executeschedule;//'Tiến độ thực hiện;0: Đãhoàn thành, 1: tiến hành dòng trong '
    private String picture;
    private String remark;
    private String fileid;
    private String auditType;
}
