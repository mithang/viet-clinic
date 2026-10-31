package com.geeke.common.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Tham số truy vấnxây dựngthiết bị
 * cung cấpdạng luồngAPIxây dựngTham số truy vấn, Nâng cao tính dễ đọc của code
 *
 * Ví dụ sử dụng: 
 * List<Parameter> params = SearchParamsBuilder.create()
 *     .eq("company_id", companyId)
 *     .like("name", name)
 *     .like("linkman", linkman)
 *     .eq("type", type)
 *     .build();
 */
public class SearchParamsBuilder {

    private final List<Parameter> parameters;

    private SearchParamsBuilder() {
        this.parameters = new ArrayList<>();
    }

    /**
     * tạomới của xây dựngthiết bịinstance
     */
    public static SearchParamsBuilder create() {
        return new SearchParamsBuilder();
    }

    /**
     * Tạo kèm bộ lọc tenant của xây dựngthiết bịinstance
     */
    public static SearchParamsBuilder createWithTenant(String companyId) {
        return new SearchParamsBuilder().eq("company_id", companyId);
    }

    /**
     *  v.v. tạiTruy vấn
     */
    public SearchParamsBuilder eq(String columnName, Object value) {
        if (value != null) {
            parameters.add(new Parameter(columnName, "=", value));
        }
        return this;
    }

    /**
     * không v.v. tạiTruy vấn
     */
    public SearchParamsBuilder ne(String columnName, Object value) {
        if (value != null) {
            parameters.add(new Parameter(columnName, "!=", value));
        }
        return this;
    }

    /**
     * mờTruy vấn
     */
    public SearchParamsBuilder like(String columnName, Object value) {
        if (value != null && !"".equals(value.toString().trim())) {
            parameters.add(new Parameter(columnName, "like", value));
        }
        return this;
    }

    /**
     * tráimờTruy vấn
     */
    public SearchParamsBuilder leftLike(String columnName, Object value) {
        if (value != null && !"".equals(value.toString().trim())) {
            parameters.add(new Parameter(columnName, "left_like", value));
        }
        return this;
    }

    /**
     * phảimờTruy vấn
     */
    public SearchParamsBuilder rightLike(String columnName, Object value) {
        if (value != null && !"".equals(value.toString().trim())) {
            parameters.add(new Parameter(columnName, "right_like", value));
        }
        return this;
    }

    /**
     * lớntạiTruy vấn
     */
    public SearchParamsBuilder gt(String columnName, Object value) {
        if (value != null) {
            parameters.add(new Parameter(columnName, ">", value));
        }
        return this;
    }

    /**
     * lớntại v.v. tạiTruy vấn
     */
    public SearchParamsBuilder ge(String columnName, Object value) {
        if (value != null) {
            parameters.add(new Parameter(columnName, ">=", value));
        }
        return this;
    }

    /**
     * nhỏtạiTruy vấn
     */
    public SearchParamsBuilder lt(String columnName, Object value) {
        if (value != null) {
            parameters.add(new Parameter(columnName, "<", value));
        }
        return this;
    }

    /**
     * nhỏtại v.v. tạiTruy vấn
     */
    public SearchParamsBuilder le(String columnName, Object value) {
        if (value != null) {
            parameters.add(new Parameter(columnName, "<=", value));
        }
        return this;
    }

    /**
     * INTruy vấn
     */
    public SearchParamsBuilder in(String columnName, List<?> values) {
        if (values != null && !values.isEmpty()) {
            parameters.add(new Parameter(columnName, "in", values));
        }
        return this;
    }

    /**
     * NOT INTruy vấn
     */
    public SearchParamsBuilder notIn(String columnName, List<?> values) {
        if (values != null && !values.isEmpty()) {
            parameters.add(new Parameter(columnName, "not in", values));
        }
        return this;
    }

    /**
     * BETWEENTruy vấn
     */
    public SearchParamsBuilder between(String columnName, Object start, Object end) {
        if (start != null && end != null) {
            List<Object> value = new ArrayList<>();
            value.add(start);
            value.add(end);
            parameters.add(new Parameter(columnName, "between", value));
        }
        return this;
    }

    /**
     * IS NULLTruy vấn
     */
    public SearchParamsBuilder isNull(String columnName) {
        parameters.add(new Parameter(columnName, "is null", null));
        return this;
    }

    /**
     * IS NOT NULLTruy vấn
     */
    public SearchParamsBuilder isNotNull(String columnName) {
        parameters.add(new Parameter(columnName, "is not null", null));
        return this;
    }

    /**
     * phảimờkhớp(dùng đểcâyhìnhcấu trúcTruy vấn)
     */
    public SearchParamsBuilder rightLikeForTree(String columnName, Object value) {
        if (value != null && !"".equals(value.toString().trim())) {
            parameters.add(new Parameter(columnName, "right_like", value));
        }
        return this;
    }

    /**
     * Thêmtùy chỉnhTham số
     */
    public SearchParamsBuilder add(Parameter parameter) {
        if (parameter != null) {
            parameters.add(parameter);
        }
        return this;
    }

    /**
     * ThêmORlogicTham số
     */
    public SearchParamsBuilder or(String columnName, String queryType, Object value) {
        if (value != null) {
            parameters.add(new Parameter(columnName, queryType, value, "OR"));
        }
        return this;
    }

    /**
     * xây dựngTham sốDanh sách
     */
    public List<Parameter> build() {
        return new ArrayList<>(parameters);
    }
}
