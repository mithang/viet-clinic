package com.geeke.cure.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Stparameter {
    private int limit;
    private int offset;
    private String companyId;
    private String kssj;
    private  String jssj;
    private String zxzt;
    private String hzxm;
    private String kdys;
    private String detailid;
    private String zxr;
    private Date zxsj;
    private String stresult;
    private String sttime;

    /**
     * DS204: limit chi co tran tren, offset khong am (mirror
     * Visitprogresspara) — tran 10000 = PageRequest.MAX_PAGE_SIZE.
     * Truoc day limit client di thang vao LIMIT #{limit} o
     * stmanagementlist khong co guard nao.
     */
    public boolean isValidLimit() {
        return limit >= 0 && limit <= com.geeke.common.data.PageRequest.MAX_PAGE_SIZE;
    }

    public boolean isValidOffset() {
        return offset >= 0;
    }
}
