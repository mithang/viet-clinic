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

    // DS417: smallType gia mao ngoai mien {'recipelSmallType_1'..'_6'}
    // qua allSave create/update (probe pre 13/13: '9' /
    // 'recipelSmallType_7' / 'x' / 'recipelSmallType_0' deu 200 +
    // persist; danh sach LEFT JOIN hien ten loai rong, form sua select
    // trong, phan loai thu vien mau gay me/huong than bi pha).
    // Entity khong annotation mien, cot small_type varchar(100)
    // NULL-able (live 3/9 dong NULL) nen null/blank giu hop le
    // (tien le DS364/DS359); chi gia tri ngoai mien moi 400.
    // Khong co controller rieng cho recipetemplateInfo nen guard
    // allSave + updateInfo bao het duong ghi that (allSave va aiSave
    // deu qua saveTemplate/updateTemplate). Khong co caller noi bo
    // nao set smallType (grep setSmallType = 0 caller). Fail-closed
    // 400 truoc moi ghi.
    private static void checkRecipeTemplateInfoSmallTypeValues(
            RecipeTemplateInfo entity) {
        if (entity == null || entity.getSmallType() == null
                || entity.getSmallType().getValue() == null
                || entity.getSmallType().getValue().trim().isEmpty()) {
            return;
        }
        String smallType = entity.getSmallType().getValue();
        if (!"recipelSmallType_1".equals(smallType)
                && !"recipelSmallType_2".equals(smallType)
                && !"recipelSmallType_3".equals(smallType)
                && !"recipelSmallType_4".equals(smallType)
                && !"recipelSmallType_5".equals(smallType)
                && !"recipelSmallType_6".equals(smallType)) {
            throw new com.geeke.common.service.ServiceException(
                    "Loại đơn thuốc mẫu không hợp lệ: " + smallType);
        }
    }

    // DS449: takeFrequency gia mao ngoai mien
    // {'chineseMedicineRecipelTakeFrequency_0.5','_1','_1.5','_2','_3'}
    // qua allSave create/update (probe pre 18/18: 'x' /
    // '..._9' / '..._4' deu 200 + persist ca create lan update;
    // reader GET hien takeFrequency.name rong; ap mau copy nguyen
    // takeFrequency sang don that nen don gia in tan suat uong rong
    // tren phieu don thuoc Dong y va phieu tinh tien).
    // Entity khong annotation mien, cot take_frequency varchar
    // NULL-able (live 6/9 dong NULL) nen null/blank giu hop le
    // (tien le DS417/DS442/DS448); chi gia tri ngoai mien moi 400.
    // Khong co controller rieng cho recipetemplateInfo nen guard
    // allSave + updateInfo bao het duong ghi that (allSave va aiSave
    // deu qua saveTemplate/updateTemplate). Khong co caller noi bo
    // nao set takeFrequency (grep setTakeFrequency = 0 caller).
    // Fail-closed 400 truoc moi ghi.
    private static void checkRecipeTemplateInfoTakeFrequencyValues(
            RecipeTemplateInfo entity) {
        if (entity == null || entity.getTakeFrequency() == null
                || entity.getTakeFrequency().getValue() == null
                || entity.getTakeFrequency().getValue().trim().isEmpty()) {
            return;
        }
        String takeFrequency = entity.getTakeFrequency().getValue();
        if (!"chineseMedicineRecipelTakeFrequency_0.5".equals(takeFrequency)
                && !"chineseMedicineRecipelTakeFrequency_1".equals(takeFrequency)
                && !"chineseMedicineRecipelTakeFrequency_1.5".equals(takeFrequency)
                && !"chineseMedicineRecipelTakeFrequency_2".equals(takeFrequency)
                && !"chineseMedicineRecipelTakeFrequency_3".equals(takeFrequency)) {
            throw new com.geeke.common.service.ServiceException(
                    "Tần suất uống đơn thuốc mẫu không hợp lệ: " + takeFrequency);
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
        checkRecipeTemplateInfoSmallTypeValues(recipeTemplateInfo); // DS417
        checkRecipeTemplateInfoTakeFrequencyValues(recipeTemplateInfo); // DS449
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
        checkRecipeTemplateInfoSmallTypeValues(recipeTemplateInfo); // DS417
        checkRecipeTemplateInfoTakeFrequencyValues(recipeTemplateInfo); // DS449
        this.dao.updateInfo(recipeTemplateInfo);
    }
}