
# Sinh mãỨng dụng nền tảng phát triển nhanh
Thao tácBước

1） Hạng mụcDựng，DựnggeekeHạng mục（1.1Mục）

2） Sử dụngERMasterTạoDữ liệuMô hình（1.2Mục）

3） VàoMô-đun sinh mã，ThêmBảng nghiệp vụCấu hình（1.3Mục）

4） VàoMô-đun sinh mã，ThêmPhương ánCấu hìnhVàSinhMã（1.4Mục）


## 1.1Hạng mụcDựng
Tải xuốngMã： https://code.newtouch.com/BfENepkG/geeke.git

##### Cơ sở dữ liệuKhởi tạo
Quaback\db\geedb.sqlSinhmysql của schema。

##### eclipseDự án backend
1） NhậpmavenHạng mục，
 
![NhậpmavenChức năng](https://console-step.newtouch.com/stepapi/v3/project/6f345ba01729459fb9a44651af59866e/file/download/34fea13e44d449a5bccbd8e0a80b787c)

maven  của pomTệp：\back\back

2） Sửa đổiCơ sở dữ liệuCấu hình

Sửa đổiTệpback\src\main\resources\application.yml，Cơ sở dữ liệuCấu hình。

spring:

  datasource:

    driver-class-name: com.mysql.cj.jdbc.Driver

    url: jdbc:mysql://10.1.1.219:3306/geedb?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC

    username: geeke

    password: 123

3) Sửa đổiSinh mãCấu hình

genConfigure:

  rootPath: E:\git\geeke  		 #Sinh mãĐịa chỉCấu hình

  dbName: mysql                          #Cơ sở dữ liệuLoại:  mysql/oracle/mssql

4）Khởi động dịch vụ backend
 
![NhậpmavenChức năng](https://console-step.newtouch.com/stepapi/v3/project/6f345ba01729459fb9a44651af59866e/file/download/c1cd93710fe34fa6ac2791627026b773)

Springboot Hạng mụcKhởi độngGiờBáoSilentExitExceptionNgoại lệ，Vào điểm dừng，F8Sau đó chương trình có thểBình thườngChạy，起初我以 là 只LàCái别情况，后来发现每Lần我TrongEclipse IDE（Spring Tool Suite）Trong của 调试模式DướiChạySpring BootHạng mụcGiờ，Luồng đều dừng tại“throw new SilentExitException（）;” Ngay cả khi không có điểm dừng cũng sẽ vào。

Đã tra cứu tài liệu，Làspring-boot-devtoolsMô-đun của 一CáiVấn đề đã biết（Tham khảohttps://github.com/spring-projects/spring-boot/issues/3100），因 là 用 của LàEclipse，Trực tuyến也给出了一些解决Phương án，可以TrongEclipseTrong首选Hạng mụcTrongJava - > DebugChuyển trong tùy chọn“Tạm dừngThực hiện未捕获 của Ngoại lệ”复选Khung以Ngăn它发生。

##### Dự án frontend
1） Trongcmd窗口TrongChuyển sangMã\clientDanh mục

按Dưới面 của 顺序输入命令

npm install

npm run dev

Tài khoản người dùngvàMật khẩu：system/123456

## 1.2Dữ liệuMô hình

1） 进行Cơ sở dữ liệu设计，建Bảng。

2） 自己TạoBảngMô hình thì xin sao chép một bản“geek.erm”Tệp，Trong此Mô hìnhCơ bảnTrênTạo自己 của Bảng nghiệp vụ即可。

3） eclipseCài đặtERMasterChènChiếc

Tải xuốngChènChiếc，Địa chỉ：https://sourceforge.net/projects/ermaster/files/

Giải nénorg.insightech.er_1.0.0.v20150619-0219.zip到eclipseDưới của pluginsDanh mụcTrong，Khởi động lạieclipse。

![Cài đặtERMasterChènChiếc](https://console-step.newtouch.com/stepapi/v3/project/6f345ba01729459fb9a44651af59866e/file/download/a33c93f3d00f4b95a6f1ad8a7bc11e63)

### 1.2.1 Các trường bắt buộc của bảng nghiệp vụ
Trong khung đỏ là Bắt buộc phải có của Trường：

![Nghiệp vụ须有Trường](https://console-step.newtouch.com/stepapi/v3/project/6f345ba01729459fb9a44651af59866e/file/download/5b2630dbf09a450bb73d0dcb0d7d25c6)

### 1.2.2 Các trường bắt buộc đối với một - nhiều
Trong khung đỏ là Bắt buộc phải có của Trường：

![1对多须有Trường](https://console-step.newtouch.com/stepapi/v3/project/6f345ba01729459fb9a44651af59866e/file/download/1c4dbed81a34460eb9e16fa5204e8754)

### 1.2.3 Các trường bắt buộc đối với cấu trúc cây
Trong khung đỏ là Bắt buộc phải có của Trường：

![树Cấu trúc须有Trường](https://console-step.newtouch.com/stepapi/v3/project/6f345ba01729459fb9a44651af59866e/file/download/0982cd5765f347ef9ee1e921efe91ff5)

### 1.2.4 Thêm nhanh các trường bắt buộc
Tronggeek.ermTệpTrongĐã内置HaiCáiTrườngNhóm，Phút别Làcommonvàtree_field，TrongBảngSửa界面Trong可快速选择VàThêmTương ứngTrường，Như hình dưới：

![Thêm nhanh các trường bắt buộc](https://console-step.newtouch.com/stepapi/v3/project/6f345ba01729459fb9a44651af59866e/file/download/3dc6a9659b124a7e9a87c52505848af5)


## 1.3Bảng nghiệp vụCấu hình

![Bảng nghiệp vụCấu hình](https://console-step.newtouch.com/stepapi/v3/project/6f345ba01729459fb9a44651af59866e/file/download/be7ff7d687924949b4961adc0df38947)

##### 基CuốnThông tin
1） Tên（Tên bảng）：Vật lýBảngTên bảng

2） Giải thích：Vật lýBảngMô tả bảng

3） Tên lớp thực thể：SinhBảngLiên kết của Tên lớp thực thể

4） Bảng cha liên kết：Bảng cha liên kết của Tên bảng，Khóa ngoại：Khi前BảngBảng cha liên kết của Khóa chính，Như果Khi前Bảng là Bảng con，cầnTrong此指定Bảng cha及Khóa ngoại。Khóa ngoạiTrườngcầnTrongTrườngCộtBảngTrong手动设置Thuộc tínhTên（Đối tượng.Khóa chính，CaNhư：SẽuserIdSửa đổi là user.id）

5） Bảng chaKhóa ngoại： và Bảng cha liên kếtLiên kết của Trường

6） Có hay khôngBảng liên kết：指明Bảng nghiệp vụThể hiện否属于HaiCáiNghiệp vụThực thểBảng多对多情况Dưới của Bảng liên kết。

7） Điều kiện lọc：Cấu hìnhCộtBảngTra cứuGiờ của Dữ liệuwhereĐiều kiện。

##### Trường ràng buộcBảng
ỐngHỗ trợDữ liệuKiểm tra ràng buộc trùng lặp，NhưNgười dùngĐăng nhậpTên不能重复。

1） Tên：Tên ràng buộc

2） Mô tả：Lưu重复Dữ liệuGiờ của Gợi ýThông tin

3） Trường ràng buộc：Ràng buộcKiểm tra của Cơ sở dữ liệuTrường


##### Trường nghiệp vụ
1） Tên cột：Dữ liệuBảngĐịnh nghĩa của TrườngTên

2） Giải thích：Dữ liệuBảngĐịnh nghĩa của TrườngChú ýGiải thích

3） Loại vật lý：Dữ liệuBảngĐịnh nghĩaTrườngLoại

4） JavaLoại：Thực thểĐối tượng của Thuộc tínhTrườngLoại

5） Loại tùy chỉnh：KhijavaLoại型选择customerGiờ，cần指定 của Cụ thểNghiệp vụĐịnh nghĩa của Loại（Định nghĩa của Phương án）

6） Tên thuộc tính：Thực thểĐối tượng của Thuộc tínhTrường（Đối tượngTên.Thuộc tínhTên|Thuộc tínhTên2|Thuộc tínhTên3，CaNhư：Người dùnguser.id|name|loginName，Thuộc tínhTên2vàThuộc tínhTên3 là JoinGiờLiên kếtTra cứu của Trường）

7） Giá trị mặc định：Khởi tạoĐối tượngGiá trị mặc định。可以使Cụ thể của Giá trị，Như1Hoặc'2018Năm';可以Làjs của Hàm，Nhưnew Data(）; 可以js của Tùy chỉnhHàm ẩn danh，Như：

function(） {var user = JSON.parse(sessionStorage.getItem('currentUser'））; return {id: user.objectId, name:''};}(）

8） Khóa chính：Có phải làKhóa chínhTrường

9） Có thể trống：ĐóTrườngCó hay khôngCó thể trống，Như果不 là 空Thì会Sinh界面不能 là 空 của Kiểm chứng

10） Có thể chèn：Có phải làChèn入Trường，Nếu phải thìGóiBao gồm tronginsertTrong câu lệnh

11） Có thể sửa：Có phải làSửaTrường，Nếu phải thìGóiBao gồm trongupdateTrong câu lệnh，Và且TrongSửa界面TrongCâyDữ liệu【Điều khiển】SinhTương ứng của 录入Khung

12） Hiển thị cột：Có hay khôngHiển thịTrongMặc định của CộtBảngTrong，Nếu phải thìGóiBao gồm trongCộtBảng页 của Bảng格Cột里

13） Độ rộng cột：Hiển thịTrong主页面CộtBảngTrên của 最小Chiều rộng（min-width）

14） Truy vấn：Có phải làTrường truy vấn，Nếu phải thìGóiBao gồm trongTra cứu栏Trong作 là Điều kiện truy vấn。

15） Cách truy vấn：Trường truy vấn của Cách truy vấn，也就Làwhere后 của ĐiềuChiếcBảng达式，Như：Trường1=Trường2ANDTrường3>Trường4ANDTrường5 !=Trường6。

16） Điều khiển：Biểu mẫuTrongTrườngSinh của NhómChiếc，Như：input，select，treeselect，areatextv.v.v.v.
17)  Biến liên động：KhiBiến liên động của Giá trị发生变化Giờ，Giao diện chínhĐiều kiện truy vấnKhung chọn hoặcSửa选择Khung của 选Hạng mục重MớiKhởi tạo。
    DùngKịch bảnNhư：切换Công ty，HoặcPhòng ban以后，Kích hoạt thay đổi lựa chọn trong khung chọn nhân viên。
    Cấu hình：员工TrườngCấu hìnhCông tyvàPhòng ban là Biến liên động。Sinh mãCông cụSẽTự độngSinhwatch，监听Công tyvàPhòng banBiến，VàCâyDữ liệuTham số liên độngCập nhật员工CộtBảngDữ liệu。

18） Tham số liên động：Giao diện chínhĐiều kiện truy vấnKhung chọn hoặcSửa选择KhungTrong，选Hạng mục của LọcĐiềuChiếc。Như：

	'a.company_id': {queryType: '=', value: This.company.id}

    [1] ĐiềuChiếc của 格式 là ：Trường: {ĐiềuChiếc运算符，ĐiềuChiếcGiá trị}

    [2] 多CáiĐiềuChiếc của 情况Dưới，采用逗号Phút隔开。

    [3] ThisLà từ khóa dành riêng，代BảngUI界面 của modelĐối tượng nghiệp vụ。

    [4] LọcĐiềuChiếc还可以DùngjsBiến、jsMã nguồn，甚đếnjsHàm ẩn danh。Thể hiệnCa：

'a.building_id': {queryType: '=', value: function(） {var user = JSON.parse(sessionStorage.getItem('currentUser'））; return user.objectId;}(）}

19） Sắp xếp：TrườngSinh của 先后顺序，Tăng dần。决定CộtBảngTrongMặc định顺序vàSửaKhungTrong của Điều khiển顺序。



## 1.4Phương ánCấu hình
![Phương ánCấu hình](https://console-step.newtouch.com/stepapi/v3/project/6f345ba01729459fb9a44651af59866e/file/download/db1465df9dc64c6599f29ab42b5853f0)

1） Phương ánTên：自定 của Phương ánTên。

2） MẫuPhútLớp：Sinh của Mẫu，目前ỐngHỗ trợ以DướiMẫu。

    [1] Thêm - xóa - sửa - tra cứu（Bảng đơn）：NhưQuản lý vai trò

    [2] Thêm - xóa - sửa - tra cứu（Một - nhiều）：NhưTừ điểnQuản lý

    [3] Bảng cấu trúc cây (tích hợp): NhưQuản lý định tuyến

    [4] Cây trái - bảng phải: như quản lý tài nguyên

    [5] Bảng cây cha - cây con：（NhưMô hình nghiệp vụCông ty（Dạng cây），Phòng ban（Dạng cây），对Phòng ban của Quản lý）

    [6] Chỉ tầng lưu trữ (dao/entity/mapper)： Tự độngSinhdao，entityvàmapperMã nguồn

    [7] Chỉ dịch vụ và giao diện (api/controller/service)： Tự độngSinhapi，controllervàserviceMã nguồn。

    [8]） Tầng lưu trữ, dịch vụ và giao diện (dao/api)：Tự độngGói含Chỉ tầng lưu trữ (dao/entity/mapper)vàChỉ dịch vụ và giao diện (api/controller/service) của Mã。

3） Đường dẫn gói：Sinh哪CáiGóiDưới。

4） Tên mô-đun：SinhGóiDưới của Tên mô-đun称，Tên mô-đun称Dưới进行Phút层。

5） Tên mô-đun con：Phút层Dưới của TệpThư mục，Có thể trống。

6） Tên chức năng：Sinh到LớpChú ýGiải thích里。

7） Tên chức năngViết tắt：SinhChức năngGợi ý，NhưTABTrên、CộtBảngTrên、Gợi ýThông tinv.v.。Như：Lưu"Ai đó"Thành công

8） Tác giả：Tên nhà phát triển

9） SửaChiều rộng：SửaKhung của Chiều rộng，Như：640px，Hoặc者80%

10） SửaCột数： SửaKhung của 输入Khung，按照几CộtBố cục。

11） Bảng nghiệp vụ：Phương ánTương ứng của Bảng nghiệp vụ。Một - nhiềuTrong trường hợp chọnBảng chính，Cây trái - bảng phảiTrong trường hợp chọnBảng con。

Chú ý：

    [1] 后台Sinh mãCấu trúc là  {Đường dẫn gói}/{Tên mô-đun}/controller/{Tên mô-đun con}/{Tên lớp}

    [2] FrontendSinh mãCấu trúc là src/views/{Tên mô-đun}/{Tên mô-đun con}/{Tên lớp}







