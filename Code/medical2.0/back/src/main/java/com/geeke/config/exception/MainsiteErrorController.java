package com.geeke.config.exception;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;

/**
 * @author: hxy
 * @description: Lỗi hệ thốngchặn, chínhcần có Đối với404 của Lỗi
 * @date: 2017/10/24 10:31
 */
@Controller
public class MainsiteErrorController implements ErrorController {

    private static final String ERROR_PATH = "/error";

    /**
     * chínhcần có đăng nhậpsau của đủ loạiLỗiđường dẫn  404trangmặtsửa là Trả vềnàyjson
     * ChưaĐăng nhập của tình hìnhdưới,lớnMột phầngiao diệnđềuĐãbịshirochặn,Trả vềđểNgười dùngĐăng nhậprồi
     *
     * @return 501 của Thông tin lỗijson
     */
    @RequestMapping(value = ERROR_PATH)
    @ResponseBody
    public JSONObject handleError() {
        return ResultUtil.errorJson(ErrorEnum.E_501);
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

}

