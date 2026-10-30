package com.geeke.toll.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;

import java.math.BigDecimal;

/**
 * Audit hủy/hoàn hóa đơn (blocker 6).
 * Một dòng ghi lại một lần hoàn phí: hóa đơn hoàn nào (toll_id), đơn thuốc
 * nào (recipel_id), bệnh nhân nào, số tiền bao nhiêu, phương thức nào, và
 * người thao tác + thời điểm lấy từ create_by/create_date của DataEntity
 * (preInsert ghi "Tên(loginName)" từ session, đúng quy ước các bảng khác).
 * Bảng này chỉ ghi thêm (append-only theo nghiệp vụ): không có luồng nào
 * sửa/xóa dòng audit.
 */
public class TollAuditLog extends DataEntity<TollAuditLog> {

    private static final long serialVersionUID = 1L;

    private Company company;            // Phòng khám
    private String tollId;              // id dòng toll_info hoàn phí vừa tạo
    private String recipelId;           // id đơn thuốc được hoàn phí
    private String patientId;           // id bệnh nhân
    private String action;              // REFUND: hoàn phí; CANCEL: hủy (dự phòng)
    private BigDecimal amountReceivable; // số tiền phải thu (hoàn lại)
    private BigDecimal amountReceived;   // số tiền thực thu (hoàn lại)
    private String paymentType;          // vd payType_0
    private String amountStatus;         // vd amountStatus_2
    private String remarks;

    public TollAuditLog() {
        super();
    }

    public TollAuditLog(String id) {
        super(id);
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getTollId() {
        return tollId;
    }

    public void setTollId(String tollId) {
        this.tollId = tollId;
    }

    public String getRecipelId() {
        return recipelId;
    }

    public void setRecipelId(String recipelId) {
        this.recipelId = recipelId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public BigDecimal getAmountReceivable() {
        return amountReceivable;
    }

    public void setAmountReceivable(BigDecimal amountReceivable) {
        this.amountReceivable = amountReceivable;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(BigDecimal amountReceived) {
        this.amountReceived = amountReceived;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getAmountStatus() {
        return amountStatus;
    }

    public void setAmountStatus(String amountStatus) {
        this.amountStatus = amountStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Lấy tên bảng tương ứng của thực thể
     */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "toll_audit_log";
    }
}
