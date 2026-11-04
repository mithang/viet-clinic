package com.geeke.schedule.controller;

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
import com.geeke.schedule.entity.ScheduleJob;
import com.geeke.schedule.service.ScheduleJobService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Quản lý tác vụ định kỳController
 * @author shenzy
 * @version 2021-10-28
 */
@RestController
@RequestMapping(value = "/schedule/scheduleJob")
public class ScheduleJobController extends CrudController<ScheduleJobService, ScheduleJob> {

	@Autowired
	protected ScheduleJobService scheduleJobService;

	@Override
	protected ScheduleJobService getService() {
		return scheduleJobService;
	}

	// ITER68 item 2-code-5: san phan quyen theo thao tac cho write generic
	// cua bang dinh nghia job toan cuc (schedule_job), theo mau
	// ResourceController / RouterController / ThemeController / CodeRuleController
	// (iter68 item 2-code-1/2/3/4) va GenTableController / DictTypeController,
	// thay san cu hasAnySessionPermission (bat ky session nao co 1 resource
	// cung qua). Ly do, da chung minh bang code + DB + API live, khong
	// phai suy doan:
	// - schedule_job khong co cot company_id (DESCRIBE: id, name, code,
	// cron, status, is_lock, remarks, create/update, del_flag — khong
	// company_id) nen guard tenant cua CrudService (chi so company payload
	// voi session tenant) khong chan duoc: session clinic gui company = OWN
	// la qua guard roi ghi dong toan cuc (sweep iter68; probe-first pre-fix
	// cua item nay: clinic save/delete/bulkInsert/bulkUpdate/bulkDelete
	// deu 200 tao/doi/xoa dong that; khac CodeRule, ScheduleJobDao.xml CO
	// bulkInsert/bulkUpdate/bulkDelete nen bulk la lo song, khong phai
	// quirk 500).
	// - KHONG khoa han 403 nhu /sys/action (iter67 item 2), vi o day KHAC
	// action: scheduleJob:create (683902640570327050) / update (...052) /
	// delete (...056) CO THAT trong DB (router 683902640570327041
	// schedule/scheduleJob) va chi role 2000 (super) + 2001 + 4000 (testfull)
	// giu; role clinic testys giu 0 scheduleJob:*. Trang
	// views/schedule/scheduleJob/index.vue gat dung cac quyen nay
	// (permissionPrefix 'scheduleJob': nut Them -> scheduleJob:create, nut
	// Sua -> scheduleJob:update, nut Xoa -> scheduleJob:delete), form
	// scheduleJobForm.vue luu qua saveScheduleJob ->
	// /schedule/scheduleJob/save — do la luong admin hop le that, can giu
	// cho admin, khong phai lo can khoa han. Khong tu tao resource moi
	// (tao + cap resource se la thay doi quyen vai tro goc, cam); san nay
	// chi doc resource da co.
	// - KHONG khoa o tang service: khong co backend caller nao cua
	// ScheduleJobService.save/delete/bulk* ngoai chinh controller nay.
	// ScheduleUtil chi duoc FollowupReminderJob dung cho job cung ngam cua
	// chinh no, khong bao gio doc bang schedule_job; khong co Quartz,
	// khong co loader nap dong schedule_job khi khoi dong, khong co
	// controller DynamicTask o backend (cac URL startTask/endTask/
	// restartTask cua frontend la loi goi chet, ngoai pham vi item nay).
	// Service.save chi validate bieu thuc cron, khong dang ky gi vao
	// scheduler, nen dong seed isLock=1 (vo hieu hoa) khong bao gio chay
	// va moi seed deu bi xoa trong cung luot probe — khong seed nao song
	// sot qua mot lan restart backend. Khong sua logic nap scheduler
	// trong item nay.
	// - Luu y khac CodeRule: ScheduleJob khong override getCompany() nen
	// save KHONG company roi vao @Valid 400 truoc ca san cu lan san moi
	// (khong phai lo fallback, khong phai hanh vi cua san nay).
	// - Doc (get/list/listAll) GIU NGUYEN trong item nay.
	@Override
	@PostMapping(value = "save")
	public ResponseEntity<JSONObject> save(@Valid @RequestBody ScheduleJob entity) {
		if (!hasSessionPermission("scheduleJob:create") && !hasSessionPermission("scheduleJob:update")) {
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
	public ResponseEntity<JSONObject> delete(@RequestBody ScheduleJob entity) {
		if (entity == null) {
			return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
		}
		if (!hasSessionPermission("scheduleJob:delete")) {
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
	public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<ScheduleJob> entitys) {
		if (!hasSessionPermission("scheduleJob:create")) {
			return forbidden();
		}
		List<String> ids = getService().bulkInsert(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkUpdate")
	public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<ScheduleJob> entitys) {
		if (!hasSessionPermission("scheduleJob:update")) {
			return forbidden();
		}
		List<String> ids = getService().bulkUpdate(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(ids));
	}

	@Override
	@PostMapping(value = "bulkDelete")
	public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<ScheduleJob> entitys) {
		if (!hasSessionPermission("scheduleJob:delete")) {
			return forbidden();
		}
		int rows = getService().bulkDelete(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}
}
