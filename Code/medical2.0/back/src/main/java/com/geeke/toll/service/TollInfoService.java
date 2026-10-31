package com.geeke.toll.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geeke.admin.entity.User;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.sequence.service.SequenceService;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;

import com.geeke.cure.entity.InspectionCheck;
import com.geeke.cure.entity.InspectionCheckDetail;
import com.geeke.cure.entity.InspectionCheckInfo;
import com.geeke.cure.service.InspectionCheckDetailService;
import com.geeke.cure.service.InspectionCheckInfoService;
import com.geeke.cure.service.InspectionCheckService;
import com.geeke.medicareutils.config.MedicareConfigProperties;
import com.geeke.medicareutils.domain.respo.MdFeeDetail;
import com.geeke.medicareutils.service.MdInventoryService;
import com.geeke.medicareutils.service.MdPsnDataService;
import com.geeke.medicareutils.service.MdRegistrationService;
import com.geeke.member.entity.MemberManagement;
import com.geeke.member.service.MemberManagementDetailService;
import com.geeke.org.entity.ClinicOffice;
import com.geeke.org.entity.Company;
import com.geeke.org.service.CompanyService;
import com.geeke.outpatient.dao.RecipelDetailDao;
import com.geeke.outpatient.entity.*;
import com.geeke.outpatient.service.*;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.InventoryVerification;
import com.geeke.stock.entity.Stuff;
import com.geeke.stock.service.*;
import com.geeke.sys.entity.DictItem;
import com.geeke.toll.dao.TollAuditLogDao;
import com.geeke.toll.dao.TollInfoDao;
import com.geeke.toll.entity.*;
import com.geeke.toll.utils.BigdecimalConvert;
import com.geeke.treatment.entity.CostItem;
import com.geeke.treatment.entity.CostItemPackage;
import com.geeke.treatment.service.CostItemService;
import com.geeke.treatment.service.impl.CostItemPackageService;
import com.geeke.utils.*;

import lombok.RequiredArgsConstructor;
import com.geeke.utils.excel.ExcelExportBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.BeanUtils;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Quản lý thu phíService
 * @author lc
 * @version 2022-06-22
 */
 
@Service("tollInfoService")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TollInfoService extends CrudService<TollInfoDao, TollInfo>{
    private final PatientService patientService;

    @Lazy
    private final RecipelInfoService recipelInfoService;

    private final SequenceService sequenceService;

    @Lazy
    private final RegistrationService registrationService;

    private final TollDetailService tollDetailService;

    @Lazy
    private final MedicalRecordService medicalRecordService;

    private final RecipelDetailService recipelDetailService;

    private final StuffService stuffService;

    private final DrugService drugService;

    private final TollInfoDao tollInfoDao;

    private final CostItemService costItemService;

    private final InspectionCheckInfoService inspectionCheckInfoService;

    private final InspectionCheckDetailService inspectionCheckDetailService;

    private final InspectionCheckService inspectionCheckService;

    private final CostItemPackageService costItemPackageService;

    private final MemberManagementDetailService memberManagementDetailService;

    private final InventoryVerificationService inventoryVerificationService;

    private final SupplierStockService supplierStockService;

    private final DispensingService dispensingService;

    private final MedicinalStorageControlService medicinalStorageControlService;

    private final SerialNoUtils serialNoUtils;

    private final CompanyService companyService;

    private final RecipelDetailDao recipelDetailDao;

    private final MedicareConfigProperties medicareConfigProperties;

    private final PatientMdDataService patientMdDataService;

    private final MdRegistrationService mdRegistrationService;

    private final MdInventoryService mdInventoryService;

    private final MdPsnDataService mdPsnDataService;

    private final TollAuditLogDao tollAuditLogDao;

    /**
     * Sẽ  SearchParams Chuyển đổi thành PageRequest
     * đơn giảnhóabáo cáoTruy vấnPhương thứcTrongtrùng lặpmã
     */
    private PageRequest toPageRequest(SearchParams searchParams) {
        return new PageRequest(searchParams.getOffset(), searchParams.getLimit(),
                searchParams.getParams(), searchParams.getOrderby());
    }

    @Override
    @Transactional(readOnly = false)
    public TollInfo save(TollInfo tollInfo) {
        // Khi thêm mới, Xử lý trường tự động đánh số
        tollInfo.setCreateDate(new Date());
        if (StringUtils.isBlank(tollInfo.getTollNumber())){
            String sn = sequenceService.generate(SessionUtils.getUser().getCompanyId(), "toll_code", tollInfo);
            tollInfo.setTollNumber(sn);
        }

        TollInfo tollInfoTemp = super.save(tollInfo);
        return tollInfoTemp;
    }
    

    @Transactional(readOnly = false)
    public void saveToll(TollEvt tollEvt,String type) {
        Company company = SessionUtils.getUser().getCompany();
        String seltId="";//BHYTQuyết toánid thangphísaulưu vàoĐăng ký khámbảng
        //Đang tiến hành kiểm kê giờKhông thểtiến hành dòngThu phí/Hoàn phí
        validateInventoryNotInProgress(company.getId(), tollEvt.getTollInfo().getAmountStatus().getValue());
        TollInfo tollInfo = tollEvt.getTollInfo();
        List<RecipelInfoEvt> recipelInfoEvts = tollEvt.getRecipelInfos();
        TollInfo tollSave = null;
        Registration registration = null;
        if (!CollectionUtils.isEmpty(recipelInfoEvts)) {
            //Kiểm tratrùng lặpđề xuấtgiao
            List<String> InRecipeIds = recipelInfoEvts.stream().map(recipelInfoEvt -> {
                return recipelInfoEvt.getRecipelInfo().getId();
            }).collect(Collectors.toList());
            List<TollInfo> inRecipeIds = this.getInRecipeIds(InRecipeIds,tollInfo.getAmountStatus().getValue());
            if (!CollectionUtils.isEmpty(inRecipeIds)) {
                throw new ServiceException("Dữ liệuĐã tồn tạiVui lòngđừngtrùng lặpđề xuấtgiao");
            }
            Patient patient = tollEvt.getTollInfo().getPatient();
            if (StringUtils.isNotBlank(patient.getId())) {
                patient = patientService.get(patient.getId());
            }
            //Thu phí
            if (BizConstants.AMOUNT_STATUS_PAID.equals(tollInfo.getAmountStatus().getValue())) {
                if (BizConstants.TOLL_TYPE_REGISTRATION.equals(tollInfo.getTollType().getValue())){
                    //Thu phí bán lẻ
                    registration = processRetailCharge(tollEvt, tollInfo, patient, recipelInfoEvts);
                }else {
                    registration = processNormalCharge(tollEvt, tollInfo, patient, recipelInfoEvts);
                }
            }
            for (RecipelInfoEvt recipelInfo:tollEvt.getRecipelInfos()){
                List<RecipelDetail> entity = recipelDetailService.getRecipelDetail(recipelInfo.getRecipelInfo().getId());
                for (RecipelDetail e:entity){
                    BigDecimal c = BigDecimal.valueOf(tollEvt.getTollInfo().getDiscount());
                    e.setActualPayment(e.getAllFee().multiply(c));
                    recipelDetailDao.updateActualPayment(e.getActualPayment(),e.getId());
                }
            }
            //Hoàn phí
            if (BizConstants.AMOUNT_STATUS_REFUNDED.equals(tollInfo.getAmountStatus().getValue())) {
                processRefund(tollEvt, tollInfo, recipelInfoEvts);
            }
            //Đăng ký khámphương thức thanh toán, Trạng thái thu phí
            if (null == registration) {
                registration = registrationService.get(tollInfo.getMedical().getRegistration().getId());

            }
            //phương thức thanh toánTổng dùng
            if (StringUtils.isNullOrEmpty(registration.getPayType())) {
                registration.setPayType(tollInfo.getPaymentType());
            }
            //Không cóđăng kýrõcách  hoặc bán lẻ thìMặc định có nguyênCáiĐăng ký khámđơn
            if(StringUtils.isNullOrEmpty(type) || BizConstants.TOLL_TYPE_INFUSION.equals(tollInfo.getTollType().getValue()))
            {
                registration.setChargeStatus(tollEvt.getChargeStatus());
            }
            //Nếu khôngchỉ có đơnCáiĐơn thuốc
            if (BizConstants.AMOUNT_STATUS_PAID.equals(tollInfo.getAmountStatus().getValue())) {
                registration.setChargeDate(new Date());
            }else if (BizConstants.AMOUNT_STATUS_REFUNDED.equals(tollInfo.getAmountStatus().getValue())) {
                registration.setretreatsDate(new Date());
            }
            registration.setSetlId(seltId);
            registrationService.save(registration);
        }
    }

    //hạng mục khám chữa bệnhhoàn phí, NếuTồn tạiVật tưThu phíthìcầntrảcòn
    private void returnPremium(RecipelInfo recipelInfo) {
        medicinalStorageControlService.materialRefund(recipelInfo);
        //Sửaphát thuốcThông tinbảng
        dispensingService.updateDelFlag(recipelInfo.getId());
    }

    //hạng mục khám chữa bệnhtiến hành dòngphí phụ thêmVật tưkhấu trừ
    private void deduction(RecipelInfo recipelInfo) {
        medicinalStorageControlService.okOccupyStock(recipelInfo.getRegistration().getId(),recipelInfo.getId());
    }

    /**
     * Kiểm trakiểm kêTrạng thái: kiểm kê trong cấmdừngThu phí/hoàn phíthao tác
     */
    private void validateInventoryNotInProgress(String companyId, String amountStatus) {
        List<InventoryVerification> inventoryVerifications = inventoryVerificationService.getByCompanyId(companyId);
        if (!CollectionUtils.isEmpty(inventoryVerifications)) {
            String msg = BizConstants.AMOUNT_STATUS_PAID.equals(amountStatus)
                    ? "Đang tiến hành kiểm kê, không thể tiến hành dòngThu phíthao tác!" : "Đang tiến hành kiểm kê, không thể tiến hành dònghoàn phíthao tác!";
            throw new ServiceException(msg);
        }
    }

    /**
     * Xử lýThu phí bán lẻ: tạoBệnh nhân, Đăng ký khám, ca bệnh, Đơn thuốc, chiếm dụng trướcTồn kho
     */
    private Registration processRetailCharge(TollEvt tollEvt, TollInfo tollInfo, Patient patient, List<RecipelInfoEvt> recipelInfoEvts) {
        Patient patient1 = patientService.get(patient.getId());
        if (ObjectUtils.isEmpty(patient1)) {
            patient = patientService.save(patient);
        } else {
            patient = patient1;
        }
        Registration registration = this.addRetailRegistration(patient.getCompany(), patient);
        MedicalRecord medicalRecord = this.addMedicalRecord(registration);
        for (RecipelInfoEvt recipelInfoEvt : recipelInfoEvts) {
            RecipelInfo recipelInfo = recipelInfoEvt.getRecipelInfo();
            recipelInfo.setIsDispension("0");
            recipelInfo.setIsPay("1");
            recipelInfo.setPayDate(new Date());
            recipelInfo.setChargeStatus(1);
        }
        RecipelInfo retail = this.addRetailRecipelInfo(recipelInfoEvts, medicalRecord);
        tollInfo.setPatient(patient);
        tollInfo.setRecipel(retail);
        tollInfo.setMedical(medicalRecord);
        this.save(tollInfo);
        medicinalStorageControlService.preOccupyStock(retail);
        if ("true".equals(medicareConfigProperties.getCheck())) {
            mdInventoryService.updateInventoryList_3502A(tollEvt.getRecipelInfos().get(0));
        }
        return registration;
    }

    /**
     * Xử lýbình thườngThu phí(không phảibán lẻ): Lưu đơn thuốc, TạoThu phíBản ghi, BHYTTải lên, Trừ thẻ trải nghiệm, Xét nghiệm và kiểm tra
     */
    private Registration processNormalCharge(TollEvt tollEvt, TollInfo tollInfo, Patient patient, List<RecipelInfoEvt> recipelInfoEvts) {
        Registration registration = null;
        tollInfo.setPatient(patient);
        JSONArray array = new JSONArray();
        for (RecipelInfoEvt recipelInfoEvt : recipelInfoEvts) {
            RecipelInfo recipelInfo = recipelInfoService.get(recipelInfoEvt.getRecipelInfo().getId());
            String value = tollInfo.getAmountStatus().getValue();
            recipelInfo.setIsPay(value.substring(value.length() - 1));
            recipelInfo.setPayDate(new Date());
            recipelInfo.setIsDispension("0");
            recipelInfo.setChargeStatus(1);
            recipelInfo.setAmountReceivedTotal(tollEvt.getTollInfo().getAmountReceived());
            recipelInfoService.save(recipelInfo);
            TollInfo newTollInfo = new TollInfo();
            BeanUtils.copyProperties(tollInfo, newTollInfo);
            newTollInfo.setMedical(medicalRecordService.get(tollInfo.getMedical().getId()));
            newTollInfo.setRecipel(recipelInfo);
            newTollInfo.setAmountReceivable(recipelInfo.getFee());
            newTollInfo.setAmountReceived(tollInfo.getAmountReceived());
            newTollInfo.setAmountDiscounted(newTollInfo.getAmountReceivable().subtract(newTollInfo.getAmountReceived()));
            // TheoĐơn thuốcTênánh xạLoại thu phí
            String tollTypeName = recipelInfo.getName().substring(0, 2);
            DictItem tollType = newTollInfo.getTollType();
            tollType.setValue(getTollTypeByTypeName(tollTypeName));
            if ("诊疗".equals(tollTypeName) && newTollInfo.getAmountReceivable() != null && newTollInfo.getAmountReceived() != null && newTollInfo.getAmountReceivable().compareTo(newTollInfo.getAmountReceived()) != 0) {
                newTollInfo.setRemarks("Trừ thẻ trải nghiệm");
            }
            newTollInfo.setTollType(tollType);
            this.save(newTollInfo);
            // BHYTThu phíThông tinTải lên
            if ("true".equals(medicareConfigProperties.getCheck())) {
                registration = buildAndUploadMedicareData(tollInfo, newTollInfo, recipelInfoEvt, array);
            }
            // Trừ thẻ trải nghiệm
            MemberManagement memberManagement = tollEvt.getMemberManagement();
            if (!ObjectUtils.isEmpty(memberManagement) && "诊疗".equals(tollTypeName) && !CollectionUtils.isEmpty(memberManagement.getMemberManagementDetails())) {
                memberManagementDetailService.updateUseNumber(memberManagement);
            }
            // Xét nghiệm và kiểm trahạng mụcnhập kho
            if ("诊疗".equals(tollTypeName)) {
                addInspectionCheck(recipelInfo);
                deduction(recipelInfo);
            }
        }
        if (medicareConfigProperties.getCheck().equals("true") && registration != null) {
            mdRegistrationService.upRegistrationInfo_2203(registration);
        }
        return registration;
    }

    /**
     * Ánh xạ giá trị loại thu phí theo hai chữ đầu của tên đơn thuốc
     */
    private String getTollTypeByTypeName(String typeName) {
        switch (typeName) {
            case "Thuốc tây": return BizConstants.TOLL_TYPE_WESTERN;
            case "Thuốc đông y": return BizConstants.TOLL_TYPE_CHINESE;
            case "Truyền dịch": return BizConstants.TOLL_TYPE_INFUSION;
            case "诊疗": return BizConstants.TOLL_TYPE_TREATMENT;
            default: return "";
        }
    }

    /**
     * xây dựngBHYTThu phíDữ liệuvàTải lên
     */
    private Registration buildAndUploadMedicareData(TollInfo tollInfo, TollInfo tollSave, RecipelInfoEvt recipelInfoEvt, JSONArray array) {
        Registration registration = registrationService.get(tollInfo.getMedical().getRegistration().getId());
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        ClinicOffice clinicOffice = registration.getClinicOffice();
        User doctor = registration.getDoctor();
        JSONObject data = new JSONObject();
        data.put("feedetl_sn", tollSave.getId());
        data.put("mdtrt_id", registration.getMdtrtId());
        data.put("psn_no", patientMdData.getPsnNo());
        data.put("chrg_bchno", tollSave.getTollNumber());
        if (Objects.nonNull(tollSave.getRecipel())) {
            data.put("rxno", tollSave.getRecipel().getCode());
            data.put("rxd_circ_flag", "0");
        }
        data.put("fee_ocur_time", tollSave.getRecipel().getCreateDate());
        data.put("med_list_codg", "");
        data.put("medins_list_codg", "");
        data.put("det_item_fee_sumamt", recipelInfoEvt.getRecipelInfo().getFee());
        data.put("cnt", recipelInfoEvt.getRecipelDetailEvtList().get(0).getTotal());
        data.put("pric", recipelInfoEvt.getRecipelDetailEvtList().get(0).getUnitPrice());
        data.put("sin_dos_dscr", "");
        data.put("used_frqu_dscr", "");
        data.put("prd_days", recipelInfoEvt.getRecipelDetailEvtList().get(0).getDays().getValue());
        data.put("medc_way_dscr", "");
        data.put("bilg_dept_codg", clinicOffice.getCode());
        data.put("bilg_dept_name", clinicOffice.getName());
        data.put("bilg_dr_codg", doctor.getUserExt().getPracPsnCode());
        data.put("bilg_dr_name", doctor.getName());
        data.put("acord_dept_codg", clinicOffice.getCode());
        data.put("acord_dept_name", clinicOffice.getName());
        data.put("orders_dr_code", doctor.getUserExt().getPracPsnCode());
        data.put("orders_dr_name", doctor.getName());
        data.put("hosp_appr_flag", "1");
        data.put("tcmdrug_used_way", "");
        data.put("etip_flag", "");
        data.put("etip_hosp_code", "");
        data.put("dscg_tkdrug_flag", "");
        data.put("matn_fee_flag", "");
        data.put("comb_no", "");
        data.put("expContent", "");
        array.add(data);
        return registration;
    }

    /**
     * Xử lýhoàn phílogic: Kiểm tra, Cập nhật trạng thái đơn thuốc, Tạohoàn phíBản ghi, trảcònTồn kho, BHYThoàn phí
     */
    private void processRefund(TollEvt tollEvt, TollInfo tollInfo, List<RecipelInfoEvt> recipelInfoEvts) {
        for (RecipelInfoEvt recipelInfoEvt : recipelInfoEvts) {
            RecipelInfo recipelInfo = recipelInfoService.get(recipelInfoEvt.getRecipelInfo().getId());
            String value = tollInfo.getAmountStatus().getValue();
            // Kiểm traCó hay khôngĐãphát thuốc
            if ("1".equals(recipelInfo.getIsDispension())) {
                throw new ServiceException("đóNgười dùngĐãphát thuốc, Không thể hoàn phí!");
            }
            // khámtrị liệuĐơn thuốchoàn phíKiểm tra
            if (BizConstants.RECIPEL_TYPE_OTHER.equals(recipelInfo.getRecipelType().getValue())) {
                validateRefundForTreatmentRecipel(recipelInfo);
            }
            // Cập nhật trạng thái đơn thuốc
            recipelInfo.setIsPay(value.substring(value.length() - 1));
            recipelInfo.setChargeStatus(-1);
            recipelInfo.setRetreatDate(new Date());
            recipelInfoService.save(recipelInfo);
            // Tạohoàn phíBản ghi
            List<TollInfo> tollInfos = this.getByRecipeId(recipelInfo.getId());
            for (TollInfo info : tollInfos) {
                TollInfo refund = new TollInfo();
                TollInfo tollInfo1 = new TollInfo();
                BeanUtils.copyProperties(info, refund);
                BeanUtils.copyProperties(info, tollInfo1);
                refund.setId("");
                tollInfo1.setReturnType(1);
                refund.setAmountStatus(tollInfo.getAmountStatus());
                refund.setReturnType(1);
                TollInfo refundSaved = this.save(refund);
                // Audit hủy/hoàn hóa đơn (blocker 6): ghi 1 dòng toll_audit_log
                // cho mỗi phiếu hoàn vừa tạo, cùng transaction với lần hoàn
                // phí (hoàn phí lỗi thì rollback cả log). Không đổi logic thu
                // phí, không đổi số tiền: chỉ đọc lại các trường của phiếu
                // hoàn vừa lưu. Người thao tác + thời điểm do preInsert()
                // ghi từ session (create_by dạng "Tên(loginName)").
                TollAuditLog auditLog = new TollAuditLog();
                auditLog.setCompany(refundSaved.getCompany() != null
                        ? refundSaved.getCompany()
                        : SessionUtils.getUser().getCompany());
                auditLog.setTollId(refundSaved.getId());
                auditLog.setRecipelId(recipelInfo.getId());
                if (tollEvt.getTollInfo().getPatient() != null) {
                    auditLog.setPatientId(tollEvt.getTollInfo().getPatient().getId());
                }
                auditLog.setAction("REFUND");
                auditLog.setAmountReceivable(refundSaved.getAmountReceivable());
                auditLog.setAmountReceived(refundSaved.getAmountReceived());
                auditLog.setPaymentType(refundSaved.getPaymentType() != null
                        ? refundSaved.getPaymentType().getValue() : null);
                auditLog.setAmountStatus(tollInfo.getAmountStatus() != null
                        ? tollInfo.getAmountStatus().getValue() : null);
                auditLog.setRemarks("Hoàn phí đơn thuốc, phiếu gốc " + info.getId());
                auditLog.preInsert();
                tollAuditLogDao.insertAudit(auditLog);
                this.save(tollInfo1);
            }
            // trảcònVật tư
            if (BizConstants.RECIPEL_TYPE_OTHER.equals(recipelInfo.getRecipelType().getValue())) {
                returnPremium(recipelInfo);
            }
            // Trả thuốctrảVật tưthao tác
            // NPE đã gặp khi hoàn phí đơn có dispensionStatus NULL (đơn tạo
            // qua API không đặt trạng thái phát thuốc): Integer null bị unbox
            // ở phép == nên cả giao dịch hoàn phí rollback. NULL nghĩa là
            // chưa phát thuốc, xử lý như 0 (hủy chiếm dụng tồn kho); chỉ
            // -1 mới đi nhánh trả thuốc. Đơn UI tạo sẵn có 0/1/-1 nên hành
            // vi với dữ liệu cũ không đổi.
            Integer dispensionStatus = recipelInfo.getDispensionStatus();
            if ((dispensionStatus == null || dispensionStatus == 0) && !BizConstants.RECIPEL_TYPE_OTHER.equals(recipelInfo.getRecipelType().getValue())) {
                medicinalStorageControlService.cancelOccupy(recipelInfo);
            } else if (dispensionStatus != null && dispensionStatus == -1 && !BizConstants.RECIPEL_TYPE_OTHER.equals(recipelInfo.getRecipelType().getValue())) {
                medicinalStorageControlService.goBackFee(recipelInfo);
            }
        }
        // BHYThoàn phí
        if ("true".equals(medicareConfigProperties.getCheck())) {
            Registration registration = registrationService.get(tollInfo.getMedical().getRegistration().getId());
            mdPsnDataService.getAndSetPsnData(registration);
            PatientMdData psnData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
            mdRegistrationService.revokeOutpatientSettlement_2208(registration.getSetlId(), psnData.getPsnNo(), registration.getMdtrtId());
        }
    }

    /**
     * khámtrị liệuĐơn thuốchoàn phíKiểm tra: Kiểm trahành nghề dòngTrạng thái và Xét nghiệm và kiểm trabáocáo
     */
    private void validateRefundForTreatmentRecipel(RecipelInfo recipelInfo) {
        List<RecipelDetail> recipelDetails = recipelDetailService.getByRecipelInfoId(recipelInfo.getId());
        for (RecipelDetail recipelDetail : recipelDetails) {
            if (recipelDetail.getExecutions().compareTo(BigDecimal.valueOf(0)) != 0) {
                throw new ServiceException("Hạng mục khám chữa bệnh đã thực hiện khấu trừ, không thể hoàn phí!");
            }
        }
        List<InspectionCheck> inspectionChecks = inspectionCheckService.getByRecipelInfoId(recipelInfo.getId());
        for (InspectionCheck inspectionCheck : inspectionChecks) {
            if ("1".equals(inspectionCheck.getStatus())) {
                throw new ServiceException("Xét nghiệm và kiểm tra đã có báo cáo, không thể hoàn phí!");
            }
        }
        inspectionCheckService.deleteByRecipelInfoId(recipelInfo.getId());
    }

    //LưuXét nghiệmkiểmđo
    @Transactional(readOnly = false)
    public void addInspectionCheck(RecipelInfo recipelInfo) {
        //QuaĐơn thuốcid, điLấyhạng mục khám chữa bệnh
        List<CostItem> costItems = costItemService.getByRecipelInfo(recipelInfo);
        //Kiểm trahạng mục khám chữa bệnhCó hay không là Xét nghiệm và kiểm tra
        for (CostItem costItem : costItems) {
            String itemType = costItem.getItemType().getValue();
            if (BizConstants.TREATMENT_ITEM_TYPE_LAB.equals(itemType) || BizConstants.TREATMENT_ITEM_TYPE_EXAM.equals(itemType)) {
                // 0-Xét nghiệm 1-Kiểm tra
                String type = BizConstants.TREATMENT_ITEM_TYPE_LAB.equals(itemType) ? "0" : "1";
                saveInspectionCheck(recipelInfo, costItem, type);
            }
        }
    }

    /**
     * LưuXét nghiệm và kiểm traBản ghi(hợpvàXét nghiệm và Kiểm tra của chungTổng logic)
     */
    private void saveInspectionCheck(RecipelInfo recipelInfo, CostItem costItem, String type) {
        //Theođăng kýThông tinLấyThông tin bệnh nhân
        Registration registration = registrationService.get(recipelInfo.getRegistration().getId());
        Patient patient = patientService.get(registration.getPatientId().getId());

        InspectionCheck inspectionCheck = new InspectionCheck();
        inspectionCheck.setRecipelInfo(recipelInfo);
        inspectionCheck.setCompany(recipelInfo.getCompany());
        inspectionCheck.setCostItem(costItem);
        inspectionCheck.setName(costItem.getItemName());
        inspectionCheck.setStatus("0");
        inspectionCheck.setType(type);
        inspectionCheck.setRegistration(recipelInfo.getRegistration());
        inspectionCheck.setPatient(patient);
        inspectionCheck.setPatientName(patient.getName());
        inspectionCheck.setSex(patient.getGender().getValue());
        inspectionCheck.setPhone(patient.getPhone());
        inspectionCheck.setCompleteBy(registration.getDoctor().getName());
        inspectionCheck.setCompleteDate(registration.getReceptionEndDate());
        inspectionCheck.setId(IdGen.uuid());

        // Xét nghiệmLoạiCầnĐặt recipelDetail
        if ("0".equals(type)) {
            inspectionCheck.setRecipelDetail(costItem.getRecipelDetail());
        }

        inspectionCheckService.allSave(inspectionCheck);

        //chènBảng chínhsau, CầnchènĐếnChi tiết và Chi tiếtbảng trong 
        InspectionCheckInfo inspectionCheckInfo = new InspectionCheckInfo();
        inspectionCheckInfo.setId(IdGen.uuid());
        inspectionCheckInfo.setCompany(recipelInfo.getCompany());
        inspectionCheckInfo.setPatient(patient);
        inspectionCheckInfo.setInspectionCheck(inspectionCheck);
        JSONObject userObj = com.geeke.sys.utils.SessionUtils.getUserJson();
        inspectionCheckInfo.setCreateBy(userObj.getString("name"));
        inspectionCheckInfo.setUpdateBy(userObj.getString("name"));
        inspectionCheckInfo.setCreateDate(new Date());
        inspectionCheckInfo.setUpdateDate(new Date());
        inspectionCheckInfoService.allSave(inspectionCheckInfo);

        //chènChi tiếtbảng
        List<CostItemPackage> all = costItemPackageService.getAll(costItem.getId());
        if (!CollectionUtils.isEmpty(all)) {
            int seq = 0;
            for (CostItemPackage costItemPackage : all) {
                seq++;
                InspectionCheckDetail inspectionCheckDetail = new InspectionCheckDetail();
                inspectionCheckDetail.setId(IdGen.uuid());
                inspectionCheckDetail.setCompany(recipelInfo.getCompany());
                inspectionCheckDetail.setInspectionCheckInfo(inspectionCheckInfo);
                inspectionCheckDetail.setSeq(seq);
                CostItem costItem1 = new CostItem();
                String costItemId = costItemPackage.getCostItemId();
                if (costItemId != null) {
                    costItem1.setId(costItemId);
                } else {
                    costItem1.setId(costItemPackage.getCostItemPkgId());
                }
                inspectionCheckDetail.setCostItem(costItem1);
                inspectionCheckDetail.setCreateBy(userObj.getString("name"));
                inspectionCheckDetail.setUpdateBy(userObj.getString("name"));
                inspectionCheckDetail.setCreateDate(new Date());
                inspectionCheckDetail.setUpdateDate(new Date());
                inspectionCheckDetailService.allSave(inspectionCheckDetail);
            }
        }
    }

    private MedicalRecord addMedicalRecord(Registration registration) {
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setCompany(registration.getCompany());
        medicalRecord.setRegistration(registration);
        //nàyCáiTrườngbắt buộc
        medicalRecord.setDoctor(SessionUtils.getUser());
        medicalRecord.setPatientTell("bán lẻĐăng ký khám");
        medicalRecord.setDiagnose("bán lẻĐăng ký khám");
        return medicalRecordService.save(medicalRecord);
    }

    private List<Patient> getPatientByNameAndPhone(Patient patient) {
        Company company = SessionUtils.getUser().getCompany();
        List<Parameter> parameters = SearchParamsBuilder.create()
                .eq("name", patient.getName())
                .eq("company_id", company.getId())
                .eq("phone", patient.getPhone())
                .build();
        return patientService.listAll(parameters,"");
    }


    private BigDecimal getAmountReceived(RecipelInfo recipelInfo, TollInfo tollInfo){
        if (tollInfo.getDiscount() == 0) {
            return recipelInfo.getFee();
        }
        return recipelInfo.getFee().multiply(new BigDecimal(tollInfo.getDiscount()));
    }

    private Registration addRetailRegistration(Company company,Patient patient) {
            Registration registration = new Registration();
            registration.setCompany(company);
            registration.setChargeDate(new Date());
            registration.setPatientId(patient);
            registration.setDispensingStatus("0");
            DictItem dictItem = new DictItem();
            //Đãkhám bệnh
            dictItem.setValue(BizConstants.REG_STATUS_VISITED);
            registration.setStatus(dictItem);
            //Đã thu phí
            registration.setChargeStatus("2");
            return registrationService.save(registration);
    }

    private RecipelInfo addRetailRecipelInfo(List<RecipelInfoEvt> recipelInfos,MedicalRecord medicalRecord) {
        RecipelInfo save = null;
        DictItem dictItem = new DictItem();
        dictItem.setValue(BizConstants.RECIPEL_TYPE_EXTERNAL);
        //bán lẻĐơn thuốcMặc địnhchỉ có mộtCáiĐơn thuốc
        for (RecipelInfoEvt recipelInfoEvt : recipelInfos) {
            RecipelInfo recipelInfo = recipelInfoEvt.getRecipelInfo();
            recipelInfo.setRecipelType(dictItem);
            //recipelInfo.setMedicalRecord(medicalRecord);
            recipelInfo.setRegistration(medicalRecord.getRegistration());
            recipelInfo.setIsFollowUp(0);
            String recipelNo = serialNoUtils.generateSerialNo(org.apache.commons.lang3.StringUtils.EMPTY);
            recipelInfo.setCode(recipelNo);
            save = recipelInfoService.save(recipelInfo);
            //ThêmĐơn thuốcChi tiết
            for (RecipelDetail recipelDetail : recipelInfoEvt.getRecipelDetailEvtList()) {
                recipelDetail.setRecipelInfo(recipelInfo);
                recipelDetail.setCompany(recipelInfo.getCompany());
                //mụctrướcchỉkiểm traxem xétThuốc
                DrugStuffEvt drugStuffEvt = new DrugStuffEvt();
                if(Objects.equals("4",recipelDetail.getStuffType())){
                    Stuff stuff = stuffService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                    drugStuffEvt.setDrugStuffId(stuff.getId());
                    drugStuffEvt.setName(stuff.getName());
                    drugStuffEvt.setPrice(stuff.getPriceOutSell());
                    drugStuffEvt.setPreparationUnit(stuff.getMinUnit());
                    drugStuffEvt.setPack(stuff.getPackUnit());
                    drugStuffEvt.setStuff(stuff);
                }else {
                    Drug drug = drugService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                    drugStuffEvt.setDrugStuffId(drug.getId());
                    drugStuffEvt.setName(drug.getGoodsName());
                    drugStuffEvt.setPrice(drug.getPrice());
                    drugStuffEvt.setDosisUnit(drug.getDosisUnit());
                    drugStuffEvt.setPreparationUnit(drug.getPreparationUnit());
                    drugStuffEvt.setPack(drug.getPack());
                    drugStuffEvt.setDrug(drug);
                }
                recipelDetail.setDrugStuffId(drugStuffEvt);
                recipelDetail.setId("");
                recipelDetailService.save(recipelDetail);
            }
        }
        return save;
    }

    public List<TollInfo> getInRecipeIds(List<String> recipelInfoIds,String amountStatus){
        List<Parameter> parameters = SearchParamsBuilder.create()
                .in("recipel_id", recipelInfoIds)
                .eq("amount_status", amountStatus)
                .build();
        return this.listAll(parameters,"");
    }

    public List<TollInfo> getByRecipeId(String recipelInfoId){
        List<Parameter> parameters = SearchParamsBuilder.create()
                .eq("recipel_id", recipelInfoId)
                .build();
        return this.listAll(parameters,"");
    }

    public List<TollInfo> getByMedicalId(String medicalId) {
        List<Parameter> parameters = SearchParamsBuilder.create()
                .eq("medical_id", medicalId)
                .build();
        return this.listAll(parameters, "");
    }

    public ResponseEntity<JSONObject> tollTotalForm(SearchParams searchParams) {
        // Tham số đầu vàocolumnNameĐịnh dạng: recipelInfo.recipel_type|costItem.item_type
        PageRequest pageRequest = toPageRequest(searchParams);
        Page<TollInfo> page = paginate(
            () -> this.dao.formTollCount(pageRequest),
            () -> this.dao.tollTotalForm(pageRequest)
        );
        TollVo tollVo = this.getTotalTollForSearch(pageRequest);
        tollVo.setPage(page);
        return ResponseEntity.ok(ResultUtil.successJson(tollVo));
    }

    public ResponseEntity<JSONObject> orgtolldetail(SearchParams searchParams) {
        // Tham số đầu vàocolumnNameĐịnh dạng: recipelInfo.recipel_type|costItem.item_type
        PageRequest pageRequest = toPageRequest(searchParams);
        Page<TollInfo> page = paginate(
            () -> this.dao.formTollCount(pageRequest),
            () -> this.dao.tollTotalForm(pageRequest)
        );
        TollVo tollVo = this.getTotalTollForSearch(pageRequest);
        tollVo.setPage(page);
        return ResponseEntity.ok(ResultUtil.successJson(tollVo));
    }

    public ResponseEntity<JSONObject> tollDetailForm(SearchParams searchParams) {
        // Tham số đầu vàocolumnNameĐịnh dạng: toll_type|patient_id|department.id|user.id|recipelInfo.recipel_type in ["_0","_1"]
        PageRequest pageRequest = toPageRequest(searchParams);
        Page<TollInfo> page = paginate(
            () -> this.dao.formDetailCount(pageRequest),
            () -> {
                List<TollInfo> list = this.dao.tollDetailForm(pageRequest);
                for (TollInfo i : list) {
                    if (i.getDoctor() != null && i.getDoctor().getDepartment() != null) {
                        i.getDoctor().getDepartment().setName(i.getDepartment());
                    }
                }
                return list;
            }
        );
        TollVo tollVo = this.dao.tollDetailAmountReceivedAble(pageRequest);
        tollVo.setPage(page);
        return ResponseEntity.ok(ResultUtil.successJson(tollVo));
    }


    public ResponseEntity<JSONObject> orgtollDetailForm(SearchParams searchParams) {
        // Tham số đầu vàocolumnNameĐịnh dạng: toll_type|patient_id|department.id|user.id|recipelInfo.recipel_type in ["_0","_1"]
        PageRequest pageRequest = toPageRequest(searchParams);
        Page<TollInfo> page = paginate(
            () -> this.dao.formDetailCount(pageRequest),
            () -> this.dao.tollDetailForm(pageRequest)
        );
        TollVo tollVo = this.dao.tollDetailAmountReceivedAble(pageRequest);
        tollVo.setPage(page);
        return ResponseEntity.ok(ResultUtil.successJson(tollVo));
    }

    //LấyTruy vấntổngSố tiền(Tối ưu hóa: hợpvà là một lầnTruy vấn)
    private TollVo getTotalTollForSearch(PageRequest pageRequest) {
        return this.dao.tollTotalAllPayments(pageRequest);
    }

    public TollInfo getTollInfoByRegistrationId(String registrationId) {
        return tollInfoDao.getTollInfoByRegistrationId(registrationId);
    }

    public List<TollInfo> getCreateBy(List<Parameter> parameters, String orderby) {
        PageRequest pageRequest = new PageRequest(parameters, orderby);
        return this.dao.getCreateBy(pageRequest);
    }

    public Page<WorkLoad> getWorkload(List<Parameter> params,int offset,int limit, String orderby) {
        List<Parameter> parameters = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(offset, limit, params, orderby);
        List<WorkLoad> count = this.dao.countWorkload(pageRequest);
        int total=0;
        if(count!=null&&count.size()>0){
            total=count.size();
        }
        List<WorkLoad> workloads=null;
        if(total>0){
            workloads = this.dao.getWorkload(pageRequest);
            List<WorkLoad> workLoads = this.dao.getCount(pageRequest);

            // Đặtkhám bệnh lầnsố
            for (WorkLoad workLoad: workloads) {
                for (WorkLoad workload: workLoads){
                    if (workLoad.getName().equals(workload.getName())){
                        workLoad.setCount(workload.getCount());
                    }
                }
            }

            // hàng loạtTruy vấntất cảBác sĩ của lâm sàng giờChi phí(Tối ưu hóa N+1 Truy vấn)
            List<String> doctorNames = workloads.stream()
                    .map(WorkLoad::getName)
                    .distinct()
                    .collect(Collectors.toList());
            PageRequest batchPageRequest = new PageRequest(params, orderby);
            List<WorkLoad> allTemporaryCosts = this.dao.getTemporaryCostBatch(batchPageRequest, doctorNames);

            // theoBác sĩTên phútnhóm
            Map<String, List<WorkLoad>> costsByDoctor = allTemporaryCosts.stream()
                    .collect(Collectors.groupingBy(WorkLoad::getName));

            // Đặtlâm sàng giờChi phí
            for (WorkLoad workLoad: workloads) {
                List<WorkLoad> temporaryCosts = costsByDoctor.getOrDefault(workLoad.getName(), Collections.emptyList());
                for (WorkLoad temporaryCost : temporaryCosts) {
                    setCostByTypeName(workLoad, temporaryCost.getTypes(), temporaryCost.getTemporaryCost());
                }
            }
        }

        return new Page<>((long)total, workloads);
    }

    public WorkLoadStat getWorkLoadStat(List<Parameter> params,int offset,int limit, String orderby){
        List<Parameter> parameters = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(offset, limit, params, orderby);
        WorkLoadStat workLoadStat = this.dao.getWorkLoadStat(pageRequest);
        WorkLoadStat workLoads = null;
        workLoads = this.dao.getCounts(pageRequest);
        if (workLoads != null && workLoads.getCount() != null && !workLoads.getCount().equals("0")){
            workLoadStat.setCount(workLoads.getCount());
        }
        if(workLoadStat!=null){
            parameters.clear();
            parameters.addAll(params);
            PageRequest pageRequest1 = new PageRequest(parameters, orderby);
            List<WorkLoadStat> temporaryCostStat = this.dao.getTemporaryCostStat(pageRequest1);
            if (temporaryCostStat != null && temporaryCostStat.size() > 0) {
                for (WorkLoadStat workLoadStat1 : temporaryCostStat) {
                    setCostByTypeName(workLoadStat, workLoadStat1.getTypes(), workLoadStat1.getTemporaryCost());
                }
            }
        }
        return workLoadStat;
    }

    public Page<DrugSales> getDrugSales(List<Parameter> params, int offset, int limit, String orderby) {
        PageRequest pageRequest = new PageRequest(offset, limit, params, orderby);
        List<DrugSales> count = this.dao.countDrugSales(pageRequest);
        int total=0;
        if(count!=null&&count.size()>0){
            total=count.size();
        }
        List<DrugSales> drugSales=null;
        if(total>0){
            drugSales=this.dao.getDrugSales(pageRequest);
        }
        return new Page<>(total,drugSales);
    }

    public DrugSales getDrugSalesStat(List<Parameter> params, int offset, int limit, String orderby){
        PageRequest pageRequest = new PageRequest(offset, limit, params, orderby);
        DrugSales drugSalesStat = this.dao.getDrugSalesStat(pageRequest);
        return drugSalesStat;
    }

    /**
     * Thông tin nhập - xuất - tồn của thuốc
     * @param ypjxcRc
     * @return
     */
    public Page<Ypjxc> getypjxcmanagement(YpjxcRc ypjxcRc){
        String institution = companyService.getInstitution(ypjxcRc.getCompanyId());
        ypjxcRc.setJgid(institution);
        int total = tollInfoDao.countYpjxcManagement(ypjxcRc);
        List<Ypjxc> list2 = null;
        if(total > 0){
            list2 = tollInfoDao.getypjxcmanagement(ypjxcRc);
        }
        return new Page<>((long) total, list2);
    }

    /**
     * Tổng hợp số lượng và giá nhập - xuất - tồn của thuốc
     * @param ypjxcRc
     * @return
     */
    public Page<Ypjxc> getypjxcmanagementsums(YpjxcRc ypjxcRc){
        String institution = companyService.getInstitution(ypjxcRc.getCompanyId());
        ypjxcRc.setJgid(institution);
        int total = tollInfoDao.countYpjxcManagement(ypjxcRc);
        List<Ypjxc> list2 = null;
        if(total > 0){
            list2 = tollInfoDao.getypjxcmanagementsums(ypjxcRc);
        }
        return new Page<>((long) total, list2);
    }

    /**
     * LấyTình hình bán vật tư tiêu hao
     * @param stuffsalessummaryRc
     * @return
     */
    public Page<Stuffsalessummary> getStuffsalessummarylists(StuffsalessummaryRc stuffsalessummaryRc){
        String institution = companyService.getInstitution(stuffsalessummaryRc.getCompanyId());
        stuffsalessummaryRc.setJgid(institution);
        int total = tollInfoDao.countStuffSalesSummary(stuffsalessummaryRc);
        List<Stuffsalessummary> list2 = null;
        if(total > 0){
            list2 = tollInfoDao.getStuffsalessummarylist(stuffsalessummaryRc);
        }
        return new Page<>((long) total, list2);
    }

    public Page<Stuffsalessummary> getStuffsalessummarysumss(StuffsalessummaryRc stuffsalessummaryRc){
        String institution = companyService.getInstitution(stuffsalessummaryRc.getCompanyId());
        stuffsalessummaryRc.setJgid(institution);
        int total = tollInfoDao.countStuffSalesSummary(stuffsalessummaryRc);
        List<Stuffsalessummary> list2 = null;
        if(total > 0){
            list2 = tollInfoDao.getStuffsalessummarysums(stuffsalessummaryRc);
        }
        return new Page<>((long) total, list2);
    }

    /**
     * Thông tin nhập - xuất - tồn của vật tư
     * @param ypjxcRc
     * @return
     */
    public Page<Ypjxc> getcljxcmanagement(YpjxcRc ypjxcRc){
        String institution = companyService.getInstitution(ypjxcRc.getCompanyId());
        ypjxcRc.setJgid(institution);
        int total = tollInfoDao.countCljxcManagement(ypjxcRc);
        List<Ypjxc> list2 = null;
        if(total > 0){
            list2 = tollInfoDao.getcljxcmanagement(ypjxcRc);
        }
        return new Page<>((long) total, list2);
    }

    /**
     * Tổng hợp số lượng và giá nhập - xuất - tồn của vật tư
     * @param ypjxcRc
     * @return
     */
    public Page<Ypjxc> getcljxcmanagementsums(YpjxcRc ypjxcRc){
        String institution = companyService.getInstitution(ypjxcRc.getCompanyId());
        ypjxcRc.setJgid(institution);
        int total = tollInfoDao.countCljxcManagement(ypjxcRc);
        List<Ypjxc> list2 = null;
        if(total > 0){
            list2 = tollInfoDao.getcljxcmanagementsums(ypjxcRc);
        }
        return new Page<>((long) total, list2);
    }

    /**
     * Thống kê thông tin nhập kho thuốc và vật tư
     * @param ypjxcRc
     * @return
     */
    public Page<Ypclrkcx> getypclrkcxlist(YpjxcRc ypjxcRc){
        int total = tollInfoDao.countYpclrkcx(ypjxcRc);
        List<Ypclrkcx> list2 = null;
        if(total > 0){
            list2 = tollInfoDao.getypclrkcxlist(ypjxcRc);
        }
        return new Page<>((long) total, list2);
    }

    /**
     * Tổng hợp thông tin nhập kho thuốc và vật tư
     * @param ypjxcRc
     * @return
     */
    public Page<Ypclrkcx> getypclrkcxsums(YpjxcRc ypjxcRc){
        int total = tollInfoDao.countYpclrkcx(ypjxcRc);
        List<Ypclrkcx> list2 = null;
        if(total > 0){
            list2 = tollInfoDao.getypclrkcxsums(ypjxcRc);
        }
        return new Page<>((long) total, list2);
    }

    /**
     * Quản lý cơ sở-Thông tin nhập - xuất - tồn của thuốc
     */
    public Page<Ypjxc> getpharmaceuticalInventoryManagement(YpjxcRc ypjxcRc){
        ypjxcRc.setJgzt("1");
        return getypjxcmanagement(ypjxcRc);
    }
    /**
     * Quản lý cơ sở-Tổng hợp số lượng và giá nhập - xuất - tồn của thuốc
     */
    public Page<Ypjxc> getpharmaceuticalInventoryManagementsums(YpjxcRc ypjxcRc){
        ypjxcRc.setJgzt("1");
        return getypjxcmanagementsums(ypjxcRc);
    }

    public List<Ypjxc> getjglist(YpjxcRc ypjxcRc){
        return tollInfoDao.getjglist(ypjxcRc);
    }

    /**
     * Quản lý cơ sở-Thông tin nhập - xuất - tồn của vật tư
     */
    public Page<Ypjxc> getmaterialmanagement(YpjxcRc ypjxcRc){
        ypjxcRc.setJgzt("1");
        return getcljxcmanagement(ypjxcRc);
    }

    /**
     * Quản lý cơ sở-Tổng hợp số lượng và giá nhập - xuất - tồn của vật tư
     */
    public Page<Ypjxc> getmaterialmanagementsums(YpjxcRc ypjxcRc){
        ypjxcRc.setJgzt("1");
        return getcljxcmanagementsums(ypjxcRc);
    }

    /**
     * Quản lý cơ sở-Thống kê thông tin nhập kho thuốc và vật tư
     */
    public Page<Ypclrkcx> drugmaterialsstockmanagement(YpjxcRc ypjxcRc){
        ypjxcRc.setJgzt("1");
        return getypclrkcxlist(ypjxcRc);
    }

    /**
     * Quản lý cơ sở-Tổng hợp thông tin nhập kho thuốc và vật tư
     */
    public Page<Ypclrkcx> drugmaterialsstockmanagementsums(YpjxcRc ypjxcRc){
        ypjxcRc.setJgzt("1");
        return getypclrkcxsums(ypjxcRc);
    }

    /**
     * Quản lý cơ sở-LấyTình hình bán vật tư tiêu hao
     */
    public Page<Stuffsalessummary> getconsumablemarketstatistics(StuffsalessummaryRc stuffsalessummaryRc){
        stuffsalessummaryRc.setJgzt("1");
        return getStuffsalessummarylists(stuffsalessummaryRc);
    }

    /**
     * Quản lý cơ sở-Lấy tổng hợp giá và số lượng bán vật tư tiêu hao
     */
    public Page<Stuffsalessummary> getconsumablemarketstatisticssum(StuffsalessummaryRc stuffsalessummaryRc){
        stuffsalessummaryRc.setJgzt("1");
        return getStuffsalessummarysumss(stuffsalessummaryRc);
    }

    public Page<DrugSales> getdrugmarketstatistics(List<Parameter> params, int offset, int limit, String orderby) {
        return getDrugSales(params, offset, limit, orderby);
    }

    public DrugSales getdrugmarketstatisticsStat(List<Parameter> params, int offset, int limit, String orderby){
        return getDrugSalesStat(params, offset, limit, orderby);
    }


    public Page<WorkLoad> getdoctorDetailstatistics(List<Parameter> params,int offset,int limit, String orderby) {
        PageRequest pageRequest = new PageRequest(offset, limit, params, orderby);
        List<WorkLoad> count = this.dao.countWorkload(pageRequest);
        int total=0;
        if(count!=null&&count.size()>0){
            total=count.size();
        }
        List<WorkLoad> workloads=null;
        if(total>0){
            workloads = this.dao.getWorkload(pageRequest);

            for (WorkLoad workLoad:
                    workloads) {
                List<Parameter> doctorParams = new ArrayList<>(params);
                doctorParams.addAll(SearchParamsBuilder.create()
                        .eq("su.name", workLoad.getName())
                        .build());
                PageRequest pageRequest1 = new PageRequest(doctorParams, orderby);
                List<WorkLoad> temporaryCosts = this.dao.getTemporaryCost(pageRequest1);
                for (WorkLoad temporaryCost : temporaryCosts) {
                    setCostByTypeName(workLoad, temporaryCost.getTypes(), temporaryCost.getTemporaryCost());
                }
            }
        }

        return new Page<>((long)total, workloads);
    }

    public WorkLoadStat getdoctorDetailstatisticsStat(List<Parameter> params,int offset,int limit, String orderby){
        List<Parameter> parameters = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(offset, limit, params, orderby);
        List<WorkLoadStat> workLoadStat = this.dao.getWorkLoadStats(pageRequest);
        WorkLoadStat workLoadStat2 = new WorkLoadStat();
        if(!CollectionUtils.isEmpty(workLoadStat)){

            BigDecimal count = new BigDecimal("0");
            BigDecimal grossAmount = new BigDecimal("0");
            BigDecimal registrationCost = new BigDecimal("0");
            for (WorkLoadStat loadStat : workLoadStat) {
                count = count.add(new BigDecimal(loadStat.getCount())==null?new BigDecimal("0"):new BigDecimal(loadStat.getCount()));
                grossAmount = grossAmount.add(loadStat.getGrossAmount()==null?new BigDecimal("0"):loadStat.getGrossAmount());
                registrationCost = registrationCost.add(loadStat.getRegistrationCost()==null?new BigDecimal("0"):loadStat.getRegistrationCost());
            }

            workLoadStat2.setCount(count.toString());
            workLoadStat2.setGrossAmount(grossAmount);
            workLoadStat2.setRegistrationCost(registrationCost);

            parameters.clear();
            parameters.addAll(params);
            PageRequest pageRequest1 = new PageRequest(parameters, orderby);
            List<WorkLoadStat> temporaryCostStat = this.dao.getTemporaryCostStat(pageRequest1);
            if (temporaryCostStat != null && temporaryCostStat.size() > 0) {
                for (WorkLoadStat workLoadStat1 : temporaryCostStat) {
                    setCostByTypeName(workLoadStat2, workLoadStat1.getTypes(), workLoadStat1.getTemporaryCost());
                }
            }
        }
        return workLoadStat2;
    }


    //báo cáoXuất
    public void exportExcel(SearchParams searchParams, HttpServletResponse response) throws IOException {
        if(Objects.equals("doctorDetail",searchParams.getColumnName())){
            //Bác sĩThu nhậpthống kê
            exportDoctorDetail(searchParams,response);
        }else if(Objects.equals("drugDetail",searchParams.getColumnName())){
            //ThuốcThống kê tổng hợp bán hàng
            exportDrugDetail(searchParams,response);
        }
    }

    //ThuốcThống kê tổng hợp bán hàngXuất
    private void exportDrugDetail(SearchParams searchParams, HttpServletResponse response) throws IOException {
        Page<DrugSales> drugSalesPage = getDrugSales(searchParams.getParams(), 0, 1000000, searchParams.getOrder());
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!CollectionUtils.isEmpty(drugSalesPage.getRows())) {
            for (DrugSales ds : drugSalesPage.getRows()) {
                Map<String, Object> row = new HashMap<>();
                row.put("type", ds.getType());
                row.put("name", ds.getName());
                // Quy cách = Liều lượng+Đơn vị liều lượng*Số lượng+Số lượngĐơn vị/Đóng gói
                String norms = ds.getDosis() + ds.getDosisUnit() + "*" + ds.getPreparation() + ds.getPreparationUnit() + "/" + ds.getPack();
                row.put("norms", norms);
                // Số lượngquy đổi
                int prep = Integer.parseInt(ds.getPreparation());
                int wholePacks = ds.getTotal() / prep;
                int remainder = ds.getTotal() % prep;
                String number = wholePacks + ds.getPack() + (remainder > 0 ? remainder + ds.getPreparationUnit() : "");
                row.put("number", number);
                row.put("allFee", BigdecimalConvert.convert(ds.getAllFee()));
                rows.add(row);
            }
        }
        new ExcelExportBuilder("ThuốcBảng thống kê tổng hợp bán hàng")
                .addColumns(
                        new ExcelExportBuilder.Column("Phân loại thuốc", "type"),
                        new ExcelExportBuilder.Column("Tên thuốc", "name"),
                        new ExcelExportBuilder.Column("Quy cách", "norms"),
                        new ExcelExportBuilder.Column("Số lượng", "number"),
                        new ExcelExportBuilder.Column("tổng giá( đồng)", "allFee")
                )
                .data(rows)
                .write(response, "ThuốcThống kê tổng hợp bán hàng.xlsx");
    }

    //Bác sĩThu nhậpthống kêXuất
    private void exportDoctorDetail(SearchParams searchParams, HttpServletResponse response) throws IOException {
        Page<WorkLoad> workloadPage = getWorkload(searchParams.getParams(), 0, 1000000, searchParams.getOrder());
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!CollectionUtils.isEmpty(workloadPage.getRows())) {
            for (WorkLoad wl : workloadPage.getRows()) {
                Map<String, Object> row = new HashMap<>();
                row.put("name", wl.getName());
                row.put("count", wl.getCount());
                row.put("grossAmount", BigdecimalConvert.convert(wl.getGrossAmount()));
                row.put("registrationCost", BigdecimalConvert.convert(wl.getRegistrationCost()));
                row.put("westCost", BigdecimalConvert.convert(wl.getWestCost()));
                row.put("chineseCost", BigdecimalConvert.convert(wl.getChineseCost()));
                row.put("chinesePatentCost", BigdecimalConvert.convert(wl.getChinesePatentCost()));
                row.put("stuffCost", BigdecimalConvert.convert(wl.getStuffCost()));
                row.put("examinesCost", BigdecimalConvert.convert(wl.getExaminesCost()));
                row.put("checkoutCost", BigdecimalConvert.convert(wl.getCheckoutCost()));
                row.put("therapyCost", BigdecimalConvert.convert(wl.getTherapyCost()));
                row.put("cureCost", BigdecimalConvert.convert(wl.getCureCost()));
                row.put("otherCost", BigdecimalConvert.convert(wl.getOtherCost()));
                rows.add(row);
            }
        }
        new ExcelExportBuilder("Bác sĩThu nhậpthống kêbảng")
                .addColumns(
                        new ExcelExportBuilder.Column("Bác sĩTên", "name"),
                        new ExcelExportBuilder.Column("Số lần tiếp nhận", "count"),
                        new ExcelExportBuilder.Column("tổngSố tiền( đồng)", "grossAmount"),
                        new ExcelExportBuilder.Column("Phí đăng ký khám( đồng)", "registrationCost"),
                        new ExcelExportBuilder.Column("Tâythuốcphí( đồng)", "westCost"),
                        new ExcelExportBuilder.Column("Trong đó tiền thuốc Đông y (đồng)", "chineseCost"),
                        new ExcelExportBuilder.Column(" trong thànhthuốcphí( đồng)", "chinesePatentCost"),
                        new ExcelExportBuilder.Column("Phí vật tư( đồng)", "stuffCost"),
                        new ExcelExportBuilder.Column("Xét nghiệmphí( đồng)", "examinesCost"),
                        new ExcelExportBuilder.Column("Kiểm traphí( đồng)", "checkoutCost"),
                        new ExcelExportBuilder.Column("Vật lý trị liệuphí( đồng)", "therapyCost"),
                        new ExcelExportBuilder.Column("điều trịphí( đồng)", "cureCost"),
                        new ExcelExportBuilder.Column("Khác", "otherCost")
                )
                .data(rows)
                .write(response, "Bác sĩThu nhậpthống kê.xlsx");
    }

    @Transactional(readOnly = true)
    public void exportDrugOrStuffStock(YpjxcRc ypjxcRc, HttpServletResponse response) throws IOException {
            exportDrugStock(ypjxcRc,response);
    }

    private void exportDrugStock(YpjxcRc ypjxcRc, HttpServletResponse response) throws IOException {
        boolean isDrug = Objects.equals("1", ypjxcRc.getYpcltype());
        String sheetName = isDrug ? "Thuốcnhập khothống kêbảng" : "Vật tưnhập khothống kêbảng";
        String fileName = isDrug ? "Thuốcnhập khothống kê.xlsx" : "Vật tưnhập khothống kê.xlsx";

        Page<Ypclrkcx> page = getypclrkcxlist(ypjxcRc);
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!CollectionUtils.isEmpty(page.getRows())) {
            for (Ypclrkcx item : page.getRows()) {
                Map<String, Object> row = new HashMap<>();
                row.put("rkrq", item.getRkrq());
                row.put("rkdh", item.getRkdh());
                row.put("rkry", item.getRkry());
                row.put("spbh", item.getSpbh());
                row.put("spmc", item.getSpmc());
                // Phân loại
                if (isDrug) {
                    String type = Objects.equals(BizConstants.MEDICAL_TYPE_WESTERN, item.getLx()) ? "Thuốc tây" : Objects.equals(BizConstants.MEDICAL_TYPE_HERBAL, item.getLx()) ? "Dược liệu đông y" : "Thuốc thành phẩm đông y";
                    row.put("type", type);
                } else {
                    String type = Objects.equals(BizConstants.STUFF_TYPE_MATERIAL, item.getLx()) ? "không phảiydùngVật tư" : "ydùngVật tư";
                    row.put("type", type);
                }
                row.put("ph", item.getPh() != null ? item.getPh() : "");
                row.put("gg", item.getGg());
                row.put("sccj", item.getSccj() != null ? item.getSccj() : "");
                row.put("gys", item.getGys() != null ? item.getGys() : "");
                // Số lượngquy đổi
                int sl = Integer.parseInt(item.getSl());
                int zj = Integer.parseInt(item.getZj());
                int wholePacks = sl / zj;
                int remainder = sl % zj;
                String number = wholePacks + item.getDw() + (remainder > 0 ? remainder + item.getZxdw() : "");
                row.put("number", number);
                row.put("dw", item.getDw() != null ? item.getDw() : "");
                row.put("lsj", new BigDecimal(item.getLsj() != null ? item.getLsj() : "0"));
                row.put("cbj", new BigDecimal(item.getCbj() != null ? item.getCbj() : "0"));
                row.put("cbhj", new BigDecimal(item.getCbhj() != null ? item.getCbhj() : "0"));
                row.put("yxq", item.getYxq() != null ? item.getYxq() : "");
                row.put("shzt", Objects.equals(BizConstants.SUPPLIER_STORAGE_EXAMINE_PASS, item.getShzt()) ? "Qua" : "Đãhủy");
                row.put("bz", item.getBz() != null ? item.getBz() : "");
                rows.add(row);
            }
        }
        new ExcelExportBuilder(sheetName)
                .addColumns(
                        new ExcelExportBuilder.Column("nhập khoNgày", "rkrq"),
                        new ExcelExportBuilder.Column("phiếu nhập khosố", "rkdh"),
                        new ExcelExportBuilder.Column("nhập khonhân viên", "rkry"),
                        new ExcelExportBuilder.Column("hàng hóaMã", "spbh"),
                        new ExcelExportBuilder.Column("Tên hàng hóa", "spmc"),
                        new ExcelExportBuilder.Column("Phân loại", "type"),
                        new ExcelExportBuilder.Column("Số lô", "ph"),
                        new ExcelExportBuilder.Column("Quy cách", "gg"),
                        new ExcelExportBuilder.Column("Nhà sản xuất", "sccj"),
                        new ExcelExportBuilder.Column("Nhà cung cấp", "gys"),
                        new ExcelExportBuilder.Column("Số lượng", "number"),
                        new ExcelExportBuilder.Column("Đơn vị", "dw"),
                        new ExcelExportBuilder.Column("Giá bán lẻ( đồng)", "lsj"),
                        new ExcelExportBuilder.Column("Giá vốn( đồng)", "cbj"),
                        new ExcelExportBuilder.Column("Tổng chi phí( đồng)", "cbhj"),
                        new ExcelExportBuilder.Column("Thời hạn hiệu lực", "yxq"),
                        new ExcelExportBuilder.Column("Trạng thái xét duyệt", "shzt"),
                        new ExcelExportBuilder.Column("Ghi chú", "bz")
                )
                .data(rows)
                .write(response, fileName);
    }

    /**
     * Ánh xạ chi tiết chi phí theo loại sang trường tương ứng của đối tượng thống kê
     * Xóa bỏ getWorkload/getWorkLoadStat/getdoctorDetailstatistics  trong trùng lặp của  if-else chuỗi
     *
     * @param items Chi phíChi tiếtDanh sách(mỗihạng mụcbao gồm types  và  temporaryCost)
     * @param setter Loại→Chi phí  của Đặtcallback
     */
    private <T> void applyCostByType(List<T> items, java.util.function.BiConsumer<String, java.math.BigDecimal> setter) {
        if (items == null || items.isEmpty()) return;
        for (T item : items) {
            String types;
            java.math.BigDecimal cost;
            if (item instanceof WorkLoad) {
                types = ((WorkLoad) item).getTypes();
                cost = ((WorkLoad) item).getTemporaryCost();
            } else if (item instanceof WorkLoadStat) {
                types = ((WorkLoadStat) item).getTypes();
                cost = ((WorkLoadStat) item).getTemporaryCost();
            } else {
                continue;
            }
            setter.accept(types, cost);
        }
    }

    /**
     * TheoLoạiTênĐặt WorkLoad tương ứng của Chi phíTrường
     */
    private void setCostByTypeName(WorkLoad workLoad, String types, java.math.BigDecimal cost) {
        switch (types) {
            case "Thuốc thành phẩm đông y": workLoad.setChinesePatentCost(cost); break;
            case "Thuốc tây":   workLoad.setWestCost(cost); break;
            case "Dược liệu đông y": workLoad.setChineseCost(cost); break;
            case "Vật tư":   workLoad.setStuffCost(cost); break;
            case "Xét nghiệm":   workLoad.setExaminesCost(cost); break;
            case "Kiểm tra":   workLoad.setCheckoutCost(cost); break;
            case "Vật lý trị liệu":   workLoad.setTherapyCost(cost); break;
            case "Điều trị":   workLoad.setCureCost(cost); break;
            case "Khác":   workLoad.setOtherCost(cost); break;
            default: break;
        }
    }

    /**
     * TheoLoạiTênĐặt WorkLoadStat tương ứng của Chi phíTrường
     */
    private void setCostByTypeName(WorkLoadStat stat, String types, java.math.BigDecimal cost) {
        switch (types) {
            case "Thuốc thành phẩm đông y": stat.setChinesePatentCost(cost); break;
            case "Thuốc tây":   stat.setWestCost(cost); break;
            case "Dược liệu đông y": stat.setChineseCost(cost); break;
            case "Vật tư":   stat.setStuffCost(cost); break;
            case "Xét nghiệm":   stat.setExaminesCost(cost); break;
            case "Kiểm tra":   stat.setCheckoutCost(cost); break;
            case "Vật lý trị liệu":   stat.setTherapyCost(cost); break;
            case "Điều trị":   stat.setCureCost(cost); break;
            case "Khác":   stat.setOtherCost(cost); break;
            default: break;
        }
    }
}