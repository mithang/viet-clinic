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
}
