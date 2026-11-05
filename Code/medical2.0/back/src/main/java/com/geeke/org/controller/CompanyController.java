package com.geeke.org.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.org.entity.Company;
import com.geeke.org.service.CompanyService;
import com.geeke.sys.utils.SessionUserDto;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Quản lý công tyController
 * @author lys
 * @version 2022-05-25
 */
@RestController
@RequestMapping(value = "/org/company")
public class CompanyController extends CrudController<CompanyService, Company> {

    @Autowired
    protected CompanyService companyService;

    @Override
    protected CompanyService getService() {
        return companyService;
    }

    // DS16: override get generic — Company khong co company_id nen
    // CrudService.verifyTenantOwnership bo qua, doc don /org/company/{id}
    // truoc day tra bat ky company nao (gom appSecret/privateKey).
    // Id khong ton tai giu hop dong generic (200 + data null); chi
    // dong ngoai nhom co so cua session moi 403.
    @Override
    @GetMapping(value = "/{id}")
    public ResponseEntity<JSONObject> getById(@PathVariable("id") String id) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        Company entity = companyService.get(id);
        if (entity != null && !companyService.isCompanyVisibleToSession(entity)) {
            return forbidden();
        }
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    @PostMapping(value = "getCompanys")
    public ResponseEntity<JSONObject> getCompanys(@RequestBody String id) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        List<Company> result = companyService.getCompanys(id);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "tree")
    public ResponseEntity<JSONObject> tree(@RequestBody SearchParams searchParams) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        List<Company> result = companyService.tree(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    @PostMapping(value = "saveWithFile")
    public ResponseEntity<JSONObject> saveWithFile(@RequestParam("entity") String strEntity,
      @RequestParam("fileIdUploads") MultipartFile[] fileIdUploads,
      @RequestParam("deleteIds")String strDeleteIds) throws java.io.IOException {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        // ITER68 Company-create: san phan quyen theo thao tac cho write
        // cua /org/company (mau code-1..6, quyet dinh 2026-11-05 duoi uy
        // quyen toan quyen cua chu so huu — sweep de lai Company-create
        // cho quyet dinh rieng vi day la tao tenant).
        // Ly do, da chung minh bang code + DB + API live:
        // - Sweep A12: session clinic (testys) tao tenant con qua
        // generic save (parent=OWN -> 200 + dong), qua saveWithFile
        // (duong UI that companyForm saveCompany -> 200 + dong), qua
        // bulkInsert/bulkUpdate, trong khi san cu chi la
        // hasAnySessionPermission (bat ky session nao co 1 resource
        // cung qua). Update dong foreign thi guard service
        // checkCompanySaveOwnership da chan 400, nhung CREATE thi
        // guard bo qua (blank id khong co dong da luu de doi chieu,
        // parent trong nhom thi qua).
        // - Khac PropertySet/PersonalTheme/QueryCondition/UserGrid:
        // company:create/update/delete CO THAT va chi duoc giu boi
        // system 2001 + full 4000 (clinic giu 0) — san proxy ap thang
        // duoc, khong can thu hep grant, khong can san so huu.
        // - Phong kham tao moi qua /org/clinic/save (ClinicService.save
        // -> CompanyService.save o TANG SERVICE) KHONG BI ANH HUONG
        // boi san controller nay; floor tao clinic giu nguyen theo
        // quyet dinh iter22. Guard service checkCompanySaveOwnership
        // cung duoc giu nguyen ben duoi (full update foreign van 400).
        // - saveWithFile la duong UI that (api/org/company.js
        // saveCompany) nen phai co cung san — khong thi lo van mo qua
        // duong file (K5 chi guard deleteIds trong service, khong
        // phai quyen ghi).
        // - Doc (getCompanys/tree/getSubordinateClinic/get/list/
        // listAll) GIU NGUYEN. Khong tao resource moi.
        // - DS9 (round 9): single delete 500 va bulkDelete NPE truoc day
        // KHONG phai do stored-row check (no bo qua khi company null)
        // ma do TreeEntity.preUpdate deref parent null khi payload chi
        // co id; da sua bang null-guard o TreeEntity (preUpdate +
        // preInsert), nhanh else cu (ids = id + ".") xu ly nut goc.
        // San proxy van chan clinic 403 truoc khi cham service.
        if (!hasSessionPermission("company:create") && !hasSessionPermission("company:update")) {
            return forbidden();
        }
        Company entity = JSONObject.parseObject(strEntity, Company.class);
        String[] deleteIds = JSONObject.parseObject(strDeleteIds, String[].class);
        String id = companyService.save(entity,
            fileIdUploads,
            deleteIds
        ).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @Override
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@javax.validation.Valid @RequestBody Company entity) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        if (!hasSessionPermission("company:create") && !hasSessionPermission("company:update")) {
            return forbidden();
        }
        if (!beforeSave(entity)) {
            return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50002, "LưutrướcKiểm traChưaQua"));
        }
        String id = getService().save(entity).getId();
        afterSave(entity);
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    @Override
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody Company entity) {
        if (entity == null) {
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        if (!hasSessionPermission("company:delete")) {
            return forbidden();
        }
        if (!beforeDelete(entity)) {
            return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50002, "XóatrướcKiểm traChưaQua"));
        }
        int rows = getService().delete(entity);
        afterDelete(entity);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    @Override
    @PostMapping(value = "bulkInsert")
    public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<Company> entitys) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        if (!hasSessionPermission("company:create")) {
            return forbidden();
        }
        List<String> ids = getService().bulkInsert(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }

    @Override
    @PostMapping(value = "bulkUpdate")
    public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<Company> entitys) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        if (!hasSessionPermission("company:update")) {
            return forbidden();
        }
        List<String> ids = getService().bulkUpdate(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }

    @Override
    @PostMapping(value = "bulkDelete")
    public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<Company> entitys) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        if (!hasSessionPermission("company:delete")) {
            return forbidden();
        }
        int rows = getService().bulkDelete(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    /**
     * Lấytenantdướimặttất cả của Phòng khám
     *
     * @return
     */
    @GetMapping(value = "getSubordinateClinic")
    public ResponseEntity<JSONObject> getSubordinateClinic() {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        SessionUserDto userDto = SessionUtils.getUserDto();
        String companyId = userDto.getCompanyId();
        List<Company> result = companyService.getSubordinateClinic(companyId);
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }
}