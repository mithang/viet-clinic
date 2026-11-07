package com.geeke.cure.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.cure.entity.CureManagement;
import com.geeke.cure.service.CureManagementService;
import com.geeke.outpatient.entity.RecipelDetail;
import com.geeke.outpatient.service.RecipelDetailService;
import com.geeke.outpatient.service.RecipelInfoService;
import com.geeke.stock.entity.Drug;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.geeke.sys.controller.BaseController;
import org.springframework.http.HttpStatus;
import com.geeke.utils.constants.ErrorEnum;

@RestController
@RequestMapping("/cure/cureManagement")
public class CureManagementController extends BaseController {
    @Autowired
    private CureManagementService cureManagementService;
    @Autowired
    private RecipelDetailService recipelDetailService;
    @Autowired
    private RecipelInfoService recipelInfoService;
    /**
     * M2 (iteration 60): tenant cua phien dang nhap; rong hoac "null"
     * (luong noi bo khong session) thi giu hanh vi cu, nhu
     * verifyTenantOwnership / mau X3.
     */
    private String sessionTenantId() {
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return null;
        }
        return tenantId;
    }

    /**
     * M2: id tran khong thuoc tenant hien tai thi la. Id khong ton tai
     * (company null) thi cho qua nhu cu — duong cu van 500 o updateById
     * (xem bao cao M2 case C), khong doi hanh vi do o day.
     */
    private boolean isForeign(String ownerCompanyId) {
        String tenantId = sessionTenantId();
        return tenantId != null && ownerCompanyId != null && !tenantId.equals(ownerCompanyId);
    }

    @PostMapping("save")
    public ResponseEntity<JSONObject> save(@RequestBody CureManagement entity){
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }

        // M2: guard detail + don thuoc bang id TRUOC moi ghi. Controller
        // khong co @Transactional, moi service call la mot transaction
        // rieng (cure insert da commit ngay ca khi updateById sau do NPE
        // — case C pre-fix chung minh), nen guard dat SAU save se khong
        // cuu duoc cure OWN vua tao; dat truoc save thi foreign -> 400
        // va khong co ghi nao xay ra. Guard nay phu ca 2 nhanh stuffType
        // "3" va thuong (cung id detail) + updateSchedule (cung id don).
        // Khong sua updateById/updateSchedule generic, khong doi guard
        // cua CureManagementService.save.
        if (entity.getRecipelDetail() != null) {
            String detailId = entity.getRecipelDetail().getId();
            if (StringUtils.isNotBlank(detailId)
                    && isForeign(recipelDetailService.getRecipelDetailCompanyId(detailId))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResultUtil.errorJson(ErrorEnum.E_400));
            }
            if (entity.getRecipelDetail().getRecipelInfo() != null) {
                String prescriptionId = entity.getRecipelDetail().getRecipelInfo().getId();
                if (StringUtils.isNotBlank(prescriptionId)
                        && isForeign(recipelInfoService.getRecipelInfoCompanyId(prescriptionId))) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ResultUtil.errorJson(ErrorEnum.E_400));
                }
            }
        }

        // DS109 CM2/CM3: controller dereference entity.getRecipelDetail()
        // (getStuffType, dong duoi) va, tren nhanh thuong voi id trong,
        // getRecipelInfo().getId() TRUOC khi toi service save, nen null
        // detail / null info NPE 500. Service save cung validate cac
        // truong nay, nhung chi duoc goi SAU cac dereference do; chan
        // null o day truoc voi 400 ro nghia.
        if (entity.getRecipelDetail() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        if (!"3".equals(entity.getRecipelDetail().getStuffType())
                && (entity.getId() == null || entity.getId().isEmpty())
                && entity.getRecipelDetail().getRecipelInfo() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }

        String crueId=entity.getId();
        String id="";
        if("3".equals(entity.getRecipelDetail().getStuffType())){
            id = cureManagementService.save(entity).getId();
        }else {
            if(null==entity.getId()||entity.getId().isEmpty()){
                entity.setRecipelInfoId(entity.getRecipelDetail().getRecipelInfo().getId());
                entity.setInfuseGroup(entity.getRecipelDetail().getInfuseGroup());
            }
            id = cureManagementService.save(entity).getId();
        }
        if(id!=null){
            recipelDetailService.updateById(entity.getRecipelDetail().getExecutions(),entity.getRecipelDetail().getId(),entity.getRecipelDetail().getStuffType(),entity.getRecipelDetail().getInfuseType(),crueId);
        }else {
            recipelDetailService.updateById(entity.getRecipelDetail().getExecutions(),entity.getRecipelDetail().getId(),entity.getRecipelDetail().getStuffType(),entity.getRecipelDetail().getInfuseType(),crueId);
        }

        final String prescriptionId = entity.getRecipelDetail().getRecipelInfo().getId();
        int schedule = recipelDetailService.schedule(prescriptionId);
        if (schedule == 0){
            int cureType = 1;
            recipelInfoService.updateSchedule(prescriptionId,cureType);
        }

        return ResponseEntity.ok(ResultUtil.successJson(id));
    }
    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }

        Page<CureManagement> result = cureManagementService.listPage(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }
}
