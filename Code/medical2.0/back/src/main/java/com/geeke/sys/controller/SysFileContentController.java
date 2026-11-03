package com.geeke.sys.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.sys.entity.SysFile;
import com.geeke.sys.entity.SysFileContent;
import com.geeke.sys.service.SysFileContentService;
import com.geeke.sys.service.SysFileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Tệp đính kèm hệ thốngbyteController
 * @author szy
 * @version 2021-08-26
 */
@RestController
@RequestMapping(value = "/sys/fileContent")
public class SysFileContentController extends BaseController {

	@Autowired
	private SysFileContentService sysFileContentService;

	@Autowired
	private SysFileService sysFileService;

    @GetMapping("/{id}")
    public void getById(@PathVariable("id") String id, HttpServletResponse response){
        if (!hasAnySessionPermission()) { response.setStatus(HttpServletResponse.SC_FORBIDDEN); return; } // San RBAC Y2f.
        // M1 (iteration 60): duong byte tran cung ho L1 — file chu la -> 403,
        // khong ghi byte (parity SysSetingController.getFile cua L1; khong
        // dung verifySysFileReadOwnership 400 vi day la endpoint void).
        // sys_file_content.id chinh la sys_file.id nen resolver L1 ap dung
        // truc tiep; file khong resolve duoc giu hanh vi cu.
        if (sysFileService.isSysFileForeignOwned(id)) { response.setStatus(HttpServletResponse.SC_FORBIDDEN); return; }
        SysFileContent sysFileContent = sysFileContentService.get(id);
        // M5 (iteration 60): content khong ton tai -> 404, khong ghi byte,
        // thay vi NPE 500 o getFileType() (caveat L1 #1). Khong doi guard owner.
        if (sysFileContent == null) { response.setStatus(HttpServletResponse.SC_NOT_FOUND); return; }
        response.setHeader("Content-Type",sysFileContent.getFileType());
        try {
            OutputStream os = response.getOutputStream();

            os.write(sysFileContent.getFileByte());

            os.flush();

            os.close();
        } catch (IOException e) {
            logger.error("Xuất tệp thất bại", e);
        }
    }




}