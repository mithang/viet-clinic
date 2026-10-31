package com.geeke.utils.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;

import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * ExcelXuấtxây dựngthiết bị - Dựa trên EasyExcel, dạng luồngAPI
 *
 * <p>Ví dụ sử dụng:</p>
 * <pre>{@code
 * new ExcelExportBuilder("ThuốcChi tiết")
 *     .addColumns(
 *         new ExcelExportBuilder.Column("Tên thuốc", "drugName"),
 *         new ExcelExportBuilder.Column("Quy cách", "spec"),
 *         new ExcelExportBuilder.Column("Số lượng", "quantity")
 *     )
 *     .data(dataList)
 *     .write(response, "ThuốcChi tiết.xlsx");
 * }</pre>
 *
 * <p>Hỗ trợ truy cập thuộc tính lồng nhau, Ví dụ "company.name" Sẽ gọi getCompany().getName(). </p>
 * <p>Dựa trên EasyExcel, bộ nhớchiếm dụngthấp, phù hợphợplớnDữ liệulượngXuất. </p>
 */
public class ExcelExportBuilder implements Closeable {

    private String sheetName;
    private final List<Column> columns = new ArrayList<>();
    private List<?> dataList;
    private OutputStream outputStream;

    public ExcelExportBuilder(String sheetName) {
        this.sheetName = sheetName;
    }

    public ExcelExportBuilder addColumns(Column... cols) {
        this.columns.addAll(Arrays.asList(cols));
        return this;
    }

    public ExcelExportBuilder addColumns(List<Column> cols) {
        this.columns.addAll(cols);
        return this;
    }

    public ExcelExportBuilder data(List<?> dataList) {
        this.dataList = dataList;
        return this;
    }

    /**
     * Sẽ ExcelGhiHttpServletResponse, kích hoạttrình duyệtTải xuống
     */
    public void write(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                .replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        try (OutputStream os = response.getOutputStream()) {
            this.outputStream = os;
            buildAndWrite(os);
        }
    }

    /**
     * Sẽ Excelghichỉchỉ định của Luồng xuất
     */
    public void writeTo(OutputStream outputStream) throws IOException {
        this.outputStream = outputStream;
        buildAndWrite(outputStream);
    }

    private void buildAndWrite(OutputStream os) {
        if (columns.isEmpty()) {
            throw new IllegalStateException("Chưa định nghĩa bất kỳ cột nào, vui lòng gọi phương thức addColumns() trước");
        }

        // xây dựngbảngtiêu đề
        List<List<String>> head = new ArrayList<>();
        for (Column col : columns) {
            List<String> headRow = new ArrayList<>();
            headRow.add(col.getHeaderName());
            head.add(headRow);
        }

        // xây dựngDữ liệu(Sẽ Đối tượng/MapChuyển đổi thànhList<List<Object>>)
        List<List<Object>> data = new ArrayList<>();
        if (dataList != null) {
            for (Object rowData : dataList) {
                List<Object> row = new ArrayList<>();
                for (Column col : columns) {
                    row.add(resolveValue(rowData, col.getFieldPath()));
                }
                data.add(row);
            }
        }

        // Ghi Excel
        EasyExcel.write(os)
                .head(head)
                .sheet(sheetName)
                .doWrite(data);
    }

    /**
     * phân tíchĐối tượng của thuộc tínhGiá trị, hỗ trợlồng nhauthuộc tính và Map
     */
    private Object resolveValue(Object obj, String fieldPath) {
        if (obj == null || fieldPath == null) return null;

        if (obj instanceof Map) {
            return resolveMapValue((Map<?, ?>) obj, fieldPath);
        }

        String[] parts = fieldPath.split("\\.");
        Object current = obj;
        for (String part : parts) {
            if (current == null) return null;
            current = invokeGetter(current, part);
        }
        return current;
    }

    private Object resolveMapValue(Map<?, ?> map, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        Object current = map;
        for (String part : parts) {
            if (current == null) return null;
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                current = invokeGetter(current, part);
            }
        }
        return current;
    }

    private Object invokeGetter(Object obj, String methodName) {
        Class<?> clazz = obj.getClass();
        String capitalized = Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);

        try {
            return clazz.getMethod("get" + capitalized).invoke(obj);
        } catch (Exception e1) {
            try {
                return clazz.getMethod("is" + capitalized).invoke(obj);
            } catch (Exception e2) {
                try {
                    java.lang.reflect.Field field = clazz.getDeclaredField(methodName);
                    field.setAccessible(true);
                    return field.get(obj);
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        // EasyExcel tự độngQuản lýtài nguyên
    }

    /**
     * Cộtđịnh nghĩa
     */
    public static class Column {
        private final String headerName;
        private final String fieldPath;
        private int width = 0;

        public Column(String headerName, String fieldPath) {
            this.headerName = headerName;
            this.fieldPath = fieldPath;
        }

        public Column(String headerName, String fieldPath, int width) {
            this.headerName = headerName;
            this.fieldPath = fieldPath;
            this.width = width;
        }

        public String getHeaderName() { return headerName; }
        public String getFieldPath() { return fieldPath; }
        public int getWidth() { return width; }
    }
}
