package com.geeke.sys.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.sys.entity.SysFile;
import com.geeke.sys.entity.SysFileContent;
import com.geeke.sys.entity.SysSeting;
import com.geeke.sys.service.SysFileContentService;
import com.geeke.sys.service.SysFileService;
import com.geeke.sys.service.SysSetingService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

/**
 * Cài đặt hệ thốngController
 *
 * @author szy
 * @version 2021-08-26
 */
@RestController
@RequestMapping(value = "/sys/sysSeting")
public class SysSetingController extends CrudController<SysSetingService, SysSeting> {

    @Autowired
    protected SysSetingService sysSetingService;

    @Autowired
    private SysFileService sysFileService;

    @Autowired
    private SysFileContentService sysFileContentService;

    @Override
    protected SysSetingService getService() {
        return sysSetingService;
    }

    /**
     * Ngoại lệ anon duy nhất của sàn CrudController: trang login gọi listAll
     * này TRƯỚC khi đăng nhập (logo/cấu hình hiển thị), và Shiro cũng để anon
     * cho đường này. Override tường minh để sàn generic không chặn nhầm trang
     * login. Các method generic khác của controller này vẫn chịu sàn.
     */
    @Override
    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        java.util.List<SysSeting> result = getService().listAll(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Lưu cấu hình hệ thống: không có resource sysSeting:* trong DB để gắn cụ
     * thể nên gắn sàn — tài khoản 0-resource bị 403. Trước đây mọi token hợp
     * lệ đều đổi được cấu hình hệ thống.
     */
    @PostMapping(value = "saveWithFile")
    public ResponseEntity<JSONObject> saveWithFile(@RequestParam("sysSeting") String sysSeting, @RequestParam("attachments") MultipartFile[] files) {
        if (!hasAnySessionPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResultUtil.errorJson(ErrorEnum.E_502));
        }
        SysSeting entity = JSONObject.parseObject(sysSeting, SysSeting.class);
        String id = sysSetingService.save(entity, files).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    /**
     * Ảnh hiển thị lại của cài đặt hệ thống
     *
     * @param id
     * @param response
     */
    @GetMapping("/getPhoto/{id}")
    public ResponseEntity getPhotoById(@PathVariable("id") String id, HttpServletResponse response, HttpServletRequest request) throws Exception {
        SysFileContent sysFileContent = sysFileContentService.get(id);

        String base64Str = Base64.getEncoder().encodeToString(sysFileContent.getFileByte());
        return ResponseEntity.ok(base64Str);

    }

    /**
     * Ảnh hiển thị lại của cài đặt hệ thống
     *
     * @param id
     * @param response
     */
    @GetMapping("/getFiled/{id}")
    public ResponseEntity getFiledId(@PathVariable("id") String id, HttpServletResponse response, HttpServletRequest request) throws Exception {
        SysFileContent sysFileContent = sysFileContentService.get(id);
        SysFile sysFile = sysFileService.get(id);
        String base64Str = Base64.getEncoder().encodeToString(sysFileContent.getFileByte());
        sysFileContent.setBase64Str(base64Str);
        sysFileContent.setName(sysFile.getName());
        return ResponseEntity.ok(sysFileContent);

    }

    /**
     * Ảnh hiển thị lại của cài đặt hệ thống
     *
     * @param id
     * @param response
     */
    @GetMapping("/getFile/{id}")
    public void getById(@PathVariable("id") String id, HttpServletResponse response, HttpServletRequest request) throws Exception {
        if (id.indexOf(".") >= 0) {
            id = id.substring(0, id.lastIndexOf("."));
        }
        String modifiedSice = request.getHeader("if-modified-since");
        //Định dạng ngày
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        if (StringUtils.isNotBlank(modifiedSice)) {
            //Thu, 04 Feb 2021 07:02:25 GMT   chuyển hóaNgày
            Date modifiedSiceDate = simpleDateFormat.parse(modifiedSice);
            SysFile sysFile = sysFileService.get(id);
            if (sysFile != null) {
                Date updateDate = sysFile.getUpdateDate();
                if (updateDate.getTime() == modifiedSiceDate.getTime()) {
                    response.setStatus(304);
                    return;
                }
            }
        }
        SysFileContent sysFileContent = sysFileContentService.get(id);
        if (sysFileContent != null) {
            response.setHeader("Content-Type", sysFileContent.getFileType());
            Date updateDate = sysFileContent.getUpdateDate();
            String format = simpleDateFormat.format(updateDate);
            response.addHeader("Last-Modified", format);
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

    @GetMapping("/filedownload")
    public void filedownload(@RequestParam("fileId") String fileId, HttpServletResponse response, HttpServletRequest request) throws Exception {
        if (fileId.indexOf(".") >= 0) {
            fileId = fileId.substring(0, fileId.lastIndexOf("."));
        }
        logger.info("fileId = {}", fileId);
        String modifiedSice = request.getHeader("if-modified-since");
        //Định dạng ngày
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        if (StringUtils.isNotBlank(modifiedSice)) {
            //Thu, 04 Feb 2021 07:02:25 GMT   chuyển hóaNgày
            Date modifiedSiceDate = simpleDateFormat.parse(modifiedSice);
            SysFile sysFile = sysFileService.get(fileId);
            if (sysFile != null) {
                Date updateDate = sysFile.getUpdateDate();
                if (updateDate.getTime() == modifiedSiceDate.getTime()) {
                    response.setStatus(304);
                    return;
                }
            }
        }
        SysFileContent sysFileContent = sysFileContentService.get(fileId);
        response.setHeader("Content-Type", sysFileContent.getFileType());
        Date updateDate = sysFileContent.getUpdateDate();
        String format = simpleDateFormat.format(updateDate);
        response.addHeader("Last-Modified", format);
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