package com.geeke.org.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.org.dao.LesseeDao;
import com.geeke.org.entity.Lessee;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * Quản lý tenantService
 * @author txl
 * @version 2022-05-23
 */

@Service("lesseeService")
@Transactional(readOnly = true)
public class LesseeService extends CrudService<LesseeDao, Lessee>{

    /**
     * N3c: chặn xóa/đọc dòng lessee mang tên tenant khác.
     * Lessee KHÔNG có cột tenant id nào — tín hiệu chủ sở hữu duy nhất là
     * chuỗi tên `company` (varchar), không phải đối tượng Company, nên cả
     * hai guard generic của CrudService đều luôn pass ở đây:
     * verifyTenantOwnership đọc entity.getCompany() (DataEntity.company,
     * null khi load từ DB) và verifyTenantOwnershipAgainstStoredRow đọc
     * stored.getCompany() (cũng null — mapper chỉ map chuỗi a.company,
     * không map company.id). Probe N3 đã chứng minh: delete/bulkDelete/
     * get dòng lạ đều 200.
     * Quy tắc (đối chiếu trực tiếp cột DB, bài học N1/N3d — KHÔNG tin
     * dao.get() hay company object trong body):
     * - Dòng lạ = dòng có `company` trùng tên ĐÃ ĐĂNG KÝ của một
     *   org_company khác tenant phiên: dao.getCompanyIdByName tra tên
     *   thành id công ty, khác tenant phiên → 400. Phủ cả delete (body
     *   chỉ có id — chỉ kiểm tra body sẽ bỏ lọt) và bulkDelete (tiền
     *   kiểm TOÀN BỘ list trước khi ghi dòng nào, fail-closed cả list,
     *   mẫu N1/N3e) và get.
     * - Dòng own = tên trùng đúng công ty của tenant phiên → cho qua.
     * - Dòng không gắn được tenant nào (tên rỗng, hoặc tên không trùng
     *   bất kỳ org_company nào — live có cả 3 dòng như vậy, join tên
     *   trả 0 dòng — và id không tồn tại): giữ hành vi cũ (parity,
     *   tiền lệ dòng mồ côi của N3d/N3e). Chặn mù các dòng này sẽ khóa
     *   luôn việc dọn dữ liệu legacy/demo mà không chứng minh được
     *   chúng thuộc tenant nào; lỗ hổng cho dòng gắn tên tenant thật
     *   thì đã đóng.
     * - Phiên không session (tenant rỗng/"null", luồng nội bộ): cho qua
     *   như cũ.
     * - KHÔNG có nhánh dùng chung: không dòng NULL/0-company kiểu
     *   catalogue, LesseeService không khai báo isInstitutionShared
     *   (mặc định false). Công ty 297 là công ty MẸ của OWN 309 theo
     *   org_company.ids, nhưng đó là quan hệ phân cấp tổ chức, không
     *   phải policy dùng chung lessee — không áp ngoại lệ institution.
     * - Ngoài phạm vi, KHÔNG sửa: save/insert (hỏng toàn cục — serialize
     *   Company object vào cột varchar `company` gây 1366/400 validation)
     *   và listPage/listAll (500 toàn cục do ensureCompanyFilter thêm
     *   lọc company_id lên bảng không có cột đó). Sửa mapping sau này
     *   phải re-probe save/list; list 500 hiện tại không lộ dữ liệu.
     *   (ITER63 item 2 đã sửa mapping và mở rộng guard xuống dưới —
     *   đoạn này giữ lại làm lịch sử N3c; xem khối ITER63.)
     * Ném ServiceException (400) như verifyTenantOwnership của
     * CrudService. Không sửa CrudService generic, không sửa mapper
     * update/delete; chỉ thêm hai SELECT trực tiếp phục vụ guard.
     */
    public void checkLesseeTenant(Lessee entity) {
        if (entity == null) {
            return;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        if (StringUtils.isBlank(entity.getId())) {
            return;
        }
        String storedName = dao.getCompanyNameById(entity.getId());
        if (StringUtils.isBlank(storedName)) {
            return;
        }
        String ownerId = dao.getCompanyIdByName(storedName);
        if (StringUtils.isBlank(ownerId)) {
            return;
        }
        if (!tenantId.equals(ownerId)) {
            throw new ServiceException("Không có quyền truy cập tenant khác");
        }
    }

    /**
     * ITER63 item 2: chặn ghi tên công ty của tenant khác — cùng một truth
     * như checkLesseeTenant nhưng xét tên trong PAYLOAD (chặn tạo mới mang
     * tên lạ và re-home dòng own sang tên lạ). Tên rỗng, tên không trùng
     * bất kỳ org_company nào, hoặc phiên không session: cho qua như N3c
     * (parity dòng mồ côi / legacy). Phiên có tenant mà tên trùng đúng
     * công ty của tenant phiên: cho qua.
     */
    public void checkLesseeNameTenant(String companyName) {
        if (StringUtils.isBlank(companyName)) {
            return;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        String ownerId = dao.getCompanyIdByName(companyName);
        if (StringUtils.isBlank(ownerId)) {
            return;
        }
        if (!tenantId.equals(ownerId)) {
            throw new ServiceException("Không có quyền truy cập tenant khác");
        }
    }

    /**
     * ITER63 item 2: save đã sửa mapping (companyName, xem Lessee.java)
     * nên giờ đi được tới đây thay vì 500/400 như trước — vì vậy PHẢI
     * kèm guard, không được để đường ghi mới mở mà không chặn (quy tắc
     * item 1 TollDetail). Insert: chặn tên payload lạ. Update: chặn cả
     * dòng đã lưu lạ (checkLesseeTenant, N3c) và tên payload lạ (re-home).
     * Generic verify của CrudService vẫn cho qua (getCompany() null) như
     * trước. @Transactional(readOnly = false) tường minh vì class-level
     * readOnly = true (mẫu N3d).
     */
    @Override
    @Transactional(readOnly = false)
    public Lessee save(Lessee entity) {
        if (entity != null && StringUtils.isBlank(entity.getId())) {
            checkLesseeNameTenant(entity.getCompanyName());
        } else if (entity != null) {
            checkLesseeTenant(entity);
            checkLesseeNameTenant(entity.getCompanyName());
            // DS106 C1: save voi id khong ton tai truoc day roi qua
            // CrudService.save -> doUpdate 0 dong roi van 200 silent
            // no-op (checkLesseeTenant bo qua khi stored null, cung mau
            // DS94/DS99). Kiem tra ton tai truoc moi ghi, sau guard ten.
            // Truong bat buoc (userName/phoneNumber/isUse @NotNull) da
            // duoc @Valid o controller chan o save don.
            if (dao.get(entity.getId()) == null) {
                throw new ServiceException("Thuê bao không tồn tại: "
                        + entity.getId());
            }
        }
        return super.save(entity);
    }

    /**
     * ITER63 item 2: bulkInsert tiền kiểm TOÀN BỘ list trước khi ghi
     * dòng nào — một tên lạ thì không dòng nào được ghi (fail-closed).
     */
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<Lessee> entitys) {
        // DS106 C4/C6/C10/C11: bulkInsert truoc day de lo: null/rong roi
        // xuong for-loop (NPE 500) / MyBatis <foreach> rong (500 SQL);
        // thieu userName/phoneNumber (NOT NULL) -> 500 vi bulk bypass
        // @Valid. Chan het o service de 400 ro nghia, truoc vong tenant.
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách thuê bao không được để trống");
        }
        for (Lessee entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Thuê bao không được để trống");
            }
            if (StringUtils.isBlank(entity.getUserName())) {
                throw new ServiceException("Tên tenant không được để trống");
            }
            if (StringUtils.isBlank(entity.getPhoneNumber())) {
                throw new ServiceException("Số điện thoại không được để trống");
            }
            if (StringUtils.isBlank(entity.getIsUse())) {
                throw new ServiceException("Trạng thái không được để trống");
            }
        }
        if (entitys != null) {
            for (Lessee entity : entitys) {
                if (entity != null) {
                    checkLesseeNameTenant(entity.getCompanyName());
                }
            }
        }
        return super.bulkInsert(entitys);
    }

    /**
     * ITER63 item 2: bulkUpdate tiền kiểm TOÀN BỘ list, cả dòng đã lưu
     * và tên payload mới, trước khi ghi dòng nào (fail-closed).
     */
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<Lessee> entitys) {
        // DS106 C2/C3/C5/C9/C12: bulkUpdate truoc day de lo: null/rong ->
        // 500; id khong ton tai -> 200 silent no-op; batch hon hop doi ca
        // dong hop le; isUse bi drop -> CASE set is_use=NULL -> 500 NOT
        // NULL. Tien kiem TOAN BO list (ton tai + truong bat buoc) truoc
        // moi ghi (fail-closed ca list, mau N1), truoc vong tenant.
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách thuê bao không được để trống");
        }
        for (Lessee entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Thuê bao không được để trống");
            }
            if (StringUtils.isNotBlank(entity.getId())
                    && dao.get(entity.getId()) == null) {
                throw new ServiceException("Thuê bao không tồn tại: "
                        + entity.getId());
            }
            if (StringUtils.isBlank(entity.getUserName())) {
                throw new ServiceException("Tên tenant không được để trống");
            }
            if (StringUtils.isBlank(entity.getPhoneNumber())) {
                throw new ServiceException("Số điện thoại không được để trống");
            }
            if (StringUtils.isBlank(entity.getIsUse())) {
                throw new ServiceException("Trạng thái không được để trống");
            }
        }
        if (entitys != null) {
            for (Lessee entity : entitys) {
                if (entity != null) {
                    checkLesseeTenant(entity);
                    checkLesseeNameTenant(entity.getCompanyName());
                }
            }
        }
        return super.bulkUpdate(entitys);
    }

    /**
     * ITER63 item 2: lọc tenant cho list — Lessee KHÔNG có cột tenant id
     * nên không thể lọc SQL theo company_id (đó chính là nguyên nhân
     * listPage/listAll 500 trước đây: ensureCompanyFilter chèn lọc
     * company_id lên bảng không có cột đó; sau khi bỏ field `company`
     * che ở Lessee.java, ensureCompanyFilter đã bỏ qua Lessee).
     * Lọc ở service theo cùng truth tên→org_company như guard N3c:
     * giữ dòng tên trùng tenant phiên, dòng tên rỗng/không trùng công ty
     * nào (parity N3c), và mọi dòng khi phiên không session; ẩn dòng
     * tên trùng công ty khác — nếu không, sửa mapping sẽ mở đường đọc
     * lách guard get của N3c qua list. Lọc trong bộ nhớ sau khi lấy
     * toàn bộ dòng khớp tham số client (bảng quản trị, vài dòng; tránh
     * JOIN org_company theo tên vì tên không unique-constrained nên
     * JOIN có thể nhân dòng và làm get/count sai), rồi phân trang tay
     * để total khớp đúng số dòng đã lọc. Tham số company_id do client
     * gửi bị loại trước (chống spoof và tránh 500 vì bảng không có cột
     * đó, mẫu TollDetail).
     */
    private List<Lessee> tenantVisibleLessees(List<Parameter> parameters, String orderby) {
        List<Parameter> params = parameters == null
                ? new java.util.ArrayList<Parameter>() : parameters;
        Parameter.extractAndRemoveCompanyId(params);
        List<Lessee> all = dao.listAll(new PageRequest(params, orderby));
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return all;
        }
        Map<String, String> ownerByName = Maps.newHashMap();
        List<Lessee> visible = new java.util.ArrayList<Lessee>();
        for (Lessee row : all) {
            String name = row == null ? null : row.getCompanyName();
            if (StringUtils.isBlank(name)) {
                visible.add(row);
                continue;
            }
            if (!ownerByName.containsKey(name)) {
                ownerByName.put(name, dao.getCompanyIdByName(name));
            }
            String ownerId = ownerByName.get(name);
            if (StringUtils.isBlank(ownerId) || tenantId.equals(ownerId)) {
                visible.add(row);
            }
        }
        return visible;
    }

    @Override
    public Page<Lessee> listPage(List<Parameter> parameters, int offset, int limit, String orderby) {
        List<Lessee> visible = tenantVisibleLessees(parameters, orderby);
        int from = Math.max(0, offset);
        int pageSize = limit < 1 ? visible.size() : limit;
        int to = Math.min(visible.size(), from + pageSize);
        List<Lessee> page = from >= visible.size()
                ? new java.util.ArrayList<Lessee>()
                : new java.util.ArrayList<Lessee>(visible.subList(from, to));
        return new Page<Lessee>(visible.size(), page);
    }

    @Override
    public List<Lessee> listAll(List<Parameter> parameters, String orderby) {
        return tenantVisibleLessees(parameters, orderby);
    }

    /**
     * N3c: chặn đọc dòng lạ. id không tồn tại (stored null) hoặc phiên
     * không session: cho qua như cũ (200 data null).
     */
    @Override
    public Lessee get(String id) {
        if (StringUtils.isNotBlank(id)) {
            checkLesseeTenant(new Lessee(id));
        }
        return super.get(id);
    }

    /**
     * N3c: chặn xóa dòng lạ. @Transactional(readOnly = false) tường minh
     * là bắt buộc vì service này khai báo class-level readOnly = true
     * (mẫu N3d) — override không annotation sẽ kế thừa readOnly và
     * không commit được xóa.
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(Lessee entity) {
        // DS131 C: checkLesseeTenant cho qua im lang id trong/ghost
        // (getCompanyNameById tra blank) nen ghost delete 200 rows 0 im
        // lang, blank 200, xoa lap 200 rows 1 (mapper soft-delete khong
        // loc del_flag) -- cung lop DS123/DS129/DS130. Chan truoc bang
        // dao.get (khong loc del_flag nen tra ca dong da xoa mem,
        // delFlag phan biet) theo mau CodeRuleService.delete (DS129 A);
        // KHONG tai dung checkExists (DS103): no cho qua blank va khong
        // check delFlag, lai dung chung voi save/bulkUpdate; giu
        // checkLesseeTenant sau do (dong live la tenant van 400).
        // bulkDelete ghost GIU NGUYEN 200 rows 0 (DS99/DS106/DS126 B).
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được thuê bao cần xóa.");
        }
        Lessee stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Thuê bao không tồn tại: "
                    + entity.getId());
        }
        checkLesseeTenant(entity);
        return super.delete(entity);
    }

    /**
     * N3c: tiền kiểm TOÀN BỘ list trước khi ghi dòng nào — một dòng lạ
     * thì không dòng nào được xóa (fail-closed cả list, mẫu N1/N3e).
     */
    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<Lessee> entitys) {
        // DS106 C7-C8: bulkDelete null/rong -> 400 ro nghia thay vi NPE
        // 500 o vong tenant / <foreach> rong. bulkDelete id khong ton tai
        // GIU NGUYEN 200 rows 0 idempotent (DS99 G11).
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách thuê bao không được để trống");
        }
        if (entitys != null) {
            for (Lessee entity : entitys) {
                checkLesseeTenant(entity);
            }
        }
        return super.bulkDelete(entitys);
    }

}
