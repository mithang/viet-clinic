-- ============================================================================
-- migration_v2_toll_audit_log.sql — Bảng audit hủy/hoàn hóa đơn (blocker 6)
--
-- BỐI CẢNH: trước migration này, hoàn phí chỉ tạo thêm 1 dòng toll_info
-- (amount_status = amountStatus_2, return_type = 1) và đổi trạng thái đơn
-- thuốc/đăng ký — KHÔNG có bảng nào ghi riêng "ai hoàn, hóa đơn nào,
-- số tiền bao nhiêu, lúc nào" ngoài create_by/create_date của chính dòng
-- toll_info hoàn phí. Bảng này ghi lại đúng các trường đó tại thời điểm
-- hoàn phí, để tra cứu/kiểm toán mà không cần suy luận từ toll_info.
--
-- PHẠM VI: chỉ thêm bảng mới. Không sửa md_yzs.sql gốc, không sửa bảng
-- cũ, không đổi logic thu phí hay số tiền. Log chỉ được ghi ở đường
-- hoàn phí của TollInfoService.processRefund (luồng UI:
-- POST /toll/tollInfo/save/toll/{type} với amount_status = amountStatus_2),
-- cùng transaction với lần hoàn phí nên hoàn phí lỗi thì không có log.
--
-- GHI CHÚ: "hủy hóa đơn" trong hệ thống này chính là "hoàn phí"
-- (amountStatus_2); không có endpoint hủy phiếu riêng. action giữ giá trị
-- 'REFUND' cho luồng hiện tại và 'CANCEL' dự phòng cho tương lai.
-- Người thao tác lấy từ create_by (DataEntity.preInsert ghi dạng
-- "Tên(loginName)" từ session), đúng quy ước của mọi bảng khác.
--
-- CHẠY: mysql -u root md_yzs < DB/migration_v2_toll_audit_log.sql
-- (an toàn chạy lại: CREATE TABLE IF NOT EXISTS, không INSERT mẫu).
-- ============================================================================
CREATE TABLE IF NOT EXISTS toll_audit_log (
    id                BIGINT UNSIGNED NOT NULL,
    company_id        BIGINT          NULL,
    toll_id           BIGINT          NULL COMMENT 'id dòng toll_info hoàn phí vừa tạo',
    recipel_id        BIGINT          NULL COMMENT 'id đơn thuốc được hoàn phí',
    patient_id        BIGINT          NULL,
    action            VARCHAR(16)     NOT NULL COMMENT 'REFUND: hoàn phí; CANCEL: hủy (dự phòng)',
    amount_receivable DECIMAL(10,2)    NULL COMMENT 'số tiền phải thu (hoàn lại)',
    amount_received   DECIMAL(10,2)    NULL COMMENT 'số tiền thực thu (hoàn lại)',
    payment_type      VARCHAR(64)     NULL COMMENT 'vd payType_0',
    amount_status     VARCHAR(64)     NULL COMMENT 'vd amountStatus_2',
    remarks           VARCHAR(255)    NULL,
    create_by         VARCHAR(64)     NOT NULL,
    create_date       DATETIME        NOT NULL,
    update_by         VARCHAR(64)     NOT NULL,
    update_date       DATETIME        NOT NULL,
    del_flag          VARCHAR(1)      NOT NULL DEFAULT '0',
    PRIMARY KEY (id),
    KEY idx_toll_audit_toll (toll_id),
    KEY idx_toll_audit_recipel (recipel_id),
    KEY idx_toll_audit_create_date (create_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
