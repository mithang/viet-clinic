package com.geeke.wx.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.ErrorEnum;
import com.geeke.wx.entity.WxUser;
import com.geeke.wx.service.WxUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wxUser")
public class WxUserController {
    @Autowired
    private WxUserService wxUserService;

    @PostMapping ("getCode")
    public ResponseEntity<JSONObject> getCode(@RequestBody WxUser wxUser1){
        WxUser wxUser = wxUserService.getCode(wxUser1);
        // DS84: doi code WeChat that bai (code gia/het han, chua cau
        // hinh app-id) -> 400 thay vi NPE 500; khong tao dong nao.
        if (wxUser == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        return ResponseEntity.ok(ResultUtil.successJson(wxUser));
    }

    @PostMapping ("getPhone")
    public ResponseEntity<JSONObject> getPhone(@RequestBody WxUser wxUser){
        WxUser wxUser1 = wxUserService.getPhone(wxUser);
        // DS84: nhu getCode — lay so dien thoai that bai -> 400.
        if (wxUser1 == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        return ResponseEntity.ok(ResultUtil.successJson(wxUser1));
    }

    @PostMapping("initLogin")
    public ResponseEntity<JSONObject> initLogin(@RequestBody WxUser wxUser){
        WxUser user = wxUserService.initLogin(wxUser);
        // DS84: nhu getCode — doi code that bai -> 400.
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        return ResponseEntity.ok(ResultUtil.successJson(user));
    }
}
