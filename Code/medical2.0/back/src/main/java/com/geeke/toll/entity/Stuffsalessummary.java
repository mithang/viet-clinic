package com.geeke.toll.entity;

import lombok.Data;

@Data
public class Stuffsalessummary {
    /** Cơ sởid */
    private String jgid;
    /** Cơ sởTên */
    private String jgmc;
    /** Vật tưTên khác */
    private String clbm;
    /** Tên vật tư */
    private String clmc;
    private String gg;
    /** Số lượng */
    private Integer sl;
    /** Chế phẩm */
    private Integer zj;
    private String bzsl;
    private String bzdw;
    private String zxdw;
    private String dw;
    /** Loại vật tư */
    private String cllx;
}
