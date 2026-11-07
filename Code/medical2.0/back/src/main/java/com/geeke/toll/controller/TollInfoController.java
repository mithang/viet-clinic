package com.geeke.toll.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.persistence.DataEntity;
import com.geeke.common.service.ServiceException;
import com.geeke.common.data.Page;
import com.geeke.outpatient.entity.*;
import com.geeke.outpatient.service.MedicalRecordService;
import com.geeke.outpatient.service.RecipelInfoService;
import com.geeke.outpatient.service.RegistrationService;
import com.geeke.org.entity.Company;
import com.geeke.toll.entity.*;
import com.geeke.toll.service.TollInfoService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Quản lý thu phíController
 * @author lc
 * @version 2022-06-22
 */
@RestController
@RequestMapping(value = "/toll/tollInfo")
public class TollInfoController extends CrudController<TollInfoService, TollInfo> {

	@Autowired
	protected TollInfoService tollInfoService;
	@Autowired
    private RecipelInfoService recipelInfoService;
	@Autowired
    private RegistrationService registrationService;
	@Autowired
    private MedicalRecordService medicalRecordService;

    @Override
    protected TollInfoService getService() {
        return tollInfoService;
    }

    @GetMapping("/getTollInfoByRegistrationId/{registrationId}")
    public ResponseEntity<JSONObject> getTollInfoByRegistrationId(@PathVariable("registrationId") String registrationId) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        TollInfo entity = tollInfoService.getTollInfoByRegistrationId(registrationId);
        // DS9: phieu thu cua phong kham khac -> 403, khong tra du lieu.
        // DAO khong loc company (WHERE registration_fee_id=... tran) nen
        // guard o day, theo mau SysFileContentController (file la -> 403).
        // Chi guard endpoint GET nay; luong save() noi bo goi service truc
        // tiep nen khong doi. Session rong/"null" (luong noi bo) va dong
        // company rong giu hanh vi cu, parity verifyTenantOwnership.
        if (entity != null && entity.getCompany() != null) {
            String tenant = SessionUtils.getLoginTenantId();
            String companyId = entity.getCompany().getId();
            if (StringUtils.isNotBlank(tenant) && !"null".equals(tenant)
                    && StringUtils.isNotBlank(companyId) && !tenant.equals(companyId)) {
                return forbidden();
            }
        }
        // DS10: phieu da xoa mem (del_flag=1, vd H6 huy phi) khong tra
        // qua GET nay — tra null nhu khong ton tai. Chi loc o controller
        // GET; DAO/service khong doi vi nhanh save() REFUNDED goi service
        // truc tiep va khong chiu duoc null (setReturnType tren null se
        // NPE), nen loc o DAO se lam hong luong hoan tien.
        if (entity != null && DataEntity.DEL_FLAG_DELETE.equals(entity.getDelFlag())) {
            return ResponseEntity.ok(ResultUtil.successJson(null));
        }
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    // K2 (iteration 58): ep tenant theo session, theo mau D1/E1/K1.
    // Body khong duoc dieu khien tenant nua: session rong/"null" (luong
    // noi bo khong session) moi giu gia tri body cu.
    private String sessionTenantOr(String fallback) {
        String tenant = SessionUtils.getLoginTenantId();
        return (StringUtils.isNotBlank(tenant) && !"null".equals(tenant)) ? tenant : fallback;
    }

    @Override
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@RequestBody TollInfo entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        // K2 (iteration 58): ep tenant theo session cho NHANH INSERT
        // (id trong), mau K1/D1/E1, truoc moi ghi ke ca nhanh REFUNDED
        // ben duoi (nhanh do chi dung registrationFeeId de load dong cu,
        // khong dung company cua entity, nen ep truoc khong doi hanh vi
        // cua no — case R kiem chung). Truoc day payload id trong +
        // company tenant la tao that phieu thu o tenant la (nhanh insert
        // cua CrudService.save khong verify tenant), trong khi so phieu
        // lai sinh theo sequence cua SESSION company (tai hien
        // tools/iter58-k2.py: so ARK-... cua OWN nhung dong o OTHER).
        // Nhanh UPDATE KHONG ep: guard stored-row cua CrudService.save
        // van chan payload company la (case C van 400 + dong van OWN).
        if (StringUtils.isBlank(entity.getId())) {
            String tenant = sessionTenantOr(entity.getCompany() == null ? null : entity.getCompany().getId());
            if (tenant != null) {
                entity.setCompany(new Company(tenant));
            }
        }
        // DS12: amountStatus la field @NotNull nhung controller khong co
        // @Valid, nen payload thieu amountStatus truoc day NPE 500 o dieu
        // kien nhanh REFUNDED ben duoi. Fail-closed 400 thay vi bo qua
        // (bo qua se bat dau insert phieu khong co trang thai — chat hon
        // hanh vi hien tai, von tu choi payload dang nay, chi sai tin
        // hieu). Chi anh huong POST /toll/tollInfo/save; cac luong noi
        // bo goi TollInfoService.save truc tiep khong qua day.
        if (entity.getAmountStatus() == null) {
            throw new ServiceException("Trạng thái phiếu thu không được để trống");
        }
        if(BizConstants.AMOUNT_STATUS_REFUNDED.equals(entity.getAmountStatus().getValue())){
            TollInfo tollInfoByRegistrationId = tollInfoService.getTollInfoByRegistrationId(entity.getRegistrationFeeId());
            // DS11: khong co phieu thu goc cho registration nay (chua thu
            // hoac registration khong ton tai) thi fail-closed 400 thay vi
            // NPE 500 o setReturnType ben duoi. Khong ghi dong nao.
            if (tollInfoByRegistrationId == null) {
                throw new ServiceException("Không tìm thấy phiếu thu gốc của đăng ký này để hoàn phí");
            }
            tollInfoByRegistrationId.setReturnType(1);
            tollInfoService.save(tollInfoByRegistrationId);
            entity.setReturnType(1);
        } else {
            // DS122 C: paymentType / amountStatus / tollType la @NotNull
            // tren entity nhung controller save khong co @Valid (chi check
            // amountStatus, DS12) nen payload thieu truoc day insert phieu
            // voi NULL (200). Chan o nhanh thuong; nhanh REFUNDED tren
            // duoc mien (phieu hoan chi mang amountStatus, hanh vi da
            // duoc ds11 pin, khong co caller UI).
            if (entity.getPaymentType() == null
                    || StringUtils.isBlank(entity.getPaymentType().getValue())
                    || entity.getTollType() == null
                    || StringUtils.isBlank(entity.getTollType().getValue())) {
                throw new ServiceException("Loại thanh toán và loại thu phí không được để trống");
            }
        }
        String id = tollInfoService.save(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @PostMapping(value = "tollTotalForm")
    public ResponseEntity<JSONObject> tollTotalForm(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        return tollInfoService.tollTotalForm(searchParams);
    }

    @PostMapping(value = "tollDetailForm")
    public ResponseEntity<JSONObject> tollDetailForm(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        return tollInfoService.tollDetailForm(searchParams);
    }

    //TheonộpphíThông tinTruy vấnThông tin đăng ký khám
    @GetMapping("registration/{chargeStatus}")
    public ResponseEntity<JSONObject> listRegistrationPage(@PathVariable("chargeStatus") String chargeStatus) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        List<MedicalRecord> medicalRecords = medicalRecordService.listMedicalRecordByChargeStatus(chargeStatus);
        List<Registration> registrations = medicalRecords.stream().map(m -> m.getRegistration()).collect(Collectors.toList());
        Page<Registration> result = new Page<>(registrations.size(),registrations);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    //Lấy đơn thuốc thuộc thông tin đăng ký khám theo trạng thái nộp phí
    @PostMapping(value = "medicalRecipelEvt/{chargeStatus}")
    public ResponseEntity<JSONObject> medicalRecipelEvt(@RequestBody Registration registration,
                                               @PathVariable("chargeStatus") String chargeStatus)
    {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        if(StringUtils.isNullOrEmpty(chargeStatus))
        {
            throw new ServiceException("Tham sốcósai");
        }
        MedicalRecipelEvt medicalRecipelEvt = medicalRecordService.allQuery(registration,chargeStatus);
        return ResponseEntity.ok(ResultUtil.successJson(medicalRecipelEvt));
    }

    //Thu phígiao diện -- tăngtype
    @PostMapping(value = "save/toll/{type}")
    public ResponseEntity<JSONObject> saveToll(@RequestBody @NotNull TollEvt tollEvt,
                                               @PathVariable("type") String type) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        tollInfoService.saveToll(tollEvt,type);
        return ResponseEntity.ok(ResultUtil.successJson(""));
    }

    // Hủy phiếu thu chưa thu (H6): chỉ phiếu amountStatus_0 + thực thu 0
    // mới hủy được; các trường hợp khác service ném ServiceException
    // (global handler trả 400 kèm thông báo tiếng Việt). Fail-closed
    // như các endpoint tùy chỉnh khác của controller này.
    @PostMapping(value = "cancel/{id}")
    public ResponseEntity<JSONObject> cancelUnpaidToll(@PathVariable("id") String id) {
        if (!hasAnySessionPermission()) { return forbidden(); }
        TollInfo cancelled = tollInfoService.cancelUnpaidToll(id);
        return ResponseEntity.ok(ResultUtil.successJson(cancelled.getTollNumber()));
    }

    @PostMapping(value = "getCreateBy")
    public ResponseEntity<JSONObject> getCreateBy(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        List<TollInfo> result = tollInfoService.getCreateBy(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "getWorkload")
    public ResponseEntity<JSONObject> getWorkload(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<WorkLoad> workload = tollInfoService.getWorkload(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(workload));
    }

    @PostMapping(value = "getDrugSales")
    public ResponseEntity<JSONObject> getDrugSales(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        final Page<DrugSales> drugSales = tollInfoService.getDrugSales(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(drugSales));
    }

    @PostMapping(value = "getWorkLoadStat")
    public ResponseEntity<JSONObject> getWorkLoadStat(@RequestBody SearchParams searchParams){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        WorkLoadStat workLoadStat = tollInfoService.getWorkLoadStat(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(workLoadStat));
    }

    @PostMapping(value = "getDrugSalesStat")
    public ResponseEntity<JSONObject> getDrugSalesStat(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        DrugSales drugSalesStat = tollInfoService.getDrugSalesStat(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(drugSalesStat));
    }

    /**
     * Thông tin nhập - xuất - tồn của thuốc
     * @param ypjxcRc
     * @return
     */
    @PostMapping(value = "getypjxcmanagement")
    public ResponseEntity<JSONObject> getypjxcmanagement(@RequestBody YpjxcRc ypjxcRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Ypjxc> result=tollInfoService.getypjxcmanagement(ypjxcRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Lấy tổng hợp số lượng và số tiền nhập - xuất - tồn của thuốc
     * @param ypjxcRc
     * @return
     */
    @PostMapping(value = "getypjxcmanagementsums")
    public ResponseEntity<JSONObject> getypjxcmanagementsums(@RequestBody YpjxcRc ypjxcRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Ypjxc> result=tollInfoService.getypjxcmanagementsums(ypjxcRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Lấyvật tư tiêu haobán hàngThông tin
     * @param stuffsalessummaryRc
     * @return
     */
    @PostMapping(value = "getStuffsalessummarylists")
    public ResponseEntity<JSONObject> getStuffsalessummarylists(@RequestBody StuffsalessummaryRc stuffsalessummaryRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Stuffsalessummary> result=tollInfoService.getStuffsalessummarylists(stuffsalessummaryRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Lấy tổng hợp giá và số lượng bán vật tư tiêu hao
     * @param stuffsalessummaryRc
     * @return
     */
    @PostMapping(value = "getStuffsalessummarysumss")
    public ResponseEntity<JSONObject> getStuffsalessummarysumss(@RequestBody StuffsalessummaryRc stuffsalessummaryRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.

        Page<Stuffsalessummary> result=tollInfoService.getStuffsalessummarysumss(stuffsalessummaryRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }


    /**
     * Thông tin nhập - xuất - tồn của vật tư
     * @param ypjxcRc
     * @return
     */
    @PostMapping(value = "getcljxcmanagement")
    public ResponseEntity<JSONObject> getcljxcmanagement(@RequestBody YpjxcRc ypjxcRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Ypjxc> result=tollInfoService.getcljxcmanagement(ypjxcRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Lấy tổng hợp số lượng và số tiền nhập - xuất - tồn của vật tư
     * @param ypjxcRc
     * @return
     */
    @PostMapping(value = "getcljxcmanagementsums")
    public ResponseEntity<JSONObject> getcljxcmanagementsums(@RequestBody YpjxcRc ypjxcRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Ypjxc> result=tollInfoService.getcljxcmanagementsums(ypjxcRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Thống kê thông tin nhập kho thuốc và vật tư
     * @param ypjxcRc
     * @return
     */
    @PostMapping(value = "getypclrkcxlist")
    public ResponseEntity<JSONObject> getypclrkcxlist(@RequestBody YpjxcRc ypjxcRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Ypclrkcx> result=tollInfoService.getypclrkcxlist(ypjxcRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Tổng hợp thông tin nhập kho thuốc và vật tư
     * @param ypjxcRc
     * @return
     */
    @PostMapping(value = "getypclrkcxsums")
    public ResponseEntity<JSONObject> getypclrkcxsums(@RequestBody YpjxcRc ypjxcRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Ypclrkcx> result=tollInfoService.getypclrkcxsums(ypjxcRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * báo cáoXuất
     * @param searchParams
     * @param response
     * @param request
     * @throws IOException
     */
    @PostMapping("exportExcel")
    public void exportExcel(@RequestBody SearchParams searchParams, HttpServletResponse response, HttpServletRequest request) throws IOException {
        if (!hasAnySessionPermission()) { response.setStatus(HttpServletResponse.SC_FORBIDDEN); return; } // San RBAC W1 (void export: 403, khong ghi file).
        tollInfoService.exportExcel(searchParams,response);
    }


    /**
     *
     * @param ypjxcRc
     * @param response
     * @param request
     */
    @PostMapping("exportDrugOrStuffStock")
    public void exportDrugOrStuffStock(@RequestBody YpjxcRc ypjxcRc,HttpServletResponse response,HttpServletRequest request) throws IOException {
        if (!hasAnySessionPermission()) { response.setStatus(HttpServletResponse.SC_FORBIDDEN); return; } // San RBAC W1 (void export: 403, khong ghi file).
        tollInfoService.exportDrugOrStuffStock(ypjxcRc,response);
    }


    /**
     * Quản lý cơ sở-Thông tin nhập - xuất - tồn của thuốc
     * @param ypjxcRc
     * @return
     */
    @PostMapping(value = "getpharmaceuticalInventoryManagement")
    public ResponseEntity<JSONObject> getpharmaceuticalInventoryManagement(@RequestBody YpjxcRc ypjxcRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Ypjxc> result=tollInfoService.getpharmaceuticalInventoryManagement(ypjxcRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Quản lý cơ sở-Lấy tổng hợp số lượng và số tiền nhập - xuất - tồn của thuốc
     * @param ypjxcRc
     * @return
     */
    @PostMapping(value = "getpharmaceuticalInventoryManagementsums")
    public ResponseEntity<JSONObject> getpharmaceuticalInventoryManagementsums(@RequestBody YpjxcRc ypjxcRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Ypjxc> result=tollInfoService.getpharmaceuticalInventoryManagementsums(ypjxcRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /*
    Lấy các phòng khám thuộc tenant
     */
    @PostMapping(value = "getjglist")
    public ResponseEntity<JSONObject> getjglist(@RequestBody YpjxcRc ypjxcRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        List<Ypjxc> result=tollInfoService.getjglist(ypjxcRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Quản lý cơ sở-Thông tin nhập - xuất - tồn của vật tư
     * @param ypjxcRc
     * @return
     */
    @PostMapping(value = "getmaterialmanagement")
    public ResponseEntity<JSONObject> getmaterialmanagement(@RequestBody YpjxcRc ypjxcRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Ypjxc> result=tollInfoService.getmaterialmanagement(ypjxcRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Quản lý cơ sở-Lấy tổng hợp số lượng và số tiền nhập - xuất - tồn của vật tư
     * @param ypjxcRc
     * @return
     */
    @PostMapping(value = "getmaterialmanagementsums")
    public ResponseEntity<JSONObject> getmaterialmanagementsums(@RequestBody YpjxcRc ypjxcRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Ypjxc> result=tollInfoService.getmaterialmanagementsums(ypjxcRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Thống kê thông tin nhập kho thuốc và vật tư
     * @param ypjxcRc
     * @return
     */
    @PostMapping(value = "drugmaterialsstockmanagement")
    public ResponseEntity<JSONObject> drugmaterialsstockmanagement(@RequestBody YpjxcRc ypjxcRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Ypclrkcx> result=tollInfoService.drugmaterialsstockmanagement(ypjxcRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Tổng hợp thông tin nhập kho thuốc và vật tư
     * @param ypjxcRc
     * @return
     */
    @PostMapping(value = "drugmaterialsstockmanagementsums")
    public ResponseEntity<JSONObject> drugmaterialsstockmanagementsums(@RequestBody YpjxcRc ypjxcRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Ypclrkcx> result=tollInfoService.drugmaterialsstockmanagementsums(ypjxcRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Quản lý cơ sở-Lấyvật tư tiêu haobán hàngThông tin
     * @param stuffsalessummaryRc
     * @return
     */
    @PostMapping(value = "getconsumablemarketstatistics")
    public ResponseEntity<JSONObject> getconsumablemarketstatistics(@RequestBody StuffsalessummaryRc stuffsalessummaryRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Stuffsalessummary> result=tollInfoService.getconsumablemarketstatistics(stuffsalessummaryRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Quản lý cơ sở-Lấy tổng hợp giá và số lượng bán vật tư tiêu hao
     * @param stuffsalessummaryRc
     * @return
     */
    @PostMapping(value = "getconsumablemarketstatisticssum")
    public ResponseEntity<JSONObject> getconsumablemarketstatisticssum(@RequestBody StuffsalessummaryRc stuffsalessummaryRc){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<Stuffsalessummary> result=tollInfoService.getconsumablemarketstatisticssum(stuffsalessummaryRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "getdrugmarketstatistics")
    public ResponseEntity<JSONObject> getdrugmarketstatistics(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        final Page<DrugSales> drugSales = tollInfoService.getdrugmarketstatistics(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(drugSales));
    }
    @PostMapping(value = "getdrugmarketstatisticsStat")
    public ResponseEntity<JSONObject> getdrugmarketstatisticsStat(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        DrugSales drugSalesStat = tollInfoService.getdrugmarketstatisticsStat(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(drugSalesStat));
    }


    /*
    Quản lý cơ sở-Bác sĩThu nhập
     */
    @PostMapping(value = "getdoctorDetailstatistics")
    public ResponseEntity<JSONObject> getdoctorDetailstatistics(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        Page<WorkLoad> workload = tollInfoService.getdoctorDetailstatistics(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(workload));
    }

    /*
    Quản lý cơ sở-Bác sĩThu nhập
     */
    @PostMapping(value = "getdoctorDetailstatisticsStat")
    public ResponseEntity<JSONObject> getdoctorDetailstatisticsStat(@RequestBody SearchParams searchParams){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        WorkLoadStat workLoadStat = tollInfoService.getdoctorDetailstatisticsStat(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(workLoadStat));
    }

    @PostMapping(value = "orgtolldetail")
    public ResponseEntity<JSONObject> orgtolldetail(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        return tollInfoService.orgtolldetail(searchParams);
    }


    @PostMapping(value = "orgtollDetailForm")
    public ResponseEntity<JSONObject> orgtollDetailForm(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC W1: tai khoan 0-resource bi 403, parity save/saveToll/cancel.
        return tollInfoService.orgtollDetailForm(searchParams);
    }

}
