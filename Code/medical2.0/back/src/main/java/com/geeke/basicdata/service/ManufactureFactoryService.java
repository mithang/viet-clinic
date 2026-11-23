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

        // DS624: kiem tra trung ten nha san xuat ca hai nhanh tao moi
        // VA cap nhat (truoc day chi chay khi getId()==null). Duong
        // update/bulkInsert/bulkUpdate bo qua guard nay trong khi mapper
        // update/bulkUpdate deu ghi `name` -> tao duoc 2 dong cung ten
        // trong mot tenant -> lan save ke tiep goi findBy (selectOne)
        // nem TooManyResultsException -> 500. Dong cua chinh minh (cung
        // id) duoc mien; ten cua dong da xoa mem van chan nhu cu.
        checkManufactureFactoryNameDuplicate(manufactureFactory);

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
        checkManufactureFactoryStatusValues(manufactureFactory); // DS326
        checkManufactureFactoryTypeValues(manufactureFactory); // DS378
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

    // DS326: status gia mao ('9'/'2'/'x'...) qua save/bulkInsert/
    // bulkUpdate — truoc day 200 + persist (chi co guard do dai max 1,
    // DS149). Mien hop le duy nhat '0'/'1' (form switch active-value
    // "1" / inactive-value "0"; index.vue search options '0' Khong /
    // '1' Co). listAll/listAlls loc AND a.status="1" nen gia tri ma
    // thanh hang ma tang hinh khoi dropdown nha san xuat (mau DS323/
    // DS324). NULL van hop le: cot nullable, live DB co 3 dong NULL.
    private static void checkManufactureFactoryStatusValues(
            ManufactureFactory entity) {
        if (entity == null || entity.getStatus() == null) {
            return;
        }
        if (!"0".equals(entity.getStatus())
                && !"1".equals(entity.getStatus())) {
            throw new ServiceException(
                    "Trạng thái kích hoạt chỉ nhận giá trị 0 (Không) hoặc 1 (Có)");
        }
    }

    // DS378: type gia mao ('9'/'0'/'x'...) qua save/bulkInsert/
    // bulkUpdate — truoc day 200 + persist (chi co guard do dai max 1,
    // DS149, + status DS326). Mien hop le duy nhat '1' (Hang thuoc) /
    // '2' (Hang vat tu) (manufactureFactoryForm.vue:111-126 radio chi
    // '1'/'2'; index.vue types chi '1'/'2'). Reader that: drugForm.vue
    // loc type='1', stuffForm.vue loc type='2' qua listAll — hang gia
    // '9' bien khoi CA HAI dropdown chon hang, khong duong nao chon
    // lai duoc. NULL van qua guard nay: cot @NotNull nen save don da
    // 400 san qua @Valid, bulk* da 400 san qua isBlank(type) o
    // service; khong can chan them o day.
    private static void checkManufactureFactoryTypeValues(
            ManufactureFactory entity) {
        if (entity == null || entity.getType() == null) {
            return;
        }
        if (!"1".equals(entity.getType())
                && !"2".equals(entity.getType())) {
            throw new ServiceException(
                    "Loại nhà sản xuất chỉ nhận giá trị 1 (Hãng thuốc) hoặc 2 (Hãng vật tư)");
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
            checkManufactureFactoryStatusValues(manufactureFactory); // DS326
            checkManufactureFactoryTypeValues(manufactureFactory); // DS378
        }
        // DS624: bulkInsert di thang dao.bulkInsert, bo qua guard trung
        // ten cua save() -> 2 dong cung ten trong mot batch, lan save ke
        // tiep 500 o findBy. Chan truoc moi ghi (ca trung voi dong da co
        // lan trung nhau trong list).
        checkManufactureFactoryNameDuplicates(entitys);
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
            checkManufactureFactoryStatusValues(manufactureFactory); // DS326
            checkManufactureFactoryTypeValues(manufactureFactory); // DS378
        }
        // DS624: nhu bulkInsert — mapper bulkUpdate ghi `name` (CASE)
        // ma khong qua guard trung ten cua save().
        checkManufactureFactoryNameDuplicates(entitys);
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


    // DS624: guard trung ten nha san xuat trong cung tenant.
    // findBy tra id cua dong co ten do (LIMIT 1 o mapper); id khac
    // dong hien tai -> 400. Dong cua chinh minh duoc mien de update
    // giu nguyen ten khong bao loi oan. Ten rong bo qua o day (da 400
    // o vong kiem tra truoc trong save()/bulk*); company lay tu payload,
    // fallback ve tenant session; khong xac dinh duoc company thi bo qua
    // (giu hanh vi cu cho caller truc tiep khong mang company).
    private void checkManufactureFactoryNameDuplicate(
            ManufactureFactory manufactureFactory) {
        if (manufactureFactory == null
                || StringUtils.isBlank(manufactureFactory.getName())) {
            return;
        }
        String companyId = resolveManufactureFactoryCompanyId(manufactureFactory);
        if (StringUtils.isBlank(companyId)) {
            return;
        }
        String foundId = manufactureFactoryDao.findBy(
                manufactureFactory.getName(), companyId);
        if (foundId != null && !foundId.equals(manufactureFactory.getId())) {
            throw new ServiceException("Nhà sản xuất Đã tồn tại");
        }
    }

    private void checkManufactureFactoryNameDuplicates(
            List<ManufactureFactory> entitys) {
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (ManufactureFactory manufactureFactory : entitys) {
            if (manufactureFactory == null
                    || StringUtils.isBlank(manufactureFactory.getName())) {
                continue;
            }
            String companyId =
                    resolveManufactureFactoryCompanyId(manufactureFactory);
            if (StringUtils.isBlank(companyId)) {
                continue;
            }
            if (!seen.add(companyId + "\n" + manufactureFactory.getName())) {
                throw new ServiceException("Nhà sản xuất Đã tồn tại");
            }
            checkManufactureFactoryNameDuplicate(manufactureFactory);
        }
    }

    private String resolveManufactureFactoryCompanyId(
            ManufactureFactory manufactureFactory) {
        String companyId = null;
        if (manufactureFactory != null && manufactureFactory.getCompany() != null) {
            companyId = manufactureFactory.getCompany().getId();
        }
        if (StringUtils.isBlank(companyId)) {
            companyId = com.geeke.utils.SessionUtils.getLoginTenantId();
        }
        if ("null".equals(companyId)) {
            return null;
        }
        return companyId;
    }

    public String repeatBy(ManufactureFactory manufactureFactory){
      return  manufactureFactoryDao.findBy(manufactureFactory.getName(), manufactureFactory.getCompany().getId());
    }
    

}