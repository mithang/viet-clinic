package com.geeke.noticesend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * Gửi thông báo (noticesend/noticeSend).
 *
 * Module này trước đây chỉ tồn tại ở frontend (client/src/views/noticesend/
 * noticeSend + client/src/api/noticesend/noticeSend.js) mà không có
 * entity/controller/service/DAO hay bảng nào ở backend, nên
 * POST /noticesend/noticeSend/list và GET /noticesend/noticeSend/tree/2
 * đều 404. Module này là backend thật theo đúng pattern CrudController/
 * CrudService của dự án, lưu vào bảng notice_send (DB test local).
 *
 * Lưu ý về company: form frontend không gửi company. Company được lấy từ
 * tenant của phiên đăng nhập (SessionUtils.getLoginTenantId) — không tin
 * company do frontend gửi — để vừa qua được @NotNull của DataEntity, vừa
 * giữ cách ly tenant giống ensureCompanyFilter của CrudService (list chỉ
 * thấy company_id của tenant đang đăng nhập).
 */
public class NoticeSend extends DataEntity<NoticeSend> {

    private static final long serialVersionUID = 723822523350949890L;

    private Company company;
    private String senderId;
    private String title;
    private String isSite;
    private String isWechat;
    private String isEmail;
    private String isSms;
    private String siteContent;
    private String wechatContent;
    private String emailContent;
    private String smsContent;
    private String receivedBy;

    public NoticeSend() {
        super();
    }

    public NoticeSend(String id) {
        super(id);
    }

    @NotNull(message = "Phòng khámIDKhông được để trống")
    public Company getCompany() {
        if (company == null) {
            String tenantId = SessionUtils.getLoginTenantId();
            if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
                Company tenant = new Company();
                tenant.setId(tenantId);
                return tenant;
            }
        }
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    @Length(min = 1, max = 50, message = "Tiêu đềĐộ dài phải nằm trong khoảng 1 đến 50 ")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Length(min = 0, max = 1, message = "Có phải tin nhắn trong hệ thống khôngĐộ dài phải nằm trong khoảng 0 đến 1 ")
    public String getIsSite() {
        return isSite;
    }

    public void setIsSite(String isSite) {
        this.isSite = isSite;
    }

    @Length(min = 0, max = 1, message = "Có phải tin nhắn WeChat khôngĐộ dài phải nằm trong khoảng 0 đến 1 ")
    public String getIsWechat() {
        return isWechat;
    }

    public void setIsWechat(String isWechat) {
        this.isWechat = isWechat;
    }

    @Length(min = 0, max = 1, message = "Có phải thông báo email khôngĐộ dài phải nằm trong khoảng 0 đến 1 ")
    public String getIsEmail() {
        return isEmail;
    }

    public void setIsEmail(String isEmail) {
        this.isEmail = isEmail;
    }

    @Length(min = 0, max = 1, message = "Có gửi SMS khôngĐộ dài phải nằm trong khoảng 0 đến 1 ")
    public String getIsSms() {
        return isSms;
    }

    public void setIsSms(String isSms) {
        this.isSms = isSms;
    }

    public String getSiteContent() {
        return siteContent;
    }

    public void setSiteContent(String siteContent) {
        this.siteContent = siteContent;
    }

    public String getWechatContent() {
        return wechatContent;
    }

    public void setWechatContent(String wechatContent) {
        this.wechatContent = wechatContent;
    }

    public String getEmailContent() {
        return emailContent;
    }

    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }

    public String getSmsContent() {
        return smsContent;
    }

    public void setSmsContent(String smsContent) {
        this.smsContent = smsContent;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "notice_send";
    }

    @Override
    @JsonIgnore
    public String getBusTableId() {
        // ID router noticesend/noticeSend trong sys_router (DB test local).
        return "723822523350949890";
    }

    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
        return true;
    }
}
