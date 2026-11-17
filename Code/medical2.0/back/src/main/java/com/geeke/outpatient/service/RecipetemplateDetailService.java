package com.geeke.outpatient.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.org.entity.Company;
import com.geeke.outpatient.dao.RecipeTemplateDetailDao;
import com.geeke.outpatient.dao.RecipetemplateDao;
import com.geeke.outpatient.entity.*;
import com.geeke.stock.dao.DrugDao;
import com.geeke.stock.dao.StuffDao;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.Stuff;
import com.geeke.sys.utils.SessionUtils;
import com.geeke.treatment.dao.CostItemDao;
import com.geeke.treatment.entity.CostItem;
import com.geeke.utils.IdGen;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Đơn thuốc mẫuService
 * @author rys
 * @version 2022-10-08
 */
 
@Service
@Transactional(readOnly = true)
public class RecipetemplateDetailService extends CrudService<RecipeTemplateDetailDao, RecipeTemplateDetail> {
    protected String createBy;
    protected Date createDate;
    protected String updateBy;
    protected Date updateDate;
    protected String delFlag;
    private String code="MB000000001";

    @Autowired
    private RecipeTemplateDetailDao recipeTemplateDetailDao;

    // Đọc trực tiếp qua DAO (không qua Service.get()): Service.get() chạy
    // verifyTenantOwnership theo tenant của phiên và ném ServiceException
    // khi thuốc/vật tư thuộc tenant khác; ngoại lệ đó đánh dấu transaction
    // hiện tại là rollback-only nên dù bắt ở đây cả trang list vẫn lỗi 500
    // "Transaction rolled back because it has been marked as rollback-only".
    // DAO.get() không kiểm tra tenant; việc lọc vẫn do logic so sánh
    // company dưới đây thực hiện (chi tiết không cùng phòng khám bị bỏ qua,
    // không lộ dữ liệu của tenant khác).
    @Autowired
    private DrugDao drugDao;

    @Autowired
    private StuffDao stuffDao;

    @Autowired
    private CostItemDao costItemDao;

    // DS214: chi tiết mẫu đơn thuốc chạy vào số lượng/tiền: total,
    // singleDosage, minTotal, unitPrice, allFee. Null-safe (null giữ
    // nguyên — các cột đều nullable), 0 giữ nguyên; âm ->
    // ServiceException (400). Cùng họ DS209 (RecipelDetail) nhưng
    // là entity khác: allSave/updateDetail ở đây không hề kiểm tra
    // số, nên mẫu với số âm vẫn lưu được và phá việc kê đơn khi áp
    // dụng (RecipelDetail đã chặn ở DS209).
    // DS307: executions (tiến độ thực hiện, int primitive nên không
    // null) âm trước đây cũng lọt qua helper này — DS214 chỉ liệt kê
    // 5 trường trên, DS218 chỉ chặn executions âm ở RecipelDetail
    // (đơn thật), không phải ở chi tiết mẫu. Mẫu có tiến độ âm là dữ
    // liệu vô nghĩa (tiến độ bắt đầu từ 0); frontend không gửi trường
    // này nên guard không phá luồng hợp lệ. 0 giữ nguyên (quy ước
    // DS219), âm -> 400. Gọi ở cả 2 vòng ghi thật allSave /
    // updateDetail qua chính helper này, cạnh DS214/DS301.
    private static void checkDetailNonNegative(RecipeTemplateDetail detail) {
        if (detail == null) {
            return;
        }
        if (detail.getTotal() != null && detail.getTotal() < 0) {
            throw new ServiceException("Tổng số lượng không được âm");
        }
        if (detail.getSingleDosage() != null && detail.getSingleDosage() < 0) {
            throw new ServiceException("Liều dùng mỗi lần không được âm");
        }
        if (detail.getMinTotal() != null && detail.getMinTotal() < 0) {
            throw new ServiceException("Tổng lượng đơn vị nhỏ nhất không được âm");
        }
        if (detail.getUnitPrice() != null
                && detail.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Đơn giá không được âm");
        }
        if (detail.getAllFee() != null
                && detail.getAllFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Tổng tiền không được âm");
        }
        if (detail.getExecutions() < 0) {
            throw new ServiceException("Tiến độ thực hiện không được âm");
        }
    }

    // DS301: tong luong don vi nho nhat (minTotal) khong duoc thap hon
    // tong so luong ke don (total) cua chinh dong mau do. minTotal la
    // tong luong theo don vi bao che / don vi nho nhat, frontend mau
    // tinh minTotal = Math.ceil(tong luong bao che) va total ban theo
    // goi = ceil(minTotal / preparation) <= minTotal, ban le total =
    // minTotal (recipetemplateForm.vue :2853-2872) nen luong that luon
    // thoa; mau xau ap dung sang don that se mang minTotal < total vao
    // RecipelDetail (DS300) va lam kho chiem dung thieu. Ca 2 co gia
    // tri va minTotal < total -> ServiceException 400 fail-closed;
    // == hop le, > hop le, mot trong hai null giu hop le (cot
    // nullable; mau cu co min_total NULL, quy uoc DS219). Goi canh
    // checkDetailNonNegative (DS214) o ca 2 vong ghi that allSave /
    // updateDetail; ca hai join transaction cua
    // RecipetemplateService.allSave nen dong xau bi tu choi truoc moi
    // ghi (updateDetail xoa truoc chen sau nhung rollback khoi phuc
    // chi tiet cu). Khac DS300 (RecipelDetail, don that) va DS297 da
    // loai tru (allFee vs total x unitPrice phu thuoc isUnpackSell).
    private static void checkMinTotalNotLessThanTotal(
            RecipeTemplateDetail detail) {
        if (detail == null) {
            return;
        }
        if (detail.getTotal() != null && detail.getMinTotal() != null
                && detail.getMinTotal() < detail.getTotal()) {
            throw new ServiceException(
                    "Tổng lượng đơn vị nhỏ nhất không được thấp hơn tổng số lượng");
        }
    }


    // DS375: isExtra chi co mien {0,1} (0: thuoc chinh, 1: phi phu them;
    // frontend mac dinh 0 cho dong moi, khong co switch nao ghi gia tri
    // khac). Truoc day khong guard nao nhin isExtra nen isExtra=9/2/-1
    // deu 200 + persist qua allSave (tao) va updateDetail (sua, xoa
    // truoc chen sau) — dong gia roi vao CA HAI danh sach o man hinh
    // kham (filterRecipel giu isExtra !== 1, filterAdditional giu
    // isExtra !== 0) va ne dedup + cap 5 thuoc truyen dich o form mau
    // (chi dem isExtra===0). Khac RecipelDetail.isExtra (DS373, don
    // that) — day la chi tiet MAU. null giu hop le (cot nullable, mau
    // cu co NULL). Goi canh DS214/DS301 o ca 2 vong ghi that allSave /
    // updateDetail.
    private static void checkRecipeTemplateDetailIsExtraValues(
            RecipeTemplateDetail detail) {
        if (detail == null || detail.getIsExtra() == null) {
            return;
        }
        if (detail.getIsExtra() != 0 && detail.getIsExtra() != 1) {
            throw new ServiceException(
                    "Phí phụ thêm của chi tiết mẫu đơn thuốc không hợp lệ (0: Không; 1: Có).");
        }
    }


    // DS376: isUnpackSell chi co mien {0,1} (0: ban theo dong goi,
    // 1: ban le tach le; frontend mac dinh 0 cho dong moi, select don
    // vi chi co 2 option 0/1). Truoc day khong guard nao nhin
    // isUnpackSell nen isUnpackSell=9/2/-1 deu 200 + persist qua
    // allSave (tao) va updateDetail (sua, xoa truoc chen sau) — dong
    // gia hien gia le o form mau (isUnpackSell === 0 ? gia goi : gia
    // le, :1297/:1525) trong khi MedicalCalculate (:2685/:2856/:2919)
    // tinh theo isUnpackSell === 1 ? ban le : dong goi (gia hien thi
    // != allFee/unitPrice da luu); ap mau sang don that
    // (medicalOutpatientRecord/index.vue :4634-4645 push nguyen chi
    // tiet mau vao don that) roi MedicalCalculate, ma
    // RecipelDetail.isUnpackSell co tinh khong guard (DS108: 5 caller
    // noi bo cua RecipelDetailService.save trong vao gia tri frontend
    // gui + cot nullable — ly do do KHONG ap cho chi tiet MAU: duong
    // ghi duy nhat la allSave/updateDetail o day, khong caller noi
    // bo, frontend luon gui 0/1). Khac Drug.isUnpackSell (DS364) va
    // Stuff.isUnpackSell (DS367) — day la chi tiet MAU. null giu hop
    // le nhu DS375 (null hien da 400 san qua rang buoc NOT NULL cua
    // DB, khong dong nao persist). Goi canh DS214/DS301/DS375 o ca 2
    // vong ghi that allSave / updateDetail.
    private static void checkRecipeTemplateDetailIsUnpackSellValues(
            RecipeTemplateDetail detail) {
        if (detail == null || detail.getIsUnpackSell() == null) {
            return;
        }
        if (detail.getIsUnpackSell() != 0
                && detail.getIsUnpackSell() != 1) {
            throw new ServiceException(
                    "Bán lẻ tách lẻ của chi tiết mẫu đơn thuốc không hợp lệ (0: Không; 1: Có).");
        }
    }


    // Ghi: method này INSERT vào recipetemplate_detail nên phải là
    // read-write (lớp đang readOnly=true); khi được gọi từ
    // RecipetemplateService.allSave (đã read-write) nó join transaction đó.
    @Transactional(readOnly = false)
    public void allSave(List<RecipeTemplateDetail> recipeTemplateDetails){
        // DS116 A: a null list NPEd in the for-loop (reached from
        // saveTemplate with a payload without a detail list); an empty
        // list is a valid header-only template, so null is a no-op.
        if (recipeTemplateDetails == null) {
            return;
        }
        for (RecipeTemplateDetail recipeTemplateDetail:
        recipeTemplateDetails) {
            checkDetailNonNegative(recipeTemplateDetail);
            checkMinTotalNotLessThanTotal(recipeTemplateDetail); // DS301
            checkRecipeTemplateDetailIsExtraValues(recipeTemplateDetail); // DS375
            checkRecipeTemplateDetailIsUnpackSellValues(recipeTemplateDetail); // DS376
            recipeTemplateDetail.preInsert();
            recipeTemplateDetail.setId(IdGen.uuid());
            recipeTemplateDetailDao.allSave(recipeTemplateDetail);
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

    public List<RecipeTemplateDetail> getByRecipeTemplateInfoId(String recipeTemplateInfoId) {
        List<RecipeTemplateDetail> recipeTemplateDetails=this.dao.getByRecipeTemplateInfoId(recipeTemplateInfoId);

        //Sau khi lấy được chi tiết đơn thuốc, lấy thuốc và vật tư theo chi tiết đơn thuốc, qualọckhông phảinàyPhòng khámThuốc
        if(!CollectionUtils.isEmpty(recipeTemplateDetails)){
            List<RecipeTemplateDetail> validDetails = new java.util.ArrayList<>();
            for (RecipeTemplateDetail recipeTemplateDetail:
            recipeTemplateDetails) {
                DrugStuffEvt drugStuffEvt = this.getDrugStuffEvt(recipeTemplateDetail);
                if (drugStuffEvt != null) {
                    recipeTemplateDetail.setDrugStuffId(drugStuffEvt);
                    validDetails.add(recipeTemplateDetail);
                }
            }
            recipeTemplateDetails.clear();
            recipeTemplateDetails.addAll(validDetails);
        }
        return recipeTemplateDetails;
    }

    public DrugStuffEvt getDrugStuffEvt(RecipeTemplateDetail recipeTemplateDetail) {
        DrugStuffEvt drugStuffEvt = new DrugStuffEvt();
        String stuffType = recipeTemplateDetail.getStuffType();        // Loại vật liệu:0-Thuốc tây 1-Thuốc đông y  2-Truyền dịch 3-Hạng mục khám chữa bệnh  4-Vật tư
        String detailCompanyId = recipeTemplateDetail.getCompany() != null ? recipeTemplateDetail.getCompany().getId() : null;
        if ("0".equals(stuffType) || "1".equals(stuffType) || "2".equals(stuffType)) {
            //Thuốc đông y, Thuốc tây, Đơn truyền dịch tra cứu thông tin thuốc
            Drug drug = this.drugDao.get(recipeTemplateDetail.getDrugStuffId().getDrugStuffId());
            if (drug == null) return null;
            // Phòng khámcấpcách ly: Bỏ quakhácPhòng khám của Thuốc
            if (detailCompanyId != null && drug.getCompany() != null && !detailCompanyId.equals(drug.getCompany().getId())) {
                logger.warn("mẫu Chi tiết bao gồm không phải này Phòng khám Thuốc, Đã bỏ qua: detailCompanyId={}, drugCompanyId={}, drugName={}",
                    detailCompanyId, drug.getCompany().getId(), drug.getGoodsName());
                return null;
            }
            drugStuffEvt.setDrugStuffId(drug.getId());
            drugStuffEvt.setName(drug.getGoodsName());
            drugStuffEvt.setPrice(drug.getPrice());
            drugStuffEvt.setRetailPrice(drug.getRetailPrice());
            drugStuffEvt.setIsUnpackSell(drug.getIsUnpackSell());
            drugStuffEvt.setDosisUnit(drug.getDosisUnit());
            drugStuffEvt.setPreparationUnit(drug.getPreparationUnit());
            drugStuffEvt.setPack(drug.getPack());
            drugStuffEvt.setDrug(drug);
        } else if ("3".equals(stuffType)) {
            //Truy vấn thông tin hạng mục khám chữa bệnh
            CostItem costItem = this.costItemDao.get(recipeTemplateDetail.getDrugStuffId().getDrugStuffId());
            if (costItem == null) return null;
            if (detailCompanyId != null && costItem.getCompany() != null && !detailCompanyId.equals(costItem.getCompany().getId())) {
                logger.warn("mẫu Chi tiết bao gồm không phải này Phòng khám hạng mục khám chữa bệnh, Đã bỏ qua: detailCompanyId={}, costItemCompanyId={}, itemName={}",
                    detailCompanyId, costItem.getCompany().getId(), costItem.getItemName());
                return null;
            }
            drugStuffEvt.setDrugStuffId(costItem.getId());
            drugStuffEvt.setName(costItem.getItemName());
            drugStuffEvt.setPrice(costItem.getSalePrice());

            drugStuffEvt.setRetailPrice(null);
            drugStuffEvt.setIsUnpackSell("0");
            drugStuffEvt.setDosisUnit(costItem.getUnit());
            drugStuffEvt.setPack(costItem.getUnit());
            drugStuffEvt.setCostItem(costItem);
        } else if ("4".equals(stuffType)) {
            Stuff stuff = this.stuffDao.get(recipeTemplateDetail.getDrugStuffId().getDrugStuffId());
            if (stuff == null) return null;
            if (detailCompanyId != null && stuff.getCompany() != null && !detailCompanyId.equals(stuff.getCompany().getId())) {
                logger.warn("mẫu Chi tiết bao gồm không phải này Phòng khám Vật tư, Đã bỏ qua: detailCompanyId={}, stuffCompanyId={}, stuffName={}",
                    detailCompanyId, stuff.getCompany().getId(), stuff.getName());
                return null;
            }
            drugStuffEvt.setDrugStuffId(stuff.getId());
            drugStuffEvt.setName(stuff.getName());
            drugStuffEvt.setPrice(stuff.getPriceOutSell());
            drugStuffEvt.setRetailPrice(stuff.getRetailPrice());
            drugStuffEvt.setIsUnpackSell(stuff.getIsUnpackSell());
            drugStuffEvt.setDosisUnit(stuff.getMinUnit());
            drugStuffEvt.setPreparationUnit(stuff.getMinUnit());
            drugStuffEvt.setPack(stuff.getPackUnit());
            drugStuffEvt.setStuff(stuff);
        }

        return drugStuffEvt;
    }

    // Sửa mẫu không có chi tiết thuốc nào: bản cũ gọi get(0) vô điều kiện
    // nên allSave luôn lỗi "Index: 0, Size: 0" dù tạo mẫu rỗng vẫn thành công.
    // Bản này nhận thêm info để khi rỗng vẫn xóa được chi tiết cũ (nếu có).
    @Transactional
    public void updateDetail(List<RecipeTemplateDetail> recipeTemplateDetails, Company company, RecipeTemplateInfo info) {
        if (CollectionUtils.isEmpty(recipeTemplateDetails)) {
            if (info != null && StringUtils.isNotBlank(info.getId())) {
                this.dao.deleteTemplateDetail(info.getId());
            }
            return;
        }
        updateDetail(recipeTemplateDetails, company);
    }

    @Transactional
    public void updateDetail(List<RecipeTemplateDetail> recipeTemplateDetails, Company company) {
        RecipeTemplateInfo recipeTemplateInfoId=recipeTemplateDetails.get(0).getRecipeTemplateInfo();

        //Thao tác sửa chi tiết là xóa trước rồi mới thêm
        int i=this.dao.deleteTemplateDetail(recipeTemplateInfoId.getId());

        if(!CollectionUtils.isEmpty(recipeTemplateDetails)&&i>0){
            for (RecipeTemplateDetail recipeTemplateDetail : recipeTemplateDetails) {

                    checkDetailNonNegative(recipeTemplateDetail);
                    checkMinTotalNotLessThanTotal(recipeTemplateDetail); // DS301
                    checkRecipeTemplateDetailIsExtraValues(recipeTemplateDetail); // DS375
                    checkRecipeTemplateDetailIsUnpackSellValues(recipeTemplateDetail); // DS376
                    recipeTemplateDetail.preInsert();
                    recipeTemplateDetail.setId(IdGen.uuid());
                    recipeTemplateDetail.setRecipeTemplateInfo(recipeTemplateInfoId);
                    recipeTemplateDetail.setCompany(company);
                    this.dao.allSave(recipeTemplateDetail);

            }
        }
    }
}