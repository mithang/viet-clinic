package com.geeke.outpatient.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.service.ServiceException;
import com.geeke.common.data.Page;
import com.geeke.org.entity.Company;
import com.geeke.outpatient.entity.*;
import com.geeke.outpatient.service.*;
import com.geeke.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * Điền bệnh ánController
 * @author txl
 * @version 2022-06-13
 */
@RestController
@RequestMapping(value = "/outpatient/medicalRecord")
public class MedicalRecordController extends CrudController<MedicalRecordService, MedicalRecord> {

    @Autowired
    protected MedicalRecordService medicalRecordService;

    @Autowired
    private RemoteDiagnosisTreatmentService remoteDiagnosisTreatmentService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private XtZdService zdService;

    @Autowired
    private XtZyzhService xtZyzhService;

    @Override
    protected MedicalRecordService getService() {
        return medicalRecordService;
    }

    @GetMapping("/instant/{phone}")
    public ResponseEntity<JSONObject> getByPhone(@PathVariable("phone") String phone) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC T1: duong doc nhu duong ghi cung controller.
        Patient patient = patientService.getPatientByPhone(phone);
        return ResponseEntity.ok(ResultUtil.successJson(patient));
    }

    @GetMapping("/medical/{registration}")
    public ResponseEntity<JSONObject> getByOrder(@PathVariable("registration") String registration) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC T1: duong doc nhu duong ghi cung controller.
        RemoteDiagnosisTreatment entity = remoteDiagnosisTreatmentService.diagnosisById(registration);
        if (entity == null) { // C1: khong co dong remote_diagnosis -> tra list rong thay vi NPE.
            return ResponseEntity.ok(ResultUtil.successJson(java.util.Collections.emptyList()));
        }
        registration = entity.getRegistrationId();
        List<MedicalRecord> result = medicalRecordService.getByOrder(registration);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "saveWithFile")
    public ResponseEntity<JSONObject> saveWithFile(@RequestParam("entity") String strEntity,
      @RequestParam("fileIdUploads") MultipartFile[] fileIdUploads,  // Tệp: Tải lên tệp đính kèm
      @RequestParam("deleteIds")String strDeleteIds) throws java.io.IOException {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        MedicalRecord entity = JSONObject.parseObject(strEntity, MedicalRecord.class);
        String[] deleteIds = JSONObject.parseObject(strDeleteIds, String[].class);
        String id = medicalRecordService.save(entity,
            fileIdUploads,
            deleteIds
        ).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @PostMapping(value = "saveAdd")
    public ResponseEntity<JSONObject> save(@RequestBody MedicalRecord entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        RemoteDiagnosisTreatment remote = remoteDiagnosisTreatmentService.diagnosisById(entity.getDiagnosisId());
        // K4: dong remote khong ton tai -> 400 truoc moi ghi (truoc day NPE 500 o remote.getRegistrationId()).
        if (remote == null) {
            throw new ServiceException("Không tìm thấy chẩn đoán từ xa");
        }
        // K4: neo verify nhu J3/registerSave — get() verify tenant cua luot kham, luot la -> 400 truoc moi ghi.
        // Company cua case ep theo luot da verify, khong lay tu dong remote (mapper diagnosisById khong loc tenant).
        Registration registration = registrationService.get(remote.getRegistrationId());
        if (registration == null) {
            throw new ServiceException("Không tìm thấy lượt khám của chẩn đoán từ xa");
        }
        entity.setRegistration(registration);
        entity.setCompany(registration.getCompany());
        String id = medicalRecordService.save(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @PostMapping(value = "allSave")
    public ResponseEntity<JSONObject> allSave(@RequestParam("entity") String strEntity,
                                              @RequestParam(name = "fileIdUploads",required=false) MultipartFile[] fileIdUploads,  // Tệp: Tải lên tệp đính kèm
                                              @RequestParam(name = "deleteIds",required=false)String strDeleteIds) throws java.io.IOException {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        MedicalRecipelEvt medicalRecipelEvt = JSONObject.parseObject(strEntity, MedicalRecipelEvt.class);
        Registration registration = medicalRecordService.allSave(medicalRecipelEvt, fileIdUploads, strDeleteIds);
        return ResponseEntity.ok(ResultUtil.successJson(registration));
    }

    @PostMapping(value = "allQuery")
    public ResponseEntity<JSONObject> allQuery(@RequestBody Registration registration) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC T1: duong doc nhu duong ghi cung controller.
        MedicalRecipelEvt medicalRecipelEvt = medicalRecordService.allQuery(registration,"");
        return ResponseEntity.ok(ResultUtil.successJson(medicalRecipelEvt));
    }

    @GetMapping(value = "details/{infoId}")
    public ResponseEntity<JSONObject> recipelInfoEvt(@PathVariable("infoId") String infoId) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC T1: duong doc nhu duong ghi cung controller.
        RecipelInfoEvt recipelInfoEvt = medicalRecordService.queryByInfoId(infoId);
        return ResponseEntity.ok(ResultUtil.successJson(recipelInfoEvt));
    }


    @PostMapping(value = "v2/allSave")
    public ResponseEntity<JSONObject> allNewSave(@RequestParam("entity") String strEntity,
                                                 @RequestParam(name = "fileIdUploads",required=false) MultipartFile[] fileIdUploads,
                                                 @RequestParam(name = "delFileIds",required=false) String strDelFileIds,
                                                 @RequestParam(name = "medicalFiles",required=false) MultipartFile[] medicalFiles
                                                 ) throws Exception {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC U1: duong ghi nhu cac duong ghi cung controller (v2/allSave tunghi de duoc bang token 0 quyen).
        ReceptionEvt receptionEvt = JSONObject.parseObject(strEntity, ReceptionEvt.class);

        for (RecipelInfoEvt recipelInfoEvtList : receptionEvt.getRecipelInfoEvtList()) {
            for (RecipelDetail recipelDetail : recipelInfoEvtList.getRecipelDetailEvtList()) {
                recipelDetail.setExecutions(BigDecimal.valueOf(0));
            }
        }
        String[] delFileIds = StringUtils.commaDelimitedListToStringArray(strDelFileIds);

        String registrationId = this.medicalRecordService.allNewSave(receptionEvt, fileIdUploads, delFileIds,medicalFiles);
        JSONObject result = new JSONObject();
        result.put("registrationId", registrationId);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "addRecipel")
    public ResponseEntity<JSONObject> addRecipel(@RequestBody ReceptionEvt receptionEvt) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC U1: duong ghi nhu cac duong ghi cung controller (hien 500 ca 2 token do remote_diagnosis rong — loi cu, guard van ap nhu T1 instant/getByOrder).
        medicalRecordService.saveRecipelInfoEvtTo(receptionEvt.getRecipelInfoEvtList(), receptionEvt.getId());
        return ResponseEntity.ok(ResultUtil.successJson(receptionEvt.getId()));
    }

    @PostMapping(value = "v2/allQuery/{registrationId}")
    public ResponseEntity<JSONObject> allNewQuery(@PathVariable("registrationId") String registrationId,
                                                  @RequestParam(name = "recipelInfoIds",required=false) String strRecipelInfoIds) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC T1: duong doc nhu duong ghi cung controller.
        String[] recipelInfoIds = StringUtils.commaDelimitedListToStringArray(strRecipelInfoIds);
        ReceptionEvt receptionEvt = this.medicalRecordService.allNewQuery(registrationId, recipelInfoIds);

        return ResponseEntity.ok(ResultUtil.successJson(receptionEvt));
    }
    @PostMapping(value = "history/recipel")
    public ResponseEntity getHistoryRecipel(@RequestBody SearchParams searchParams){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC T1: duong doc nhu duong ghi cung controller.
        List<RecipelInfoDTO> byRegistrationId = medicalRecordService.getByRegistrationId(searchParams.getParams(),searchParams.getLimit(),searchParams.getOffset(),searchParams.getOrder());
        return ResponseEntity.ok(ResultUtil.successJson(byRegistrationId));
    }

    /**
     * Tiền sử dị ứng mới nhất đã lưu của bệnh nhân (phục vụ banner cảnh báo
     * dị ứng trên màn khám/kê đơn). Chỉ đọc dữ liệu hồ sơ sẵn có.
     */
    @GetMapping(value = "/allergy/{patientId}")
    public ResponseEntity allergy(@PathVariable("patientId") String patientId) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC T1: duong doc nhu duong ghi cung controller.
        String allergyHistory = medicalRecordService.getLatestAllergyHistory(patientId);
        return ResponseEntity.ok(ResultUtil.successJson(allergyHistory));
    }

    @GetMapping(value = "/recordpat/{id}")
    public ResponseEntity recordpat(@PathVariable("id") String id){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC T1: duong doc nhu duong ghi cung controller.
        List<Recordpatvo> result = medicalRecordService.recordpatlist(id);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    // QuaĐăng ký khámidThêmca bệnhThông tin
    @PostMapping(value = "registerSave")
    public ResponseEntity<JSONObject> registerSave(@RequestBody MedicalRecord entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC: tai khoan 0-resource bi 403 (xem CrudController).
        Registration medicalRecord = registrationService.get(entity.getRegistration().getId());
        entity.setCompany(medicalRecord.getCompany());
        String id = medicalRecordService.save(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }


    /**
     * LấyHệ thốngChẩn đoán
     * @param searchParam
     * @return
     */
    @PostMapping("/getxtzd")
    public ResponseEntity<JSONObject> getxtzd(String searchParam,String zdType){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC Y2h: danh muc tham chieu nhu cac duong cung controller.
        LambdaQueryWrapper<XtZd> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(XtZd::getZdlx, zdType)
                .eq(XtZd::getZt, 1)
                .and(!StringUtils.isEmpty(searchParam),wrapper -> wrapper
                        .like(XtZd::getZdmc, searchParam)
                        .or()
                        .like( XtZd::getPy, searchParam) // Py Trườngmờkhớp
                        .or()
                        .like(XtZd::getZdcode, searchParam)
                ); // Zdcode Trườngmờkhớp
        IPage<XtZd> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 30);
        page = zdService.page(page, queryWrapper);
        return ResponseEntity.ok(ResultUtil.successJson(page));
    }

    /**
     * LấyHệ thốngChứng hậu Đông y
     * @param searchParam
     * @return
     */
    @PostMapping("/getzyzh")
    public ResponseEntity<JSONObject> getxtzd(String searchParam){
        if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC Y2h: danh muc tham chieu nhu cac duong cung controller.
        LambdaQueryWrapper<XtZyzh> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(XtZyzh::getZt, 1)
                .and(!StringUtils.isEmpty(searchParam),wrapper -> wrapper
                                .like( XtZyzh::getZhmc, searchParam)
                                .or()
                                .like( XtZyzh::getZhcode, searchParam)
                                .or()
                                .like( XtZyzh::getPy, searchParam)
                );
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<XtZyzh> page = xtZyzhService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 30), queryWrapper);
        return ResponseEntity.ok(ResultUtil.successJson(page));
    }




}
