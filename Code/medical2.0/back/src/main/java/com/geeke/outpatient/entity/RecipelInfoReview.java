package com.geeke.outpatient.entity;

import com.geeke.common.persistence.DataEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Đơn thuốcThông tinxem xétEntity
 * @author lc
 * @version 2023-10-09
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RecipelInfoReview extends DataEntity<RecipelInfoReview> {
    private RecipelInfo recipelInfo; //Thông tin đơn thuốc
    private MedicalRecord medicalRecord; //ca bệnhThông tin
    private Integer reviewResult = 2; // xem xétKết quả: 1cho biếthợplý, 0cho biếtkhônghợplý
    private String reviewContent = ""; // xem xétnội dung
    private String reviewerName = ""; // xem xétngười của tênchữ,xem xétThời gianlấycreateDate
    private Integer reviewStatus = 0; // xem xétTrạng thái: 0cho biếtChưaxem xét, 1cho biếtĐãxem xét ;2Đãhủy
    private String reviewerId ; //xem xétngườiID


    public MedicalRecord getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(MedicalRecord medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    public RecipelInfo getRecipelInfo() {
        return recipelInfo;
    }

    public void setRecipelInfo(RecipelInfo recipelInfo) {
        this.recipelInfo = recipelInfo;
    }

    public Integer getReviewResult() {
        return reviewResult;
    }

    public void setReviewResult(Integer reviewResult) {
        this.reviewResult = reviewResult;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public Integer getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(Integer reviewStatus) {
        this.reviewStatus = reviewStatus;
    }
}
