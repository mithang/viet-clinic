package com.geeke.admin.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.entity.Router;
import com.geeke.admin.entity.User;
import com.geeke.admin.service.RouterService;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.utils.constants.ErrorEnum;

/**
 * Quản lý tuyến đườngController
 * @author lys
 * @version 2021-11-18
 */
@RestController
@RequestMapping(value = "/admin/router")
public class RouterController extends CrudController<RouterService, Router> {

    @Autowired
    protected RouterService routerService;

    @Override
    protected RouterService getService() {
        return routerService;
    }

    // ITER68 item 2-code-2: san phan quyen theo thao tac cho write generic
    // cua bang menu (sys_router), theo mau ResourceController (iter68
    // item 2-code-1) / GenTableController / DictTypeController, thay san
    // cu hasAnySessionPermission (bat ky session nao co 1 resource cung
    // qua). Ly do, da chung minh bang code + DB + probe live, khong phai
    // suy doan:
    // - sys_router khong co cot company_id nen guard tenant cua
    // CrudService (chi so company payload voi session tenant) khong chan
    // duoc: session clinic gui company = OWN la qua guard roi ghi dong
    // toan cuc vao bang menu (sweep iter68 A3; probe-first pre-fix cua
    // item nay: clinic save/delete/bulkInsert/bulkUpdate/bulkDelete deu
    // 200 tao/doi/xoa dong that).
    // - KHONG khoa han 403 nhu /sys/action (iter67 item 2), vi o day KHAC
    // action: router:create (9021) / update (9027) / delete (9042) CO THAT
    // trong DB (router 3007 admin/router) va chi role 2000 (super) + 4000
    // (testfull) giu; role clinic testys giu 0 router:*. Trang
    // views/admin/router/index.vue co permissionPrefix 'router', map
    // add/edit/remove tu router:create/update/delete (searchParamsBuilder
    // mapPermissions): nut Them / Them cap duoi / Sao chep -> create, nut
    // Sua -> update, nut Xoa -> delete; routerForm.vue luu qua saveRouter
    // -> /admin/router/save — do la luong admin hop le that, can giu cho
    // admin, khong phai lo can khoa han. Khong tu tao resource moi (tao
    // + cap resource se la thay doi quyen vai tro goc, cam); san nay chi
    // doc resource da co.
    // - KHONG khoa o tang service: backend caller duy nhat cua
    // RouterService ngoai controller nay la PermissionController
    // (getAllRoutersByVersion / listAll — CHI DOC, phuc vu trang cap
    // quyen), khong co caller noi bo nao goi save/delete/bulk* nen khoa
    // o controller la du va khong vo luong noi bo.
    // - Doc (get/list/listAll/tree/getUserIndateWarning) GIU NGUYEN.
    // - Luu y probe: delete/bulkDelete cua Router can entity day du
    // (parent/ids lay tu GET) moi chay; payload toi thieu {id, company}
    // tra 500 voi MOI session ngay ca pre-fix (quirk san co cua
    // TreeEntity/service, khong phai hanh vi cua san moi) — probe dung
    // entity day du cho ca pre va post.
    @Override
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@Valid @RequestBody Router entity) {
        if (!hasSessionPermission("router:create") && !hasSessionPermission("router:update")) {
            return forbidden();
        }
        if (!beforeSave(entity)) {
            return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50002, "Lưu trước Kiểm tra Chưa Qua"));
        }
        String id = getService().save(entity).getId();
        afterSave(entity);
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @Override
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody Router entity) {
        if (entity == null) {
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        if (!hasSessionPermission("router:delete")) {
            return forbidden();
        }
        if (!beforeDelete(entity)) {
            return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50002, "Xóa trước Kiểm tra Chưa Qua"));
        }
        int rows = getService().delete(entity);
        afterDelete(entity);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    @Override
    @PostMapping(value = "bulkInsert")
    public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<Router> entitys) {
        if (!hasSessionPermission("router:create")) {
            return forbidden();
        }
        List<String> ids = getService().bulkInsert(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }

    @Override
    @PostMapping(value = "bulkUpdate")
    public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<Router> entitys) {
        if (!hasSessionPermission("router:update")) {
            return forbidden();
        }
        List<String> ids = getService().bulkUpdate(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }

    @Override
    @PostMapping(value = "bulkDelete")
    public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<Router> entitys) {
        if (!hasSessionPermission("router:delete")) {
            return forbidden();
        }
        int rows = getService().bulkDelete(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    @PostMapping(value = "tree")
    public ResponseEntity<JSONObject> tree(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        List<Router> result = routerService.tree(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    // Y2b: chỉ chính mình mới được xem cảnh báo hết hạn của mình, mẫu
    // SysCollectController. Caller duy nhất trong client là header.vue, luôn
    // gửi currentUser.id — không có luồng admin xem hộ user khác.
    private boolean isSelf(String userId) {
        if (userId == null) {
            return false;
        }
        try {
            User sessionUser = SessionUtils.getUser();
            return sessionUser != null && userId.equals(sessionUser.getId());
        } catch (Exception e) {
            return false;
        }
    }

    //LấyNgười dùngCó hay khôngsở hữucóThời hạn hiệu lựctrướccảnh báoQuyền
    @GetMapping("getUserIndateWarning/{userId}")
    public ResponseEntity<JSONObject> getUserIndateWarning(@PathVariable("userId") String userId){
        if (!hasAnySessionPermission() || !isSelf(userId)) {
            return forbidden();
        }
        List<String> userIndateWarning = routerService.getUserIndateWarning(userId);
        return ResponseEntity.ok(ResultUtil.successJson(userIndateWarning));
    }
}