package com.geeke.toll.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geeke.admin.entity.User;
import com.geeke.admin.entity.UserExt;
import com.geeke.admin.service.UserExtService;
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

    private final UserExtService userExtService;

    /**
     * Sẽ  SearchParams Chuyển đổi thành PageRequest
     * đơn giảnhóabáo cáoTruy vấnPhương thứcTrongtrùng lặpmã
     */
    private PageRequest toPageRequest(SearchParams searchParams) {
        // W2 (iteration 17): body khong duoc dieu khien tenant nua — strip
        // company_id cua body roi ep tenant session, theo mau
        // CrudService.ensureCompanyFilter (nhu T2/V1). Session rong/"null"
        // (luong noi bo khong session) moi giu nguyen nhu cu.
        List<Parameter> params = searchParams.getParams();
        ensureCompanyFilter(params);
        return new PageRequest(searchParams.getOffset(), searchParams.getLimit(),
                params, searchParams.getOrderby());
    }

    @Override
    @Transactional(readOnly = false)
    public TollInfo save(TollInfo tollInfo) {
        // DS122 C: id khong ton tai truoc day roi qua super.save -> doUpdate
        // 0 dong roi van 200 silent no-op (lop DS94/DS99); chan truoc moi
        // ghi. Khong check paymentType/amountStatus/tollType o day:
        // nhanh REFUNDED cua controller luu lai phieu goc + phieu hoan
        // chi co amountStatus (hanh vi da duoc ds11 pin, khong co caller
        // UI); check required dat o controller, chi nhanh thuong.
        if (StringUtils.isNotBlank(tollInfo.getId())
                && this.get(tollInfo.getId()) == null) {
            throw new ServiceException("Phiếu thu không tồn tại: "
                    + tollInfo.getId());
        }
        // DS148 C: chan do dai truoc moi xu ly (gia tri DictItem
        // paymentType/amountStatus/tollType viet thang vao varchar(64),
        // @Valid khong guard gia tri DictItem).
        checkTollInfoLengths(tollInfo);
        // DS224: chan so tien am truoc moi ghi (amountReceivable /
        // amountReceived khong @Min/@Positive, cot decimal co dau +
        // nullable nen DB chap nhan; phieu hoan dung so duong goc +
        // amountStatus REFUNDED + returnType=1 nen guard nay khong pha
        // luong hoan; amountDiscounted KHONG guard vi tra thua
        // (received > receivable) hop le cho discount am).
        checkTollInfoNonNegative(tollInfo);
        // Khi thêm mới, Xử lý trường tự động đánh số
        tollInfo.setCreateDate(new Date());
        if (StringUtils.isBlank(tollInfo.getTollNumber())){
            String sn = sequenceService.generate(SessionUtils.getUser().getCompanyId(), "toll_code", tollInfo);
            tollInfo.setTollNumber(sn);
        }

        TollInfo tollInfoTemp = super.save(tollInfo);
        return tollInfoTemp;
    }

    /**
     * DS122 C: bulk* fail-closed 400. bulkInsert/bulkUpdate khong qua
     * save() nen khong co ca check amountStatus cua controller: body chi
     * co company cung insert duoc phieu ma (moi cot nghiep vu deu nullable
     * trong DB, lop DS120C), id ma -> 200 silent no-op, [null] -> NPE 500.
     * Validate toan bo list truoc moi ghi: null/empty/[null] -> 400;
     * thieu paymentType/amountStatus/tollType -> 400; bulkUpdate/
     * bulkDelete id khong ton tai -> 400 (kiem tra stored-row tenant).
     * Company rong -> ep theo session; company la de guard generic chan.
     */
    /**
     * DS148 C: chan do dai qua dai qua bulk/save. bulkInsert/bulkUpdate
     * khong qua @Valid nen tollNumber (co @Length 64) van 500 o mapper
     * khi qua bulk (probe C1; bulkUpdate KHONG viet tollNumber nen C1
     * chi bulkInsert). Gia tri DictItem paymentType/amountStatus/tollType
     * viet thang vao varchar(64) -- DictItem @Length(128) khong guard
     * cot dich, ca duong save don cung 500 (probe C2/C3/C4). jgid/jgmc/
     * department/doseUnit khong co cot DB (transient) nen khong guard.
     * Gioi han theo cot DB, mau checkCostItemLengths (DS146 A / DS147 B).
     * Null-safe: nhanh REFUNDED cua controller luu phieu hoan chi co
     * amountStatus (paymentType/tollType null) van qua nhu cu.
     */
    private static void checkTollInfoLengths(TollInfo tollInfo) {
        if (tollInfo == null) {
            return;
        }
        checkLength(tollInfo.getTollNumber(), 64, "Thu phí đơn số Độ dài phải nằm trong khoảng 1 đến 64");
        checkLength(tollInfo.getRemarks(), 255, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
        checkDictLength(tollInfo.getPaymentType(), 64, "Loại thanh toán Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(tollInfo.getAmountStatus(), 64, "Trạng thái phiếu thu Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(tollInfo.getTollType(), 64, "Loại thu phí Độ dài phải nằm trong khoảng 0 đến 64");
    }

    private static void checkDictLength(com.geeke.sys.entity.DictItem item, int max, String message) {
        if (item != null) {
            checkLength(item.getValue(), max, message);
        }
    }

    /**
     * DS224: so tien phieu thu khong duoc am. Null-safe: null van qua
     * (cot nullable; cho nao bat buoc da co guard null rieng, DS158).
     * Chi amountReceivable / amountReceived; amountDiscounted co the am
     * hop le khi tra thua (received > receivable).
     */
    private static void checkTollInfoNonNegative(TollInfo tollInfo) {
        if (tollInfo == null) {
            return;
        }
        if (tollInfo.getAmountReceivable() != null
                && tollInfo.getAmountReceivable().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Số tiền phải thu không được âm");
        }
        if (tollInfo.getAmountReceived() != null
                && tollInfo.getAmountReceived().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Số tiền thực thu không được âm");
        }
    }

    private static void checkLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ServiceException(message);
        }
    }

    private void checkTollInfoRequired(TollInfo tollInfo) {
        if (tollInfo == null) {
            throw new ServiceException("Phiếu thu không được để trống");
        }
        if (tollInfo.getPaymentType() == null
                || StringUtils.isBlank(tollInfo.getPaymentType().getValue())
                || tollInfo.getAmountStatus() == null
                || StringUtils.isBlank(tollInfo.getAmountStatus().getValue())
                || tollInfo.getTollType() == null
                || StringUtils.isBlank(tollInfo.getTollType().getValue())) {
            throw new ServiceException("Loại thanh toán, trạng thái phiếu thu và loại thu phí không được để trống");
        }
    }

    private void forceSessionCompanyWhenBlank(TollInfo tollInfo) {
        if (tollInfo.getCompany() != null
                && StringUtils.isNotBlank(tollInfo.getCompany().getId())) {
            return;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        Company company = new Company();
        company.setId(tenantId);
        tollInfo.setCompany(company);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<TollInfo> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiếu thu không được để trống");
        }
        for (TollInfo tollInfo : entitys) {
            checkTollInfoRequired(tollInfo);
            checkTollInfoLengths(tollInfo);
            checkTollInfoNonNegative(tollInfo);
            forceSessionCompanyWhenBlank(tollInfo);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<TollInfo> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiếu thu không được để trống");
        }
        for (TollInfo tollInfo : entitys) {
            if (tollInfo == null || StringUtils.isBlank(tollInfo.getId())) {
                throw new ServiceException("Phiếu thu không được để trống");
            }
            if (this.get(tollInfo.getId()) == null) {
                throw new ServiceException("Phiếu thu không tồn tại: "
                        + tollInfo.getId());
            }
            verifyTenantOwnershipAgainstStoredRow(tollInfo);
            checkTollInfoRequired(tollInfo);
            checkTollInfoLengths(tollInfo);
            checkTollInfoNonNegative(tollInfo);
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<TollInfo> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách phiếu thu không được để trống");
        }
        for (TollInfo tollInfo : entitys) {
            if (tollInfo == null || StringUtils.isBlank(tollInfo.getId())) {
                throw new ServiceException("Phiếu thu không được để trống");
            }
            if (this.get(tollInfo.getId()) == null) {
                throw new ServiceException("Phiếu thu không tồn tại: "
                        + tollInfo.getId());
            }
            verifyTenantOwnershipAgainstStoredRow(tollInfo);
        }
        return super.bulkDelete(entitys);
    }

    /**
     * DS123 A: single delete fail-closed 400. CrudService.delete chi
     * chan qua stored-row tenant check, ma check do bo qua stored==null
     * nen delete id ma tra 200 data=0 (DS94 class, nhu bulkDelete truoc
     * DS122); mapper delete khong loc del_flag nen xoa dong da xoa mem
     * tra 200 rows=1 va ghi them 1 audit DELETED (probe C4). Kiem tra
     * ton tai truoc: id rong/id ma/dong da xoa mem (del_flag != '0')
     * -> 400. Caller noi bo duy nhat cancelUnpaidToll() chi goi sau khi
     * khoa doc va kiem tra del_flag='0' nen khong doi.
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(TollInfo tollInfo) {
        if (tollInfo == null || StringUtils.isBlank(tollInfo.getId())) {
            throw new ServiceException("Không xác định được phiếu thu cần xóa.");
        }
        TollInfo stored = this.get(tollInfo.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Phiếu thu không tồn tại: "
                    + tollInfo.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(tollInfo);
        return super.delete(tollInfo);
    }
    

    @Transactional(readOnly = false)
    public void saveToll(TollEvt tollEvt,String type) {
        // DS156 C: checkTollInfoRequired chi duoc goi tu save()/bulk*,
        // chua bao gio goi tu saveToll; tollInfo/amountStatus null truoc
        // day NPE -> 500 ngay o dong validateInventoryNotInProgress
        // duoi (controller nhan @NotNull TollEvt nhung khong @Valid nen
        // khong co validation nao 400 truoc). Goi guard o day, truoc
        // moi deref/getAmountStatus().getValue(), fail-closed 400.
        TollInfo tollInfo = tollEvt == null ? null : tollEvt.getTollInfo();
        checkTollInfoRequired(tollInfo);
        Company company = SessionUtils.getUser().getCompany();
        String seltId="";//BHYTQuyết toánid thangphísaulưu vàoĐăng ký khámbảng
        //Đang tiến hành kiểm kê giờKhông thểtiến hành dòngThu phí/Hoàn phí
        validateInventoryNotInProgress(company.getId(), tollInfo.getAmountStatus().getValue());
        // DS98: discount null unboxes to NPE at :232
        // (BigDecimal.valueOf(getDiscount())) AFTER the charge already
        // ran -- validate up front so a missing discount fails closed
        // 400 with nothing written. The UI always sends discount
        // (retailCharge tollEncapsulation / tollInfo form); a missing
        // one is a malformed call, not "no discount" (that is discount=1).
        if (tollInfo.getDiscount() == null) {
            throw new ServiceException("Chiết khấu không được để trống");
        }
        // DS224: chan so tien am ngay o cua saveToll, truoc moi claim/ghi
        // (luong thuong ghi lai qua save() ben trong nen cung duoc guard,
        // nhung luc do claimPaid da lat don sang da thu; chan o day thi
        // don giu nguyen is_pay=0, khong dong nao duoc ghi).
        checkTollInfoNonNegative(tollInfo);
        List<RecipelInfoEvt> recipelInfoEvts = tollEvt.getRecipelInfos();
        TollInfo tollSave = null;
        Registration registration = null;
        if (!CollectionUtils.isEmpty(recipelInfoEvts)) {
            // N5-TOLL (iter62): verify stored company cua tung recipelInfo
            // trong payload TRUOC moi ghi (nhanh PAID, vong dong dau :206,
            // processRefund :495): khong tin company tu payload; lookup
            // truc tiep 1 cot getRecipelInfoCompanyId (tien le X3, SELECT
            // don bang, khong INNER JOIN nhu dao.get() cua N1) nen khong
            // bi null gia. Stored company lech session tenant -> 400
            // fail-closed ca TollEvt (cung transaction, chua ghi dong nao).
            // Khong ton tai / stored company rong giu hanh vi cu (tien le
            // orphan N3c); session khong tenant thi skip nhu cac guard khac.
            // processRefund (:495) va cancel (:881) khong can guard rieng:
            // processRefund chi goi tu day (sau guard nay), cancel da bi
            // CrudService.get chan foreign truoc vong reset (probe iter62).
            String tollTenantId = SessionUtils.getLoginTenantId();
            if (StringUtils.isNotBlank(tollTenantId) && !"null".equals(tollTenantId)) {
                for (RecipelInfoEvt recipelInfoEvt : recipelInfoEvts) {
                    if (recipelInfoEvt == null || recipelInfoEvt.getRecipelInfo() == null) {
                        continue;
                    }
                    String recipelId = recipelInfoEvt.getRecipelInfo().getId();
                    if (StringUtils.isBlank(recipelId)) {
                        continue;
                    }
                    String storedCompanyId = recipelInfoService.getRecipelInfoCompanyId(recipelId);
                    if (StringUtils.isNotBlank(storedCompanyId) && !tollTenantId.equals(storedCompanyId)) {
                        throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
                    }
                }
            }
            // DS170 B: phan tu null trong recipelInfos (evt null hoac
            // recipelInfo null) lam stream map ben duoi NPE 500 o
            // getRecipelInfo().getId() truoc moi guard/claim. DS158 A1
            // chi chan id TRONG, khong chan evt null. Fail-closed 400
            // truoc moi doc; id trong KHONG chan o day vi luong ban le
            // tao don moi co id trong hop le (addRetailRecipelInfo),
            // duong thuong van duoc guard DS158 o processNormalCharge.
            for (RecipelInfoEvt recipelInfoEvt : recipelInfoEvts) {
                if (recipelInfoEvt == null || recipelInfoEvt.getRecipelInfo() == null) {
                    throw new ServiceException("Đơn thuốc của phiếu thu không được để trống");
                }
            }
            //Kiểm tratrùng lặpđề xuấtgiao
            List<String> InRecipeIds = recipelInfoEvts.stream().map(recipelInfoEvt -> {
                return recipelInfoEvt.getRecipelInfo().getId();
            }).collect(Collectors.toList());
            List<TollInfo> inRecipeIds = this.getInRecipeIds(InRecipeIds,tollInfo.getAmountStatus().getValue());
            if (!CollectionUtils.isEmpty(inRecipeIds)) {
                throw new ServiceException("Dữ liệuĐã tồn tạiVui lòngđừngtrùng lặpđề xuấtgiao");
            }
            Patient patient = tollEvt.getTollInfo().getPatient();
            // DS157 B: patient null truoc day NPE -> 500 o patient.getId()
            // ben duoi; checkTollInfoRequired chi bat paymentType /
            // amountStatus / tollType, khong bat patient; discount da guard
            // rieng (DS98). Fail-closed 400 truoc moi ghi (claimPaid va
            // vong chinh chay sau dong nay). Patient co id rong giu hanh
            // vi cu (luong ban le tao benh nhan moi tu payload).
            if (patient == null) {
                throw new ServiceException("Bệnh nhân của phiếu thu không được để trống");
            }
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
                    // DS170 A: allFee NULL (chi tiet chua tinh tien, DS90)
                    // thi getAllFee().multiply(c) NPE 500 o day, sau khi
                    // claimPaid + thu phi da ghi (cung transaction nen
                    // rollback, don van chua thu duoc); chi tiet chua tinh
                    // tien dong gop 0 nhu DS90/DS169, khong phai loi.
                    BigDecimal allFee = e.getAllFee() == null ? BigDecimal.ZERO : e.getAllFee();
                    e.setActualPayment(allFee.multiply(c));
                    recipelDetailDao.updateActualPayment(e.getActualPayment(),e.getId());
                }
            }
            //Hoàn phí
            if (BizConstants.AMOUNT_STATUS_REFUNDED.equals(tollInfo.getAmountStatus().getValue())) {
                processRefund(tollEvt, tollInfo, recipelInfoEvts);
            }
            //Đăng ký khámphương thức thanh toán, Trạng thái thu phí
            if (null == registration) {
                // DS227: fallback nay chay tren MOI lan thu thuong (BHYT
                // tat nen processNormalCharge LUON tra null) va moi lan
                // hoan phi; medical/registration lay THANG tu payload
                // (tollInfo.getMedical() khong duoc ghi de boi
                // processNormalCharge) nen ca 2 deref deu NPE 500 khi
                // payload sai (probe tools/ds227-verify.py, log
                // backend: ghost registration -> :495 getPayType();
                // registration thieu -> :491 getId(); refund + medical
                // thieu -> :491, duong refund khong co guard DS158).
                // Fail-closed 400 truoc moi deref; giao dich rollback,
                // don giu is_pay cu, khong phieu nao duoc ghi.
                if (tollInfo.getMedical() == null
                        || tollInfo.getMedical().getRegistration() == null
                        || StringUtils.isBlank(tollInfo.getMedical()
                                .getRegistration().getId())) {
                    throw new ServiceException(
                            "Lượt khám của phiếu thu chưa có thông tin đăng ký");
                }
                registration = registrationService.get(tollInfo.getMedical()
                        .getRegistration().getId());
                if (registration == null) {
                    throw new ServiceException(
                            "Không tìm thấy lượt đăng ký khám");
                }
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
        // DS171 C: don dieu tri co registration_id NULL (dong luu) ->
        // getRegistration() null, getId() NPE 500 giua thu phi (probe
        // C1). Fail-closed 400 nhu DS171 A/B; giao dich rollback.
        if (recipelInfo.getRegistration() == null) {
            throw new ServiceException("Không tìm thấy lượt đăng ký khám");
        }
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
        // DS158 A: fail-closed truoc moi ghi (claimPaid + vong chinh).
        // Vong claim ben duoi `continue` khi id trong, roi vong chinh
        // get("") tra null -> NPE 500 o :542; medical null NPE 500 o :552
        // (sau khi recipel da save); fee null / amountReceived null NPE
        // 500 o :555 (subtract); ten don null/ngan hon 2 ky tu NPE /
        // StringIndexOutOfBounds 500 o :557 (substring(0,2)). Tat ca deu
        // la payload sai dinh dang, khong phai du lieu that -- chan 400
        // ro nghia truoc khi claim bat ky don nao. Don ghost van 400
        // (khong ton tai, thay cho bao trung lap cua claimPaid).
        if (tollInfo.getMedical() == null
                || StringUtils.isBlank(tollInfo.getMedical().getId())) {
            throw new ServiceException("Ca bệnh của phiếu thu không được để trống");
        }
        if (tollInfo.getAmountReceived() == null) {
            throw new ServiceException("Số tiền thực thu không được để trống");
        }
        for (RecipelInfoEvt recipelInfoEvt : recipelInfoEvts) {
            if (recipelInfoEvt == null || recipelInfoEvt.getRecipelInfo() == null
                    || StringUtils.isBlank(recipelInfoEvt.getRecipelInfo().getId())) {
                throw new ServiceException("Đơn thuốc của phiếu thu không được để trống");
            }
            RecipelInfo stored = recipelInfoService.get(recipelInfoEvt.getRecipelInfo().getId());
            if (stored == null) {
                throw new ServiceException("Đơn thuốc không tồn tại: "
                        + recipelInfoEvt.getRecipelInfo().getId());
            }
            if (stored.getFee() == null) {
                throw new ServiceException("Phí đơn thuốc không được để trống: "
                        + stored.getId());
            }
            // DS224: phi am chay vao amountReceivable am o vong chinh
            // (receivable = fee); chan truoc claim nhu cac guard DS158.
            if (stored.getFee().compareTo(BigDecimal.ZERO) < 0) {
                throw new ServiceException("Phí đơn thuốc không được âm: "
                        + stored.getId());
            }
            if (stored.getName() == null || stored.getName().length() < 2) {
                throw new ServiceException("Tên đơn thuốc phải có ít nhất 2 ký tự để xác định loại thu phí: "
                        + stored.getId());
            }
        }
        // DS53: claim tung don la "đã thu" TRUOC moi dong tac thu phi/kho,
        // nguyen tu (UPDATE ... WHERE is_pay='0', chi 1 luong song song
        // thang claim; mau DS52). Guard getInRecipeIds o saveToll chi la
        // SELECT-then-act nen cac lan thu song song deu vuot qua roi moi
        // ghi TollInfo + goi deduction -> okOccupyStock, lam used tang gap
        // N (probe: x8 deu 200, used 40 thay vi 5, 8 phieu). Claim o day,
        // truoc vong chinh, nen don đã thu / da duoc claim boi luong khac
        // deu 400 truoc khi ghi dong nao; don trong cung TollEvt bi lap
        // cung bi chan (claim lan 2 tra 0 dong). Id trong duoc bo qua nhu
        // cu (vong chinh giu hanh vi cu). Thong bao giong guard trung lap
        // o saveToll de UI thay cung mot loi.
        for (RecipelInfoEvt recipelInfoEvt : recipelInfoEvts) {
            if (recipelInfoEvt == null || recipelInfoEvt.getRecipelInfo() == null
                    || StringUtils.isBlank(recipelInfoEvt.getRecipelInfo().getId())) {
                continue;
            }
            if (recipelInfoService.claimPaid(recipelInfoEvt.getRecipelInfo().getId()) == 0) {
                throw new ServiceException("Dữ liệuĐã tồn tạiVui lòngđừngtrùng lặpđề xuấtgiao");
            }
        }
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
        // DS225: deref xep chong -- truoc day bat ky thieu nao cung NPE
        // 500 giua thu phi (probe tools/ds225-verify.py, log
        // backend-ds225.log: :702 medical/registration payload trong,
        // :703 registration khong ton tai, :709 benh nhan chua co
        // patient_md_data, :719 recipelDetailEvtList rong, :723 detail
        // chua co days, :727 doctor.getUserExt() -- RegistrationDao
        // khong join sys_user_ext nen userExt LUON null, ca voi bac si
        // da co dong sys_user_ext). Fail-closed 400 truoc moi doc;
        // ma hanh nghe load qua UserExtService khi entity chua co
        // (bac si co dong sys_user_ext thi upload duoc, chi thieu that
        // moi 400). Chua goi web service BHYT o day nen 8101 chet
        // khong anh huong cac guard nay.
        if (tollInfo.getMedical() == null || tollInfo.getMedical().getRegistration() == null
                || StringUtils.isBlank(tollInfo.getMedical().getRegistration().getId())) {
            throw new ServiceException("Lượt khám của phiếu thu chưa có thông tin đăng ký để tải lên BHYT");
        }
        Registration registration = registrationService.get(tollInfo.getMedical().getRegistration().getId());
        if (registration == null) {
            throw new ServiceException("Lượt khám của phiếu thu không tồn tại để tải lên BHYT");
        }
        if (registration.getPatientId() == null || StringUtils.isBlank(registration.getPatientId().getId())) {
            throw new ServiceException("Lượt khám chưa có bệnh nhân để tải lên BHYT");
        }
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        if (patientMdData == null || StringUtils.isBlank(patientMdData.getPsnNo())) {
            throw new ServiceException("Bệnh nhân chưa có thông tin BHYT (mã số BHXH)");
        }
        if (recipelInfoEvt.getRecipelDetailEvtList() == null || recipelInfoEvt.getRecipelDetailEvtList().isEmpty()) {
            throw new ServiceException("Đơn thuốc chưa có chi tiết để tải lên BHYT");
        }
        RecipelDetail firstDetail = recipelInfoEvt.getRecipelDetailEvtList().get(0);
        if (firstDetail == null || firstDetail.getDays() == null || StringUtils.isBlank(firstDetail.getDays().getValue())) {
            throw new ServiceException("Chi tiết đơn thuốc chưa có số ngày dùng để tải lên BHYT");
        }
        ClinicOffice clinicOffice = registration.getClinicOffice();
        if (clinicOffice == null || StringUtils.isBlank(clinicOffice.getId())) {
            throw new ServiceException("Lượt khám chưa có khoa/phòng để tải lên BHYT");
        }
        User doctor = registration.getDoctor();
        if (doctor == null || StringUtils.isBlank(doctor.getId())) {
            throw new ServiceException("Lượt khám chưa có bác sĩ để tải lên BHYT");
        }
        String pracPsnCode = resolveDoctorPracCode(doctor);
        if (StringUtils.isBlank(pracPsnCode)) {
            throw new ServiceException("Bác sĩ chưa có mã hành nghề BHYT (prac_psn_code)");
        }
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
        data.put("cnt", firstDetail.getTotal());
        data.put("pric", firstDetail.getUnitPrice());
        data.put("sin_dos_dscr", "");
        data.put("used_frqu_dscr", "");
        data.put("prd_days", firstDetail.getDays().getValue());
        data.put("medc_way_dscr", "");
        data.put("bilg_dept_codg", clinicOffice.getCode());
        data.put("bilg_dept_name", clinicOffice.getName());
        data.put("bilg_dr_codg", pracPsnCode);
        data.put("bilg_dr_name", doctor.getName());
        data.put("acord_dept_codg", clinicOffice.getCode());
        data.put("acord_dept_name", clinicOffice.getName());
        data.put("orders_dr_code", pracPsnCode);
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
     * DS225: ma hanh nghe BHYT cua bac si. RegistrationDao khong join
     * sys_user_ext nen doctor.getUserExt() luon null -- load qua
     * UserExtService (mau UserService:81, tenant-scoped); van khong
     * co thi tra null de caller fail-closed 400 thay vi NPE 500.
     */
    private String resolveDoctorPracCode(User doctor) {
        if (doctor == null) {
            return null;
        }
        if (doctor.getUserExt() != null && StringUtils.isNotBlank(doctor.getUserExt().getPracPsnCode())) {
            return doctor.getUserExt().getPracPsnCode();
        }
        UserExt userExt = userExtService.getExtByUserId(doctor.getId());
        return userExt == null ? null : userExt.getPracPsnCode();
    }

    /**
     * Xử lýhoàn phílogic: Kiểm tra, Cập nhật trạng thái đơn thuốc, Tạohoàn phíBản ghi, trảcònTồn kho, BHYThoàn phí
     */
    private void processRefund(TollEvt tollEvt, TollInfo tollInfo, List<RecipelInfoEvt> recipelInfoEvts) {
        // DS158 A5: cung dang skip-roi-deref nhu processNormalCharge (A1):
        // vong claim ben duoi `continue` khi id trong, roi vong chinh
        // get("") tra null -> NPE 500 khi doc getIsDispension(). Chan 400
        // truoc moi claim.
        for (RecipelInfoEvt recipelInfoEvt : recipelInfoEvts) {
            if (recipelInfoEvt == null || recipelInfoEvt.getRecipelInfo() == null
                    || StringUtils.isBlank(recipelInfoEvt.getRecipelInfo().getId())) {
                throw new ServiceException("Đơn thuốc của phiếu hoàn không được để trống");
            }
        }
        // DS54: claim tung don la "đã hoàn" TRUOC moi dong tac hoan phi/kho,
        // nguyen tu (UPDATE ... WHERE is_pay='1', chi 1 luong song song
        // thang claim; mau DS53). Guard getInRecipeIds o saveToll chi la
        // SELECT-then-act nen cac lan hoan song song deu vuot qua roi moi
        // ghi phieu hoan + audit REFUND + materialRefund, lam phieu/audit
        // nhan doi va ton kho lech (probe pre: x8 deu 200, 8 phieu, 8
        // audit, surplus 10035 thay vi 10000). Claim o day, truoc vong
        // chinh, nen don đã hoàn / chua thu / da duoc claim boi luong khac
        // deu 400 truoc khi ghi dong nao; don trong cung TollEvt bi lap
        // cung bi chan (claim lan 2 tra 0 dong). Id trong duoc bo qua nhu
        // cu (vong chinh giu hanh vi cu). Thong bao giong guard trung lap
        // o saveToll de UI thay cung mot loi. Cung transaction saveToll
        // nen claim cua luong thua rollback cung giao dich.
        for (RecipelInfoEvt recipelInfoEvt : recipelInfoEvts) {
            if (recipelInfoEvt == null || recipelInfoEvt.getRecipelInfo() == null
                    || StringUtils.isBlank(recipelInfoEvt.getRecipelInfo().getId())) {
                continue;
            }
            if (recipelInfoService.claimRefunded(recipelInfoEvt.getRecipelInfo().getId()) == 0) {
                throw new ServiceException("Dữ liệuĐã tồn tạiVui lòngđừngtrùng lặpđề xuấtgiao");
            }
        }
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
            // Q1: đảo ngược dấu thực thu của saveToll trên các chi tiết
            // thuộc đơn vừa hoàn phí, về NULL (trước khi thu — như K2
            // ở nhánh hủy phiếu chưa thu). Vòng lặp đóng dấu ở saveToll
            // chạy TRƯỚC processRefund trong cùng lần gọi nên reset phải
            // đặt ở đây, sau nó, nếu không sẽ bị đóng dấu lại. Hoàn phí
            // đảo ngược toàn bộ đơn (chargeStatus=-1) nên không cần
            // guard "còn phiếu khác hiệu lực" như K2: phiếu gốc đã bị
            // đánh dấu trả lại (returnType=1) và phiếu hoàn cũng chỉ là
            // tiền đã trả lại, không còn tiền thực thu nào sở hữu dấu.
            // Cùng transaction với audit REFUND nên lỗi ở bước nào cũng
            // rollback cả ba.
            for (RecipelDetail detail : recipelDetailService.getRecipelDetail(recipelInfo.getId())) {
                if (detail != null && "0".equals(detail.getDelFlag())) {
                    recipelDetailDao.updateActualPayment(null, detail.getId());
                }
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
            // DS54: executions NULL nghia la chua thuc hien (don tao qua
            // allSave/UI khong gui executions; chi luong truyen dich moi
            // set) nen xu ly nhu 0. Truoc day getExecutions().compareTo
            // NPE -> hoan phi don 诊疗 nao co executions NULL cung 500
            // (probe DS54: refund#1 500, khong phieu hoan/audit nao).
            if (recipelDetail.getExecutions() != null
                    && recipelDetail.getExecutions().compareTo(BigDecimal.valueOf(0)) != 0) {
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
        // DS226: addInspectionCheck chay truoc guard registration NULL cua
        // deduction (DS171 C) nen don dieu tri co registration NULL /
        // registration thieu benh nhan van toi day va NPE 500 giua thu phi
        // (probe P1 :988, P2 :1000, P3 :988). Fail-closed 400 truoc moi
        // deref; giao dich rollback. Chi guard cac deref da probe chung
        // minh NPE: gender NOT NULL o DB nen khong guard (khong fix mu),
        // doctor mapper tra shell ten rong nen getDoctor().getName() khong
        // NPE (probe P6 200 ca pre lan fixed).
        if (recipelInfo.getRegistration() == null
                || StringUtils.isBlank(recipelInfo.getRegistration().getId())) {
            throw new ServiceException("Không tìm thấy lượt đăng ký khám");
        }
        //Theođăng kýThông tinLấyThông tin bệnh nhân
        Registration registration = registrationService.get(recipelInfo.getRegistration().getId());
        if (registration == null) {
            throw new ServiceException("Không tìm thấy lượt đăng ký khám");
        }
        if (registration.getPatientId() == null
                || StringUtils.isBlank(registration.getPatientId().getId())) {
            throw new ServiceException("Lượt đăng ký khám thiếu bệnh nhân");
        }
        Patient patient = patientService.get(registration.getPatientId().getId());
        if (patient == null) {
            throw new ServiceException("Không tìm thấy bệnh nhân của lượt đăng ký khám");
        }

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
        // DS98 (K3-class): recipel_info.seq is NOT NULL, but the retail
        // path never defaulted it -- a retail charge without seq 500d
        // (Column 'seq' cannot be null), while v1 allSave / v2
        // saveRecipelInfoEvt both set seq=i+1. Default the same way
        // here; the UI sends seq:1, so this only bites API callers.
        int recipelIdx = 0;
        for (RecipelInfoEvt recipelInfoEvt : recipelInfos) {
            RecipelInfo recipelInfo = recipelInfoEvt.getRecipelInfo();
            recipelIdx++;
            if (recipelInfo.getSeq() == null) {
                recipelInfo.setSeq(recipelIdx);
            }
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

    /**
     * Hủy phiếu thu chưa thu (H6 research loop).
     * Chỉ hủy khi phiếu còn hiệu lực (del_flag = '0'), trạng thái là
     * amountStatus_0 (chưa thanh toán) và thực thu bằng 0/null — tức
     * chưa có tiền di chuyển. Phiếu đã thu, đã hoàn hoặc đã có thực thu
     * bị từ chối: hủy chúng phải đi đường hoàn phí, không phải đường này.
     * Nếu phiếu gắn đơn thuốc đã phát thuốc thì cũng từ chối (không
     * đảo ngược phát thuốc ở đây). Cùng transaction: ghi 1 dòng
     * toll_audit_log action = 'CANCEL' trước, rồi xóa mềm phiếu qua
     * CrudService.delete (del_flag = '1' + sys_action DELETED); lỗi ở
     * bước nào cũng rollback cả hai. Đảo ngược actualPayment (K2):
     * saveToll đóng dấu mọi RecipelDetail.actualPayment = allFee ×
     * discount kể cả phiếu chưa thu, nên hủy phiếu chưa thu phải đưa
     * actualPayment của các chi tiết thuộc đơn của phiếu này về NULL
     * (giá trị trước khi thu — chi tiết mới tạo không có actualPayment;
     * về allFee sẽ bịa thành đã thu đủ, về 0 lẫn với chiết khấu 0).
     * Chỉ reset khi không còn phiếu nào khác đang hiệu lực cho cùng đơn
     * (phiếu đã thu/hoàn sau đó vẫn sở hữu dấu thực thu); phiếu không
     * gắn đơn thì không có chi tiết nào để reset. Không chạm
     * recipel_info/registration: trạng thái đơn/đăng ký do luồng thu/
     * hoàn sở hữu, không phải luồng hủy phiếu chưa thu.
     * Tenant được CrudService.get/delete kiểm tra (verifyTenantOwnership).
     */
    @Transactional(readOnly = false)
    public TollInfo cancelUnpaidToll(String id) {
        if (StringUtils.isBlank(id)) {
            throw new ServiceException("Không tìm thấy phiếu thu phí cần hủy.");
        }
        // DS60: locked read (TollInfoDao.getForUpdate) instead of this.get:
        // two concurrent cancels both read del_flag='0' before either
        // commits, so each wrote its own CANCEL audit (probe pre: par x8
        // all 200, 8 CANCEL audits, day summary cancelTotal 9x150000).
        // FOR UPDATE serializes them inside this @Transactional method:
        // the loser blocks, then re-reads del_flag='1' and is refused
        // 400 by the guard below, exactly like a sequential 2nd cancel.
        // Tenant check kept (CrudService.get did it; the DAO read does
        // not), and the rest of the method is unchanged.
        TollInfo info = tollInfoDao.getForUpdate(id);
        if (info != null) {
            verifyTenantOwnership(info);
        }
        if (info == null) {
            throw new ServiceException("Không tìm thấy phiếu thu phí cần hủy.");
        }
        // Dao.get không lọc del_flag nên phải kiểm tra tường minh:
        // phiếu đã xóa mềm trước đó không được hủy lần hai (tránh audit trùng).
        if (!"0".equals(info.getDelFlag())) {
            throw new ServiceException("Phiếu thu phí này đã bị hủy trước đó.");
        }
        String status = info.getAmountStatus() != null ? info.getAmountStatus().getValue() : null;
        if (!BizConstants.AMOUNT_STATUS_UNPAID.equals(status)) {
            throw new ServiceException("Chỉ hủy được phiếu chưa thu (trạng thái chưa thanh toán).");
        }
        BigDecimal received = info.getAmountReceived();
        if (received != null && received.compareTo(BigDecimal.ZERO) != 0) {
            throw new ServiceException("Phiếu đã có tiền thực thu, không thể hủy theo diện chưa thu.");
        }
        if (info.getRecipel() != null && StringUtils.isNotBlank(info.getRecipel().getId())) {
            RecipelInfo recipel = recipelInfoService.get(info.getRecipel().getId());
            if (recipel != null && "1".equals(recipel.getIsDispension())) {
                throw new ServiceException("Đơn thuốc đã phát thuốc, không thể hủy phiếu thu phí.");
            }
        }
        TollAuditLog auditLog = new TollAuditLog();
        auditLog.setCompany(info.getCompany() != null
                ? info.getCompany()
                : SessionUtils.getUser().getCompany());
        auditLog.setTollId(info.getId());
        auditLog.setRecipelId(info.getRecipel() != null ? info.getRecipel().getId() : null);
        auditLog.setPatientId(info.getPatient() != null ? info.getPatient().getId() : null);
        auditLog.setAction("CANCEL");
        auditLog.setAmountReceivable(info.getAmountReceivable());
        auditLog.setAmountReceived(info.getAmountReceived());
        auditLog.setPaymentType(info.getPaymentType() != null
                ? info.getPaymentType().getValue() : null);
        auditLog.setAmountStatus(status);
        auditLog.setRemarks("Hủy phiếu chưa thu " + info.getTollNumber());
        auditLog.preInsert();
        tollAuditLogDao.insertAudit(auditLog);
        // K2: đảo ngược dấu thực thu của saveToll trên các chi tiết
        // thuộc đơn của phiếu này, về NULL (trước khi thu). Bỏ qua khi
        // còn phiếu khác đang hiệu lực cho cùng đơn (phiếu đó sở hữu
        // dấu thực thu), và khi phiếu không gắn đơn. Cùng transaction
        // với audit + xóa mềm nên lỗi ở bước nào cũng rollback cả ba.
        if (info.getRecipel() != null && StringUtils.isNotBlank(info.getRecipel().getId())) {
            String recipelId = info.getRecipel().getId();
            boolean otherActiveToll = false;
            for (TollInfo other : this.getByRecipeId(recipelId)) {
                // getByRecipeId qua listAll vốn đã lọc del_flag='0' nên
                // danh sách này chỉ gồm phiếu đang hiệu lực; phiếu đang
                // hủy vẫn còn trong đó nên phải loại đúng id của nó.
                if (other != null && "0".equals(other.getDelFlag())
                        && !info.getId().equals(other.getId())) {
                    otherActiveToll = true;
                    break;
                }
            }
            if (!otherActiveToll) {
                for (RecipelDetail detail : recipelDetailService.getRecipelDetail(recipelId)) {
                    if (detail != null && "0".equals(detail.getDelFlag())) {
                        recipelDetailDao.updateActualPayment(null, detail.getId());
                    }
                }
            }
        }
        this.delete(info);
        return info;
    }

    public List<TollInfo> getCreateBy(List<Parameter> parameters, String orderby) {
        // W2: ep tenant session nhu toPageRequest (body company_id khong duoc
        // dieu khien bao cao; xem toPageRequest).
        ensureCompanyFilter(parameters);
        PageRequest pageRequest = new PageRequest(parameters, orderby);
        return this.dao.getCreateBy(pageRequest);
    }

    public Page<WorkLoad> getWorkload(List<Parameter> params,int offset,int limit, String orderby) {
        // W2: ep tenant session (mutate in-place nen ca batchPageRequest sau
        // cung mang filter session, khong chi query chinh).
        ensureCompanyFilter(params);
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
        // W2: ep tenant session nhu getWorkload.
        // DS188: body {} khong co params -> getParams() tra null -> NPE 500
        // o parameters.addAll(params) (:1312) khi co du lieu; chuan hoa ve
        // rong de fail-closed 200 + ensureCompanyFilter ep tenant.
        if (params == null) { params = new ArrayList<>(); }
        ensureCompanyFilter(params);
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
        // W2: ep tenant session (getdrugmarketstatistics uy thac qua day nen
        // cung duoc bao phu, goi lap lai van idempotent: strip roi them lai).
        ensureCompanyFilter(params);
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
        // W2: ep tenant session nhu getDrugSales.
        ensureCompanyFilter(params);
        PageRequest pageRequest = new PageRequest(offset, limit, params, orderby);
        DrugSales drugSalesStat = this.dao.getDrugSalesStat(pageRequest);
        return drugSalesStat;
    }

    /**
     * ITER65 item 2: chan doc bao cao Ypjxc/Rc cua nhom khac parent.
     * Pham vi cho phep = cung parent group voi session tenant (plan iter65
     * §2(c)): companyId duoc chap nhan khi bang session tenant hoac co cung
     * parent_id voi session tenant (lookup stored parent, single-column,
     * khong tin parent trong body). Khac parent -> ServiceException 400
     * fail-closed TRUOC moi truy van. Company khong ton tai (lookup null)
     * cung 400 fail-closed (ly do: count cua getcljxcmanagement khong loc
     * company nen giu parity cho companyId la se tra ve toan bo du lieu).
     * Session rong / "null" (luong noi bo khong session) giu hanh vi cu;
     * companyId rong giu hanh vi cu (truy van tra rong). Rieng tenant la
     * cong ty goc (parent_id = 0): chi cho chinh no + cac con truc tiep
     * cua no, khong cho cong ty goc khac (tat ca deu co parent_id = 0,
     * khong phai cung nhom). Khong ep session mu nhu OutpatientLog X1:
     * xem kho phong kham anh em cung nhom la tinh nang that (dropdown
     * jgid o trang drugmaterialsstockmanagement) nen giu lai.
     * Dat o SERVICE (khong phai controller): da grep, khong co caller noi
     * bo nao ngoai controller cho cac method nay — khac truong hop
     * savePermission cua item 1 (CompanyService.savePermisson goi noi bo).
     * Cac method ho Ypjxc ghi de jgid = institution cua companyId nen chi
     * can check companyId; ho rkcx dung truc tiep jgid trong body nen
     * check ca companyId lan jgid.
     */
    private void verifyYpjxcCompanyScope(String companyId) {
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        verifyCompanyInSessionGroup(tenantId, companyId);
    }

    private void verifyYpjxcCompanyAndJgidScope(String companyId, String jgid) {
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        verifyCompanyInSessionGroup(tenantId, companyId);
        verifyCompanyInSessionGroup(tenantId, jgid);
    }

    /**
     * ITER66 item 5-followup: chot scope that su truy van cho ho
     * ypjxc/stuffsummary (getypjxcmanagement(+sums),
     * getStuffsalessummarylists(+sumss) va cac delegate jgzt=1 cua
     * chung: getpharmaceuticalInventoryManagement(+sums),
     * getconsumablemarketstatistics(+sum)).
     * TRUOC DAY: 4 method nay ghi de jgid = institution cua companyId
     * (getInstitution) — mapper cua ho nay KHONG CO nhanh jgid!=null
     * cho list/count nen khi co institution, moi nhanh loc company deu
     * tat va query tra toan bo tenant, ke ca nhom khac parent (probe
     * iter66 item 5: ypjxc total 5 chua i66i-p2-drug, stuffsummary
     * total 4 chua i66i-p2-stuff). Sua admission guard la KHONG DU vi
     * request gay ro la request hop le toi company OWN.
     * TU NAY: khong ghi de jgid bang institution nua. Query LUON loc
     * theo company yeu cau: jgzt null -> companyId; jgzt=1 + jgid rong
     * -> nhom sibling cua companyId (nhu Phase A); jgzt=1 + jgid co gia
     * tri -> facility do (dropdown trang quan ly co so), va jgid do
     * phai qua cung guard cung-parent nhu companyId (neu khong, ke
     * gui jgid la truc tiep se mo lo moi khi service het ghi de).
     * Chuan hoa rong -> null vi MyBatis chi nhan nhanh jgid==null /
     * jgzt==null khi dung null, chuoi rong se lam tat ca nhanh loc.
     * QUYET DINH TACH cljxc: cljxc (getcljxcmanagement(+sums),
     * getmaterialmanagement(+sums)) GIU NGUYEN hanh vi iter65 (co
     * institution -> tra hang institution), khong dong trong commit
     * nay; chi ho ypjxc/stuffsummary moi tach institution khoi loc.
     * Hang institution van xem duoc bang cach yeu cau truc tiep
     * companyId = institution (guard cho phep cung parent).
     */
    private void normalizeYpjxcFacilityFilter(YpjxcRc ypjxcRc) {
        verifyYpjxcCompanyScope(ypjxcRc.getCompanyId());
        if (StringUtils.isBlank(ypjxcRc.getJgid())) {
            ypjxcRc.setJgid(null);
        } else {
            verifyYpjxcCompanyScope(ypjxcRc.getJgid());
        }
        if (StringUtils.isBlank(ypjxcRc.getJgzt())) {
            ypjxcRc.setJgzt(null);
        }
    }

    private void normalizeYpjxcFacilityFilter(StuffsalessummaryRc stuffsalessummaryRc) {
        verifyYpjxcCompanyScope(stuffsalessummaryRc.getCompanyId());
        if (StringUtils.isBlank(stuffsalessummaryRc.getJgid())) {
            stuffsalessummaryRc.setJgid(null);
        } else {
            verifyYpjxcCompanyScope(stuffsalessummaryRc.getJgid());
        }
        if (StringUtils.isBlank(stuffsalessummaryRc.getJgzt())) {
            stuffsalessummaryRc.setJgzt(null);
        }
    }

    private void verifyCompanyInSessionGroup(String tenantId, String companyId) {
        if (StringUtils.isBlank(companyId)) {
            return;
        }
        if (tenantId.equals(companyId)) {
            return;
        }
        String tenantParent = companyService.getCompanyParentId(tenantId);
        String companyParent = companyService.getCompanyParentId(companyId);
        if (tenantParent == null || companyParent == null) {
            throw new ServiceException("Không có quyền truy cập dữ liệu kho của phòng khám khác nhóm");
        }
        boolean sameGroup;
        if ("0".equals(tenantParent)) {
            sameGroup = tenantId.equals(companyParent);
        } else {
            sameGroup = tenantParent.equals(companyParent);
        }
        if (!sameGroup) {
            throw new ServiceException("Không có quyền truy cập dữ liệu kho của phòng khám khác nhóm");
        }
    }

    /**
     * DS201: escape LIKE wildcards (%, _, \) in the drug/material report
     * filters (mirror DS200 patientName) — "%" must match literally, not
     * every drug/batch. Same Rc bean feeds the count + list statements,
     * so escape once here, at the top of each leaf method; the
     * getpharmaceuticalInventoryManagement / getmaterialmanagement /
     * drugmaterialsstockmanagement / getconsumablemarketstatistics
     * wrappers delegate to these leaves and are covered automatically.
     */
    private void escapeYpjxcLikeFilters(YpjxcRc ypjxcRc) {
        ypjxcRc.setSpm(StringUtils.escapeLike(ypjxcRc.getSpm()));
        ypjxcRc.setPh(StringUtils.escapeLike(ypjxcRc.getPh()));
    }

    private void escapeStuffSummaryLikeFilters(
            StuffsalessummaryRc stuffsalessummaryRc) {
        stuffsalessummaryRc.setClmc(
                StringUtils.escapeLike(stuffsalessummaryRc.getClmc()));
    }

    /**
     * Thông tin nhập - xuất - tồn của thuốc
     * @param ypjxcRc
     * @return
     */
    public Page<Ypjxc> getypjxcmanagement(YpjxcRc ypjxcRc){
        // DS204: chan limit vuot tran 10000 / limit am / offset am
        // (PageRequest.MAX_PAGE_SIZE) — truoc day limit client di thang
        // vao LIMIT #{limit} o getypjxcmanagement khong co guard nao.
        // Wrapper getpharmaceuticalInventoryManagement uy quyen xuong day
        // nen duoc phu tu dong.
        if (!ypjxcRc.isValidLimit()) {
            throw new IllegalArgumentException("Invalid limit: " + ypjxcRc.getLimit());
        }
        if (!ypjxcRc.isValidOffset()) {
            throw new IllegalArgumentException("Invalid offset: " + ypjxcRc.getOffset());
        }
        escapeYpjxcLikeFilters(ypjxcRc);
        // ITER66 item 5-followup: khong ghi de jgid = institution (xem
        // normalizeYpjxcFacilityFilter) — query luon loc theo companyId.
        normalizeYpjxcFacilityFilter(ypjxcRc);
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
        escapeYpjxcLikeFilters(ypjxcRc);
        // ITER66 item 5-followup: khong ghi de jgid = institution (xem
        // normalizeYpjxcFacilityFilter) — query luon loc theo companyId.
        normalizeYpjxcFacilityFilter(ypjxcRc);
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
        // DS204: chan limit vuot tran 10000 / limit am / offset am
        // (PageRequest.MAX_PAGE_SIZE) — truoc day limit client di thang
        // vao LIMIT #{limit} o getStuffsalessummarylist khong co guard
        // nao. Wrapper getconsumablemarketstatistics uy quyen xuong day
        // nen duoc phu tu dong.
        if (!stuffsalessummaryRc.isValidLimit()) {
            throw new IllegalArgumentException("Invalid limit: " + stuffsalessummaryRc.getLimit());
        }
        if (!stuffsalessummaryRc.isValidOffset()) {
            throw new IllegalArgumentException("Invalid offset: " + stuffsalessummaryRc.getOffset());
        }
        escapeStuffSummaryLikeFilters(stuffsalessummaryRc);
        // ITER66 item 5-followup: khong ghi de jgid = institution (xem
        // normalizeYpjxcFacilityFilter) — query luon loc theo companyId.
        normalizeYpjxcFacilityFilter(stuffsalessummaryRc);
        int total = tollInfoDao.countStuffSalesSummary(stuffsalessummaryRc);
        List<Stuffsalessummary> list2 = null;
        if(total > 0){
            list2 = tollInfoDao.getStuffsalessummarylist(stuffsalessummaryRc);
        }
        return new Page<>((long) total, list2);
    }

    public Page<Stuffsalessummary> getStuffsalessummarysumss(StuffsalessummaryRc stuffsalessummaryRc){
        escapeStuffSummaryLikeFilters(stuffsalessummaryRc);
        // ITER66 item 5-followup: khong ghi de jgid = institution (xem
        // normalizeYpjxcFacilityFilter) — query luon loc theo companyId.
        normalizeYpjxcFacilityFilter(stuffsalessummaryRc);
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
        // DS204: chan limit vuot tran 10000 / limit am / offset am
        // (PageRequest.MAX_PAGE_SIZE) — truoc day limit client di thang
        // vao LIMIT #{limit} o getcljxcmanagement khong co guard nao.
        // Wrapper getmaterialmanagement uy quyen xuong day nen duoc phu
        // tu dong.
        if (!ypjxcRc.isValidLimit()) {
            throw new IllegalArgumentException("Invalid limit: " + ypjxcRc.getLimit());
        }
        if (!ypjxcRc.isValidOffset()) {
            throw new IllegalArgumentException("Invalid offset: " + ypjxcRc.getOffset());
        }
        escapeYpjxcLikeFilters(ypjxcRc);
        verifyYpjxcCompanyScope(ypjxcRc.getCompanyId());
        // ITER65 item 2: count/list cua cljxc KHONG loc company khi jgid
        // null (nhanh tautology a.company_id = oc.id) — guard admission
        // rieng no khong du, phai chot scope that su truy van: jgid trong
        // body (dropdown chon co so, da kiem chung cung-parent) neu co,
        // neu khong thi institution cua companyId neu co, neu khong thi
        // companyId; ep jgzt = "1" de nhanh a.company_id = #{jgid} chay.
        // Giua nguyen hanh vi institution khi co institution (trang
        // material o deployment co institution van tra hang cua
        // institution); khi khong co institution thi tra hang cua
        // companyId thay vi toan bo nhu truoc (do la lo).
        String requestedJgid = ypjxcRc.getJgid();
        if (StringUtils.isNotBlank(requestedJgid)) {
            verifyYpjxcCompanyScope(requestedJgid);
        }
        String institution = companyService.getInstitution(ypjxcRc.getCompanyId());
        if (StringUtils.isNotBlank(requestedJgid)) {
            ypjxcRc.setJgid(requestedJgid);
        } else if (institution != null) {
            ypjxcRc.setJgid(institution);
        } else {
            ypjxcRc.setJgid(ypjxcRc.getCompanyId());
        }
        ypjxcRc.setJgzt("1");
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
        escapeYpjxcLikeFilters(ypjxcRc);
        verifyYpjxcCompanyScope(ypjxcRc.getCompanyId());
        // ITER65 item 2: chot scope that su truy van nhu
        // getcljxcmanagement (count/list cua cljxc khong loc company khi
        // jgid null) — xem ghi chu tai getcljxcmanagement.
        String requestedJgid = ypjxcRc.getJgid();
        if (StringUtils.isNotBlank(requestedJgid)) {
            verifyYpjxcCompanyScope(requestedJgid);
        }
        String institution = companyService.getInstitution(ypjxcRc.getCompanyId());
        if (StringUtils.isNotBlank(requestedJgid)) {
            ypjxcRc.setJgid(requestedJgid);
        } else if (institution != null) {
            ypjxcRc.setJgid(institution);
        } else {
            ypjxcRc.setJgid(ypjxcRc.getCompanyId());
        }
        ypjxcRc.setJgzt("1");
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
        // DS204: chan limit vuot tran 10000 / limit am / offset am
        // (PageRequest.MAX_PAGE_SIZE) — truoc day limit client di thang
        // vao LIMIT #{limit} o getypclrkcxlist khong co guard nao.
        // Wrapper drugmaterialsstockmanagement uy quyen xuong day nen
        // duoc phu tu dong.
        if (!ypjxcRc.isValidLimit()) {
            throw new IllegalArgumentException("Invalid limit: " + ypjxcRc.getLimit());
        }
        if (!ypjxcRc.isValidOffset()) {
            throw new IllegalArgumentException("Invalid offset: " + ypjxcRc.getOffset());
        }
        escapeYpjxcLikeFilters(ypjxcRc);
        verifyYpjxcCompanyAndJgidScope(ypjxcRc.getCompanyId(), ypjxcRc.getJgid());
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
        escapeYpjxcLikeFilters(ypjxcRc);
        verifyYpjxcCompanyAndJgidScope(ypjxcRc.getCompanyId(), ypjxcRc.getJgid());
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
        // ITER65 item 2: chi liet ke nhom cua session tenant (ep companyId
        // = session tenant truoc khi lookup sibling, theo plan §2(c)).
        // Voi caller cung nhom thi danh sach khong doi (cung parent_id);
        // voi companyId la thi khong ro ri danh muc nhom la.
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
            ypjxcRc.setCompanyId(tenantId);
        }
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
        // W2: ep tenant session (doctorParams copy sau cung ke thua filter).
        // DS188: nhu getWorkLoadStat — params null (body {}) NPE 500 o
        // new ArrayList<>(params) (:1725) khi total > 0; chuan hoa ve rong.
        if (params == null) { params = new ArrayList<>(); }
        ensureCompanyFilter(params);
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
        // W2: ep tenant session nhu getdoctorDetailstatistics.
        // DS188: nhu getWorkLoadStat — params null (body {}) NPE 500 o
        // parameters.addAll(params) (:1763) khi co du lieu; chuan hoa ve rong.
        if (params == null) { params = new ArrayList<>(); }
        ensureCompanyFilter(params);
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
        // DS203: getDrugSales tran limit 10000 (PageRequest.MAX_PAGE_SIZE)
        // nen khong the lay het 1000000 dong mot lan nhu truoc — lay theo
        // trang 10000 dong roi gop lai.
        List<DrugSales> allDrugSales = new ArrayList<>();
        int exportOffset = 0;
        while (true) {
            Page<DrugSales> drugSalesPage = getDrugSales(searchParams.getParams(), exportOffset, PageRequest.MAX_PAGE_SIZE, searchParams.getOrder());
            if (CollectionUtils.isEmpty(drugSalesPage.getRows())) {
                break;
            }
            allDrugSales.addAll(drugSalesPage.getRows());
            if (drugSalesPage.getRows().size() < PageRequest.MAX_PAGE_SIZE) {
                break;
            }
            exportOffset += PageRequest.MAX_PAGE_SIZE;
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!CollectionUtils.isEmpty(allDrugSales)) {
            for (DrugSales ds : allDrugSales) {
                Map<String, Object> row = new HashMap<>();
                row.put("type", ds.getType());
                row.put("name", ds.getName());
                // Quy cách = Liều lượng+Đơn vị liều lượng*Số lượng+Số lượngĐơn vị/Đóng gói
                String norms = ds.getDosis() + ds.getDosisUnit() + "*" + ds.getPreparation() + ds.getPreparationUnit() + "/" + ds.getPack();
                row.put("norms", norms);
                // Số lượngquy đổi. ITER66 item 2: preparation trong DB là
                // chuỗi thập phân ('1.0'/'100.0'/'6.0') nên parseInt vỡ
                // toàn bộ file (NumberFormatException tại đây, đã probe
                // pre-fix); tái dùng parseExportCount (BigDecimal,
                // null/rỗng/không số -> 0) như exportDrugStock ở iter65
                // item 2, prep <= 0 thì giữ nguyên tổng, không chia 0.
                int prep = parseExportCount(ds.getPreparation());
                int wholePacks = prep > 0 ? ds.getTotal() / prep : ds.getTotal();
                int remainder = prep > 0 ? ds.getTotal() % prep : 0;
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
        // DS203: nhu exportDrugDetail — lay theo trang 10000 dong
        // (PageRequest.MAX_PAGE_SIZE) roi gop lai, thay cho 1000000 dong.
        List<WorkLoad> allWorkloads = new ArrayList<>();
        int exportOffset = 0;
        while (true) {
            Page<WorkLoad> workloadPage = getWorkload(searchParams.getParams(), exportOffset, PageRequest.MAX_PAGE_SIZE, searchParams.getOrder());
            if (CollectionUtils.isEmpty(workloadPage.getRows())) {
                break;
            }
            allWorkloads.addAll(workloadPage.getRows());
            if (workloadPage.getRows().size() < PageRequest.MAX_PAGE_SIZE) {
                break;
            }
            exportOffset += PageRequest.MAX_PAGE_SIZE;
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!CollectionUtils.isEmpty(allWorkloads)) {
            for (WorkLoad wl : allWorkloads) {
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

    // ITER65 item 2: parse so luong export kieu decimal/null-safe (xem
    // ghi chu tai cho goi trong exportDrugStock).
    private int parseExportCount(String value) {
        if (StringUtils.isBlank(value)) {
            return 0;
        }
        try {
            return new BigDecimal(value.trim()).intValue();
        } catch (Exception e) {
            return 0;
        }
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
                // Số lượngquy đổi. So luong trong DB la decimal
                // ("100.0000"): parse qua BigDecimal thay vi
                // Integer.parseInt (parseInt nem NumberFormatException lam
                // vo export moi khi co dong du lieu — loi san co, phat
                // hien o probe iter65 item 2). zj null/rong/khong so thi
                // giu nguyen sl (truoc day la throw -> khong co file).
                int sl = parseExportCount(item.getSl());
                int zj = parseExportCount(item.getZj());
                int wholePacks = zj > 0 ? sl / zj : sl;
                int remainder = zj > 0 ? sl % zj : 0;
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