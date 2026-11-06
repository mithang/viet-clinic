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
import com.geeke.stock.dao.DrugDao;
import com.geeke.stock.dao.StuffDao;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.Stuff;
import com.geeke.stock.service.DrugService;
import com.geeke.treatment.dao.CostItemDao;
import com.geeke.treatment.entity.CostItem;
import com.geeke.sys.utils.SessionUtils;
import com.geeke.utils.IdGen;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.common.service.ServiceException;
import com.geeke.outpatient.dao.RecipeTemplateInfoDao;
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
    // Đọc trực tiếp qua DAO trong isDetailBelongsToCompany (xem chú thích
    // ở đó): Service.get() ném ServiceException khi thuốc/vật tư thuộc
    // tenant khác và đánh dấu transaction lưu mẫu là rollback-only, làm
    // cả lần lưu thất bại dù ý đồ chỉ là lọc bỏ chi tiết đó.
    @Autowired
    private DrugDao drugDao;
    @Autowired
    private StuffDao stuffDao;
    @Autowired
    private CostItemDao costItemDao;
    // Q2 (iteration 40): đọc trực tiếp ownership của info qua DAO (cùng lý
    // do như trên): updateTemplate cần dòng info đã lưu để đối chiếu
    // tenant/template trước khi updateInfo/deleteTemplateDetail ghi theo
    // id trần, mà không dùng Service.get() ném lỗi giữa transaction.
    @Autowired
    private RecipeTemplateInfoDao recipeTemplateInfoDao;

    // Ghi: entry-point phải là read-write. Lớp này là readOnly=true và
    // saveTemplate()/updateTemplate() được gọi nội bộ (this.*) nên
    // @Transactional trên chính chúng bị proxy bỏ qua; chỉ annotation
    // ở đây mới tạo transaction ghi cho toàn bộ chuỗi allSave.
    @Transactional(readOnly = false)
    public void allSave(Recipetemplate recipetemplate){
        if(StringUtils.isBlank(recipetemplate.getId())){
            this.saveTemplate(recipetemplate);
        }else {
            this.updateTemplate(recipetemplate);
        }
    }
    @Transactional(readOnly = false)
    private void updateTemplate(Recipetemplate recipetemplate) {
        // Q2 (iteration 40): đường ghi này gọi dao.updateById trực tiếp
        // nên né guard generic (M1/M2) — kiểm tra TRƯỚC mọi ghi:
        // (1) template đã lưu phải thuộc session tenant (dòng lạ → 400,
        // dòng không tồn tại / session rỗng giữ hành vi cũ: updateById
        // trả 0, cascade bỏ qua);
        verifyTenantOwnershipAgainstStoredRow(recipetemplate);
        // (2) company trong payload phải là session tenant (khi session
        // hợp lệ) — nếu không mẫu own sẽ bị chuyển company sang lạ.
        blockForeignPayloadCompany(recipetemplate.getCompany());
        RecipeTemplateInfo info = recipetemplate.getRecipeTemplateInfo();
        if (info != null && StringUtils.isNotBlank(info.getId())) {
            blockForeignPayloadCompany(info.getCompany());
            // (3) info đã lưu phải thuộc session tenant VÀ thuộc đúng
            // template này — nếu không updateInfo sẽ ghi đè info lạ.
            RecipeTemplateInfo storedInfo = recipeTemplateInfoDao.getOwnership(info.getId());
            if (storedInfo != null) {
                checkStoredInfoTenant(storedInfo);
                if (storedInfo.getRecipetemplate() != null
                        && StringUtils.isNotBlank(storedInfo.getRecipetemplate().getId())
                        && !storedInfo.getRecipetemplate().getId().equals(recipetemplate.getId())) {
                    throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
                }
            }
        }
        // (4) detail-info của chi tiết phải khớp info của template (khi
        // info đã biết) — nếu không deleteTemplateDetail sẽ xóa cứng
        // chi tiết của info lạ; khi info chưa biết thì chỉ kiểm tra
        // tenant của detail-info đó.
        List<RecipeTemplateDetail> details = recipetemplate.getRecipeTemplateDetail();
        if (details != null && !details.isEmpty()) {
            RecipeTemplateDetail first = details.get(0);
            if (first != null && first.getRecipeTemplateInfo() != null
                    && StringUtils.isNotBlank(first.getRecipeTemplateInfo().getId())) {
                String detailInfoId = first.getRecipeTemplateInfo().getId();
                if (info != null && StringUtils.isNotBlank(info.getId())) {
                    if (!detailInfoId.equals(info.getId())) {
                        throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
                    }
                } else {
                    RecipeTemplateInfo storedDetailInfo = recipeTemplateInfoDao.getOwnership(detailInfoId);
                    if (storedDetailInfo != null) {
                        checkStoredInfoTenant(storedDetailInfo);
                    }
                }
            }
        }
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
            recipetemplateDetailService.updateDetail(recipetemplate.getRecipeTemplateDetail(),recipetemplate.getCompany(),recipetemplate.getRecipeTemplateInfo());
        }
    }

    @Transactional(readOnly = false)
    public void saveTemplate(Recipetemplate recipetemplate){
        // Q2 (iteration 40): create ghi thẳng company từ payload (template
        // + info + chi tiết) — chặn company lạ trước khi sinh uuid/code.
        blockForeignPayloadCompany(recipetemplate.getCompany());
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

    // Q2 (iteration 40): company trong payload phải là session tenant khi
    // session hợp lệ; company lạ (hoặc thiếu session thì giữ hành vi cũ).
    private void blockForeignPayloadCompany(Company company) {
        if (company == null || StringUtils.isBlank(company.getId())) {
            return;
        }
        String tenantId = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        if (!tenantId.equals(company.getId())) {
            throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
        }
    }

    // Q2 (iteration 40): dòng info đã lưu phải thuộc session tenant khi
    // session hợp lệ; dòng không tồn tại / company rỗng giữ hành vi cũ.
    private void checkStoredInfoTenant(RecipeTemplateInfo storedInfo) {
        if (storedInfo == null || storedInfo.getCompany() == null
                || StringUtils.isBlank(storedInfo.getCompany().getId())) {
            return;
        }
        String tenantId = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        if (!tenantId.equals(storedInfo.getCompany().getId())) {
            throw new ServiceException("Không có quyền truy cập tenant khácDữ liệu");
        }
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
        // DS87: getById filters by session company: a missing id or a
        // foreign-tenant template returns null -> NPE at getId() below
        // (live: foreign template id and fake id both 500) -> 400.
        if (recipetemplate == null) {
            throw new ServiceException("Không tìm thấy mẫu đơn thuốc");
        }

        //Lấy thông tin đơn thuốc
        RecipeTemplateInfo recipeTemplateInfo=recipetemplateInfoService.getByRecipeTemplateId(recipetemplate.getId());
        // DS87: a template without an info row NPEd at getId() below.
        if (recipeTemplateInfo == null) {
            throw new ServiceException("Mẫu đơn thuốc thiếu thông tin đơn thuốc");
        }

        //Lấy chi tiết đơn thuốc
        List<RecipeTemplateDetail> recipeTemplateDetails=recipetemplateDetailService.getByRecipeTemplateInfoId(recipeTemplateInfo.getId());

        recipetemplate.setRecipeTemplateInfo(recipeTemplateInfo);
        recipetemplate.setRecipeTemplateDetail(recipeTemplateDetails);
        return recipetemplate;
    }
    // Cùng lý do như allSave: aiSave cũng tự gọi saveTemplate/updateTemplate
    // và ghi DB, nên entry-point này phải là read-write.
    @Transactional(readOnly = false)
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
        // ưu tiêntrướcKiểm traChi tiếtbản thân của company_id
        if (detail.getCompany() != null && companyId.equals(detail.getCompany().getId())) {
            return true;
        }
        // Kiểm traliên kết của Thuốc/Vật tư/hạng mục khám chữa bệnhCó hay khôngthuộctạiHiện tạiPhòng khám
        // Đọc qua DAO (không qua Service.get()): xem chú thích ở trường
        // drugDao/stuffDao/costItemDao — Service.get() sẽ ném ngoại lệ tenant
        // và đánh dấu rollback-only cho transaction đang lưu mẫu.
        if (detail.getDrugStuffId() != null && detail.getDrugStuffId().getDrugStuffId() != null) {
            String stuffType = detail.getStuffType();
            String drugStuffId = detail.getDrugStuffId().getDrugStuffId();
            if ("0".equals(stuffType) || "1".equals(stuffType) || "2".equals(stuffType)) {
                Drug drug = this.drugDao.get(drugStuffId);
                return drug != null && drug.getCompany() != null && companyId.equals(drug.getCompany().getId());
            } else if ("3".equals(stuffType)) {
                CostItem costItem = this.costItemDao.get(drugStuffId);
                return costItem != null && costItem.getCompany() != null && companyId.equals(costItem.getCompany().getId());
            } else if ("4".equals(stuffType)) {
                Stuff stuff = this.stuffDao.get(drugStuffId);
                return stuff != null && stuff.getCompany() != null && companyId.equals(stuff.getCompany().getId());
            }
        }
        return false;
    }
}