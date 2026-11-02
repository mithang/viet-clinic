package com.geeke.taskmanagement.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.cure.entity.InspectionCheck;
import com.geeke.cure.entity.InspectionCheckInfo;
import com.geeke.outpatient.entity.Blmb;
import com.geeke.sys.controller.BaseController;
import com.geeke.sys.utils.SessionUtils;
import com.geeke.taskmanagement.entity.gettaskrcvo;
import com.geeke.taskmanagement.entity.taskmanagement;
import com.geeke.taskmanagement.service.TaskmanageService;
import com.geeke.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;


@RestController
@RequestMapping(value = "/taskmanagement/taskmanage")
public class TaskmanageController extends BaseController {

    @Autowired
    private TaskmanageService taskmanageService;

    /*
    Thêm tác vụ mới
     */
    @PostMapping(value = "inserttask")
    public ResponseEntity<JSONObject> inserttask(@RequestBody taskmanagement task)  {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        return ResponseEntity.ok(ResultUtil.successJson(taskmanageService.inserttask(task)));
    }

    /*
    Truy vấntác vụ
     */
    @PostMapping(value = "selecttasklist")
    public ResponseEntity<JSONObject> selecttasklist(@RequestBody gettaskrcvo rcvo){
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        return ResponseEntity.ok(ResultUtil.successJson(taskmanageService.selecttasklist(rcvo)));
    }



    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@RequestParam("entity") String strInspection,
                                           @RequestParam("fileIdUploads") MultipartFile[] fileIdUploads,  // Tệp: Ảnh người dùng
                                           @RequestParam(value = "deleteIds",required = false)String strDeleteIds,
                                           @RequestParam("countId") String countId) throws IOException {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        taskmanagement task = JSONObject.parseObject(strInspection, taskmanagement.class);
        String[] deleteIds = JSONObject.parseObject(strDeleteIds, String[].class);

        return ResponseEntity.ok(ResultUtil.successJson(taskmanageService.save(task,deleteIds,fileIdUploads,countId)));
    }

    /**
     * tác vụLưu
     * @param strInspection   Chi tiết tác vụ
     * @param fileIdUploads   Tải lênảnh
     * @param strDeleteIds    ảnhXóaid
     * @return
     * @throws IOException
     */
    @PostMapping(value = "storage")
    public ResponseEntity<JSONObject> storage(@RequestParam("entity") String strInspection,
                                           @RequestParam("fileIdUploads") MultipartFile[] fileIdUploads,  // Tệp: Ảnh người dùng
                                           @RequestParam(value = "deleteIds",required = false)String strDeleteIds , @RequestParam("countId") String countId) throws IOException {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        taskmanagement task = JSONObject.parseObject(strInspection, taskmanagement.class);
        String[] strings = JSONObject.parseObject(strDeleteIds, String[].class);

        return ResponseEntity.ok(ResultUtil.successJson(taskmanageService.storage(task,fileIdUploads,strings,countId)));
    }

    /**
     * tác vụSửasauphátphát hành
     * @param taskmanagement
     * @return
     */
    @PostMapping(value = "release")
    public ResponseEntity<JSONObject> release(@RequestBody taskmanagement taskmanagement){
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        taskmanagement taskmanagement1 = taskmanageService.release(taskmanagement);
        return ResponseEntity.ok(ResultUtil.successJson(taskmanagement1));
    }

    /**
     * tác vụXóa
     * @param taskmanagement
     * @return
     */
    @PostMapping("delete")
    public ResponseEntity delete(@RequestBody taskmanagement taskmanagement){
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
       taskmanagement taskmanagement1 =  taskmanageService.delete(taskmanagement);
       return ResponseEntity.ok(ResultUtil.successJson(taskmanagement1));
    }

    /*
    Truy vấnhành nghề dòngngườiDữ liệu
    */
    @PostMapping(value = "getusertree")
    public ResponseEntity<JSONObject> getusertree(@RequestBody gettaskrcvo rcvo){
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        return ResponseEntity.ok(ResultUtil.successJson(taskmanageService.getusertree(rcvo)));
    }


    /*
       Lấyphản hồitình hình
       */
    @PostMapping(value = "getfeedbacktable")
    public ResponseEntity<JSONObject> getfeedbacktable(@RequestBody gettaskrcvo rcvo){
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        return ResponseEntity.ok(ResultUtil.successJson(taskmanageService.getfeedbacktable(rcvo)));
    }

    /*
    tác vụxét duyệt
     */
    @PostMapping(value = "updateaudit")
    public ResponseEntity<JSONObject> updateaudit(@RequestBody taskmanagement task){
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(com.geeke.utils.constants.ErrorEnum.E_502));
        }
        return ResponseEntity.ok(ResultUtil.successJson(taskmanageService.updateaudit(task)));
    }



}
