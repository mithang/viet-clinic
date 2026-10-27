package com.geeke.toll.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorkLoad {
    private String jgid;
    private String jgmc;
    private String companyId;
    private String name;      //Bác sĩTên
    private String count;   //Số lần tiếp nhận
    private BigDecimal grossAmount;  //phải thuSố tiền
    private BigDecimal actual;  //Số tiền thực thu
    private BigDecimal favorable;  //Số tiền ưu đãi
    private BigDecimal registrationCost;    //Phí đăng ký khám
    private BigDecimal westCost;
    private BigDecimal chineseCost;
    private BigDecimal chinesePatentCost;
    private BigDecimal stuffCost;
    private BigDecimal examinesCost;
    private BigDecimal checkoutCost;
    private BigDecimal therapyCost;
    private BigDecimal cureCost;
    private BigDecimal otherCost;
    private BigDecimal temporaryCost;
    private String types;
    private int total;
}
