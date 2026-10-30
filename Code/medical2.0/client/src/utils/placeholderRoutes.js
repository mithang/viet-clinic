/**
 * Các route placeholder "Chức năng chưa triển khai" (15 route).
 *
 * Cơ chế ẩn an toàn cho production (blocker 5a):
 * - Chỉ ẩn ở tầng hiển thị của bản production build (NODE_ENV === 'production'):
 *   router/index.js không đăng ký các route này, store/modules/menus.js không
 *   đưa chúng vào menu. Bản dev/test vẫn hiện để còn kiểm chứng được.
 * - KHÔNG xóa route khỏi DB (sys_router), KHÔNG đổi quyền của bất kỳ vai trò
 *   gốc nào, KHÔNG lật del_flag/is_locked (company del_flag=1 và department
 *   is_locked=1 — lật flag sẽ lộ menu cho vai trò gốc 2001).
 * - Đây chỉ là ẩn giao diện, KHÔNG phải ranh giới bảo mật: quyền gọi API vẫn
 *   do backend/RBAC quyết định (nhóm RBAC sở hữu phần đó).
 *
 * Mã route = đường dẫn thư mục view, khớp với cách router/index.js gọi
 * _import(router.code + '/index').
 */
export const PLACEHOLDER_ROUTE_CODES = [
  '01',
  '02',
  '03',
  '04',
  'workbench/mytask',
  'workbench/myprocess',
  'workbench/myapplied',
  'workbench/myapproved',
  'workbench/myterminated',
  'wf/wfDraft',
  'wf/designer',
  'wf/process',
  'wf/task',
  'test/leave',
  'test/expense'
]

export function isPlaceholderRoute(code) {
  return PLACEHOLDER_ROUTE_CODES.indexOf(code) !== -1
}

export function shouldHidePlaceholderRoutes() {
  return process.env.NODE_ENV === 'production'
}
