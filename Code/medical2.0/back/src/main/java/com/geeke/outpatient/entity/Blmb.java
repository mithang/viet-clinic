package com.geeke.outpatient.entity;

import com.geeke.common.persistence.DataEntity;
import lombok.Data;

import java.util.Date;

@Data
public class Blmb  {
    /** Mã mẫu */
    private String mbbm ;
    /** Mã tenant */
    private String companyId ;
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
    /** Tên mẫu */
    private String mbmc ;
    /** Loại mẫu;(0: Chung, 1: Cá nhân) */
    private String mblx ;
    /** Loại bệnh án;(0: Bệnh án Tây y, 1: Bệnh án Đông y) */
    private String bllx ;
    /** Lý do khám chính */
    private String zs ;
    /** Tiền sử cá nhân */
    private String grs ;
    /** Tiền sử dị ứng */
    private String gms ;
    /** Tiền sử bệnh */
    private String jbs ;
    /** Tiền sử bệnh truyền nhiễm */
    private String crbs ;
    /** Tiền sử phẫu thuật */
    private String sss ;
    /** Tiền sử truyền máu */
    private String sxs ;
    /** Khám thực thể */
    private String tgjc ;
    /** Chẩn đoán cấp cứu */
    private String jzzd ;
    /** Hiệu quả cấp cứu */
    private String jzxg ;
    /** Tiền sử gia đình */
    private String jzs ;
    /** Tiền sử kinh nguyệt */
    private String yjz ;
    /** Tiền sử hôn nhân và sinh đẻ */
    private String hys ;
    /** Kiểm tra hỗ trợ */
    private String fzjc ;
    /** Bệnh sử hiện tại */
    private String xbs ;
    /** Tiền sử */
    private String jws ;
    /** Kiểm tra khác */
    private String qtjc ;
    /** Tình hình xử lý */
    private String clqk ;
    /** Tiền sử dịch tễ */
    private String lxbxs ;
    /** Giáo dục sức khỏe cá thể hóa */
    private String gthjkjy ;
    /**
     * Người tạoID
     */
    private  String createdID;
    private  String cjr;
    /**
     * Nhiệt độ cơ thể
     */
    private String temperature;
    /**
     * mạch
     */
    private String pulse;
    /**
     * hô hấp
     */
    private String breathe;
    /**
     * Huyết áp
     */
    private String bloodPressure;
    /**
     * Chẩn đoán tây y
     */
    private String westernDiagnose;
    /**
     * Chẩn đoán đông y
     */
    private String chinaDiagnose;

    public String getCjr() {
        return cjr;
    }

    public void setCjr(String cjr) {
        this.cjr = cjr;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getPulse() {
        return pulse;
    }

    public void setPulse(String pulse) {
        this.pulse = pulse;
    }

    public String getBreathe() {
        return breathe;
    }

    public void setBreathe(String breathe) {
        this.breathe = breathe;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
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

    /** Mã mẫu */
    public String getMbbm(){
        return this.mbbm;
    }
    /** Mã mẫu */
    public void setMbbm(String mbbm){
        this.mbbm=mbbm;
    }
    /** Mã tenant */
    public String getCompanyId(){
        return this.companyId;
    }
    /** Mã tenant */
    public void setCompanyId(String companyId){
        this.companyId=companyId;
    }
    /** Cờ xóa;(0: Bình thường; 1: Xóa) */
    public String getDelFlag(){
        return this.delFlag;
    }
    /** Cờ xóa;(0: Bình thường; 1: Xóa) */
    public void setDelFlag(String delFlag){
        this.delFlag=delFlag;
    }
    /** Người tạo */
    public String getCreatedBy(){
        return this.createdBy;
    }
    /** Người tạo */
    public void setCreatedBy(String createdBy){
        this.createdBy=createdBy;
    }
    /** Thời gian tạo */
    public Date getCreatedTime(){
        return this.createdTime;
    }
    /** Thời gian tạo */
    public void setCreatedTime(Date createdTime){
        this.createdTime=createdTime;
    }
    /** Người cập nhật */
    public String getUpdatedBy(){
        return this.updatedBy;
    }
    /** Người cập nhật */
    public void setUpdatedBy(String updatedBy){
        this.updatedBy=updatedBy;
    }
    /** Thời gian cập nhật */
    public Date getUpdatedTime(){
        return this.updatedTime;
    }
  /** Thời gian cập nhật */
    public void setUpdatedTime(Date updatedTime){
        this.updatedTime=updatedTime;
    }
    /** Tên mẫu */
    public String getMbmc(){
        return this.mbmc;
    }
    /** Tên mẫu */
    public void setMbmc(String mbmc){
        this.mbmc=mbmc;
    }
    /** Loại mẫu;(0: Chung, 1: Cá nhân) */
    public String getMblx(){
        return this.mblx;
    }
    /** Loại mẫu;(0: Chung, 1: Cá nhân) */
    public void setMblx(String mblx){
        this.mblx=mblx;
    }
    /** Loại bệnh án;(0: Bệnh án Tây y, 1: Bệnh án Đông y) */
    public String getBllx(){
        return this.bllx;
    }
    /** Loại bệnh án;(0: Bệnh án Tây y, 1: Bệnh án Đông y) */
    public void setBllx(String bllx){
        this.bllx=bllx;
    }
    /** Lý do khám chính */
    public String getZs(){
        return this.zs;
    }
    /** Lý do khám chính */
    public void setZs(String zs){
        this.zs=zs;
    }
    /** Tiền sử cá nhân */
    public String getGrs(){
        return this.grs;
    }
    /** Tiền sử cá nhân */
    public void setGrs(String grs){
        this.grs=grs;
    }
    /** Tiền sử dị ứng */
    public String getGms(){
        return this.gms;
    }
    /** Tiền sử dị ứng */
    public void setGms(String gms){
        this.gms=gms;
    }
    /** Tiền sử bệnh */
    public String getJbs(){
        return this.jbs;
    }
    /** Tiền sử bệnh */
    public void setJbs(String jbs){
        this.jbs=jbs;
    }
    /** Tiền sử bệnh truyền nhiễm */
    public String getCrbs(){
        return this.crbs;
    }
    /** Tiền sử bệnh truyền nhiễm */
    public void setCrbs(String crbs){
        this.crbs=crbs;
    }
    /** Tiền sử phẫu thuật */
    public String getSss(){
        return this.sss;
    }
    /** Tiền sử phẫu thuật */
    public void setSss(String sss){
        this.sss=sss;
    }
    /** Tiền sử truyền máu */
    public String getSxs(){
        return this.sxs;
    }
    /** Tiền sử truyền máu */
    public void setSxs(String sxs){
        this.sxs=sxs;
    }
    /** Khám thực thể */
    public String getTgjc(){
        return this.tgjc;
    }
    /** Khám thực thể */
    public void setTgjc(String tgjc){
        this.tgjc=tgjc;
    }
    /** Chẩn đoán cấp cứu */
    public String getJzzd(){
        return this.jzzd;
    }
    /** Chẩn đoán cấp cứu */
    public void setJzzd(String jzzd){
        this.jzzd=jzzd;
    }
    /** Hiệu quả cấp cứu */
    public String getJzxg(){
        return this.jzxg;
    }
    /** Hiệu quả cấp cứu */
    public void setJzxg(String jzxg){
        this.jzxg=jzxg;
    }
    /** Tiền sử gia đình */
    public String getJzs(){
        return this.jzs;
    }
    /** Tiền sử gia đình */
    public void setJzs(String jzs){
        this.jzs=jzs;
    }
    /** Tiền sử kinh nguyệt */
    public String getYjz(){
        return this.yjz;
    }
    /** Tiền sử kinh nguyệt */
    public void setYjz(String yjz){
        this.yjz=yjz;
    }
    /** Tiền sử hôn nhân và sinh đẻ */
    public String getHys(){
        return this.hys;
    }
    /** Tiền sử hôn nhân và sinh đẻ */
    public void setHys(String hys){
        this.hys=hys;
    }
    /** Kiểm tra hỗ trợ */
    public String getFzjc(){
        return this.fzjc;
    }
    /** Kiểm tra hỗ trợ */
    public void setFzjc(String fzjc){
        this.fzjc=fzjc;
    }
    /** Bệnh sử hiện tại */
    public String getXbs(){
        return this.xbs;
    }
    /** Bệnh sử hiện tại */
    public void setXbs(String xbs){
        this.xbs=xbs;
    }
    /** Tiền sử */
    public String getJws(){
        return this.jws;
    }
    /** Tiền sử */
    public void setJws(String jws){
        this.jws=jws;
    }
    /** Kiểm tra khác */
    public String getQtjc(){
        return this.qtjc;
    }
    /** Kiểm tra khác */
    public void setQtjc(String qtjc){
        this.qtjc=qtjc;
    }
    /** Kiểm tra khác */
    public String getClqk(){
        return this.clqk;
    }
    /** Kiểm tra khác */
    public void setClqk(String clqk){
        this.clqk=clqk;
    }
    /** Kiểm tra khác */
    public String getGthjkjy(){
        return this.gthjkjy;
    }
    /** Kiểm tra khác */
    public void setGthjkjy(String gthjkjy){
        this.gthjkjy=gthjkjy;
    }
    /** Tiền sử dịch tễ */
    public String getLxbxs(){
        return this.lxbxs;
    }
    /** Tiền sử dịch tễ */
    public void setLxbxs(String lxbxs){
        this.lxbxs=lxbxs;
    }
    /** Người tạoID */
    public String getCreatedID(){
        return this.createdID;
    }
    /** Người tạoID */
    public void setCreatedID(String createdID){
        this.createdID=createdID;
    }
}
