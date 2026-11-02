package com.geeke.largeScreen.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.largeScreen.entity.Basics;
import com.geeke.largeScreen.service.ScreenService;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Màn hình dữ liệu lớnController
 * @author zhanghan
 * @version 2023-09-05
 */
@RestController
@RequestMapping(value = "/largeScreen")
public class ScreenController extends BaseController {

	@Autowired
	private ScreenService screenService;

	/** cơ bảnDữ liệuLấy */
	@GetMapping("/{parentId}")
	public ResponseEntity<JSONObject> getBasics(@PathVariable("parentId") String parentId) {
		if (!hasAnySessionPermission()) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResultUtil.errorJson(ErrorEnum.E_502)); } // San RBAC Y2f.
		Basics entity = screenService.getBasics(parentId);
		return ResponseEntity.ok(ResultUtil.successJson(entity));
	}

}