# Viet-clinic — Runbook triển khai production

> **TRẠNG THÁI — ĐỌC TRƯỚC KHI LÀM THEO: CHƯA CÓ DOMAIN THẬT, CHƯA CÓ MÁY CHỦ
> PRODUCTION, CHƯA DEPLOY THẬT.**
>
> Tài liệu này chỉ là **chuẩn bị**: cấu hình mẫu, checklist và quy trình để chủ
> sở hữu dùng khi đã có domain và máy chủ thật. Phần mua domain, trỏ DNS thật,
> cấp chứng chỉ cho domain thật và deploy thật là **việc còn lại của chủ sở hữu**,
> chưa làm trong đợt này và không được báo cáo là đã xong.
>
> Hệ thống hiện tại chỉ chạy **local test**: frontend dev 7020, backend 7016,
> database test `md_yzs`. Mọi lệnh dưới đây có ghi `<...>` đều là chỗ giữ —
> thay bằng giá trị thật của chủ sở hữu khi deploy, **không ghi secret thật
> vào repo, vào file này hay vào log**.

- Repo: `~/workspace/your_files/Code`, HEAD tham chiếu lúc viết: `cfbbf51`.
- Backend: Java 8, Spring Boot 2.1, cổng production `7016`
  (`Code/medical2.0/back/src/main/resources/application-prod.yml`).
- Frontend: Vue 2, build ra `dist` (`Code/medical2.0/client`, `npm run build`).
  Ở môi trường dev, proxy `/api` → `http://localhost:7016`
  (`Code/medical2.0/client/vue.config.js`); bản production phải giữ đúng quy ước
  này (xem mục 2).
- Đóng gói sẵn (file, chưa chạy deploy thật): `Dockerfile` ở gốc repo
  (build frontend + backend, backend chạy jar, frontend dist nằm ở
  `/opt/frontend/dist` trong image, healthcheck gọi `/actuator/health`) và
  workflow `.github/workflows/ci.yml` (chỉ lint + build + test, **không có
  job deploy**).
- Giám sát: chỉ endpoint `/actuator/health` được mở ẩn danh
  (`Code/medical2.0/back/src/main/java/com/geeke/config/shiro/ShiroConfiguration.java`
  — `filterChainDefinitionMap.put("/actuator/health", "anon")` — và
  `application.yml`: chỉ expose `health`). Các endpoint actuator khác vẫn yêu
  cầu xác thực; đừng mở thêm.

## 1. Kiến trúc production mục tiêu

```
Người dùng ──HTTPS──> Caddy (:443) ──┬─> frontend tĩnh (/opt/frontend/dist)
                                     └─> /api/* ──> backend :7016 ──> MySQL + Redis
```

- Chỉ Caddy nghe cổng công khai 443 (và 80 để redirect sang HTTPS). Backend
  7016, MySQL 3306, Redis 6379 **không mở ra internet** — chỉ nghe
  `127.0.0.1` hoặc mạng nội bộ của máy chủ.
- Một máy chủ duy nhất là đủ cho giai đoạn đầu; tách DB ra máy riêng chỉ khi
  chủ sở hữu quyết định có nhu cầu (xem checklist mục 6).

## 2. Reverse proxy mẫu — Caddy (lựa chọn duy nhất trong runbook này)

Chọn Caddy vì tự động cấp và gia hạn chứng chỉ Let's Encrypt, không cần chạy
certbot riêng. (Không dùng nginx trong runbook này để tránh hai cấu hình mẫu
lệch nhau; nếu chủ sở hữu bắt buộc dùng nginx thì phải viết lại và kiểm chứng
riêng, không tự suy ra từ file này.)

```caddyfile
# Caddyfile — thay <domain-that> bằng domain thật của chủ sở hữu.
<domain-that> {
    # Frontend tĩnh: bản build từ Code/medical2.0/client (npm run build).
    root * /opt/frontend/dist
    file_server

    # API: giữ đúng quy ước dev proxy /api -> backend 7016.
    # Frontend production PHẢI được build với BASE_API trỏ tới /api
    # (kiểm chứng ở checklist mục 6 trước khi go-live).
    reverse_proxy /api/* http://127.0.0.1:7016

    # Ép HTTPS, chặn header nhạy cảm rò rỉ phiên bản.
    header -Server
    # Không cache trang đăng nhập / API.
    @nocache path /api/* /login*
    header @nocache Cache-Control "no-store"
}
```

Kiểm chứng sau khi cấu hình (trên máy chủ thật, khi đã có domain):

```bash
curl -I https://<domain-that>/            # expect: 200, redirect từ http sang https
curl https://<domain-that>/actuator/health # KHÔNG đi qua Caddy công khai nếu
                                           # backend không expose trực tiếp;
                                           # health check nội bộ xem mục 5.
curl http://<domain-that>/                 # expect: redirect 301 sang https
```

Lưu ý: đừng proxy `/actuator/*` ra ngoài. Health check chỉ chạy nội bộ
(mục 5); nếu cần giám sát từ xa, giám sát qua một endpoint kiểm tra riêng
được bảo vệ, không mở actuator công khai.

## 3. HTTPS

- Caddy tự cấp chứng chỉ Let's Encrypt cho domain thật khi DNS đã trỏ đúng
  (mục 4) và cổng 80/443 mở. Không cần tự tạo chứng chỉ, không cần ghi
  private key vào repo.
- Trước go-live, kiểm tra: chứng chỉ còn hạn, trình duyệt không cảnh báo,
  `http://` redirect sang `https://`, và không còn tài nguyên nào tải qua
  `http://` (mixed content) trên trang đăng nhập và trang thu phí `/charge`.
- Gia hạn: Caddy tự gia hạn; đưa việc kiểm tra hạn chứng chỉ vào lịch kiểm
  tra hằng tuần của người vận hành (không tạo lịch tự động mới trong đợt này).

## 4. Checklist domain / DNS (việc của chủ sở hữu — chưa làm)

- [ ] Mua/chọn domain thật; quyết định dùng domain gốc hay subdomain
      (ví dụ `clinic.<domain-that>`).
- [ ] Tạo bản ghi DNS: `A` (hoặc `AAAA` nếu dùng IPv6) trỏ domain về IP
      công khai của máy chủ production. TTL thấp (ví dụ 300s) trong ngày
      chuyển đổi, rồi tăng lại sau khi ổn định.
- [ ] Chờ DNS lan truyền rồi kiểm chứng: `dig +short <domain-that>` trả về
      đúng IP máy chủ; `curl -I https://<domain-that>` trả về 200.
- [ ] Chỉ sau bước này mới bật Caddy với domain thật để cấp chứng chỉ.
- [ ] Không trỏ DNS của domain thật về máy test local, không dùng domain
      thật cho môi trường test.

## 5. Giám sát qua /actuator/health

Endpoint đã mở ẩn danh duy nhất cho monitoring:

```bash
# Chạy NỘI BỘ trên máy chủ production (backend nghe 127.0.0.1:7016):
curl -s http://127.0.0.1:7016/actuator/health
# Kỳ vọng: {"status":"UP", ...} — nếu khác UP hoặc không kết nối được,
# backend hoặc DB/Redis có vấn đề, xử lý theo mục 7 trước khi mở lại.
```

- Giám sát tối thiểu: kiểm tra `/actuator/health` mỗi 1–5 phút từ trong
  mạng máy chủ, cảnh báo khi không UP quá 2 lần liên tiếp.
- Không giám sát bằng cách gọi API nghiệp vụ có xác thực (đăng nhập, thu phí)
  theo lịch — tránh tạo dữ liệu test trong DB production.
- Log backend: theo dõi log khởi động và lỗi kết nối DB/Redis; giữ log
  backend ngoài thư mục code (log hiện tại của bản test nằm trong thư mục
  code, 180 ngày / 40 GB — trên production phải cấu hình xoay log và giới
  hạn dung lượng trước go-live, xem checklist mục 6).

## 6. Checklist trước khi go-live (tất cả phải tick mới được mở cho người dùng thật)

- [ ] Máy chủ production riêng, không dùng chung máy test; MySQL/Redis/
      backend chỉ nghe nội bộ.
- [ ] Biến môi trường production đã cấp đủ, **không dùng giá trị mặc định
      của file mẫu**: `SPRING_PROFILES_ACTIVE=prod`, `DB_URL`,
      `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (xem `application-prod.yml`
      và `Dockerfile` — các secret trong repo hiện vẫn **chưa rotate**,
      xem cảnh báo mục 8; cấp secret production hoàn toàn mới, không tái
      dùng secret của môi trường test).
- [ ] Frontend production build với `BASE_API` đúng quy ước `/api`
      (đối chiếu `vue.config.js` dev proxy); kiểm tra trang đăng nhập gọi
      đúng `https://<domain-that>/api/...`, không gọi `localhost:7016`.
- [ ] Database production là DB mới, import schema từ migration v1/v2 của
      repo, **không copy nguyên DB test `md_yzs`** (DB test chứa tài khoản
      test, dữ liệu test và router test, không dùng cho người dùng thật).
- [ ] Tài khoản quản trị production tạo mới, mật khẩu mạnh, riêng biệt;
      không mang tài khoản test (`testfull`, các tài khoản test khác) sang
      production. Quyền vai trò gốc không đổi trong đợt này; rà soát lại
      quyền từng vai trò trên DB production trước khi mở.
- [ ] Backup đã chạy thử thành công trên môi trường production (mục 7),
      gồm cả restore-test vào DB riêng.
- [ ] HTTPS, redirect http→https, health check nội bộ UP (mục 3, 5).
- [ ] Luồng nghiệp vụ chính kiểm chứng tay trên production bằng tài khoản
      quản trị: đăng nhập, đăng ký khám, khám, kê đơn, phát thuốc, thu phí
      `/charge`, in phiếu — với dữ liệu thật nhưng giao dịch test phải được
      hủy/hoàn và ghi chú rõ, không để lại doanh thu giả.
- [ ] Kế hoạch rollback (mục 7) đã được người vận hành đọc và đồng ý.

## 7. Backup và rollback

### Backup (lịch đã có — không tạo thêm)

- Lịch backup hằng ngày của Muse đã được tạo: **`viet-clinic-daily-backup`,
  02:00 Asia/Saigon**, chạy script `~/workspace/tools/backup_vietclinic.sh`.
  **Đợt này KHÔNG tạo thêm lịch nào.** Trên production, chủ sở hữu phải
  chuyển script/lịch này sang chạy trên máy chủ production (script hiện tại
  sao lưu DB test local `md_yzs`; chạy nguyên xi trên production mà không
  sửa cấu hình DB là sai) và kiểm chứng restore-test trước go-live.
- Script giữ 14 ngày gần nhất; kiểm tra định kỳ: file backup có tạo mới
  mỗi ngày không, dung lượng có bất thường không.
- Restore-test: chạy `backup_vietclinic.sh --restore-test <file-dump>`
  (tạo DB riêng `md_yzs_restore_test`, kiểm tra số bảng/dòng, rồi mới xóa
  DB test đó). **Không bao giờ import dump trực tiếp vào DB production
  đang chạy để "thử"** — restore-test chỉ làm trên DB riêng.

### Rollback (khi bản deploy mới lỗi)

1. Dừng Caddy (ngừng nhận traffic mới) hoặc chuyển Caddy về thư mục
   frontend của bản cũ.
2. Khôi phục backend về image/jar của bản cũ đã chạy ổn trước đó
   (ghi lại tag/commit của bản đang chạy trước mỗi lần deploy — nếu không
   có tag, rollback không có mốc để quay về).
3. Chỉ khôi phục DB khi lỗi do migration mới gây ra, và chỉ từ backup
   02:00 gần nhất **sau khi đã sao lưu DB hiện tại** sang file riêng
   (để không mất dữ liệu phát sinh sau backup).
4. Kiểm tra lại `/actuator/health` nội bộ và đăng nhập bằng tài khoản
   quản trị trước khi mở Caddy lại.
5. Ghi lại sự cố: bản nào lỗi, triệu chứng, đã rollback về bản nào, dữ liệu
   nào bị ảnh hưởng.

## 8. Những việc KHÔNG thuộc runbook này (còn lại của chủ sở hữu)

- Mua domain, trỏ DNS thật, cấp máy chủ production, deploy thật.
- Rotate secret trong lịch sử git và cấp secret production mới (đang là
  blocker production, chưa làm trong đợt này — không được báo cáo là đã
  rotate).
- Tích hợp BHYT / hóa đơn điện tử / liên thông: xem
  `Doc/viet-clinic-bhyt-einvoice-brief.md` — runbook này không bao gồm
  các tích hợp đó và không được hiểu là đã có chúng.
- RBAC tổng quát, xử lý các endpoint `listAll` chưa chặn, và các blocker
  production còn lại theo báo cáo audit 2026-10-30 — vẫn mở, không thuộc
  phạm vi tài liệu chuẩn bị này.

---

*Chuẩn bị ngày 2026-10-31, từ code và cấu hình thật của repo tại HEAD
`cfbbf51`. Chưa deploy thật, chưa có domain/máy chủ thật.*
