package com.geeke.outpatient.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.cure.entity.InspectionCheck;
import com.geeke.cure.service.InspectionCheckService;
import com.geeke.outpatient.dao.RecipelDetailDao;
import com.geeke.outpatient.dao.RecipelInfoDao;
import com.geeke.outpatient.entity.*;
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
        // DS149 C: chan do dai truoc moi ghi (mau DS148 A). bulk* bypass
        // @Valid nen truong co @Length van 500 o mapper khi qua bulk
        // (drippingSpeed 45, retailType 1, unitType 100); gia tri
        // DictItem westernMedicineUse/frequency/chineseMedicineUse/
        // infuseUse/skinTest .value -> varchar(64), days .name ->
        // varchar(64) (mapper bind #{days.name}, khong phai .value);
        // truong KHONG co @Length: stuffType -> varchar(100), remarks
        // -> varchar(255). Gioi han theo cot DB; null-safe.
        checkRecipelDetailLengths(entity);
        return super.save(entity);
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
            checkRecipelDetailLengths(entity);
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
            checkRecipelDetailLengths(entity);
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
                                    inspectionCheck.setStatus("1");
                                    inspectionCheckService.save(inspectionCheck);
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
                                inspectionCheck.setStatus("1");
                                inspectionCheckService.save(inspectionCheck);
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