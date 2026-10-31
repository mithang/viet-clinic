package com.geeke.outpatient.controller;

import java.util.List;

import com.geeke.outpatient.entity.Registration;
import com.geeke.outpatient.service.RegistrationService;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
import com.geeke.outpatient.entity.Patient;
import com.geeke.outpatient.service.PatientService;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.http.HttpStatus;

/**
 * Thông tin bệnh nhânController
 * @author txl
 * @version 2022-06-23
 */
@RestController
@RequestMapping(value = "/outpatient/patient")
public class PatientController extends CrudController<PatientService, Patient> {

	@Autowired
	protected PatientService patientService;

    @Autowired
    private RegistrationService registrationService;

    @Override
    protected PatientService getService() {
        return patientService;
    }

    /**
     * Danh sách bệnh nhân có phân trang: chỉ tài khoản có quyền tài nguyên
     * "patient:list" mới được gọi. Trước đây mọi token hợp lệ đều đọc được
     * danh sách bệnh nhân, vì filter chỉ kiểm tra xác thực. Lọc theo tenant
     * vẫn do service đảm nhiệm (ensureCompanyFilter: chỉ trả bệnh nhân thuộc
     * phòng khám đang đăng nhập).
     */
    @Override
    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        if (!hasSessionPermission("patient:list")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        Page<Patient> result = getService().listPage(searchParams.getParams(), searchParams.getOffset(),
                searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    //WeChatLưu
    @PostMapping(value = "wxSave")
    public ResponseEntity<JSONObject> wxSave(@RequestBody Patient entity) {
        String id = patientService.wxSave(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @PostMapping(value = "getByCard")
    public ResponseEntity<JSONObject> getByCard(@RequestBody Patient patient) {
        Patient byCard = patientService.getByCard(patient);
        return ResponseEntity.ok(ResultUtil.successJson(byCard));
    }

    @PostMapping(value = "getByOpenId")
    public ResponseEntity<JSONObject> getByOpenId(@RequestBody Patient patient){
        List<Patient> patients = patientService.getByOpenId(patient.getOpenId(),patient.getCompany().getId());
        return ResponseEntity.ok(ResultUtil.successJson(patients));
    }

    /* Tra cứu thông tin bệnh nhân theo họ tên và CMND/CCCD rồi đăng ký khám */
    @PostMapping(value = "inquire")
    public ResponseEntity<JSONObject> inquire(@RequestBody Patient patient) {
        // Truy vấnThông tin bệnh nhân
        Patient result = patientService.inquire(patient);
        if (result==null){
            return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50001, "Truy vấnThất bại, khôngđóThông tin bệnh nhân"));
        }
        // Đăng ký khám
        Registration registration = new Registration();
        registration.setPatientId(result);
        registration.setCompany(result.getCompany());
        String id = registrationService.save(registration).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    /* ThêmBệnh nhânhồ sơánThông tinvàĐăng ký khám */
    @PostMapping(value = "add")
    public ResponseEntity<JSONObject> add(@RequestBody Patient patient) {
        // ThêmThông tin bệnh nhân
        Patient result = patientService.save(patient);
        // Đăng ký khám
        Registration registration = new Registration();
        registration.setPatientId(result);
        registration.setCompany(result.getCompany());
        String id = registrationService.save(registration).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

}
