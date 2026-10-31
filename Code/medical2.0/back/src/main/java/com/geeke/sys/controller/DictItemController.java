package com.geeke.sys.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.sys.entity.DictItem;
import com.geeke.sys.service.DictItemService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Mục từ điểnController
 * @author lys
 * @version 2020-06-30
 */
@RestController
@RequestMapping(value = "/sys/dictItem")
public class DictItemController extends CrudController<DictItemService, DictItem> {

	@Autowired
	protected DictItemService dictItemService;

	@Override
	protected DictItemService getService() {
		return dictItemService;
	}

	/**
	 * Theo loại từ điểncodeLấy tất cả mục từ điển.
	 * CỐ TÌNH KHÔNG chặn bằng permission ở vòng này: trong DB không tồn tại
	 * resource "dictItem:*", và nếu chặn bằng "dictType:list" thì mọi vai trò
	 * nghiệp vụ (bác sĩ, dược sĩ, kho, thu ngân) đều gãy vì không ai có
	 * dictType:* ngoài tài khoản toàn quyền, trong khi đây là lookup bắt buộc
	 * của gần như mọi form (payType, giới tính, đơn vị thuốc…). Endpoint vẫn
	 * chịu authc của Shiro (không token → 20011). Muốn chặn đúng thì phải seed
	 * sys_resource dictItem + cấp quyền vai trò — thay đổi DB/quyền, ngoài
	 * phạm vi vòng này.
	 * Riêng listPage/listAll generic chịu sàn hasAnySessionPermission của
	 * CrudController (lookup dict-item-select dùng listAll nên cũng không
	 * chặn bằng dictType:list).
	 */
	@GetMapping("/listByCode/{code}")
	public ResponseEntity<JSONObject> listByCode(@PathVariable String code) {
		List<DictItem> items = dictItemService.listByDictTypeCode(code);
		return ResponseEntity.ok(ResultUtil.successJson(items));
	}

	/**
	 * Ghi mục từ điển: không UI nào gọi saveDictItem (chỉ api def), đường
	 * quản trị từ điển thuộc họ dictType nên yêu cầu dictType:create/update.
	 * Tài khoản toàn quyền có đủ quyền này.
	 */
	@Override
	@PostMapping(value = "save")
	public ResponseEntity<JSONObject> save(@Valid @RequestBody DictItem entity) {
		if (!hasSessionPermission("dictType:create") && !hasSessionPermission("dictType:update")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(ResultUtil.errorJson(ErrorEnum.E_502));
		}
		String id = getService().save(entity).getId();
		return ResponseEntity.ok(ResultUtil.successJson(id));
	}

	@Override
	@PostMapping(value = "delete")
	public ResponseEntity<JSONObject> delete(@RequestBody DictItem entity) {
		if (entity == null) {
			return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
		}
		if (!hasSessionPermission("dictType:delete")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(ResultUtil.errorJson(ErrorEnum.E_502));
		}
		int rows = getService().delete(entity);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}

	@Override
	@PostMapping(value = "bulkInsert")
	public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<DictItem> entitys) {
		if (!hasSessionPermission("dictType:create")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(ResultUtil.errorJson(ErrorEnum.E_502));
		}
		List<String> ids = getService().bulkInsert(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkUpdate")
	public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<DictItem> entitys) {
		if (!hasSessionPermission("dictType:update")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(ResultUtil.errorJson(ErrorEnum.E_502));
		}
		List<String> ids = getService().bulkUpdate(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkDelete")
	public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<DictItem> entitys) {
		if (!hasSessionPermission("dictType:delete")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(ResultUtil.errorJson(ErrorEnum.E_502));
		}
		int rows = getService().bulkDelete(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}
}
