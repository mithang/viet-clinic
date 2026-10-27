package com.geeke.toll.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.service.ServiceException;
import com.geeke.common.data.Page;
import com.geeke.outpatient.entity.*;
import com.geeke.outpatient.service.MedicalRecordService;
import com.geeke.outpatient.service.RecipelInfoService;
import com.geeke.outpatient.service.RegistrationService;
import com.geeke.toll.entity.*;
import com.geeke.toll.service.TollInfoService;
import com.geeke.utils.ResultUtil;
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
        TollInfo entity = tollInfoService.getTollInfoByRegistrationId(registrationId);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    @Override
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@RequestBody TollInfo entity) {
        if(BizConstants.AMOUNT_STATUS_REFUNDED.equals(entity.getAmountStatus().getValue())){
            TollInfo tollInfoByRegistrationId = tollInfoService.getTollInfoByRegistrationId(entity.getRegistrationFeeId());
            tollInfoByRegistrationId.setReturnType(1);
            tollInfoService.save(tollInfoByRegistrationId);
            entity.setReturnType(1);
        }
        String id = tollInfoService.save(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @PostMapping(value = "tollTotalForm")
    public ResponseEntity<JSONObject> tollTotalForm(@RequestBody SearchParams searchParams) {
        return tollInfoService.tollTotalForm(searchParams);
    }

    @PostMapping(value = "tollDetailForm")
    public ResponseEntity<JSONObject> tollDetailForm(@RequestBody SearchParams searchParams) {
        return tollInfoService.tollDetailForm(searchParams);
    }

    //Theo缴phíThông tinTruy vấnThông tin đăng ký khám
    @GetMapping("registration/{chargeStatus}")
    public ResponseEntity<JSONObject> listRegistrationPage(@PathVariable("chargeStatus") String chargeStatus) {
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
        tollInfoService.saveToll(tollEvt,type);
        return ResponseEntity.ok(ResultUtil.successJson(""));
    }

    @PostMapping(value = "getCreateBy")
    public ResponseEntity<JSONObject> getCreateBy(@RequestBody SearchParams searchParams) {
        List<TollInfo> result = tollInfoService.getCreateBy(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "getWorkload")
    public ResponseEntity<JSONObject> getWorkload(@RequestBody SearchParams searchParams) {
        Page<WorkLoad> workload = tollInfoService.getWorkload(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(workload));
    }

    @PostMapping(value = "getDrugSales")
    public ResponseEntity<JSONObject> getDrugSales(@RequestBody SearchParams searchParams) {
        final Page<DrugSales> drugSales = tollInfoService.getDrugSales(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(drugSales));
    }

    @PostMapping(value = "getWorkLoadStat")
    public ResponseEntity<JSONObject> getWorkLoadStat(@RequestBody SearchParams searchParams){
        WorkLoadStat workLoadStat = tollInfoService.getWorkLoadStat(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(workLoadStat));
    }

    @PostMapping(value = "getDrugSalesStat")
    public ResponseEntity<JSONObject> getDrugSalesStat(@RequestBody SearchParams searchParams) {
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
        tollInfoService.exportDrugOrStuffStock(ypjxcRc,response);
    }


    /**
     * Quản lý cơ sở-Thông tin nhập - xuất - tồn của thuốc
     * @param ypjxcRc
     * @return
     */
    @PostMapping(value = "getpharmaceuticalInventoryManagement")
    public ResponseEntity<JSONObject> getpharmaceuticalInventoryManagement(@RequestBody YpjxcRc ypjxcRc){
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
        Page<Ypjxc> result=tollInfoService.getpharmaceuticalInventoryManagementsums(ypjxcRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /*
    Lấytenant底dưới của Phòng khám
     */
    @PostMapping(value = "getjglist")
    public ResponseEntity<JSONObject> getjglist(@RequestBody YpjxcRc ypjxcRc){
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
        Page<Stuffsalessummary> result=tollInfoService.getconsumablemarketstatisticssum(stuffsalessummaryRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "getdrugmarketstatistics")
    public ResponseEntity<JSONObject> getdrugmarketstatistics(@RequestBody SearchParams searchParams) {
        final Page<DrugSales> drugSales = tollInfoService.getdrugmarketstatistics(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(drugSales));
    }
    @PostMapping(value = "getdrugmarketstatisticsStat")
    public ResponseEntity<JSONObject> getdrugmarketstatisticsStat(@RequestBody SearchParams searchParams) {
        DrugSales drugSalesStat = tollInfoService.getdrugmarketstatisticsStat(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(drugSalesStat));
    }


    /*
    Quản lý cơ sở-Bác sĩThu nhập
     */
    @PostMapping(value = "getdoctorDetailstatistics")
    public ResponseEntity<JSONObject> getdoctorDetailstatistics(@RequestBody SearchParams searchParams) {
        Page<WorkLoad> workload = tollInfoService.getdoctorDetailstatistics(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(workload));
    }

    /*
    Quản lý cơ sở-Bác sĩThu nhập
     */
    @PostMapping(value = "getdoctorDetailstatisticsStat")
    public ResponseEntity<JSONObject> getdoctorDetailstatisticsStat(@RequestBody SearchParams searchParams){
        WorkLoadStat workLoadStat = tollInfoService.getdoctorDetailstatisticsStat(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(workLoadStat));
    }

    @PostMapping(value = "orgtolldetail")
    public ResponseEntity<JSONObject> orgtolldetail(@RequestBody SearchParams searchParams) {
        return tollInfoService.orgtolldetail(searchParams);
    }


    @PostMapping(value = "orgtollDetailForm")
    public ResponseEntity<JSONObject> orgtollDetailForm(@RequestBody SearchParams searchParams) {
        return tollInfoService.orgtollDetailForm(searchParams);
    }

}
