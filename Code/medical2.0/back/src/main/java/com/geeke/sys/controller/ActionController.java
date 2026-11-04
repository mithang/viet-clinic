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
import com.geeke.sys.entity.Action;
import com.geeke.sys.service.ActionService;

/**
 * Nhật ký thao tácController
 * @author lys
 * @version 2020-06-29
 */
@RestController
@RequestMapping(value = "/sys/action")
public class ActionController extends CrudController<ActionService, Action> {

	@Autowired
	protected ActionService actionService;

	@Override
	protected ActionService getService() {
		return actionService;
	}

	// ITER67 item 2 (8d): khoa han moi write generic qua controller (403
	// fail-closed cho moi session, ke ca super/testfull). Ly do, da chung
	// minh bang code + probe live, khong phai suy doan:
	// - sys_action la nhat ky thao tac do he thong tu ghi (DbActionSaver
	// ghi truc tiep qua ActionDao, duoc CrudService.saveAction goi sau
	// moi save/update/delete; RecycleService.restore di qua
	// RecycleController /sys/recycle/restor rieng) — khong luong nao trong
	// so do di qua controller nay, nen khoa o day khong cham audit noi bo
	// hay restore. KHONG khoa o tang service (RecycleService extends
	// ActionService; khoa service se vo restore/audit noi bo).
	// - Khong ton tai resource action:create/update (live GET
	// /permission/resources?routerId=3009 chi tra list/read/delete/export/
	// restore) nen khong co san resource de kiem, va tu tao resource moi
	// + cap cho role se la thay doi quyen role goc (cam). Nut Sua/Sao
	// chep theo dong (permission.edit/add) vi vay khong bao gio bat voi
	// bat ky role nao; nut Them tren toolbar khong bi gating nen form
	// van mo duoc — do chinh la lo (user tu ghi dong audit gia mao),
	// khong phai luong admin hop le can giu.
	// - Doc (get/list/listAll) GIU NGUYEN trong item nay. Cau hoi doc co
	// tra dong cua tenant khac khong (bang khong co cot company nen
	// ensureCompanyFilter khong co gi de loc) la item doc rieng sau nay,
	// khong nhet vao commit khoa write.
	// - He qua co y thuc: nut An theo dong (gated boi action:delete) cung
	// tra 403 tu nay — an bang chung audit la thao tac nhay cam ve toan
	// ven, khong phai luong nghiep vu can giu; cac dong da an van khoi
	// phuc duoc qua /sys/recycle/restor.
	// - Duong bulk (bulkInsert/bulkUpdate/bulkDelete) von chet 500 voi moi
	// session do ActionDao.xml thieu statement (BindingException) — san
	// 403 phu ca 3 duong de mapper co song lai sau nay cung khong mo
	// lai duong bulk mo.
	@Override
	@PostMapping(value = "save")
	public ResponseEntity<JSONObject> save(@Valid @RequestBody Action entity) {
		return forbidden();
	}

	@Override
	@PostMapping(value = "delete")
	public ResponseEntity<JSONObject> delete(@RequestBody Action entity) {
		return forbidden();
	}

	@Override
	@PostMapping(value = "bulkInsert")
	public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<Action> entitys) {
		return forbidden();
	}

	@Override
	@PostMapping(value = "bulkUpdate")
	public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<Action> entitys) {
		return forbidden();
	}

	@Override
	@PostMapping(value = "bulkDelete")
	public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<Action> entitys) {
		return forbidden();
	}
}
