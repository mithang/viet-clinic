package com.geeke.medicareutils.domain.respo;

import lombok.Data;

import java.util.Date;

/**
 * @Description điện tửĐơn thuốcxét duyệtThông tin
 * @Author
 * @Date 2025/3/25
 */
@Data
public class MdExamine {
    // BHYTĐơn thuốcmã số
    private String hiRxno;

    // BHYTTên dược sĩ
    private String pharName;

    // BHYTDược sĩmã
    private String pharCode;

    // Đơn thuốcTrạng thái xét duyệtmã
    private String rxChkStasCodg;

    // Đơn thuốcxét duyệtý kiến
    private String rxChkOpnn;

    // Đơn thuốcThời gian xét duyệt
    private Date rxChkTime;
}
