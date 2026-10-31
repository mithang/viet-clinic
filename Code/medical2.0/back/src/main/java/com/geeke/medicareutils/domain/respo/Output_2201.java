package com.geeke.medicareutils.domain.respo;

import lombok.Data;

@Data
public class Output_2201 {
    private data2201_out data;
    @Data
    public static class data2201_out {
        /**
         * Khám bệnh ID Kiểu ký tự 30 Y BHYTTrả vềduy nhất luồngnước
         */
        private String mdtrt_id;

        /**
         * Mã nhân viên Kiểu ký tự 30 Y
         */
        private String psn_no;

        /**
         * Nhập viện/Số khám ngoại trú Kiểu ký tự 30 Y Số thứ tự duy nhất trong viện
         */
        private String ipt_otp_no;
    }
}
