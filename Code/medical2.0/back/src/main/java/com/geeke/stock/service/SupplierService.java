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
    // DS125 C: single delete truoc day de lo: ghost id / dong da xoa mem
    // roi van 200 silent no-op (verifyTenantOwnershipAgainstStoredRow bo
    // qua khi stored==null, cung mau DS123/DS124/DS125A; mapper delete
    // khong loc del_flag). Chan o service de 400 ro nghia truoc khi xoa.
    // Khong caller noi bo nao goi delete() nay; UI xoa hang live van 200
    // nhu cu.
    @Override
    @Transactional(readOnly = false)
    public int delete(Supplier entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được nhà cung cấp cần xóa.");
        }
        Supplier stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Nhà cung cấp không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
    }

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
        // DS622: kiem tra trung ten nha cung cap ca hai nhanh tao moi
        // VA cap nhat (truoc day chi chay khi getId()==null). Duong
        // update/bulkInsert/bulkUpdate bo qua guard nay trong khi mapper
        // update/bulkUpdate deu ghi `name` -> tao duoc 2 dong cung ten
        // trong mot tenant -> lan save ke tiep goi findBy (selectOne)
        // nem TooManyResultsException -> 500. Dong cua chinh minh (cung
        // id) duoc mien; ten cua dong da xoa mem van chan nhu cu.
        checkSupplierNameDuplicate(supplier);
        // DS104 C1: save voi id khong ton tai truoc day roi qua
        // CrudService.save -> doUpdate 0 dong roi van 200 silent no-op
        // (verifyTenantOwnershipAgainstStoredRow bo qua khi stored==null,
        // cung mau DS94/DS99). Kiem tra ton tai truoc moi ghi.
        if (StringUtils.isNotBlank(supplier.getId())
                && dao.get(supplier.getId()) == null) {
            throw new ServiceException("Nhà cung cấp không tồn tại: "
                    + supplier.getId());
        }
        checkSupplierStatusValues(supplier); // DS443
        checkSupplierTypeValues(supplier); // DS546
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
            checkSupplierStatusValues(supplier); // DS443
            checkSupplierTypeValues(supplier); // DS546
        }
        // DS622: bulkInsert di thang dao.bulkInsert, bo qua guard trung
        // ten cua save() -> 2 dong cung ten trong mot batch, lan save ke
        // tiep 500 o findBy. Chan truoc moi ghi (ca trung voi dong da co
        // lan trung nhau trong list).
        checkSupplierNameDuplicates(entitys);
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
            checkSupplierStatusValues(supplier); // DS443
            checkSupplierTypeValues(supplier); // DS546
        }
        // DS622: nhu bulkInsert — mapper bulkUpdate ghi `name` (CASE)
        // ma khong qua guard trung ten cua save().
        checkSupplierNameDuplicates(entitys);
        return super.bulkUpdate(entitys);
    }

    // DS622: guard trung ten nha cung cap trong cung tenant.
    // findBy tra id cua dong co ten do (LIMIT 1 o mapper); id khac
    // dong hien tai -> 400. Dong cua chinh minh duoc mien de update
    // giu nguyen ten khong bao loi oan. Ten rong bo qua o day (da 400
    // o vong kiem tra truoc trong save()/bulk*); company lay tu payload,
    // fallback ve tenant session; khong xac dinh duoc company thi bo qua
    // (giu hanh vi cu cho caller truc tiep khong mang company).
    private void checkSupplierNameDuplicate(Supplier supplier) {
        if (supplier == null || StringUtils.isBlank(supplier.getName())) {
            return;
        }
        String companyId = resolveSupplierCompanyId(supplier);
        if (StringUtils.isBlank(companyId)) {
            return;
        }
        String foundId = supplierDao.findBy(supplier.getName(), companyId);
        if (foundId != null && !foundId.equals(supplier.getId())) {
            throw new ServiceException("Nhà cung cấp đã tồn tại");
        }
    }

    private void checkSupplierNameDuplicates(List<Supplier> entitys) {
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (Supplier supplier : entitys) {
            if (supplier == null || StringUtils.isBlank(supplier.getName())) {
                continue;
            }
            String companyId = resolveSupplierCompanyId(supplier);
            if (StringUtils.isBlank(companyId)) {
                continue;
            }
            if (!seen.add(companyId + "\n" + supplier.getName())) {
                throw new ServiceException("Nhà cung cấp đã tồn tại");
            }
            checkSupplierNameDuplicate(supplier);
        }
    }

    private String resolveSupplierCompanyId(Supplier supplier) {
        String companyId = null;
        if (supplier != null && supplier.getCompany() != null) {
            companyId = supplier.getCompany().getId();
        }
        if (StringUtils.isBlank(companyId)) {
            companyId = com.geeke.utils.SessionUtils.getLoginTenantId();
        }
        if ("null".equals(companyId)) {
            return null;
        }
        return companyId;
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<Supplier> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách nhà cung cấp không được để trống");
        }
        return super.bulkDelete(entitys);
    }

    // DS443: status chi co 2 gia tri co nghia '0'/'1' (switch kich hoat
    // o supplierForm.vue:52, mac dinh '1'; findList loc cung status='1'
    // nen hang gia vo hinh voi moi dropdown). Form luon gui '0'/'1',
    // khong writer noi bo nao tao Supplier null-status, live 6/6 deu
    // '1' -> null/blank cung 400 (tien le DS324 Stuff.status).
    private static void checkSupplierStatusValues(Supplier supplier) {
        if (supplier == null) {
            return;
        }
        String status = supplier.getStatus();
        if (!"0".equals(status) && !"1".equals(status)) {
            throw new ServiceException(
                    "Trạng thái nhà cung cấp không hợp lệ: " + status);
        }
    }

    // DS546: type chi co 2 gia tri co nghia '1'/'2' (radio loai nha cung
    // cap o supplierForm.vue:71-72, loc list chi co '1'/'2' o
    // supplier/index.vue:189-197; hang gia vo hinh duoi ca 2 bo loc loai
    // va khong radio nao duoc chon o form). Form luon gui '1'/'2',
    // khong writer noi bo nao tao Supplier, live 6/6 deu '1'/'2', cot
    // type varchar(1) NOT NULL -> null/blank cung 400 (tien le DS443
    // cho status; save da 400 blank nho @NotNull/@Length tren getType,
    // bulkInsert da 400 blank, lo blank con lai la bulkUpdate -- bulk*
    // bypass @Valid nen guard o day la tuyen cuoi). Goi o
    // save()/bulkInsert()/bulkUpdate() ngay sau guard status DS443.
    private static void checkSupplierTypeValues(Supplier supplier) {
        if (supplier == null) {
            return;
        }
        String type = supplier.getType();
        if (!"1".equals(type) && !"2".equals(type)) {
            throw new ServiceException(
                    "Loại nhà cung cấp không hợp lệ: " + type);
        }
    }

}