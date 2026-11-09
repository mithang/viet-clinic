#!/bin/bash
# Khởi tạo DB md_yzs lần đầu (chỉ chạy khi volume mysql-data còn trống).
# Thứ tự y hệt tools/restore_all.sh bước 3 — KHÔNG dựa vào thứ tự alphabet
# của docker-entrypoint-initdb.d (v10 sẽ chạy trước v2 nếu xếp theo tên).
# Chạy bằng user root qua socket trong container; MYSQL_ROOT_PASSWORD có sẵn
# trong biến môi trường của container.
set -e
M="mysql -uroot -p${MYSQL_ROOT_PASSWORD}"

echo "[init] import dump goc md_yzs.sql ..."
(echo "SET SESSION sql_mode='';"; cat /repo-db/md_yzs.sql) | $M md_yzs
echo "[init] ureport_file.sql ..."
(echo "SET SESSION sql_mode='';"; cat /repo-db/ureport_file.sql) | $M md_yzs
echo "[init] add_indexes_p0.sql ..."
$M md_yzs < /repo-db/add_indexes_p0.sql
echo "[init] payment_dict_vi.sql ..."
$M md_yzs < /repo-db/payment_dict_vi.sql

# Migrations v1..v21 — v4 tách riêng vì BẮT BUỘC utf8mb4 (literal tiếng Trung
# trong WHERE; sai charset là UPDATE khớp 0 dòng mà vẫn exit 0), v18 có 2 file,
# v20/v21 cũng utf8mb4 (tên hiển thị tiếng Việt có dấu + còn sót chữ Trung).
for f in migration_v1_schema.sql migration_v2_toll_audit_log.sql migration_v3_print_charge.sql \
         migration_v5_followup_reminder.sql migration_v6_booking_request.sql migration_v7_vietqr_config.sql \
         migration_v8_drug_interaction.sql migration_v9_record_access_audit.sql migration_v10_vitals.sql \
         migration_v11_diagnosis_catalog.sql migration_v12_queue_number.sql migration_v13_tolldetail_grants.sql \
         migration_v14_propertyset_grants.sql migration_v15_audit_company.sql migration_v16_his_crkmx_id.sql \
         migration_v17_stock_control_unique.sql; do
  echo "[init] $f ..."
  $M md_yzs < "/repo-db/$f"
done
echo "[init] migration_v4_labels_vi.sql (utf8mb4) ..."
mysql --default-character-set=utf8mb4 -uroot -p"${MYSQL_ROOT_PASSWORD}" md_yzs < /repo-db/migration_v4_labels_vi.sql
echo "[init] migration_v18_freeze_stock.sql ..."
$M md_yzs < /repo-db/migration_v18_freeze_stock.sql
echo "[init] migration_v18_wx_user.sql ..."
$M md_yzs < /repo-db/migration_v18_wx_user.sql
echo "[init] migration_v19_clinic_is_institution.sql ..."
$M md_yzs < /repo-db/migration_v19_clinic_is_institution.sql
echo "[init] migration_v20_display_names_vi.sql (utf8mb4) ..."
mysql --default-character-set=utf8mb4 -uroot -p"${MYSQL_ROOT_PASSWORD}" md_yzs < /repo-db/migration_v20_display_names_vi.sql
echo "[init] migration_v21_display_names_vi.sql (utf8mb4) ..."
mysql --default-character-set=utf8mb4 -uroot -p"${MYSQL_ROOT_PASSWORD}" md_yzs < /repo-db/migration_v21_display_names_vi.sql

echo "[init] seed: notice_send + role full 4000 + grants ..."
$M md_yzs < /seed.sql
echo "[init] DB khoi tao xong."
