package com.geeke.taskmanagement.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.geeke.admin.entity.User;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;

import java.util.Date;
/**
 * bài viếtQuản lýEntity
 * @author txl
 * @version 2023-02-09
 */


public class article extends DataEntity<article>{
    private static final long serialVersionUID = 1014474470772900019L;
    /** Định danh phòng khám */
    private String companyid;
    /** Định danh người dùng */
    private String userid ;
    /** Thời gian đăng */
    private Date aricledate ;
    /** Tiêu đề */
    private String title ;
    /** Tóm tắt */
    private String abstracts;
    /** Nội dung chính */
    private String content ;
    /** Tác giả nguồn */
    private String sources ;
    /** Liên kết gốc */
    private String originallink ;

    /** Người duyệt */
    private String auditor ;

    /** Thời gian xét duyệt */
    private Date auditordate ;
    /** Tên người phát hành */
    private String username ;
    /** Trạng thái xét duyệt(0: Chờ xét duyệt;1: Qua 2: Không đạt) */
    private String auditstatus ;
    /** Có bật không(0:  có ; 1:  không ) */
    private String isLocked ;

    private Company company;

    private User user;

    public article() {
        super();
    }

    public article(String id){
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
    /** Định danh người đăng */
    public String getUserid(){
        return this.userid;
    }
    /** Định danh người đăng */
    public void setUserid(String userid){
        this.userid=userid;
    }
    /** Thời gian đăng */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getAricledate(){
        return this.aricledate;
    }
    /** Thời gian đăng */
    public void setAricledate(Date aricledate){
        this.aricledate=aricledate;
    }
    /** Tiêu đề */
    public String getTitle(){
        return this.title;
    }
    /** Tiêu đề */
    public void setTitle(String title){
        this.title=title;
    }
    /** Tóm tắt */
    public String getAbstracts(){
        return this.abstracts;
    }
    /** Tóm tắt */
    public void setAbstracts(String abstracts){
        this.abstracts=abstracts;
    }
    /** Nội dung chính */
    public String getContent(){
        return this.content;
    }
    /** Nội dung chính */
    public void setContent(String content){
        this.content=content;
    }
    /** Tác giả nguồn */
    public String getSources(){
        return this.sources;
    }
    /** Tác giả nguồn */
    public void setSources(String sources){
        this.sources=sources;
    }
    /** Liên kết gốc */
    public String getOriginallink(){
        return this.originallink;
    }
    /** Liên kết gốc */
    public void setOriginallink(String originallink){
        this.originallink=originallink;
    }
    /** Người duyệt */
    public String getAuditor(){
        return this.auditor;
    }
    /** Người duyệt */
    public void setAuditor(String auditor){
        this.auditor=auditor;
    }
    /** Thời gian xét duyệt */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getAuditordate(){
        return this.auditordate;
    }
    /** Thời gian xét duyệt */
    public void setAuditordate(Date auditordate){
        this.auditordate=auditordate;
    }
    /** Tên người phát hành */
    public String getUsername(){
        return this.username;
    }
    /** Tên người phát hành */
    public void setUsername(String username){
        this.username=username;
    }
    /** Trạng thái xét duyệt(0: Chờ xét duyệt;1: Qua 2: Không đạt) */
    public String getAuditstatus(){
        return this.auditstatus;
    }
    /** Trạng thái xét duyệt(0: Chờ xét duyệt;1: Qua 2: Không đạt) */
    public void setAuditstatus(String auditstatus){
        this.auditstatus=auditstatus;
    }
    /** Có bật không(0:  có ; 1:  không ) */
    public String getIsLocked(){
        return this.isLocked;
    }
    /** Có bật không(0:  có ; 1:  không ) */
    public void setIsLocked(String isLocked){
        this.isLocked=isLocked;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
