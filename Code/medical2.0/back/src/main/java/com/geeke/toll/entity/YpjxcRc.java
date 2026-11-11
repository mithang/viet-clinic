package com.geeke.toll.entity;

import lombok.Data;

@Data
public class YpjxcRc {
    private int limit;
    private int offset;
    private String companyId;
    private String kssj;
    private  String jssj;
    private String lx;
    private String zt;
    private String spm;
    private String ph;
    private String orderby;
    private String ypcltype;
    /*
    Trạng thái cơ sở(1: Đăng nhập tenant)
     */
    private String jgzt;
    /*
    Cơ sởid(Đơn vị thuêid)
     */
    private String jgid;

    /**
     * DS204: limit chi co tran tren, offset khong am (mirror
     * Visitprogresspara) — tran 10000 = PageRequest.MAX_PAGE_SIZE.
     * Truoc day limit client di thang vao LIMIT #{limit} o
     * getypjxcmanagement/getcljxcmanagement/getypclrkcxlist khong
     * co guard nao.
     */
    public boolean isValidLimit() {
        return limit >= 0 && limit <= com.geeke.common.data.PageRequest.MAX_PAGE_SIZE;
    }

    public boolean isValidOffset() {
        return offset >= 0;
    }
}
