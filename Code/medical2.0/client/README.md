# Geeke-Client

------

Geeke-ClientLà dựa trênvue+elementPhát triển của GeekeHạng mục của Ứng dụng frontend，GóiBao gồm như sauChức năngĐặc điểm：

> * Vue+Element，Nhẹ、Hiệu năng cao、Dễ sử dụng。
> * BackendWebỨng dụngKhôngTrạng thái，Sử dụngJWTQuản lý xác thực danh tính。
> * Định tuyến động，Tải theo nhu cầu。


## Cách sử dụng

### Sửa file cấu hình

config\dev.env.jsvàprod.env.js，SửaBASE_API là  【back】Dịch vụ  của Địa chỉ。

### Chạy

 - Cài đặt gói phụ thuộc：npm（Khuyên dùngDùngcnpm） install
 - npm run dev，Quahttp://localhost:3000 Truy cập
 - npm run build，Sau khi hoàn thành, mởdist\index.htmlTruy cập
 - Người dùng mặc định：system，Mật khẩu：123456

### Lưu ý

**Nếu cần thêm module，thì cần：**

 1. Định nghĩa nội dung module trong quản lý định tuyến，Định tuyếnMãBảngThể hiệnFrontendMã nguồnĐường dẫnTrongviewsDưới của TệpThư mụcTên，Ví dụ thêm quản lý hàng hóa，Đặt mã định tuyến thànhproduct，Thì cần tạiviewsTệpThư mụcDướiTạoproductThư mục，sau đó tạoindex.vue；Thuộc tính mở rộngrequiresAuthBảngThể hiện访问ĐóĐịnh tuyếnLà cần kiểm chứng ở frontend trướctokenCó tồn tại không，cssClasslà kiểu biểu tượng cho menu định tuyến bên trái trang chủ（element-ui của IconKiểu biểu tượng），nameFullPathlà đường dẫn đầy đủ của breadcrumb điều hướng。
 2. Định nghĩa chức năng của module trong quản lý tài nguyên，Tài nguyên của urlCần vàWebDịch vụControllerĐịnh nghĩa trong của API UrlNhất quán，Tài nguyênQuyềnSau dấu hai chấm trong của Nội dung vàHttp Method của Tương ứngQuan hệ：get->read、put->update、post->create、delete->delete
 