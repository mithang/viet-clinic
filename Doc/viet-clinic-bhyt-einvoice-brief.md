# Viet-clinic — Brief quyết định: BHYT / hóa đơn điện tử / liên thông

> **TRẠNG THÁI — ĐỌC TRƯỚC: KHÔNG TỰ TÍCH HỢP VỚI HỆ THỐNG THẬT, KHÔNG QUẢNG
> CÁO LÀ ĐÃ CÓ LIÊN THÔNG.**
>
> Brief này chỉ là **chuẩn bị quyết định**: rà soát code thật để chỉ ra điểm
> tích hợp sẽ nằm ở đâu, và liệt kê chính xác chủ sở hữu cần cung cấp hay
> quyết định gì. Chưa có tích hợp nào với BHYT Việt Nam, hóa đơn điện tử hay
> hệ thống liên thông nào được thực hiện trong đợt này, và không được báo
> cáo là đã có.

- Repo: `~/workspace/your_files/Code`, HEAD tham chiếu lúc viết: `cfbbf51`.
- Kết luận rà soát (một câu): gateway BHYT hiện có trong code là **gateway
  yibao của Trung Quốc**, đang **tắt** (`check: false`), và **không có bằng
  chứng code nào** về liên thông đơn thuốc Việt Nam, hóa đơn điện tử Việt Nam,
  hay kết nối tới BHXH/Cổng tiếp nhận BHYT Việt Nam.

## 1. Code thật đã rà ra — điểm tích hợp nằm ở đâu

### 1.1. Cấu hình gateway yibao (Trung Quốc) — điểm bật/tắt duy nhất

- `Code/medical2.0/back/src/main/java/com/geeke/medicareutils/config/MedicareConfigProperties.java`
  — `@ConfigurationProperties(prefix = "spring.medicare")`, các trường:
  `check` (có mở giao diện BHYT không), `province`, `url`, `webUrl`,
  `appId`, `appSecret`, `privateKey`, `webPort`, `isDemo`.
- `Code/medical2.0/back/src/main/resources/application.yml` (mục `spring.medicare`):
  `check: false`, `web-url: http://127.0.0.1:8101/api/YibaoWeb`
  (ghi chú trong file nhắc tới service trung gian `prescription-service:8101`
  / tên service Docker tương ứng), `isDemo: false`, `web-port: 7020`
  (cổng frontend dùng để in đơn thuốc). `url`, `province`, `appId`,
  `appSecret`, `privateKey` để trống trong file này — **không có credential
  thật nào trong code, và brief này không yêu cầu cung cấp credential vào
  repo**.
- Mọi luồng yibao đều đi qua cờ `check`: chỉ khi `check == "true"` mới gọi
  sang gateway. Hiện tại `false` nên toàn bộ luồng này không chạy.

### 1.2. Hai lớp gọi gateway — nơi sẽ phải thay/thêm adapter Việt Nam

- `Code/medical2.0/back/src/main/java/com/geeke/medicareutils/util/MdRequestUtil.java`
  — `getMedicareData(infoNo, jsonObject)` (POST tới
  `medicareConfigProperties.getUrl()`, ghi log vào `YbjkLogcontent` qua
  `YbjkLogcontentService`), `dzcfUpload(infoNo, jsonObject)` (tải lên đơn
  thuốc điện tử, giải mã `encData` bằng `appId`/`appSecret`), `getMedicareFile(...)`.
  Danh sách mã cần mã hóa: `INFO_LIST = Ld7801, Ld7802, Ld7101, Ld7104,
  Ld7202, Ld7102, Ld7805, Ld7804, Ld7806` — đây là **mã giao dịch yibao
  Trung Quốc, không phải mã nghiệp vụ BHYT Việt Nam**.
- `Code/medical2.0/back/src/main/java/com/geeke/medicareutils/util/YbWebApiUtil.java`
  — gọi service trung gian qua `webUrl + /getMedicareData` và
  `webUrl + /getDzcfData` (tham số `infoNo`, `jsonObject`).
- Bảng log/giao dịch yibao đi kèm:
  `medicareutils/db/entity/YbjkLogcontent.java`,
  `medicareutils/db/entity/DzcfD001Output.java`, `DzcfD002Output.java`,
  `DzcfD003Output.java` cùng mapper/service tương ứng trong
  `medicareutils/db/`. Đây là schema của luồng Trung Quốc; luồng Việt Nam
  không được tự ý tái dùng nguyên xi mà phải đối chiếu với đặc tả của
  phía Việt Nam sau khi chủ sở hữu cung cấp (mục 3).

### 1.3. Ba điểm nghiệp vụ gọi vào luồng yibao (nơi tích hợp Việt Nam sẽ cắm vào)

| # | Điểm cắm | File / hàm | Hành vi hiện tại khi `check = true` |
|---|----------|------------|--------------------------------------|
| 1 | Lưu đăng ký khám | `Code/medical2.0/back/src/main/java/com/geeke/outpatient/controller/RegistrationController.java`, `save()` (dòng ~195) | Gọi `MdPsnDataService.getAndSetPsnData(entity)` (lấy thông tin người tham gia), rồi `MdRegistrationService.getRegistrationInfo_2201(entity)` (`MdRegistrationServiceImpl.java:61`) và gán `mdtrtId`, `iptOtpNo` lên `Registration` trước khi lưu. **Đây là điểm cắm cho "đăng ký khám BHYT / tra cứu thẻ BHYT" của Việt Nam nếu làm.** |
| 2 | Hủy đăng ký / hoàn phí đăng ký | Cùng file, endpoint `refundRegistrationPay` (dòng ~141) | Gọi `MdRegistrationService.revokeRegistrationInfo_2202(registration)` (`MdRegistrationServiceImpl.java:94`). **Điểm cắm cho "hủy đăng ký khám BHYT".** |
| 3 | Duyệt đơn thuốc (chỉ đơn kê trước `isPre`, kết quả duyệt = 1) | `Code/medical2.0/back/src/main/java/com/geeke/outpatient/controller/RecipelInfoReviewController.java`, `save()` (dòng ~76) | Gọi lần lượt `MdMedicationOrderService.validateElectronicPrescriptionUpload_Ld7801(...)` (`MdMedicationOrderImpl.java:79`), `signElectronicPrescriptionWithInsurance_Ld7802(...)`, `uploadElectronicPrescription_Ld7101(...)` (`MdMedicationOrderImpl.java:472`). **Điểm cắm cho "ký và tải lên đơn thuốc điện tử / liên thông đơn thuốc" của Việt Nam nếu làm.** |

Các service yibao còn lại (`MdCompanyServiceImpl`, `MdInventoryServiceImpl`,
`MdPsnDataServiceImpl`, `MdCommonServiceImpl`, `MdDirServiceImpl`) thuộc cùng
gói `com.geeke.medicareutils` và đều đi qua `MdRequestUtil`/`YbWebApiUtil` ở
mục 1.2 — không có service riêng nào cho BHYT Việt Nam.

### 1.4. Thu phí và hóa đơn — nền tảng nội bộ đã có, tích hợp ngoài chưa có

- Thu phí nội bộ đã nối API thật (commit `cfbbf51`):
  `Code/medical2.0/back/src/main/java/com/geeke/toll/controller/TollInfoController.java`
  (`@RequestMapping("/toll/tollInfo")`),
  `TollInfoService` (trong đó `processRefund(...)` dòng ~403, ghi audit vào
  `toll_audit_log`), entity `TollInfo` / `TollDetail`, và frontend
  `Code/medical2.0/client/src/views/charge/index.vue`.
  **Đây là điểm cắm tự nhiên cho hóa đơn điện tử: sau khi thu phí thành
  công (chargeStatus = đã thu), mới phát hành hóa đơn — nhưng hiện tại
  chưa có code nào gọi nhà cung cấp hóa đơn điện tử.**
- Nút "Xuất hóa đơn" trong `charge/index.vue` (dòng ~86) hiện chỉ là nút
  giao diện; rà soát không thấy code gọi API hóa đơn điện tử nào phía sau nó.
- Các chỗ có chữ "hóa đơn" khác trong code đều không liên quan: số hóa đơn
  nhập kho nhà cung cấp (`SupplierStorage.invoiceNumber`), log hoàn phí
  nội bộ (`TollAuditLog` — "hóa đơn" ở đây là phiếu thu nội bộ, không phải
  hóa đơn điện tử theo quy định thuế).
- **Không tìm thấy** code, dependency hay cấu hình nào cho: BHXH Việt Nam,
  Cổng tiếp nhận dữ liệu khám chữa bệnh BHYT, đơn thuốc liên thông theo
  quy định Việt Nam, nhà cung cấp hóa đơn điện tử (VNPT, Viettel hay bất kỳ
  hãng nào), chữ ký số cho hóa đơn/đơn thuốc Việt Nam. Tìm kiếm các từ
  khóa "hóa đơn điện tử", "einvoice", "liên thông", "BHXH", "VSSID",
  "VNPT", "Viettel" trong `back/src/main/java` và `client/src` không trả
  về bằng chứng tích hợp nào (chỉ có các trường nội bộ nêu trên).

## 2. Lựa chọn kiến trúc (đề xuất để chủ sở hữu quyết định, chưa triển khai)

1. **Không sửa luồng yibao Trung Quốc thành luồng Việt Nam bằng cách đổi
   tên.** Hai hệ thống khác nhau về mã giao dịch, chữ ký, endpoint và
   pháp lý. Cách đúng là: giữ gói `medicareutils` hiện tại nguyên trạng
   (tắt, `check: false`) cho tới khi có quyết định, và xây **adapter Việt
   Nam riêng** (package/service mới) cắm vào đúng 3 điểm ở mục 1.3 cộng
   với 1 điểm phát hành hóa đơn sau thu phí ở mục 1.4.
2. Thứ tự ưu tiên đề xuất (chủ sở hữu chốt, xem mục 3):
   - **P0 — Hóa đơn điện tử sau thu phí**: phạm vi hẹp nhất, điểm cắm rõ
     nhất (`TollInfoService` sau khi thu phí thành công), giá trị pháp lý
     trực tiếp cho phòng khám.
   - **P1 — Tra cứu thẻ BHYT + đăng ký khám BHYT** (điểm cắm 1, mục 1.3):
     cần credential và endpoint phía BHXH/cổng tiếp nhận — phụ thuộc hoàn
     toàn vào việc chủ sở hữu có mã cơ sở khám chữa bệnh và hợp đồng
     liên thông hay không.
   - **P2 — Liên thông đơn thuốc điện tử** (điểm cắm 3, mục 1.3): phức tạp
     nhất (ký số, kiểm tra trước, tải lên, đối soát), chỉ làm sau khi P1
     đã chạy và có đặc tả liên thông chính thức.
3. Không bật `check: true` trong bất kỳ môi trường nào cho tới khi adapter
   Việt Nam được viết và kiểm chứng — bật cờ hiện tại sẽ gọi sang endpoint
   yibao Trung Quốc (hiện để trống / trỏ `127.0.0.1:8101`) và gây lỗi.

## 3. Danh sách chính xác chủ sở hữu cần cung cấp / quyết định

Không có mục nào dưới đây được tự suy ra hay tự tạo trong đợt này. Mỗi mục
còn thiếu đều là blocker cho phần việc tương ứng.

### 3.1. Quyết định (chủ sở hữu chốt bằng văn bản)

1. **Phạm vi**: chỉ làm hóa đơn điện tử, hay cả BHYT, hay cả liên thông
   đơn thuốc? (Mặc định đề xuất theo thứ tự P0 → P1 → P2 ở mục 2.)
2. **Nhà cung cấp hóa đơn điện tử**: chọn hãng nào (ví dụ VNPT, Viettel
   hay hãng khác) — brief này không đề xuất hãng cụ thể vì chưa có thông
   tin hợp đồng/giá của phòng khám.
3. **Phạm vi BHYT**: phòng khám có ký hợp đồng khám chữa bệnh BHYT không?
   Nếu không, bỏ toàn bộ P1/P2, chỉ làm hóa đơn điện tử.
4. **Thứ tự ưu tiên và thời gian**: chốt P0 trước hay làm song song?
   (Khuyến nghị: P0 trước, vì P1/P2 phụ thuộc thủ tục hành chính bên ngoài.)

### 3.2. Cung cấp (qua kênh an toàn, KHÔNG ghi vào repo/chat công khai)

5. **Hóa đơn điện tử**: API endpoint + tài liệu tích hợp của nhà cung cấp
   đã chọn, tài khoản/mã số thuế/mẫu số/ký hiệu hóa đơn của phòng khám,
   chứng thư chữ ký số dùng để ký hóa đơn, môi trường test (sandbox) của
   nhà cung cấp để kiểm chứng trước khi phát hành thật.
6. **BHYT (chỉ khi quyết định làm P1)**: mã cơ sở khám chữa bệnh, endpoint
   và tài liệu cổng tiếp nhận dữ liệu BHYT áp dụng cho cơ sở, credential
   truy cập (cấp qua kênh an toàn, không commit), quy định về tra cứu
   thẻ và đăng ký khám của cơ quan quản lý nơi phòng khám hoạt động.
7. **Liên thông đơn thuốc (chỉ khi quyết định làm P2)**: đặc tả liên thông
   đơn thuốc điện tử chính thức áp dụng cho phòng khám, chứng thư chữ ký
   số của bác sĩ/dược sĩ dùng để ký đơn, môi trường test của cổng liên
   thông.
8. **Pháp lý**: xác nhận phòng khám tự chịu trách nhiệm tuân thủ quy định
   thuế (hóa đơn điện tử), BHYT và đơn thuốc tại thời điểm triển khai —
   brief này không phải tư vấn pháp lý và không thay thế văn bản của cơ
   quan quản lý.

## 4. Ước lượng phạm vi công việc (sau khi đã có đủ mục ở mục 3)

Ước lượng này là **phạm vi tương đối để chủ sở hữu lên kế hoạch, không
phải cam kết thời gian**, vì đặc tả thật của phía Việt Nam chưa có:

| Hạng mục | Phạm vi code chính | Độ phức tạp |
|----------|-------------------|-------------|
| P0 — Hóa đơn điện tử sau thu phí | Adapter mới gọi nhà cung cấp sau khi `TollInfoService` thu phí thành công; lưu trạng thái/mã hóa đơn vào `TollInfo`/`toll_audit_log`; nút "Xuất hóa đơn" ở `charge/index.vue` gọi API thật; xử lý lỗi/hủy hóa đơn | Trung bình — 1 adapter + 1 điểm cắm + giao diện |
| P1 — Tra cứu thẻ + đăng ký khám BHYT | Adapter Việt Nam mới thay thế điểm cắm 1 và 2 (mục 1.3); map dữ liệu thẻ BHYT sang `Registration` (`mdtrtId`, `iptOtpNo` hiện là trường của luồng Trung Quốc — cần đối chiếu, không tái dùng mù); xử lý lỗi cổng | Cao — phụ thuộc đặc tả và credential bên ngoài |
| P2 — Liên thông đơn thuốc | Adapter mới thay thế điểm cắm 3 (mục 1.3): kiểm tra trước, ký số, tải lên, đối soát; map `RecipelInfo`/`MedicalRecord` sang định dạng cổng | Cao nhất — ký số + nhiều bước + đối soát |
| Kiểm chứng | Mỗi hạng mục cần kiểm chứng trên sandbox của nhà cung cấp/cổng trước, rồi mới kiểm chứng trên môi trường thật với giao dịch test và hủy/hoàn rõ ràng | Bắt buộc cho cả 3 hạng mục |

Điều kiện tiên quyết chung: môi trường production đã chạy ổn theo runbook
(`Doc/viet-clinic-prod-runbook.md`), backup/restore đã kiểm chứng, secret
production đã cấp mới — không triển khai tích hợp ngoài lên một hệ thống
chưa qua go-live checklist.

## 5. Những việc KHÔNG làm trong đợt này (ghi rõ để tránh hiểu lầm)

- Không tích hợp với BHXH, cổng BHYT, cổng liên thông hay nhà cung cấp
  hóa đơn điện tử thật nào.
- Không bật `spring.medicare.check`, không điền `url`/`appId`/`appSecret`/
  `privateKey` thật, không tạo credential nào.
- Không quảng cáo hệ thống "đã có liên thông BHYT / hóa đơn điện tử".
  Câu đúng hiện tại: **hệ thống có sẵn gateway yibao Trung Quốc đang tắt,
  và có điểm cắm nghiệp vụ rõ ràng để xây adapter Việt Nam khi chủ sở
  hữu cung cấp đủ thông tin ở mục 3.**

---

*Chuẩn bị ngày 2026-10-31, từ code thật của repo tại HEAD `cfbbf51`.
Chưa tích hợp thật, chưa có credential thật, chưa có quyết định nào của
chủ sở hữu được thay mặt đưa ra.*
