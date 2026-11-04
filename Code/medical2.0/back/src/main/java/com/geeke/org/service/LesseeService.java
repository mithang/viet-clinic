package com.geeke.org.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        if (entitys != null) {
            for (Lessee entity : entitys) {
                checkLesseeTenant(entity);
            }
        }
        return super.bulkDelete(entitys);
    }

}
