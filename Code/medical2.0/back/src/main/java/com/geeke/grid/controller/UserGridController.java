package com.geeke.grid.controller;

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
		UserGrid entity = getService().get(id);
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
		Page<UserGrid> result = getService().listPage(forceOwnUserParams(searchParams), searchParams.getOffset(),
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
		java.util.List<UserGrid> result = getService().listAll(forceOwnUserParams(searchParams), searchParams.getOrderby());
		return ResponseEntity.ok(ResultUtil.successJson(result));
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
			// DS48: bulkUpdate must ALSO check the STORED row, not only the
			// payload userId -- same hole DS47 closed for save() on the
			// sibling controllers. The generic stored-row cross-check
			// (CrudService.bulkUpdate ->
			// verifyTenantOwnershipAgainstStoredRow) is blind here:
			// user_grid has no company_id, so the stored company is null
			// and the check early-returns -- and unlike the single-row
			// update mapper (WHERE id AND user_id), the bulkUpdate CASE
			// mapper can only filter WHERE id IN, so a bulkUpdate with a
			// victim row id + own userId would overwrite the victim's
			// layout. Same stored-row pattern as delete() below; rows
			// with null userId and nonexistent ids pass through.
			for (UserGrid e : entitys) {
				if (e != null && e.getId() != null) {
					UserGrid stored = getService().get(e.getId());
					if (stored != null && stored.getUserId() != null && !isOwnUser(stored.getUserId())) {
						return forbidden();
					}
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
