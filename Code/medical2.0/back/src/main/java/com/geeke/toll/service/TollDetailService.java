package com.geeke.toll.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.toll.dao.TollDetailDao;
import com.geeke.toll.dao.TollInfoDao;
import com.geeke.toll.entity.TollDetail;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;

/**
 * Quản lý thu phíService
 * @author lc
 * @version 2022-06-15
 */

@Service("tollDetailService")
public class TollDetailService extends CrudService<TollDetailDao, TollDetail>{

    @Autowired
    private TollInfoDao tollInfoDao;

    /**
     * N3a: chặn ghi chi tiết thu phí gắn phiếu toll của tenant khác.
     * TollDetail không có company riêng (bảng toll_detail không có cột
     * company_id, mapper không map company nào) nên guard generic của
     * CrudService luôn pass trên company null/payload — probe N3 đã
     * chứng minh insert và bulkInsert gắn toll FOREIGN đều 200 và ghi
     * thật. Guard này resolve company của phiếu toll cha bằng lookup
     * trực tiếp một cột (KHÔNG dùng TollInfoService.get() hay
     * TollDetailDao.get(): bài học N1 — get() có thể trả null vì JOIN,
     * và mapper TollDetail.get() hiện lỗi SQL tollId.name nên không
     * được dùng làm nguồn truth), rồi so với tenant phiên.
     * - Chỉ phủ NHÁNH INSERT của save (id rỗng) và bulkInsert: đó là
     *   phạm vi N3a. Nhánh UPDATE của save, delete, get, listPage
     *   KHÔNG động — chúng hiện 500 toàn cục do bug mapper
     *   tollId.name (cả dòng own), là bug chức năng riêng; khi mapper
     *   được sửa sau này phải re-probe update/delete vì mapper update/
     *   delete chỉ WHERE id, không có điều kiện company.
     * - Phiếu không tồn tại (lookup null), phiếu không mang tollId, hoặc
     *   phiên không session (tenant rỗng/"null", luồng nội bộ): cho qua
     *   như cũ — parity pre-fix (insert gắn toll không tồn tại vốn 200
     *   và tạo dòng mồ côi; không biến thành 400 mới).
     * - bulkInsert: tiền kiểm TOÀN BỘ list trước khi ghi dòng nào —
     *   fail-closed cả list (mẫu bulkDelete/N1 saveList): một dòng lạ
     *   thì không dòng nào được ghi.
     * Ném ServiceException (400) như verifyTenantOwnership của
     * CrudService, đồng nhất mã lỗi tenant của các item trước.
     */
    private void checkTollTenant(TollDetail entity) {
        if (entity == null || entity.getTollId() == null
                || StringUtils.isBlank(entity.getTollId().getId())) {
            return;
        }
        String ownerCompanyId = tollInfoDao.getTollInfoCompanyId(entity.getTollId().getId());
        if (StringUtils.isBlank(ownerCompanyId)) {
            return;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        if (!tenantId.equals(ownerCompanyId)) {
            throw new ServiceException("Không có quyền truy cập tenant khác");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public TollDetail save(TollDetail entity) {
        // N3a: chỉ nhánh insert; nhánh update giữ nguyên hành vi cũ.
        if (StringUtils.isBlank(entity.getId())) {
            checkTollTenant(entity);
        }
        return super.save(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<TollDetail> entitys) {
        // N3a: tiền kiểm toàn bộ list trước khi super ghi dòng nào.
        if (entitys != null) {
            for (TollDetail entity : entitys) {
                checkTollTenant(entity);
            }
        }
        return super.bulkInsert(entitys);
    }

}
