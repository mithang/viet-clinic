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
        // DS149 C: chan do dai truoc moi ghi (mau DS148 A). remarks la
        // field rieng cua entity (Lombok @Data, khong ke thua @Length(255)
        // cua DataEntity) nhung cure.remarks chi varchar(100) (probe C6:
        // remarks 101 -> 500); delType -> varchar(1). Khong co route
        // bulk (mapper khong co bulkInsert/bulkUpdate) nen chi guard
        // save() don. recipelInfoId (String -> bigint) la loi kieu,
        // khong phai loi do dai, ngoai pham vi.
        checkCureManagementLengths(entity);
        return super.save(entity);
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
