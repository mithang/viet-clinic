package com.geeke.stock.service;
import com.geeke.common.constants.BizConstants;

import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.org.entity.Company;
import com.geeke.org.service.CompanyService;
import com.geeke.outpatient.entity.RecipelDetail;
import com.geeke.outpatient.entity.RecipelInfo;
import com.geeke.outpatient.entity.Registration;
import com.geeke.outpatient.service.RecipelDetailService;
import com.geeke.outpatient.service.RecipelInfoService;
import com.geeke.outpatient.service.RegistrationService;
import com.geeke.stock.dao.MedicinalStockControlDao;
import com.geeke.stock.dao.SupplierStockDao;
import com.geeke.stock.entity.*;
import com.geeke.sys.entity.DictItem;
import com.geeke.utils.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * Tồn kho nhà cung cấpService
 *
 * @author txl
 * @version 2022-06-09
 */

@Service("supplierStockService")
@Transactional(readOnly = true)
public class SupplierStockService extends CrudService<SupplierStockDao, SupplierStock> {

    /** Tồn kho nhà cung cấp là Cơ sởTổng hưởngDữ liệu: Cho phép phòng khám thuộc cùng cơ sở truy cập dữ liệu phòng khám của cơ sở(Với danh sách #{institution} SQL Nhất quán) */
    @Override
    protected boolean isInstitutionShared() {
        return true;
    }


    @Autowired
    private DispensingService dispensingService;

    @Autowired
    private SupplierStockService supplierStockService;

    @Lazy
    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private SupplierStorageService supplierStorageService;

    @Lazy
    @Autowired
    private RecipelInfoService recipelInfoService;

    @Autowired
    private StuffService stuffService;

    @Lazy
    @Autowired
    private RecipelDetailService recipelDetailService;

    @Autowired
    private MedicinalStorageControlService medicinalStorageControlService;

    @Lazy
    @Autowired
    private InventoryVerificationService inventoryVerificationService;

    @Autowired
    private MedicinalStockControlDao medicinalStockControlDao;

    @Autowired
    private MedicinalStockControlService medicinalStockControlService;
    @Autowired
    private CompanyService companyService;
    @Transactional(readOnly = false)
    public void updateStock(DispensingEvt dispensingEvt) {

        Company company = SessionUtils.getUser().getCompany();
        //Đang tiến hành kiểm kê giờKhông thểtiến hành dòngphát thuốcthao tác
        List<InventoryVerification> inventoryVerifications =
                inventoryVerificationService.getByCompanyId(company.getId());
        if ("0".equals(dispensingEvt.getDispensingType())) {
            if (!CollectionUtils.isEmpty(inventoryVerifications)) {
                throw new ServiceException("Đang tiến hành kiểm kê, không thể tiến hành dòngphát thuốcthao tác!");

            }
        } else {
            if (!CollectionUtils.isEmpty(inventoryVerifications)) {
                throw new ServiceException("Đang tiến hành kiểm kê, không thể tiến hành dòngTrả thuốcthao tác!");

            }
        }
        String registrationId = dispensingEvt.getRegistrationId();
        Registration registration = registrationService.get(registrationId);
        if ("0".equals(dispensingEvt.getDispensingType())) {

            //Phát thuốc
            for (String recipelInfoId : dispensingEvt.getRecipelInfoIdList()) {
                //tiến hành dòngđộngTồn khochiếm dụng thực tế
                medicinalStorageControlService.okOccupyStock(registrationId, recipelInfoId);
            }


            //Cập nhật trạng thái đơn thuốc
            List<String> recipelInfoIdList = dispensingEvt.getRecipelInfoIdList();
            if (CollectionUtils.isEmpty(recipelInfoIdList)) {
                throw new ServiceException("phát thuốc của Đơn thuốcThông tinKhông thể là trống");
            }

            for (String recipelInfoId : recipelInfoIdList) {
                RecipelInfo recipelInfo = this.recipelInfoService.get(recipelInfoId);
                if (recipelInfo == null || !"0".equals(recipelInfo.getIsDispension())) {
                    throw new ServiceException("Đơn thuốcThông tinKiểm traThất bại, Không tồn tại hoặc Đãphát thuốcqua");
                }
                recipelInfo.setIsDispension("1");
                recipelInfo.setDispensionDate(new Date());
                recipelInfo.setDispensionStatus(1);
                this.recipelInfoService.save(recipelInfo);
            }


            registration.setDispensingDate(new Date());
        } else {
            //Trả thuốc
            List<Parameter> parameters = SearchParamsBuilder.create()
                    .eq("registration_id", registrationId)
                    .in("recipel_info_id", dispensingEvt.getRecipelInfoIdList())
                    .build();
            List<Dispensing> dispensingList = dispensingService.listAll(parameters, "");
            if (null != dispensingList && !dispensingList.isEmpty()) {
            }
            //Cập nhật trạng thái đơn thuốc
            List<String> recipelInfoIdList = dispensingEvt.getRecipelInfoIdList();
            if (CollectionUtils.isEmpty(recipelInfoIdList)) {
                throw new ServiceException("phát thuốc của Đơn thuốcThông tinKhông thể là trống");
            }
            for (String recipelInfoId : recipelInfoIdList) {
                RecipelInfo recipelInfo = this.recipelInfoService.get(recipelInfoId);
                if (recipelInfo == null || !"1".equals(recipelInfo.getIsDispension())) {
                    throw new ServiceException("Đơn thuốcThông tinKiểm traThất bại, Không tồn tại hoặc ĐãTrả thuốcqua");
                }
                if (BizConstants.RECIPEL_TYPE_INFUSION.equals(recipelInfo.getRecipelType().getValue())) {
                    List<RecipelDetail> recipelDetails = recipelDetailService.getByRecipelInfoId(recipelInfo.getId());
                    for (RecipelDetail recipelDetail :
                            recipelDetails) {
                        if (recipelDetail.getExecutions() != null) {
                            throw new ServiceException("Truyền dịchĐơn thuốcĐãhành nghề dòng, Không thể hoàn phí!");
                        }
                    }
                }
                //Trả thuốctạmkhônghoàn phí
                medicinalStorageControlService.drugRepercussion(recipelInfo);

                recipelInfo.setIsDispension("2");
                recipelInfo.setDispensionStatus(-1);
                recipelInfo.setRetreatChargeDate(new Date());
                this.recipelInfoService.save(recipelInfo);
                //Sửaphát thuốcThông tinbảng
                dispensingService.updateDelFlag(recipelInfoId);
            }
            registration.setReturnDate(new Date());
        }
        //Sửaphát thuốcTrạng thái
        registration.setDispensingStatus(dispensingEvt.getDispensingStatus());
        //registration.setDispensingDate(new Date());
        registrationService.save(registration);
    }

    public List<SupplierStock> getByStorageId(String id) {
        return this.dao.getByStorageId(id);
    }

    @Transactional(readOnly = false)
    public void saves(StorageEvt storageEvt) {
        // Thêm mớiThuốc hoặc Vật tưTồn kho giờ, Nếu là khởi tạo tồn kho thì khởi tạo trước dữ liệu bản ghi kiểm soát tổng tồn kho
        for (SupplierStock entity : storageEvt.getSupplierStockList()) {
            String companyID = entity.getCompany().getId();
            String drugID = entity.getDrug().getId();
            // String drug_stuff_ID = drugID != null ? drugID : stuffID;

            if (null != drugID) {
                List<MedicinalStockControl> inventory = medicinalStockControlDao.inventory(companyID, drugID);
                if (inventory.isEmpty()) {
                    Drug drugTemp = drugService.get(drugID);
                    drugTemp.getCompany().setId(companyID);
                    this.medicinalStockControlService.initStockTo(drugTemp);
                }
            }
            String stuffID = entity.getStuff().getId();
            if (null != stuffID) {
                List<MedicinalStockControl> inventory = medicinalStockControlDao.inventory(companyID, stuffID);
                if (inventory.isEmpty()) {
                    Stuff drugTemp = stuffService.get(stuffID);
                    drugTemp.getCompany().setId(companyID);
                    this.medicinalStockControlService.initStockTo(drugTemp);
                }
            }
        }
    }

    //thônphiếu nhập kho và Tồn kho
    @Transactional(readOnly = false)
    public void savesTo(StorageEvt storageEvt) {
        //trướcLưuphiếu nhập kho
        SupplierStorage supplierStorage = storageEvt.getSupplierStorage();
        supplierStorage.setInitial(supplierStorage.getNumber());
        SupplierStorage save = supplierStorageService.save(supplierStorage);
        if (null != storageEvt.getSupplierStockList() && !storageEvt.getSupplierStockList().isEmpty()) {
            for (SupplierStock entity : storageEvt.getSupplierStockList()) {


                //Lưu số phiếu nhập kho vào từng bảng nhập kho
                entity.setSupplierStorage(save);
                entity.setInitial(entity.getNumber());
                entity.setCancellation("0");
                String id = supplierStockService.save(entity).getId();
                //Sau khi lưu thành công, lưu tồn kho của mặt hàng đó vào cơ sở dữ liệu tương ứng
//                if(id!=null){
//                    if(entity.getDrug().getId()!=null){
//                        //thìLấytương ứng của ThuốcTồn kho
//                        drugService.updateInventory_3502(entity.getNumber(),entity.getDrug().getId());
//                    }else if (entity.getStuff().getId()!=null){
//                        stuffService.updateInventory_3502(entity.getNumber(),entity.getStuff().getId());
//                    }
//                }
            }

            //độngTồn khokiểm soát
            this.medicinalStorageControlService.addStorageStock(save);
        }
    }


    /*
     * Lấynhập khoThuốc của Trung bìnhgiá nhập*/
    @Transactional
    public BigDecimal getByDrugId(String drugOrStuffId) {
        BigDecimal byDrugOrStuffId = this.dao.getByDrugId(drugOrStuffId);

        return byDrugOrStuffId;
    }

    @Transactional(readOnly = false)
    public void cancellation(List<SupplierStock> supplierStockList) {
        this.dao.updateCancel(supplierStockList);
    }

    public BigDecimal getByStuffId(String stuffId) {
        BigDecimal supplierStocDecimal = this.dao.getByStuffId(stuffId);
        return supplierStocDecimal;
    }

    @Transactional
    public List<SupplierStock> getByStuffIdDetail(String drugStuffId) {
        List<SupplierStock> supplierStockList = this.dao.getByStuffIdDetail(drugStuffId);
        return supplierStockList;
    }

    public Page<SupplierStock> getDrugIndateWarning(List<Parameter> parameters, int offset, int limit, String
            orderby) {
        // Loại bỏ ở frontend company_id(Ngăn vượt quyền), Thống nhất lấy tenant đăng nhập trong phiên làm ranh giới cách ly
        Parameter.extractAndRemoveCompanyId(parameters);
        String id = SessionUtils.getLoginTenantId();
        if ("null".equals(id)) {
            id = null;
        }
        String institution = companyService.getInstitution(id);

        PageRequest pageRequest = new PageRequest(offset, limit, parameters, orderby, id, institution);
        return paginate(
            () -> this.dao.countWarning(pageRequest),
            () -> {
                List<SupplierStock> list = this.dao.getDrugIndateWarning(pageRequest);
                for (SupplierStock supplierStock : list) {
                    if (!Objects.isNull(supplierStock.getMedicinalStorageControl()) && !Objects.isNull(supplierStock.getMedicinalStorageControl().getStorageStock())) {
                        String number =
                                supplierStock.getMedicinalStorageControl().getStorageStock().subtract(supplierStock.getMedicinalStorageControl().getUsedStock().add(supplierStock.getMedicinalStorageControl().getReimburseStock())).stripTrailingZeros().toPlainString();
                        supplierStock.setNumber(Integer.parseInt(number));
                    } else {
                        supplierStock.setNumber(0);
                    }
                }
                return list;
            }
        );
    }

    public Page<SupplierStock> getStuffIndateWarning(List<Parameter> parameters, int offset, int limit,
                                                     String orderby) {
        PageRequest pageRequest = new PageRequest(offset, limit, parameters, orderby);
        return paginate(
            () -> this.dao.countStuffWarning(pageRequest),
            () -> {
                List<SupplierStock> list = this.dao.getStuffIndateWarning(pageRequest);
                for (SupplierStock supplierStock : list) {
                    if (!Objects.isNull(supplierStock.getMedicinalStorageControl()) && !Objects.isNull(supplierStock.getMedicinalStorageControl().getStorageStock())) {
                        String number =
                                supplierStock.getMedicinalStorageControl().getStorageStock().subtract(supplierStock.getMedicinalStorageControl().getUsedStock().add(supplierStock.getMedicinalStorageControl().getReimburseStock())).stripTrailingZeros().toPlainString();
                        supplierStock.setNumber(Integer.parseInt(number));
                    } else {
                        supplierStock.setNumber(0);
                    }
                }
                return list;
            }
        );
    }

    /**
     * nhập khoChờ xét duyệt
     *
     * @param storageEvt
     */
    @Transactional
    public void savesToAudit(StorageEvt storageEvt) {
        //trướcLưuphiếu nhập kho
        SupplierStorage supplierStorage = storageEvt.getSupplierStorage();
        supplierStorage.setInitial(supplierStorage.getNumber());
        SupplierStorage save = supplierStorageService.save(supplierStorage);


        if (null != storageEvt.getSupplierStockList() && !storageEvt.getSupplierStockList().isEmpty()) {
            for (SupplierStock entity : storageEvt.getSupplierStockList()) {

                //Lưu số phiếu nhập kho vào từng bảng nhập kho
                entity.setSupplierStorage(save);
                entity.setInitial(entity.getNumber());
                entity.setCancellation("0");
                supplierStockService.save(entity);

            }
        }

    }

    /**
     * xét duyệtQuavànhập kho
     *
     * @param storageEvt
     */

    @Transactional
    public void auditStorage(StorageEvt storageEvt) {
        DictItem examine = storageEvt.getSupplierStorage().getExamine();
        storageEvt.getSupplierStockList().forEach(stock -> {
            SupplierStorage storage = supplierStorageService.get(stock.getId());
            if (storage.getExamine().getValue().equalsIgnoreCase(BizConstants.SUPPLIER_STORAGE_EXAMINE_PENDING)) {
                storage.setExamine(examine);
                storage.setExamineDate(new Date());
                supplierStorageService.save(storage);
                //độngTồn khokiểm soát
                this.medicinalStorageControlService.addStorageStock(storage);
            }
        });
    }
}