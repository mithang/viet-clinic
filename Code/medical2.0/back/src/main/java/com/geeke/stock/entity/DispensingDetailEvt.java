package com.geeke.stock.entity;

import lombok.Data;

/**
 * Nhập khoEvt
 * @author txl
 * @version 2022-06-07
 */
@Data
public class DispensingDetailEvt {

    private static final long serialVersionUID = 1005591224273698910L;
    //  Thông tin đơn thuốcid
    private String recipelInfoId;
    //  Thuốc/Vật tưid
    private String id;
    //  0|1 Thuốc|Vật tư
    private String type;
    private Integer number;
    //Trường BHYT
    private String drugTracCodg;   //Mã truy xuất thuốc


    public String getRecipelInfoId() {
        return recipelInfoId;
    }

    public void setRecipelInfoId(String recipelInfoId) {
        this.recipelInfoId = recipelInfoId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}
