package com.geeke.cure.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.geeke.common.controller.CrudController;
import com.geeke.cure.entity.InspectionCheck;
import com.geeke.cure.service.InspectionCheckService;
import com.geeke.sys.utils.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
import com.geeke.cure.entity.InspectionCheckInfo;
import com.geeke.cure.service.InspectionCheckInfoService;
import com.geeke.utils.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 * Chi tiết xét nghiệm và kiểm traController
 * @author rys
 * @version 2022-10-19
 */
@RestController
@RequestMapping(value = "/cure/inspectionCheckInfo")
public class InspectionCheckInfoController extends CrudController<InspectionCheckInfoService, InspectionCheckInfo> {

    private static final Logger logger = LoggerFactory.getLogger(InspectionCheckInfoController.class);

    @Autowired
    protected InspectionCheckInfoService inspectionCheckInfoService;

    @Autowired
    private InspectionCheckService inspectionCheckService;

    @Override
    protected InspectionCheckInfoService getService() {
        return inspectionCheckInfoService;
    }

    @GetMapping("/info/{inspecId}")
    public ResponseEntity<JSONObject> getByInspecId(@PathVariable("inspecId") String inspecId) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }

        InspectionCheckInfo entity = inspectionCheckInfoService.getByInspecId(inspecId);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    @PostMapping(value = "saveWithFile")
    public ResponseEntity<JSONObject> saveWithFile(@RequestParam("entity") String strInspection,
                                               @RequestParam("fileIdUploads") MultipartFile[] fileIdUploads,
                                           @RequestParam(value = "deleteIds",required = false)String strDeleteIds) throws IOException {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }

        InspectionCheckInfo inspectionCheckInfo;
        try {
            inspectionCheckInfo = JSONObject.parseObject(strInspection, InspectionCheckInfo.class);
        } catch (com.alibaba.fastjson.JSONException e) {
            // DS85: malformed entity JSON threw at parse -> 500;
            // unparseable input can never succeed -> 400.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_400));
        }
        // DS85: parse from String (no @Valid); a "null" entity NPEd in
        // the service at getInspectionCheckDetails() -> 400 early
        // (DS83 pattern). The remaining null shapes are already 400
        // via the service T1/T2/T3 guards (iter41/42), and the
        // post-save controller deref is only reachable after those
        // guards pass, so no further change here.
        if (inspectionCheckInfo == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_400));
        }
        String[] deleteIds;
        try {
            deleteIds = JSONObject.parseObject(strDeleteIds, String[].class);
        } catch (com.alibaba.fastjson.JSONException e) {
            // DS85: same as the entity parse above.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_400));
        }
        logger.debug("LưuChi tiết xét nghiệm và kiểm tra: {}", inspectionCheckInfo);
        inspectionCheckInfoService.save(inspectionCheckInfo,fileIdUploads,deleteIds);
        InspectionCheck inspectionCheck = inspectionCheckService.get(inspectionCheckInfo.getInspectionCheck().getId());
        if (inspectionCheckInfo.getInspectionCheckDetails() != null && !inspectionCheckInfo.getInspectionCheckDetails().isEmpty()) {
            inspectionCheck.setStatus("1");
        }
        JSONObject userObj = SessionUtils.getUserJson();
        inspectionCheck.setUpdateBy(userObj.getString("name"));
        inspectionCheck.setUpdateDate(new Date());
        inspectionCheckService.save(inspectionCheck);
        return ResponseEntity.ok(ResultUtil.successJson(inspectionCheckInfo.getId()));
    }
}