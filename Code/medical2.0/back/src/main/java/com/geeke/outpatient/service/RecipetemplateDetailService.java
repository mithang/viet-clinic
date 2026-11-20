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
import com.geeke.sys.entity.DictItem;
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


    // DS398: infuseGroup chi co mien null-hoac->=1 (null: dong khong
    // truyen dich; >=1: so thu tu nhom truyen dich; frontend chi set
    // i+1 / index+1 o recipetemplateForm.vue:2156/:2252). Truoc day
    // khong guard nao nhin infuseGroup nen infuseGroup=0/-5 deu 200 +
    // persist qua allSave (tao) va updateDetail (sua, xoa truoc chen
    // sau) — mo lai mau thi recipetemplateForm.vue:3188-3214 gom nhom
    // bang infusionProject[infuseGroup-1].push(...) nen 0 -> index -1
    // -> undefined.push TypeError, vo form mau; ap mau sang don that
    // (medicalOutpatientRecord/index.vue:4614-4632) cung index -1 vo
    // tab truyen dich, va chi tiet mau push nguyen object vao don that
    // nen gia tri gia chay sang RecipelDetail.infuseGroup (DS397,
    // don that — entity/bang/endpoint khac). Khac DS391 BLOCKED
    // (RecipeTemplateDetail.infuseType — field khac cung entity) va
    // DS375/DS376 (cung entity, field khac). null giu hop le (cot
    // nullable, live 20/20 dong deu NULL). Goi canh DS375/DS376 o ca
    // 2 vong ghi that allSave / updateDetail.
    private static void checkRecipeTemplateDetailInfuseGroupValues(
            RecipeTemplateDetail detail) {
        if (detail == null || detail.getInfuseGroup() == null) {
            return;
        }
        if (detail.getInfuseGroup() < 1) {
            throw new ServiceException(
                    "Nhóm truyền dịch của chi tiết mẫu đơn thuốc không hợp lệ (để trống nếu không truyền dịch, hoặc số nhóm từ 1 trở lên).");
        }
    }


    // DS442: days chi co mien {recipelDetailDays_0..recipelDetailDays_6}
    // (_0 '1' .. _6 '7'; dict 'recipelDetailDays' id
    // 1014474470772900052, dung 7 item del_flag='0'; frontend chi set
    // tu DayNumOption dict kin nay o dictCache.js:32). Truoc day khong
    // guard nao nhin days nen '_9'/'_7'/'x' deu 200 + persist qua
    // allSave (tao) va updateDetail (sua, xoa truoc chen sau) — man
    // kham hien `{{ scope.row.days.name }}ngày`
    // (medicalOutpatientRecord/index.vue:2324) thanh 'ngày' khong so;
    // ap mau sang don that (:4614-4632 push nguyen chi tiet mau vao
    // don that) nen gia tri gia chay sang RecipelDetail.days (don
    // that — entity/bang/endpoint khac, DS373 giu lai); mo lai mau thi
    // select so ngay trong. Khac DS434/DS435 (cung entity, field
    // infuseUse/frequency — van BLOCKED chua guard) va DS375/DS376/
    // DS398 (cung entity, field khac). null/blank giu hop le (cot
    // varchar NULL-able, live 16/20 dong NULL). Goi canh DS375/DS376/
    // DS398 o ca 2 vong ghi that allSave / updateDetail.
    private static void checkRecipeTemplateDetailDaysValues(
            RecipeTemplateDetail detail) {
        if (detail == null) {
            return;
        }
        DictItem days = detail.getDays();
        if (days == null || StringUtils.isBlank(days.getValue())) {
            return;
        }
        String value = days.getValue();
        if (!"recipelDetailDays_0".equals(value)
                && !"recipelDetailDays_1".equals(value)
                && !"recipelDetailDays_2".equals(value)
                && !"recipelDetailDays_3".equals(value)
                && !"recipelDetailDays_4".equals(value)
                && !"recipelDetailDays_5".equals(value)
                && !"recipelDetailDays_6".equals(value)) {
            throw new ServiceException(
                    "Số ngày của chi tiết mẫu đơn thuốc không hợp lệ (chỉ nhận recipelDetailDays_0 đến recipelDetailDays_6).");
        }
    }


    // DS460: infuseUse chi co mien {infuseUse_0..infuseUse_3}
    // (_0 'Tiêm tĩnh mạch', _1 'Truyền tĩnh mạch', _2 'Tiêm bắp',
    // _3 'Khí dung'; dict 'infuseUse', dung 4 item del_flag='0';
    // frontend chi set tu infuseUseOption dict kin nay o
    // recipetemplateForm.vue:881-888). Truoc day khong guard nao nhin
    // infuseUse nen '_9'/'_4'/'x' deu 200 + persist qua allSave (tao)
    // va updateDetail (sua, xoa truoc chen sau) — man duyet don hien
    // `{{ scope.row.infuseUse.name }}` (reviewForm.vue:135) thanh ten
    // rong; ap mau sang don that thi gia tri gia bi guard DS433 chan
    // 400 o buoc luu nen mau bi doc/khong dung duoc. Khac DS433
    // (RecipelDetail.infuseUse — don that, entity/bang/endpoint
    // khac) va DS375/DS376/DS398/DS442 (cung entity, field khac).
    // null/blank giu hop le (cot varchar(64) NULL-able, live 20/20
    // dong deu NULL). Goi canh DS375/DS376/DS398/DS442 o ca 2 vong
    // ghi that allSave / updateDetail.
    private static void checkRecipeTemplateDetailInfuseUseValues(
            RecipeTemplateDetail detail) {
        if (detail == null) {
            return;
        }
        DictItem infuseUse = detail.getInfuseUse();
        if (infuseUse == null || StringUtils.isBlank(infuseUse.getValue())) {
            return;
        }
        String value = infuseUse.getValue();
        if (!"infuseUse_0".equals(value)
                && !"infuseUse_1".equals(value)
                && !"infuseUse_2".equals(value)
                && !"infuseUse_3".equals(value)) {
            throw new ServiceException(
                    "Cách dùng truyền dịch của chi tiết mẫu đơn thuốc không hợp lệ (chỉ nhận infuseUse_0 đến infuseUse_3).");
        }
    }


    // DS469: frequency chi co mien hop 9 gia tri cua 2 dict
    // 'recipelDetailFrequency' (_0.._3) + 'chineseMedicineRecipelTakeFrequency'
    // (_0.5/_1/_1.5/_2/_3), dung 9 item del_flag='0' — cung mien DS419
    // da guard cho RecipelDetail.frequency (don that). Form mau chi
    // hien 5 gia tri chineseMedicine (recipetemplateForm.vue:1886)
    // nhung chi tiet mau duoc push nguyen vao don that khi ap mau
    // (medicalOutpatientRecord/index.vue:4614-4632) noi DS419 chap
    // nhan ca 9 gia tri, nen guard mau phai chap nhan ca 9 gia tri
    // neu khong mau mang recipelDetailFrequency se khong bao gio
    // ap duoc. Truoc day khong guard nao nhin frequency nen
    // '_9'/'_4'/'x' deu 200 + persist qua allSave (tao) va
    // updateDetail (sua, xoa truoc chen sau) — man duyet don hien
    // `{{ scope.row.frequency.name }}` (reviewForm.vue:136) thanh
    // ten rong; MedicalCalculate dung getFrequencyCount(
    // row.frequency.value) (recipetemplateForm.vue:2495-2498) nen 'x'
    // parse NaN -> count 1, sai tong; ap mau sang don that thi gia
    // tri gia bi guard DS419 chan 400 o buoc luu nen mau bi doc/
    // khong dung duoc; mo lai mau thi select tan suat trong. Khac
    // DS419 (RecipelDetail.frequency — don that, entity/bang/endpoint
    // khac) va DS375/DS376/DS398/DS442/DS460 (cung entity, field
    // khac). null/blank giu hop le (cot varchar(64) NULL-able, live
    // 12/20 dong NULL). Goi canh DS375/DS376/DS398/DS442/DS460 o ca
    // 2 vong ghi that allSave / updateDetail.
    private static void checkRecipeTemplateDetailFrequencyValues(
            RecipeTemplateDetail detail) {
        if (detail == null) {
            return;
        }
        DictItem frequency = detail.getFrequency();
        if (frequency == null || StringUtils.isBlank(frequency.getValue())) {
            return;
        }
        String value = frequency.getValue();
        if (!"recipelDetailFrequency_0".equals(value)
                && !"recipelDetailFrequency_1".equals(value)
                && !"recipelDetailFrequency_2".equals(value)
                && !"recipelDetailFrequency_3".equals(value)
                && !"chineseMedicineRecipelTakeFrequency_0.5".equals(value)
                && !"chineseMedicineRecipelTakeFrequency_1".equals(value)
                && !"chineseMedicineRecipelTakeFrequency_1.5".equals(value)
                && !"chineseMedicineRecipelTakeFrequency_2".equals(value)
                && !"chineseMedicineRecipelTakeFrequency_3".equals(value)) {
            throw new ServiceException(
                    "Tần suất của chi tiết mẫu đơn thuốc không hợp lệ (chỉ nhận recipelDetailFrequency_0 đến recipelDetailFrequency_3 hoặc chineseMedicineRecipelTakeFrequency_0.5, _1, _1.5, _2, _3).");
        }
    }


    // DS471: infuseType chi co mien hop {0,1} — 0 'Cho truyen'
    // (dang cho), 1 'Da hoan thanh'. Entity RecipeTemplateDetail.java:36
    // `private int infuseType` (primitive nen omit -> 0, hop le, van
    // qua). Form mau khong bao gio ghi infuseType
    // (recipetemplateForm.vue khong co infuseType) nen gia tri ngoai
    // {0,1} chi den tu gia mao. Truoc day khong guard nao nhin
    // infuseType nen 9/2/-1 deu 200 + persist qua allSave (tao) va
    // updateDetail (sua, xoa truoc chen sau) — man truyen dich chi re
    // ===0/===1 (cure/infusion/index.vue:673-674, :1458) nen dong gia
    // khong hien nut trang thai nao; ap mau sang don that thi dong
    // gia bi guard DS390 chan 400 o buoc luu nen mau bi doc/khong
    // dung duoc; logic hoan thanh truyen (:1458) coi moi gia tri !=1
    // la chua xong, sai tien do truyen. Khac DS390
    // (RecipelDetail.infuseType — don that, entity/bang/endpoint
    // khac) va DS375/DS376/DS398/DS442/DS460/DS469 (cung entity,
    // field khac). Goi canh DS375/DS376/DS398/DS442/DS460/DS469 o
    // ca 2 vong ghi that allSave / updateDetail.
    private static void checkRecipeTemplateDetailInfuseTypeValues(
            RecipeTemplateDetail detail) {
        if (detail == null) {
            return;
        }
        int infuseType = detail.getInfuseType();
        if (infuseType != 0 && infuseType != 1) {
            throw new ServiceException(
                    "Trạng thái truyền dịch của chi tiết mẫu đơn thuốc không hợp lệ (chỉ nhận 0 — Chờ truyền, hoặc 1 — Đã hoàn thành).");
        }
    }


    // DS474: stuffType chi co mien hop {'0','1','2','3','4'} — 0 'Thuoc
    // tay', 1 'Thuoc dong y', 2 'Truyen dich', 3 'Hang muc kham chua
    // benh', 4 'Vat tu' (service comment getDrugStuffEvt :433). Entity
    // RecipeTemplateDetail.java:33 `private String stuffType` (plain
    // String, cot varchar(100) NULL-able nen null/blank van qua, tien
    // le DS469/DS471). Khong co dict nao quan field nay (dict
    // 'stuffType' chi co stuffType_0/_1 — do la dict cua Stuff.type,
    // field khac). Truoc day khong guard nao nhin stuffType nen
    // '5'/'9'/'x' deu 200 + persist qua allSave (tao) va updateDetail
    // (sua, xoa truoc chen sau) — reader getDrugStuffEvt :433-490 roi
    // qua het cac nhanh -> tra DrugStuffEvt rong (name/price null) nen
    // mo lai mau thay dong khong ten/khong gia; form chi hien thi ten
    // cho '3'/'4' nen dong gia trong ten, sai tong tien; ap mau day
    // detail nguyen van vao don that (MedicalRecordService.allSave ->
    // recipelDetailService.save) — RecipelDetail.stuffType chua co
    // guard, gia tri gia chay vao don that noi cac reader chi re 0-4;
    // RecipelStastics dung bang stuffType tho (MedicalRecordService
    // .java:620-624) -> o nhiem thong ke thuoc/vat tu. Khac DS473
    // (RecipelDetail.retailType — don that, entity/bang/endpoint
    // khac) va DS375/DS376/DS398/DS442/DS460/DS469/DS471 (cung entity,
    // field khac). Goi canh DS375/DS376/DS398/DS442/DS460/DS469/DS471 o
    // ca 2 vong ghi that allSave / updateDetail.
    private static void checkRecipeTemplateDetailStuffTypeValues(
            RecipeTemplateDetail detail) {
        if (detail == null) {
            return;
        }
        String stuffType = detail.getStuffType();
        if (StringUtils.isBlank(stuffType)) {
            return;
        }
        if (!"0".equals(stuffType)
                && !"1".equals(stuffType)
                && !"2".equals(stuffType)
                && !"3".equals(stuffType)
                && !"4".equals(stuffType)) {
            throw new ServiceException(
                    "Loại vật liệu của chi tiết mẫu đơn thuốc không hợp lệ (chỉ nhận 0 — Thuốc tây, 1 — Thuốc đông y, 2 — Truyền dịch, 3 — Hạng mục khám chữa bệnh, hoặc 4 — Vật tư).");
        }
    }


    // DS496: chineseMedicineUse chi co mien {chineseMedicineUse_0,
    // chineseMedicineUse_1} (_0 'Sac truoc nua gio', _1 'Sac truoc mot
    // gio'; dict 'chineseMedicineUse', dung 2 item del_flag='0';
    // frontend chi set tu ChineseUseTimeOption dict kin nay o
    // recipetemplateForm.vue:582). Truoc day khong guard nao nhin
    // chineseMedicineUse nen '_2'/'_9'/'x' deu 200 + persist qua
    // allSave (tao) va updateDetail (sua, xoa truoc chen sau) — mo
    // lai mau thi select cach dung thuoc Dong y trong; ap mau sang
    // don that (medicalOutpatientRecord/index.vue:4614-4632 push
    // nguyen chi tiet mau vao don that) thi gia tri gia bi guard
    // DS467 chan 400 o buoc luu nen mau bi doc/khong dung duoc; gia
    // tri gia con che gia tri hop le cua chinh thuoc
    // (recipetemplateForm.vue:2604-2605 chi copy
    // drug.chineseMedicineUse khi detail chua co). Khac DS467
    // (RecipelDetail.chineseMedicineUse — don that, entity/bang/
    // endpoint khac) va DS440 (Drug.chineseMedicineUse — entity
    // khac) va DS375/DS376/DS398/DS442/DS460/DS469/DS471/DS474
    // (cung entity, field khac). null/blank giu hop le (cot varchar
    // NULL-able, live 15/20 dong NULL). Goi canh DS375/DS376/DS398/
    // DS442/DS460/DS469/DS471/DS474 o ca 2 vong ghi that allSave /
    // updateDetail.
    private static void checkRecipeTemplateDetailChineseMedicineUseValues(
            RecipeTemplateDetail detail) {
        if (detail == null) {
            return;
        }
        DictItem chineseMedicineUse = detail.getChineseMedicineUse();
        if (chineseMedicineUse == null
                || StringUtils.isBlank(chineseMedicineUse.getValue())) {
            return;
        }
        String value = chineseMedicineUse.getValue();
        if (!"chineseMedicineUse_0".equals(value)
                && !"chineseMedicineUse_1".equals(value)) {
            throw new ServiceException(
                    "Cách dùng thuốc Đông y của chi tiết mẫu đơn thuốc không hợp lệ (chỉ nhận chineseMedicineUse_0 hoặc chineseMedicineUse_1).");
        }
    }


    // DS497: westernMedicineUse chi co mien {westernMedicineUse_0, ..,
    // westernMedicineUse_7} (_0 'Uong', _1 'Tiem tinh mach', _2 'Truoc
    // bua an', _3 'Sau bua an', _4 'Tiem duoi da', _5 'Ngam tan',
    // _6 'Dap ngoai', _7 'Truyen tinh mach'; dict 'westernMedicineUse',
    // dung 8 item del_flag='0'; frontend chi set tu WesternUseOption
    // dict kin nay o recipetemplateForm.vue:246-258). Truoc day khong
    // guard nao nhin westernMedicineUse nen '_8'/'_9'/'x' deu 200 +
    // persist qua allSave (tao) va updateDetail (sua, xoa truoc chen
    // sau) — mo lai mau thi select cach dung thuoc Tay trong; ap mau
    // sang don that (medicalOutpatientRecord/index.vue:4614-4632 push
    // nguyen chi tiet mau vao don that) thi gia tri gia bi guard
    // DS466 chan 400 o buoc luu nen mau bi doc/khong dung duoc; gia
    // tri gia con che gia tri hop le cua chinh thuoc
    // (recipetemplateForm.vue:2586-2587 chi copy
    // drug.westernMedicineUse khi detail chua co). Khac DS466
    // (RecipelDetail.westernMedicineUse — don that, entity/bang/
    // endpoint khac) va DS458 (Drug.westernMedicineUse — entity
    // khac) va DS496 (cung entity, field chineseMedicineUse).
    // null/blank giu hop le (cot varchar(64) NULL-able, live 12/20
    // dong NULL). Goi canh DS375/DS376/DS398/DS442/DS460/DS469/
    // DS471/DS474/DS496 o ca 2 vong ghi that allSave / updateDetail.
    private static void checkRecipeTemplateDetailWesternMedicineUseValues(
            RecipeTemplateDetail detail) {
        if (detail == null) {
            return;
        }
        DictItem westernMedicineUse = detail.getWesternMedicineUse();
        if (westernMedicineUse == null
                || StringUtils.isBlank(westernMedicineUse.getValue())) {
            return;
        }
        String value = westernMedicineUse.getValue();
        if (!"westernMedicineUse_0".equals(value)
                && !"westernMedicineUse_1".equals(value)
                && !"westernMedicineUse_2".equals(value)
                && !"westernMedicineUse_3".equals(value)
                && !"westernMedicineUse_4".equals(value)
                && !"westernMedicineUse_5".equals(value)
                && !"westernMedicineUse_6".equals(value)
                && !"westernMedicineUse_7".equals(value)) {
            throw new ServiceException(
                    "Cách dùng thuốc Tây của chi tiết mẫu đơn thuốc không hợp lệ (chỉ nhận westernMedicineUse_0 đến westernMedicineUse_7).");
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
            checkRecipeTemplateDetailInfuseGroupValues(recipeTemplateDetail); // DS398
            checkRecipeTemplateDetailDaysValues(recipeTemplateDetail); // DS442
            checkRecipeTemplateDetailInfuseUseValues(recipeTemplateDetail); // DS460
            checkRecipeTemplateDetailFrequencyValues(recipeTemplateDetail); // DS469
            checkRecipeTemplateDetailInfuseTypeValues(recipeTemplateDetail); // DS471
            checkRecipeTemplateDetailStuffTypeValues(recipeTemplateDetail); // DS474
            checkRecipeTemplateDetailChineseMedicineUseValues(recipeTemplateDetail); // DS496
            checkRecipeTemplateDetailWesternMedicineUseValues(recipeTemplateDetail); // DS497
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
                    checkRecipeTemplateDetailInfuseGroupValues(recipeTemplateDetail); // DS398
                    checkRecipeTemplateDetailDaysValues(recipeTemplateDetail); // DS442
                    checkRecipeTemplateDetailInfuseUseValues(recipeTemplateDetail); // DS460
                    checkRecipeTemplateDetailFrequencyValues(recipeTemplateDetail); // DS469
                    checkRecipeTemplateDetailInfuseTypeValues(recipeTemplateDetail); // DS471
                    checkRecipeTemplateDetailStuffTypeValues(recipeTemplateDetail); // DS474
                    checkRecipeTemplateDetailChineseMedicineUseValues(recipeTemplateDetail); // DS496
                    checkRecipeTemplateDetailWesternMedicineUseValues(recipeTemplateDetail); // DS497
                    recipeTemplateDetail.preInsert();
                    recipeTemplateDetail.setId(IdGen.uuid());
                    recipeTemplateDetail.setRecipeTemplateInfo(recipeTemplateInfoId);
                    recipeTemplateDetail.setCompany(company);
                    this.dao.allSave(recipeTemplateDetail);

            }
        }
    }
}