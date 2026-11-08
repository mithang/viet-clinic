package com.geeke.stock.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.geeke.common.constants.BizConstants;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.sequence.service.SequenceService;
import com.geeke.org.entity.Company;
import com.geeke.outpatient.entity.PageRegistration;
import com.geeke.outpatient.service.RegistrationService;
import com.geeke.stock.entity.*;
import com.geeke.toll.service.TollInfoService;
import com.geeke.utils.SessionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.common.service.ServiceException;
import com.geeke.stock.dao.InventoryVerificationDao;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;

/**
 * Kiểm kê tồn khoService
 * @author rys
 * @version 2022-11-02
 */
 
@Service("inventoryVerificationService")
@Transactional(readOnly = true)
public class InventoryVerificationService extends CrudService<InventoryVerificationDao, InventoryVerification>{

    private static final Boolean Flage=Boolean.FALSE;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private StuffService stuffService;

    @Autowired
    private InventoryVerificationDetailService inventoryVerificationDetailService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private SupplierStockService supplierStockService;

    @Autowired
    private InventoryVerificationRecordService inventoryVerificationRecordService;

    @Autowired
    private MedicinalStorageControlService medicinalStorageControlService;

    @Autowired
    private MedicinalStockControlService medicinalStockControlService;

    @Autowired
    private MedicinalStockRecordService medicinalStockRecordService;

    // DS116 B: generic save/bulk* had no overrides (CrudService only), so
    // the CrudController surface at /stock/inventoryVerification failed
    // open: save with a nonexistent id returned 200 silent no-op,
    // bulkInsert/bulkUpdate [null] NPEd 500, bulk rows missing a NOT NULL
    // column 500d (bulk bypasses @Valid), bulkUpdate with a nonexistent
    // id silently updated 0 rows 200. Validate the DB NOT NULL columns
    // here (code, status); type/company are nullable in the DB and the
    // custom save(type, variety) path sets them itself, so they are not
    // required. No internal caller omits code/status: generic save/bulk
    // are only reached via the controller, and the custom save() below
    // sets code (sequence) + status '0' before calling super.save.
    private void validateRequired(InventoryVerification entity) {
        if (entity == null) {
            throw new ServiceException("Phiếu kiểm kê không được để trống");
        }
        if (StringUtils.isBlank(entity.getCode())) {
            throw new ServiceException("Số phiếu kiểm kê không được để trống");
        }
        if (StringUtils.isBlank(entity.getStatus())) {
            throw new ServiceException("Trạng thái kiểm kê không được để trống");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public InventoryVerification save(InventoryVerification entity) {
        validateRequired(entity);
        if (StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Phiếu kiểm kê không tồn tại: "
                    + entity.getId());
        }
        return super.save(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<InventoryVerification> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiếu kiểm kê thêm mới không được để trống");
        }
        for (InventoryVerification entity : entitys) {
            validateRequired(entity);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<InventoryVerification> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiếu kiểm kê cập nhật không được để trống");
        }
        for (InventoryVerification entity : entitys) {
            validateRequired(entity);
            if (StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của phiếu kiểm kê đã tồn tại");
            }
            if (dao.get(entity.getId()) == null) {
                throw new ServiceException("Phiếu kiểm kê không tồn tại: "
                        + entity.getId());
            }
        }
        return super.bulkUpdate(entitys);
    }

    /**
     * DS124 C: single delete fail-closed 400. CrudService.delete chi chan
     * qua stored-row tenant check ma check do bo qua stored==null, nen
     * delete id ma tra 200 data=0 im lang. Delete la XOA CUNG (mapper
     * DELETE FROM inventory_verification) nen chi can kiem tra ton tai
     * qua dao.get; khong co del_flag de kiem tra them. Controller chi
     * xoa chi tiet khi rows>0 nen ghost delete khong dong den chi tiet
     * truoc cung nhu sau fix (sau fix con 400 truoc khi xoa).
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(InventoryVerification entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được phiếu kiểm kê cần xóa.");
        }
        InventoryVerification stored = dao.get(entity.getId());
        if (stored == null) {
            throw new ServiceException("Phiếu kiểm kê không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<InventoryVerification> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiếu kiểm kê xóa không được để trống");
        }
        for (InventoryVerification entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Phiếu kiểm kê xóa không được để trống");
            }
        }
        return super.bulkDelete(entitys);
    }

    /** L1 (iteration 34): doc phieu goc khong qua san tenant, de ben goi tu
     * kiem tra chu so huu theo dong DB truoc khi ghi tong tien. */
    public InventoryVerification getRaw(String id) {
        return this.dao.get(id);
    }

    @Transactional
    public  void updateAllPrice(String id, BigDecimal allPrice) {
        this.dao.updateAllPrice(id,allPrice);
    }

    /** DS65: khoa dong phieu cha (SELECT ... FOR UPDATE) de saveAll
     * tuan tu hoa tren cung phieu; phieu khong ton tai tra null, giu
     * parity nhu cu (updateAllPrice sau do update 0 dong). */
    @Transactional
    public InventoryVerification lockById(String id) {
        return this.dao.lockById(id);
    }

    @Transactional(readOnly = false)
    /*LưuKiểm kê tồn khođơn*/
    public InventoryVerification save(String type,String variety){

        Company company = SessionUtils.getUser().getCompany();

         // Khi thêm kiểm kê mới, kiểm tra trước có thuốc nào chưa phát không, Cầntiến hành dòngphát thuốc hoặc ngườiTrả thuốcXử lý
        // ThuốcKhông thểtiến hành dòngThu phíthao tác
        // Bác sĩKhông thểtiến hành dònghoàn thành tiếp nhận của thao tác


        //LấyCó tồn tại khôngChưaphát thuốc của Thuốc/Vật tư
        if(InventoryVerificationService.Flage){
            whetherTheDispensing(company);
        }

        //LấyCó tồn tại khôngChưaThu phí của Thuốc/Vật tư
        if(InventoryVerificationService.Flage){
            //whetherTheDispensing(company);
            whetherChargeOrNot(company);
        }

        InventoryVerification inventoryVerification = new InventoryVerification();

        inventoryVerification.setCompany(company);
        inventoryVerification.setStatus("0");
        inventoryVerification.setType(variety);
        inventoryVerification.setCreateDate(new Date());
        if (StringUtils.isBlank(inventoryVerification.getId())){
            String sn = sequenceService.generate(SessionUtils.getUser().getCompanyId(), "check_inventory_code", inventoryVerification);
            inventoryVerification.setCode(sn);
        }
        InventoryVerification save = super.save(inventoryVerification);

        if(!StringUtils.isNullOrEmpty(save)){
            List<MedicinalStorageControl> medicinalStorageControls = medicinalStorageControlService.getAll(company.getId(), type, variety);
            //LưuChi tiết kiểm kê tồn khobảng
            if(ObjectUtils.equals("0",variety)){
                //Kiểm kê tất cả thuốc có hoặc không có tồn kho
                //List<Drug> drugs = drugService.getAll(company.getId(), type);
                inventoryVerificationDetailService.save(save,variety,medicinalStorageControls);
            }else {
//                //LấyVật tư
//                List<Stuff> stuffs = stuffService.getAll(company.getId(),type);
                inventoryVerificationDetailService.saveStuff(save,variety,medicinalStorageControls);
            }
        }

        return save;
    }


    @Transactional(readOnly = false)
    public InventoryVerification accomplishInventoryVerification(InventoryVerification inventoryVerification) {
        // DS116 B: null entity NPEd at getId() below. (A JSON-null body
        // is already 400d by Spring before reaching here; this guard is
        // defence in depth for direct/service callers.)
        if (inventoryVerification == null) {
            throw new ServiceException("Phiếu kiểm kê không được để trống");
        }
        // N2 (iteration 61): guard tenant TRUOC DAO update, vi DAO update chi
        // WHERE id (khong dieu kien company) va la lenh dau tien cua method —
        // guard sau khong cuu duoc status da lat neu adjustStock loi sau do.
        // Lookup duoc kiem chung live: dao.get(id) (qua getRaw, khong qua san
        // tenant) tra dung company.id cho ca phieu OWN lan FOREIGN (xem
        // iter61-n2-impl-report.md), nen verifyTenantOwnership phan biet duoc;
        // phieu khong ton tai (stored null) cho qua nhu cu (parity case C).
        // updateAllPrice KHONG them guard rieng: caller duy nhat saveAll da
        // guard moi phieu cha bang ngu nghia institution-shared (O2-37),
        // chung minh live U2 (400 + all_price la khong doi); guard strict them
        // o day se pha luong institution-shared do.
        InventoryVerification stored = getRaw(inventoryVerification.getId());
        if (stored != null) {
            verifyTenantOwnership(stored);
        }
        // DS56: claim nguyen tu status '0'->'1' TRUOC moi dong tac kho (mau
        // DS55): UPDATE ... WHERE id AND status='0' nen dung 1 caller thang;
        // thua claim thi no-op -- chot lap (ke ca tuan tu) tra 200 nhu cu,
        // khong doi kho, khong bia ma loi moi. Phieu khong ton tai / id null:
        // claim 0 dong -> no-op nhu cu (update 0 dong + adjustStock tren
        // list rong, cung tra 200).
        if (this.dao.claimAccomplish(inventoryVerification.getId()) == 0) {
            return inventoryVerification;
        }
        //Sau khi hoàn thành kiểm kê, thực hiện công việc tăng giảm tồn kho liên quan
        logger.debug("Accomplish inventory verification: {}", inventoryVerification);
        if(Objects.equals(inventoryVerification.getType(),"0")){
            //Thuốctiến hành dòngTồn khotăngtrừ
            List<InventoryVerificationDetail> list = inventoryVerificationDetailService.getByInfoId(inventoryVerification.getId());
            for (InventoryVerificationDetail inventoryVerificationDetail : list) {
                List<Parameter> parameters = SearchParamsBuilder.create()
                        .eq("id", inventoryVerificationDetail.getMedicinalStorageControl().getId())
                        .build();
//                parameters.add(new Parameter("storage_stock-used_stock-reimburse_stock",">",0));
//                List<SupplierStock> supplierStocks = supplierStockService.listAll(parameters, "a.create_date");
                List<MedicinalStorageControl> medicinalStorageControls = medicinalStorageControlService.listAll(parameters, "a.create_date");
                //vòng lặptăngtrừTồn kho(Giá trị dương thì tăng, Giá trị âm thì trừ)
                adjustStock(medicinalStorageControls, inventoryVerificationDetail.getProfitAndLoss(), inventoryVerificationDetail);
            }
        }else {
            //Vật tưtiến hành dòngTồn khotăngtrừ
            List<InventoryVerificationDetail> list = inventoryVerificationDetailService.getByInfoId(inventoryVerification.getId());
            for (InventoryVerificationDetail inventoryVerificationDetail : list) {
                List<Parameter> parameters = SearchParamsBuilder.create()
                        .eq("id", inventoryVerificationDetail.getMedicinalStorageControl().getId())
                        .build();
                List<MedicinalStorageControl> medicinalStorageControls = medicinalStorageControlService.listAll(parameters, "a.create_date");
//                parameters.add(new Parameter("stuff_id","=",inventoryVerificationDetail.getStuff().getId()));
//                parameters.add(new Parameter("number",">",0));
//                List<SupplierStock> supplierStocks = supplierStockService.listAll(parameters, "a.create_date");
                //vòng lặptăngtrừTồn kho(Giá trị dương thì tăng, Giá trị âm thì trừ)
                adjustStock(medicinalStorageControls, inventoryVerificationDetail.getProfitAndLoss(), inventoryVerificationDetail);
            }
        }
        return inventoryVerification;
    }

    /**
     * điềunguyênTồn kho(Giá trị dương thì tăng, Giá trị âm thì trừ)
     */
    private void adjustStock(List<MedicinalStorageControl> medicinalStorageControls, Integer profitAndLoss, InventoryVerificationDetail inventoryVerificationDetail) {
        if (CollectionUtils.isEmpty(medicinalStorageControls)) {
            return;
        }
        MedicinalStorageControl medicinalStorageControl = medicinalStorageControls.get(0);
        BigDecimal newStock = medicinalStorageControl.getStorageStock().add(new BigDecimal(profitAndLoss));
        medicinalStorageControl.setStorageStock(newStock);
        medicinalStorageControl.setSurplusStock(newStock);
        // DS69: internal save -- the generic save() is fail-closed, so
        // the guarded accomplish flow writes through saveInternal().
        medicinalStorageControlService.saveInternal(medicinalStorageControl);

        //Truy vấnđộngTồn khotổngkiểm soátbảng
        // DS98: the aggregate must be looked up by the MSC row's own
        // drug_stuff_id. The old code used getDrug().getId(), but the
        // mapper fills drug.id with IFNULL(drug.id,'') -- '' for a stuff
        // row or a dangling drug -- and Common.whereParams SKIPS an empty
        // '=' value, so listAll returned ALL aggregates and .get(0)
        // (ORDER BY update_date DESC) adjusted a WRONG drug's aggregate
        // while the op-5 record carried the right id (silent cross-drug
        // corruption on every stuff-type accomplish). A blank
        // drug_stuff_id skips the lookup entirely (never touch an
        // arbitrary row).
        if (StringUtils.isNotBlank(medicinalStorageControl.getDrugStuffId())) {
            List<Parameter> parameters = SearchParamsBuilder.create()
                    .eq("drug_stuff_id", medicinalStorageControl.getDrugStuffId())
                    .build();
            List<MedicinalStockControl> medicinalStockControls = medicinalStockControlService.listAll(parameters, "");
            if (!CollectionUtils.isEmpty(medicinalStockControls)) {
                //độngTồn khotổngkiểm soátbảngSửa
                MedicinalStockControl medicinalStockControl = medicinalStockControls.get(0);
                BigDecimal newControlStock = medicinalStockControl.getStorageStock().add(new BigDecimal(profitAndLoss));
                medicinalStockControl.setStorageStock(newControlStock);
                medicinalStockControl.setSurplusStock(newControlStock);
                // DS69: internal save -- the generic save() is fail-closed, so
                // the guarded accomplish flow writes through saveInternal().
                medicinalStockControlService.saveInternal(medicinalStockControl);
            }
        }
        // DS98: the op-5 record documents the detail-stock change that
        // already happened above, so it is written even when no aggregate
        // row exists (dangling drug / deleted aggregate) -- previously
        // the early return skipped it, leaving an unaudited stock move.

        //Bản ghi thao tác tồn kho
        MedicinalStockRecord medicinalStockRecord = new MedicinalStockRecord();
        medicinalStockRecord.setOperationType(5);
        medicinalStockRecord.setOperationStock(new BigDecimal(profitAndLoss + ""));
        medicinalStockRecord.setCompany(medicinalStorageControl.getCompany());
        medicinalStockRecord.setDrugStuffId(medicinalStorageControl.getDrugStuffId());
        medicinalStockRecord.setStorageId(medicinalStorageControl.getStorageId());
        if (inventoryVerificationDetail.getDrug() != null && Objects.equals("", inventoryVerificationDetail.getDrug().getId())) {
            medicinalStockRecord.setType(1);
        } else {
            medicinalStockRecord.setType(2);
        }
        medicinalStockRecord.setDrugStuffName(medicinalStorageControl.getDrugStuffName());
        medicinalStockRecordService.saveInternal(medicinalStockRecord);

        //Tồn kho kiểm kêthao tácbảngLưu
        InventoryVerificationRecord inventoryVerificationRecord = new InventoryVerificationRecord();
        inventoryVerificationRecord.setCompany(medicinalStorageControl.getCompany());
        SupplierStock supplierStock = new SupplierStock();
        supplierStock.setId(medicinalStorageControl.getStorageId());
        inventoryVerificationRecord.setSupplierStock(supplierStock);
        inventoryVerificationRecord.setNumber(profitAndLoss);
        inventoryVerificationRecordService.save(inventoryVerificationRecord);
    }

    //Kiểm traCó tồn tại khôngThuốcKhông cóphát thuốc
    public void whetherTheDispensing(Company company){
        PageRegistration pageRegistration = new PageRegistration();
        pageRegistration.setCompanyId(company.getId());
        pageRegistration.setChargeStatus(1);
        pageRegistration.setColumnName("charge_date");
        pageRegistration.setDispensionStatus(0);
        pageRegistration.setStatus(BizConstants.REG_STATUS_VISITED);
        pageRegistration.setRecipelType(BizConstants.RECIPEL_TYPE_OTHER);
        pageRegistration.setRecipeStatus("0");
        String dateTime="2022-06-01 00:00:00";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date parse = simpleDateFormat.parse(dateTime);
            pageRegistration.setUpdateDate(parse);
        } catch (ParseException e) {
            logger.warn("phân tích Ngày Thất bại: {}", dateTime, e);
        }

        List<String> strings = registrationService.whetherTheDispensing(pageRegistration);
        if(!CollectionUtils.isEmpty(strings)){
            throw new ServiceException("Tồn tại Thuốc/Vật tư còn Chưa phát thuốc, Không thể tiến hành dòng kiểm kê!");
        }

    }

    //Kiểm traCó tồn tại khôngChưaThu phí của 
    public void whetherChargeOrNot(Company company) {
        PageRegistration pageRegistration = new PageRegistration();
        pageRegistration.setCompanyId(company.getId());
        pageRegistration.setColumnName("reception_end_date");
        pageRegistration.setDispensionStatus(3);
        pageRegistration.setStatus(BizConstants.REG_STATUS_VISITED);
        pageRegistration.setChargeStatus(0);
        pageRegistration.setRecipeStatus("0");
        String dateTime="2022-06-01 00:00:00";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date parse = simpleDateFormat.parse(dateTime);
            pageRegistration.setUpdateDate(parse);
        } catch (ParseException e) {
            logger.warn("phân tích Ngày Thất bại: {}", dateTime, e);
        }
        List<String> strings = registrationService.whetherChargeOrNot(pageRegistration);
        if(!CollectionUtils.isEmpty(strings)){
            throw new ServiceException("Tồn tại Thuốc/Vật tư còn Chưa Thu phí, Không thể tiến hành dòng kiểm kê!");
        }
    }

    public List<InventoryVerification> getByCompanyId(String companyId) {
        List<InventoryVerification> inventoryVerifications = this.dao.getByCompanyId(companyId,"0");
        return inventoryVerifications;
    }
}