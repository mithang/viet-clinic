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
import com.geeke.sys.entity.CodeRule;
import com.geeke.sys.service.CodeRuleService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Quy tắc mã hệ thốngController
 * @author lys
 * @version 2021-05-17
 */
@RestController
@RequestMapping(value = "/sys/codeRule")
public class CodeRuleController extends CrudController<CodeRuleService, CodeRule> {

	@Autowired
	protected CodeRuleService codeRuleService;

	@Override
	protected CodeRuleService getService() {
		return codeRuleService;
	}

	// ITER68 item 2-code-4: san phan quyen theo thao tac cho write generic
	// cua bang quy tac ma toan cuc (sys_code_rule), theo mau
	// ResourceController / RouterController / ThemeController (iter68
	// item 2-code-1/2/3) va GenTableController / DictTypeController, thay
	// san cu hasAnySessionPermission (bat ky session nao co 1 resource
	// cung qua). Ly do, da chung minh bang code + DB + API live, khong
	// phai suy doan:
	// - sys_code_rule khong co cot company_id (DESCRIBE: id, code, name,
	// rule_def, remarks, create/update, del_flag — khong company_id) nen
	// guard tenant cua CrudService (chi so company payload voi session
	// tenant) khong chan duoc: session clinic gui company = OWN la qua
	// guard roi ghi dong toan cuc (sweep iter68; probe-first pre-fix cua
	// item nay: clinic save/delete deu 200 tao/xoa dong that). Them mot
	// duong lo rieng cua entity nay: CodeRule.getCompany() fallback ve
	// tenant cua session khi payload khong co company, nen save KHONG
	// company cung 200 tao dong that — san moi chay truoc service nen
	// chan ca hai duong.
	// - KHONG khoa han 403 nhu /sys/action (iter67 item 2), vi o day KHAC
	// action: codeRule:create (9203) / update (9205) / delete (9206) CO
	// THAT trong DB (router 3024 sys/codeRule) va chi role 2000 (super)
	// + 2001 + 4000 (testfull) giu; role clinic testys giu 0 codeRule:*.
	// Trang views/sys/codeRule/index.vue gat dung cac quyen nay
	// (permissionPrefix 'codeRule': nut Them -> codeRule:create, nut Sua
	// -> codeRule:update, nut Xoa -> codeRule:delete, Sao chep ->
	// codeRule:create), form codeRuleForm.vue luu qua saveCodeRule ->
	// /sys/codeRule/save — do la luong admin hop le that, can giu cho
	// admin, khong phai lo can khoa han. Khong tu tao resource moi (tao
	// + cap resource se la thay doi quyen vai tro goc, cam); san nay chi
	// doc resource da co.
	// - KHONG khoa o tang service: backend caller duy nhat cua
	// CodeRuleService ngoai controller nay CHI DOC — SequenceService
	// (dao.getRuleDefByCode, sinh ma doc rule) va CompanyCodeRuleController
	// (listNotInCompany); khong co caller noi bo nao goi save/delete/
	// bulk* nen khoa o controller la du va khong vo sinh ma.
	// CompanyCodeRuleController luu CompanyCodeRule (bang rieng, co
	// company_id, da guard) — KHONG dong trong item nay.
	// - Bulk: CodeRuleDao.xml KHONG co bulkInsert/bulkUpdate/bulkDelete
	// nen bulk 500 voi moi session ke ca testfull truoc fix (quirk mapper
	// da chung minh pre-fix, cung ho genTable bulkInsert); san moi cho
	// clinic 403 truoc khi cham mapper, testfull giu 500 khong doi —
	// KHONG sua mapper trong item nay.
	// - Doc (get/list/listAll) GIU NGUYEN trong item nay.
	@Override
	@PostMapping(value = "save")
	public ResponseEntity<JSONObject> save(@Valid @RequestBody CodeRule entity) {
		if (!hasSessionPermission("codeRule:create") && !hasSessionPermission("codeRule:update")) {
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
	public ResponseEntity<JSONObject> delete(@RequestBody CodeRule entity) {
		if (entity == null) {
			return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
		}
		if (!hasSessionPermission("codeRule:delete")) {
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
	public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<CodeRule> entitys) {
		if (!hasSessionPermission("codeRule:create")) {
			return forbidden();
		}
		List<String> ids = getService().bulkInsert(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkUpdate")
	public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<CodeRule> entitys) {
		if (!hasSessionPermission("codeRule:update")) {
			return forbidden();
		}
		List<String> ids = getService().bulkUpdate(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkDelete")
	public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<CodeRule> entitys) {
		if (!hasSessionPermission("codeRule:delete")) {
			return forbidden();
		}
		int rows = getService().bulkDelete(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}
}
