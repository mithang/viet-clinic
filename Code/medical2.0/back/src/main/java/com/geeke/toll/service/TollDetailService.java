package com.geeke.toll.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.data.Page;
import com.geeke.common.data.Parameter;
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
     * TollDetailDao.get(): bài học N1 — get() có thể trả null vì JOIN),
     * rồi so với tenant phiên.
     * - N3a chỉ phủ NHÁNH INSERT của save (id rỗng) và bulkInsert.
     * - ITER63 mở rộng sang mọi đường còn lại (mapper tollId.name đã
     *   sửa nên các đường này không còn 500, và mapper update/delete
     *   chỉ WHERE id nên không kèm guard sẽ thành lỗ mới — tuyệt đối
     *   không commit mapper fix đơn lẻ):
     *   + save-UPDATE: kiểm tra cả phiếu cha ĐÃ LƯU của dòng (chặn ghi
     *     đè dòng FOREIGN dù payload mang toll own) và phiếu cha MỚI
     *     trong payload (chặn re-home dòng own sang toll FOREIGN).
     *   + delete / bulkDelete: kiểm tra phiếu cha đã lưu của từng id.
     *   + bulkUpdate: kiểm tra cả hai chiều cho từng item, tiền kiểm
     *     toàn bộ list trước khi ghi dòng nào (fail-closed cả list).
     *   + get: kiểm tra phiếu cha của dòng trả về.
     *   + listPage / listAll: lọc SQL theo company phiếu cha
     *     (tollId.company_id = tenant phiên, JOIN tollId đã có sẵn
     *     trong mapper) — TollDetail không khai báo field company riêng
     *     nên ensureCompanyFilter của CrudService mù với nó, phải lọc
     *     riêng ở đây. Dòng mồ côi (toll không tồn tại, tollId.id IS
     *     NULL) vẫn hiện trong list như parity get (lookup null cho
     *     qua, xem dưới).
     * - Phiếu không tồn tại (lookup null), phiếu không mang tollId, hoặc
     *   phiên không session (tenant rỗng/"null", luồng nội bộ): cho qua
     *   như cũ — parity pre-fix (insert gắn toll không tồn tại vốn 200
     *   và tạo dòng mồ côi; dòng mồ côi không biến thành 400 mới).
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
        checkTollCompany(entity.getTollId().getId());
    }

    /** ITER63: kiểm tra company của phiếu cha đã lưu theo id dòng chi
     * tiết — nguồn truth là cột toll_id trong DB, không phải payload.
     * Dòng không tồn tại / toll_id rỗng / phiếu cha không tồn tại đều
     * cho qua (parity N3c cho id không tồn tại và dòng mồ côi). */
    private void checkStoredTollTenant(String detailId) {
        if (StringUtils.isBlank(detailId)) {
            return;
        }
        String storedTollId = dao.getTollDetailTollId(detailId);
        if (StringUtils.isBlank(storedTollId)) {
            return;
        }
        checkTollCompany(storedTollId);
    }

    /** So company của một phiếu toll với tenant phiên; lạ -> 400. */
    private void checkTollCompany(String tollId) {
        String ownerCompanyId = tollInfoDao.getTollInfoCompanyId(tollId);
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

    /**
     * ITER63: tham số lọc tenant cho listPage/listAll. Loại bỏ company_id
     * do client gửi trước (tránh spoof), rồi thêm nhóm
     * (tollId.company_id = tenant phiên OR tollId.id IS NULL) — nhóm OR
     * giữ dòng mồ côi trong list như parity get. Logic "" ở điều kiện
     * đầu nhóm vì whereParams render "(" rồi tới điều kiện (không được
     * để AND thừa sau "("). Phiên không tenant thì không lọc (luồng nội
     * bộ). Lưu ý: extractAndRemoveCompanyId phải chạy TRƯỚC khi thêm
     * điều kiện tollId.company_id, vì isCompanyIdColumn cũng khớp hậu tố
     * company_id — và ensureCompanyFilter của CrudService không chạm
     * tới các tham số này vì TollDetail không khai báo field company.
     */
    private List<Parameter> tenantFilteredParams(List<Parameter> parameters) {
        List<Parameter> params = parameters == null
                ? new java.util.ArrayList<Parameter>() : parameters;
        Parameter.extractAndRemoveCompanyId(params);
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return params;
        }
        params.add(new Parameter(null, "(", null, "AND"));
        params.add(new Parameter("tollId.company_id", "=", tenantId, ""));
        params.add(new Parameter("tollId.id", "is null", null, "or"));
        params.add(new Parameter(null, ")", null, ""));
        return params;
    }

    /**
     * DS127 A: save ghost id fail-closed 400 (DS94/DS120 class). Guard
     * tenant hien co khong chan duoc: checkStoredTollTenant cho qua khi
     * id khong ton tai (lookup toll_id tra null), nen save voi id la roi
     * thang vao CrudService.save -> doUpdate 0 dong -> 200 silent no-op
     * (probe A1). Chi chap nhan update khi dong da ton tai (mau DS99/
     * DS101); kiem tra bang countById truc tiep — getTollDetailTollId
     * khong dung duoc vi no tra null ca khi dong ton tai nhung toll_id
     * NULL. Insert (id rong) giu nguyen: ghi dong mo coi gan toll khong
     * ton tai van 200 nhu parity cu (ghi chu o checkTollTenant).
     */
    @Override
    @Transactional(readOnly = false)
    public TollDetail save(TollDetail entity) {
        if (entity == null) {
            throw new ServiceException("Chi tiết thu phí không được để trống");
        }
        if (StringUtils.isNotBlank(entity.getId()) && dao.countById(entity.getId()) == 0) {
            throw new ServiceException("Chi tiết thu phí không tồn tại: " + entity.getId());
        }
        if (StringUtils.isBlank(entity.getId())) {
            // N3a: nhánh insert — kiểm tra phiếu cha trong payload.
            checkTollTenant(entity);
        } else {
            // ITER63: nhánh update — kiểm tra cả phiếu cha đã lưu
            // (chặn ghi đè dòng FOREIGN) và phiếu cha mới trong payload
            // (chặn re-home dòng own sang toll FOREIGN).
            checkStoredTollTenant(entity.getId());
            checkTollTenant(entity);
        }
        return super.save(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<TollDetail> entitys) {
        // DS127 A: danh sach null/rong, phan tu null -> 400 (mau DS120).
        // Truoc day [null] NPE 500 o CrudService.bulkInsert (preInsert),
        // null/[] roi toi dao voi <foreach> rong (probe A2/A3/A4).
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chi tiết thu phí thêm mới không được để trống");
        }
        // N3a: tiền kiểm toàn bộ list trước khi super ghi dòng nào.
        for (TollDetail entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Chi tiết thu phí không được để trống");
            }
            checkTollTenant(entity);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<TollDetail> entitys) {
        // DS127 A: danh sach null/rong, phan tu null, id rong, id la ->
        // 400 (mau DS120/DS102). Truoc day bulkUpdate id la 200 silent
        // (CASE 0 dong) va list tron [valid, ghost] ghi mot phan
        // (probe A6/A7). Tien kiem TOAN BO list truoc khi ghi dong nao.
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chi tiết thu phí cập nhật không được để trống");
        }
        // ITER63: tiền kiểm toàn bộ list, cả hai chiều, trước khi ghi.
        for (TollDetail entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Chi tiết thu phí không được để trống");
            }
            if (StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của chi tiết thu phí đã tồn tại");
            }
            if (dao.countById(entity.getId()) == 0) {
                throw new ServiceException("Chi tiết thu phí không tồn tại: " + entity.getId());
            }
            checkStoredTollTenant(entity.getId());
            checkTollTenant(entity);
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int delete(TollDetail entity) {
        // DS119 A: mapper <update id="delete"> co nhanh <otherwise> xoa
        // CUNG (DELETE FROM toll_detail) theo toll_id khi id rong --
        // delete khong id + tollId se xoa sach moi dong cua phieu thu.
        // Chan o service: chi xoa khi co id; khong caller noi bo nao
        // dung duong khong id (kiem tra 2026-11-07).
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được chi tiết thu phí cần xóa.");
        }
        // ITER63: kiểm tra phiếu cha đã lưu trước khi xóa.
        checkStoredTollTenant(entity.getId());
        return super.delete(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<TollDetail> entitys) {
        // ITER63: tiền kiểm toàn bộ list trước khi xóa dòng nào.
        if (entitys != null) {
            for (TollDetail entity : entitys) {
                if (entity != null) {
                    checkStoredTollTenant(entity.getId());
                }
            }
        }
        return super.bulkDelete(entitys);
    }

    @Override
    public TollDetail get(String id) {
        TollDetail entity = super.get(id);
        // ITER63: super.get chỉ verify company null (luôn pass với
        // TollDetail) nên kiểm tra thêm phiếu cha của dòng trả về.
        // Dòng mồ côi (toll không tồn tại) cho qua như parity N3a.
        checkTollTenant(entity);
        return entity;
    }

    @Override
    public Page<TollDetail> listPage(List<Parameter> parameters, int offset, int limit, String orderby) {
        return super.listPage(tenantFilteredParams(parameters), offset, limit, orderby);
    }

    @Override
    public List<TollDetail> listAll(List<Parameter> parameters, String orderby) {
        return super.listAll(tenantFilteredParams(parameters), orderby);
    }

}
