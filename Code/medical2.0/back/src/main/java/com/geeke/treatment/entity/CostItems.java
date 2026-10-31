package com.geeke.treatment.entity;

import lombok.Data;

@Data
public class CostItems extends CostItem{
    private String sign; //caothấpchỉsốđánh dấu
    private String conclusion;   //kếtluận
    private String describe;    //Mô tả
}
