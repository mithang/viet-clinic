package com.geeke.outpatient.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Blmcxrc {
    private int limit;
    private int offset;
    private String companyId;
    private String mbmc;
    private String cjr;
    private String mblx;
    private String bllx;
    private String mbbm;
    private String updatedBy;
    private Date updatedTime;
    private String cjrid;

    /**
     * DS204: limit chi co tran tren, offset khong am (mirror
     * Visitprogresspara) — tran 10000 = PageRequest.MAX_PAGE_SIZE.
     * Truoc day limit client di thang vao LIMIT #{limit} o getblmblist
     * khong co guard nao (probe: limit=100000 tra het 3 dong live).
     */
    public boolean isValidLimit() {
        return limit >= 0 && limit <= com.geeke.common.data.PageRequest.MAX_PAGE_SIZE;
    }

    public boolean isValidOffset() {
        return offset >= 0;
    }
}
