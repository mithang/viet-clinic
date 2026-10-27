package com.geeke.taskmanagement.entity;

import com.geeke.org.entity.Company;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class taskmanagement {
    private  String ID;
    /** Mã tenant */
    private String companyId ;

    private Company company;
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
    private  String tasktype;//'tác vụLoại;tasktype_0: Tác vụ thông thường, tasktype_1: 宣truyềnsống动' ,
    private  String taskname;
    private  String taskdescribe;
    private  String taskaccessory;
    private  String taskinitiator;
    private  Date taskdeadline;
    private String taskstatus;//'tác vụTrạng thái;0: Không đạt; 1: Qua
    private String auditresult;//'xét duyệtKết quả;auditresult_0: Qua; auditresult_1: ChưaQua' ,
    private String taskremark;
    private String taskinitiatorname;//tác vụphátbắt đầungười


/*
hành nghề dòngngười
 */
    private List<String> taskregion;

    private List<taskexecutor> taskexecutors;
}
