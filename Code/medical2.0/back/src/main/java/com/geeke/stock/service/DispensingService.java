package com.geeke.stock.service;

import java.util.List;
import java.util.Map;

import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.stock.entity.DispensingReportEvt;
import com.geeke.stock.entity.DispensingReportTotalEvt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.stock.dao.DispensingDao;
import com.geeke.stock.entity.Dispensing;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * Chi tiết phát thuốcService
 * @author txl
 * @version 2022-08-11
 */
 
@Service("dispensingService")
@Transactional(readOnly = true)
public class DispensingService extends CrudService<DispensingDao, Dispensing>{

    /**
     * DS71: generic single-entity save() is fail-closed. CrudController
     * exposes POST /stock/dispensing/save, but the mapper <update>
     * writes company_id, registration_id, supplier_stock_id,
     * recipel_info_id, number, remarks verbatim by id, so any caller
     * with the permission could set the dispensed quantity absolutely
     * with no stock move, no recipel change and no aggregate adjustment
     * (probe-proven: fixture row 100 -> 888 with SUM 314 -> 1102, a
     * phantom row 777 inserted, and on a live row reportAmount
     * numberAmount 5 -> 888 / bidTotal 0.10 -> 17.76). Dispensing rows
     * are written only by the real dispense-confirm flow
     * (okOccupyStock below) -- the frontend has no UI path through
     * this endpoint (dispensingForm.vue is imported by no view).
     * Internal callers use saveInternal() below, which keeps the old
     * CrudService.save behaviour (tenant check + audit action).
     */
    @Override
    @Transactional(readOnly = false)
    public Dispensing save(Dispensing entity) {
        throw new ServiceException("Chi tiết phát thuốc chỉ thay đổi qua phát thuốc / hoàn phí, không hỗ trợ lưu trực tiếp!");
    }

    /**
     * DS71: same fail-closed guard for the bulk writes -- CrudService
     * bulkInsert/bulkUpdate call dao.bulk* directly, NOT save(), so the
     * save() override above does not cover them, and the bulk mapper is
     * well-formed (unlike DS70's broken bulk mapper), so the bulk paths
     * were genuinely exploitable (probe-proven: bulkUpdate set the
     * fixture 555, bulkInsert added a phantom row 333, both 200).
     * No internal caller uses bulk on this service.
     */
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<Dispensing> entitys) {
        throw new ServiceException("Chi tiết phát thuốc chỉ thay đổi qua phát thuốc / hoàn phí, không hỗ trợ thao tác hàng loạt!");
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<Dispensing> entitys) {
        throw new ServiceException("Chi tiết phát thuốc chỉ thay đổi qua phát thuốc / hoàn phí, không hỗ trợ thao tác hàng loạt!");
    }

    /**
     * DS71: same fail-closed guard for the deletes -- CrudController
     * exposes POST /stock/dispensing/delete and /bulkDelete, and the
     * mapper soft-deletes the row by id with no guard (probe-proven:
     * both 200, fixture row soft-deleted, SUM 314 -> 214). A dispensing
     * row is voided only through the guarded flows (drugRepercussion /
     * updateDelFlag by recipel), never by a direct delete. No internal
     * caller uses delete/bulkDelete on this service.
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(Dispensing entity) {
        throw new ServiceException("Chi tiết phát thuốc chỉ thay đổi qua phát thuốc / hoàn phí, không hỗ trợ xóa trực tiếp!");
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<Dispensing> entitys) {
        throw new ServiceException("Chi tiết phát thuốc chỉ thay đổi qua phát thuốc / hoàn phí, không hỗ trợ xóa trực tiếp!");
    }

    /**
     * DS71: internal single-entity save for the guarded dispense-confirm
     * flow (MedicinalStorageControlService.okOccupyStock). NOT reachable
     * from the controller -- CrudController routes only to
     * save()/bulkInsert()/bulkUpdate()/delete()/bulkDelete(). Delegates
     * to CrudService.save, so the tenant check and the audit action row
     * are kept exactly as before.
     */
    @Transactional(readOnly = false)
    public Dispensing saveInternal(Dispensing entity) {
        return super.save(entity);
    }

    public Page<DispensingReportEvt> reportList(SearchParams searchParams) {
        // Z2a: strip company_id body + ep session tenant (mau buildTenantPageRequest);
        // SQL DispensingDao.xml loc them theo PageRequest id/institution.
        PageRequest pageRequest = buildTenantPageRequest(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return paginate(
            () -> this.dao.reportListCount(pageRequest),
            () -> this.dao.reportList(pageRequest)
        );
    }

    public DispensingReportTotalEvt reportAmount(SearchParams searchParams) {
        // Z2a: nhu reportList.
        PageRequest pageRequest = buildTenantPageRequest(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return this.dao.reportAmount(pageRequest);
    }

    public void updateDelFlag(String recipelInfoId) {
        this.dao.updateDelFlag(recipelInfoId);
    }

    @Transactional(readOnly = true)
    public Page<DispensingReportEvt> getOrganizationList(SearchParams searchParams) {
        // Z2a: nhu reportList.
        PageRequest pageRequest = buildTenantPageRequest(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return paginate(
            () -> this.dao.getOrganizationListCount(pageRequest),
            () -> this.dao.getOrganizationList(pageRequest)
        );
    }
}