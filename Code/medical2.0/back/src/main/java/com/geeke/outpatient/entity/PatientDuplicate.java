package com.geeke.outpatient.entity;

/**
 * Một bệnh nhân trùng khớp trả về cho prompt ở form đăng ký (G3).
 *
 * Đây là DTO thuần, KHÔNG phải entity map bảng: form đăng ký chỉ cần các
 * trường hiển thị + id để nạp lại, và không được để một trường phụ (như
 * matchType) lọt vào câu INSERT/UPDATE của bảng patient.
 * birthday là chuỗi yyyy-MM-dd đã chuẩn hóa theo GMT+7 — cột DATE đi qua
 * JDBC/Jackson ở GMT+7 (bài học G2), nên format ở đây, frontend chỉ tách
 * chuỗi để hiển thị dd/MM/yyyy, không parse thành Date (tránh lệch ngày).
 */
public class PatientDuplicate {

    private String id;
    private String name;
    private String phone;
    private String card;
    private String genderValue;
    private String genderName;
    private String age;
    private String month;
    private String birthday;
    private String healthCardNo;
    private String province;
    private String city;
    private String area;
    private String address;
    private String guardianName;
    private String guardianPhone;
    /** phone | card | both — khớp theo SĐT trước, rồi CMND/CCCD. */
    private String matchType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getGenderValue() {
        return genderValue;
    }

    public void setGenderValue(String genderValue) {
        this.genderValue = genderValue;
    }

    public String getGenderName() {
        return genderName;
    }

    public void setGenderName(String genderName) {
        this.genderName = genderName;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getHealthCardNo() {
        return healthCardNo;
    }

    public void setHealthCardNo(String healthCardNo) {
        this.healthCardNo = healthCardNo;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }
}
