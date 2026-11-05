package com.geeke.stock.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.stock.dao.MedicinalStockRecordDao;
import com.geeke.stock.entity.MedicinalStockRecord;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * Bản ghi thao tác tồn khoService
 * @author hl
 * @version 2022-09-26
 */
 
@Service("medicinalStockRecordService")
@Transactional(readOnly = true)
public class MedicinalStockRecordService extends CrudService<MedicinalStockRecordDao, MedicinalStockRecord>{
    @Autowired
    private MedicinalStockRecordDao medicinalStockRecordDao;

    /**
     * DS72: generic save() is fail-closed. CrudController exposes POST
     * /stock/medicinalStockRecord/save, but the mapper <update> writes
     * company, drugStuffId, drugStuffName, type, operation_type,
     * operation_stock, storage_id, recipelInfoId, refId verbatim by id,
     * so any caller with the permission could fabricate op=1 import
     * records or rewrite a real record's op type/quantity, corrupting
     * the stock audit trail (probe-proven: fixture operation_stock 8
     * -> 888, SUM 10108 -> 10988; phantom inserts; soft deletes).
     * Records are written only by the internal stock flows below --
     * the frontend has no UI path through this endpoint. Internal
     * callers use saveInternal(), which keeps the old CrudService.save
     * behaviour (tenant check + audit action).
     */
    @Override
    @Transactional(readOnly = false)
    public MedicinalStockRecord save(MedicinalStockRecord entity) {
        throw new ServiceException("Bản ghi thao tác tồn kho chỉ được tạo bởi hệ thống, không hỗ trợ lưu trực tiếp!");
    }

    /**
     * DS72: same fail-closed guard for the bulk writes -- CrudService
     * bulkInsert/bulkUpdate call dao.bulk* directly, NOT save(), so the
     * save() override above does not cover them (probe-proven:
     * bulkUpdate set the fixture 555, bulkInsert added phantom rows).
     * No internal caller uses bulk on this service.
     */
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<MedicinalStockRecord> entitys) {
        throw new ServiceException("Bản ghi thao tác tồn kho chỉ được tạo bởi hệ thống, không hỗ trợ thao tác hàng loạt!");
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<MedicinalStockRecord> entitys) {
        throw new ServiceException("Bản ghi thao tác tồn kho chỉ được tạo bởi hệ thống, không hỗ trợ thao tác hàng loạt!");
    }

    /**
     * DS72: same fail-closed guard for the generic deletes -- the
     * mapper soft-deletes by id with no guard (probe-proven: generic
     * delete/bulkDelete soft-deleted the fixture, SUM 10108 -> 10100).
     * No internal caller uses delete on this service (batchDelete below
     * is internal-only, not exposed by the controller).
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(MedicinalStockRecord entity) {
        throw new ServiceException("Bản ghi thao tác tồn kho chỉ được tạo bởi hệ thống, không hỗ trợ xóa trực tiếp!");
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<MedicinalStockRecord> entitys) {
        throw new ServiceException("Bản ghi thao tác tồn kho chỉ được tạo bởi hệ thống, không hỗ trợ xóa trực tiếp!");
    }

    /**
     * DS72: internal-only save used by the 8 stock flows that create
     * audit records (import op=1, void op=-1, auto-void op=-2, occupy
     * op=2, actual occupy op=3, return op=4, inventory op=5, outbound
     * examine op=6). Not reachable from the controller.
     */
    @Transactional(readOnly = false)
    public MedicinalStockRecord saveInternal(MedicinalStockRecord entity) {
        return super.save(entity);
    }

    @Transactional(readOnly = false)
    public int batchDelete(List<MedicinalStockRecord> var1)
    {
        return this.medicinalStockRecordDao.batchDelete(var1);
    }

    @Transactional(readOnly = false)
    public List<MedicinalStockRecord> getStockRecordByStorageId(String storageId)
    {
        return this.medicinalStockRecordDao.getStockRecordByStorageId(storageId);
    }
}