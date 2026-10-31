package com.geeke.utils.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.geeke.sys.entity.DictItem;
import com.geeke.sys.service.DictTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

/**
 * ExcelNhậpxây dựngthiết bị - Dựa trên EasyExcel, annotationđiều khiểnđộng
 *
 * <p>Ví dụ sử dụng:</p>
 * <pre>{@code
 * ExcelImportResult<Drug> result = new ExcelImportBuilder<>(Drug.class, inputStream)
 *     .dictTypeService(dictTypeService)
 *     .skipRows(2)
 *     .read();
 * }</pre>
 *
 * @param <T> Loại thực thể
 */
public class ExcelImportBuilder<T> implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ExcelImportBuilder.class);

    private final Class<T> entityClass;
    private final InputStream inputStream;
    private DictTypeService dictTypeService;
    private int skipRows = 0;

    // Cộtsố -> ánh xạThông tin
    private final Map<Integer, ColumnMapping> columnMapping = new LinkedHashMap<>();

    public ExcelImportBuilder(Class<T> entityClass, InputStream inputStream) {
        this.entityClass = entityClass;
        this.inputStream = inputStream;
        initColumnMapping();
    }

    public ExcelImportBuilder<T> dictTypeService(DictTypeService dictTypeService) {
        this.dictTypeService = dictTypeService;
        return this;
    }

    public ExcelImportBuilder<T> skipRows(int skipRows) {
        this.skipRows = skipRows;
        return this;
    }

    /**
     * đọcvàphân tíchExcel
     */
    public ExcelImportResult<T> read() {
        ExcelImportResult<T> result = new ExcelImportResult<>();

        // Sử dụng EasyExcel từng dòngđọc
        EasyExcel.read(inputStream, new ReadListener<Map<Integer, String>>() {
            private int currentRow = 0;

            @Override
            public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
                currentRow++;
                if (currentRow <= skipRows) return;

                int rowNum = context.readRowHolder().getRowIndex() + 1;

                try {
                    T entity = entityClass.newInstance();
                    boolean hasError = false;

                    for (Map.Entry<Integer, ColumnMapping> entry : columnMapping.entrySet()) {
                        int colIdx = entry.getKey();
                        ColumnMapping mapping = entry.getValue();
                        String cellValue = rowData.get(colIdx);
                        String label = mapping.getLabel();

                        // bắt buộcKiểm tra
                        if (mapping.annotation.required() && isBlank(cellValue)) {
                            result.addError(new ImportError(rowNum, label, "Không được để trống"));
                            hasError = true;
                            continue;
                        }

                        // Bỏ quatrốngGiá trị
                        if (isBlank(cellValue)) {
                            if (!mapping.annotation.defaultValue().isEmpty()) {
                                mapping.setValue(entity, mapping.annotation.defaultValue());
                            }
                            continue;
                        }

                        // độ dàiKiểm tra
                        if (mapping.annotation.maxLength() > 0 && cellValue.length() > mapping.annotation.maxLength()) {
                            result.addError(new ImportError(rowNum, label,
                                    "độ dàiKhông thểvượtqua" + mapping.annotation.maxLength() + "Cáichữký tự"));
                            hasError = true;
                            continue;
                        }

                        // ĐặtTrườngGiá trị
                        try {
                            Object value = convertValue(cellValue, mapping);
                            mapping.setValue(entity, value);
                        } catch (Exception e) {
                            result.addError(new ImportError(rowNum, label,
                                    "định dạngkhôngđúng: " + e.getMessage()));
                            hasError = true;
                        }
                    }

                    if (!hasError) {
                        result.addSuccess(entity);
                    }
                } catch (Exception e) {
                    logger.warn("phân tíchDòng {} dòng thất bại", rowNum, e);
                    result.addError(new ImportError(rowNum, null, "phân tíchThất bại: " + e.getMessage()));
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // đọchoàn thành
            }
        }).sheet().doRead();

        return result;
    }

    /**
     * Khởi tạoCộtánh xạ(quét @ExcelImportField annotation, hỗ trợTrường và getter)
     */
    private void initColumnMapping() {
        List<Class<?>> classChain = new ArrayList<>();
        Class<?> clazz = entityClass;
        while (clazz != null && clazz != Object.class) {
            classChain.add(0, clazz);
            clazz = clazz.getSuperclass();
        }

        for (Class<?> c : classChain) {
            // quétTrường
            for (Field field : c.getDeclaredFields()) {
                ExcelImportField annotation = field.getAnnotation(ExcelImportField.class);
                if (annotation != null) {
                    field.setAccessible(true);
                    columnMapping.put(annotation.columnIndex(), new FieldColumnMapping(field, annotation));
                }
            }

            // quétgetterPhương pháp
            for (Method method : c.getDeclaredMethods()) {
                ExcelImportField annotation = method.getAnnotation(ExcelImportField.class);
                if (annotation != null && isGetter(method)) {
                    Method setter = findSetter(c, method);
                    if (setter != null) {
                        setter.setAccessible(true);
                        columnMapping.put(annotation.columnIndex(),
                                new MethodColumnMapping(setter, method.getReturnType(), annotation));
                    }
                }
            }
        }
    }

    private boolean isGetter(Method method) {
        String name = method.getName();
        return (name.startsWith("get") && name.length() > 3 && method.getParameterCount() == 0)
                || (name.startsWith("is") && name.length() > 2 && method.getParameterCount() == 0);
    }

    private Method findSetter(Class<?> clazz, Method getter) {
        String setterName = getter.getName().startsWith("get")
                ? "set" + getter.getName().substring(3)
                : "set" + getter.getName().substring(2);
        try {
            return clazz.getMethod(setterName, getter.getReturnType());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Object convertValue(String cellValue, ColumnMapping mapping) {
        Class<?> fieldType = mapping.getFieldType();
        String dictTypeId = mapping.annotation.dictTypeId();

        // mục từ điểnXử lý
        if (!dictTypeId.isEmpty() && fieldType == DictItem.class) {
            if (dictTypeService == null) {
                throw new IllegalStateException("sử dụngtừ điểnphân tích giờPhảiĐặt dictTypeService");
            }
            String dictValue = dictTypeService.getValue(cellValue, dictTypeId);
            if (dictValue == null) {
                throw new IllegalArgumentException("Mục từ điển[" + cellValue + "]Không tồn tại");
            }
            DictItem dictItem = new DictItem();
            dictItem.setName(cellValue);
            dictItem.setValue(dictValue);
            return dictItem;
        }

        // cơ bảnLoạichuyển đổi
        if (fieldType == String.class) {
            return cellValue;
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return Integer.parseInt(cellValue.trim());
        } else if (fieldType == Long.class || fieldType == long.class) {
            return Long.parseLong(cellValue.trim());
        } else if (fieldType == Double.class || fieldType == double.class) {
            return Double.parseDouble(cellValue.trim());
        } else if (fieldType == BigDecimal.class) {
            return new BigDecimal(cellValue.trim());
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return "是".equals(cellValue) || "1".equals(cellValue) || "true".equalsIgnoreCase(cellValue);
        }

        return cellValue;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) inputStream.close();
    }

    // ---- trongbộLớp ----

    private static abstract class ColumnMapping {
        final ExcelImportField annotation;
        ColumnMapping(ExcelImportField annotation) { this.annotation = annotation; }
        abstract Class<?> getFieldType();
        abstract void setValue(Object entity, Object value) throws Exception;
        String getLabel() {
            return annotation.label().isEmpty() ? "Cột" + annotation.columnIndex() : annotation.label();
        }
    }

    private static class FieldColumnMapping extends ColumnMapping {
        final Field field;
        FieldColumnMapping(Field field, ExcelImportField annotation) {
            super(annotation);
            this.field = field;
        }
        Class<?> getFieldType() { return field.getType(); }
        void setValue(Object entity, Object value) throws Exception { field.set(entity, value); }
    }

    private static class MethodColumnMapping extends ColumnMapping {
        final Method setter;
        final Class<?> fieldType;
        MethodColumnMapping(Method setter, Class<?> fieldType, ExcelImportField annotation) {
            super(annotation);
            this.setter = setter;
            this.fieldType = fieldType;
        }
        Class<?> getFieldType() { return fieldType; }
        void setValue(Object entity, Object value) throws Exception { setter.invoke(entity, value); }
    }
}
