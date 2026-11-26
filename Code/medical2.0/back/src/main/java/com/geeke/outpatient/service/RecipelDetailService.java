package com.geeke.outpatient.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.cure.entity.InspectionCheck;
import com.geeke.cure.service.InspectionCheckInfoService;
import com.geeke.cure.service.InspectionCheckService;
import com.geeke.outpatient.dao.PresDrugMapper;
import com.geeke.outpatient.dao.RecipelDetailDao;
import com.geeke.outpatient.dao.RecipelInfoDao;
import com.geeke.outpatient.entity.*;
import com.geeke.stock.dao.DrugDao;
import com.geeke.stock.dao.StuffDao;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.Stuff;
import com.geeke.treatment.dao.CostItemDao;
import com.geeke.treatment.entity.CostItem;
import com.geeke.sys.entity.SysParamConfig;
import com.geeke.sys.service.SysParamConfigService;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Chi tiết đơn thuốc Service
 * @author txl
 * @version 2022-06-07
 */
 
@Service("recipelDetailService")
@Transactional(readOnly = true)
public class RecipelDetailService extends CrudService<RecipelDetailDao, RecipelDetail>{
    @Autowired
    private RecipelDetailDao recipelDetailDao;

    @Autowired
    private RecipelInfoDao recipelInfoDao;

    @Lazy
    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private InspectionCheckService inspectionCheckService;

    @Autowired
    private InspectionCheckInfoService inspectionCheckInfoService;

    @Autowired
    private SysParamConfigService sysParamConfigService;

    @Autowired
    private DrugDao drugDao;

    @Autowired
    private StuffDao stuffDao;

    @Autowired
    private PresDrugMapper presDrugMapper;

    @Autowired
    private CostItemDao costItemDao;

    // DS108 C1/C10-C12: save/bulkInsert/bulkUpdate truoc day de lo:
    // save voi id khong ton tai roi qua CrudService.save -> doUpdate 0
    // dong roi van 200 silent no-op (verifyTenantOwnershipAgainstStoredRow
    // bo qua khi stored==null, cung mau DS94/DS99); bulkInsert/bulkUpdate
    // bypass @Valid nen minTotal thieu van ghi duoc (min_total NOT NULL
    // trong DB) -> 500; single save thieu minTotal cung 500 vi minTotal
    // KHONG co @NotNull nen @Valid khong bat duoc (probe C11).
    // Kiem tra ton tai + minTotal truoc moi ghi. @Transactional
    // (readOnly = false) tuong minh vi class-level la readOnly = true
    // (mau DS105/DS106/DS107). Chi validate minTotal o day, CO TINH:
    // isUnpackSell tuy @NotNull tren single save nhung cac caller noi bo
    // (MedicalRecordService saveRecipelInfoEvtTo) trong vao gia tri
    // frontend gui va cot is_unpack_sell nullable -- chan o service save
    // se lam vo luong ke don dang chay; company cung nullable va caller
    // noi bo co the de null khi registration khong co company. Ca hai
    // deu khong thuoc lop 500-NOT-NULL. addRetailrecipelDetails goi
    // super.save nen khong bi anh huong; updateById/batchDelete khong
    // doi.
    @Override
    @Transactional(readOnly = false)
    public RecipelDetail save(RecipelDetail entity) {
        if (entity != null && StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Chi tiết đơn thuốc không tồn tại: "
                    + entity.getId());
        }
        checkRecipelDetailRequired(entity);
        // DS209: so luong / don gia / tong tien am truoc day ghi thang
        // (probe pre 9/9: save total=-5/minTotal=-5/unitPrice=-10/
        // allFee=-50 tra 100 va luu am that; bulkInsert cung lo). total
        // va allFee chay thang vao phieu thu (TollInfoService.saveToll
        // cong allFee vao actualPayment) va tru kho
        // (MedicinalStorageControlService dung total); so am lam phieu
        // thu am va kho tang khi phat thuoc. Khong co @Min/@Positive
        // tren entity nen @Valid khong bat duoc; khong co CHECK o DB.
        // Chan fail-closed 400 truoc moi ghi; null giu nguyen (cot
        // nullable, UI tinh sau), 0 giu nguyen (vo hai, khong pha luong
        // so luong 0 hien co).
        checkNonNegative(entity);
        // DS299: thuc thu khong duoc vuot tong tien cua chinh dong do.
        // Luong that: saveToll dat actualPayment = allFee * discount,
        // discount trong [0,1] (DS254) nen actualPayment <= allFee;
        // MedicalRecordService chi set 0.00; K2/Q1 reset ve NULL.
        // DS254 dong duong he so; day dong duong ghi truc tiep.
        // DS239 chi chan chieu am. == giu hop le (thu du), null giu
        // hop le (cot nullable, quy uoc DS219).
        checkActualPaymentNotExceedAllFee(entity);
        // DS300: tong luong don vi nho nhat khong duoc thap hon tong
        // so luong ke don (xem checkMinTotalNotLessThanTotal).
        checkMinTotalNotLessThanTotal(entity);
        checkRecipelDetailStuffTypeValues(entity);
        // DS390: trang thai truyen dich cua dong don chi 0 (cho truyen)
        // / 1 (da hoan thanh) -- frontend cure/infusion/index.vue:673-674
        // chi re ===0 / ===1; gia tri gia 9/2/-1 truoc day ghi thang qua
        // mapper (insert :216, update :248) roi khong hien nut trang thai
        // nao tren trang truyen dich. int primitive nen omit -> 0 van qua.
        checkRecipelDetailInfuseTypeValues(entity);
        checkRecipelDetailIsUnpackSellValues(entity); // DS394
        checkRecipelDetailFrequencyValues(entity); // DS419
        checkRecipelDetailIsExtraValues(entity); // DS425
        checkRecipelDetailInfuseUseValues(entity); // DS433
        checkRecipelDetailDaysValues(entity); // DS464
        checkRecipelDetailWesternMedicineUseValues(entity); // DS466
        checkRecipelDetailChineseMedicineUseValues(entity); // DS467
        checkRecipelDetailRetailTypeValues(entity); // DS473
        checkRecipelDetailSkinTestValues(entity); // DS479
        checkRecipelDetailInfuseGroupValues(entity); // DS536
        checkExecutionsNotExceedCap(entity);
        // DS149 C: chan do dai truoc moi ghi (mau DS148 A). bulk* bypass
        // @Valid nen truong co @Length van 500 o mapper khi qua bulk
        // (drippingSpeed 45, retailType 1, unitType 100); gia tri
        // DictItem westernMedicineUse/frequency/chineseMedicineUse/
        // infuseUse/skinTest .value -> varchar(64), days .name ->
        // varchar(64) (mapper bind #{days.name}, khong phai .value);
        // truong KHONG co @Length: stuffType -> varchar(100), remarks
        // -> varchar(255). Gioi han theo cot DB; null-safe.
        checkRecipelDetailLengths(entity);
        checkRecipelDetailRefsExist(entity);
        return super.save(entity);
    }

    // DS732: fail-closed 400 khi don thuoc / thuoc-vat tu khong ton tai.
    // Truoc day save()/bulkInsert/bulkUpdate chi kiem tra minTotal
    // (checkRecipelDetailRequired) roi ghi recipel_info_id/drug_stuff_id
    // thang vao DB: id ma (vd 999999999999999999) van 200 + dong mo coi
    // (probe DS732 B1c/B2c/B4b/B5b). Khong co FK that tren 2 cot nen DB
    // khong chan. drugStuffId theo luong that cua he thong la id thuoc
    // (drug) hoac vat tu (stuff) hoac thuoc ke don (pres_drug) -- xem
    // MedicinalStorageControlService:672-673 (drug roi stuff) va
    // DataBean:212 (presDrug truoc drug) -- HOAC hang muc kham chua benh
    // (cost_item): RecipelDetailDao.xml:579 findByInfoId JOIN cost_item
    // ON ci.id = a.drug_stuff_id (item_type treatmentItemType_2/3), cac
    // luong cure/infusion + RegistrationDao/SchedulesDao deu JOIN nhu vay;
    // chi can 1 trong 4 ton tai. DS732 sua (reg-final phat hien hoi quy):
    // ban dau chi kiem drug/stuff/pres_drug nen detail dung cost_item hop
    // le (vd DS316 fixture COST2/COST3) bi 400 sai, lam 45 leg reg-final
    // FAIL; them cost_item del_flag='0' thi het.
    // Chi kiem tra khi id khac rong: null giu hanh vi cu (DS159 C/DS712
    // chan thieu thuoc o duong allSave, khong phai o day).
    private void checkRecipelDetailRefsExist(RecipelDetail entity) {
        if (entity == null) {
            return;
        }
        if (entity.getRecipelInfo() != null
                && StringUtils.isNotBlank(entity.getRecipelInfo().getId())) {
            RecipelInfo recipelInfo = recipelInfoDao.get(entity.getRecipelInfo().getId());
            if (recipelInfo == null || !"0".equals(recipelInfo.getDelFlag())) {
                throw new ServiceException("Đơn thuốc không tồn tại: "
                        + entity.getRecipelInfo().getId());
            }
        }
        if (entity.getDrugStuffId() != null
                && StringUtils.isNotBlank(entity.getDrugStuffId().getDrugStuffId())) {
            String drugStuffId = entity.getDrugStuffId().getDrugStuffId();
            boolean exists = false;
            Drug drug = drugDao.get(drugStuffId);
            if (drug != null && "0".equals(drug.getDelFlag())) {
                exists = true;
            }
            if (!exists) {
                Stuff stuff = stuffDao.get(drugStuffId);
                if (stuff != null && "0".equals(stuff.getDelFlag())) {
                    exists = true;
                }
            }
            if (!exists && presDrugMapper.selectById(drugStuffId) != null) {
                exists = true;
            }
            if (!exists) {
                CostItem costItem = costItemDao.get(drugStuffId);
                if (costItem != null && "0".equals(costItem.getDelFlag())) {
                    exists = true;
                }
            }
            if (!exists) {
                throw new ServiceException("Thuốc/vật tư không tồn tại: " + drugStuffId);
            }
        }
    }

    /**
     * DS119 A: mapper <update id="delete"> co nhanh <otherwise> xoa mem
     * theo recipel_info_id khi id rong -- delete khong id + recipelInfo
     * se xoa sach moi dong cua don thuoc. Chan o service: chi xoa khi
     * co id; khong caller noi bo nao dung duong khong id (kiem tra
     * 2026-11-07). Mau guard giong Department/Role (DS50) va
     * GenTableColumn (DS118).
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(RecipelDetail entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được chi tiết đơn thuốc cần xóa.");
        }
        return super.delete(entity);
    }

    // DS108 C3-C6/C10: bulkInsert truoc day de lo: null/rong roi xuong
    // MyBatis <foreach> rong -> 500 (hien tai global handler anh xa mot
    // so loi thanh 400, nhung van chan ro o service de hanh vi khong phu
    // thuoc vao exception mapping); thieu minTotal -> 500 NOT NULL.
    // Chan het o service de 400 ro nghia.
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<RecipelDetail> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chi tiết đơn thuốc không được để trống");
        }
        for (RecipelDetail entity : entitys) {
            checkRecipelDetailRequired(entity);
            checkNonNegative(entity);
            checkActualPaymentNotExceedAllFee(entity);
            checkMinTotalNotLessThanTotal(entity);
            checkRecipelDetailStuffTypeValues(entity);
            checkRecipelDetailInfuseTypeValues(entity); // DS390
            checkRecipelDetailIsUnpackSellValues(entity); // DS394
            checkRecipelDetailFrequencyValues(entity); // DS419
            checkRecipelDetailIsExtraValues(entity); // DS425
            checkRecipelDetailInfuseUseValues(entity); // DS433
            checkRecipelDetailDaysValues(entity); // DS464
            checkRecipelDetailWesternMedicineUseValues(entity); // DS466
        checkRecipelDetailChineseMedicineUseValues(entity); // DS467
        checkRecipelDetailRetailTypeValues(entity); // DS473
        checkRecipelDetailSkinTestValues(entity); // DS479
        checkRecipelDetailInfuseGroupValues(entity); // DS536
            checkExecutionsNotExceedCap(entity);
            checkRecipelDetailLengths(entity);
            checkRecipelDetailRefsExist(entity);
        }
        return super.bulkInsert(entitys);
    }

    // DS108 C2/C3/C5/C9/C12: bulkUpdate truoc day de lo: id khong ton tai
    // -> 200 silent no-op; batch hon hop [valid, nonexistent] doi ca dong
    // hop le truoc khi cham id ma; minTotal bi drop -> CASE set
    // min_total=NULL -> 500 NOT NULL. Tien kiem TOAN BO list truoc moi ghi
    // (fail-closed ca list, mau N1).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<RecipelDetail> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chi tiết đơn thuốc không được để trống");
        }
        for (RecipelDetail entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Chi tiết đơn thuốc không được để trống");
            }
            if (StringUtils.isNotBlank(entity.getId())
                    && dao.get(entity.getId()) == null) {
                throw new ServiceException("Chi tiết đơn thuốc không tồn tại: "
                        + entity.getId());
            }
            checkRecipelDetailRequired(entity);
            checkNonNegative(entity);
            checkActualPaymentNotExceedAllFee(entity);
            checkMinTotalNotLessThanTotal(entity);
            checkRecipelDetailStuffTypeValues(entity);
            checkRecipelDetailInfuseTypeValues(entity); // DS390
            checkRecipelDetailIsUnpackSellValues(entity); // DS394
            checkRecipelDetailFrequencyValues(entity); // DS419
            checkRecipelDetailIsExtraValues(entity); // DS425
            checkRecipelDetailInfuseUseValues(entity); // DS433
            checkRecipelDetailDaysValues(entity); // DS464
            checkRecipelDetailWesternMedicineUseValues(entity); // DS466
        checkRecipelDetailChineseMedicineUseValues(entity); // DS467
        checkRecipelDetailRetailTypeValues(entity); // DS473
        checkRecipelDetailSkinTestValues(entity); // DS479
        checkRecipelDetailInfuseGroupValues(entity); // DS536
            checkExecutionsNotExceedCap(entity);
            checkRecipelDetailLengths(entity);
            checkRecipelDetailRefsExist(entity);
        }
        return super.bulkUpdate(entitys);
    }

    // DS108 C7-C8: bulkDelete null/rong -> 400 ro nghia thay vi roi xuong
    // MyBatis <foreach> rong. bulkDelete id khong ton tai GIU NGUYEN
    // 200 rows 0 idempotent (DS99 G11) -- khong chan o day.
    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<RecipelDetail> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chi tiết đơn thuốc không được để trống");
        }
        return super.bulkDelete(entitys);
    }

    // DS209: xem ghi chu o save(). Chi 4 truong chay vao tien/kho:
    // total, minTotal, unitPrice, allFee. Null-safe (null giu nguyen),
    // 0 giu nguyen; am -> ServiceException (400).
    // DS218: them executions — tien do thuc hien tich luy. Duong viet
    // la save/bulkInsert/bulkUpdate o day (insert/bulk mapper deu co cot
    // executions) VA duong updateById qua CureManagementController.save
    // (guard rieng o CureManagementService.save, co mien tru thu hoi).
    // executions am lam sai schedule() (sum(executions)-sum(total)) va
    // dem hoan thanh total==executions trong updateById. Null-safe,
    // 0 giu nguyen; am -> ServiceException (400).
    // DS239: them singleDosage + actualPayment — 2 truong so con lai
    // cua entity chua duoc chan. singleDosage (lieu dung moi lan) am
    // vo nghia lam sang va chay vao phieu in (DataBean useText) +
    // payload BHYT bat buoc sinDoscnt (MdMedicationOrderImpl:308);
    // ban mau RecipetemplateDetailService (DS214) DA chan singleDosage
    // trong khi don that o day thi chua. actualPayment (thuc thu) am
    // qua save truc tiep lam sai doi soat thu phi; cac caller noi bo
    // chi set 0.00 (MedicalRecordService) hoac gia tri khong am qua
    // updateActualPayment DAO (TollInfoService) nen chan o day khong
    // vo luong dang chay. Mapper ghi ca 2 cot tren insert/update/
    // bulkInsert/bulkUpdate; updateById chi ghi executions +
    // infuse_type nen khong cham 2 truong nay.
    private static void checkNonNegative(RecipelDetail entity) {
        if (entity == null) {
            return;
        }
        if (entity.getSingleDosage() != null
                && entity.getSingleDosage().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Liều dùng mỗi lần không được âm");
        }
        if (entity.getTotal() != null && entity.getTotal() < 0) {
            throw new ServiceException("Tổng số lượng không được âm");
        }
        if (entity.getMinTotal() != null && entity.getMinTotal() < 0) {
            throw new ServiceException("Tổng lượng đơn vị nhỏ nhất không được âm");
        }
        if (entity.getUnitPrice() != null
                && entity.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Đơn giá không được âm");
        }
        if (entity.getAllFee() != null
                && entity.getAllFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Tổng tiền không được âm");
        }
        if (entity.getActualPayment() != null
                && entity.getActualPayment().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Thực thu không được âm");
        }
        if (entity.getExecutions() != null
                && entity.getExecutions().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Tiến độ thực hiện không được âm");
        }
    }

    // DS299: thuc thu khong duoc vuot tong tien cua chinh dong do.
    // Ca 2 co gia tri va actualPayment > allFee -> ServiceException
    // 400 fail-closed; == hop le, mot trong hai null hop le.
    private static void checkActualPaymentNotExceedAllFee(
            RecipelDetail entity) {
        if (entity == null) {
            return;
        }
        if (entity.getAllFee() != null && entity.getActualPayment() != null
                && entity.getActualPayment().compareTo(entity.getAllFee())
                        > 0) {
            throw new ServiceException(
                    "Thực thu không được vượt quá tổng tiền");
        }
    }

    // DS316: tien do thuc hien (executions) khong duoc vuot tran cua
    // chinh dong do. Tran = so ngay (days.name) voi truyen dich
    // (stuffType "2", tien do dem theo NGAY, updateById nhanh
    // thuong dem hoan thanh theo days, infusion UI cap excuteNum
    // o days-executions); tran = tong so luong ke don (total) voi
    // cac loai con lai (cureManagement UI cap o total-executions,
    // nhanh stuffType "3" dem hoan thanh theo total, schedule() =
    // sum(executions)-sum(total) nen vuot tran lam don khong bao
    // gio hoan thanh). Ca 2 co gia tri va executions > tran ->
    // ServiceException 400 fail-closed; == hop le, mot trong hai
    // null hop le (cot nullable, quy uoc DS111/DS219). days khong
    // parse duoc so -> roi ve total (du lieu cu lech, khong 400
    // mu). Luong that: UI luon gui executions <= tran (input-number
    // :max o ca 2 man hinh), caller noi bo chi set 0 khi tao
    // (MedicalRecordController:233); live DB 0 dong vuot tran.
    private static void checkExecutionsNotExceedCap(
            RecipelDetail entity) {
        if (entity == null || entity.getExecutions() == null) {
            return;
        }
        BigDecimal cap = null;
        boolean infusionCap = false;
        if ("2".equals(entity.getStuffType()) && entity.getDays() != null
                && entity.getDays().getName() != null) {
            try {
                cap = new BigDecimal(entity.getDays().getName().trim());
                infusionCap = true;
            } catch (NumberFormatException e) {
                cap = null;
            }
        }
        if (cap == null && entity.getTotal() != null) {
            cap = BigDecimal.valueOf(entity.getTotal());
            infusionCap = false;
        }
        if (cap != null
                && entity.getExecutions().compareTo(cap) > 0) {
            throw new ServiceException(infusionCap
                    ? "Tiến độ thực hiện không được vượt quá số ngày truyền"
                    : "Tiến độ thực hiện không được vượt quá tổng số lượng");
        }
    }

    // DS300: tong luong don vi nho nhat (minTotal) khong duoc thap hon
    // tong so luong ke don (total) cua chinh dong do. minTotal la tong
    // luong theo don vi bao che / don vi nho nhat; total la tong so
    // luong theo don vi ban. Ban theo goi thi minTotal = total x so
    // vien/goi (> total); ban le thi minTotal = total. Luong that duy
    // nhat: MedicalRecordService :612/:1258/:1328/:1508 default
    // minTotal = total khi frontend khong gui (frontend khong he gui
    // minTotal), nen luong that luon co minTotal >= total; khong caller
    // noi bo nao set minTotal khac total. Live DB 56/56 dong active co
    // minTotal == total, 0 dong vi pham. minTotal chay vao chiem dung
    // kho (RecipelStastics.occupy = minTotal) nen minTotal < total lam
    // kho bi chiem dung thieu. Ca 2 co gia tri va minTotal < total ->
    // ServiceException 400 fail-closed; == hop le (mac dinh cua luong
    // that), > hop le (ban theo goi), total null hop le (cot nullable;
    // minTotal null da 400 o checkRecipelDetailRequired, quy uoc
    // DS219). Khac DS297 da loai tru (allFee vs total x unitPrice phu
    // thuoc isUnpackSell, co ngoai le) va DS111 (executions vs total,
    // BigDecimal phan so, ngu nghia mo).
    private static void checkMinTotalNotLessThanTotal(
            RecipelDetail entity) {
        if (entity == null) {
            return;
        }
        if (entity.getTotal() != null && entity.getMinTotal() != null
                && entity.getMinTotal() < entity.getTotal()) {
            throw new ServiceException(
                    "Tổng lượng đơn vị nhỏ nhất không được thấp hơn tổng số lượng");
        }
    }

    // DS379: loai vat lieu (stuffType) chi co 5 gia tri co nghia:
    // '0' Thuoc tay, '1' Thuoc dong y, '2' Truyen dich, '3' Hang muc
    // kham chua benh, '4' Vat tu (MedicalRecordService.java:1642,
    // RecipetemplateDetailService.java:247). Truoc day service chi
    // chan do dai (DS149 checkLength <= 100) nen gia tri gia ('9'/
    // '5'/'x') qua save/bulkInsert/bulkUpdate roi luu thang (probe
    // pre 18/18). Gia tri gia roi khoi moi nhanh doc: getDrugStuffEvt
    // chi re nhanh '0'/'1'/'2' (thuoc), '3' (costItem), '4' (vat tu)
    // nen chi tiet gia tra ve evt rong -- mat ten/gia o trang thu phi
    // (charge/index.vue:948) va roi khoi ban in (DataBean '0'/'1'/
    // '2'/'3'); '2' con quyet WESTERN/PATENT (:1472) va tran truyen
    // dich (RecipelDetailService:287). Khac isUnpackSell cua cung
    // entity: truong do CO TINH khong guard theo DS108 (5 caller noi
    // bo trong vao gia tri frontend gui); stuffType khong co ghi chu
    // nao nhu vay, va cac caller noi bo (MedicalRecordService
    // saveRecipelInfoEvtTo) chi dat '0'..'4' tu tab don cua UI.
    // Ngoai {'0'..'4'} -> ServiceException 400 fail-closed; null/''
    // giu nguyen (cot nullable, DB live co 1 dong NULL, tien le
    // DS373/DS375/DS376).
    private static void checkRecipelDetailStuffTypeValues(
            RecipelDetail entity) {
        if (entity == null) {
            return;
        }
        String stuffType = entity.getStuffType();
        if (StringUtils.isBlank(stuffType)) {
            return;
        }
        if (!"0".equals(stuffType) && !"1".equals(stuffType)
                && !"2".equals(stuffType) && !"3".equals(stuffType)
                && !"4".equals(stuffType)) {
            throw new ServiceException(
                    "Loại vật liệu không hợp lệ: " + stuffType);
        }
    }

    // DS390: trang thai truyen dich cua dong don chi 0 / 1 (xem ghi chu
    // o save()). int primitive nen omit mac dinh 0, khong co null.
    // Ngoai {0,1} -> ServiceException 400 fail-closed.
    private static void checkRecipelDetailInfuseTypeValues(
            RecipelDetail entity) {
        if (entity == null) {
            return;
        }
        int infuseType = entity.getInfuseType();
        if (infuseType != 0 && infuseType != 1) {
            throw new ServiceException(
                    "Trạng thái truyền dịch không hợp lệ: " + infuseType);
        }
    }

    // DS394: ban le tach le (isUnpackSell) chi co mien {0,1} (0: ban
    // theo dong goi, 1: ban le tach le; frontend el-select chi co 2
    // option 0/1, medicalOutpatientRecord/index.vue :1072/:1764).
    // Truoc day khong guard nao nhin isUnpackSell nen 9/2/-1 deu 200
    // + persist qua save/bulkInsert/bulkUpdate (probe pre 20/20).
    // Gia tri gia roi khoi moi nhanh doc: MedicinalStorageControl-
    // Service:704 chi doi don vi khi == 1 nen dong gia chiem dung kho
    // sai (total tinh nhu so goi); frontend === 1 hien don vi bao
    // che / gia le (:1033/:1165) nen dong gia hien sai don vi/gia.
    // Khac DS108: ghi chu do chi noi KHONG chan null (5 caller noi
    // bo trong vao gia tri frontend gui + cot nullable) — guard nay
    // null-safe, null giu hop le (bulk omit -> NULL nhu 4 dong live;
    // single-save omit van 400 san qua @NotNull :221, khong doi).
    // Khac Drug.isUnpackSell (DS364), Stuff.isUnpackSell (DS367),
    // RecipeTemplateDetail.isUnpackSell (DS376) — day la chi tiet
    // DON THAT. Ngoai {0,1} -> ServiceException 400 fail-closed.
    private static void checkRecipelDetailIsUnpackSellValues(
            RecipelDetail entity) {
        if (entity == null || entity.getIsUnpackSell() == null) {
            return;
        }
        if (entity.getIsUnpackSell() != 0
                && entity.getIsUnpackSell() != 1) {
            throw new ServiceException(
                    "Bán lẻ tách lẻ không hợp lệ (0: Không; 1: Có).");
        }
    }

    // DS419: tan suat va lieu dung (frequency) chi co nghia trong hop
    // cua 2 dict kin cung ghi cot nay: 'recipelDetailFrequency' (4 item
    // _0.._3; recipelDetailForm.vue:294) + 'chineseMedicineRecipelTakeFrequency'
    // (5 item _0.5/_1/_1.5/_2/_3; trang kham FrequencyOption,
    // medicalOutpatientRecord/index.vue:6600). Truoc day service chi
    // chan do dai (DS149 checkDictLength <= 64) nen gia tri gia ('9'/
    // 'x'/'recipelDetailFrequency_9') qua save/bulkInsert/bulkUpdate
    // roi luu thang (probe pre 18/18). Gia tri gia roi khoi moi nhanh
    // doc: DataBean :226/:251 in don dung frequency.getName() -> ten
    // rong, :330 setSkinTest(frequency.getRemarks()) -> rong;
    // MdMedicationOrderImpl :312-314 gui BHYT usedFrquCodg =
    // frequency.value + usedFrquName = frequency.name (bat buoc) -> ma
    // gia ra ngoai BHYT; list LEFT JOIN hien frequency.name rong
    // (:2309/:3208). Ngoai hop 9 gia tri -> ServiceException 400
    // fail-closed; null/blank giu nguyen (cot nullable, live 12/55
    // NULL, tien le DS379/DS390/DS394). Writer noi bo an toan:
    // MedicalRecordService khong setFrequency, trang kham + form chi
    // set tu 2 dict kin tren.
    private static void checkRecipelDetailFrequencyValues(
            RecipelDetail entity) {
        if (entity == null || entity.getFrequency() == null) {
            return;
        }
        String frequency = entity.getFrequency().getValue();
        if (StringUtils.isBlank(frequency)) {
            return;
        }
        if (!"recipelDetailFrequency_0".equals(frequency)
                && !"recipelDetailFrequency_1".equals(frequency)
                && !"recipelDetailFrequency_2".equals(frequency)
                && !"recipelDetailFrequency_3".equals(frequency)
                && !"chineseMedicineRecipelTakeFrequency_0.5".equals(frequency)
                && !"chineseMedicineRecipelTakeFrequency_1".equals(frequency)
                && !"chineseMedicineRecipelTakeFrequency_1.5".equals(frequency)
                && !"chineseMedicineRecipelTakeFrequency_2".equals(frequency)
                && !"chineseMedicineRecipelTakeFrequency_3".equals(frequency)) {
            throw new ServiceException(
                    "Tần suất và liều dùng không hợp lệ: " + frequency);
        }
    }

    // DS425: isExtra chi co mien {0,1} (0: thuoc chinh, 1: phi phu
    // them; dong moi mac dinh 0, MedicalRecordService:1510). Truoc day
    // khong guard nao nhin isExtra nen isExtra=9/2/-1 deu 200 +
    // persist qua save/bulkInsert/bulkUpdate — dong gia roi vao CA HAI
    // danh sach o man hinh kham (filterRecipel giu isExtra !== 1,
    // filterAdditional giu isExtra !== 0, medicalOutpatientRecord
    // /index.vue :4428/:4446) va bien mat khoi list loc is_extra=0
    // (mapper :581), dong thoi cac nhanh hien thi `isExtra === 0`
    // (ten thuoc/gia/so ngay) an dong gia. Khac DS375
    // (RecipeTemplateDetail.isExtra, chi tiet MAU) — day la don
    // that. null giu hop le (cot int nullable, 55 dong live deu 0).
    // Goi o ca 4 vong ghi that save/bulkInsert/bulkUpdate/
    // addRetailrecipelDetails, mau DS394/DS419.
    private static void checkRecipelDetailIsExtraValues(
            RecipelDetail entity) {
        if (entity == null || entity.getIsExtra() == null) {
            return;
        }
        if (entity.getIsExtra() != 0 && entity.getIsExtra() != 1) {
            throw new ServiceException(
                    "Phí phụ thêm không hợp lệ (0: Không; 1: Có).");
        }
    }

    // DS433: infuseUse chi co mien {infuseUse_0, infuseUse_1,
    // infuseUse_2, infuseUse_3} (0: Tiem tinh mach, 1: Truyen tinh
    // mach, 2: Tiem bap, 3: Khi dung; dict type 'infuseUse', dung 4
    // item del_flag='0', frontend chi set tu dict kin nay qua
    // infuseUseOption). Truoc day khong guard nao nhin infuseUse
    // nen gia tri gia ('infuseUse_9'/'infuseUse_4'/'x') deu 200 +
    // persist qua save/bulkInsert/bulkUpdate — dong gia hien ten
    // duong truyen RONG tren phieu truyen dich (cure/infusion/
    // index.vue :750/:811, theo tung nhom infuseGroup), tren form
    // duyet don (reviewForm.vue :135) va man kham
    // (medicalOutpatientRecord/index.vue :2706), dieu duong khong
    // biet duong truyen nao. Khac DS390 (infuseType chi la trang
    // thai 0/1 cua dong truyen). null/blank giu hop le (cot
    // varchar(64) nullable, live 52/55 NULL). Goi o ca 4 duong ghi
    // that save/bulkInsert/bulkUpdate/addRetailrecipelDetails,
    // mau DS419/DS425.
    private static void checkRecipelDetailInfuseUseValues(
            RecipelDetail entity) {
        if (entity == null || entity.getInfuseUse() == null) {
            return;
        }
        String infuseUse = entity.getInfuseUse().getValue();
        if (StringUtils.isBlank(infuseUse)) {
            return;
        }
        if (!"infuseUse_0".equals(infuseUse)
                && !"infuseUse_1".equals(infuseUse)
                && !"infuseUse_2".equals(infuseUse)
                && !"infuseUse_3".equals(infuseUse)) {
            throw new ServiceException(
                    "Cách dùng truyền dịch không hợp lệ: " + infuseUse);
        }
    }

    // DS464: so ngay (days.name) chi co nghia la so nguyen khong am <=
    // chronicDays cau hinh cua tenant phien (live 60 ca 2 tenant; mac
    // dinh frontend 90 khi chua cau hinh). Truoc day khong guard nao
    // nhin days nen '999'/'61'/'-5'/'x' deu 200 + persist qua
    // save/bulkInsert/bulkUpdate (probe pre 18/18) — mapper ghi
    // #{days.name} verbatim (insert :195, update :231, bulkInsert :322,
    // bulkUpdate CASE :392-394). Hai that: MdMedicationOrderImpl :287
    // parseLong(days.name) -> '999' thanh don BHYT medcDays='999',
    // end = start+999 ngay; '-5' -> end TRUOC start; DataBean :222 in
    // '999 ngay'; calculateTotalByDays nhan lieu x tan suat x days ->
    // tong luong ke phinh; DS316 + CureManagement dung days.name lam
    // tran tien do truyen. Dict 'recipelDetailDays' chi la 7 item
    // '1'..'7' de hien thi cu — mien THAT la so nguyen theo cau hinh,
    // KHONG phai dict code (UI song da la el-input-number :min=0
    // :max=chronicDisease?chronicDays:normalDays). Cap doc live moi lan
    // ghi qua sysParamConfigService.listAll (tenant-filter san, N3e):
    // lay max cac dong chronicDays parse duoc; thieu/hong/loi doc ->
    // fallback 90 (mac dinh frontend). null/blank giu hop le (cot
    // varchar(64) nullable, live 12/55 NULL). Chuoi khong toan chu so
    // (dau +/-, thap phan, chu) hoac vuot int -> 400; 0 <= n <= cap
    // qua, n > cap -> 400. Goi o ca 4 duong ghi that
    // save/bulkInsert/bulkUpdate/addRetailrecipelDetails, ngay sau
    // checkRecipelDetailInfuseUseValues (DS433). Han che ghi nhan:
    // guard dung cap ngoai chronicDays (60), KHONG tach normalDays
    // (7/10) theo co chronicDisease cua don cha — phan biet
    // normal/chronic van chi UI enforce; don thuong gia 8-60 ngay van
    // qua server. Dung thiet ke DS430; tach theo don cha la item rieng
    // neu can.
    private void checkRecipelDetailDaysValues(RecipelDetail entity) {
        if (entity == null || entity.getDays() == null) {
            return;
        }
        String days = entity.getDays().getName();
        if (StringUtils.isBlank(days)) {
            return;
        }
        String value = days.trim();
        int n;
        try {
            if (!value.matches("[0-9]+")) {
                throw new NumberFormatException(value);
            }
            n = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ServiceException(
                    "Số ngày không hợp lệ (phải là số nguyên không âm): "
                            + value);
        }
        int cap = getChronicDaysCap();
        if (n > cap) {
            throw new ServiceException(
                    "Số ngày vượt quá số ngày tối đa cho phép (" + cap
                            + " ngày)");
        }
    }

    // DS466: cach dung thuoc Tay (westernMedicineUse.value) chi co nghia
    // trong dict 'westernMedicineUse' — dung 8 item live (_0 'Uong',
    // _1 'Tiem tinh mach', _2 'Truoc bua an', _3 'Sau bua an',
    // _4 'Tiem duoi da', _5 'Ngam tan', _6 'Dap ngoai',
    // _7 'Truyen tinh mach'), 0 item da xoa. Truoc day khong guard nao
    // nhin truong nay nen 'westernMedicineUse_9'/'_8'/'x' deu 200 +
    // persist qua save/bulkInsert/bulkUpdate (probe pre 18/18) — mapper
    // ghi #{westernMedicineUse.value} verbatim (insert :193, update
    // :229, bulkInsert :320, bulkUpdate CASE :382-384). Hai that:
    // MdMedicationOrderImpl :272 put medcWayCodg =
    // westernMedicineUse.value — ma duong dung thuoc BAT BUOC cua don
    // BHYT dien tu thanh ma khong ton tai; reviewForm.vue:111 /
    // reviewFormPre.vue:111 hien westernMedicineUse.name rong khi
    // duyet don; man kham :981 hien ten rong. null/blank giu hop le
    // (cot varchar(64) nullable). Goi o ca 4 duong ghi that
    // save/bulkInsert/bulkUpdate/addRetailrecipelDetails, ngay sau
    // checkRecipelDetailDaysValues (DS464), mau DS433 infuseUse.
    private static void checkRecipelDetailWesternMedicineUseValues(
            RecipelDetail entity) {
        if (entity == null || entity.getWesternMedicineUse() == null) {
            return;
        }
        String westernMedicineUse =
                entity.getWesternMedicineUse().getValue();
        if (StringUtils.isBlank(westernMedicineUse)) {
            return;
        }
        if (!"westernMedicineUse_0".equals(westernMedicineUse)
                && !"westernMedicineUse_1".equals(westernMedicineUse)
                && !"westernMedicineUse_2".equals(westernMedicineUse)
                && !"westernMedicineUse_3".equals(westernMedicineUse)
                && !"westernMedicineUse_4".equals(westernMedicineUse)
                && !"westernMedicineUse_5".equals(westernMedicineUse)
                && !"westernMedicineUse_6".equals(westernMedicineUse)
                && !"westernMedicineUse_7".equals(westernMedicineUse)) {
            throw new ServiceException(
                    "Cách dùng thuốc Tây không hợp lệ: "
                            + westernMedicineUse);
        }
    }

    // DS467: cach dung thuoc Dong y (chineseMedicineUse.value) chi co
    // nghia trong dict 'chineseMedicineUse' — dung 2 item live (_0
    // 'Sac truoc nua gio', _1 'Sac truoc mot gio'), 0 item da xoa.
    // Truoc day khong guard nao nhin truong nay nen
    // 'chineseMedicineUse_2'/'_9'/'x' deu 200 + persist qua
    // save/bulkInsert/bulkUpdate (probe pre 18/18) — mapper ghi
    // #{chineseMedicineUse.value} verbatim (insert :196, update
    // :232, bulkInsert :323, bulkUpdate CASE :397-399). Hai that:
    // medicalOutpatientRecord/index.vue:2496/:3304 cot 'Cach dung'
    // hien chineseMedicineUse.name rong khi duyet/xem don;
    // charge/index.vue:968 in (detail.chineseMedicineUse && name)
    // || "" -> cach dung thuoc Dong y rong tren phieu thu/in;
    // recipelDetailForm.vue:94 man xem hien ten rong. null/blank
    // giu hop le (cot varchar(64) nullable). Goi o ca 4 duong ghi
    // that save/bulkInsert/bulkUpdate/addRetailrecipelDetails, ngay
    // sau checkRecipelDetailWesternMedicineUseValues (DS466), mau
    // DS466 westernMedicineUse + DS440 Drug.
    private static void checkRecipelDetailChineseMedicineUseValues(
            RecipelDetail entity) {
        if (entity == null || entity.getChineseMedicineUse() == null) {
            return;
        }
        String chineseMedicineUse =
                entity.getChineseMedicineUse().getValue();
        if (StringUtils.isBlank(chineseMedicineUse)) {
            return;
        }
        if (!"chineseMedicineUse_0".equals(chineseMedicineUse)
                && !"chineseMedicineUse_1".equals(chineseMedicineUse)) {
            throw new ServiceException(
                    "Cách dùng thuốc Đông y không hợp lệ: "
                            + chineseMedicineUse);
        }
    }

    // DS473: loai ban le (retailType) chi co 5 gia tri co nghia,
    // dinh nghia boi nhanh doc DUY NHAT o backend,
    // MedicalRecordService.java:820-842 (nhanh don ban le,
    // RECIPEL_TYPE_EXTERNAL): '0'/'1'/'2' -> tra cuu thuoc (Drug),
    // '3' -> hang muc kham chua benh (CostItem), '4' -> thuoc hoac
    // vat tu (Drug neu co, neu khong Stuff). Truoc day service chi
    // chan do dai (DS149 checkLength <= 1) nen gia tri gia ('5'/
    // '9'/'x') qua save/bulkInsert/bulkUpdate roi luu thang (probe
    // pre 18/18). Gia tri gia roi khoi moi nhanh doc: drugStuffEvt
    // giu rong (khong ten/gia) khi doc/in don ban le. UI writer chi
    // dat '0'/'1' (medicalType_0/_1, GetRetailRecipelDetailEvtList
    // o cureManagement/infusion/tollInfo index.vue); '2'/'3'/'4'
    // tu cac dong truyen dich / hang muc / vat tu. Khac DS379
    // stuffType (cung mien {'0'..'4'} nhung cot/reader khac) va
    // RecipeTemplateDetail.retailType (entity template, chua probe).
    // Ngoai {'0'..'4'} -> ServiceException 400 fail-closed;
    // null/blank giu nguyen (cot varchar(1) NULL-able, live 55/55
    // NULL, tien le DS379/DS467).
    private static void checkRecipelDetailRetailTypeValues(
            RecipelDetail entity) {
        if (entity == null) {
            return;
        }
        String retailType = entity.getRetailType();
        if (StringUtils.isBlank(retailType)) {
            return;
        }
        if (!"0".equals(retailType) && !"1".equals(retailType)
                && !"2".equals(retailType) && !"3".equals(retailType)
                && !"4".equals(retailType)) {
            throw new ServiceException(
                    "Loại bán lẻ không hợp lệ: " + retailType);
        }
    }

    // DS479: test da (skinTest.value) chi co nghia trong dict
    // 'skinTest' — dung 3 item live (_0 'Thu phan ung da',
    // _1 'Dung tiep', _2 'Mien thu'), 0 item da xoa. Truoc day
    // khong guard nao nhin truong nay nen 'skinTest_3'/'_99'/'x'
    // deu 200 + persist qua save/bulkInsert/bulkUpdate (probe
    // pre 18/18) — mapper ghi #{skinTest.value} verbatim
    // (insert :199, update :235, bulkInsert, bulkUpdate CASE).
    // Hai that: DataBean :321
    // infuseEvt.setSkinTest(skinTest.getName()) -> ten rong tren
    // phieu truyen dich; man kham :2718/:3445 + form duyet don
    // hien skinTest.name rong; InfusionPrescription.vue:47-49
    // hien rong. null/blank giu hop le (cot varchar(64)
    // nullable, live 52/55 NULL). Goi o ca 4 duong ghi that
    // save/bulkInsert/bulkUpdate/addRetailrecipelDetails, ngay
    // sau checkRecipelDetailRetailTypeValues (DS473), mau
    // DS466/DS467.
    private static void checkRecipelDetailSkinTestValues(
            RecipelDetail entity) {
        if (entity == null || entity.getSkinTest() == null) {
            return;
        }
        String skinTest = entity.getSkinTest().getValue();
        if (StringUtils.isBlank(skinTest)) {
            return;
        }
        if (!"skinTest_0".equals(skinTest)
                && !"skinTest_1".equals(skinTest)
                && !"skinTest_2".equals(skinTest)) {
            throw new ServiceException(
                    "Test da không hợp lệ: " + skinTest);
        }
    }

    // DS536: nhom truyen dich cua chi tiet don thuoc chi duoc de
    // trong (khong truyen dich) hoac so nhom tu 1 tro len -- frontend
    // cure/infusion/index.vue:1968 index infusionProject[infuseGroup-1];
    // gia tri 0/am khong thuoc nhom nao, tach khoi moi nhom that tren
    // trang truyen dich. Goi o ca 4 duong ghi that
    // save/bulkInsert/bulkUpdate/addRetailrecipelDetails, ngay sau
    // checkRecipelDetailSkinTestValues (DS479), mau DS504/DS398.
    private static void checkRecipelDetailInfuseGroupValues(
            RecipelDetail entity) {
        if (entity == null || entity.getInfuseGroup() == null) {
            return;
        }
        if (entity.getInfuseGroup() < 1) {
            throw new ServiceException(
                    "Nhóm truyền dịch của chi tiết đơn thuốc không hợp lệ (để trống nếu không truyền dịch, hoặc số nhóm từ 1 trở lên).");
        }
    }
    // DS464: doc chronicDays cua tenant phien, live moi lan ghi.
    // listAll tu ap tenant-filter (ensureCompanyFilter, N3e) nen chi
    // thay dong cua tenant phien; lay max cac dong parse duoc (phong
    // cau hinh trung); dong hong/am bo qua; thieu dong, toan dong hong,
    // hoac bat ky loi doc nao -> fallback 90 (mac dinh frontend
    // medicalOutpatientRecord/index.vue:9705).
    private int getChronicDaysCap() {
        try {
            List<Parameter> params = new ArrayList<>();
            params.add(new Parameter("config_name", "=", "chronicDays"));
            List<SysParamConfig> configs =
                    sysParamConfigService.listAll(params, null);
            int cap = 0;
            boolean found = false;
            for (SysParamConfig config : configs) {
                if (config == null
                        || StringUtils.isBlank(config.getConfigValue())) {
                    continue;
                }
                try {
                    int v = Integer.parseInt(
                            config.getConfigValue().trim());
                    if (v < 0) {
                        continue;
                    }
                    if (v > cap) {
                        cap = v;
                    }
                    found = true;
                } catch (NumberFormatException e) {
                    // Dong cau hinh hong -> bo qua, dung fallback neu
                    // khong con dong nao parse duoc.
                }
            }
            return found ? cap : 90;
        } catch (Exception e) {
            return 90;
        }
    }

    // DS149 C: xem ghi chu o save().
    private static void checkRecipelDetailLengths(RecipelDetail entity) {
        if (entity == null) {
            return;
        }
        checkLength(entity.getDrippingSpeed(), 45, "tốc độ truyền Độ dài phải nằm trong khoảng 0 đến 45");
        checkLength(entity.getRetailType(), 1, "retail_type Độ dài phải từ 0 và 1 Giữa");
        checkLength(entity.getUnitType(), 100, "đơn giá Đơn vị Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getStuffType(), 100, "Loại vật liệu Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getRemarks(), 255, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
        checkDictLength(entity.getWesternMedicineUse(), 64, "Cách dùng thuốc Tây Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(entity.getFrequency(), 64, "Tần suất và liều dùng Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictNameLength(entity.getDays(), 64, "Số ngày Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(entity.getChineseMedicineUse(), 64, "Cách dùng thuốc Đông y Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(entity.getInfuseUse(), 64, "Cách dùng truyền dịch Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(entity.getSkinTest(), 64, "Test da Độ dài phải nằm trong khoảng 0 đến 64");
    }

    private static void checkDictLength(com.geeke.sys.entity.DictItem item, int max, String message) {
        if (item != null) {
            checkLength(item.getValue(), max, message);
        }
    }

    // Mapper bind #{days.name} (ten hien thi), khong phai #{days.value}.
    private static void checkDictNameLength(com.geeke.sys.entity.DictItem item, int max, String message) {
        if (item != null) {
            checkLength(item.getName(), max, message);
        }
    }

    private static void checkLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ServiceException(message);
        }
    }

    // min_total la cot NOT NULL duy nhat co the bi drop qua bulk (cac cot
    // NOT NULL khac: id/audit do preInsert lo; infuse_type la int
    // primitive mac dinh 0, khong the null). MedicalRecordService da
    // default minTotal = total truoc moi save noi bo, nen chan o day
    // khong lam vo luong dang chay -- dong nao ve insert voi minTotal
    // null hien da 500 san.
    private void checkRecipelDetailRequired(RecipelDetail entity) {
        if (entity == null) {
            throw new ServiceException("Chi tiết đơn thuốc không được để trống");
        }
        if (entity.getMinTotal() == null) {
            throw new ServiceException("Tổng lượng đơn vị nhỏ nhất không được để trống");
        }
    }

    @Transactional
    public void addRetailrecipelDetails(List<RecipelDetail> recipelDetails, RecipelInfo recipelInfo) {
        for (RecipelDetail recipelDetail : recipelDetails) {
            recipelDetail.setRecipelInfo(recipelInfo);
            if (null != recipelInfo.getCompany()) {
                recipelDetail.setCompany(recipelInfo.getCompany());
            }
            // DS176 A: phuong thuc nay dung super.save nen vong qua
            // checkRecipelDetailLengths (hien chua co caller — guard
            // phong thu cho caller tuong lai).
            checkRecipelDetailLengths(recipelDetail);
            // DS239: cung goi checkNonNegative truoc super.save — neu
            // khong, duong nay bypass guard DS209/DS218/DS239 cua save()
            // (hien chua co caller, phong thu cho caller tuong lai).
            checkNonNegative(recipelDetail);
            // DS299: cung chan actualPayment > allFee o duong nay.
            checkActualPaymentNotExceedAllFee(recipelDetail);
            // DS300: cung chan minTotal < total o duong nay.
            checkMinTotalNotLessThanTotal(recipelDetail);
            checkRecipelDetailStuffTypeValues(recipelDetail);
            checkRecipelDetailInfuseTypeValues(recipelDetail); // DS390
            checkRecipelDetailIsUnpackSellValues(recipelDetail); // DS394
            checkRecipelDetailFrequencyValues(recipelDetail); // DS419
            checkRecipelDetailIsExtraValues(recipelDetail); // DS425
            checkRecipelDetailInfuseUseValues(recipelDetail); // DS433
            checkRecipelDetailDaysValues(recipelDetail); // DS464
            checkRecipelDetailWesternMedicineUseValues(recipelDetail); // DS466
            checkRecipelDetailChineseMedicineUseValues(recipelDetail); // DS467
            checkRecipelDetailRetailTypeValues(recipelDetail); // DS473
            checkRecipelDetailSkinTestValues(recipelDetail); // DS479
            checkRecipelDetailInfuseGroupValues(recipelDetail); // DS536
            DrugStuffEvt drugStuffId = recipelDetail.getDrugStuffId();
            super.save(recipelDetail);
        }

    }

    // Đơn thuốcidLấy chi tiết đơn thuốc
    @Transactional(readOnly = true)
    public List<RecipelDetail> getRecipelDetail(String id) {
        List<RecipelDetail> recipelDetail = recipelDetailDao.getRecipelDetail(id);
        return recipelDetail;
    }

    // Đơn thuốcidLấyhạng mụcTiến độ thực hiện
    @Transactional
    public int schedule(String id) {
        // NếuTrả vềGiá trị là âmsốthìChưahoàn thành, Nếu là 0thìĐãhoàn thành
        int recipelDetail = recipelDetailDao.schedule(id);
        return recipelDetail;
    }

    /** M2 (iteration 60): company cua dong detail theo id, uy thac cho DAO
     * (controller kiem tra so huu truoc khi ghi de executions/schedule);
     * mau X3 getRecipelInfoCompanyId, tra null khi id khong ton tai. */
    public String getRecipelDetailCompanyId(String id) {
        return recipelDetailDao.getCompanyIdById(id);
    }

    //thống kêChưahết hạn của Đãbịchiếm dụng của Thuốc
    public List<RecipelStastics> getDetailStasticsForOccupy(int days)
    {
        return this.dao.getDetailStasticsForOccupy(SessionUtils.getLoginTenantId(),days);
    }

    //thống kêHiện tạiCần của Thuốc
    public List<RecipelStastics> getDetailStasticsForNowByRecordId(String medicalRecordId)
    {
        return this.dao.getDetailStasticsForNowByRecordId(medicalRecordId,SessionUtils.getLoginTenantId());
    }

    public List<RecipelDetail> getByRecipelInfoId(String recipelInfoId)
    {

        return this.dao.getByRecipelInfoId(recipelInfoId);
    }

    /**
     * hàng loạtTruy vấnnhiềuCáiĐơn thuốc của Chi tiết
     * @param recipelInfoIds Đơn thuốcIDDanh sách
     * @return theoĐơn thuốcID phútnhóm của Chi tiếtMap
     */
    public Map<String, List<RecipelDetail>> getByRecipelInfoIds(List<String> recipelInfoIds) {
        if (CollectionUtils.isEmpty(recipelInfoIds)) {
            return Collections.emptyMap();
        }
        List<RecipelDetail> details = this.dao.getByRecipelInfoIds(recipelInfoIds);
        return details.stream().collect(Collectors.groupingBy(
                d -> d.getRecipelInfo().getId()
        ));
    }

    @Transactional(readOnly = false)
    public int batchDelete(List<RecipelDetail> var1){
        return this.dao.batchDelete(var1);
    }

    @Transactional(readOnly = false)
    public void updateById(BigDecimal executions, String id, String stuffType, int infuseType, String cureId){
        // DS111: fail-closed 400 thay vi NPE 500 — (a) id khong ton tai
        // (nhanh thuong khong co guard i>0 nhu nhanh "3"; service save
        // DS109 da chan ghost id truoc khi toi day, day la phong thu
        // them), (b) dong detail luu recipel_info_id NULL (cot nullable,
        // khong @NotNull; service save chi kiem tra info id trong payload,
        // khong kiem tra dong da luu) — ca hai deu NPE o
        // dao.get(id).getRecipelInfo().getId() (:225/:274).
        RecipelDetail target = this.dao.get(id);
        if (target == null || target.getRecipelInfo() == null) {
            throw new ServiceException("Chi tiết đơn thuốc không tồn tại hoặc thiếu đơn thuốc: "
                    + id);
        }
        if("3".equals(stuffType)){
            int i = recipelDetailDao.updateById(executions, id,0);
            if(i>0){
                RecipelDetail recipelDetail = this.dao.get(id);
                List<RecipelDetail> recipelDetails = recipelDetailDao.findByInfoId(recipelDetail.getRecipelInfo().getId(),recipelDetail.getStuffType());
                int count=0;
                if(recipelDetails!=null){
                    for (RecipelDetail recipelDetail1:
                            recipelDetails) {
                        // DS111: total/executions nullable (cot DB nullable;
                        // total khong @NotNull) — dong null coi nhu chua hoan
                        // thanh, khong NPE. GIU NGUYEN ngu nghia hien tai:
                        // Integer.equals(BigDecimal) von luon false nen count
                        // khong doi voi cap non-null; khong doi thanh so
                        // sanh gia tri (do la thay doi hanh vi hoan thanh
                        // don, can quyet dinh lam sang — xem bao cao DS111).
                        if(recipelDetail1.getTotal() != null
                                && recipelDetail1.getExecutions() != null
                                && recipelDetail1.getTotal().equals(recipelDetail1.getExecutions())){
                            count++;
                        }
                    }
                    //NếuTất cả các hạng mục đều hoàn thành của nói, thìSửaĐơn thuốc của cờ
                    if(count==recipelDetails.size()){
                        int i1 = recipelInfoDao.updateById(1,0, recipelDetail.getRecipelInfo().getId());
                        if(i1>0){
                            RecipelInfo recipelInfo = recipelInfoDao.get(recipelDetail.getRecipelInfo().getId());
                            // DS110: registration cua recipel_info co the khong ton tai
                            // (39/51 don live tro toi registration ma khong co dong
                            // tuong ung; vi du 2222745407924404224) -> get() tra null
                            // roi NPE o setTreatmentDate, cuoi bang 500 trong khi
                            // cure row da commit. Chan fail-closed 400 qua
                            // ServiceException (GlobalExceptionHandler -> HTTP 400);
                            // updateById tx rollback nen co + flag deu giu nguyen.
                            if (recipelInfo == null || recipelInfo.getRegistration() == null) {
                                throw new ServiceException("Đơn thuốc không tồn tại hoặc thiếu đơn đăng ký: "
                                        + recipelDetail.getRecipelInfo().getId());
                            }
                            Registration registration = registrationService.get(recipelInfo.getRegistration().getId());
                            if (registration == null) {
                                throw new ServiceException("Đơn đăng ký không tồn tại: "
                                        + recipelInfo.getRegistration().getId());
                            }
                            registration.setTreatmentDate(new Date());
                            registrationService.save(registration);
                            
                            // Cập nhật trạng thái xét nghiệm - kiểm tra của đơn thuốc này thành đã điền
                            List<InspectionCheck> inspectionChecks = inspectionCheckService.getByRecipelInfoId(recipelInfo.getId());
                            if (!CollectionUtils.isEmpty(inspectionChecks)) {
                                for (InspectionCheck inspectionCheck : inspectionChecks) {
                                    // DS318: chi lat sang '1' khi phieu da co dong ket qua that;
                                    // phieu chua co ket qua giu '0' de van nhap duoc ket qua qua UI
                                    // (lat mu se ket phieu: fill() bao "Khong tim thay ket qua").
                                    // Khong nem 400 o day -- day la luong noi bo hoan thanh dieu
                                    // tri (@Transactional), nem se rollback ca lan hoan thanh.
                                    if (inspectionCheckInfoService.getByInspecId(inspectionCheck.getId()) != null) {
                                        inspectionCheck.setStatus("1");
                                        inspectionCheckService.save(inspectionCheck);
                                    }
                                }
                            }
                        }
                    }else {
                        recipelInfoDao.updateById(0,0,recipelDetail.getRecipelInfo().getId());
                    }
                }
            }

        }else {
            RecipelDetail recipelDetail = this.dao.get(id);
            List<RecipelDetail> recipelDetails = recipelDetailDao.findByInfoId(recipelDetail.getRecipelInfo().getId(),recipelDetail.getStuffType());
            for (RecipelDetail recipelDetail1:
            recipelDetails) {
                if(recipelDetail.getInfuseGroup()==recipelDetail1.getInfuseGroup()){
                    if(null==cureId||cureId.isEmpty()){
                        recipelDetailDao.updateById(executions, recipelDetail1.getId(),1);
                    }else {
                        recipelDetailDao.updateById(executions, recipelDetail1.getId(),0);
                    }
                }
            }
            List<RecipelDetail> recipelDetails2 = recipelDetailDao.findByInfoId(recipelDetail.getRecipelInfo().getId(),recipelDetail.getStuffType());
            int count=0;
            if(recipelDetails2!=null){
                for (RecipelDetail recipelDetail1:
                        recipelDetails2) {
                    // DS111: executions nullable (44 dong live NULL; chi
                    // dong cung infuseGroup duoc set o vong tren) va days co
                    // the null — dong null coi nhu chua hoan thanh, khong
                    // NPE. GIU NGUYEN phep so sanh == hien tai (khong doi
                    // thanh equals — thay doi hanh vi hoan thanh don, can
                    // quyet dinh lam sang; xem bao cao DS111).
                    String daysName = recipelDetail1.getDays() == null
                            ? null : recipelDetail1.getDays().getName();
                    String execStr = recipelDetail1.getExecutions() == null
                            ? null : recipelDetail1.getExecutions().toString();
                    if(daysName != null && execStr != null && daysName == execStr){
                        count++;
                    }
                }
                //NếuTất cả các hạng mục đều hoàn thành của nói, thìSửaĐơn thuốc của cờ
                if(count==recipelDetails2.size()){
                    int i1 = recipelInfoDao.updateById(1,1, recipelDetail.getRecipelInfo().getId());
                    if(i1>0){
                        RecipelInfo recipelInfo = recipelInfoDao.get(recipelDetail.getRecipelInfo().getId());
                        // DS110: nhu nhanh "3" o tren — registration khong ton tai
                        // -> 400 thay vi NPE 500 (xem ghi chu o nhanh "3").
                        if (recipelInfo == null || recipelInfo.getRegistration() == null) {
                            throw new ServiceException("Đơn thuốc không tồn tại hoặc thiếu đơn đăng ký: "
                                    + recipelDetail.getRecipelInfo().getId());
                        }
                        Registration registration = registrationService.get(recipelInfo.getRegistration().getId());
                        if (registration == null) {
                            throw new ServiceException("Đơn đăng ký không tồn tại: "
                                    + recipelInfo.getRegistration().getId());
                        }
                        registration.setTreatmentDate(new Date());
                        registrationService.save(registration);
                        
                        // Cập nhật trạng thái xét nghiệm - kiểm tra của đơn thuốc này thành đã điền
                        List<InspectionCheck> inspectionChecks = inspectionCheckService.getByRecipelInfoId(recipelInfo.getId());
                        if (!CollectionUtils.isEmpty(inspectionChecks)) {
                            for (InspectionCheck inspectionCheck : inspectionChecks) {
                                // DS318: nhu nhanh tren -- chi lat khi da co dong ket qua that.
                                if (inspectionCheckInfoService.getByInspecId(inspectionCheck.getId()) != null) {
                                    inspectionCheck.setStatus("1");
                                    inspectionCheckService.save(inspectionCheck);
                                }
                            }
                        }
                    }
                }else {
                    recipelInfoDao.updateById(0,0,recipelDetail.getRecipelInfo().getId());
                }
            }
        }
    }
}