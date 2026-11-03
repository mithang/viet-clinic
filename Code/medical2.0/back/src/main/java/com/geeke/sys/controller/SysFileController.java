package com.geeke.sys.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.sys.entity.SysFile;
import com.geeke.sys.service.SysFileService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Tệp đính kèm hệ thốngController
 * @author szy
 * @version 2021-09-22
 */
@RestController
@RequestMapping(value = "/sys/sysFile")
public class SysFileController extends CrudController<SysFileService, SysFile> {

	@Autowired
	protected SysFileService sysFileService;

	@Override
	protected SysFileService getService() {
		return sysFileService;
	}

	/**
	 * K3 (iteration 58): sys_file khong co cot company_id nen
	 * verifyTenantOwnership + stored-row check cua CrudService deu pass,
	 * session chi can san hasAnySessionPermission la xoa duoc file cua
	 * bat ky tenant nao bang id tran. Override rieng 2 endpoint xoa nay
	 * (khong doi delete(SysFile)/delete(String[]) cua service hay cac
	 * caller noi bo): guard object_id truoc moi ghi, roi moi goi service
	 * nhu cu. List (liet ke) ngoai pham vi K3, giu nguyen.
	 */
	@Override
	@PostMapping(value = "delete")
	public ResponseEntity<JSONObject> delete(@RequestBody SysFile entity) {
		if (entity == null) {
			return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
		}
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		if (!beforeDelete(entity)) {
			return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50002, "XóatrướcKiểm traChưaQua"));
		}
		getService().verifySysFileDeleteOwnership(java.util.Collections.singletonList(entity.getId()));
		int rows = getService().delete(entity);
		afterDelete(entity);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}

	@Override
	@PostMapping(value = "bulkDelete")
	public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<SysFile> entitys) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		java.util.List<String> ids = new java.util.ArrayList<>();
		if (entitys != null) {
			for (SysFile entity : entitys) {
				if (entity != null) {
					ids.add(entity.getId());
				}
			}
		}
		getService().verifySysFileDeleteOwnership(ids);
		int rows = getService().bulkDelete(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}
}
