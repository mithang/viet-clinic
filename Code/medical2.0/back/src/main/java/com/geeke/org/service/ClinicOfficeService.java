package com.geeke.org.service;

import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.org.dao.ClinicOfficeDao;
import com.geeke.org.entity.ClinicOffice;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Thông tin khoaService
 * @author ch
 * @version 2022-06-14
 */

@Service("clinicOfficeService")
@Transactional(readOnly = true)
public class ClinicOfficeService extends CrudService<ClinicOfficeDao, ClinicOffice>{
    @Autowired
    private ClinicOfficeDao clinicOfficeDao;

    @Transactional(readOnly = false)
    public void updateDefault(String companyId) {
        clinicOfficeDao.updateDefault(companyId);
    }

    @Transactional(readOnly = false)
    public ClinicOffice save(ClinicOffice clinicOffice){
       if(StringUtils.isBlank(clinicOffice.getId())){
           //tiến hành dòngmã sốKiểm tra
           List<ClinicOffice> clinicOffices = this.dao.getByCode(clinicOffice);
           if(!CollectionUtils.isEmpty(clinicOffices)){
               throw new ServiceException("Đã tồn tạigiống hệtmã sốKhoa,Vui lòng nhập lại");
           }

           //tiến hành dòngTên khoaKiểm tra
           List<ClinicOffice> clinicOfficeList = this.dao.getByName(clinicOffice);
           if(!CollectionUtils.isEmpty(clinicOfficeList)){
               throw new ServiceException("Đã tồn tạigiống hệtTênKhoa,Vui lòng nhập lại");
           }
       }
        ClinicOffice save = super.save(clinicOffice);
        return clinicOffice;
    }
}