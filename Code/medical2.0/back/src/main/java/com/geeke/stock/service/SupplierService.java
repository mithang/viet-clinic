package com.geeke.stock.service;

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
        Supplier supplierTemp = super.save(supplier);
        return supplierTemp;
    }
    

}