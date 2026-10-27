package com.geeke.utils.excel;

/**
 * ExcelNhậpThông tin lỗi
 */
public class ImportError {

    /** Số dòng(Từ 1Bắt đầu, Tương ứngExcelTrongthực tếSố dòng) */
    private int rowNum;

    /** Tên trường( trong văn bản) */
    private String fieldName;

    /** Thông tin lỗi */
    private String message;

    public ImportError(int rowNum, String fieldName, String message) {
        this.rowNum = rowNum;
        this.fieldName = fieldName;
        this.message = message;
    }

    public int getRowNum() {
        return rowNum;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Dòng " + rowNum + " dòng" + (fieldName != null ? "[" + fieldName + "]" : "") + ": " + message;
    }
}
