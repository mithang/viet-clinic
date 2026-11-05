package com.geeke.sys.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.entity.User;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.common.data.Parameter;
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

	// DS17 (deep-search round 17): READ-side ownership, completing the
	// iter68 write guards (which left "Doc GIU NGUYEN"). The generic
	// CrudController reads are blind here: these tables have NO
	// company_id column, so verifyTenantOwnership early-returns, and
	// the floor is only hasAnySessionPermission — any permissioned
	// session could GET another user's row by id, or listAll/list with
	// a client-supplied user_id filter (or none) and read foreign
	// themes / saved query conditions / grid layouts (probe-proven,
	// tools/ds17-userread.py pre 28/28). Fix, controller only:
	// - getById: stored userId != session user -> 403. Nonexistent
	//   keeps the generic 200+null contract; rows with null userId
	//   pass through as before (same pattern as the write guards).
	// - listPage/listAll: strip any client user_id param and force
	//   user_id = session user (SearchParams.getParams() is called
	//   exactly once here — a second call would throw on the
	//   backticked column names it already wrote). Legit callers
	//   already filter by self (queryForm.vue passes currentUser.id),
	//   so their results are unchanged; attacker filters are ignored.
	//   No service change: AuthController reads PersonalTheme via the
	//   service directly at login and is untouched.
	private String sessionUserId() {
		User sessionUser = SessionUtils.getUser();
		return (sessionUser != null && sessionUser.getId() != null) ? sessionUser.getId() : null;
	}

	private java.util.List<Parameter> forceOwnUserParams(SearchParams searchParams) {
		java.util.List<Parameter> params = searchParams.getParams();
		if (params == null) {
			params = new java.util.ArrayList<>();
		} else {
			params = new java.util.ArrayList<>(params);
		}
		params.removeIf(p -> {
			if (p == null || p.getColumnName() == null) {
				return false;
			}
			String c = p.getColumnName().replace("`", "");
			int dot = c.lastIndexOf('.');
			if (dot >= 0) {
				c = c.substring(dot + 1);
			}
			return "user_id".equals(c);
		});
		params.add(new Parameter("user_id", "=", sessionUserId()));
		return params;
	}

	@Override
	@GetMapping(value = "/{id}")
	public ResponseEntity<JSONObject> getById(@PathVariable("id") String id) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		PersonalTheme entity = getService().get(id);
		if (entity != null && entity.getUserId() != null && !isOwnUser(entity.getUserId())) {
			return forbidden();
		}
		return ResponseEntity.ok(ResultUtil.successJson(entity));
	}

	@Override
	@PostMapping(value = {"list", ""})
	public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		if (sessionUserId() == null) {
			return forbidden();
		}
		Page<PersonalTheme> result = getService().listPage(forceOwnUserParams(searchParams), searchParams.getOffset(),
				searchParams.getLimit(), searchParams.getOrderby());
		return ResponseEntity.ok(ResultUtil.successJson(result));
	}

	@Override
	@PostMapping(value = "listAll")
	public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		if (sessionUserId() == null) {
			return forbidden();
		}
		java.util.List<PersonalTheme> result = getService().listAll(forceOwnUserParams(searchParams), searchParams.getOrderby());
		return ResponseEntity.ok(ResultUtil.successJson(result));
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
		// DS47: save-update must also check the STORED row, not only the
		// payload userId. The generic stored-row cross-check
		// (CrudService.save -> verifyTenantOwnershipAgainstStoredRow) is
		// blind here -- sys_personal_theme has no company_id, so the
		// stored company is null and the check early-returns -- and the
		// update mapper sets user_id WHERE id only, so a save with a
		// foreign row id + own userId re-homed the victim's row to the
		// caller (probe-proven, tools/ds47-verify.py pre 10/10). Same
		// stored-row pattern as delete() below; rows with null userId
		// and nonexistent ids pass through as before.
		if (entity.getId() != null) {
			PersonalTheme stored = getService().get(entity.getId());
			if (stored != null && stored.getUserId() != null && !isOwnUser(stored.getUserId())) {
				return forbidden();
			}
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
