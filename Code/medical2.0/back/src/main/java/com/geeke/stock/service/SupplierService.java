package com.geeke.stock.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.common.service.ServiceException;
import com.geeke.stock.dao.SupplierDao;
import com.geeke.stock.entity.Supplier;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * Quản lý nhà cung cấpService
 * @author txl
 * @version 2022-06-22
 */
 
@Service("supplierService")
@Transactional(readOnly = true)
public class SupplierService extends CrudService<SupplierDao, Supplier>{
     @Autowired
     private SupplierDao supplierDao;
    @Override
    @Transactional(readOnly = false)
    public Supplier save(Supplier supplier) {
        Map<String, String> colMaps = Maps.newHashMap();
        // Nhà cung cấp đã tồn tại
        colMaps.clear();
        colMaps.put("name", "name");

        /*if(exists(dao, supplier, colMaps)) {
            throw new ServiceException("Nhà cung cấp đã tồn tại");
        }*/
        //Kiểm traNhà cung cấpCó tồn tại không
       if(supplier.getId()==null){
           String by = supplierDao.findBy(supplier.getName(), supplier.getCompany().getId());
           if(by!=null){
               throw new ServiceException("Nhà cung cấp đã tồn tại");

           }
       }
        // DS104 C1: save voi id khong ton tai truoc day roi qua
        // CrudService.save -> doUpdate 0 dong roi van 200 silent no-op
        // (verifyTenantOwnershipAgainstStoredRow bo qua khi stored==null,
        // cung mau DS94/DS99). Kiem tra ton tai truoc moi ghi.
        if (StringUtils.isNotBlank(supplier.getId())
                && dao.get(supplier.getId()) == null) {
            throw new ServiceException("Nhà cung cấp không tồn tại: "
                    + supplier.getId());
        }
        Supplier supplierTemp = super.save(supplier);
        return supplierTemp;
    }

    // DS104 C3-C8/C10-C12: bulkInsert/bulkUpdate/bulkDelete generic truoc
    // day de lo: null/rong roi xuong MyBatis <foreach> rong -> 500 SQL
    // syntax (chi vo tinh ve 400 nho handler DS94, sai message), thieu
    // truong NOT NULL (name/pinyin_code/type) -> 500
    // DataIntegrityViolation, bulkUpdate id khong ton tai -> 200 silent
    // no-op. Chan het o service de 400 ro nghia, kiem tra truoc moi ghi
    // de batch hon hop khong doi dong hop le (C9). @Transactional
    // (readOnly = false) tuong minh vi class-level la readOnly = true
    // (mau DepartmentService DS103).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<Supplier> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách nhà cung cấp không được để trống");
        }
        for (Supplier supplier : entitys) {
            if (StringUtils.isBlank(supplier.getName())) {
                throw new ServiceException("Tên nhà cung cấp không được để trống");
            }
            if (StringUtils.isBlank(supplier.getPinyinCode())) {
                throw new ServiceException("Mã pinyin không được để trống");
            }
            if (StringUtils.isBlank(supplier.getType())) {
                throw new ServiceException("Loại nhà cung cấp không được để trống");
            }
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<Supplier> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách nhà cung cấp không được để trống");
        }
        for (Supplier supplier : entitys) {
            if (StringUtils.isNotBlank(supplier.getId())
                    && dao.get(supplier.getId()) == null) {
                throw new ServiceException("Nhà cung cấp không tồn tại: "
                        + supplier.getId());
            }
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<Supplier> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách nhà cung cấp không được để trống");
        }
        return super.bulkDelete(entitys);
    }
    

}