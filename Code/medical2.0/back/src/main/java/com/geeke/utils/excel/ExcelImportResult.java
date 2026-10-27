package com.geeke.utils.excel;

import java.util.ArrayList;
import java.util.List;

/**
 * ExcelNhậpKết quả
 *
 * @param <T> Loại thực thể
 */
public class ExcelImportResult<T> {

    /** Thành côngphân tích của Dữ liệuDanh sách */
    private final List<T> dataList;

    /** Thông tin lỗiDanh sách */
    private final List<ImportError> errors;

    /** Thành côngsố */
    private int successCount;

    /** Thất bạisố */
    private int failCount;

    public ExcelImportResult() {
        this.dataList = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public List<T> getDataList() {
        return dataList;
    }

    public List<ImportError> getErrors() {
        return errors;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void addSuccess(T data) {
        dataList.add(data);
        successCount++;
    }

    public void addError(ImportError error) {
        errors.add(error);
        failCount++;
    }

    /**
     * Có hay khôngcóLỗi
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Lấytất cảThông tin lỗi của văn bảnnày摘cần
     */
    public String getErrorSummary() {
        StringBuilder sb = new StringBuilder();
        for (ImportError error : errors) {
            sb.append(error.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Chuyển đổi thànhtrướcđầukỳ望 của định dạng [Thành côngsố, Thất bạisố, Thông tin lỗi]
     */
    public List<String> toResponseList() {
        List<String> response = new ArrayList<>();
        response.add(String.valueOf(successCount));
        response.add(String.valueOf(failCount));
        response.add(getErrorSummary());
        return response;
    }
}
