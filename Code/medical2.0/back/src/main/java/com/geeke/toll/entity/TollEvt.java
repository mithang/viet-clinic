package com.geeke.toll.entity;

import com.geeke.member.entity.MemberManagement;
import com.geeke.outpatient.entity.RecipelInfoEvt;

import java.util.List;

/**
 * LC
 * LưuThu phíThông tin
 */

public class TollEvt {
    private TollInfo tollInfo;
    private List<RecipelInfoEvt> recipelInfos; //Thông tin đơn thuốc
    private String chargeStatus; //Trạng thái thu phí  0- Chờ thu phí   1-Một phầnThu phí   2- Đã thu phí   3-  Một phầnhoàn phí    4- Đã hoàn phí
    private MemberManagement memberManagement;   //Lưu của Hội viênthẻThông tin

    public String getChargeStatus() {
        return chargeStatus;
    }

    public void setChargeStatus(String chargeStatus) {
        this.chargeStatus = chargeStatus;
    }

    public TollInfo getTollInfo() {
        return tollInfo;
    }

    public void setTollInfo(TollInfo tollInfo) {
        this.tollInfo = tollInfo;
    }

    public List<RecipelInfoEvt> getRecipelInfos() {
        return recipelInfos;
    }

    public void setRecipelInfos(List<RecipelInfoEvt> recipelInfos) {
        this.recipelInfos = recipelInfos;
    }

    public MemberManagement getMemberManagement() {
        return memberManagement;
    }

    public void setMemberManagement(MemberManagement memberManagement) {
        this.memberManagement = memberManagement;
    }
}
