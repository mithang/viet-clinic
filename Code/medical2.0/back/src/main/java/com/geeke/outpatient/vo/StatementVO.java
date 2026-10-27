package com.geeke.outpatient.vo;

import java.io.Serializable;
/**
 * Đơn thuốcxem xéttổng hợptổngVO
 */
public class StatementVO implements Serializable {
    private String company;
    private String clinicOffice;
    private String doctor;
    /**
     *đánh giáĐơn thuốcsố
     */
    private Long comment;
    /**
     *đạt chuẩnĐơn thuốcsố
     */
    private Long rational;
    /**
     *Đơn thuốcđạt chuẩntỷ lệ
     */
    private Long rate;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getClinicOffice() {
        return clinicOffice;
    }

    public void setClinicOffice(String clinicOffice) {
        this.clinicOffice = clinicOffice;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public Long getComment() {
        return comment;
    }

    public void setComment(Long comment) {
        this.comment = comment;
    }

    public Long getRational() {
        return rational;
    }

    public void setRational(Long rational) {
        this.rational = rational;
    }

    public Long getRate() {
        return rate;
    }

    public void setRate(Long rate) {
        this.rate = rate;
    }
}
