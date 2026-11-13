package com.geeke.outpatient.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.service.CrudService;
import com.geeke.outpatient.dao.RecipeTemplateInfoDao;
import com.geeke.outpatient.dao.RecipetemplateDao;
import com.geeke.outpatient.entity.RecipeTemplateDetail;
import com.geeke.outpatient.entity.RecipeTemplateInfo;
import com.geeke.outpatient.entity.Recipetemplate;
import com.geeke.sys.utils.SessionUtils;
import com.geeke.utils.IdGen;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Đơn thuốc mẫuService
 * @author rys
 * @version 2022-10-08
 */
 
@Service
@Transactional(readOnly = true)
public class RecipetemplateInfoService extends CrudService<RecipeTemplateInfoDao, RecipeTemplateInfo>{
    protected String createBy;
    protected Date createDate;
    protected String updateBy;
    protected Date updateDate;
    protected String delFlag;

    @Autowired
    private RecipeTemplateInfoDao recipeTemplateInfoDao;


    // DS241: fee am truoc day ghi thang (probe pre 14/14: allSave create
    // fee=-5/-10, aiSave create fee=-7, allSave update fee 50->-3 deu
    // 200 + persist; control 50/0/NULL van 200). Entity khong @Min,
    // khong CHECK o DB; ca hai duong ghi allSave (mapper allSave
    // :170-210) va updateInfo (:149-169) deu ghi fee khong dieu kien.
    // Song sinh DS240 (RecipelInfo.fee da chan); mau co phi am ap dung
    // duoc qua aiSave/listAi sang don thuc roi chay len phieu in /
    // amountReceivable nhu DS240. Khong co controller rieng cho
    // recipetemplateInfo nen CrudService save/bulk khong toi duoc qua
    // API — guard allSave + updateInfo bao het duong ghi that (allSave
    // va aiSave deu qua saveTemplate/updateTemplate). Chan fail-closed
    // 400 truoc moi ghi; null giu NULL (1 dong live fee NULL), 0 giu
    // nguyen (quy uoc DS219). Caller noi bo chi luu lai entity da load
    // (live 0 dong am) nen khong vo luong dang chay.
    private static void checkNonNegative(RecipeTemplateInfo entity) {
        if (entity == null) {
            return;
        }
        if (entity.getFee() != null
                && entity.getFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new com.geeke.common.service.ServiceException("Tổng giá đơn thuốc mẫu không được âm");
        }
    }

    @Transactional(readOnly = false)
    public void allSave(RecipeTemplateInfo recipeTemplateInfo){
        // DS116 A: null info NPEd at preInsert() (called from
        // saveTemplate via RecipetemplateService); fail-closed 400.
        if (recipeTemplateInfo == null) {
            throw new com.geeke.common.service.ServiceException("Thông tin mẫu đơn thuốc không được để trống");
        }
        checkNonNegative(recipeTemplateInfo);
        recipeTemplateInfo.preInsert();
        recipeTemplateInfo.setId(IdGen.uuid());
        recipeTemplateInfoDao.allSave(recipeTemplateInfo);
    }

    public void preInsert() {
        JSONObject userObj = SessionUtils.getUserJson();
        if (userObj != null && StringUtils.isNotBlank(userObj.getString("id"))) {
            this.updateBy = userObj.getString("name") ;
            this.createBy = userObj.getString("name") ;
        }
        this.updateDate = new Date();
        this.createDate = this.updateDate;
    }

    public RecipeTemplateInfo getByRecipeTemplateId(String recipeTemplateId) {
        RecipeTemplateInfo recipeTemplateInfo=this.dao.getByRecipeTemplateId(recipeTemplateId);
        return recipeTemplateInfo;
    }

    @Transactional(readOnly = false)
    public void updateInfo(RecipeTemplateInfo recipeTemplateInfo) {
        // DS116 A: null info NPEd in the mapper (#{company.id} on null);
        // the caller (updateTemplate) now skips a null info, this guard
        // is defence in depth for any other caller.
        if (recipeTemplateInfo == null) {
            throw new com.geeke.common.service.ServiceException("Thông tin mẫu đơn thuốc không được để trống");
        }
        checkNonNegative(recipeTemplateInfo);
        this.dao.updateInfo(recipeTemplateInfo);
    }
}