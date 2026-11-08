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
import com.geeke.org.entity.Lessee;
import com.geeke.org.service.LesseeService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Quản lý tenantController
 * @author txl
 * @version 2022-05-23
 */
@RestController
@RequestMapping(value = "/org/lessee")
public class LesseeController extends CrudController<LesseeService, Lessee> {

	@Autowired
	protected LesseeService lesseeService;

	@Override
	protected LesseeService getService() {
		return lesseeService;
	}

	// ITER68 item 2-code-11: san phan quyen theo thao tac cho write generic
	// cua so dang ky don vi thue (lessee), theo mau ResourceController /
	// RouterController / ThemeController / CodeRuleController /
	// ScheduleJobController / AdministrativeDivisionController (iter68 item
	// 2-code-1..6), thay san cu hasAnySessionPermission (bat ky session
	// nao co 1 resource cung qua).
	// Ly do, da chung minh bang code + DB + API live, khong phai suy doan:
	// - Bang lessee KHONG co cot company_id (DESCRIBE: id, user_name,
	// phone_number, company, contact_name, address_*, is_use, ... — cot
	// `company` la CHUOI TEN, khong phai tenant id) nen guard tenant cua
	// CrudService khong loc duoc; LesseeService chi mang guard theo ten
	// tu iter63 (checkLesseeNameTenant / checkLesseeTenant): tao/sua dong
	// mang ten trung org_company khac -> 400, xoa dong da luu mang ten
	// trung cong ty khac -> 400 — NHUNG ten khong trung cong ty nao
	// (orphan parity, iter63) thi cho qua, va do chinh la lo sweep iter68:
	// session clinic gui company dang CHUOI (khong gui object — gui
	// object -> 400, khong phai lo) thi save/delete/bulkInsert/bulkUpdate/
	// bulkDelete deu 200 tao/doi/xoa dong that (probe pre-fix item nay:
	// 23/23, dong i68les-save id ...183, bulk 2 dong ...189/...190).
	// - Xoa o day la XOA MEM (LesseeDao.xml delete = UPDATE lessee SET
	// del_flag = DEL_FLAG_DELETE), khac AdministrativeDivision (xoa cung):
	// probe delete phai dem theo del_flag='0' va xac nhan dong con ton tai
	// voi del_flag='1'. LesseeDao.xml CO bulkInsert/bulkUpdate/bulkDelete
	// nen bulk la lo song, khong phai quirk 500 nhu PersonalTheme/
	// QueryCondition/UserGrid.
	// - KHONG khoa han 403 nhu /sys/action (iter67 item 2), vi o day KHAC
	// action: lessee:create / update / delete CO THAT trong DB (router
	// 986248959313960962 org/lessee, chi role 4000 duoc cap router nay)
	// va duoc cac role 2001 + 4000 + 2077468568630583721 +
	// 2174053741646364675 + 2174053741646364757 giu; role clinic testys
	// (YS 2298416718726168646) giu 0 lessee:*. Trang
	// views/org/lessee/index.vue gat dung cac quyen nay (permissionPrefix
	// 'lessee': nut Them/Sao chep -> create, nut Sua -> update, nut Xoa
	// -> delete), form lesseeForm.vue luu qua saveLessee ->
	// /org/lessee/save (ca view tenants dung chung lesseeForm) — do la
	// luong admin hop le that, can giu cho admin, khong phai lo can khoa
	// han. Khong tu tao resource moi (tao + cap resource se la thay doi
	// quyen vai tro goc, cam); san nay chi doc resource da co.
	// - KHONG khoa o tang service: khong co backend caller nao cua
	// LesseeService.save/delete/bulk* ngoai chinh controller nay (chi
	// Clinic/Company entity THAM CHIEU lessee id, khong goi save);
	// Company-create (L6, /org/company/save) la endpoint khac, tuyet doi
	// khong dong trong item nay. Guard theo ten cua LesseeService van
	// chay sau san nay cho session du quyen (testfull save ten la van
	// 400 nhu cu — probe post-fix kiem chung san truoc guard).
	// - Luu y ve pham vi san: san nay thi hanh dung grant da co, khong
	// thu hep grant — session giu lessee:* (vd role supplier-admin
	// 2174053741646364675, company 0) van qua san nhu grant noi; thu hep
	// grant goc la viec cua chu so huu, cam trong item nay. Block-case
	// la testys (clinic, giu 0 lessee:*), allow-case la testfull.
	// - Doc (get/list/listAll) GIU NGUYEN trong item nay (list da duoc
	// loc theo ten o LesseeService tu iter63).
	@Override
	@PostMapping(value = "save")
	public ResponseEntity<JSONObject> save(@Valid @RequestBody Lessee entity) {
		if (!hasSessionPermission("lessee:create") && !hasSessionPermission("lessee:update")) {
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
	public ResponseEntity<JSONObject> delete(@RequestBody Lessee entity) {
		if (entity == null) {
			return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
		}
		if (!hasSessionPermission("lessee:delete")) {
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
	public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<Lessee> entitys) {
		if (!hasSessionPermission("lessee:create")) {
			return forbidden();
		}
		List<String> ids = getService().bulkInsert(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkUpdate")
	public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<Lessee> entitys) {
		if (!hasSessionPermission("lessee:update")) {
			return forbidden();
		}
		List<String> ids = getService().bulkUpdate(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkDelete")
	public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<Lessee> entitys) {
		if (!hasSessionPermission("lessee:delete")) {
			return forbidden();
		}
		int rows = getService().bulkDelete(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}
}
