-- ============================================================
-- P0 Tối ưu hiệu năng: thêm chỉ mục phụ cho các cột truy vấn tần suất cao
-- Trước khi chạy, hãy kiểm chứng ở môi trường kiểm tra, xác nhận không có rủi ro khóa bảng rồi chạy vào giờ thấp điểm
-- Cách chạy: mysql -u root -p dbname < add_indexes_p0.sql
-- ============================================================

-- Bảng thuốc: thuê bao + mã + tên, truy vấn tần suất cao
ALTER TABLE drug ADD INDEX idx_drug_company (company_id);
ALTER TABLE drug ADD INDEX idx_drug_code (company_id, code);
ALTER TABLE drug ADD INDEX idx_drug_goods_name (company_id, goods_name);

-- Bảng bệnh nhân: số điện thoại di động / số thẻ / thuê bao
ALTER TABLE patient ADD INDEX idx_patient_company (company_id);
ALTER TABLE patient ADD INDEX idx_patient_phone (company_id, phone);
ALTER TABLE patient ADD INDEX idx_patient_card (company_id, card);

-- Bảng đăng ký khám
ALTER TABLE registration ADD INDEX idx_reg_company (company_id);
ALTER TABLE registration ADD INDEX idx_reg_patient (patient_id);
ALTER TABLE registration ADD INDEX idx_reg_status (company_id, status);

-- Bảng thông tin đơn thuốc
ALTER TABLE recipel_info ADD INDEX idx_ri_registration (registration_id);
ALTER TABLE recipel_info ADD INDEX idx_ri_company (company_id);

-- Bảng kiểm soát tổng tồn kho
ALTER TABLE medicinal_stock_control ADD INDEX idx_msc_drug_stuff (drug_stuff_id);
ALTER TABLE medicinal_stock_control ADD INDEX idx_msc_company (company_id);

-- Bảng chi tiết tồn kho
ALTER TABLE medicinal_stock_record ADD INDEX idx_msr_drug_stuff (drug_stuff_id);

-- Bảng tồn kho nhà cung cấp
ALTER TABLE supplier_stock ADD INDEX idx_ss_company (company_id);
ALTER TABLE supplier_stock ADD INDEX idx_ss_drug (drug_id);

-- Bảng quản lý nhiệm vụ
ALTER TABLE taskmanagement ADD INDEX idx_tm_company (company_id);
ALTER TABLE taskmanagement ADD INDEX idx_tm_status (company_id, taskstatus);
ALTER TABLE feedbacktable ADD INDEX idx_fb_task (taskmanagement_id);

-- Bảng mẫu bệnh án
ALTER TABLE casehistorymbgl ADD INDEX idx_chmb_company (company_id);

-- Bảng bài viết
ALTER TABLE article ADD INDEX idx_article_company (company_id);
