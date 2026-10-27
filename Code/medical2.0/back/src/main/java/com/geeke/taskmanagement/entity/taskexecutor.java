package com.geeke.taskmanagement.entity;

import com.geeke.org.entity.Company;
import lombok.Data;

import java.util.Date;

@Data
public class taskexecutor {
    private String ID;
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
    private String clinic_id;
    private String userid;

    private Company company;
}
