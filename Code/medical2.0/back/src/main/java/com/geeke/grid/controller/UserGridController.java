package com.geeke.grid.controller;

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
import com.geeke.grid.entity.UserGrid;
import com.geeke.grid.service.UserGridService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Bố cục tùy chỉnhController
 * @author ycy
 * @version 2021-10-12
 */
@RestController
@RequestMapping(value = "/grid/userGrid")
public class UserGridController extends CrudController<UserGridService, UserGrid> {

	@Autowired
	protected UserGridService userGridService;

	@Override
	protected UserGridService getService() {
		return userGridService;
	}

	// ITER68 item 2-code-10, san so huu (ownership), quyet dinh chung cho
	// PersonalTheme + QueryCondition + UserGrid duoi uy quyen toan quyen
	// cua chu so huu ngay 2026-11-05 (bao cao gate de lai nguyen).
	// Ly do, da chung minh bang code + DB + API live, khong phai suy doan:
	// - user_grid la bo cuc luoi tuy chinh theo user: KHONG co cot
	// company_id, khong co router, khong co resource userGrid:* o bat
	// ky role nao (ke ca 4000) — san proxy theo thao tac se la khoa han
	// tra hinh (testfull cung 403).
	// - Lo o day KHONG phai cheo tenant ma la cheo user CUNG tenant:
	// UserGridService.save da co guard tenant iter62 (N5-P4) nhung guard
	// do CHO QUA khi user dich cung tenant voi session (testys 3001 va
	// testfull 3020 cung company OWN), roi service upsert theo cap
	// (user_id, router_id) nen GHI DE layout cua user khac. Proxy chi
	// kiem tra quyen session nen du co grant van khong sua duoc lo nay.
	// - Guard tenant iter62 trong UserGridService.save DUOC GIU NGUYEN
	// (phong thu theo lop): san controller nay tra 403 cho save cheo
	// user TRUOC KHI cham service, nen save cheo tenant gio 403 thay vi
	// 400 cua service — ca hai deu fail-closed, probe post ghi 403.
	// - Client HIEN TAI khong con caller (tuy chinh cot di qua ColumnView),
	// 4 dong live la legacy — nhung endpoint van live va du lieu legacy
	// van ton tai, nen san so huu giu endpoint song cho ghi layout cua
	// CHINH MINH (client cu/ngoai) thay vi khoa han go tinh nang.
	// - Doc (get/list/listAll) GIU NGUYEN.
	// San nay (chi o controller, khong doi service):
	// - save/bulkInsert/bulkUpdate: chi cho userId == session user.
	// userId trong -> cho qua nhu cu de @Valid/service xu ly (giữ
	// nguyen hanh vi 400/500 hien co, khong tu tao loi moi).
	// - delete/bulkDelete: tra dong da luu, chi cho xoa dong co user_id
	// == session user (delete generic khong kiem tra user nao truoc day;
	// dong khong ton tai hoac khong ro chu -> cho qua nhu cu).
	// - Khong tao resource, khong doi quyen role goc, khong doi service.
	// Delete o day la XOA MEM (UPDATE del_flag='1', khac PersonalTheme
	// xoa cung) — probe phai loc del_flag='0' va xac nhan dong con ton
	// tai voi del_flag='1'.
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
	public ResponseEntity<JSONObject> save(@Valid @RequestBody UserGrid entity) {
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
	public ResponseEntity<JSONObject> delete(@RequestBody UserGrid entity) {
		if (entity == null) {
			return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
		}
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		if (entity.getId() != null) {
			UserGrid stored = getService().get(entity.getId());
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
	public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<UserGrid> entitys) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		if (entitys != null) {
			for (UserGrid e : entitys) {
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
	public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<UserGrid> entitys) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		if (entitys != null) {
			for (UserGrid e : entitys) {
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
	public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<UserGrid> entitys) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		if (entitys != null) {
			for (UserGrid e : entitys) {
				if (e != null && e.getId() != null) {
					UserGrid stored = getService().get(e.getId());
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
