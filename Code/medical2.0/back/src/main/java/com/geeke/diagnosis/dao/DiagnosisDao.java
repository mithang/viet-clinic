package com.geeke.diagnosis.dao;

import com.geeke.diagnosis.entity.DiagnosisCatalog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * Chẩn đoán ICD-10 DAO (H3 research loop).
 *
 * Cố tình KHÔNG kế thừa CrudDao: H3 chỉ đọc catalog và ghi/xóa ghim,
 * không có CRUD catalog. Mọi truy vấn đều có del_flag = '0'.
 * Catalog dùng chung mọi tenant (chuẩn chung); ghim gắn theo bác sĩ
 * (doctor_id = id user đang đăng nhập), không gắn tenant.
 */
@Mapper
public interface DiagnosisDao {

    /**
     * Tìm kiếm phân trang trên catalog: mã hoặc tên tiếng Việt chứa
     * chuỗi truy vấn (pattern LIKE đã escape ký tự %, _ trong service).
     * Không có từ khóa thì trả trang đầu theo mã — client không bao
     * giờ tải toàn bộ catalog (catalog BYT đầy đủ ~15.800 mã).
     */
    List<DiagnosisCatalog> search(Map<String, Object> params);

    /**
     * Tổng số dòng khớp truy vấn tìm kiếm (để phân trang ở client).
     */
    int countSearch(Map<String, Object> params);

    /**
     * Một dòng catalog đang hiệu lực theo id, hoặc null.
     */
    DiagnosisCatalog getById(Map<String, Object> params);

    /**
     * Danh sách ghim của một bác sĩ, mới ghim trước (theo
     * diagnosis_pin.create_date giảm dần), chỉ lấy dòng catalog còn
     * hiệu lực.
     */
    List<DiagnosisCatalog> listPinned(Map<String, Object> params);

    /**
     * Ghim một chẩn đoán cho một bác sĩ. Idempotent nhờ INSERT IGNORE
     * trên khóa chính (doctor_id, diagnosis_id): ghim lại chẩn đoán
     * đã ghim không lỗi, không tạo dòng trùng.
     */
    int insertPin(Map<String, Object> params);

    /**
     * Bỏ ghim. Không có dòng ghim thì trả 0, không lỗi.
     */
    int deletePin(Map<String, Object> params);
}
