package com.geeke.stock.entity;

import lombok.Getter;

@Getter
public enum InstantPatient
{
    RETAIL("Thu phí bán lẻ", 2),
    INSTANT("Tiếp nhận nhanh",1),
    NORMAL("Tiếp nhận thông thường",0);

    InstantPatient(String name, int code) {
        this.name = name;
        this.code = code;
    }

    private String name;
    private int code;
}
