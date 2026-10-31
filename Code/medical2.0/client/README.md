# Geeke-Client

------

Geeke-Client là ứng dụng frontend của Geeke, phát triển trên Vue + Element. Gói bao gồm các chức năng, đặc điểm sau:

> * Vue + Element, nhẹ, hiệu năng cao, dễ sử dụng.
> * Ứng dụng Web backend không trạng thái, sử dụng JWT để quản lý xác thực danh tính.
> * Định tuyến động, tải theo nhu cầu.

## Cách sử dụng

### Sửa file cấu hình

Sửa BASE_API trong config\dev.env.js và prod.env.js thành địa chỉ của dịch vụ backend.

### Chạy

 - Cài đặt gói phụ thuộc: npm (khuyên dùng cnpm) install
 - npm run dev, truy cập qua http://localhost:3000
 - npm run build, sau khi hoàn thành, mở dist\index.html để truy cập
 - Người dùng mặc định: system, mật khẩu: 123456

### Lưu ý

**Nếu cần thêm module, thì cần:**

 1. Định nghĩa nội dung module trong quản lý định tuyến. Mã định tuyến thể hiện đường dẫn mã nguồn frontend trong thư mục views: ví dụ thêm quản lý hàng hóa, đặt mã định tuyến thành product, thì cần tạo thư mục product trong thư mục views, sau đó tạo index.vue. Thuộc tính mở rộng requiresAuth thể hiện việc truy cập định tuyến đó có cần kiểm chứng token ở frontend trước hay không; cssClass là kiểu biểu tượng cho menu định tuyến bên trái trang chủ (kiểu biểu tượng Icon của element-ui); name và FullPath là đường dẫn đầy đủ của breadcrumb điều hướng.
 2. Định nghĩa chức năng của module trong quản lý tài nguyên. Url của tài nguyên cần nhất quán với API Url định nghĩa trong Controller của dịch vụ Web. Quan hệ tương ứng giữa nội dung sau dấu hai chấm của quyền tài nguyên và Http Method: get->read, put->update, post->create, delete->delete
