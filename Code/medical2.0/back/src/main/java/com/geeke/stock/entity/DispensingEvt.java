package com.geeke.stock.entity;

import java.util.List;

/**
 * Nhập khoEvt
 * @author txl
 * @version 2022-06-07
 */
public class DispensingEvt {

    private static final long serialVersionUID = 1005591224273698941L;
    private String registrationId;
    //  Trả thuốc giờ của Đơn thuốcid
    private List<String> recipelInfoIdList;
    //  0|1|2|3|4 Chưaphát|Một phầnĐãphát|Đãphát|Đãtrả|Một phầnĐãtrả
    private String dispensingStatus;
    //  0|1 Phát thuốc|Trả thuốc
    private String dispensingType;
    private List<DispensingDetailEvt> dispensingDetailEvtList;

    public String getRegistrationId() {
        return registrationId;
    }

    public List<String> getRecipelInfoIdList() {
        return recipelInfoIdList;
    }

    public void setRecipelInfoIdList(List<String> recipelInfoIdList) {
        this.recipelInfoIdList = recipelInfoIdList;
    }

    public String getDispensingStatus() {
        return dispensingStatus;
    }

    public void setDispensingStatus(String dispensingStatus) {
        this.dispensingStatus = dispensingStatus;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getDispensingType() {
        return dispensingType;
    }

    public void setDispensingType(String dispensingType) {
        this.dispensingType = dispensingType;
    }

    public List<DispensingDetailEvt> getDispensingDetailEvtList() {
        return dispensingDetailEvtList;
    }

    public void setDispensingDetailEvtList(List<DispensingDetailEvt> dispensingDetailEvtList) {
        this.dispensingDetailEvtList = dispensingDetailEvtList;
    }
}
