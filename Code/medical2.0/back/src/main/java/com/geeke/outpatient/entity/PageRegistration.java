package com.geeke.outpatient.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRegistration {
    private String columnName;
    private int limit;
    private int offset;
    private String order;
    private String companyId;
    private String recipeStatus;
    private Date updateDate;
    private String status;
    private int chargeStatus=3;
    private int dispensionStatus=3;
    private String recipelType;
    private String patientName;
    private String patientCode;
    private int cureType=3;
    private int infuseType=3;
    private String overlook="3";
    private String doctorid;
    private String openId;

    /**
     * DS203: limit chi co tran tren. limit=0 phai giu hop le: UI workbench
     * gui limit: 0 mac dinh va duong do van chay (mapper LIMIT 0 tra 0 dong
     * nhung ORDER BY van danh gia). Chi chan limit > 10000 — cung tran nhu
     * PageRequest.MAX_PAGE_SIZE; truoc day khong tran, v2/list gui
     * limit=100000 la lay het dang ky ve mot trang.
     */
    public boolean isValidLimit() {
        return limit <= com.geeke.common.data.PageRequest.MAX_PAGE_SIZE;
    }

    /**
     * Xác thựcCộttênCó hay khôngan toàn(phòngdừngSQLinject)
     * cho phép của định dạng: chữ cái, Số, Gạch dưới, dấu chấm(dùng đểlồng nhauthuộc tínhNhư  "a.update_date")
     */
    public boolean isValidColumnName() {
        if (columnName == null || columnName.isEmpty()) {
            return false;
        }
        return columnName.matches("^[a-zA-Z_][a-zA-Z0-9_.]*$");
    }

}
