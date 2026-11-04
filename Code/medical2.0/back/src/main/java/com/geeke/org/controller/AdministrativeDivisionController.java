package com.geeke.org.controller;

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
import com.geeke.org.entity.AdministrativeDivision;
import com.geeke.org.service.AdministrativeDivisionService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Phân chia khu vực hành chínhController
 * @author txl
 * @version 2022-06-21
 */
@RestController
@RequestMapping(value = "/org/administrativeDivision")
public class AdministrativeDivisionController extends CrudController<AdministrativeDivisionService, AdministrativeDivision> {

	@Autowired
	protected AdministrativeDivisionService administrativeDivisionService;

	@Override
	protected AdministrativeDivisionService getService() {
		return administrativeDivisionService;
	}

	// ITER68 item 2-code-6: san phan quyen theo thao tac cho write generic
	// cua bang du lieu tham chieu toan cuc (administrative_division),
	// theo mau ResourceController / RouterController / ThemeController /
	// CodeRuleController / ScheduleJobController (iter68 item 2-code-1/2/3/4/5)
	// va GenTableController / DictTypeController, thay san cu
	// hasAnySessionPermission (bat ky session nao co 1 resource cung qua).
	// Ly do, da chung minh bang code + DB + API live, khong phai suy doan:
	// - administrative_division KHONG co cot company_id (DESCRIBE: id,
	// parten_id, name, level — khong company_id, khong del_flag) nen guard
	// tenant cua CrudService (chi so company payload voi session tenant)
	// khong chan duoc: session clinic gui company = OWN la qua guard roi
	// ghi dong toan cuc (sweep iter68; probe-first pre-fix cua item nay:
	// clinic save/delete/bulkInsert/bulkUpdate/bulkDelete deu 200 tao/doi/
	// xoa dong that; AdministrativeDivisionDao.xml CO bulkInsert/
	// bulkUpdate/bulkDelete nen bulk la lo song, khong phai quirk 500).
	// - Xoa o day la XOA CUNG (getBusTableHasDelFlag() == false, mapper
	// dung DELETE FROM), khac Theme/ScheduleJob (soft-delete): mot session
	// clinic xoa duoc dong tham chieu that (6951 dong live) se mat vinh
	// vien, khong khoi phuc bang recycle — vi vay san nay chan truoc khi
	// cham mapper, cang quan trong hon cac item soft-delete.
	// - KHONG khoa han 403 nhu /sys/action (iter67 item 2), vi o day KHAC
	// action: administrativeDivision:create (989213277022437448) / update
	// (...457) / delete (...455) CO THAT trong DB (router
	// 989213277022437444 org/administrativeDivision) va duoc cac role
	// 4000 (testfull) + 2077468568630583721 + 2174053741646364675 +
	// 2174053741646364757 giu (KHAC cac item truoc: khong phai chi
	// 2000/2001/4000); role clinic testys giu 0 administrativeDivision:*.
	// Trang views/org/administrativeDivision/index.vue gat dung cac quyen
	// nay (permissionPrefix 'administrativeDivision': nut Them/Sao chep ->
	// create, nut Sua -> update, nut Xoa -> delete), form
	// administrativeDivisionForm.vue luu qua saveAdministrativeDivision ->
	// /org/administrativeDivision/save — do la luong admin hop le that,
	// can giu cho admin, khong phai lo can khoa han. Khong tu tao
	// resource moi (tao + cap resource se la thay doi quyen vai tro goc,
	// cam); san nay chi doc resource da co.
	// - KHONG khoa o tang service: khong co backend caller nao cua
	// AdministrativeDivisionService.save/delete/bulk* ngoai chinh
	// controller nay; cac form benh nhan (patientForm.vue) chi dung
	// listAdministrativeDivisionAll — CHI DOC, khong bi san nay anh huong.
	// - Luu y nhu ScheduleJob: entity nay khong override getCompany() nen
	// save KHONG company roi vao @Valid 400 truoc ca san cu lan san moi
	// (khong phai lo fallback, khong phai hanh vi cua san nay).
	// - Doc (get/list/listAll) GIU NGUYEN trong item nay.
	@Override
	@PostMapping(value = "save")
	public ResponseEntity<JSONObject> save(@Valid @RequestBody AdministrativeDivision entity) {
		if (!hasSessionPermission("administrativeDivision:create") && !hasSessionPermission("administrativeDivision:update")) {
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
	public ResponseEntity<JSONObject> delete(@RequestBody AdministrativeDivision entity) {
		if (entity == null) {
			return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
		}
		if (!hasSessionPermission("administrativeDivision:delete")) {
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
	public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<AdministrativeDivision> entitys) {
		if (!hasSessionPermission("administrativeDivision:create")) {
			return forbidden();
		}
		List<String> ids = getService().bulkInsert(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkUpdate")
	public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<AdministrativeDivision> entitys) {
		if (!hasSessionPermission("administrativeDivision:update")) {
			return forbidden();
		}
		List<String> ids = getService().bulkUpdate(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkDelete")
	public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<AdministrativeDivision> entitys) {
		if (!hasSessionPermission("administrativeDivision:delete")) {
			return forbidden();
		}
		int rows = getService().bulkDelete(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}
}
