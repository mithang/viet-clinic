package com.geeke.cure.service;

import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.cure.dao.CureManagementDao;
import com.geeke.cure.entity.CureManagement;
import com.geeke.outpatient.entity.RecipelDetail;
import com.geeke.outpatient.entity.RecipelInfo;
import com.geeke.outpatient.service.RecipelDetailService;
import com.geeke.outpatient.service.RecipelInfoService;
import com.geeke.utils.StringUtils;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("cureManagementService")
@Transactional(readOnly = true)
public class CureManagementService extends CrudService<CureManagementDao, CureManagement> {

    @Autowired
    private RecipelDetailService recipelDetailService;

    @Autowired
    private RecipelInfoService recipelInfoService;

    // DS109 CM1-CM14: save truoc day de lo — service la subclass
    // CrudService rong (0 ServiceException), controller tu viet save.
    // (a) id khong ton tai roi qua CrudService.save -> doUpdate 0 dong
    // roi van 200 silent no-op (mau DS94/DS99), sau do controller van
    // chay recipel side effect (updateById/schedule) tren cure row
    // khong ton tai (CM1/CM11/CM14: 200 hoac 500 NPE che lap);
    // (b) detail/info id ma khong ton tai -> insert cure row "ma" xong
    // moi NPE o updateById (CM4/CM5: 500 + phantom cure row; CM9/CM14:
    // 200, nhanh stuffType "3" bo qua updateById 0 dong);
    // (c) thieu executeDate/company -> 500 NOT NULL (CM6/CM7);
    // (d) detail.executions null + stuffType "3" -> updateById set
    // executions=NULL tren cot NOT NULL sau insert (CM13: 500).
    // Chan het truoc moi ghi: ton tai + required -> 400 ro nghia qua
    // ServiceException (GlobalExceptionHandler -> HTTP 400).
    // @Transactional (readOnly = false) tuong minh vi class-level la
    // readOnly = true (mau DS105/DS106/DS107/DS108).
    // CHI caller cua service nay la CureManagementController (da grep
    // xac minh); khong co caller noi bo nao khac bi anh huong.
    // CO TINH KHONG sua hanh vi downstream: updateById van NPE khi
    // registration cua recipel_info khong ton tai (lo downstream ngoai
    // pham vi DS109, ung vien DS110) — CM10/CM12 chi assert hieu ung
    // DB, khong assert ma tra ve.
    @Override
    @Transactional(readOnly = false)
    public CureManagement save(CureManagement entity) {
        if (entity == null) {
            throw new ServiceException("Thông tin điều trị không được để trống");
        }
        if (StringUtils.isNotBlank(entity.getId()) && dao.get(entity.getId()) == null) {
            throw new ServiceException("Thông tin điều trị không tồn tại: " + entity.getId());
        }
        if (entity.getCompany() == null || StringUtils.isBlank(entity.getCompany().getId())) {
            throw new ServiceException("Đơn vị không được để trống");
        }
        if (entity.getExecuteDate() == null) {
            throw new ServiceException("Ngày thực hiện không được để trống");
        }
        RecipelDetail detail = entity.getRecipelDetail();
        if (detail == null || StringUtils.isBlank(detail.getId())) {
            throw new ServiceException("Chi tiết đơn thuốc không được để trống");
        }
        if (detail.getExecutions() == null) {
            throw new ServiceException("Tiến độ thực hiện không được để trống");
        }
        // DS218: detail.executions am — duong viet con lai cua ho "so am".
        // Controller sau save() goi recipelDetailService.updateById(
        // detail.executions, ...) ghi thang vao recipel_detail.executions
        // (mapper set executions=#{executions}); DS209 chi chan
        // save/bulkInsert/bulkUpdate cua RecipelDetail, khong chan
        // updateById, va checkNonNegative cung khong gom executions.
        // Chan o day (truoc moi ghi, ca create lan update, ca 2 nhanh
        // stuffType) de khong de lai cure row ma khi bi chan sau do.
        // Mien tru thu hoi (delType='1') nhu DS217: frontend revocation()
        // copy executions da tru addNumber (co the am voi du lieu cu) vao
        // recipelDetail.executions roi save (cureManagement index.vue:922,
        // infusion:1329) — chan tuyet doi se vo thu hoi 35 dong seed cu.
        if (detail.getExecutions().compareTo(BigDecimal.ZERO) < 0
                && !"1".equals(entity.getDelType())) {
            throw new ServiceException("Tiến độ thực hiện không được âm");
        }
        RecipelInfo info = detail.getRecipelInfo();
        if (info == null || StringUtils.isBlank(info.getId())) {
            throw new ServiceException("Đơn thuốc không được để trống");
        }
        if (recipelDetailService.get(detail.getId()) == null) {
            throw new ServiceException("Chi tiết đơn thuốc không tồn tại: " + detail.getId());
        }
        if (recipelInfoService.get(info.getId()) == null) {
            throw new ServiceException("Đơn thuốc không tồn tại: " + info.getId());
        }
        // DS150 B: recipelInfoId la String nhung cot cure.recipel_info_id
        // la bigint (nullable): "abc" hoac "" qua mapper deu 500 o DB
        // (error 1366, probe B1/B2 pre). Chan truoc moi ghi: chi cho
        // null hoac so nguyen vua long (parse that bai -> 400 ro nghia);
        // "" cung 400 (khong de MySQL tu ep). Ap cho ca create lan
        // update vi ca hai deu qua save() nay.
        String recipelInfoId = entity.getRecipelInfoId();
        if (recipelInfoId != null && StringUtils.isNotBlank(recipelInfoId)) {
            try {
                Long.parseLong(recipelInfoId);
            } catch (NumberFormatException e) {
                throw new ServiceException("Mã đơn thuốc không hợp lệ: " + recipelInfoId);
            }
        } else if (recipelInfoId != null) {
            throw new ServiceException("Mã đơn thuốc không được để trống");
        }
        // DS149 C: chan do dai truoc moi ghi (mau DS148 A). remarks la
        // field rieng cua entity (Lombok @Data, khong ke thua @Length(255)
        // cua DataEntity) nhung cure.remarks chi varchar(100) (probe C6:
        // remarks 101 -> 500); delType -> varchar(1). Khong co route
        // bulk (mapper khong co bulkInsert/bulkUpdate) nen chi guard
        // save() don. recipelInfoId (String -> bigint) la loi kieu,
        // khong phai loi do dai, ngoai pham vi.
        checkCureManagementLengths(entity);
        // DS217: addNumber / executions am — cung ho "so am" DS209-DS216.
        // addNumber = so lan thuc hien them trong lan nay (frontend gui
        // addNumber = excuteNum, executions = cumulative + excuteNum);
        // revocation() lam executions -= addNumber va infusion tinh
        // executions con lai = tong addNumber - tong da thu hoi -
        // addNumber, nen so am luu duoc se lam TANG so lan thuc hien thay
        // vi giam. Ca hai cot deu signed (add_number int NULL,
        // executions int NOT NULL), khong @Min/@Positive, mapper ghi
        // thang #{addNumber} / #{executions}. Chan truoc moi ghi: 0 van
        // cho phep (lan thuc hien 0 lan / chua thuc hien), chi am moi 400.
        // NGOAI LE: thu hoi (delType='1') duoc mien khoi kiem tra
        // executions — xem ghi chu trong checkCureManagementNonNegative.
        checkCureManagementNonNegative(entity);
        return super.save(entity);
    }

    // DS217: xem ghi chu o save().
    // Thu hoi (delType='1') duoc MIEN khoi kiem tra executions: frontend
    // tinh executions -= addNumber khi thu hoi, va 35 dong seed live co
    // executions=0 < addNumber (09-11/2023) nen thu hoi mot dong cu se ra
    // am — viec huy ban ghi khong duoc phep bi chan boi du lieu cu (10
    // dong seed da huy san co executions=-1, addNumber duong, chung minh
    // duong nay da ton tai). executions cua dong da huy la vestigial
    // (cac tong chi cong addNumber, khong cong executions), va khong co
    // dong nao co add_number am nen addNumber van chan tuyet doi ke ca
    // khi thu hoi.
    private static void checkCureManagementNonNegative(CureManagement entity) {
        if (entity == null) {
            return;
        }
        if (entity.getAddNumber() < 0) {
            throw new ServiceException("Số lần thực hiện thêm không được âm");
        }
        if (entity.getExecutions() < 0 && !"1".equals(entity.getDelType())) {
            throw new ServiceException("Số lần đã thực hiện không được âm");
        }
    }

    // DS149 C: xem ghi chu o save().
    private static void checkCureManagementLengths(CureManagement entity) {
        if (entity == null) {
            return;
        }
        checkLength(entity.getRemarks(), 100, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getDelType(), 1, "Loại xóa Độ dài phải nằm trong khoảng 0 đến 1");
    }

    private static void checkLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ServiceException(message);
        }
    }

}
