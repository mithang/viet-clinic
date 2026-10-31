package com.geeke.stock.service;

import cn.hutool.core.collection.ListUtil;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.data.Page;
import com.geeke.common.sequence.service.SequenceService;
import com.geeke.common.service.CrudService;
import com.geeke.stock.dao.SupplierStorageDao;
import com.geeke.stock.entity.SupplierStock;
import com.geeke.stock.entity.SupplierStorage;
import com.geeke.sys.entity.DictItem;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

/**
 * Phiếu nhập khoService
 * @author txl
 * @version 2022-06-02
 */
 
@Service("supplierStorageService")
@Transactional(readOnly = true)
public class SupplierStorageService extends CrudService<SupplierStorageDao, SupplierStorage>{
    @Autowired
    SequenceService sequenceService;

    @Autowired
    private SupplierStockService supplierStockService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private StuffService stuffService;

    @Autowired
    private SupplierStorageDao supplierStorageDao;

    @Autowired
    private MedicinalStorageControlService medicinalStorageControlService;

    @Override
    @Transactional(readOnly = false)
    public SupplierStorage save(SupplierStorage supplierStorage) {
        // Khi thêm mới, Xử lý trường tự động đánh số
        supplierStorage.setCreateDate(new Date());
        if (StringUtils.isBlank(supplierStorage.getId())){
            String oldCode = supplierStorageDao.getCode(supplierStorage.getCompany().getId());
            supplierStorage.setCode(generateDatePrefixCode(oldCode));
        }
        SupplierStorage supplierStorageTemp = super.save(supplierStorage);
        return supplierStorageTemp;
    }

    @Transactional(readOnly = false)
    public void cancel(String id) {
        //Trước khi hủy, kiểm tra trước thuốc hoặc vật tư đó có đang được sử dụng không
        SupplierStorage supplierStorage = this.get(id);
        // phiếu nhập khoTrạng thái->hủy
        DictItem dictItem = new DictItem();
        dictItem.setValue(BizConstants.SUPPLIER_STORAGE_EXAMINE_VOIDED);
        supplierStorage.setExamine(dictItem);
        supplierStorage.setExamineDate(new Date());
        this.save(supplierStorage);
        //hủysauCầnSẽ tương ứng của Chi tiếtbảnghủyrơi
        List<SupplierStock> byStorageId = supplierStockService.getByStorageId(id);

        supplierStockService.cancellation(byStorageId);

        //gọiđộngTồn khokiểm soát
        this.medicinalStorageControlService.invalidStorageStock(supplierStorage);

        // hủytrừTồn kho - Đãchuyển đổiđếnđộngTồn khokiểm soátmáychế
    }


    public Page<SupplierStorage> listByCode(String pinyinCode, String status, String startTime, String endTime, int limit, int offset) {

        String companyId = SessionUtils.getUserDto().getCompanyId();
        List<SupplierStorage> supplierStorages = supplierStorageDao.listByCode(companyId, pinyinCode, status, startTime, endTime);
        int total = supplierStorages.size();
        List<SupplierStorage> list = ListUtil.page(offset, limit, supplierStorages);

        return new Page<>(total, list);
    }
}