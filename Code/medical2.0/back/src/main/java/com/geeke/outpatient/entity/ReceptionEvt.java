package com.geeke.outpatient.entity;

import java.io.Serializable;
import java.util.List;

/**
 * hoàn thành tiếp nhậnLưuDữ liệulắp rápthực thể
 * @author helong
 * @date 2022/8/22 16:00
 */
public class ReceptionEvt implements Serializable
{
    private static final long serialVersionUID = -4774843195581832448L;
    /**
     * hoàn thành tiếp nhậnDữ liệuID(Tiếp nhận thông thường必truyền)
     */
    private String id;
    /**
     * Thông tin đăng ký
     */
    private Registration registration;
    /**
     * cơ bảnThông tin(tức làThông tin bệnh nhân)
     */
    private Patient patient;
    /**
     * Điền ca bệnhThông tin
     */
    private MedicalRecord medicalRecord;
    /**
     * kêĐơn thuốcThông tin(bao gồmĐơn thuốcchínhconChi tiết)
     */
    private List<RecipelInfoEvt> recipelInfoEvtList;
    /**
     * tiếp nhậnLoại:0-Tiếp nhận thông thường  1-Tiếp nhận nhanh  2-Thu phí bán lẻ
     */
    private int type = 0;
    /**
     * Xóa của tệp đính kèmidDanh sách
     */
    private List<String> deleteFileIdList;

    public List<String> getDeleteFileIdList() {
        return deleteFileIdList;
    }

    public void setDeleteFileIdList(List<String> deleteFileIdList) {
        this.deleteFileIdList = deleteFileIdList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Registration getRegistration() {
        return registration;
    }

    public void setRegistration(Registration registration) {
        this.registration = registration;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public MedicalRecord getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(MedicalRecord medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    public List<RecipelInfoEvt> getRecipelInfoEvtList() {
        return recipelInfoEvtList;
    }

    public void setRecipelInfoEvtList(List<RecipelInfoEvt> recipelInfoEvtList) {
        this.recipelInfoEvtList = recipelInfoEvtList;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
