package com.geeke.basicdata.service;

import com.geeke.basicdata.dao.ManufactureFactoryDao;
import com.geeke.basicdata.entity.ManufactureFactory;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.org.service.CompanyService;


import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Nhà sản xuấtService
 * @author txl
 * @version 2022-06-22
 */
 
@Service("manufactureFactoryService")
@Transactional(readOnly = true)
public class ManufactureFactoryService extends CrudService<ManufactureFactoryDao, ManufactureFactory>{
    @Autowired
    private ManufactureFactoryDao manufactureFactoryDao;

    /** Nhà sản xuất là Cơ sởtừ điển dùng chung: Cho phép phòng khám thuộc cùng cơ sở truy cập dữ liệu phòng khám của cơ sở(Với danh sách #{institution} SQL Nhất quán) */
    @Override
    protected boolean isInstitutionShared() {
        return true;
    }
    @Autowired
    private CompanyService companyService;
    @Override
    @Transactional(readOnly = false)
    public ManufactureFactory save(ManufactureFactory manufactureFactory) {
        Map<String, String> colMaps = Maps.newHashMap();
        // Nhà sản xuấtĐã tồn tại
        colMaps.clear();
        colMaps.put("name", "name");
        
        // duplicate check handled below by findBy
        //Kiểm traNhà sản xuấtCó tồn tại không
       if(manufactureFactory.getId()==null){
           String by = manufactureFactoryDao.findBy(manufactureFactory.getName(), manufactureFactory.getCompany().getId());
           if(by!=null){
               throw new ServiceException("Nhà sản xuấtĐã tồn tại");

           }
       }

        ManufactureFactory manufactureFactoryTemp = super.save(manufactureFactory);
        return manufactureFactoryTemp;
    }

    /**
     * Truy vấnNhà sản xuấtDanh sách
     * @param parameters Danh sách tham số truy vấn
     * @param orderby sắp xếpĐiều kiện
     * @return Nhà sản xuấtDanh sách
     */
public List<ManufactureFactory> listAlls(List<Parameter> parameters, String orderby) {
    PageRequest pageRequest = buildTenantPageRequest(parameters, orderby);
    return dao.listAlls(pageRequest);
}


    public String repeatBy(ManufactureFactory manufactureFactory){
      return  manufactureFactoryDao.findBy(manufactureFactory.getName(), manufactureFactory.getCompany().getId());
    }
    

}