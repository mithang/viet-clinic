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

    @Transactional
    public  void updateAllPrice(String id, BigDecimal allPrice) {
        this.dao.updateAllPrice(id,allPrice);
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
        int i = this.dao.accomplishInventoryVerification(inventoryVerification);
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
        medicinalStorageControlService.save(medicinalStorageControl);

        //Truy vấnđộngTồn khotổngkiểm soátbảng
        List<Parameter> parameters = SearchParamsBuilder.create()
                .eq("drug_stuff_id", medicinalStorageControl.getDrug().getId())
                .build();
        List<MedicinalStockControl> medicinalStockControls = medicinalStockControlService.listAll(parameters, "");
        if (CollectionUtils.isEmpty(medicinalStockControls)) {
            return;
        }

        //độngTồn khotổngkiểm soátbảngSửa
        MedicinalStockControl medicinalStockControl = medicinalStockControls.get(0);
        BigDecimal newControlStock = medicinalStockControl.getStorageStock().add(new BigDecimal(profitAndLoss));
        medicinalStockControl.setStorageStock(newControlStock);
        medicinalStockControl.setSurplusStock(newControlStock);
        medicinalStockControlService.save(medicinalStockControl);

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
        medicinalStockRecordService.save(medicinalStockRecord);

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
            logger.warn("phân tíchNgàyThất bại: {}", dateTime, e);
        }

        List<String> strings = registrationService.whetherTheDispensing(pageRegistration);
        if(!CollectionUtils.isEmpty(strings)){
            throw new ServiceException("Tồn tạiThuốc/Vật tưcònChưaphát thuốc, Không thểtiến hành dòngkiểm kê!");
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
            logger.warn("phân tíchNgàyThất bại: {}", dateTime, e);
        }
        List<String> strings = registrationService.whetherChargeOrNot(pageRegistration);
        if(!CollectionUtils.isEmpty(strings)){
            throw new ServiceException("Tồn tạiThuốc/Vật tưcònChưaThu phí, Không thểtiến hành dòngkiểm kê!");
        }
    }

    public List<InventoryVerification> getByCompanyId(String companyId) {
        List<InventoryVerification> inventoryVerifications = this.dao.getByCompanyId(companyId,"0");
        return inventoryVerifications;
    }
}