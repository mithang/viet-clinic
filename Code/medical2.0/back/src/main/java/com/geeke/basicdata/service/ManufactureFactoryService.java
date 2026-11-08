package com.geeke.basicdata.service;

import com.geeke.basicdata.dao.ManufactureFactoryDao;
import com.geeke.basicdata.entity.ManufactureFactory;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.org.service.CompanyService;
import com.geeke.utils.StringUtils;


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
               throw new ServiceException("Nhà sản xuất Đã tồn tại");

           }
       }

        // DS105 A1: save voi id khong ton tai truoc day roi qua
        // CrudService.save -> doUpdate 0 dong roi van 200 silent no-op
        // (verifyTenantOwnershipAgainstStoredRow bo qua khi stored==null,
        // cung mau DS94/DS99). Kiem tra ton tai truoc moi ghi, sau
        // check trung ten chi-chay-khi-tao-moi.
        if (StringUtils.isNotBlank(manufactureFactory.getId())
                && manufactureFactoryDao.get(manufactureFactory.getId()) == null) {
            throw new ServiceException("Nhà sản xuất không tồn tại: "
                    + manufactureFactory.getId());
        }

        // DS149 B: chan do dai truoc moi ghi (mau DS148 A). bulk* bypass
        // @Valid nen truong co @Length van 500 o mapper khi qua bulk
        // (name/address 128, people/phone/province/city/area 45, type 1);
        // status @Length(45) LECH voi varchar(1) -- da sua annotation
        // nhom A, service guard them cho duong bulk. remarks ->
        // varchar(255). Gioi han theo cot DB; null-safe.
        checkManufactureFactoryLengths(manufactureFactory);
        ManufactureFactory manufactureFactoryTemp = super.save(manufactureFactory);
        return manufactureFactoryTemp;
    }

    // DS149 B: xem ghi chu o save().
    private static void checkManufactureFactoryLengths(ManufactureFactory entity) {
        if (entity == null) {
            return;
        }
        checkLength(entity.getName(), 128, "Tên nhà sản xuất Độ dài phải nằm trong khoảng 1 đến 128");
        checkLength(entity.getPeople(), 45, "Người liên hệ Độ dài phải nằm trong khoảng 0 đến 45");
        checkLength(entity.getPhone(), 45, "Số điện thoại Độ dài phải nằm trong khoảng 0 đến 45");
        checkLength(entity.getProvince(), 45, "Tỉnh Độ dài phải nằm trong khoảng 0 đến 45");
        checkLength(entity.getCity(), 45, "Thành phố Độ dài phải nằm trong khoảng 0 đến 45");
        checkLength(entity.getArea(), 45, "Quận huyện Độ dài phải nằm trong khoảng 0 đến 45");
        checkLength(entity.getAddress(), 128, "Độ dài địa chỉ phải nằm trong khoảng 0 đến 128");
        checkLength(entity.getStatus(), 1, "Trạng thái Độ dài phải nằm trong khoảng 0 đến 1");
        checkLength(entity.getType(), 1, "Loại nhà sản xuất Độ dài phải nằm trong khoảng 1 đến 1");
        checkLength(entity.getRemarks(), 255, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
    }

    private static void checkLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ServiceException(message);
        }
    }

    // DS125 A: single delete fail-closed 400 (mau DS123/DS124) -- truoc day
    // delete id ma / dong da xoa mem deu 200 silent (CrudService.delete chi
    // chan qua stored-row tenant check ma check do bo qua stored==null;
    // mapper delete khong loc del_flag nen double-delete tra 200 rows=1).
    // Kiem tra ton tai + con live (del_flag='0') truoc moi xoa; khong co
    // caller noi bo nao goi delete() cua service nay.
    @Override
    @Transactional(readOnly = false)
    public int delete(ManufactureFactory manufactureFactory) {
        if (manufactureFactory == null || StringUtils.isBlank(manufactureFactory.getId())) {
            throw new ServiceException("Không xác định được nhà sản xuất cần xóa.");
        }
        ManufactureFactory stored = manufactureFactoryDao.get(manufactureFactory.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Nhà sản xuất không tồn tại: " + manufactureFactory.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(manufactureFactory);
        return super.delete(manufactureFactory);
    }

    // DS105 A3-A8/A10-A12: bulkInsert/bulkUpdate/bulkDelete generic truoc
    // day de lo: null/rong roi xuong MyBatis <foreach> rong -> 500 SQL
    // syntax, thieu truong NOT NULL (name/type) -> 500
    // DataIntegrityViolation, bulkUpdate id khong ton tai -> 200 silent
    // no-op. Chan het o service de 400 ro nghia, kiem tra truoc moi ghi
    // de batch hon hop khong doi dong hop le (A9). @Transactional
    // (readOnly = false) tuong minh vi class-level la readOnly = true
    // (mau SupplierService DS104).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<ManufactureFactory> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách nhà sản xuất không được để trống");
        }
        for (ManufactureFactory manufactureFactory : entitys) {
            if (StringUtils.isBlank(manufactureFactory.getName())) {
                throw new ServiceException("Tên nhà sản xuất không được để trống");
            }
            if (StringUtils.isBlank(manufactureFactory.getType())) {
                throw new ServiceException("Loại nhà sản xuất không được để trống");
            }
            checkManufactureFactoryLengths(manufactureFactory);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<ManufactureFactory> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách nhà sản xuất không được để trống");
        }
        for (ManufactureFactory manufactureFactory : entitys) {
            if (StringUtils.isNotBlank(manufactureFactory.getId())
                    && manufactureFactoryDao.get(manufactureFactory.getId()) == null) {
                throw new ServiceException("Nhà sản xuất không tồn tại: "
                        + manufactureFactory.getId());
            }
            if (StringUtils.isBlank(manufactureFactory.getName())) {
                throw new ServiceException("Tên nhà sản xuất không được để trống");
            }
            if (StringUtils.isBlank(manufactureFactory.getType())) {
                throw new ServiceException("Loại nhà sản xuất không được để trống");
            }
            checkManufactureFactoryLengths(manufactureFactory);
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<ManufactureFactory> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách nhà sản xuất không được để trống");
        }
        return super.bulkDelete(entitys);
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