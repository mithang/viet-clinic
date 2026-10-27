package com.geeke.utils.excel;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

/**
 * @làmngười lc
 * @Thời gian tạo 2022Năm11Tháng16Ngày 10:31
 */
public class TableMapData {

    public String sheetName;

    public List<String> headers;

    public List<String> headersIndex;
    public List<Map<String,Object>> contents;

    /**
     * excel
     * key  là excelTrườngdùngheadersIndex của Giá trị
     * value  có excel hợpvà của đơn đồngô4CáiDữ liệu, Dòng 5Cái có hợpvàđơn đồngôtênchữ
     *
     */
    public Map<String,List<String>> mergeCells = null;
    public TableMapData() {
        this.headers = Lists.newArrayList();
        this.headersIndex = Lists.newArrayList();
        this.contents = Lists.newArrayList();
    }

    @Override
    public String toString() {
        return "TableMapData{" +
                "sheetName='" + sheetName + '\'' +
                ", headers=" + headers +
                ", headersIndex=" + headersIndex +
                ", contents=" + contents +
                '}';
    }
}
