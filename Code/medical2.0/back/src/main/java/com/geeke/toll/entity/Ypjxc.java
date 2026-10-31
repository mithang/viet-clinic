package com.geeke.toll.entity;

import lombok.Data;

@Data
public class Ypjxc {
    /*
    Cơ sởid
     */
    private String jgid;
    /*
    Cơ sởTên
     */
    private String jgmc;
    /**
     * Mã thuốc
     */
    private String ypbm;
    /*
    Tên thuốc
     */
    private String ypmc;
    /*
    Mã vạch
     */
    private  String txm;
    /*
    Số đăng ký thuốc quốc gia
     */
    private  String gyzz;
    /*
    Quy cách
     */
    private  String gg;
    /*
    Nhà sản xuất
     */
    private  String sccj;
    /*
    Nhà cung cấp
     */
    private  String gys;
    /*
    Giá bán lẻ
     */
    private  String lsj;
    /*
    Giá vốn
     */
    private  String cbj;
    /*
    Số lô
     */
    private  String ph;
    /*
    Thời hạn hiệu lực
     */
    private  String yxq;
    /*
    kỳban đầuSố lượng
     */
    private  Integer qcsl;
    /*
    kỳban đầuthànhnày
     */
    private  Integer qccb;
    /*
    nàykỳnhập khoSố lượng
     */
    private  Integer bqrk;
    /*
    nàykỳnhập khothànhnày
     */
    private  Integer bqrkcb;
    /*
    Doanh số nhập kho trong kỳ này
     */
    private  Integer bqrkxse;
    /*
    nàykỳXuất khoSố lượng
     */
    private  Integer bqcksl;
    /*
    nàykỳXuất khothànhnày
     */
    private  Integer bqckcb;
    /*
    nàykỳXuất khobán hàngsố tiền
     */
    private  Integer bqckxse;
    /*
    nàykỳkiểm kêSố lượng
     */
    private  Integer bqpdsl;
    /*
    nàykỳkiểm kêthànhnày
     */
    private  Integer bqpdcb;
    /*
    Doanh số kiểm kê trong kỳ này
     */
    private  Integer bqpdxse;
    /*
    kỳcuốiSố lượng
     */
    private  Integer qmsl;
    /*
    kỳcuốithànhnày
     */
    private  Integer qmcb;
    /*
    kỳcuốibán hàngsố tiền
     */
    private  Integer qmxse;

    /**
     * Phân loại
     */
    private  String fl;
    /**
     * Chế phẩm
     */
    private Integer zj;
    private String zxdw;
    /**
     * đoánđo giờ Đơn thuốcChi tiếtsử dụng ngàysố mục từ điển của name(nàyCáitừ điển của name có Số)
     */
    private String dw;

}
