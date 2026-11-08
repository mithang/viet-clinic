package com.geeke.org.service;

import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.org.dao.ClinicOfficeDao;
import com.geeke.org.entity.ClinicOffice;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Thông tin khoaService
 * @author ch
 * @version 2022-06-14
 */

@Service("clinicOfficeService")
@Transactional(readOnly = true)
public class ClinicOfficeService extends CrudService<ClinicOfficeDao, ClinicOffice>{
    @Autowired
    private ClinicOfficeDao clinicOfficeDao;

    @Transactional(readOnly = false)
    public void updateDefault(String companyId) {
        clinicOfficeDao.updateDefault(companyId);
    }

    // DS125 C: single delete truoc day de lo: ghost id / dong da xoa mem
    // roi van 200 silent no-op (verifyTenantOwnershipAgainstStoredRow bo
    // qua khi stored==null, cung mau DS123/DS124/DS125A; mapper delete
    // khong loc del_flag). Chan o service de 400 ro nghia truoc khi xoa.
    // Dong la (ITER35 M2) da 403 nho stored-row check; ghost la khoang
    // trong con lai. Khong caller noi bo nao goi delete() nay; UI xoa
    // hang live van 200 nhu cu.
    @Override
    @Transactional(readOnly = false)
    public int delete(ClinicOffice entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được khoa cần xóa.");
        }
        ClinicOffice stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Khoa không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
    }

    @Transactional(readOnly = false)
    public ClinicOffice save(ClinicOffice clinicOffice){
       if(StringUtils.isBlank(clinicOffice.getId())){
           //tiến hành dòngmã sốKiểm tra
           List<ClinicOffice> clinicOffices = this.dao.getByCode(clinicOffice);
           if(!CollectionUtils.isEmpty(clinicOffices)){
               throw new ServiceException("Đã tồn tại giống hệt mã số Khoa, Vui lòng nhập lại");
           }

           //tiến hành dòngTên khoaKiểm tra
           List<ClinicOffice> clinicOfficeList = this.dao.getByName(clinicOffice);
           if(!CollectionUtils.isEmpty(clinicOfficeList)){
               throw new ServiceException("Đã tồn tại giống hệt Tên Khoa, Vui lòng nhập lại");
           }
       }
       // DS100: CrudService.save rẽ nhánh update khi id khác rỗng,
       // doUpdate không động đến dòng nào nếu id không tồn tại mà vẫn
       // trả 200 (probe S1; cùng họ silent no-op DS94/DS99). Chỉ chấp
       // nhận update khi dòng đã tồn tại; tạo mới phải gửi id rỗng.
       if (StringUtils.isNotBlank(clinicOffice.getId()) && super.get(clinicOffice.getId()) == null) {
           throw new ServiceException("Khoa không tồn tại: " + clinicOffice.getId());
       }
       // DS101: single-row insert ghi #{sort} trực tiếp; sort NOT NULL
       // (default 0 của DB chỉ áp dụng khi bỏ cột, không áp cho NULL
       // tường minh), entity chỉ có @Length nên @Valid cho null qua rồi
       // 500 ở DB (probe T1). Chỉ yêu cầu ở nhánh tạo mới: mapper
       // update không bao giờ ghi sort nên cập nhật thiếu sort vẫn 200
       // (probe T4); bulkUpdate cũng không ghi sort (probe T5).
       if (StringUtils.isBlank(clinicOffice.getId()) && StringUtils.isBlank(clinicOffice.getSort())) {
           throw new ServiceException("Sắp xếp không được để trống");
       }
        ClinicOffice save = super.save(clinicOffice);
        return clinicOffice;
    }

    /**
     * DS100: bulkInsert/bulkUpdate kế thừa từ CrudService đi thẳng qua
     * dao, bỏ qua các guard @NotNull của save() (code/name/company/
     * isLocked đều NOT NULL ở clinic_office; mapper bulkInsert ghi
     * trực tiếp, bulkUpdate ghi CASE không điều kiện) nên hàng thiếu
     * trường 500 ở DB (probe S7/S9), và bulkUpdate id không tồn tại
     * trả 200 im lặng dù mapper không động đến dòng nào (probe S2;
     * tiền lệ DS99 chỉ sửa GenTable). Ghi đè ở đây để fail-closed 400
     * trước mọi ghi: danh sách null/rỗng, trường bắt buộc, id rỗng
     * hoặc không tồn tại.
     */
    private void validateRequired(ClinicOffice clinicOffice) {
        if (clinicOffice == null) {
            throw new ServiceException("Khoa không được để trống");
        }
        if (clinicOffice.getCompany() == null || StringUtils.isBlank(clinicOffice.getCompany().getId())) {
            throw new ServiceException("Phòng khám của khoa không được để trống");
        }
        if (StringUtils.isBlank(clinicOffice.getCode())) {
            throw new ServiceException("Mã khoa không được để trống");
        }
        if (StringUtils.isBlank(clinicOffice.getName())) {
            throw new ServiceException("Tên khoa không được để trống");
        }
        if (StringUtils.isBlank(clinicOffice.getIsLocked())) {
            throw new ServiceException("Vô hiệu hóa không được để trống");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<ClinicOffice> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách khoa thêm mới không được để trống");
        }
        for (ClinicOffice clinicOffice : entitys) {
            validateRequired(clinicOffice);
            // DS101: bulkInsert ghi #{entity.sort} trực tiếp; sort NOT NULL
            // nên hàng thiếu sort 500 ở DB (probe T2/T3). Chỉ yêu cầu ở
            // bulkInsert: bulkUpdate không có CASE sort nên không yêu cầu.
            if (StringUtils.isBlank(clinicOffice.getSort())) {
                throw new ServiceException("Sắp xếp không được để trống");
            }
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<ClinicOffice> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách khoa cập nhật không được để trống");
        }
        for (ClinicOffice clinicOffice : entitys) {
            validateRequired(clinicOffice);
            if (StringUtils.isBlank(clinicOffice.getId())) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của khoa đã tồn tại");
            }
            if (super.get(clinicOffice.getId()) == null) {
                throw new ServiceException("Khoa không tồn tại: " + clinicOffice.getId());
            }
        }
        return super.bulkUpdate(entitys);
    }
}