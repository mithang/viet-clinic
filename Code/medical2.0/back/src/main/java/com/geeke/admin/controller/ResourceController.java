package com.geeke.admin.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.entity.Resource;
import com.geeke.admin.service.ResourceService;
import com.geeke.common.controller.CrudController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Quản lý tài nguyênController
 * @author lys
 * @version 2021-11-18
 */
@RestController
@RequestMapping(value = "/admin/resource")
public class ResourceController extends CrudController<ResourceService, Resource> {

	@Autowired
	protected ResourceService resourceService;

	@Override
	protected ResourceService getService() {
		return resourceService;
	}

	// ITER68 item 2-code-1: san phan quyen theo thao tac cho write generic
	// cua bang phan quyen (sys_resource), theo mau GenTableController /
	// DictTypeController, thay san cu hasAnySessionPermission (bat ky
	// session nao co 1 resource cung qua). Ly do, da chung minh bang code
	// + probe live, khong phai suy doan:
	// - sys_resource khong co cot company_id nen guard tenant cua
	// CrudService (chi so company payload voi session tenant) khong chan
	// duoc: session clinic gui company = OWN la qua guard roi ghi dong
	// toan cuc vao bang phan quyen (sweep iter68 A2; probe-first pre-fix
	// cua item nay: clinic save/delete/bulkInsert/bulkUpdate/bulkDelete
	// deu 200 tao/doi/xoa dong that).
	// - KHONG khoa han 403 nhu /sys/action (iter67 item 2), vi o day KHAC
	// action: resource resource:create/update/delete CO THAT trong DB
	// (router 3003 admin/resource) va chi role 2000 (super) + 4000 (testfull)
	// giu; role clinic testys giu 0 resource resource:*. Trang
	// views/admin/resource/index.vue gat dung cac quyen nay: nut Them /
	// Them nhanh -> resource:create, nut Sua -> resource:update, nut Xoa
	// -> resource:delete, form resourceForm.vue luu qua saveResource —
	// do la luong admin hop le that, can giu cho admin, khong phai lo
	// can khoa han. Khong tu tao resource moi (tao + cap resource se la
	// thay doi quyen vai tro goc, cam); san nay chi doc resource da co.
	// - KHONG khoa o tang service: backend caller duy nhat cua
	// ResourceService ngoai controller nay la PermissionController
	// (listAllByVersion/listAll — CHI DOC, phuc vu trang cap quyen),
	// khong co caller noi bo nao goi save/delete/bulk* nen khoa o
	// controller la du va khong vo luong noi bo.
	// - Doc (get/list/listAll) GIU NGUYEN trong item nay.
	@Override
	@PostMapping(value = "save")
	public ResponseEntity<JSONObject> save(@Valid @RequestBody Resource entity) {
		if (!hasSessionPermission("resource:create") && !hasSessionPermission("resource:update")) {
			return forbidden();
		}
		if (!beforeSave(entity)) {
			return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50002, "LưutrướcKiểm traChưaQua"));
		}
		String id = getService().save(entity).getId();
		afterSave(entity);
		return ResponseEntity.ok(ResultUtil.successJson(id));
	}

	@Override
	@PostMapping(value = "delete")
	public ResponseEntity<JSONObject> delete(@RequestBody Resource entity) {
		if (entity == null) {
			return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
		}
		if (!hasSessionPermission("resource:delete")) {
			return forbidden();
		}
		if (!beforeDelete(entity)) {
			return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50002, "XóatrướcKiểm traChưaQua"));
		}
		int rows = getService().delete(entity);
		afterDelete(entity);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}

	@Override
	@PostMapping(value = "bulkInsert")
	public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<Resource> entitys) {
		if (!hasSessionPermission("resource:create")) {
			return forbidden();
		}
		List<String> ids = getService().bulkInsert(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkUpdate")
	public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<Resource> entitys) {
		if (!hasSessionPermission("resource:update")) {
			return forbidden();
		}
		List<String> ids = getService().bulkUpdate(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkDelete")
	public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<Resource> entitys) {
		if (!hasSessionPermission("resource:delete")) {
			return forbidden();
		}
		int rows = getService().bulkDelete(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}
}
