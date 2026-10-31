package com.geeke.stock.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import com.alibaba.fastjson.JSONObject;
import com.geeke.outpatient.controller.RegistrationController;
import com.geeke.outpatient.entity.Registration;
import com.geeke.outpatient.service.RegistrationService;
import com.geeke.utils.SessionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.CollectionUtils;

import com.geeke.common.service.ServiceException;
import com.geeke.outpatient.entity.RecipelDetail;
import com.geeke.outpatient.entity.RecipelInfo;
import com.geeke.outpatient.service.RecipelDetailService;
import com.geeke.stock.dao.MedicinalStorageControlDao;
import com.geeke.stock.entity.*;
import com.geeke.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thuốc/Kiểm soát chi tiết nhập kho vật tưService
 * @author hl
 * @version 2022-09-26
 */

@Service("medicinalStorageControlService")
@Transactional(readOnly = true)
public class MedicinalStorageControlService extends CrudService<MedicinalStorageControlDao, MedicinalStorageControl>{

    private static final Logger logger = LoggerFactory.getLogger(MedicinalStorageControlService.class);
    @Autowired
    private DrugService drugService;
    @Autowired
    private StuffService stuffService;
    @Autowired
    private SupplierStockService supplierStockService;
    @Autowired
    private MedicinalStockControlService medicinalStockControlService;
    @Autowired
    private MedicinalStockRecordService medicinalStockRecordService;
    @Autowired
    private RecipelDetailService recipelDetailService;

    @Autowired
    private DispensingService dispensingService;

    @Lazy
    @Autowired
    private RegistrationService registrationService;
    /**
     * Thêm mớinhập khoThông tin giờ, kiểm soátThuốc hoặc Vật tư của độngTồn khoChi tiết
     * @param supplierStorage
     */
    @Transactional(readOnly = false)
    public void addStorageStock(SupplierStorage supplierStorage)
    {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        List<SupplierStock> list = this.supplierStockService.listAll(
                SearchParamsBuilder.create().eq("supplier_storage_id", supplierStorage.getId()).build(), "");
        if (CollectionUtils.isEmpty(list))
        {
            return;
        }

        for (SupplierStock supplierStock:list)
        {
            MedicinalStorageControl medicinalStorageControl = new MedicinalStorageControl();
            medicinalStorageControl.setCompany(supplierStock.getCompany());
            medicinalStorageControl.setStorageId(supplierStock.getId());
            if (Objects.nonNull(supplierStock.getDrug()) && StringUtils.isNotBlank(supplierStock.getDrug().getId()))
            {
                Drug drug = this.drugService.get(supplierStock.getDrug().getId());
                if (Objects.isNull(drug) || StringUtils.isBlank(drug.getId()))
                {
                    throw new ServiceException("Nhập kho thất bại, lấy thông tin cơ bản của thuốc nhập kho thất bại!");
                }
                this.medicinalStockControlService.initStock(drug);

                medicinalStorageControl.setDrugStuffId(drug.getId());
                medicinalStorageControl.setDrugStuffName(drug.getGoodsName());
                medicinalStorageControl.setType(1);
            }
            else if (Objects.nonNull(supplierStock.getStuff()) && StringUtils.isNotBlank(supplierStock.getStuff().getId()))
            {
                Stuff stuff = this.stuffService.get(supplierStock.getStuff().getId());
                if (Objects.isNull(stuff) || StringUtils.isBlank(stuff.getId()))
                {
                    throw new ServiceException("Nhập kho thất bại, lấy thông tin cơ bản của vật tư nhập kho thất bại!");
                }
                this.medicinalStockControlService.initStock(stuff);

                medicinalStorageControl.setDrugStuffId(stuff.getId());
                medicinalStorageControl.setDrugStuffName(stuff.getName());
                medicinalStorageControl.setType(2);
            }
            else
            {
                throw new ServiceException("Nhập kho thất bại, tạmchỉhỗ trợThuốc hoặc Vật tưnhập kho!");
            }

            medicinalStorageControl.setStorageStock(new BigDecimal(supplierStock.getInitial()));
            medicinalStorageControl.setUsedStock(BigDecimal.ZERO);
            medicinalStorageControl.setOccupyStock(BigDecimal.ZERO);
            medicinalStorageControl.setSurplusStock(new BigDecimal(supplierStock.getInitial()));
            medicinalStorageControl.setReimburseStock(BigDecimal.ZERO);

            //ThêmThuốc/Kiểm soát chi tiết nhập kho vật tưKhởi tạoDữ liệu
            super.save(medicinalStorageControl);

            List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                    SearchParamsBuilder.create()
                            .eq("drug_stuff_id", medicinalStorageControl.getDrugStuffId())
                            .eq("company_id", medicinalStorageControl.getCompany().getId())
                            .build(), "");
            if (CollectionUtils.isEmpty(medicinalStockControlList)) {
                return;
            }
            MedicinalStockControl medicinalStockControl = medicinalStockControlList.get(0);
            medicinalStockControl.setStorageStock(medicinalStockControl.getStorageStock().add(medicinalStorageControl.getStorageStock()));
            medicinalStockControl.setSurplusStock(medicinalStockControl.getSurplusStock().add(medicinalStorageControl.getSurplusStock()));
            //Cập nhật bản ghi kiểm soát tổng hợp nhập kho của tồn kho động
            this.medicinalStockControlService.save(medicinalStockControl);

            //tăngTồn khothao tácChi tiếtBản ghi
            MedicinalStockRecord medicinalStockRecord = new MedicinalStockRecord();
            medicinalStockRecord.setCompany(supplierStock.getCompany());
            medicinalStockRecord.setDrugStuffId(medicinalStorageControl.getDrugStuffId());
            medicinalStockRecord.setDrugStuffName(medicinalStorageControl.getDrugStuffName());
            medicinalStockRecord.setType(medicinalStorageControl.getType());
            medicinalStockRecord.setOperationType(1);    //Tồn khonhập khothao tácđịnh danh
            medicinalStockRecord.setOperationStock(new BigDecimal(supplierStock.getInitial()));
            medicinalStockRecord.setStorageId(supplierStock.getSupplierStorage().getId());

            this.medicinalStockRecordService.save(medicinalStockRecord);
        }
    }

    //hủynhập khoThông tin, Cập nhậtđộngTồn kho
    @Transactional(readOnly = false)
    public void invalidStorageStock(SupplierStorage supplierStorage)
    {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        List<SupplierStock> list = this.supplierStockService.listAll(
                SearchParamsBuilder.create().eq("supplier_storage_id", supplierStorage.getId()).build(), "");
        if (CollectionUtils.isEmpty(list))
        {
            throw new ServiceException("nhập khoThông tinhủyThất bại, Chưatìm kiếmĐếnCầnhủy của Chi tiết nhập khoThông tin. ");
        }

        for (SupplierStock supplierStock:list)
        {
            List<MedicinalStorageControl> medicinalStorageControlList = super.listAll(
                    SearchParamsBuilder.create().eq("storage_id", supplierStock.getId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStorageControlList))
            {
                continue;
            }

            for (MedicinalStorageControl medicinalStorageControl:medicinalStorageControlList)
            {
                BigDecimal storageStock = medicinalStorageControl.getStorageStock();    //Tổng lượng nhập kho
                BigDecimal surplusStock = medicinalStorageControl.getSurplusStock();    //Số lượng tồn kho còn khả dụng
                if (storageStock.compareTo(surplusStock) != 0)
                {
                    throw new ServiceException("độngTồn khokiểm soátĐãphátthay đổi, Không thểhủyXóanhập khoDữ liệu!");
                }

                //1.hủynhập khoThông tin giờ, Đưakhả dụngTồn khođặt là 0
                medicinalStorageControl.setStorageStock(BigDecimal.ZERO);
                medicinalStorageControl.setSurplusStock(BigDecimal.ZERO);
                super.save(medicinalStorageControl);

                //2.Cập nhật bản ghi kiểm soát tổng hợp nhập kho của tồn kho động, Trừ lượng hủy khỏi tổng lượng nhập kho và tổng lượng khả dụng
                List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                        SearchParamsBuilder.create().eq("drug_stuff_id", medicinalStorageControl.getDrugStuffId()).build(), "");
                if (CollectionUtils.isEmpty(medicinalStockControlList)) {
                    return;
                }
                MedicinalStockControl medicinalStockControl = medicinalStockControlList.get(0);
                medicinalStockControl.setStorageStock(medicinalStockControl.getStorageStock().subtract(storageStock));
                medicinalStockControl.setSurplusStock(medicinalStockControl.getSurplusStock().subtract(surplusStock));
                this.medicinalStockControlService.save(medicinalStockControl);

                //3.chènChi tiết thao tác tồn kho động
                MedicinalStockRecord medicinalStockRecord = new MedicinalStockRecord();
                medicinalStockRecord.setCompany(medicinalStorageControl.getCompany());
                medicinalStockRecord.setDrugStuffId(medicinalStorageControl.getDrugStuffId());
                medicinalStockRecord.setDrugStuffName(medicinalStorageControl.getDrugStuffName());
                medicinalStockRecord.setType(medicinalStorageControl.getType());
                medicinalStockRecord.setOperationType(-1);    //Tồn khoĐịnh danh thao tác hủy nhập kho
                medicinalStockRecord.setOperationStock(storageStock);
                medicinalStockRecord.setStorageId(medicinalStorageControl.getStorageId());

                this.medicinalStockRecordService.save(medicinalStockRecord);
            }
        }
    }

    //Chi tiết nhập khotự độngĐếnLogic gọi chức năng theo kỳ(nàyXử lýPhần sau để giao diện frontend làm thủ công, Nếu không, chạy tác vụ định kỳ ở backend có thể gặp vấn đề)
    @Transactional(readOnly = false)
    public void autoInvalidStorageStock(SupplierStock supplierStock)
    {
        //Kiểm tra hiện tại còn tồn kho động bị chiếm dụng chưa được giải phóng không
        List<MedicinalStockRecord> medicinalStockRecords = this.medicinalStockRecordService.getStockRecordByStorageId(supplierStock.getId());
        if (!CollectionUtils.isEmpty(medicinalStockRecords))
        {
            throw new ServiceException("Hiện vẫn còn tình trạng tồn kho bị chiếm dụng hoặc trả thuốc chưa hoàn phí và tồn kho chưa được giải phóng, Vui lòngtrướcXử lýsaulạithao tác!");
        }

        List<MedicinalStorageControl> medicinalStorageControlList = super.listAll(
                SearchParamsBuilder.create().eq("storage_id", supplierStock.getId()).build(), "");
        if (CollectionUtils.isEmpty(medicinalStorageControlList))
        {
            return;
        }

        MedicinalStorageControl medicinalStorageControl = medicinalStorageControlList.get(0);
        BigDecimal storageStock = medicinalStorageControl.getStorageStock();		// Tổng lượng nhập kho
        BigDecimal usedStock = medicinalStorageControl.getUsedStock();		        // Số lượng tồn kho đã sử dụng
        BigDecimal occupyStock = medicinalStorageControl.getOccupyStock();		    // Số lượng tồn kho đang bị chiếm dụng
        BigDecimal surplusStock = medicinalStorageControl.getSurplusStock();        // Số lượng tồn kho còn khả dụng
        BigDecimal reimburseStock = medicinalStorageControl.getReimburseStock();    // Số lượng tồn kho báo hỏng
        medicinalStorageControl.setSurplusStock(BigDecimal.ZERO);
        medicinalStorageControl.setReimburseStock(reimburseStock.add(surplusStock));
        super.save(medicinalStorageControl);

        List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                SearchParamsBuilder.create().eq("drug_stuff_id", medicinalStorageControl.getDrugStuffId()).build(), "");
        if (CollectionUtils.isEmpty(medicinalStockControlList)) {
            return;
        }
        MedicinalStockControl medicinalStockControl = medicinalStockControlList.get(0);
        medicinalStockControl.setSurplusStock(medicinalStockControl.getSurplusStock().subtract(surplusStock));
        medicinalStockControl.setReimburseStock(medicinalStockControl.getReimburseStock().add(surplusStock));
        this.medicinalStockControlService.save(medicinalStockControl);

        //3.chènChi tiết thao tác tồn kho động
        MedicinalStockRecord medicinalStockRecord = new MedicinalStockRecord();
        medicinalStockRecord.setCompany(medicinalStorageControl.getCompany());
        medicinalStockRecord.setDrugStuffId(medicinalStorageControl.getDrugStuffId());
        medicinalStockRecord.setDrugStuffName(medicinalStorageControl.getDrugStuffName());
        medicinalStockRecord.setType(medicinalStorageControl.getType());
        medicinalStockRecord.setOperationType(-2);    //Tồn khoĐịnh danh thao tác hủy nhập kho
        medicinalStockRecord.setOperationStock(surplusStock);
        medicinalStockRecord.setStorageId(medicinalStorageControl.getStorageId());

        this.medicinalStockRecordService.save(medicinalStockRecord);
    }

    /**
     * TheoĐơn thuốclấyhủychiếm dụng trướcTồn khosố
     */
    @Transactional(readOnly = false)
    public void cancelOccupy(RecipelInfo recipelInfo) {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        String recipelInfoId = recipelInfo.getId();    //Đơn thuốcID
        if (StringUtils.isBlank(recipelInfoId))
        {
            return;
        }
        List<MedicinalStockRecord> list = this.medicinalStockRecordService.listAll(
                SearchParamsBuilder.create().eq("recipel_info_id", recipelInfoId).build(), "");
        if (CollectionUtils.isEmpty(list))
        {
            return;
        }

        for (MedicinalStockRecord medicinalStockRecord:list)
        {
            BigDecimal operationStock = medicinalStockRecord.getOperationStock();    //chiếm dụngTồn khosố
            //Cập nhậttrướcchiếm dụng của Chi tiết nhập kho
            List<MedicinalStorageControl> medicinalStorageControlList = super.listAll(
                    SearchParamsBuilder.create().eq("storage_id", medicinalStockRecord.getStorageId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStorageControlList))
            {
                return;
            }
            MedicinalStorageControl medicinalStorageControl = medicinalStorageControlList.get(0);
            medicinalStorageControl.setOccupyStock(medicinalStorageControl.getOccupyStock().subtract(operationStock));   //Số lượng tồn kho đang bị chiếm dụng hiện tại cần được trừ trả lại
            medicinalStorageControl.setSurplusStock(medicinalStorageControl.getSurplusStock().add(operationStock));     //Số lượng tồn kho còn khả dụngNênthêmtrả lạiđi
            //TODO: Trả lại đúng lúc gặp chi tiết nhập kho hết hạn thì xử lý thế nào?    giải quyếtPhương thức: khôngXử lý, củatrướcnhư thế nàogìchiếm dụng của Bây giờ trả lại như thế nào
            super.save(medicinalStorageControl);

            //Cập nhật lượng chiếm dụng trước của kiểm soát tổng
            List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                    SearchParamsBuilder.create().eq("drug_stuff_id", medicinalStockRecord.getDrugStuffId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStockControlList))
            {
                return;
            }
            MedicinalStockControl medicinalStockControl = medicinalStockControlList.get(0);
            medicinalStockControl.setOccupyStock(medicinalStockControl.getOccupyStock().subtract(operationStock));   //Số lượng tồn kho đang bị chiếm dụng hiện tại cần được trừ trả lại
            medicinalStockControl.setSurplusStock(medicinalStockControl.getSurplusStock().add(operationStock));     //Số lượng tồn kho còn khả dụngNênthêmtrả lạiđi
            this.medicinalStockControlService.save(medicinalStockControl);
        }

        //XóaChi tiếtDữ liệu(mụctrướclàmvật lýXóa)
        this.medicinalStockRecordService.batchDelete(list);
    }

    /**
     * Tồn kho đặt trước
     * @param recipelInfo
     */
    @Transactional(readOnly = false)
    public void preOccupyStock(RecipelInfo recipelInfo) {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        String recipelInfoId = recipelInfo.getId();    //Đơn thuốcID
        if (StringUtils.isBlank(recipelInfoId))
        {
            return;
        }
        //1.Giải phóng trước lượng chiếm dụng trướcTồn kho,báosổ sáchchiếm dụngTồn kho của Hiệu lựctính
        this.cancelOccupy(recipelInfo);
        //2.Lạichiếm dụng
        List<RecipelDetail> recipelDetailList = this.recipelDetailService.listAll(
                SearchParamsBuilder.create().eq("recipel_info_id", recipelInfoId).build(), "");
        if (CollectionUtils.isEmpty(recipelDetailList))
        {
            return;
        }

        for (RecipelDetail recipelDetail:recipelDetailList)
        {
            String drugStuffId = recipelDetail.getDrugStuffId().getDrugStuffId();
            BigDecimal calculationBigDecimal = BigDecimal.ZERO;                       //dùng đểTính, nhấtsau và  totalBigDecimal làmso vớisotiến hành dòngkiểm soát
            
            MedicinalStockRecord medicinalStockRecord = new MedicinalStockRecord();
            medicinalStockRecord.setCompany(recipelDetail.getCompany());
            medicinalStockRecord.setOperationType(2);    //Định danh thao tác chiếm dụng trước
            //TODO thêmmộtCáiPhòng khám Id  của Điều kiện
            Drug drug = this.drugService.get(drugStuffId);
            Stuff stuff = this.stuffService.get(drugStuffId);
            if (Objects.nonNull(drug) && StringUtils.isNotBlank(drug.getId()))
            {
                medicinalStockRecord.setDrugStuffId(drug.getId());
                medicinalStockRecord.setDrugStuffName(drug.getGoodsName());
                medicinalStockRecord.setType(1);
                if(Objects.equals(drug.getStatus(), "0"))
                {
                    throw new ServiceException("ThuốcĐãdừngdùng");
                }
            }
            else if (Objects.nonNull(stuff) && StringUtils.isNotBlank(stuff.getId()))
            {
                medicinalStockRecord.setDrugStuffId(stuff.getId());
                medicinalStockRecord.setDrugStuffName(stuff.getName());
                medicinalStockRecord.setType(2);
                if(Objects.equals(stuff.getStatus(), "0"))
                {
                    throw new ServiceException("Vật tưĐãdừngdùng");
                }
            }
            else
            {
                //mụctrướcchỉcóThuốc, Vật tưmớitiến hành dòngđộngTồn khokiểm soát, kháckhôngkiểm soát
                continue;
            }
            
            // Tồn khobảnglưu trữ của  có Đơn vị đóng gói
            // - khôngbán lẻ giờ: trực tiếpsử dụng total(Đơn vị đóng gói) và Tồn khoso vớiso, chiếm dụngcũngdùngĐơn vị đóng gói
            // - bán lẻ giờ: CầnĐưatổng lượng(Đơn vị bào chế)Chuyển đổi thànhĐơn vị đóng góiđếnchiếm dụngTồn kho
            BigDecimal totalBigDecimal;
            boolean isUnpackSell = recipelDetail.getIsUnpackSell() != null && recipelDetail.getIsUnpackSell() == 1;

            if (isUnpackSell) {
                // Bán lẻ: total  có Đơn vị bào chế, CầnChuyển đổi thànhĐơn vị đóng gói(hướng tớitrênLàm tròn)
                totalBigDecimal = new BigDecimal(recipelDetail.getTotal());
            } else {
                // Không bán lẻ: total  có Đơn vị đóng gói, trực tiếpsử dụng
                totalBigDecimal = new BigDecimal(recipelDetail.getTotal());
            }
            
            if (totalBigDecimal.compareTo(BigDecimal.ZERO) < 0)
            {
                throw new ServiceException("Đơn thuốcdùnglượngTồn tạikêâmsố của tình hình, Vui lòngđúngđiềnĐơn thuốckêThông tin!");
            }
            if (totalBigDecimal.compareTo(BigDecimal.ZERO) == 0)
            {
                continue;
            }

            String companyId = recipelInfo.getCompany().getId();
            List<MedicinalStorageControl> medicinalStorageControlList = super.dao.getSurplusStockByDrugStuffIdTo(drugStuffId, companyId);

            if (CollectionUtils.isEmpty(medicinalStorageControlList))
            {
                throw new ServiceException("[" + recipelInfo.getName() + "] trong [" + medicinalStockRecord.getDrugStuffName() + "]Tồn khokhông đủ!");
            }

            // bán lẻ giờ, CầnĐưatổng lượng(Đơn vị bào chế)Chuyển đổi thànhĐơn vị đóng gói
            BigDecimal stockToOccupy = totalBigDecimal;
            if (isUnpackSell && drug != null && drug.getPreparation() != null && Integer.valueOf(drug.getPreparation()) > 0) {
                // Đơn vị bào chếchuyển là Đơn vị đóng gói: hướng tớitrênLàm tròn(chắc chắnbảocóđủđủ của bào chếSố lượng)
                BigDecimal preparationBD = new BigDecimal(drug.getPreparation());
                stockToOccupy = totalBigDecimal.divide(preparationBD, 0, BigDecimal.ROUND_UP);
            }

            for (int i = 0; i < medicinalStorageControlList.size(); i++) {
                MedicinalStorageControl medicinalStorageControl = medicinalStorageControlList.get(i);
                if (stockToOccupy.compareTo(BigDecimal.ZERO) == 0)
                {
                    break;
                }

                BigDecimal surplusStock = medicinalStorageControl.getSurplusStock();   //Số lượng tồn kho còn khả dụng(Đơn vị đóng gói)
                BigDecimal occupyStock = medicinalStorageControl.getOccupyStock();		// Số lượng tồn kho đang bị chiếm dụng

                if (surplusStock.compareTo(stockToOccupy) >= 0)   //Chi tiếtTồn khođủdùng
                {
                    medicinalStockRecord.setId(null);
                    medicinalStockRecord.setOperationStock(stockToOccupy);  //Số lượng tồn kho thao tác
                    medicinalStockRecord.setStorageId(medicinalStorageControl.getStorageId());    //chiếm dụng của Chi tiết nhập khoID
                    medicinalStockRecord.setRecipelInfoId(recipelInfoId);  //chiếm dụng của Đơn thuốcID
                    medicinalStockRecord.setRefId(recipelDetail.getId());  //chiếm dụng của liên kếtBản ghiID
                    this.medicinalStockRecordService.save(medicinalStockRecord);

                    medicinalStorageControl.setOccupyStock(occupyStock.add(stockToOccupy));
                    medicinalStorageControl.setSurplusStock(surplusStock.subtract(stockToOccupy));
                    super.save(medicinalStorageControl);

                    calculationBigDecimal = calculationBigDecimal.add(stockToOccupy);
                    stockToOccupy = stockToOccupy.subtract(stockToOccupy);
                }
                else                                            //Chi tiếtTồn khokhôngđủdùng
                {
                    medicinalStockRecord.setId(null);
                    medicinalStockRecord.setOperationStock(surplusStock);  //Số lượng tồn kho thao tác
                    medicinalStockRecord.setStorageId(medicinalStorageControl.getStorageId());    //chiếm dụng của Chi tiết nhập khoID
                    medicinalStockRecord.setRecipelInfoId(recipelInfoId);  //chiếm dụng của Đơn thuốcID
                    medicinalStockRecord.setRefId(recipelDetail.getId());  //chiếm dụng của liên kếtBản ghiID
                    this.medicinalStockRecordService.save(medicinalStockRecord);

                    medicinalStorageControl.setOccupyStock(occupyStock.add(surplusStock));
                    medicinalStorageControl.setSurplusStock(surplusStock.subtract(surplusStock));    //Số lượng tồn kho còn khả dụng,tươngkhitại0
                    super.save(medicinalStorageControl);

                    calculationBigDecimal = calculationBigDecimal.add(surplusStock);
                    stockToOccupy = stockToOccupy.subtract(surplusStock);
                }
            }

            if (stockToOccupy.compareTo(BigDecimal.ZERO) > 0)
            {
                throw new ServiceException("[" + recipelInfo.getName() + "] trong [" + medicinalStockRecord.getDrugStuffName() + "]Tồn khokhông đủ!");
            }

            //Cập nhật lượng chiếm dụng trước của kiểm soát tổng
            List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                    SearchParamsBuilder.create()
                            .eq("drug_stuff_id", medicinalStockRecord.getDrugStuffId())
                            .eq("company_id", companyId)
                            .build(), "");
            if (CollectionUtils.isEmpty(medicinalStockControlList))
            {
                return;
            }
            MedicinalStockControl medicinalStockControl = medicinalStockControlList.get(0);
            medicinalStockControl.setOccupyStock(medicinalStockControl.getOccupyStock().add(calculationBigDecimal));   //Số lượng tồn kho đang bị chiếm dụngNênthêmtrênđi
            medicinalStockControl.setSurplusStock(medicinalStockControl.getSurplusStock().subtract(calculationBigDecimal));     //Số lượng tồn kho còn khả dụngNêntrừdướiđi
            this.medicinalStockControlService.save(medicinalStockControl);
        }
    }

    /**
     * xác nhậnchiếm dụngTồn kho(tức là: Chuyển tồn kho chiếm dụng trước thành tồn kho chiếm dụng thực tế)
     * Thông thường khi phát thuốc tức là chuyển tồn kho chiếm dụng trước thành tồn kho chiếm dụng thực tế
     * @param
     */
    @Transactional(readOnly = false)
    public void okOccupyStock(String registrationId, String recipelInfoIds) {
        Registration registration = new Registration();
        registration.setId(registrationId);

        RecipelInfo recipelInfo = new RecipelInfo();
        recipelInfo.setId(recipelInfoIds);
        Registration entity = registrationService.get(registrationId);
        recipelInfo.setCompany(entity.getCompany());

        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        String recipelInfoId = recipelInfo.getId();    //Đơn thuốcID
        if (StringUtils.isBlank(recipelInfoId))
        {
            return;
        }
        //Lạitrướcchiếm dụngTồn khomộtĐưa,  là rồihơntốt của kiểm soátTồn khoKhông cóvấn đề, NếuLạichiếm dụngcóvấn đề, mộtthông thườngGiải thíchkiểm soátcótôiđâyđề xuất
        this.preOccupyStock(recipelInfo);

        //Chuyển tồn kho chiếm dụng trước thành tồn kho chiếm dụng thực tế
        List<MedicinalStockRecord> medicinalStockRecordList = this.medicinalStockRecordService.listAll(
                SearchParamsBuilder.create().eq("recipel_info_id", recipelInfoId).build(), "");
        if (CollectionUtils.isEmpty(medicinalStockRecordList))
        {
            logger.warn("Đơn thuốc[{}]Không tìm thấy bản ghi thao tác tồn kho, Bỏ qua xử lý tồn kho", recipelInfoId);
            return;
        }

        for (MedicinalStockRecord medicinalStockRecord:medicinalStockRecordList)
        {
            BigDecimal operationStock = medicinalStockRecord.getOperationStock();		// Số lượng tồn kho thao tác
            List<MedicinalStorageControl> medicinalStorageControlList = super.listAll(
                    SearchParamsBuilder.create().eq("storage_id", medicinalStockRecord.getStorageId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStorageControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy chi tiết nhập kho cần kiểm soát tồn kho[Chi tiếtID=" + medicinalStockRecord.getStorageId() + "]. ");
            }
            MedicinalStorageControl medicinalStorageControl = medicinalStorageControlList.get(0);
            medicinalStorageControl.setUsedStock(medicinalStorageControl.getUsedStock().add(operationStock));            //Số lượng tồn kho đã sử dụng hiện tại cần được cộng thêm
            medicinalStorageControl.setOccupyStock(medicinalStorageControl.getOccupyStock().subtract(operationStock));   //Số lượng tồn kho đang bị chiếm dụng hiện tại cần được trừ trả lại
            super.save(medicinalStorageControl);

            //Cập nhật lượng chiếm dụng trước của kiểm soát tổng
            List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                    SearchParamsBuilder.create().eq("drug_stuff_id", medicinalStockRecord.getDrugStuffId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStockControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy mặt hàng[ " + medicinalStockRecord.getDrugStuffName() + " ] của bản ghi kiểm soát tổng tồn kho. ");
            }
            MedicinalStockControl medicinalStockControl = medicinalStockControlList.get(0);
            medicinalStockControl.setUsedStock(medicinalStockControl.getUsedStock().add(operationStock));            //Số lượng tồn kho đã sử dụng hiện tại cần được cộng thêm
            medicinalStockControl.setOccupyStock(medicinalStockControl.getOccupyStock().subtract(operationStock));   //Số lượng tồn kho đang bị chiếm dụng hiện tại cần được trừ trả lại
            this.medicinalStockControlService.save(medicinalStockControl);

            //Cập nhật trạng thái bản ghi thao tác tồn kho động
            medicinalStockRecord.setOperationType(3);
            this.medicinalStockRecordService.save(medicinalStockRecord);

            //LưuChi tiết phát thuốc
            Dispensing dispensing = new Dispensing();
            dispensing.setRegistration(registration);
            dispensing.setRecipelInfo(recipelInfo);
            dispensing.setId("");
            SupplierStock supplierStock = new SupplierStock();
            supplierStock.setId(medicinalStorageControl.getStorageId());
            dispensing.setSupplierStock(supplierStock);
            dispensing.setCompany(medicinalStockControl.getCompany());
            dispensing.setNumber(Integer.parseInt(operationStock.stripTrailingZeros().toPlainString()));
            dispensingService.save(dispensing);
        }
    }

    /**
     * Trả thuốctạmkhônghoàn phítình hình:nàytình hìnhdướithuốc của Đãsử dụnglượngcầntrừrơi, Lượng tồn kho khả dụng phải đợi hoàn phí xong mới được cộng thêm
     * @param recipelInfo
     */
    @Transactional(readOnly = false)
    public void drugRepercussion(RecipelInfo recipelInfo)
    {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        String recipelInfoId = recipelInfo.getId();    //Đơn thuốcID
        if (StringUtils.isBlank(recipelInfoId))
        {
            return;
        }

        List<MedicinalStockRecord> medicinalStockRecordList = this.medicinalStockRecordService.listAll(
                SearchParamsBuilder.create()
                        .eq("recipel_info_id", recipelInfoId)
                        .eq("operation_type", 3)
                        .build(), "");
        if (CollectionUtils.isEmpty(medicinalStockRecordList))
        {
            logger.warn("Đơn thuốc[{}]Không tìm thấy bản ghi thao tác tồn kho, Bỏ qua xử lý tồn kho", recipelInfoId);
            return;
        }

        for (MedicinalStockRecord medicinalStockRecord:medicinalStockRecordList)
        {
            BigDecimal operationStock = medicinalStockRecord.getOperationStock();		// Số lượng tồn kho thao tác
            List<MedicinalStorageControl> medicinalStorageControlList = super.listAll(
                    SearchParamsBuilder.create().eq("storage_id", medicinalStockRecord.getStorageId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStorageControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy chi tiết nhập kho cần kiểm soát tồn kho[Chi tiếtID=" + medicinalStockRecord.getStorageId() + "]. ");
            }
            MedicinalStorageControl medicinalStorageControl = medicinalStorageControlList.get(0);
            medicinalStorageControl.setUsedStock(medicinalStorageControl.getUsedStock().subtract(operationStock));            //Số lượng tồn kho đã sử dụng hiện tại cần được trừ trả lại
            BigDecimal returnTodoFeeStock1 =medicinalStorageControl.getReturnTodoFeeStock()==null?new BigDecimal("0"):medicinalStorageControl.getReturnTodoFeeStock();
            medicinalStorageControl.setReturnTodoFeeStock(returnTodoFeeStock1.add(operationStock));

            super.save(medicinalStorageControl);

            //Cập nhật lượng chiếm dụng trước của kiểm soát tổng
            List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                    SearchParamsBuilder.create().eq("drug_stuff_id", medicinalStockRecord.getDrugStuffId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStockControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy mặt hàng[ " + medicinalStockRecord.getDrugStuffName() + " ] của bản ghi kiểm soát tổng tồn kho. ");
            }
            MedicinalStockControl medicinalStockControl = medicinalStockControlList.get(0);
            medicinalStockControl.setUsedStock(medicinalStockControl.getUsedStock().subtract(operationStock));            //Số lượng tồn kho đã sử dụng hiện tại cần được trừ trả lại
            BigDecimal returnTodoFeeStock2 =medicinalStockControl.getReturnTodoFeeStock()==null?new BigDecimal("0"):medicinalStockControl.getReturnTodoFeeStock();
            medicinalStockControl.setReturnTodoFeeStock(returnTodoFeeStock2.add(operationStock));
            this.medicinalStockControlService.save(medicinalStockControl);

            //Cập nhật trạng thái bản ghi thao tác tồn kho động
            medicinalStockRecord.setOperationType(4);
            this.medicinalStockRecordService.save(medicinalStockRecord);
        }
    }

    /**
     * Trả thuốcsau, tiến hành dònghoàn phí của tình hình:Trong trường hợp này do trước đóTrả thuốcthao tácĐãĐưa”Số lượng tồn kho đã sử dụng“trừtrả lạiđirồi, này lầnchỉĐưakhả dụngTồn khothêmtrênđithì dòngrồi
     * @param recipelInfo
     */
    @Transactional(readOnly = false)
    public void goBackFee(RecipelInfo recipelInfo)
    {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        String recipelInfoId = recipelInfo.getId();    //Đơn thuốcID
        if (StringUtils.isBlank(recipelInfoId))
        {
            return;
        }

        List<MedicinalStockRecord> medicinalStockRecordList = this.medicinalStockRecordService.listAll(
                SearchParamsBuilder.create()
                        .eq("recipel_info_id", recipelInfoId)
                        .eq("operation_type", 4)
                        .build(), "");
        if (CollectionUtils.isEmpty(medicinalStockRecordList))
        {
            logger.warn("Đơn thuốc[{}]Không tìm thấy bản ghi thao tác tồn kho, Bỏ qua xử lý tồn kho", recipelInfoId);
            return;
        }

        for (MedicinalStockRecord medicinalStockRecord:medicinalStockRecordList)
        {
            BigDecimal operationStock = medicinalStockRecord.getOperationStock();		// Số lượng tồn kho thao tác
            List<MedicinalStorageControl> medicinalStorageControlList = super.listAll(
                    SearchParamsBuilder.create().eq("storage_id", medicinalStockRecord.getStorageId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStorageControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy chi tiết nhập kho cần kiểm soát tồn kho[Chi tiếtID=" + medicinalStockRecord.getStorageId() + "]. ");
            }
            MedicinalStorageControl medicinalStorageControl = medicinalStorageControlList.get(0);
            medicinalStorageControl.setSurplusStock(medicinalStorageControl.getSurplusStock().add(operationStock));            //Số lượng tồn kho còn khả dụng hiện tại cần được cộng trả lại
            medicinalStorageControl.setReturnTodoFeeStock(medicinalStorageControl.getReturnTodoFeeStock().subtract(operationStock));
            super.save(medicinalStorageControl);

            //Cập nhật lượng chiếm dụng trước của kiểm soát tổng
            List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                    SearchParamsBuilder.create().eq("drug_stuff_id", medicinalStockRecord.getDrugStuffId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStockControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy mặt hàng[ " + medicinalStockRecord.getDrugStuffName() + " ] của bản ghi kiểm soát tổng tồn kho. ");
            }
            MedicinalStockControl medicinalStockControl = medicinalStockControlList.get(0);
            medicinalStockControl.setSurplusStock(medicinalStockControl.getSurplusStock().add(operationStock));            //Số lượng tồn kho còn khả dụng hiện tại cần được cộng trả lại
            medicinalStockControl.setReturnTodoFeeStock(medicinalStockControl.getReturnTodoFeeStock().subtract(operationStock));
            this.medicinalStockControlService.save(medicinalStockControl);
        }

        //Xóachiếm dụngthao tácChi tiết(này lầnvật lýXóa)
        this.medicinalStockRecordService.batchDelete(medicinalStockRecordList);
    }

    //Lấytất cả của Tồn khoChi tiết
    @Transactional(readOnly = true)
    public List<MedicinalStorageControl> getAll(String compayId, String type,String variety) {
        return this.dao.getAll(compayId,type,variety);
    }

    @Transactional(readOnly = true)
    public BigDecimal getByDrugOrStuffId(String drugOrStuffId) {
        return this.dao.getByDrugOrStuffId(drugOrStuffId, SessionUtils.getLoginTenantId());
    }

    @Transactional(readOnly = true)
    public BigDecimal getByDrugOrStuffIds(List<String> drugOrStuffIds) {
        return this.dao.getByDrugOrStuffIds(drugOrStuffIds);
    }



    /**
     * Hoàn phí hạng mục khám chữa bệnhVật tưthao tác
     *
     * @param recipelInfo
     */
    @Transactional
    public void materialRefund(RecipelInfo recipelInfo) {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        String recipelInfoId = recipelInfo.getId();    //Đơn thuốcID
        if (StringUtils.isBlank(recipelInfoId))
        {
            return;
        }

        List<MedicinalStockRecord> medicinalStockRecordList = this.medicinalStockRecordService.listAll(
                SearchParamsBuilder.create()
                        .eq("recipel_info_id", recipelInfoId)
                        .eq("operation_type", 3)
                        .build(), "");
        if (CollectionUtils.isEmpty(medicinalStockRecordList))
        {
            logger.warn("Đơn thuốc[{}]Không tìm thấy bản ghi thao tác tồn kho, Bỏ qua xử lý tồn kho", recipelInfoId);
            return;
        }

        for (MedicinalStockRecord medicinalStockRecord:medicinalStockRecordList)
        {
            BigDecimal operationStock = medicinalStockRecord.getOperationStock();		// Số lượng tồn kho thao tác
            List<MedicinalStorageControl> medicinalStorageControlList = super.listAll(
                    SearchParamsBuilder.create().eq("storage_id", medicinalStockRecord.getStorageId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStorageControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy chi tiết nhập kho cần kiểm soát tồn kho[Chi tiếtID=" + medicinalStockRecord.getStorageId() + "]. ");
            }
            MedicinalStorageControl medicinalStorageControl = medicinalStorageControlList.get(0);
            medicinalStorageControl.setUsedStock(medicinalStorageControl.getUsedStock().subtract(operationStock));            //Số lượng tồn kho đã sử dụng hiện tại cần được trừ trả lại
//            BigDecimal returnTodoFeeStock1 =medicinalStorageControl.getReturnTodoFeeStock()==null?new BigDecimal("0"):medicinalStorageControl.getReturnTodoFeeStock();
//            medicinalStorageControl.setReturnTodoFeeStock(returnTodoFeeStock1.add(operationStock));
            medicinalStorageControl.setSurplusStock(medicinalStorageControl.getSurplusStock().add(operationStock));
            super.save(medicinalStorageControl);

            //Cập nhật lượng chiếm dụng trước của kiểm soát tổng
            List<MedicinalStockControl> medicinalStockControlList = this.medicinalStockControlService.listAll(
                    SearchParamsBuilder.create().eq("drug_stuff_id", medicinalStockRecord.getDrugStuffId()).build(), "");
            if (CollectionUtils.isEmpty(medicinalStockControlList))
            {
                throw new ServiceException("Kiểm soát tồn kho động thất bại, Không tìm thấy mặt hàng[ " + medicinalStockRecord.getDrugStuffName() + " ] của bản ghi kiểm soát tổng tồn kho. ");
            }
            MedicinalStockControl medicinalStockControl = medicinalStockControlList.get(0);
            medicinalStockControl.setUsedStock(medicinalStockControl.getUsedStock().subtract(operationStock));            //Số lượng tồn kho đã sử dụng hiện tại cần được trừ trả lại
            medicinalStockControl.setSurplusStock(medicinalStockControl.getSurplusStock().add(operationStock));
            this.medicinalStockControlService.save(medicinalStockControl);
        }
        //Xóachiếm dụngthao tácChi tiết(này lầnvật lýXóa)
        this.medicinalStockRecordService.batchDelete(medicinalStockRecordList);
    }


    //chiếm dụng trước: theoĐơn thuốcđếnkiểm soát, trướcTruy vấnvàvàgiải phóng, lạiLạichiếm dụng trước

    //chiếm dụng thực tế: thì có phát thuốc giờ, theoĐơn thuốcđếnkiểm soát,  trướcLạichiếm dụng trướcmộtĐưa, lạiĐưachiếm dụng trước của thay đổi là chiếm dụng thực tế

    //Trả thuốc: trướcTồn khotrảtrả lạiđi, khả dụngTồn khotrướckhôngrõgiải phóng,  v.v. Hoàn phí xong mới thực sự giải phóng

    //----------------------------
    /**
     * khophòngthậtthựcTồn kholượng=Tổng lượng nhập kho - Số lượng tồn kho đã sử dụng - Số lượng tồn kho báo hỏng
     * mởthuốckhả dụngTồn kholượng=Số lượng tồn kho còn khả dụng
     * Số lượng tồn kho đang bị chiếm dụng=Số lượng tồn kho đang bị chiếm dụng
     * Số lượng tồn kho báo hỏng=Số lượng tồn kho báo hỏng
     * ĐãTrả thuốcchờhoàn phí của thuốcchiếm dụngTồn kholượng=(Tổng lượng nhập kho - Số lượng tồn kho đã sử dụng - Số lượng tồn kho báo hỏng) - Số lượng tồn kho đang bị chiếm dụng - Số lượng tồn kho còn khả dụng
     *                                     thực tếTồn kho(lớn)
     */
}