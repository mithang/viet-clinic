package com.geeke.outpatient.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class WeekdayDTO {
    //thứ ngày Bắt đầu Thứ bảyKết thúc
    //"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"
    private String Sunday;
    private String Monday;
    private String Tuesday;
    private String Wednesday;
    private String Thursday;
    private String Friday;
    private String Saturday;
}
