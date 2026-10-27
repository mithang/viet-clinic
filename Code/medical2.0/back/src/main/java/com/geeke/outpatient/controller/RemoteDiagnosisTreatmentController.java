package com.geeke.outpatient.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.outpatient.dao.RemoteDiagnosisTreatmentDao;
import com.geeke.outpatient.entity.*;
import com.geeke.outpatient.service.PatientService;
import com.geeke.outpatient.service.RecipelDetailService;
import com.geeke.outpatient.service.RecipelInfoService;
import com.geeke.outpatient.service.RemoteDiagnosisTreatmentService;
import com.geeke.sys.controller.BaseController;
import com.geeke.toll.entity.TollEvt;
import com.geeke.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * từ xaChẩn đoánQuản lýController
 * @author zh
 * @version 2023-12-06
 */
@RestController
@RequestMapping(value = "/outpatient/remoteDiagnosisTreatment")
public class RemoteDiagnosisTreatmentController extends BaseController {

	@Autowired
	private PatientService patientService;
	@Autowired
	private RecipelInfoService recipelInfoService;
	@Autowired
	private RemoteDiagnosisTreatmentService remoteDiagnosisTreatmentService;


    /** TheoidTruy vấn */
    @GetMapping("/{id}")
    public ResponseEntity<JSONObject> getById(@PathVariable("id") String id) {
        RemoteDiagnosisTreatment entity = remoteDiagnosisTreatmentService.diagnosisById(id);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    /** TheoĐăng ký khámTruy vấn */
    @GetMapping("registrationId/{registrationId}")
    public ResponseEntity<JSONObject> getRegistrationId(@PathVariable("registrationId") String registrationId) {
        RemoteDiagnosisTreatment entity = remoteDiagnosisTreatmentService.getRegistrationId(registrationId);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    /** Truy vấn phân trang */
    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        Page<RemoteDiagnosisTreatment> result = remoteDiagnosisTreatmentService.diagnosisPage(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /** Lưuđăng kýVui lòng */
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@RequestBody RemoteDiagnosisTreatment entity) {
        String id = remoteDiagnosisTreatmentService.diagnosisSave(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    /** Đơn thuốcThông tinLưu */
    @PostMapping(value = "addRecipel")
    public ResponseEntity<JSONObject> addRecipel(@RequestBody RecipelInfo recipelInfo) {
        RemoteDiagnosisTreatment entity = remoteDiagnosisTreatmentService.diagnosisById(recipelInfo.getDiagnosisId());
        recipelInfo.getCompany().setId(entity.getCompany().getId());
        String id = recipelInfoService.save(recipelInfo).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    /** Sửa trạng thái */
    @PostMapping(value = "modifiedState")
    public ResponseEntity<JSONObject> modifiedState(@RequestBody RemoteDiagnosisTreatment entity) {
        final int i = remoteDiagnosisTreatmentService.modifiedState(entity);
        return ResponseEntity.ok(ResultUtil.successJson(i));
    }

    /** Sửatừ xaThu phíThông tin */
    @PostMapping(value = "chargeState")
    public ResponseEntity<JSONObject> chargeState(@RequestBody @NotNull TollEvt tollEvt) {
        final int i = remoteDiagnosisTreatmentService.chargeState(tollEvt);
        return ResponseEntity.ok(ResultUtil.successJson(i));
    }

    /** Xóađăng kýVui lòng */
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody RemoteDiagnosisTreatment entity) {
        String id = entity.getId();
        int rows = remoteDiagnosisTreatmentService.diagnosisDelete(id);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        List<Patient> result = patientService.listAll(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }


}