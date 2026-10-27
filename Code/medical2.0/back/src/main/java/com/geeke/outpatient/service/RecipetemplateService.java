package com.geeke.outpatient.service;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.org.entity.Company;
import com.geeke.outpatient.entity.RecipeTemplateDetail;
import com.geeke.outpatient.entity.RecipeTemplateInfo;
import com.geeke.outpatient.entity.Registration;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.Stuff;
import com.geeke.stock.service.DrugService;
import com.geeke.stock.service.StuffService;
import com.geeke.treatment.entity.CostItem;
import com.geeke.treatment.service.CostItemService;
import com.geeke.sys.utils.SessionUtils;
import com.geeke.utils.IdGen;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.outpatient.dao.RecipetemplateDao;
import com.geeke.outpatient.entity.Recipetemplate;
import com.geeke.utils.StringUtils;

/**
 * Đơn thuốc mẫuService
 * @author rys
 * @version 2022-10-08
 */
 
@Service("recipetemplateService")
@Transactional(readOnly = true)
public class RecipetemplateService extends CrudService<RecipetemplateDao, Recipetemplate>{
    protected String createBy;
    protected Date createDate;
    protected String updateBy;
    protected Date updateDate;
    protected String delFlag;
    private String code="MB000000001";

    @Autowired
    private RecipetemplateInfoService recipetemplateInfoService;
    @Autowired
    private RecipetemplateDetailService recipetemplateDetailService;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private DrugService drugService;
    @Autowired
    private StuffService stuffService;
    @Autowired
    private CostItemService costItemService;

    public void allSave(Recipetemplate recipetemplate){
        if(StringUtils.isBlank(recipetemplate.getId())){
            this.saveTemplate(recipetemplate);
        }else {
            this.updateTemplate(recipetemplate);
        }
    }
    @Transactional(readOnly = false)
    private void updateTemplate(Recipetemplate recipetemplate) {
        JSONObject userObj = SessionUtils.getUserJson();
        if (userObj != null && StringUtils.isNotBlank(userObj.getString("id"))) {
            recipetemplate.setUpdateBy(userObj.getString("name"));
        }
        recipetemplate.setUpdateDate(new Date());
        int i = this.dao.updateById(recipetemplate);
        if(i>0){
            //mẫuĐơn thuốcThông tinSửa
            recipetemplateInfoService.updateInfo(recipetemplate.getRecipeTemplateInfo());
            //mẫuĐơn thuốcChi tiếtSửa
            recipetemplateDetailService.updateDetail(recipetemplate.getRecipeTemplateDetail(),recipetemplate.getCompany());
        }
    }

    @Transactional(readOnly = false)
    public void saveTemplate(Recipetemplate recipetemplate){
        //LưumẫuchínhDữ liệu
        recipetemplate.preInsert();
        recipetemplate.setId(IdGen.uuid());
        //tự độngTạoMã mẫu
        String code = this.dao.listOne(recipetemplate.getCompany().getId());
        if(code!=null){
            String substring = code.substring(2);
            int i = Integer.parseInt(substring);
            String newCode=(i+1)+"";
            int capacity=newCode.length();
            for (int j = 0; j < 9-capacity; j++) {
                newCode="0"+newCode;
            }
            recipetemplate.setCode("MB"+newCode);
        }else {
            recipetemplate.setCode(this.code);
        }
        this.dao.allSave(recipetemplate);

        //Lưumẫu của Đơn thuốcThông tin
        RecipeTemplateInfo recipeTemplateInfo=null;
        if(recipetemplate.getRecipeTemplateInfo()!=null){
             recipeTemplateInfo = recipetemplate.getRecipeTemplateInfo();
            recipeTemplateInfo.setRecipetemplate(recipetemplate);
            recipeTemplateInfo.setCompany(recipetemplate.getCompany());
            recipetemplateInfoService.allSave(recipeTemplateInfo);
        }
        //Lưumẫu của Đơn thuốcChi tiết
        List<RecipeTemplateDetail> recipeTemplateDetails = recipetemplate.getRecipeTemplateDetail();
        Company templateCompany = recipetemplate.getCompany();
        // Phòng khámcấpcách ly: qualọcrơikhông phảinàyPhòng khám của Thuốc/Vật tư/Hạng mục khám chữa bệnh
        if (templateCompany != null && recipeTemplateDetails != null) {
            List<RecipeTemplateDetail> validDetails = new java.util.ArrayList<>();
            for (RecipeTemplateDetail detail : recipeTemplateDetails) {
                if (isDetailBelongsToCompany(detail, templateCompany.getId())) {
                    validDetails.add(detail);
                } else {
                    logger.warn("Khi lưu mẫu, bỏ qua thuốc không thuộc phòng khám này: companyId={}, drugStuffId={}, stuffType={}",
                        templateCompany.getId(), detail.getDrugStuffId().getDrugStuffId(), detail.getStuffType());
                }
            }
            recipeTemplateDetails.clear();
            recipeTemplateDetails.addAll(validDetails);
        }
        for (RecipeTemplateDetail recipeTemplateDetail:
        recipeTemplateDetails) {
            recipeTemplateDetail.setRecipeTemplateInfo(recipeTemplateInfo);
            recipeTemplateDetail.setCompany(templateCompany);
        }
        recipetemplateDetailService.allSave(recipeTemplateDetails);
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

    //TheoidLấymẫuĐơn thuốc
    public Recipetemplate getById(String id){
        //LấyBảng chínhThông tin
        Recipetemplate recipetemplate=this.dao.getById(id,SessionUtils.getUserDto().getCompanyId());

        //Lấy thông tin đơn thuốc
        RecipeTemplateInfo recipeTemplateInfo=recipetemplateInfoService.getByRecipeTemplateId(recipetemplate.getId());

        //Lấy chi tiết đơn thuốc
        List<RecipeTemplateDetail> recipeTemplateDetails=recipetemplateDetailService.getByRecipeTemplateInfoId(recipeTemplateInfo.getId());

        recipetemplate.setRecipeTemplateInfo(recipeTemplateInfo);
        recipetemplate.setRecipeTemplateDetail(recipeTemplateDetails);
        return recipetemplate;
    }
    public String aiSave(Recipetemplate recipetemplate){
        Registration medicalRecord = registrationService.get(recipetemplate.getRegisterId());
        recipetemplate.setPatientId(medicalRecord.getPatientId().getId());
        recipetemplate.setCompany(medicalRecord.getCompany());
        String a = "";
        List<RecipeTemplateDetail> validDetails = new java.util.ArrayList<>();
        for (RecipeTemplateDetail r :recipetemplate.getRecipeTemplateDetail()){
            if (!isDetailBelongsToCompany(r, medicalRecord.getCompany().getId())) {
                logger.warn("AIKhi lưu mẫu, bỏ qua thuốc không thuộc phòng khám này: companyId={}, drugStuffId={}, stuffType={}",
                    medicalRecord.getCompany().getId(), r.getDrugStuffId().getDrugStuffId(), r.getStuffType());
                continue;
            }
            String id = drugService.getId(r.getDrugStuffId().getName(),r.getCompany().getId());
            if (id != null){
                r.getDrugStuffId().setDrugStuffId(id);
                validDetails.add(r);
            }else {
                a += r.getDrugStuffId().getName()+ "ThuốckhôngTồn khoThông tin\n";
            }
        }
        recipetemplate.setRecipeTemplateDetail(validDetails);
        if (!"".equals(a)) {
            return a;
        }
        if(StringUtils.isBlank(recipetemplate.getId())){
            this.saveTemplate(recipetemplate);
        }else {
            this.updateTemplate(recipetemplate);
        }
        a = "Đơn thuốcThông tinThêmThành công";
        return a;
    }

    public List<Recipetemplate> listAi(String id) {
        return dao.listAi(id);
    }

    /**
     * Kiểm tramẫuChi tiếtCó hay khôngthuộctạichỉchỉ địnhPhòng khám
     * @param detail mẫuChi tiết
     * @param companyId Phòng khámID
     * @return true-thuộctạiHiện tạiPhòng khám, false-khôngthuộctại
     */
    private boolean isDetailBelongsToCompany(RecipeTemplateDetail detail, String companyId) {
        if (detail == null || companyId == null) {
            return false;
        }
        // 优trướcKiểm traChi tiếtbản thân của company_id
        if (detail.getCompany() != null && companyId.equals(detail.getCompany().getId())) {
            return true;
        }
        // Kiểm traliên kết của Thuốc/Vật tư/hạng mục khám chữa bệnhCó hay khôngthuộctạiHiện tạiPhòng khám
        if (detail.getDrugStuffId() != null && detail.getDrugStuffId().getDrugStuffId() != null) {
            String stuffType = detail.getStuffType();
            String drugStuffId = detail.getDrugStuffId().getDrugStuffId();
            if ("0".equals(stuffType) || "1".equals(stuffType) || "2".equals(stuffType)) {
                Drug drug = this.drugService.get(drugStuffId);
                return drug != null && drug.getCompany() != null && companyId.equals(drug.getCompany().getId());
            } else if ("3".equals(stuffType)) {
                CostItem costItem = this.costItemService.get(drugStuffId);
                return costItem != null && costItem.getCompany() != null && companyId.equals(costItem.getCompany().getId());
            } else if ("4".equals(stuffType)) {
                Stuff stuff = this.stuffService.get(drugStuffId);
                return stuff != null && stuff.getCompany() != null && companyId.equals(stuff.getCompany().getId());
            }
        }
        return false;
    }
}