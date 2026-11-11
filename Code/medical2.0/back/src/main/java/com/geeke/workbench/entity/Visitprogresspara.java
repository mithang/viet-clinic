package com.geeke.workbench.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
public class Visitprogresspara {

    private int limit;
    private int offset;
    private String companyId;
    private String status;
    private String values;
    private String kssj;
    private  String jssj;

    /**
     * DS204: limit chi co tran tren (mirror PageRegistration DS203).
     * limit=0 giu hop le (UI co the gui mac dinh); limit am / offset am
     * truoc day lot toi MySQL (LIMIT -5 / OFFSET -1) roi moi bi DB tu
     * choi -> 400 message chung (DS94 handler), nen chan o day
     * fail-closed 400 voi message ro (IllegalArgumentException ->
     * DS160 handler). Tran 10000 = PageRequest.MAX_PAGE_SIZE.
     */
    public boolean isValidLimit() {
        return limit >= 0 && limit <= com.geeke.common.data.PageRequest.MAX_PAGE_SIZE;
    }

    public boolean isValidOffset() {
        return offset >= 0;
    }
}
