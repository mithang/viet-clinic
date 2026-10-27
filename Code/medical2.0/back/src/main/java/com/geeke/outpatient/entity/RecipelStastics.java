package com.geeke.outpatient.entity;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class RecipelStastics
{
    @Tolerate
    public RecipelStastics(){

    }

    //này lầnChi tiết của id
    private String id;
    //này lầnChi tiếtthuộctạigìLoại
    private String stuffType;
    //này lầnChi tiếtchiếm dụngsốmục
    private int occupy;
}
