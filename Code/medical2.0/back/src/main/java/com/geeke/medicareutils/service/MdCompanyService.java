package com.geeke.medicareutils.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.entity.User;
import com.geeke.medicareutils.domain.respo.MdCompanyData;
import com.geeke.medicareutils.domain.respo.MdStaffData;
import com.geeke.org.entity.ClinicOffice;

import java.util.List;


/**
 * Phòng khámdịch vụ
 */
public interface MdCompanyService {


    /**
     * Lấykhámtrị liệuCơ sởThông tin
     */
    String getOrgCompanyInfo_1201(String infoNo, String name, String fixmedinsCode);


    /**
     * Thông tin khoaTải lên
     */
    String upCompanyData_3401_3402(ClinicOffice clinicOffice, String infoNo);



    /**
     * hàng loạtThông tin khoaTải lên
     */
    String upListCompanyData_3402A(List<ClinicOffice> clinicOffice, String infoNo);

    /**
     * Thông tin khoahủy
     * @param clinicOffice
     * @return
     */
    String delCompanyData_3403(ClinicOffice clinicOffice);

    /**
     * LấyBHYTy tếCơ sởThông tin khoa
     * @return
     */
    String getCompanyInfo_5101();

    /**
     * Lấyy tếCơ sởBHYTnhân viênThông tin
     * @return
     */
    String getStaffInfo_5102(User user);



}
