package com.geeke.sys.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.sys.entity.SysFile;
import com.geeke.sys.service.SysFileService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Tệp đính kèm hệ thốngController
 * @author szy
 * @version 2021-09-22
 */
@RestController
@RequestMapping(value = "/sys/sysFile")
public class SysFileController extends CrudController<SysFileService, SysFile> {

	@Autowired
	protected SysFileService sysFileService;

	@Override
	protected SysFileService getService() {
		return sysFileService;
	}

	/**
	 * L1 (iteration 59): chieu DOC cua sys_file, caveat K3 de lai.
	 * getById: guard owner truoc khi get — file chu la -> 400 (nem
	 * ServiceException nhu K3), khong tra content; file khong ton tai
	 * hoac khong resolve duoc giu hanh vi cu (NPE 500 cua get() generic
	 * khong thuoc L1, chi ghi nhan).
	 */
	@Override
	@GetMapping(value = "/{id}")
	public ResponseEntity<JSONObject> getById(@PathVariable("id") String id) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		getService().verifySysFileReadOwnership(id);
		SysFile entity = getService().get(id);
		return ResponseEntity.ok(ResultUtil.successJson(entity));
	}

	/**
	 * L1 (iteration 59): listPage loc bo dong chu la. Page bat bien
	 * (khong co setter) nen khong loc truc tiep tren page tra ve:
	 * fetch het cac chunk 200 (cap 50 chunk ~ 10000 dong, bang hien
	 * tai ~117 dong), loc, roi phan trang lai tren tap da loc va
	 * dat total = so dong nhin thay — total/limit cua UI khong lech.
	 * Param loc cua UI (vd object_id) van do service ap dung theo
	 * tung chunk nhu cu.
	 */
	@Override
	@PostMapping(value = {"list", ""})
	public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		java.util.List<SysFile> visible = new java.util.ArrayList<>();
		int fetchedOffset = 0;
		for (int guard = 0; guard < 50; guard++) {
			Page<SysFile> page = getService().listPage(searchParams.getParams(), fetchedOffset, 200,
					searchParams.getOrderby());
			if (page == null || page.getRows() == null) {
				break;
			}
			for (SysFile file : page.getRows()) {
				if (file != null && !getService().isSysFileForeignOwned(file.getId())) {
					visible.add(file);
				}
			}
			if (page.getRows().size() < 200) {
				break;
			}
			fetchedOffset += 200;
			if (page.getTotal() > 0 && fetchedOffset >= page.getTotal()) {
				break;
			}
		}
		int offset = Math.max(0, searchParams.getOffset());
		int limit = searchParams.getLimit();
		int from = Math.min(offset, visible.size());
		int to = limit > 0 ? Math.min(visible.size(), from + limit) : visible.size();
		if (to < from) {
			to = from;
		}
		Page<SysFile> result = new Page<>(visible.size(), visible.subList(from, to));
		return ResponseEntity.ok(ResultUtil.successJson(result));
	}

	/**
	 * L1 (iteration 59): listAll loc bo dong chu la — day la duong
	 * uploadFile.vue thuc su dung (listSysFileAll), nen loc o day la
	 * phan chinh cua fix chieu doc.
	 */
	@Override
	@PostMapping(value = "listAll")
	public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		java.util.List<SysFile> result = getService().listAll(searchParams.getParams(), searchParams.getOrderby());
		java.util.List<SysFile> visible = new java.util.ArrayList<>();
		if (result != null) {
			for (SysFile file : result) {
				if (file != null && !getService().isSysFileForeignOwned(file.getId())) {
					visible.add(file);
				}
			}
		}
		return ResponseEntity.ok(ResultUtil.successJson(visible));
	}

	/**
	 * K3 (iteration 58): sys_file khong co cot company_id nen
	 * verifyTenantOwnership + stored-row check cua CrudService deu pass,
	 * session chi can san hasAnySessionPermission la xoa duoc file cua
	 * bat ky tenant nao bang id tran. Override rieng 2 endpoint xoa nay
	 * (khong doi delete(SysFile)/delete(String[]) cua service hay cac
	 * caller noi bo): guard object_id truoc moi ghi, roi moi goi service
	 * nhu cu. List (liet ke) ngoai pham vi K3, giu nguyen.
	 */
	@Override
	@PostMapping(value = "delete")
	public ResponseEntity<JSONObject> delete(@RequestBody SysFile entity) {
		if (entity == null) {
			return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
		}
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		if (!beforeDelete(entity)) {
			return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50002, "Xóa trước Kiểm tra Chưa Qua"));
		}
		getService().verifySysFileDeleteOwnership(java.util.Collections.singletonList(entity.getId()));
		int rows = getService().delete(entity);
		afterDelete(entity);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}

	@Override
	@PostMapping(value = "bulkDelete")
	public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<SysFile> entitys) {
		if (!hasAnySessionPermission()) {
			return forbidden();
		}
		java.util.List<String> ids = new java.util.ArrayList<>();
		if (entitys != null) {
			for (SysFile entity : entitys) {
				if (entity != null) {
					ids.add(entity.getId());
				}
			}
		}
		getService().verifySysFileDeleteOwnership(ids);
		int rows = getService().bulkDelete(entitys);
		return ResponseEntity.ok(ResultUtil.successJson(rows));
	}
}
