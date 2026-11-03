package com.geeke.outpatient.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.SearchParams;
import com.geeke.outpatient.entity.*;
import com.geeke.outpatient.service.SchedulingService;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * xếp lịchController
 * @author txl
 * @version 2022-06-13
 */
@RestController
@RequestMapping(value = "/outpatient/scheduling")
public class SchedulingController extends BaseController {

    @Autowired
    private SchedulingService schedulingService;

    /**
     * L2 (iteration 59): controller nay extends BaseController nen khong
     * thua huong guard nao cua CrudController — tu cap forbidden() nhu
     * CrudController (mau X2).
     */
    private ResponseEntity<JSONObject> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResultUtil.errorJson(ErrorEnum.E_502));
    }

    /**
     * L2: tenant cua phien dang nhap; rong hoac "null" (luong noi bo
     * khong session) thi giu hanh vi cu, nhu verifyTenantOwnership (mau X2).
     */
    private String sessionTenantId() {
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return null;
        }
        return tenantId;
    }

    /**
     * L2: ep companyid theo session tenant cho cac duong doc va save.
     * Scheduling chi co field String companyid, khong set DataEntity
     * company nen verifyTenantOwnership luon pass; DAO loc/ghi hoan toan
     * theo companyid payload. UI that luon gui companyid = company cua
     * user dang nhap (index.vue:664,691,724,755,859,1028,1048,1129) nen
     * ep khong doi hanh vi hop le.
     */
    private void forceSessionCompany(Scheduling entity) {
        String tenantId = sessionTenantId();
        if (tenantId != null) {
            entity.setCompanyid(tenantId);
        }
    }

    private void forceSessionCompany(SchedulingDTO dto) {
        String tenantId = sessionTenantId();
        if (tenantId != null) {
            dto.setCompanyid(tenantId);
        }
    }

    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SchedulingDTO strEntity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // L2: san RBAC.
        forceSessionCompany(strEntity); // L2: pham vi doc theo session, khong theo payload.
        //SchedulingDTO scheduling = strEntity;//JSONObject.parseObject(strEntity, SchedulingDTO.class);
        List<SchedulingDTO> result = schedulingService.getHisuserpblist(strEntity);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "listzl")
    public ResponseEntity<JSONObject> listzl(@RequestBody SchedulingDTO strEntity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // L2: san RBAC.
        forceSessionCompany(strEntity); // L2: pham vi doc theo session, khong theo payload.
        //SchedulingDTO scheduling = strEntity;//JSONObject.parseObject(strEntity, SchedulingDTO.class);
        List<SchedulingDTO> result = schedulingService.getlistzl(strEntity);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "listzlrday")
    public ResponseEntity<JSONObject> listzlrday(@RequestBody SchedulingDTO strEntity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // L2: san RBAC.
        forceSessionCompany(strEntity); // L2: pham vi doc theo session, khong theo payload.
        List<SchedulingDTO> result = schedulingService.listzlr(strEntity);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "listmx")
    public ResponseEntity<JSONObject> listmx(@RequestBody SchedulingDTO strEntity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // L2: san RBAC.
        forceSessionCompany(strEntity); // L2: pham vi doc theo session, khong theo payload.
        //SchedulingDTO scheduling = strEntity;//JSONObject.parseObject(strEntity, SchedulingDTO.class);
        List<SchedulingmxDTO> result = schedulingService.getlistmx(strEntity);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@RequestBody Scheduling entity) {
        if (!hasAnySessionPermission()) { return forbidden(); } // L2: san RBAC.
        // L2: ep tenant theo session, mau K1/K2. Truoc day payload
        // companyid tenant la + userid bac si la + ngay moi tao that lich
        // truc o tenant la (nhanh insert cua CrudService.save khong verify
        // tenant vi Scheduling khong set DataEntity company). Save chi tao
        // du lieu moi nen ep an toan: dedup listcount chay sau ep, UI that
        // gui company own nen khong doi hanh vi hop le.
        forceSessionCompany(entity);
        entity.setIsLocked("0");
        int countlist= schedulingService.listcount(entity);
        String refdata=null;
        if (countlist>0){
            return ResponseEntity.ok(ResultUtil.successJson(refdata));
        }else {
            refdata = schedulingService.save(entity).getId();
        }
        return ResponseEntity.ok(ResultUtil.successJson(refdata));
    }
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject>delete(@RequestBody Scheduling entity){
        if (!hasAnySessionPermission()) { return forbidden(); } // L2: san RBAC.
        // L2: delete la pha huy du lieu nen FAIL-CLOSED, KHONG ep nhu save:
        // ep companyid ve session se vo tinh xoa dong OWN that khi userid +
        // ngay trung voi dong OWN (payload chi doi company). Payload
        // company khac session tenant -> 403, dong la nguyen. Session
        // khong tenant (luong noi bo) giu hanh vi cu. UI that gui company
        // own nen khong doi hanh vi hop le.
        String tenantId = sessionTenantId();
        if (tenantId != null && StringUtils.isNotBlank(entity.getCompanyid())
                && !tenantId.equals(entity.getCompanyid())) {
            return forbidden();
        }
        Date cs= entity.getSchedulingtime();
//        String data = "2021-02-24 24:21:00";
//        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
//        Date date = sdf.format (cs);

        int refdata = schedulingService.delete(entity);
        return ResponseEntity.ok(ResultUtil.successJson(refdata));
    }

}
