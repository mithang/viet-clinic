#!/usr/bin/env bash
# setup.sh — Chạy dự án Viet Clinic trên máy local chỉ bằng Docker.
#
#   ./setup.sh           Khởi động toàn bộ (MySQL + Redis + backend + frontend),
#                        tự import DB, tạo tài khoản test, kiểm chứng login.
#   ./setup.sh stop      Dừng các container, giữ lại dữ liệu DB.
#   ./setup.sh reset     Xóa hết dữ liệu DB và dựng lại từ đầu (--yes để khỏi hỏi).
#   ./setup.sh logs [tên dịch vụ]   Xem log (mặc định xem tất cả).
#   ./setup.sh status    Trạng thái container + kiểm tra nhanh 4 cổng.
#
# Yêu cầu: Docker + Docker Compose (v2: `docker compose`), python3, curl.
#   - macOS / Linux: cài Docker Desktop rồi chạy script này.
#   - Windows: chạy trong WSL2 (Ubuntu) hoặc Git Bash, Docker Desktop bật
#     chế độ WSL2 backend.
#
# Lần chạy đầu sẽ lâu (build backend jar + npm install + import ~30MB SQL,
# khoảng 5–10 phút tùy máy). Các lần sau chỉ mất vài chục giây.
# Mật khẩu MySQL root, JWT secret và mật khẩu tài khoản test được sinh ngẫu
# nhiên lần đầu và lưu vào docker/.env (đã gitignore) để lần sau dùng lại.
# Muốn tự đặt mật khẩu test thì tạo docker/.env trước với TEST_PASSWORD=...

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"
ENV_FILE="$ROOT/docker/.env"
COMPOSE_FILE="$ROOT/docker-compose.yml"
TEST_COMPANY_ID="2077468568630583309"   # Phòng khám kiểm tra (H000123)

info()  { echo "[setup] $*"; }
fail()  { echo "[setup] LỖI: $*" >&2; exit 1; }

rand_hex() { # rand_hex <bytes>
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -hex "$1"
  else
    head -c "$1" /dev/urandom | od -An -tx1 | tr -d ' \n'
  fi
}

# --- phát hiện docker compose v2 / v1 ---------------------------------------
if docker compose version >/dev/null 2>&1; then
  DC=(docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE")
elif command -v docker-compose >/dev/null 2>&1; then
  DC=(docker-compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE")
else
  fail "Không tìm thấy Docker Compose. Cài Docker Desktop (kèm Compose v2) rồi chạy lại."
fi
command -v python3 >/dev/null 2>&1 || fail "Cần python3 (để băm mật khẩu test theo Shiro). Cài python3 rồi chạy lại."
command -v curl >/dev/null 2>&1 || fail "Cần curl (để kiểm chứng backend/frontend). Cài curl rồi chạy lại."

# --- sinh / nạp secrets ------------------------------------------------------
if [ ! -f "$ENV_FILE" ]; then
  info "Chưa có docker/.env — sinh mật khẩu ngẫu nhiên..."
  {
    echo "MYSQL_ROOT_PASSWORD=$(rand_hex 16)"
    echo "JWT_SECRET=$(rand_hex 32)"
    if [ -n "${VIETCLINIC_TEST_PASSWORD:-}" ]; then
      echo "TEST_PASSWORD=$VIETCLINIC_TEST_PASSWORD"
    else
      echo "TEST_PASSWORD=$(rand_hex 8)"
    fi
  } > "$ENV_FILE"
  chmod 600 "$ENV_FILE"
  info "Đã ghi docker/.env (quyền 600, không commit lên git)."
fi
set -a; . "$ENV_FILE"; set +a
[ -n "${MYSQL_ROOT_PASSWORD:-}" ] || fail "docker/.env thiếu MYSQL_ROOT_PASSWORD."
[ -n "${JWT_SECRET:-}" ] || fail "docker/.env thiếu JWT_SECRET."
[ -n "${TEST_PASSWORD:-}" ] || fail "docker/.env thiếu TEST_PASSWORD."

# --- helper ------------------------------------------------------------------
port_busy() { (exec 3<>"/dev/tcp/127.0.0.1/$1") 2>/dev/null; }
mysql_exec() { # mysql_exec <sql...> — chạy SQL trong container mysql
  "${DC[@]}" exec -T mysql mysql --default-character-set=utf8mb4 -uroot -p"$MYSQL_ROOT_PASSWORD" "$@"
}
wait_for() { # wait_for <giây> <mô tả> <lệnh...>
  local timeout_s="$1" label="$2"; shift 2
  local i=0
  while [ "$i" -lt "$timeout_s" ]; do
    if "$@" >/dev/null 2>&1; then return 0; fi
    sleep 5; i=$((i + 5))
  done
  fail "Hết ${timeout_s}s chờ ${label}."
}

cmd_status() {
  "${DC[@]}" ps
  for p in 3306 6379 7016 7020; do
    if port_busy "$p"; then info "Cổng $p: đang nghe"; else info "Cổng $p: chưa nghe"; fi
  done
  curl -s -m 5 http://localhost:7016/actuator/health || echo "(backend /actuator/health chưa trả lời)"
  echo
}

cmd_logs() {
  if [ "${1:-}" != "" ]; then "${DC[@]}" logs -f "$1"; else "${DC[@]}" logs -f; fi
}

cmd_stop() {
  info "Dừng các container (giữ lại dữ liệu trong volume)..."
  "${DC[@]}" down
  info "Đã dừng. Chạy lại bằng ./setup.sh"
}

cmd_reset() {
  if [ "${1:-}" != "--yes" ]; then
    read -r -p "reset sẽ XÓA HẾT dữ liệu DB trong volume mysql-data. Tiếp tục? [y/N] " ans
    [ "$ans" = "y" ] || [ "$ans" = "Y" ] || { info "Đã hủy reset."; exit 0; }
  fi
  info "Xóa container + volume dữ liệu..."
  "${DC[@]}" down -v
  cmd_up
}

cmd_up() {
  # 1. Cổng trống
  for p in 3306 6379 7016 7020; do
    if port_busy "$p"; then
      fail "Cổng $p đã có dịch vụ khác nghe. Dừng dịch vụ đó (hoặc đổi cổng trong docker-compose.yml) rồi chạy lại."
    fi
  done

  # 2. Build image backend + frontend
  info "Build image backend + frontend (lần đầu tải dep Maven/npm nên sẽ lâu)..."
  "${DC[@]}" build

  # 3. MySQL + Redis, chờ DB khởi tạo xong (đủ 149 router)
  info "Khởi động MySQL + Redis..."
  "${DC[@]}" up -d mysql redis
  wait_for 120 "MySQL trả PONG" "${DC[@]}" exec -T mysql mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD"
  info "MySQL đã lên. Chờ import DB (dump + migrations v1..v19 + seed, khoảng 1–3 phút)..."
  # Đủ 149 router CHƯA chắc DB đã import xong: dump tạo/bơm bảng theo thứ tự
  # alphabet nên sys_router (149 dòng) có trước khi sys_user được tạo và trước
  # khi migrations/seed chạy. Role 4000 chỉ được seed ở bước cuối cùng, nên
  # dùng nó làm mốc "DB đã khởi tạo xong".
  i=0
  while [ "$i" -lt 600 ]; do
    n=$(mysql_exec -N -e "SELECT COUNT(*) FROM md_yzs.sys_router;" 2>/dev/null || echo 0)
    seeded=$(mysql_exec -N -e "SELECT COUNT(*) FROM md_yzs.sys_role WHERE id=4000 AND del_flag='0';" 2>/dev/null || echo 0)
    if [ "$n" = "149" ] && [ "$seeded" = "1" ]; then break; fi
    sleep 5; i=$((i + 5))
  done
  [ "${n:-0}" = "149" ] || fail "DB chưa đủ 149 router sau 600s (đo được: ${n:-không rõ}). Xem log: ./setup.sh logs mysql"
  [ "${seeded:-0}" = "1" ] || fail "DB chưa chạy xong migrations/seed sau 600s (role 4000 chưa có). Xem log: ./setup.sh logs mysql"
  info "DB đã đủ 149 router và seed xong."

  # 4. Tài khoản test (mật khẩu = TEST_PASSWORD trong docker/.env,
  #    hash Shiro Md5Hash salt=userId 10000 vòng — khớp backend HashPwd).
  info "Tạo/reset 20 tài khoản test..."
  hash_for() { python3 "$ROOT/docker/mysql/hash_shiro.py" "$1" "$TEST_PASSWORD"; }
  # Reset mật khẩu các tài khoản gốc đã có sẵn trong dump.
  for uid in 1000 1001 2077468568630583314; do
    h=$(hash_for "$uid")
    mysql_exec -N -e "UPDATE md_yzs.sys_user SET login_password='${h}' WHERE id=${uid};" >/dev/null
  done
  # id|login|tên|company|role|user_role_id
  while IFS='|' read -r uid login uname company role urid; do
    [ -z "$uid" ] && continue
    h=$(hash_for "$uid")
    # </dev/null: mysql (qua docker compose exec -T) đọc hết stdin, nếu không
    # chặn sẽ nuốt luôn các dòng USERS còn lại của heredoc.
    mysql_exec --default-character-set=utf8mb4 -N -e \
      "INSERT INTO md_yzs.sys_user (id, company_id, name, login_name, login_password, is_locked, phone, email, create_by, create_date, update_by, update_date, del_flag, is_admin) VALUES (${uid}, ${company}, '${uname}', '${login}', '${h}', '0', '', '', 'system', NOW(), 'system', NOW(), '0', '0') ON DUPLICATE KEY UPDATE login_password='${h}', del_flag='0';" >/dev/null </dev/null
    mysql_exec -N -e \
      "INSERT INTO md_yzs.sys_user_role (id, user_id, role_id, company_id, create_by, create_date, update_by, update_date, del_flag) VALUES (${urid}, ${uid}, ${role}, ${company}, 'system', NOW(), 'system', NOW(), '0') ON DUPLICATE KEY UPDATE del_flag='0', company_id=${company};" >/dev/null </dev/null
  done <<'USERS'
3000|testdoctor|Bác sĩ test|2077468568630584464|2099436828135891044|9000
3001|testys|Bác sĩ YS test|2077468568630583309|2298416718726168646|9001
3002|testyaoshi|Dược sĩ test|2077468568630583309|2521363969936572416|9002
3003|testsup|Nhà cung cấp test|0|2174053741646364675|9003
3004|testbase|Cơ sở test|0|2077468568630583721|9004
3005|testclinic|Phòng khám test|2077468568630583309|2077468568630583319|9005
3010|testupgrade|Upgrade test|0|987820505027657851|9010
3011|testflag|Flagship test|0|987820505027657853|9011
3012|testcustom|Custom test|0|987820505027657855|9012
3013|test1029|Test 1029|0|2314654134646620573|9013
3014|testadm1|Admin c3300 test|2077468568630583300|2077468568630583302|9014
3015|testadm2|Admin c3309 test|2077468568630583309|2077468568630583311|9015
3016|testadm3|Admin c4464 test|2077468568630584464|2077468568630584466|9016
3017|testadm4|Admin c5045 test|2084869364217815045|2084869364217815047|9017
3018|testadm5|Admin c6433 test|2097952229740306433|2097952229740306435|9018
3019|testadm6|Admin c4755 test|2174053741646364755|2174053741646364757|9019
3020|testfull|Toàn quyền test|2077468568630583309|4000|9020
USERS
  info "Đã tạo/reset 20 tài khoản test."

  # 5. Backend + frontend
  info "Khởi động backend + frontend..."
  "${DC[@]}" up -d backend frontend
  wait_for 300 "backend /actuator/health trả 200" bash -c 'curl -s -m 5 -o /dev/null -w "%{http_code}" http://localhost:7016/actuator/health | grep -q "^200$"'
  info "Backend đã lên (7016)."
  wait_for 300 "frontend 7020 trả 200" bash -c 'curl -s -m 5 -o /dev/null -w "%{http_code}" http://localhost:7020/ | grep -q "^200$"'
  info "Frontend đã lên (7020)."

  # 6. Kiểm chứng login thật bằng testfull (phải trả 127 router).
  info "Kiểm chứng đăng nhập bằng testfull..."
  routers=$(curl -s -m 30 -X POST \
    --data-urlencode "loginName=testfull" \
    --data-urlencode "password=$TEST_PASSWORD" \
    --data-urlencode "companyId=$TEST_COMPANY_ID" \
    http://localhost:7016/auth/token | python3 -c "
import json, sys
def find(o):
    if isinstance(o, dict):
        for k, v in o.items():
            if k in ('routers', 'routerList') and isinstance(v, list):
                return len(v)
            n = find(v)
            if n >= 0: return n
    if isinstance(o, list):
        for v in o:
            n = find(v)
            if n >= 0: return n
    return -1
try:
    print(find(json.load(sys.stdin)))
except Exception:
    print(-1)
")
  if [ "${routers:-0}" != "127" ]; then
    fail "Đăng nhập testfull không trả 127 router (đo được: ${routers:-không rõ}). Xem log backend: ./setup.sh logs backend"
  fi

  echo
  echo "=== VIET CLINIC ĐÃ CHẠY — PASS toàn bộ ==="
  echo "  App:      http://localhost:7020"
  echo "  Backend:  http://localhost:7016  (health: /actuator/health)"
  echo "  MySQL:    127.0.0.1:3306  DB md_yzs  (user root, mật khẩu trong docker/.env)"
  echo "  Redis:    127.0.0.1:6379"
  echo "  Đăng nhập: testfull / $TEST_PASSWORD  (công ty: Phòng khám kiểm tra)"
  echo "  Tài khoản khác: testys, testdoctor, testsup... — cùng mật khẩu trên."
  echo "  Dừng:     ./setup.sh stop     Xem log: ./setup.sh logs [ten]"
  echo "  Dựng lại DB từ đầu: ./setup.sh reset"
  echo
}

case "${1:-up}" in
  up)     cmd_up ;;
  stop)   cmd_stop ;;
  reset)  cmd_reset "${2:-}" ;;
  logs)   cmd_logs "${2:-}" ;;
  status) cmd_status ;;
  -h|--help|help)
    sed -n '2,20p' "$0"
    ;;
  *) fail "Lệnh không rõ: $1 — dùng: ./setup.sh [up|stop|reset|logs|status]" ;;
esac
