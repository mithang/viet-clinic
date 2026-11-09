package com.geeke.org.controller;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.CrudController;
import com.geeke.common.service.ServiceException;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.medicareutils.config.MedicareConfigProperties;
import com.geeke.medicareutils.service.MdCompanyService;
import com.geeke.org.entity.ClinicOffice;
import com.geeke.org.service.ClinicOfficeService;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Thông tin khoaController
 * @author ch
 * @version 2022-06-14
 */
@RestController
@RequestMapping(value = "/org/clinicOffice")
public class ClinicOfficeController extends CrudController<ClinicOfficeService, ClinicOffice> {

    @Autowired
    protected ClinicOfficeService clinicOfficeService;

    @Autowired
    private MedicareConfigProperties properties;

    @Autowired
    private MdCompanyService mdcompanyService;

    @Override
    protected ClinicOfficeService getService() {
        return clinicOfficeService;
    }

    @Override
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@Valid @RequestBody ClinicOffice entity) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }
        entity.setCompany(SessionUtils.getLoginTenant());
        // DS162: session khong co tenant (login voi companyId mo coi:
        // co dong sys_user_role nhung khong co org_company) thi
        // getCompany() null, dong duoi getId() NPE 500 khi
        // isDefault=1; duong isDefault=0 cung chi 400 nho NOT NULL o
        // insert. Chan som 400 truoc moi ghi, ca hai duong.
        if (entity.getCompany() == null
                || StringUtils.isBlank(entity.getCompany().getId())) {
            throw new ServiceException("Phiên đăng nhập không có phòng khám (tenant), không thể lưu khoa");
        }
        if("1".equals(entity.getIsDefault())){
            clinicOfficeService.updateDefault(entity.getCompany().getId());
        }
        //đồng bộđếnmởBHYTgiao diện
        if (properties.getCheck().equals("true")) {
            //Thêm
            if (StringUtils.isBlank(entity.getId())) {
              mdcompanyService.upCompanyData_3401_3402(entity,"3401");
            } else {
                //Sửa
                mdcompanyService.upCompanyData_3401_3402(entity,"3402");
            }
        }
        String id = clinicOfficeService.save(entity).getId();
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

}
