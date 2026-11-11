package com.geeke.toll.entity;

import lombok.Data;

@Data
public class StuffsalessummaryRc {
    private int limit;
    private int offset;
    private String companyId;
    private String kssj;
    private String jssj;
    private String clmc;
    private String lx;
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
     * getStuffsalessummarylist khong co guard nao.
     */
    public boolean isValidLimit() {
        return limit >= 0 && limit <= com.geeke.common.data.PageRequest.MAX_PAGE_SIZE;
    }

    public boolean isValidOffset() {
        return offset >= 0;
    }
}
