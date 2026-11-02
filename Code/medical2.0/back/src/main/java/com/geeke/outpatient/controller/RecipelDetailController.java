package com.geeke.outpatient.controller;

import java.util.List;

import com.geeke.outpatient.service.MedicalRecordService;
import com.geeke.outpatient.service.RecipelInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.geeke.outpatient.entity.RecipelDetail;
import com.geeke.outpatient.service.RecipelDetailService;
import com.geeke.common.controller.CrudController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;

/**
 * Chi tiết đơn thuốcController
 * @author txl
 * @version 2022-06-07
 */
@RestController
@RequestMapping(value = "/outpatient/recipelDetail")
public class RecipelDetailController extends CrudController<RecipelDetailService, RecipelDetail> {

	@Autowired
	private RecipelDetailService recipelDetailService;

	@Override
	protected RecipelDetailService getService() {
		return recipelDetailService;
	}

	@Autowired
	private MedicalRecordService medicalRecordService;

	@Autowired
	private RecipelInfoService recipelInfoService;

	/**
	 * X3: tenant của phiên đăng nhập; rỗng hoặc "null" (luồng nội bộ
	 * không session) thì giữ hành vi cũ, như verifyTenantOwnership.
	 */
	private String sessionTenantId() {
		String tenantId = SessionUtils.getLoginTenantId();
		if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
			return null;
		}
		return tenantId;
	}

	/**
	 * X3: kiểm tra sở hữu cho đường theo id trần. id không tồn tại
	 * (company null) thì cho qua như cũ — service trả danh sách rỗng.
	 */
	private boolean isForeign(String ownerCompanyId) {
		String tenantId = sessionTenantId();
		return tenantId != null && ownerCompanyId != null && !tenantId.equals(ownerCompanyId);
	}

	@GetMapping(value = "/getByRecipelInfoId/{recipelInfoId}")
	public ResponseEntity<JSONObject> getByRecipelInfoId(@PathVariable("recipelInfoId") String recipelInfoId){
		if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X3, parity W1/X1/X2.
		if (isForeign(recipelInfoService.getRecipelInfoCompanyId(recipelInfoId))) { return forbidden(); } // X3: id don tran, chan tenant khac (DAO truoc day WHERE chi theo recipel_info_id, khong loc company).
		List<RecipelDetail> byRecipelInfoId = recipelDetailService.getByRecipelInfoId(recipelInfoId);
		for (RecipelDetail recipelDetail:
		byRecipelInfoId) {
			{
				recipelDetail.setDrugStuffId(medicalRecordService.getDrugStuffEvt(recipelDetail));
			}
		}
		return ResponseEntity.ok(ResultUtil.successJson(byRecipelInfoId));
	}
}
