package com.geeke.outpatient.entity;


import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.geeke.common.persistence.DataEntity;


import java.util.Date;
/**
 * xếp lịchBảng chínhThông tinEntity
 * @author txl
 * @version 2022-07-20
 */

public class Scheduling extends DataEntity<Scheduling> {

    private static final long serialVersionUID = 1014474470772900019L;
    /** Định danh phòng khám */
    private String companyid;
    /** Định danh người dùng */
    private String userid ;
    /** xếp lịchNgày */
    private Date schedulingtime ;
    /** Khóa chính của bảng cấu hình thời gian xếp lịch */
    private String  timeid ;

    /** Có bật không(0:  có ; 1:  không ) */
    private String isLocked ;



    public Scheduling() {
        super();
    }

    public Scheduling(String id){
        super(id);
    }
    /** Định danh phòng khám */
    public String getCompanyid() {
        return companyid;
    }
    /** Định danh phòng khám */
    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }
    /** Định danh người dùng */
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
    /** xếp lịchNgày */
    @JsonFormat(pattern = "yyyy-MM-dd")
    public Date getSchedulingtime(){
        return this.schedulingtime;
    }
    public void setSchedulingtime(Date schedulingtime){
        this.schedulingtime=schedulingtime;
    }
    /** Khóa chính của bảng cấu hình thời gian xếp lịch */
    public String  getTimeid(){
        return this.timeid;
    }
    /** Khóa chính của bảng cấu hình thời gian xếp lịch */
    public void setTimeid(String timeid){
        this.timeid=timeid;
    }
    /** Có bật không(0:  có ; 1:  không ) */
    public String getIsLocked(){
        return this.isLocked;
    }
    /** Có bật không(0:  có ; 1:  không ) */
    public void setIsLocked(String isLocked){
        this.isLocked=isLocked;
    }
}
