package com.geeke.toll.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.data.Page;
import com.geeke.outpatient.entity.Patient;
import com.geeke.sys.controller.BaseController;
import com.geeke.toll.entity.*;
import com.geeke.toll.service.OutpatientLogService;
import com.geeke.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * Nhật ký khám ngoại trúController
 *
 * @author zh
 * @version 2022-08-18
 */
@RestController
@RequestMapping(value = "/toll/outpatientLog")
public class OutpatientLogController extends BaseController {

    @Autowired
    private OutpatientLogService outpatientLogService;

    /**
     * idTruy vấnNgàynhật ký
     */
    @GetMapping("/{id}")
    public ResponseEntity<JSONObject> getLogs(@PathVariable("id") String id) {
        OutpatientLog entity = outpatientLogService.getLogs(id);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    /**
     * Truy vấntất cảNgàynhật ký
     */
    @PostMapping(value = "list")
    public ResponseEntity<JSONObject> listLog(@RequestBody OutpatientLogRc outpatientLogRc) {
        Page<OutpatientLog> result = outpatientLogService.listLog(outpatientLogRc);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * SửaNgàynhật kýThông tin
     */
    @PutMapping(value = "update")
    public int updateLog(@RequestBody OutpatientLog outpatientLogRc) {
        return outpatientLogService.updateLog(outpatientLogRc);

    }

    /**
     * Truy vấnhộ nghèo
     */
    @PostMapping(value = "getPoverty/{patientId}")
    public ResponseEntity<JSONObject> getPoverty(@PathVariable("patientId") String patientId) {
        List<Patient> poverty = outpatientLogService.getPoverty(patientId);
        return ResponseEntity.ok(ResultUtil.successJson(poverty));
    }

    /**
     * Truy vấntất cảNgàynhật ký
     */
    @PostMapping(value = "exportExcel")
    public void exportExcel(@RequestBody OutpatientLogRc outpatientLogRc, HttpServletResponse response) {
        try {
            outpatientLogService.exportExcel(outpatientLogRc,response);
        } catch (Exception e) {
            logger.error("XuấtNhật ký khám ngoại trúThất bại", e);
            throw new RuntimeException(e);
        }
    }


}