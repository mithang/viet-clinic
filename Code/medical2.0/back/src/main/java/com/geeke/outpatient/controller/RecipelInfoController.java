package com.geeke.outpatient.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.outpatient.entity.RecipelInfo;
import com.geeke.outpatient.service.RecipelInfoService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Thông tin đơn thuốcController
 * @author txl
 * @version 2022-06-20
 */
@RestController
@RequestMapping(value = "/outpatient/recipelInfo")
public class RecipelInfoController extends CrudController<RecipelInfoService, RecipelInfo> {

	@Autowired
	private RecipelInfoService recipelInfoService;

	@Override
	protected RecipelInfoService getService() {
		return recipelInfoService;
	}

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
	 * (company null) thì cho qua như cũ — service trả 0. Hai đường này
	 * trước đây đã bị chặn chéo tenant gián tiếp bằng 400 qua
	 * CrudService.get/verifyTenantOwnership; sàn 403 ở đây làm rõ
	 * fail-closed và đồng nhất với X1/X2.
	 */
	private boolean isForeign(String ownerCompanyId) {
		String tenantId = sessionTenantId();
		return tenantId != null && ownerCompanyId != null && !tenantId.equals(ownerCompanyId);
	}

	@GetMapping("/update/notShow")
	public ResponseEntity<JSONObject> updateNotShowById(@RequestParam("id") String id) {
		if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X3, parity W1/X1/X2.
		if (isForeign(recipelInfoService.getRecipelInfoCompanyId(id))) { return forbidden(); } // X3: id don tran, chan tenant khac.
		int res = recipelInfoService.updateNotShowById(id);
		return ResponseEntity.ok(ResultUtil.successJson(res));
	}

	@GetMapping("/invalid/{id}")
	public ResponseEntity<JSONObject> invalid(@PathVariable String id) {
		if (!hasAnySessionPermission()) { return forbidden(); } // San RBAC X3, parity W1/X1/X2.
		if (isForeign(recipelInfoService.getRecipelInfoCompanyId(id))) { return forbidden(); } // X3: id don tran, chan tenant khac.
		int res = recipelInfoService.invalid(id);
		return ResponseEntity.ok(ResultUtil.successJson(res));
	}
}
