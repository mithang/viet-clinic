package com.geeke.sys.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.sys.entity.PropertySet;
import com.geeke.sys.service.PropertySetService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Quản lý tập thuộc tínhController
 * @author lys
 * @version 2021-12-26
 */
@RestController
@RequestMapping(value = "/sys/propertySet")
public class PropertySetController extends CrudController<PropertySetService, PropertySet> {

	@Autowired
	protected PropertySetService propertySetService;

	@Override
	protected PropertySetService getService() {
		return propertySetService;
	}

	// ITER68 item 2-code-7, option (i): san phan quyen theo thao tac cho
	// write generic cua bang sys_property_set, theo mau LesseeController
	// (code-11) va TollDetailController (code-12), thay san cu
	// hasAnySessionPermission (bat ky session nao co 1 resource cung qua).
	// Ly do, da chung minh bang code + DB + API live, khong phai suy doan:
	// - Bang sys_property_set la cau hinh he thong TOAN CUC: KHONG co cot
	// company_id (id, name, properties_def, remarks, ...), entity khong
	// khai field company, nen guard tenant generic cua CrudService mu
	// voi no. Delete la soft-delete (del_flag='1', getBusTableHasDelFlag
	// == true), khac AdministrativeDivision (xoa cung).
	// - San proxy DON THUAN vo hieu o day neu khong thu hep grant truoc:
	// propertySet:create/update/delete CO THAT trong DB (router
	// 774235612524134400) va duoc chinh role clinic block-case giu:
	// YS 2298416718726168646 va Duoc si 2521363969936572416 giu du 6
	// propertySet:* (live /permission/resources cua testys tra du
	// create/update/delete, giong het testfull). Vi vay phuong an (i)
	// lam HAI buoc, theo quyet dinh duoi uy quyen toan quyen cua chu
	// so huu ngay 2026-11-05 (giong TollDetail code-12):
	// (1) migration v14 thu hep grant goc — bo propertySet:create/update/
	// delete khoi YS, Duoc si va role phien ban clinic
	// 2077468568630583319 (role mau company 0, khong phai role song);
	// GIU 4000 (Toan quyen he thong) va MOI role admin (OWN admin
	// 2077468568630583311 + admin cua 3 clinic khac), vi quan ly Tap
	// thuoc tinh la luong admin hop le;
	// (2) san proxy nay moi co tac dung: clinic thieu quyen -> 403.
	// - KHONG khoa han 403 nhu /sys/action (iter67 item 2): o day resource
	// create/update/delete TON TAI va trang quan ly Tap thuoc tinh
	// (views/sys/propertySet/index.vue, permissionPrefix 'propertySet',
	// Them->create / Sua->update / Xoa->delete, form luu qua
	// savePropertySet -> /sys/propertySet/save) la luong admin hop le
	// DUY NHAT ghi bang nay (grep backend: khong co caller nao cua
	// PropertySetService/PropertySetDao ngoai chinh controller; cac
	// component khac chi DOC qua listPropertySetAll/getPropertySetById).
	// Khoa han se giet tinh nang cho moi session ke ca admin.
	// - KHONG khoa o tang service: khong co backend caller nao ngoai
	// controller (xem tren). Doc (get/list/listAll) GIU NGUYEN.
	// - Khong tu tao resource moi; san nay chi doc resource da co.
	// - Luu y nhu AdministrativeDivision: entity khong override
	// getCompany() nen save KHONG company roi vao @Valid 400 truoc ca
	// san cu lan san moi (khong phai lo fallback).
	@Override
	@PostMapping(value = "save")
	public ResponseEntity<JSONObject> save(@Valid @RequestBody PropertySet entity) {
		if (!hasSessionPermission("propertySet:create") && !hasSessionPermission("propertySet:update")) {
			return forbidden();
		}
		if (!beforeSave(entity)) {
			return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50002, "Lưu trước Kiểm tra Chưa Qua"));
		}
		String id = getService().save(entity).getId();
		afterSave(entity);
		return ResponseEntity.ok(ResultUtil.successJson(id));
	}

	@Override
	@PostMapping(value = "delete")
	public ResponseEntity<JSONObject> delete(@RequestBody PropertySet entity) {
		if (entity == null) {
			return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
		}
		if (!hasSessionPermission("propertySet:delete")) {
			return forbidden();
		}
		if (!beforeDelete(entity)) {
			return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50002, "Xóa trước Kiểm tra Chưa Qua"));
		}
		int rows = getService().delete(entity);
		afterDelete(entity);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}

	@Override
	@PostMapping(value = "bulkInsert")
	public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<PropertySet> entitys) {
		if (!hasSessionPermission("propertySet:create")) {
			return forbidden();
		}
		List<String> ids = getService().bulkInsert(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkUpdate")
	public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<PropertySet> entitys) {
		if (!hasSessionPermission("propertySet:update")) {
			return forbidden();
		}
		List<String> ids = getService().bulkUpdate(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkDelete")
	public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<PropertySet> entitys) {
		if (!hasSessionPermission("propertySet:delete")) {
			return forbidden();
		}
		int rows = getService().bulkDelete(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}
}
