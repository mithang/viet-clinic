package com.geeke.druginteraction.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geeke.druginteraction.entity.DrugInteractionRule;
import com.geeke.druginteraction.service.DrugInteractionRuleService;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Quy tắc tương tác thuốc Controller (G5 research loop).
 *
 * Chỉ 3 endpoint, đều fail-closed với tài khoản 0 quyền
 * (hasAnySessionPermission — tài khoản không có resource nào nhận 403).
 * Không kế thừa CrudController nên không có route CRUD chung nào.
 * Không thêm dòng anon nào vào Shiro: cả 3 endpoint đều rơi vào
 * "/**" -> authc, tức bắt buộc đăng nhập rồi mới tới kiểm tra quyền.
 *
 * GET  /drug-interaction/rules — danh sách quy tắc của tenant đang đăng nhập.
 * POST /drug-interaction/rules — tạo quy tắc cho tenant đang đăng nhập;
 *      dữ liệu sai trả 400.
 * POST /drug-interaction/check — body {drugNames: [...]}; trả {matches: [...]},
 *      so khớp không phân biệt thứ tự/hoa-thường/khoảng trắng, khử trùng.
 *      Đầu vào rỗng trả matches rỗng, không phải lỗi.
 *
 * Cảnh báo chỉ bao phủ các quy tắc phòng khám tự cấu hình; bảng được tạo
 * trống và không seed quy tắc lâm sàng nào. Cảnh báo không chặn lưu đơn.
 */
@RestController
@RequestMapping(value = "/drug-interaction")
public class DrugInteractionController extends BaseController {

    @Autowired
    private DrugInteractionRuleService drugInteractionRuleService;

    @GetMapping(value = "rules")
    public ResponseEntity<JSONObject> listRules() {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        List<DrugInteractionRule> rules = drugInteractionRuleService.listRules(
                SessionUtils.getLoginTenantId());
        return ResponseEntity.ok(ResultUtil.successJson(rules));
    }

    @PostMapping(value = "rules")
    public ResponseEntity<JSONObject> createRule(@RequestBody DrugInteractionRule input) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        try {
            DrugInteractionRule saved = drugInteractionRuleService.createRule(
                    SessionUtils.getLoginTenantId(), input);
            return ResponseEntity.ok(ResultUtil.successJson(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResultUtil.errorJson(ErrorEnum.E_50002, e.getMessage()));
        }
    }

    @PostMapping(value = "check")
    public ResponseEntity<JSONObject> check(@RequestBody JSONObject body) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        List<String> drugNames = new ArrayList<>();
        if (body != null) {
            JSONArray names = body.getJSONArray("drugNames");
            if (names != null) {
                for (int i = 0; i < names.size(); i++) {
                    Object name = names.get(i);
                    if (name != null) {
                        drugNames.add(String.valueOf(name));
                    }
                }
            }
        }
        List<JSONObject> matches = drugInteractionRuleService.checkInteractions(
                SessionUtils.getLoginTenantId(), drugNames);
        JSONObject data = new JSONObject();
        data.put("matches", matches);
        return ResponseEntity.ok(ResultUtil.successJson(data));
    }

    private ResponseEntity<JSONObject> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResultUtil.errorJson(ErrorEnum.E_502));
    }
}
