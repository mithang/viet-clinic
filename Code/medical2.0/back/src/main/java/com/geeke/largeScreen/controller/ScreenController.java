package com.geeke.largeScreen.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.largeScreen.entity.Basics;
import com.geeke.largeScreen.service.ScreenService;
import com.geeke.org.service.CompanyService;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Màn hình dữ liệu lớnController
 * @author zhanghan
 * @version 2023-09-05
 */
@RestController
@RequestMapping(value = "/largeScreen")
public class ScreenController extends BaseController {

	@Autowired
	private ScreenService screenService;

	@Autowired
	private CompanyService companyService;

	/**
	 * DS20: parentId used to come straight from the URL, so any
	 * permissioned session could read another institution's aggregates
	 * — today's registrations, received patients, tasks and collected
	 * income. The four ScreenDao queries all scope by
	 * "company_id in (SELECT id FROM org_company WHERE parent_id = ?)",
	 * so the scope is forced to the session institution, list-style
	 * (force, not 403, as in Scheduling/Blmb/Stmanagement): a clinic
	 * sees its parent institution, an institution (parent blank/"0")
	 * sees itself. A blank/"null"/"0" session tenant keeps the old
	 * behaviour for internal callers, parity with the other guards.
	 * No client caller exists for this endpoint (the screen views are
	 * Grafana stubs), so forcing cannot regress a real UI flow.
	 */
	private String effectiveParentId(String requestedParentId) {
		String tenantId = SessionUtils.getLoginTenantId();
		if (StringUtils.isBlank(tenantId) || "null".equals(tenantId) || "0".equals(tenantId)) {
			return requestedParentId;
		}
		String parentId = companyService.getCompanyParentId(tenantId);
		if (StringUtils.isBlank(parentId) || "0".equals(parentId)) {
			return tenantId;
		}
		return parentId;
	}

	/** cơ bảnDữ liệuLấy */
	@GetMapping("/{parentId}")
	public ResponseEntity<JSONObject> getBasics(@PathVariable("parentId") String parentId) {
		if (!hasAnySessionPermission()) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResultUtil.errorJson(ErrorEnum.E_502)); } // San RBAC Y2f.
		Basics entity = screenService.getBasics(effectiveParentId(parentId));
		return ResponseEntity.ok(ResultUtil.successJson(entity));
	}

}