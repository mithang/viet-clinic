package com.geeke.outpatient.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.outpatient.entity.*;
import com.geeke.stock.service.MedicinalStorageControlService;
import com.geeke.sys.entity.DictItem;
import com.geeke.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.geeke.outpatient.dao.RecipelInfoDao;

import com.google.common.collect.Maps;

/**
 * Thông tin đơn thuốcService
 * @author txl
 * @version 2022-06-15
 */
 
@Service("recipelInfoService")
@Transactional(readOnly = true)
public class RecipelInfoService extends CrudService<RecipelInfoDao, RecipelInfo>{
    @Lazy
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private RecipelInfoDao recipelInfoDao;

    @Lazy
    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private MedicinalStorageControlService medicinalStorageControlService;

    /**
     * DS115 A: RecipelInfoService khong co save/bulk override nao nen
     * ke thua CrudService dang zero-exception: save id khong ton tai
     * tra 200 silent no-op (probe A4, lop DS94/DS99); bulkInsert/
     * bulkUpdate khong qua @Valid nen thieu seq/isFollowUp se 500 o
     * cot NOT NULL (probe A9/A10) va thieu smallType thi ghi lang
     * dong NULL (probe A11, small_type nullable trong DB); [null]
     * NPE 500 (probe A8/A15). Chi kiem tra TON TAI + truong bat
     * buoc, khong kiem tra so huu: san 403 theo tenant nam o
     * RecipelInfoController/CrudService (verifyTenantOwnership o
     * super.save/bulk*) chay sau validate va giu nguyen. Delete la
     * XOA MEM (del_flag) nen bulkDelete id khong ton tai giu 200
     * rows 0 IDEMPOTENT (DS99 G11, probe A22). Luu y: lop nay co
     * @Transactional(readOnly = true) o cap class nen moi override
     * ghi phai mang @Transactional(readOnly = false) rieng (nhu
     * claimDispensed/...), neu khong ghi se bi chan o che do chi doc.
     * Mapper: bulkUpdate dung #{entity.smallType} (DictItem, khong
     * co MyBatis type handler) nen MOI bulkUpdate deu 500 -- sua
     * thanh #{entity.smallType.value} nhu insert/update/bulkInsert
     * (RecipelInfoDao.xml L473, probe A19).
     * DS115 fix-forward (K3 hoi quy): save() DON KHONG doi smallType.
     * Cot small_type NULLABLE trong DB va luong noi bo that allSave
     * (FinishVisit, MedicalRecordService) khong gui smallType theo
     * thiet ke (ghi chu K3 o do: khong bia gia tri mac dinh vi du
     * lieu hien huu khong thong nhat); bat buoc no o service lam
     * allSave 400 (K3 allsave_finishvisit_payload_100). Duong don
     * le expose van 400 nho @Valid o CrudController.save (@NotNull
     * tren entity.smallType) nen khong mat fail-closed ngoai. Chi
     * bulkInsert/bulkUpdate (khong qua @Valid) moi giu kiem tra
     * smallType (probe A11).
     */
    private void validateRequired(RecipelInfo entity) {
        if (entity == null) {
            throw new ServiceException("Đơn thuốc không được để trống");
        }
        if (entity.getSeq() == null) {
            throw new ServiceException("Sắp xếp đơn thuốc không được để trống");
        }
        if (entity.getIsFollowUp() == null) {
            throw new ServiceException("Tái khám đơn thuốc không được để trống");
        }
    }

    private void validateSmallType(RecipelInfo entity) {
        if (entity.getSmallType() == null
                || StringUtils.isBlank(entity.getSmallType().getValue())) {
            throw new ServiceException("Loại nhỏ đơn thuốc không được để trống");
        }
    }

    // DS240: fee / amountReceivedTotal am truoc day ghi thang (probe
    // pre 16/16: save fee=-5 / amount=-10 / ca hai -1/-5, bulkInsert
    // fee=-2, bulkUpdate fee 50->-3, save-update amount 20->-4 deu
    // 200 + persist; control 0/50/20 van 200). Entity khong @Min,
    // khong CHECK o DB; service chi kiem tra seq/isFollowUp/smallType
    // hien dien + ghost id (DS115). Mapper ghi ca 2 cot tren insert/
    // update/bulkInsert/bulkUpdate (RecipelInfoDao.xml :151-152/:196,
    // :242-243, :298-299/:345, CASE :421-426); update/bulkUpdate ghi
    // khong dieu kien nen probe update phai gui full payload.
    // Khong nhat quan: TollInfoService.saveToll DA chan phi don am o
    // luc tinh phi (DS224 :665-668 "Phi don thuoc khong duoc am") —
    // don phi am tao duoc qua save truc tiep roi khong bao gio thu
    // duoc phi. Fee chay len phieu in (DataBean :169/:549) va thanh
    // amountReceivable (= fee) luc thu; amountReceivedTotal chi co
    // TollInfoService:704 ghi (tu amountReceived da guard DS224) nen
    // la truong he thong, save truc tiep gia mao duoc se lam sai doi
    // soat thu phi. Chan fail-closed 400 truoc moi ghi; null giu
    // NULL (7 dong live fee NULL), 0 giu nguyen (quy uoc DS219).
    // status/chargeStatus/dispensionStatus dung -1 hop le (huy/hoan/
    // tra) nen KHONG guard. Caller noi bo chi save lai entity da load
    // (gia tri da luu, live 0 dong am) hoac set amountReceivedTotal
    // tu so tien da guard nen khong vo luong dang chay.
    private static void checkNonNegative(RecipelInfo entity) {
        if (entity == null) {
            return;
        }
        if (entity.getFee() != null
                && entity.getFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Tổng giá đơn thuốc không được âm");
        }
        if (entity.getAmountReceivedTotal() != null
                && entity.getAmountReceivedTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Thực thu tổng cộng không được âm");
        }
    }

    // DS276: ngay tai kham tren don thuoc khong duoc la ngay qua khu —
    // ngay tai kham la ngay hen kham lai trong tuong lai theo dinh
    // nghia (truong followUpDate, RecipelInfo.java:49 "tai kham thoi
    // gian"); don co ngay tai kham da qua khong bao gio duoc diem danh
    // nen chi la du lieu chet. So sanh theo ngay trong vung gio phong
    // kham GMT+7 (DB_ZONE, nhu DS264/DS265/DS266/DS269/DS270/DS271/
    // DS272/DS273/DS274/DS275): hom nay va tuong lai giu nguyen, null
    // giu nguyen (cot nullable, don khong hen tai kham). Khong co
    // duong goi noi bo nao set followUpDate (grep chi thay
    // getter/setter cua entity; khong Vue nao dung truong nay) nen
    // guard khong doi ngu nghia luong hop le. Khac DS275
    // Registration.followUpDate (luot kham, bang khac).
    private static final ZoneId DB_ZONE = ZoneId.of("GMT+7");

    private static void checkFollowUpDateNotPast(RecipelInfo entity) {
        if (entity == null || entity.getFollowUpDate() == null) {
            return;
        }
        if (entity.getFollowUpDate().toInstant().atZone(DB_ZONE).toLocalDate()
                .isBefore(LocalDate.now(DB_ZONE))) {
            throw new ServiceException("Ngày tái khám trên đơn thuốc không được là ngày quá khứ.");
        }
    }

    // DS281: 6 truong event-date tren don thuoc khong duoc la ngay
    // tuong lai — su kien da xay ra (thanh toan / phat thuoc / hoan phi
    // / tra thuoc / huy) khong the co ngay sau hom nay. Entity khong
    // @Past, DB khong CHECK; service chi guard phi am (DS240), hien
    // dien/ghost (DS115) va followUpDate qua khu (DS276), khong nhin
    // 6 truong nay. Mapper ghi ca 6 cot tren insert / update /
    // bulkInsert / bulkUpdate (RecipelInfoDao.xml :155-179 / :246-268
    // / :302-326 / CASE :441-541). So sanh theo ngay trong vung gio
    // phong kham GMT+7 (DB_ZONE, nhu DS276): sau hom nay -> 400; hom
    // nay / qua khu / null giu nguyen (cot nullable, su kien chua
    // xay ra). Setter noi bo deu new Date() (TollInfoService:606/712
    // payDate, :923 retreatDate; SupplierStockService:166
    // dispensionDate, :215 retreatChargeDate) nen guard khong pha
    // luong hop le; invalidDate / retreatDispensionDate khong co
    // setter noi bo nao. Khac DS276 followUpDate (ngay hen tuong lai,
    // guard chieu nguoc), DS279/DS280 Registration event dates (bang
    // khac), DS268 diagnoseDate tuong lai (BLOCKED, khong cham).
    private static void checkRecipelInfoEventDatesNotFuture(
            RecipelInfo entity) {
        if (entity == null) {
            return;
        }
        LocalDate today = LocalDate.now(DB_ZONE);
        checkEventDateNotFuture(entity.getPayDate(), today,
                "Thời gian thanh toán trên đơn thuốc không được là ngày tương lai.");
        checkEventDateNotFuture(entity.getDispensionDate(), today,
                "Thời gian phát thuốc trên đơn thuốc không được là ngày tương lai.");
        checkEventDateNotFuture(entity.getRetreatChargeDate(), today,
                "Thời gian hoàn phí trên đơn thuốc không được là ngày tương lai.");
        checkEventDateNotFuture(entity.getRetreatDispensionDate(), today,
                "Thời gian trả thuốc trên đơn thuốc không được là ngày tương lai.");
        checkEventDateNotFuture(entity.getInvalidDate(), today,
                "Thời gian hủy đơn thuốc không được là ngày tương lai.");
        checkEventDateNotFuture(entity.getRetreatDate(), today,
                "Thời gian hoàn phí trên đơn thuốc không được là ngày tương lai.");
    }

    private static void checkEventDateNotFuture(Date date, LocalDate today,
            String message) {
        if (date != null
                && date.toInstant().atZone(DB_ZONE).toLocalDate()
                        .isAfter(today)) {
            throw new ServiceException(message);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public RecipelInfo save(RecipelInfo entity) {
        validateRequired(entity);
        checkNonNegative(entity);
        checkFollowUpDateNotPast(entity);
        checkRecipelInfoEventDatesNotFuture(entity);
        if (StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Đơn thuốc không tồn tại: "
                    + entity.getId());
        }
        return super.save(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<RecipelInfo> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách đơn thuốc thêm mới không được để trống");
        }
        for (RecipelInfo entity : entitys) {
            validateRequired(entity);
            validateSmallType(entity);
            checkNonNegative(entity);
            checkFollowUpDateNotPast(entity);
            checkRecipelInfoEventDatesNotFuture(entity);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<RecipelInfo> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách đơn thuốc cập nhật không được để trống");
        }
        for (RecipelInfo entity : entitys) {
            validateRequired(entity);
            validateSmallType(entity);
            checkNonNegative(entity);
            checkFollowUpDateNotPast(entity);
            checkRecipelInfoEventDatesNotFuture(entity);
            if (StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của đơn thuốc đã tồn tại");
            }
            if (dao.get(entity.getId()) == null) {
                throw new ServiceException("Đơn thuốc không tồn tại: "
                        + entity.getId());
            }
        }
        return super.bulkUpdate(entitys);
    }

    /**
     * DS124 B: single delete fail-closed 400. CrudService.delete chi chan
     * qua stored-row tenant check ma check do bo qua stored==null, nen
     * delete id ma tra 200 data=0 im lang; mapper delete la UPDATE
     * del_flag khong loc del_flag nen delete dong da xoa mem tra 200
     * rows=1 + audit DELETED trung. Kiem tra ton tai len dau qua dao.get
     * (mapper get khong loc del_flag nen phai kiem tra del_flag='0').
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(RecipelInfo entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được đơn thuốc cần xóa.");
        }
        RecipelInfo stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Đơn thuốc không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<RecipelInfo> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách đơn thuốc xóa không được để trống");
        }
        for (RecipelInfo entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Đơn thuốc xóa không được để trống");
            }
        }
        return super.bulkDelete(entitys);
    }

    /**
     * DS52: atomic claim for dispensing. Sets is_dispension='1' only when it
     * is still '0'; returns the rows affected (0 = already dispensed or
     * missing). Exactly one concurrent dispense wins the claim, so a
     * parallel double dispense cannot move stock twice. The caller still
     * saves dispensionDate/dispensionStatus afterwards as before.
     */
    @Transactional(readOnly = false)
    public int claimDispensed(String id) {
        return this.recipelInfoDao.claimDispensed(id);
    }

    /**
     * DS53: atomic claim for charging. Sets is_pay='1' only when the
     * recipel is still unpaid (is_pay '0' or NULL); returns the rows
     * affected (0 = already paid/refunded or missing). Exactly one
     * concurrent charge wins the claim, so a parallel double charge cannot
     * write a second TollInfo or deduct stock twice. The caller still
     * sets isPay/payDate/chargeStatus afterwards as before.
     */
    @Transactional(readOnly = false)
    public int claimPaid(String id) {
        return this.recipelInfoDao.claimPaid(id);
    }

    /**
     * DS54: atomic claim for refunding. Sets is_pay='2' only when the
     * recipel is still PAID (is_pay '1'); returns the rows affected
     * (0 = already refunded / never paid / missing). Exactly one
     * concurrent refund wins the claim, so a parallel double refund
     * cannot write a second refund TollInfo or REFUND audit row, or
     * run materialRefund twice. The caller still sets isPay/
     * chargeStatus/retreatDate afterwards as before.
     */
    @Transactional(readOnly = false)
    public int claimRefunded(String id) {
        return this.recipelInfoDao.claimRefunded(id);
    }

    /**
     * DS57: atomic claim for returning dispensed drugs. Sets
     * is_dispension='2' only when it is still '1'; returns the rows
     * affected (0 = already returned / never dispensed / missing).
     * Exactly one concurrent return wins the claim, so a parallel
     * double return cannot run drugRepercussion twice and drift
     * used/return_todo_fee. The caller still sets
     * dispensionStatus/retreatChargeDate afterwards as before.
     */
    @Transactional(readOnly = false)
    public int claimReturned(String id) {
        return this.recipelInfoDao.claimReturned(id);
    }

    @Transactional(readOnly = false)
    public int claimInvalid(String id) {
        return this.recipelInfoDao.claimInvalid(id);
    }

    @Transactional(readOnly = false)
    public int invalid(String id) {
        // DS58: atomic claim for invalidating an unpaid prescription. Sets
        // status=-1 only when the recipel is still ACTIVE (status 1) and
        // UNCHARGED (charge_status 0); returns the rows affected (0 =
        // already invalid / charged / missing). Exactly one concurrent
        // invalid wins the claim, so a parallel double invalid cannot run
        // cancelOccupy twice and release the same occupy N times (probe
        // pre: par x8 all returned 1, occupy -35, surplus 10035). The
        // set-status + save below is kept as before (same value the claim
        // already wrote, like DS52/DS57).
        if (this.claimInvalid(id) == 0) {
            return 0;
        }
        RecipelInfo recipelInfo = super.get(id);
        if (Objects.nonNull(recipelInfo))
        {
            recipelInfo.setStatus(-1);
            super.save(recipelInfo);

            //độngTồn khogiải phóngthao tác
            this.medicinalStorageControlService.cancelOccupy(recipelInfo);

            // DS167 B: don thuoc khong co dang ky (registration_id NULL)
            // thi getRegistration() null -> getId() NPE 500 o
            // getByMedicalId duoi day (probe: log RecipelInfoService:259).
            // Don mo coi van huy duoc (claim + save + giai phong occupy o
            // tren); chi bo qua buoc cap nhat trang thai don cua dang ky
            // vi khong co dang ky de cap nhat.
            if (recipelInfo.getRegistration() == null
                    || StringUtils.isBlank(recipelInfo.getRegistration().getId())) {
                return 1;
            }

            //Theoca bệnhidLấytất cả của Đơn thuốcThông tin
            List<RecipelInfo> byMedicalId = recipelInfoDao.getByMedicalId(recipelInfo.getRegistration().getId());
            int recipelInfoLength=byMedicalId.size();
            int count=0;
            for (RecipelInfo recipelInfos : byMedicalId  ) {
                // DS168: status NULL-able (29 live rows) -> unbox ==-1
                // NPE 500 khi don cung dang ky co status NULL; null-safe.
                if(Integer.valueOf(-1).equals(recipelInfos.getStatus())){
                    count++;
                }
            }

            //tất cảĐơn thuốcđềuhủysau, Sẽ đăng kýThông tin của cũngcờ
            if(count==recipelInfoLength){
                    //Theoca bệnhThông tinLấyđăng kýThông tin
                    //MedicalRecord medicalRecord = medicalRecordService.get(recipelInfo.getRegistration().getId());
                    //Theoca bệnhThông tinLấyđăng kýid, Đặtđăng ký của Đơn thuốcTrạng thái
                    registrationService.updateRecipeStatus(recipelInfo.getRegistration().getId());
                }
                return 1;
        }

        return 0;
    }

    public List<RecipelInfoDTO> getHistoryRecipel(PageRequest pageRequest) {
        List<RecipelInfoDTO> recipelInfos=recipelInfoDao.getHistoryRecipel(pageRequest);
        return recipelInfos;
    }

    @Transactional(readOnly=false)
    public void updateSchedule(String id, int cureType){
        recipelInfoDao.updateSchedule(id,cureType);
    }

    /** X3: company của đơn thuốc theo id, ủy thác cho DAO (controller kiểm tra sở hữu trước khi gọi service ghi/đọc). */
    public String getRecipelInfoCompanyId(String id) {
        return recipelInfoDao.getRecipelInfoCompanyId(id);
    }

    @Transactional(readOnly = false)
    public int updateNotShowById(String id) {
        RecipelInfo recipelInfo = this.get(id);
        if (ObjectUtil.isNull(recipelInfo)) {
            return 0;
        }
        recipelInfo.setNotShow(0);
        return this.doUpdate(recipelInfo);
    }
}