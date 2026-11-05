package com.geeke.toll.controller;

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
import com.geeke.toll.entity.TollDetail;
import com.geeke.toll.service.TollDetailService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Quản lý thu phíController
 * @author lc
 * @version 2022-06-15
 */
@RestController
@RequestMapping(value = "/toll/tollDetail")
public class TollDetailController extends CrudController<TollDetailService, TollDetail> {

	@Autowired
	protected TollDetailService tollDetailService;

	@Override
	protected TollDetailService getService() {
		return tollDetailService;
	}

	// ITER68 item 2-code-12, option (i): san phan quyen theo thao tac cho
	// write generic cua bang toll_detail, theo mau LesseeController
	// (code-11) va cac code item 1..6, thay san cu hasAnySessionPermission
	// (bat ky session nao co 1 resource cung qua).
	// Ly do, da chung minh bang code + DB + API live, khong phai suy doan:
	// - Bang toll_detail KHONG co cot company_id, KHONG co cot del_flag
	// (delete = DELETE FROM, xoa cung), entity khong khai field company
	// rieng, nen guard tenant generic cua CrudService mu voi no.
	// TollDetailService tu iter63 (N3a) DA mang guard theo phieu cha:
	// resolve company cua toll_info cha roi so voi tenant phien, phu
	// save-insert/update, delete/bulkDelete, bulkInsert/bulkUpdate, ca get
	// va listPage/listAll — nen lo con lai KHONG phai "ghi cheo tenant"
	// ma chi la nhanh mo coi: guard cho qua khi phieu cha khong ton tai
	// / payload khong mang tollId (probe pre-fix 26/26: foreign-linked
	// -> 400, own-linked -> 200, orphan -> 200, bulk orphan -> 200,
	// delete orphan -> 200 xoa cung).
	// - San proxy DON THUAN vo hieu o day neu khong thu hep grant truoc:
	// tollDetail:create/update/delete CO THAT trong DB (router
	// 1029656664769773575) va duoc chinh role clinic block-case giu:
	// YS 2298416718726168646 va Duoc si 2521363969936572416 giu du 6
	// tollDetail:* (live /permission/resources cua testys tra du
	// create/update/delete). Vi vay phuong an (i) lam HAI buoc, theo
	// quyet dinh cua chu so huu ngay 2026-11-05 (toan quyen tu quyet):
	// (1) migration v13 thu hep grant goc — bo tollDetail:create/update/
	// delete khoi cac role clinic (YS, Duoc si, role phien ban clinic
	// 2077468568630583319, role phong kham kiem tra 2314654134646620573),
	// GIU 4000 (Toan quyen he thong) va 2077468568630583311 (admin);
	// (2) san proxy nay moi co tac dung: clinic thieu quyen -> 403.
	// - KHONG khoa han 403 nhu /sys/action (iter67 item 2): o day resource
	// create/update/delete TON TAI, duoc cap co chu dich cho module
	// clinic, VA file form truc tiep views/toll/tollDetail/
	// tollDetailForm.vue van goi saveTollDetail -> /toll/tollDetail/save
	// (du hien khong gan route nao). Khoa han se giet module form cho
	// moi session ke ca admin. San proxy giu luong form cho admin.
	// - KHONG khoa o tang service: khong co backend caller nao cua
	// TollDetailService.save/delete/bulk* ngoai chinh controller nay
	// (TollInfoService inject tollDetailService nhung KHONG BAO GIO goi;
	// luong thu phi hop le qua TollInfoService.saveToll ghi TollInfo +
	// RecipelDetail, khong bao gio ghi toll_detail; mapper duy nhat cham
	// toll_detail la TollDetailDao.xml). San o controller khong the pha
	// luong saveToll — kiem chung bang grep + hoi quy iter35 + probe.
	// - Doc (get/list/listAll) GIU NGUYEN (da loc theo phieu cha tu iter63).
	// - Khong tu tao resource moi; san nay chi doc resource da co.
	@Override
	@PostMapping(value = "save")
	public ResponseEntity<JSONObject> save(@Valid @RequestBody TollDetail entity) {
		if (!hasSessionPermission("tollDetail:create") && !hasSessionPermission("tollDetail:update")) {
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
	public ResponseEntity<JSONObject> delete(@RequestBody TollDetail entity) {
		if (entity == null) {
			return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
		}
		if (!hasSessionPermission("tollDetail:delete")) {
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
	public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<TollDetail> entitys) {
		if (!hasSessionPermission("tollDetail:create")) {
			return forbidden();
		}
		List<String> ids = getService().bulkInsert(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkUpdate")
	public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<TollDetail> entitys) {
		if (!hasSessionPermission("tollDetail:update")) {
			return forbidden();
		}
		List<String> ids = getService().bulkUpdate(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkDelete")
	public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<TollDetail> entitys) {
		if (!hasSessionPermission("tollDetail:delete")) {
			return forbidden();
		}
		int rows = getService().bulkDelete(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}
}
