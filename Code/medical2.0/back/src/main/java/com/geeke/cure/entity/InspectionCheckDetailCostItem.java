package com.geeke.cure.entity;

import com.geeke.org.entity.Company;
import com.geeke.treatment.entity.CostItem;
import lombok.Data;

@Data
public class InspectionCheckDetailCostItem {
    private String id;
    private Company company;      // Phòng khámid
    private CostItem costItem;      // Hạng mục tương ứngid
    private String conclusion;		// Kết quả
    private int sign;		// Cờ đánh dấu chỉ số cao/thấp
    private String describes;		// Mô tả
    private int seq;          //Sắp xếp
}
