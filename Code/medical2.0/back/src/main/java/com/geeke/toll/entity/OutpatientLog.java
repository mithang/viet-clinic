package com.geeke.toll.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.geeke.common.data.Page;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author zh
 */
@Data
public class OutpatientLog {

    private Page page;

    /** ID */
    private String id;

    /** Tên bệnh nhân */
    private String patientName;

    /**Tên phụ huynh */
    private String patriarchName;

    /** Giới tính */
    private String sex;

    /** Tuổi bệnh nhân */
    private String age;

    /** Dân tộc */
    private String nation;

    /** Nghề nghiệp */
    private String occupation;

    /** Địa chỉ chi tiết */
    private String address;

    /** Ngày khám bệnh */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
/*
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd'T'HH:mm:ss")
*/
    private Date visitDate;
    /** ban đầu/Tái khám */
    private String initialVisit;

    /** Huyết áp */
    private String bloodPressure;

    /** Triệu chứng lâm sàng */
    private String symptom;

    /** Sốt, nhiệt độ cơ thể tăng */
    private String fever;

     /** Tiền sử dịch tễ */
    private String epidemicDisease;

    /** Chẩn đoán tây y */
    private String westernDiagnose;

    /** Chẩn đoán đông y */
    private String chinaDiagnose;

    /** Bệnh truyền nhiễm */
    private String infect;

    /** Tình hình xử lý */
    private String handle;

    /** Số giấy tờ hiệu lực */
    private String certificate;

    /** Đơn vị công tác */
    private String unit;

    /** Chữ ký bác sĩ */
    private String signature;

    /** Cờ hộ nghèo */
    private String poverty;

    /** Số điện thoại liên hệ */
    private String telephone;

    /** Ngày phát bệnh */
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date morbidityDate;

    /** Kết quả dương tính trong phòng thí nghiệm */
    private String positiveResult;

    /** Giáo dục sức khỏe cá thể hóa */
    private String healthEducation;

    public OutpatientLog() {
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatriarchName() {
        return patriarchName;
    }

    public void setPatriarchName(String patriarchName) {
        this.patriarchName = patriarchName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getVisitDate() {
        return (Date) visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public String getInitialVisit() {
        return initialVisit;
    }

    public void setInitialVisit(String initialVisit) {
        this.initialVisit = initialVisit;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public String getSymptom() {
        return symptom;
    }

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }

    public String getFever() {
        return fever;
    }

    public void setFever(String fever) {
        this.fever = fever;
    }

    public String getEpidemicDisease() {
        return epidemicDisease;
    }

    public void setEpidemicDisease(String epidemicDisease) {
        this.epidemicDisease = epidemicDisease;
    }

    public String getWesternDiagnose() {
        return westernDiagnose;
    }

    public void setWesternDiagnose(String westernDiagnose) {
        this.westernDiagnose = westernDiagnose;
    }

    public String getChinaDiagnose() {
        return chinaDiagnose;
    }

    public void setChinaDiagnose(String chinaDiagnose) {
        this.chinaDiagnose = chinaDiagnose;
    }

    public String getInfect() {
        return infect;
    }

    public void setInfect(String infect) {
        this.infect = infect;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPoverty() {
        return poverty;
    }

    public void setPoverty(String poverty) {
        this.poverty = poverty;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Date getMorbidityDate() {
        return (Date) morbidityDate;
    }

    public void setMorbidityDate(Date morbidityDate) {
        this.morbidityDate = (Date) morbidityDate;
    }

    public String getPositiveResult() {
        return positiveResult;
    }

    public void setPositiveResult(String positiveResult) {
        this.positiveResult = positiveResult;
    }

    public String getHealthEducation() {
        return healthEducation;
    }

    public void setHealthEducation(String healthEducation) {
        this.healthEducation = healthEducation;
    }
}