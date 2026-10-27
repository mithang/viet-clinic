package com.geeke.treatment.entity;

import lombok.Data;

@Data
public class CostItems extends CostItem{
    private String sign; //cao低chỉsốđánh dấu
    private String conclusion;   //结论
    private String describe;    //Mô tả
}
