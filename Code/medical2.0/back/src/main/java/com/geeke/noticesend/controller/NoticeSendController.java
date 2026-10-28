package com.geeke.noticesend.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.noticesend.entity.NoticeSend;
import com.geeke.noticesend.service.NoticeSendService;
import com.geeke.org.entity.Company;
import com.geeke.org.service.CompanyService;
import com.geeke.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * Gửi thông báo Controller.
 *
 * CRUD chuẩn (list/listAll/save/delete/...) kế thừa từ CrudController.
 * Ngoài ra có GET /tree/{code} mà frontend noticeSendForm gọi khi mở
 * form Thêm (getCompanyTree('2')) để lấy cây đơn vị nhận thông báo cho
 * cascader Người nhận. Cây trả về là cây org_company thật, xây bằng
 * CompanyService.tree (cùng service với POST /org/company/tree), không
 * phải dữ liệu rỗng hay hard-code. Tham số code là selector cũ của
 * frontend (hiện chỉ gọi '2'); backend chấp nhận mọi code và trả cùng
 * một cây công ty thật vì không còn định nghĩa selector nào khác trong
 * code hiện tại.
 */
@RestController
@RequestMapping(value = "/noticesend/noticeSend")
public class NoticeSendController extends CrudController<NoticeSendService, NoticeSend> {

    @Autowired
    protected NoticeSendService noticeSendService;

    @Autowired
    private CompanyService companyService;

    @Override
    protected NoticeSendService getService() {
        return noticeSendService;
    }

    @GetMapping(value = "tree/{code}")
    public ResponseEntity<JSONObject> tree(@PathVariable("code") String code) {
        List<Company> result = companyService.tree(Collections.emptyList(), null);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }
}
