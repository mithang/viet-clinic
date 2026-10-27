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
}
