package com.geeke.common.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.entity.ColumnView;
import com.geeke.common.service.ColumnViewService;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;



@RestController
@RequestMapping("/common/column")
public class ColumnViewController extends BaseController {

	@Autowired
    private ColumnViewService columnViewService;

	
    @GetMapping("")
    public ResponseEntity<JSONObject> list(@RequestParam(required = true) String routerId, @RequestParam(required = true) String tableId) {
        if (!hasAnySessionPermission()) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResultUtil.errorJson(ErrorEnum.E_502)); } // San RBAC Y2f.
    	List<ColumnView> list = columnViewService.listColumn(routerId, tableId);
    	return ResponseEntity.ok(ResultUtil.successJson(list));
    }

    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@RequestBody ColumnView entity) {
        if (!hasAnySessionPermission()) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResultUtil.errorJson(ErrorEnum.E_502)); } // San RBAC Y2f.
        int rows = columnViewService.save(entity);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }
    
    /**
     * toàn bộchọnhiển thịCột
     * @param routerId
     * @param tableId
     * @return
     */
    @GetMapping(value = "selectAll")
    public ResponseEntity<JSONObject> selectAll(@RequestParam(required = true) String routerId, @RequestParam(required = true) String tableId) {   	
        if (!hasAnySessionPermission()) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResultUtil.errorJson(ErrorEnum.E_502)); } // San RBAC Y2f.
		List<ColumnView> list = columnViewService.listColumn(routerId, tableId, true);
		return ResponseEntity.ok(ResultUtil.successJson(list));
    } 
    
    /**
     * chọnchọnMặc địnhhiển thịCột
     * @param routerId
     * @param tableId
     * @return
     */
    @GetMapping(value = "selectDefault")
    public ResponseEntity<JSONObject> selectDefault(@RequestParam(required = true) String routerId, @RequestParam(required = true) String tableId) {
        if (!hasAnySessionPermission()) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResultUtil.errorJson(ErrorEnum.E_502)); } // San RBAC Y2f.
    	columnViewService.reset(routerId, tableId);
    	List<ColumnView> list = columnViewService.listColumn(routerId, tableId);
        return ResponseEntity.ok(ResultUtil.successJson(list));
    }     

}
