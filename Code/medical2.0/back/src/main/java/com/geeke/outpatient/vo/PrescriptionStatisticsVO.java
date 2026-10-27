package com.geeke.outpatient.vo;

import java.io.Serializable;

/**
 * Đơn thuốcxem xétthống kêbảng-Đơn thuốcthống kê
 */
public class PrescriptionStatisticsVO implements Serializable {
    /**
     * Đơn thuốctổng số
     */
    private String total;
    /**
     * Đãphát thuốcsố
     */
    private String dispensedMedicine;
    /**
     * Chưaphát thuốcsố
     */
    private String undispensedMedicine;
    /**
     * Đãđánh giásố
     */
    private String alreadyReviewed;
    /**
     * đạt chuẩnsố
     */
    private String good;
    /**
     * khôngđạt chuẩnsố
     */
    private String bad;
    /**
     * đạt chuẩntỷ lệ
     */
    private String rate;

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getDispensedMedicine() {
        return dispensedMedicine;
    }

    public void setDispensedMedicine(String dispensedMedicine) {
        this.dispensedMedicine = dispensedMedicine;
    }

    public String getUndispensedMedicine() {
        return undispensedMedicine;
    }

    public void setUndispensedMedicine(String undispensedMedicine) {
        this.undispensedMedicine = undispensedMedicine;
    }

    public String getAlreadyReviewed() {
        return alreadyReviewed;
    }

    public void setAlreadyReviewed(String alreadyReviewed) {
        this.alreadyReviewed = alreadyReviewed;
    }

    public String getGood() {
        return good;
    }

    public void setGood(String good) {
        this.good = good;
    }

    public String getBad() {
        return bad;
    }

    public void setBad(String bad) {
        this.bad = bad;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
