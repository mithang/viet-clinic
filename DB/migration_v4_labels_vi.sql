-- Migration v4 (ĐÃ ÁP DỤNG lên DB test ngày 2026-10-31, đã kiểm chứng: các cột hiển thị
-- dưới đây còn 0 ký tự CJK, trừ đúng 2 dòng TAM GIU bên dưới).
-- LƯU Ý ÁP DỤNG: file chứa literal chữ Trung trong mệnh đề WHERE nên BẮT BUỘC chạy với
-- --default-character-set=utf8mb4; nếu không, literal gửi đi sai encoding, UPDATE khớp 0 dòng
-- mà vẫn exit 0 (đã gặp thực tế ngày 2026-10-31). restore_all.sh đã nối v4 với cờ này sau v3.
-- Nhãn hiển thị tiếng Trung còn lại trong DB -> tiếng Việt.
-- Nguồn rà soát: DB/md_yzs.sql (chỉ đọc, dump gốc KHÔNG sửa). Chạy lại an toàn (idempotent):
--  * UPDATE theo id + giá trị cũ (WHERE ... = 'giá trị cũ'): chạy lần 2 không đổi dòng nào.
--  * REPLACE theo đoạn chữ Trung (WHERE ... LIKE '%đoạn%'): chạy lần 2 không khớp dòng nào.
--  * Chỉ chạm cột hiển thị (name / properties.nameFullPath / comments / remarks).
--    KHÔNG chạm cột logic: sys_dict_item.value, code, url, permission, id, del_flag, is_locked.
--  * Chỉ dòng del_flag='0' (đang hiển thị). Dòng del_flag='1' đã xóa bị loại khỏi migration này.
--  * KHÔNG gồm: dữ liệu tham chiếu (xt_zd/xt_zyzh, administrative_division), tên riêng/dữ liệu
--    nghiệp vụ-demo (drug, manufacture_factory, clinic, member_set, article, patient...),
--    tên người trong create_by/update_by, tên font trong mẫu UReport, footer hard-code frontend.
--  * 2 trường hợp dùng TRIM() vì giá trị cũ có khoảng trắng cuối trong dump:
--    sys_dict_item 2256779862649512120 và gen_table 723817798886907905.
--  * 2 bản dịch suy luận từ ngữ cảnh anh em (cần chủ xác nhận trước khi áp):
--    sys_dict_item 1039656585586679847 子 -> 'Con trai' (cùng nhóm quan hệ: Vợ/chồng, Nữ, Cháu...);
--    sys_dict_item 1294206996438737800 待Phút诊 -> 'Chờ phân khám' (待分诊 = chờ phân khám/phân loại).
-- Áp dụng: mysql --default-character-set=utf8mb4 md_yzs < DB/migration_v4_labels_vi.sql
-- (đã nối vào restore_all.sh sau v3 với cờ utf8mb4).

-- 1. sys_router.name (15 dòng đang hiển thị còn chữ Trung trong name) --
UPDATE `sys_router` SET `name`='Hồ sơ bệnh nhân' WHERE `id`=1008519636055719936 AND `del_flag`='0' AND `name`='客户档案';
UPDATE `sys_router` SET `name`='Tiến trình khám bệnh' WHERE `id`=1258355633108674033 AND `del_flag`='0' AND `name`='Khám bệnh进度';
UPDATE `sys_router` SET `name`='Cảnh báo' WHERE `id`=1258355633108674046 AND `del_flag`='0' AND `name`='预警';
UPDATE `sys_router` SET `name`='Nhiệm vụ của tôi' WHERE `id`=1334242001512479101 AND `del_flag`='0' AND `name`='我 của Nhiệm vụ';
UPDATE `sys_router` SET `name`='Phát hành bài viết' WHERE `id`=1334242001512479103 AND `del_flag`='0' AND `name`='Phát hành文章';
UPDATE `sys_router` SET `name`='Xét duyệt bài viết' WHERE `id`=1334242001512479105 AND `del_flag`='0' AND `name`='Xét duyệt文章';
UPDATE `sys_router` SET `name`='Hiển thị màn hình lớn' WHERE `id`=1334242001512479405 AND `del_flag`='0' AND `name`='大屏展示';
UPDATE `sys_router` SET `name`='Theo dõi sau khám bệnh mạn tính' WHERE `id`=1334242001512479407 AND `del_flag`='0' AND `name`='慢Bệnh Theo dõi sau khám';
UPDATE `sys_router` SET `name`='Bệnh nhân tăng huyết áp theo dõi sau khám' WHERE `id`=1334242001512479419 AND `del_flag`='0' AND `name`='高Huyết áp Bệnh nhân Theo dõi sau khám';
UPDATE `sys_router` SET `name`='Bệnh nhân đái tháo đường type 2 theo dõi sau khám' WHERE `id`=1334242001512479421 AND `del_flag`='0' AND `name`='2型糖尿Bệnh Bệnh nhân Theo dõi sau khám';
UPDATE `sys_router` SET `name`='Bệnh nhân rối loạn tâm thần nặng theo dõi sau khám' WHERE `id`=1334242001512479429 AND `del_flag`='0' AND `name`='严重精神障碍Bệnh nhân Theo dõi sau khám';
UPDATE `sys_router` SET `name`='Thông tin cơ bản cá nhân' WHERE `id`=1334242001512479433 AND `del_flag`='0' AND `name`='Cá nhân基Cuốn Thông tin';
UPDATE `sys_router` SET `name`='Giấy đồng ý đã hiểu thông tin dịch vụ rối loạn tâm thần nặng' WHERE `id`=1334242001512479435 AND `del_flag`='0' AND `name`='严重精神障碍服务知情同意书';
UPDATE `sys_router` SET `name`='Bảng nhận diện thể chất Đông y' WHERE `id`=1334242001512479437 AND `del_flag`='0' AND `name`='Đông y体质辨识Bảng';
UPDATE `sys_router` SET `name`='Tra cứu đơn thuốc không hợp lý' WHERE `id`=1755596040061599754 AND `del_flag`='0' AND `name`='不合理Đơn thuốc Tra cứu';

-- 2. sys_router.properties: thay đoạn chữ Trung trong nameFullPath (dài trước, ngắn sau).
--    Sau bước này, đoạn cuối của nameFullPath khớp đúng name mới ở mục 1. --
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'文章Quản lý','Quản lý bài viết') WHERE `del_flag`='0' AND `properties` LIKE '%文章Quản lý%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'慢Bệnh Theo dõi sau khám','Theo dõi sau khám bệnh mạn tính') WHERE `del_flag`='0' AND `properties` LIKE '%慢Bệnh Theo dõi sau khám%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'高Huyết áp Bệnh nhân Theo dõi sau khám','Bệnh nhân tăng huyết áp theo dõi sau khám') WHERE `del_flag`='0' AND `properties` LIKE '%高Huyết áp Bệnh nhân Theo dõi sau khám%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'2型糖尿Bệnh Bệnh nhân Theo dõi sau khám','Bệnh nhân đái tháo đường type 2 theo dõi sau khám') WHERE `del_flag`='0' AND `properties` LIKE '%2型糖尿Bệnh Bệnh nhân Theo dõi sau khám%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'严重精神障碍Bệnh nhân Theo dõi sau khám','Bệnh nhân rối loạn tâm thần nặng theo dõi sau khám') WHERE `del_flag`='0' AND `properties` LIKE '%严重精神障碍Bệnh nhân Theo dõi sau khám%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'严重精神障碍服务知情同意书','Giấy đồng ý đã hiểu thông tin dịch vụ rối loạn tâm thần nặng') WHERE `del_flag`='0' AND `properties` LIKE '%严重精神障碍服务知情同意书%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'Cá nhân基Cuốn Thông tin','Thông tin cơ bản cá nhân') WHERE `del_flag`='0' AND `properties` LIKE '%Cá nhân基Cuốn Thông tin%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'Đông y体质辨识Bảng','Bảng nhận diện thể chất Đông y') WHERE `del_flag`='0' AND `properties` LIKE '%Đông y体质辨识Bảng%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'不合理Đơn thuốc Tra cứu','Tra cứu đơn thuốc không hợp lý') WHERE `del_flag`='0' AND `properties` LIKE '%不合理Đơn thuốc Tra cứu%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'Khám bệnh进度','Tiến trình khám bệnh') WHERE `del_flag`='0' AND `properties` LIKE '%Khám bệnh进度%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'我 của Nhiệm vụ','Nhiệm vụ của tôi') WHERE `del_flag`='0' AND `properties` LIKE '%我 của Nhiệm vụ%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'Phát hành文章','Phát hành bài viết') WHERE `del_flag`='0' AND `properties` LIKE '%Phát hành文章%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'Xét duyệt文章','Xét duyệt bài viết') WHERE `del_flag`='0' AND `properties` LIKE '%Xét duyệt文章%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'客户档案','Hồ sơ bệnh nhân') WHERE `del_flag`='0' AND `properties` LIKE '%客户档案%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'大屏展示','Hiển thị màn hình lớn') WHERE `del_flag`='0' AND `properties` LIKE '%大屏展示%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'Đặt lịch排版','Đặt lịch - xếp lịch') WHERE `del_flag`='0' AND `properties` LIKE '%Đặt lịch排版%';
UPDATE `sys_router` SET `properties`=REPLACE(`properties`,'预警','Cảnh báo') WHERE `del_flag`='0' AND `properties` LIKE '%预警%';

-- 3. sys_resource.name: tên quyền = động từ + tên đối tượng; thay đoạn chữ Trung như mục 2. --
UPDATE `sys_resource` SET `name`=REPLACE(`name`,'高Huyết áp Bệnh nhân Theo dõi sau khám','Bệnh nhân tăng huyết áp theo dõi sau khám') WHERE `del_flag`='0' AND `name` LIKE '%高Huyết áp Bệnh nhân Theo dõi sau khám%';
UPDATE `sys_resource` SET `name`=REPLACE(`name`,'2型糖尿Bệnh Bệnh nhân Theo dõi sau khám','Bệnh nhân đái tháo đường type 2 theo dõi sau khám') WHERE `del_flag`='0' AND `name` LIKE '%2型糖尿Bệnh Bệnh nhân Theo dõi sau khám%';
UPDATE `sys_resource` SET `name`=REPLACE(`name`,'严重精神障碍Bệnh nhân Theo dõi sau khám','Bệnh nhân rối loạn tâm thần nặng theo dõi sau khám') WHERE `del_flag`='0' AND `name` LIKE '%严重精神障碍Bệnh nhân Theo dõi sau khám%';
UPDATE `sys_resource` SET `name`=REPLACE(`name`,'严重精神障碍服务知情同意书','Giấy đồng ý đã hiểu thông tin dịch vụ rối loạn tâm thần nặng') WHERE `del_flag`='0' AND `name` LIKE '%严重精神障碍服务知情同意书%';
UPDATE `sys_resource` SET `name`=REPLACE(`name`,'Cá nhân基Cuốn Thông tin','Thông tin cơ bản cá nhân') WHERE `del_flag`='0' AND `name` LIKE '%Cá nhân基Cuốn Thông tin%';
UPDATE `sys_resource` SET `name`=REPLACE(`name`,'Đông y体质辨识Bảng','Bảng nhận diện thể chất Đông y') WHERE `del_flag`='0' AND `name` LIKE '%Đông y体质辨识Bảng%';
UPDATE `sys_resource` SET `name`=REPLACE(`name`,'Bệnh Ca填写(新版)','Ghi ca bệnh (bản mới)') WHERE `del_flag`='0' AND `name` LIKE '%Bệnh Ca填写(新版)%';
UPDATE `sys_resource` SET `name`=REPLACE(`name`,'Khám bệnh进度','Tiến trình khám bệnh') WHERE `del_flag`='0' AND `name` LIKE '%Khám bệnh进度%';
UPDATE `sys_resource` SET `name`=REPLACE(`name`,'我 của Nhiệm vụ','Nhiệm vụ của tôi') WHERE `del_flag`='0' AND `name` LIKE '%我 của Nhiệm vụ%';
UPDATE `sys_resource` SET `name`=REPLACE(`name`,'Phát hành文章','Phát hành bài viết') WHERE `del_flag`='0' AND `name` LIKE '%Phát hành文章%';
UPDATE `sys_resource` SET `name`=REPLACE(`name`,'Xét duyệt文章','Xét duyệt bài viết') WHERE `del_flag`='0' AND `name` LIKE '%Xét duyệt文章%';
UPDATE `sys_resource` SET `name`=REPLACE(`name`,'大屏展示','Hiển thị màn hình lớn') WHERE `del_flag`='0' AND `name` LIKE '%大屏展示%';
UPDATE `sys_resource` SET `name`=REPLACE(`name`,'组合','kết hợp') WHERE `del_flag`='0' AND `name` LIKE '%组合%';
UPDATE `sys_resource` SET `name`=REPLACE(`name`,'预警','Cảnh báo') WHERE `del_flag`='0' AND `name` LIKE '%预警%';
-- 3b. sys_resource đặc biệt (đảo thứ tự cho tự nhiên, không thay bằng đoạn): --
UPDATE `sys_resource` SET `name`='Bác sĩ đăng ký mặc định của tài khoản hiện tại' WHERE `id`=1008793465990692869 AND `del_flag`='0' AND `name`='Mặc định Đăng ký当先账户Bác sĩ';

-- 4. sys_dict_type: 1 name + 3 remarks --
UPDATE `sys_dict_type` SET `remarks`='Loại thao tác của nhật ký thao tác' WHERE `id`=5000 AND `del_flag`='0' AND `remarks`='Nhật ký thao tác中 của Loại thao tác';
UPDATE `sys_dict_type` SET `remarks`='Mô-đun sinh mã của bảng nghiệp vụ, cấu hình trường của loại thao tác tra cứu' WHERE `id`=5002 AND `del_flag`='0' AND `remarks`='Mô-đun sinh mã của Bảng nghiệp vụ中,Cấu hình Trường của Tra cứu Thao tác符Loại';
UPDATE `sys_dict_type` SET `remarks`='Mô-đun sinh mã của bảng nghiệp vụ, cấu hình trường của điều khiển hiển thị' WHERE `id`=5003 AND `del_flag`='0' AND `remarks`='Mô-đun sinh mã của Bảng nghiệp vụ中,Cấu hình Trường của Hiển thị Điều khiển';
UPDATE `sys_dict_type` SET `name`='Trạng thái đăng ký khám (gồm trạng thái khám bệnh)' WHERE `id`=1008898177293385773 AND `del_flag`='0' AND `name`='Trạng thái đăng ký khám(Gói含Trạng thái khám bệnh)';

-- 5. sys_dict_item.name (24 dòng đang hiển thị; KHÔNG đổi value) + 1 remarks --
-- TAM GIU (cho chu xac nhan, chua ap): ban dich suy luan 子 -> 'Con trai'.
-- UPDATE `sys_dict_item` SET `name`='Con trai' WHERE `id`=1039656585586679847 AND `del_flag`='0' AND `name`='子';
UPDATE `sys_dict_item` SET `name`='Pha nước uống' WHERE `id`=1107842936624948600 AND `del_flag`='0' AND `name`='水冲服';
UPDATE `sys_dict_item` SET `name`='Lần (một bên)' WHERE `id`=1128689556829987285 AND `del_flag`='0' AND `name`='Lần(单侧)';
UPDATE `sys_dict_item` SET `name`='Lần (một bên/hai bên)' WHERE `id`=1128689556829987286 AND `del_flag`='0' AND `name`='Lần(单侧/双侧)';
UPDATE `sys_dict_item` SET `name`='Tiếp nhận khám nhanh' WHERE `id`=1140922001646142209 AND `del_flag`='0' AND `name`='快速Tiếp nhận khám';
UPDATE `sys_dict_item` SET `name`='Thẻ trải nghiệm' WHERE `id`=1229966431858024522 AND `del_flag`='0' AND `name`='体验Thẻ';
-- TAM GIU (cho chu xac nhan, chua ap): ban dich suy luan 待Phút诊 -> 'Chờ phân khám'.
-- UPDATE `sys_dict_item` SET `name`='Chờ phân khám' WHERE `id`=1294206996438737800 AND `del_flag`='0' AND `name`='待Phút诊';
UPDATE `sys_dict_item` SET `name`='Hãm uống' WHERE `id`=1440747964863602939 AND `del_flag`='0' AND `name`='泡服';
UPDATE `sys_dict_item` SET `name`='Pha uống' WHERE `id`=1440747964863602940 AND `del_flag`='0' AND `name`='冲服';
UPDATE `sys_dict_item` SET `name`='Tiêm huyệt vị' WHERE `id`=1440747964863602941 AND `del_flag`='0' AND `name`='穴位Chú ý射';
UPDATE `sys_dict_item` SET `name`='Sắc nước uống' WHERE `id`=1440747964863602942 AND `del_flag`='0' AND `name`='水Sắc uống';
UPDATE `sys_dict_item` SET `name`='Thu hồi xuất kho' WHERE `id`=1607151971621723893 AND `del_flag`='0' AND `name`='Xuất kho撤回';
UPDATE `sys_dict_item` SET `name`='Hai ngày một lần' WHERE `id`=1607184304135528452 AND `del_flag`='0' AND `name`='两Ngày一Lần';
UPDATE `sys_dict_item` SET `name`='Đang xuất kho' WHERE `id`=1607184304135529161 AND `del_flag`='0' AND `name`='Xuất kho中';
UPDATE `sys_dict_item` SET `name`='Trả lại nhà cung cấp' WHERE `id`=1607184304135529254 AND `del_flag`='0' AND `name`='退还供方';
UPDATE `sys_dict_item` SET `name`='Thuốc báo hỏng' WHERE `id`=1607184304135529255 AND `del_flag`='0' AND `name`='Thuốc报损';
UPDATE `sys_dict_item` SET `name`='Điều chuyển xuất kho' WHERE `id`=1607184304135529256 AND `del_flag`='0' AND `name`='调拨Xuất kho';
UPDATE `sys_dict_item` SET `name`='Xuất kho thành công' WHERE `id`=1612936588194827077 AND `del_flag`='0' AND `name`='Xuất kho成功';
UPDATE `sys_dict_item` SET `name`='Xuất kho chưa đạt' WHERE `id`=1612936588194827078 AND `del_flag`='0' AND `name`='Xuất kho未Đạt';
UPDATE `sys_dict_item` SET `name`='Không' WHERE `id`=1654447687630167013 AND `del_flag`='0' AND `name`='否';
UPDATE `sys_dict_item` SET `name`='Có' WHERE `id`=1654447687630167014 AND `del_flag`='0' AND `name`='是';
UPDATE `sys_dict_item` SET `name`='Chờ xét duyệt' WHERE `id`=2087542517503803429 AND `del_flag`='0' AND `name`='待Xét duyệt';
UPDATE `sys_dict_item` SET `name`='Phòng khám ngoại trú thông thường' WHERE `id`=2256779862649512120 AND `del_flag`='0' AND TRIM(`name`)='普通Phòng khám ngoại trú';
UPDATE `sys_dict_item` SET `name`='Chứng minh nhân dân' WHERE `id`=2266033438408286212 AND `del_flag`='0' AND `name`='身份证';
UPDATE `sys_dict_item` SET `remarks`='Thuê bao phát thuốc, phòng khám xét duyệt xong nhập kho' WHERE `id`=2087542517503803429 AND `del_flag`='0' AND `remarks`='Thuê bao Phát thuốc,Phòng khám Xét duyệt后Nhập kho';

-- 6. gen_table.comments (2) --
UPDATE `gen_table` SET `comments`='Bảng byte tệp đính kèm' WHERE `id`=592905357446275244 AND `del_flag`='0' AND `comments`='Tệp đính kèm字节Bảng';
UPDATE `gen_table` SET `comments`='Bản ghi gửi thông báo' WHERE `id`=723817798886907905 AND `del_flag`='0' AND TRIM(`comments`)='公告发送记录';

-- 7. gen_table_column.comments (59 dòng đang hiển thị; nhãn trường dùng cho màn hình sinh mã/động) --
UPDATE `gen_table_column` SET `comments`='Tên loại từ điển' WHERE `id`=7073 AND `del_flag`='0' AND `comments`='Loại từ điển名';
UPDATE `gen_table_column` SET `comments`='Tên người dùng' WHERE `id`=7081 AND `del_flag`='0' AND `comments`='Người dùng名';
UPDATE `gen_table_column` SET `comments`='Tác giả' WHERE `id`=7097 AND `del_flag`='0' AND `comments`='作者';
UPDATE `gen_table_column` SET `comments`='Người thao tác' WHERE `id`=7115 AND `del_flag`='0' AND `comments`='Thao tác者';
UPDATE `gen_table_column` SET `comments`='Phân loại mẫu' WHERE `id`=7121 AND `del_flag`='0' AND `comments`='Mẫu Phút类';
UPDATE `gen_table_column` SET `comments`='Số hiệu bảng trực thuộc' WHERE `id`=7126 AND `del_flag`='0' AND `comments`='归属Bảng Số hiệu';
UPDATE `gen_table_column` SET `comments`='Số thứ tự hiển thị' WHERE `id`=7142 AND `del_flag`='0' AND `comments`='Hiển thị序号';
UPDATE `gen_table_column` SET `comments`='Số cột chỉnh sửa' WHERE `id`=7176 AND `del_flag`='0' AND `comments`='Sửa列数';
UPDATE `gen_table_column` SET `comments`='Số ngày nghỉ phép' WHERE `id`=7185 AND `del_flag`='0' AND `comments`='休假Ngày数';
UPDATE `gen_table_column` SET `comments`='Phòng ban cấp trên' WHERE `id`=52676365136650256 AND `del_flag`='0' AND `comments`='上级Phòng ban';
UPDATE `gen_table_column` SET `comments`='Tên hệ thống' WHERE `id`=592905357446275096 AND `del_flag`='0' AND `comments`='系统Tên';
UPDATE `gen_table_column` SET `comments`='Giới thiệu hệ thống' WHERE `id`=592905357446275097 AND `del_flag`='0' AND `comments`='系统Giới thiệu';
UPDATE `gen_table_column` SET `comments`='Tiêu đề' WHERE `id`=723817798886907909 AND `del_flag`='0' AND `comments`='标题';
UPDATE `gen_table_column` SET `comments`='Có phải tin nhắn nội bộ không' WHERE `id`=723817798886907910 AND `del_flag`='0' AND `comments`='是否站内Tin nhắn';
UPDATE `gen_table_column` SET `comments`='Có phải tin nhắn đầu WeChat không' WHERE `id`=723817798886907911 AND `del_flag`='0' AND `comments`='是否We Chat端Tin nhắn';
UPDATE `gen_table_column` SET `comments`='Có phải email không' WHERE `id`=723817798886907912 AND `del_flag`='0' AND `comments`='是否邮Chiếc';
UPDATE `gen_table_column` SET `comments`='Có phải tin nhắn SMS không' WHERE `id`=723817798886907913 AND `del_flag`='0' AND `comments`='是否短信';
UPDATE `gen_table_column` SET `comments`='Nội dung tin nhắn nội bộ' WHERE `id`=723817798886907914 AND `del_flag`='0' AND `comments`='站内Tin nhắn内容';
UPDATE `gen_table_column` SET `comments`='Nội dung tin nhắn WeChat' WHERE `id`=723817798886907915 AND `del_flag`='0' AND `comments`='We Chat Tin nhắn内容';
UPDATE `gen_table_column` SET `comments`='Nội dung tin nhắn email' WHERE `id`=723817798886907916 AND `del_flag`='0' AND `comments`='邮Chiếc Tin nhắn内容';
UPDATE `gen_table_column` SET `comments`='Nội dung tin nhắn SMS' WHERE `id`=723817798886907917 AND `del_flag`='0' AND `comments`='短信Tin nhắn内容';
UPDATE `gen_table_column` SET `comments`='Người nhận' WHERE `id`=723817798886907918 AND `del_flag`='0' AND `comments`='接收người';
UPDATE `gen_table_column` SET `comments`='Bản ghi gửi tin nhắn' WHERE `id`=726473703584546842 AND `del_flag`='0' AND `comments`='Gửi tin nhắn记录';
UPDATE `gen_table_column` SET `comments`='Người nhận tin nhắn' WHERE `id`=726473703584546843 AND `del_flag`='0' AND `comments`='Tin nhắn接收者';
UPDATE `gen_table_column` SET `comments`='Đã đọc' WHERE `id`=726473703584546844 AND `del_flag`='0' AND `comments`='已读';
UPDATE `gen_table_column` SET `comments`='Thời gian đọc' WHERE `id`=726473703584546845 AND `del_flag`='0' AND `comments`='读取Thời gian';
UPDATE `gen_table_column` SET `comments`='Cấp hệ thống' WHERE `id`=746103937512153091 AND `del_flag`='0' AND `comments`='系统级';
UPDATE `gen_table_column` SET `comments`='Phiên bản sản phẩm' WHERE `id`=986498806151577764 AND `del_flag`='0' AND `comments`='产品Phiên bản';
UPDATE `gen_table_column` SET `comments`='Giá vốn' WHERE `id`=998465736089977645 AND `del_flag`='0' AND `comments`='成Cuốn价';
UPDATE `gen_table_column` SET `comments`='Có thực hiện khấu trừ không' WHERE `id`=998465736089977653 AND `del_flag`='0' AND `comments`='是否Thực hiện划扣';
UPDATE `gen_table_column` SET `comments`='Tên hàng hóa' WHERE `id`=1004078055755374628 AND `del_flag`='0' AND `comments`='商品Tên';
UPDATE `gen_table_column` SET `comments`='Số đăng ký thuốc' WHERE `id`=1004078055755374634 AND `del_flag`='0' AND `comments`='国药准字';
UPDATE `gen_table_column` SET `comments`='Giá bán' WHERE `id`=1004406758192578611 AND `del_flag`='0' AND `comments`='售价';
UPDATE `gen_table_column` SET `comments`='Số lô' WHERE `id`=1005591224273698942 AND `del_flag`='0' AND `comments`='批号';
UPDATE `gen_table_column` SET `comments`='Thông tin liên hệ' WHERE `id`=1008489176147648539 AND `del_flag`='0' AND `comments`='联系方式';
UPDATE `gen_table_column` SET `comments`='Tổng lượng' WHERE `id`=1014474470772900079 AND `del_flag`='0' AND `comments`='总量';
UPDATE `gen_table_column` SET `comments`='Số ngày' WHERE `id`=1014474470772900083 AND `del_flag`='0' AND `comments`='Ngày数';
UPDATE `gen_table_column` SET `comments`='Tốc độ truyền' WHERE `id`=1014474470772900086 AND `del_flag`='0' AND `comments`='滴速';
UPDATE `gen_table_column` SET `comments`='Loại nhà sản xuất' WHERE `id`=1016206064147988511 AND `del_flag`='0' AND `comments`='厂商Loại';
UPDATE `gen_table_column` SET `comments`='Mã số giấy tờ' WHERE `id`=1017978820489553669 AND `del_flag`='0' AND `comments`='Số giấy tờ码';
UPDATE `gen_table_column` SET `comments`='Số tiền thực thu' WHERE `id`=1024846610635145509 AND `del_flag`='0' AND `comments`='实收Số tiền';
UPDATE `gen_table_column` SET `comments`='Có phải phí phụ thêm không' WHERE `id`=1080100145130881105 AND `del_flag`='0' AND `comments`='是否Phí phụ thêm';
UPDATE `gen_table_column` SET `comments`='Tên người phụ trách' WHERE `id`=1112505398504087558 AND `del_flag`='0' AND `comments`='负责người Tên';
UPDATE `gen_table_column` SET `comments`='Điện thoại người phụ trách' WHERE `id`=1112505398504087559 AND `del_flag`='0' AND `comments`='负责người Điện thoại';
UPDATE `gen_table_column` SET `comments`='Ngày thành lập' WHERE `id`=1112505398504087560 AND `del_flag`='0' AND `comments`='成立Ngày';
UPDATE `gen_table_column` SET `comments`='Số khu vực quy hoạch quận/huyện' WHERE `id`=1112505398504087562 AND `del_flag`='0' AND `comments`='规划Quận/huyện域号';
UPDATE `gen_table_column` SET `comments`='Số điều dưỡng' WHERE `id`=1112505398504087565 AND `del_flag`='0' AND `comments`='护士người数';
UPDATE `gen_table_column` SET `comments`='Người điền' WHERE `id`=1206424582237262788 AND `del_flag`='0' AND `comments`='填写người';
UPDATE `gen_table_column` SET `comments`='Thời gian điền' WHERE `id`=1206424582237262789 AND `del_flag`='0' AND `comments`='填写Thời gian';
UPDATE `gen_table_column` SET `comments`='Trạng thái điền' WHERE `id`=1206424582237262790 AND `del_flag`='0' AND `comments`='填写Trạng thái';
UPDATE `gen_table_column` SET `comments`='Thời gian lập đơn' WHERE `id`=1206424582237262797 AND `del_flag`='0' AND `comments`='开单Thời gian';
UPDATE `gen_table_column` SET `comments`='Tổng số thẻ' WHERE `id`=1206424599417127784 AND `del_flag`='0' AND `comments`='总张数';
UPDATE `gen_table_column` SET `comments`='Số thẻ còn lại' WHERE `id`=1206424599417127785 AND `del_flag`='0' AND `comments`='Còn lại张数';
UPDATE `gen_table_column` SET `comments`='Có hết hiệu lực không' WHERE `id`=1206424599417127787 AND `del_flag`='0' AND `comments`='是否失效';
UPDATE `gen_table_column` SET `comments`='Họ tên' WHERE `id`=1206424599417127816 AND `del_flag`='0' AND `comments`='姓名';
UPDATE `gen_table_column` SET `comments`='Số thẻ hội viên' WHERE `id`=1206424599417127820 AND `del_flag`='0' AND `comments`='Thẻ hội viên号';
UPDATE `gen_table_column` SET `comments`='Người mở thẻ' WHERE `id`=1206424599417127827 AND `del_flag`='0' AND `comments`='开Thẻngười';
UPDATE `gen_table_column` SET `comments`='Thời gian mở thẻ' WHERE `id`=1206424599417127828 AND `del_flag`='0' AND `comments`='开Thẻ Thời gian';
UPDATE `gen_table_column` SET `comments`='Có phải quản trị viên không' WHERE `id`=1294206944899131200 AND `del_flag`='0' AND `comments`='是否是Quản lý员';

-- 8. sys_code_rule.remarks (5 dòng đang hiển thị) --
UPDATE `sys_code_rule` SET `remarks`='Số thứ tự 4 chữ số' WHERE `id`=999820695192690692 AND `del_flag`='0' AND `remarks`='四位序号';
UPDATE `sys_code_rule` SET `remarks`='Số thứ tự 5 chữ số' WHERE `id`=1004078055755374621 AND `del_flag`='0' AND `remarks`='五位序号';
UPDATE `sys_code_rule` SET `remarks`='Số thứ tự 5 chữ số' WHERE `id`=1004497038405206016 AND `del_flag`='0' AND `remarks`='五位序号';
UPDATE `sys_code_rule` SET `remarks`='ARK + Năm Tháng Ngày + 5 chữ số' WHERE `id`=1029520634565500933 AND `del_flag`='0' AND `remarks`='ARK+Năm Tháng Ngày+五位数字';
UPDATE `sys_code_rule` SET `remarks`='PD + Thời gian + 5 chữ số' WHERE `id`=1251965580765667736 AND `del_flag`='0' AND `remarks`='PD+Thời gian+5位数字';
