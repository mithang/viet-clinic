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
import com.geeke.admin.entity.User;
import com.geeke.common.controller.CrudController;
import com.geeke.sys.entity.PersonalTheme;
import com.geeke.sys.service.PersonalThemeService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Chủ đề hệ thốngController
 * @author lys
 * @version 2021-07-16
 */
@RestController
@RequestMapping(value = "/sys/personalTheme")
public class PersonalThemeController extends CrudController<PersonalThemeService, PersonalTheme> {

	@Autowired
	protected PersonalThemeService personalThemeService;

	@Override
	protected PersonalThemeService getService() {
		return personalThemeService;
	}

	// ITER68 item 2-code-8, san so huu (ownership), quyet dinh chung cho
	// PersonalTheme + QueryCondition + UserGrid duoi uy quyen toan quyen
	// cua chu so huu ngay 2026-11-05 (bao cao gate de lai nguyen).
	// Ly do, da chung minh bang code + DB + API live, khong phai suy doan:
	// - sys_personal_theme la theme ca nhan theo user: KHONG co cot
	// company_id, khong co router, khong co resource personalTheme:* o
	// bat ky role nao (ke ca 4000) — san proxy theo thao tac se la khoa
	// han tra hinh (testfull cung 403), va du co grant thi proxy van
	// khong sua duoc lo cheo user (proxy chi kiem tra quyen session,
	// khong kiem tra payload userId co bang session user hay khong).
	// - Luong hop le duy nhat, co that: panel Settings
	// (store/modules/settings.js saveSetting -> savePersonalTheme,
	// resetSetting -> deletePersonalTheme) luu/xoa theme cua CHINH MINH.
	// - AuthController chi DOC listAll luc login, khong bi san write anh
	// huong. Doc (get/list/listAll) GIU NGUYEN.
	// San nay (chi o controller, khong doi service):
	// - save/bulkInsert/bulkUpdate: chi cho userId == session user.
	// userId trong -> cho qua nhu cu de @Valid/service xu ly (giữ
	// nguyen hanh vi 400/500 hien co, khong tu tao loi moi).
	// - delete/bulkDelete: tra dong da luu, chi cho xoa dong co user_id
	// == session user (delete chi gui {id} nen bat buoc tra dong da luu;
	// dong khong ton tai hoac khong ro chu -> cho qua nhu cu).
	// - Khong tao resource, khong doi quyen role goc, khong doi service.
	// Delete o day la XOA CUNG (mapper DELETE FROM, khac QueryCondition/
	// UserGrid xoa mem) — probe phai kiem tra dong BIEN MAT, khong loc
	// del_flag. user_id UNIQUE: moi user toi da 1 dong.
	private boolean isOwnUser(Long userId) {
		if (userId == null) {
			return false;
		}
		User sessionUser = SessionUtils.getUser();
		return sessionUser != null && sessionUser.getId() != null
				&& sessionUser.getId().equals(String.valueOf(userId));
	}

	@Override
	@PostMapping(value = "save")
	public ResponseEntity<JSONObject> save(@Valid @RequestBody PersonalTheme entity) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		if (entity.getUserId() != null && !isOwnUser(entity.getUserId())) {
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
	public ResponseEntity<JSONObject> delete(@RequestBody PersonalTheme entity) {
		if (entity == null) {
			return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
		}
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		if (entity.getId() != null) {
			PersonalTheme stored = getService().get(entity.getId());
			if (stored != null && stored.getUserId() != null && !isOwnUser(stored.getUserId())) {
				return forbidden();
			}
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
	public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<PersonalTheme> entitys) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		if (entitys != null) {
			for (PersonalTheme e : entitys) {
				if (e != null && e.getUserId() != null && !isOwnUser(e.getUserId())) {
					return forbidden();
				}
			}
		}
		List<String> ids = getService().bulkInsert(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkUpdate")
	public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<PersonalTheme> entitys) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		if (entitys != null) {
			for (PersonalTheme e : entitys) {
				if (e != null && e.getUserId() != null && !isOwnUser(e.getUserId())) {
					return forbidden();
				}
			}
		}
		List<String> ids = getService().bulkUpdate(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkDelete")
	public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<PersonalTheme> entitys) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		if (entitys != null) {
			for (PersonalTheme e : entitys) {
				if (e != null && e.getId() != null) {
					PersonalTheme stored = getService().get(e.getId());
					if (stored != null && stored.getUserId() != null && !isOwnUser(stored.getUserId())) {
						return forbidden();
					}
				}
			}
		}
		int rows = getService().bulkDelete(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}

}
