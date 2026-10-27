package com.geeke.cure.entity;

import com.geeke.org.entity.Company;
import com.geeke.outpatient.entity.Patient;
import com.geeke.treatment.entity.CostItems;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class InspectionCheckInfoCostItem {
    private String id;
    private Company company;      // Phòng khámid
    private InspectionCheck inspectionCheck;      // Bảng chínhid
    private String fileId;		// Tệpid
    private Patient patient;      // Bệnh nhânid
    private Date samplingDate;		// lấynhưThời gian
    private String samplingNum;		// nhưnàysố
    private String instrument;		// Xét nghiệmthiết bị
    private String checkoutNum;		// Xét nghiệmsố
    private Date checkoutDate;		// Xét nghiệmThời gian
    private String checkoutOffice;		// Xét nghiệmKhoa
    private String reportConclusion;		// kết luận báo cáo
    private String checkType;          //Kiểm traLoại
    private String checkPart;         //Kiểm trabộvị
    private String checkAdvise;      //Kiểm tranhìn thấy
    private String opinion;      //ý kiến
    private String doctor;      //Bác sĩ
    private String checkProject;   //Kiểm trahạng mục
    private List<CostItems> costItemsList;  //Hạng mục khám chữa bệnh
    private List<InspectionCheckDetailCostItem> inspectionCheckDetails;
}
