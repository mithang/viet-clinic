package com.geeke.clinic.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.clinic.entity.ClinicVersion;
import com.geeke.clinic.service.ClinicVersionService;
import com.geeke.common.controller.CrudController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

import javax.validation.Valid;

/**
 * Phiên bản phòng khámController
 * @author txl
 * @version 2022-05-23
 */
@RestController
@RequestMapping(value = "/clinic/clinicVersion")
public class ClinicVersionController extends CrudController<ClinicVersionService, ClinicVersion> {

    @Autowired
    protected ClinicVersionService clinicVersionService;

    @Override
    protected ClinicVersionService getService() {
        return clinicVersionService;
    }

    // DS5: proxy floor on ALL writes (iter68 pattern). clinicVersion:
    // create/update/delete exist and are held by 2001/4000 + version
    // roles — clinics hold none, so the proxy alone stops clinic
    // writes (including shared rows, matching the single-save posture
    // where clinics are already blocked by the role verify). The
    // proxy does NOT stop full/system (they hold the permissions),
    // so foreign rows are additionally stopped by the service
    // checks (save update branch + bulkUpdate override). Existing
    // deleteClinic/bulkDeleteClinic chains (role guard + rollback)
    // are preserved underneath the proxy.
    @Override
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@Valid @RequestBody ClinicVersion entity) {
        if (!hasSessionPermission("clinicVersion:create") && !hasSessionPermission("clinicVersion:update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        String id = getService().save(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @Override
    @PostMapping(value = "bulkInsert")
    public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<ClinicVersion> entitys) {
        if (!hasSessionPermission("clinicVersion:create")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        List<String> ids = getService().bulkInsert(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }

    @Override
    @PostMapping(value = "bulkUpdate")
    public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<ClinicVersion> entitys) {
        if (!hasSessionPermission("clinicVersion:update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        List<String> ids = getService().bulkUpdate(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }

    @Override
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody ClinicVersion entity) {
        if (!hasSessionPermission("clinicVersion:delete")) {
            return forbidden();
        }
        if (entity == null) {
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        int rows = clinicVersionService.deleteClinic(entity);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    @Override
    @PostMapping(value = "bulkDelete")
    public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<ClinicVersion> entitys) {
        if (!hasSessionPermission("clinicVersion:delete")) {
            return forbidden();
        }
        int rows = clinicVersionService.bulkDeleteClinic(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }
}