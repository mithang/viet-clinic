package com.geeke.diagnosis.entity;

import java.io.Serializable;

/**
 * Một dòng trong danh mục chẩn đoán ICD-10 (H3 research loop).
 *
 * Đây là dữ liệu tham chiếu dùng chung, KHÔNG gắn tenant: mã ICD-10 là
 * chuẩn chung, không phải dữ liệu của một phòng khám. Bảng được tạo
 * TRỐNG bởi migration v11 và KHÔNG có endpoint ghi: danh mục chỉ lớn
 * lên bằng migration có kiểm chứng từ file chính thức của BYT (xem
 * migration v11 — NEED MORE INFO về file danh mục đầy đủ), không cho
 * phòng khám tự bịa mã qua API rồi tưởng đó là mã chuẩn.
 *
 * Entity cố tình là POJO thuần, không kế thừa DataEntity/CrudDao:
 * H3 chỉ đọc catalog (tìm kiếm phân trang + ghim theo bác sĩ), không
 * có luồng CRUD catalog nào.
 */
public class DiagnosisCatalog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String nameVi;
    private String chapter;

    public DiagnosisCatalog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameVi() {
        return nameVi;
    }

    public void setNameVi(String nameVi) {
        this.nameVi = nameVi;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }
}
