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
import com.geeke.sys.entity.Theme;
import com.geeke.sys.service.ThemeService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Chủ đề hệ thốngController
 * @author lys
 * @version 2021-09-19
 */
@RestController
@RequestMapping(value = "/sys/theme")
public class ThemeController extends CrudController<ThemeService, Theme> {

	@Autowired
	protected ThemeService themeService;

	@Override
	protected ThemeService getService() {
		return themeService;
	}

	// ITER68 item 2-code-3: san phan quyen theo thao tac cho write generic
	// cua bang giao dien toan cuc (sys_theme), theo mau ResourceController /
	// RouterController (iter68 item 2-code-1/2) va GenTableController /
	// DictTypeController, thay san cu hasAnySessionPermission (bat ky
	// session nao co 1 resource cung qua). Ly do, da chung minh bang code
	// + DB + API live, khong phai suy doan:
	// - sys_theme khong co cot company_id (DESCRIBE: id, name, theme,
	// is_default, remarks, create/update, del_flag — khong company_id)
	// nen guard tenant cua CrudService (chi so company payload voi session
	// tenant) khong chan duoc: session clinic gui company = OWN la qua
	// guard roi ghi dong toan cuc vao bang giao dien (sweep iter68;
	// probe-first pre-fix cua item nay: clinic save/delete/bulkInsert/
	// bulkUpdate/bulkDelete deu 200 tao/doi/xoa dong that).
	// - KHONG khoa han 403 nhu /sys/action (iter67 item 2), vi o day KHAC
	// action: theme:create (9309) / update (9307) / delete (9306) CO THAT
	// trong DB (router 3026 sys/theme) va chi role 2000 (super) + 2001 +
	// 4000 (testfull) giu; role clinic testys giu 0 theme:*. Trang
	// views/sys/theme/index.vue gat dung cac quyen nay (permissionPrefix
	// 'theme': nut Them -> theme:create, nut Sua -> theme:update, nut Xoa
	// -> theme:delete, Sao chep -> theme:create), form themeForm.vue luu
	// qua saveTheme -> /sys/theme/save — do la luong admin hop le that,
	// can giu cho admin, khong phai lo can khoa han. Khong tu tao
	// resource moi (tao + cap resource se la thay doi quyen vai tro goc,
	// cam); san nay chi doc resource da co.
	// - KHONG khoa o tang service: backend caller duy nhat cua ThemeService
	// ngoai controller nay la AuthController (listAll — CHI DOC, tra
	// theme mac dinh khi login), khong co caller noi bo nao goi
	// save/delete/bulk* nen khoa o controller la du va khong vo luong
	// noi bo. PersonalTheme la controller/entity RIENG (ghi cheo user),
	// KHONG dong trong item nay.
	// - Doc (get/list/listAll) GIU NGUYEN trong item nay.
	@Override
	@PostMapping(value = "save")
	public ResponseEntity<JSONObject> save(@Valid @RequestBody Theme entity) {
		if (!hasSessionPermission("theme:create") && !hasSessionPermission("theme:update")) {
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
	public ResponseEntity<JSONObject> delete(@RequestBody Theme entity) {
		if (entity == null) {
			return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
		}
		if (!hasSessionPermission("theme:delete")) {
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
	public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<Theme> entitys) {
		if (!hasSessionPermission("theme:create")) {
			return forbidden();
		}
		List<String> ids = getService().bulkInsert(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkUpdate")
	public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<Theme> entitys) {
		if (!hasSessionPermission("theme:update")) {
			return forbidden();
		}
		List<String> ids = getService().bulkUpdate(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkDelete")
	public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<Theme> entitys) {
		if (!hasSessionPermission("theme:delete")) {
			return forbidden();
		}
		int rows = getService().bulkDelete(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}
}
