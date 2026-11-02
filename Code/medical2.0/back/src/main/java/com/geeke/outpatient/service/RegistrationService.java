package com.geeke.outpatient.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.medicareutils.service.MdRegistrationService;
import com.geeke.outpatient.dao.MedicalRecordDao;
import com.geeke.outpatient.dao.RecipelInfoDao;
import com.geeke.outpatient.dao.RegistrationDao;
import com.geeke.outpatient.entity.*;
import com.geeke.sys.entity.Action;
import com.geeke.utils.IdGen;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Thông tin đăng ký khámService
 * @author txl
 * @version 2022-06-15
 */
 
@Service("registrationService")
@Transactional(readOnly = true)
public class RegistrationService extends CrudService<RegistrationDao, Registration>{
    private final RegistrationDao registrationDao;

    private final MedicalRecordDao medicalRecordDao;

    private final PatientService patientService;

    private final MedicalRecordService medicalRecordService;

    //Đăng ký khám BHYT
    private final MdRegistrationService mdRegistrationService;

    public RegistrationService(RegistrationDao registrationDao, MedicalRecordDao medicalRecordDao,
            PatientService patientService, @Lazy MedicalRecordService medicalRecordService,
            MdRegistrationService mdRegistrationService) {
        this.registrationDao = registrationDao;
        this.medicalRecordDao = medicalRecordDao;
        this.patientService = patientService;
        this.medicalRecordService = medicalRecordService;
        this.mdRegistrationService = mdRegistrationService;
    }

    /**
     * H4: gán số thứ tự chờ khám trong ngày cho mỗi lượt đăng ký mới.
     * Số đếm theo (phòng khám, bác sĩ, ngày phòng khám Asia/Ho_Chi_Minh),
     * bắt đầu từ 1 mỗi ngày nhờ khóa chính của queue_counter theo queue_date;
     * cửa sổ ngày [00:00, 00:00 hôm sau) khớp quy ước GMT+7 của G6/H5/H1
     * (create_date lưu theo giờ tường GMT+7). Lượt đăng ký không có bác sĩ
     * hoặc không xác định được tenant thì không gán số (giữ NULL, không bịa).
     * Chỉ gán khi tạo mới (id rỗng) và client chưa gửi số: cập nhật sau đó
     * không đánh lại số, tránh xáo trộn thứ tự gọi khám.
     */
    @Override
    @Transactional(readOnly = false)
    public Registration save(Registration entity) {
        if (StringUtils.isBlank(entity.getId()) && entity.getQueueNumber() == null) {
            assignQueueNumber(entity);
        }
        return super.save(entity);
    }

    private void assignQueueNumber(Registration entity) {
        String companyId = entity.getCompany() != null ? entity.getCompany().getId() : null;
        if (StringUtils.isBlank(companyId)) {
            companyId = SessionUtils.getLoginTenantId();
        }
        String doctorId = entity.getDoctor() != null ? entity.getDoctor().getId() : null;
        if (StringUtils.isBlank(companyId) || StringUtils.isBlank(doctorId)) {
            return;
        }
        String queueDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString();
        registrationDao.advanceQueueCounter(companyId, doctorId, queueDate);
        Integer number = registrationDao.getQueueCounter(companyId, doctorId, queueDate);
        if (number != null) {
            entity.setQueueNumber(number);
        }
    }

    /**
     * N2: gán bác sĩ sau (đường điểm danh /status) cũng cấp số thứ tự.
     * Booking đã xác nhận được tạo không có bác sĩ nên confirm không gán
     * số (đúng H4); khi nhân viên gán bác sĩ qua đường này mà lượt vẫn
     * chưa có số thì cấp số theo cùng counter (phòng khám, bác sĩ, ngày
     * phòng khám hiện tại). Ngày hiện tại là đúng vì điểm danh ở frontend
     * đã chặn lượt có subscribeDate khác hôm nay ("Chưa đến giờ hẹn").
     * Idempotent: lượt đã có số thì giữ nguyên, không đánh lại; doctorId
     * rỗng thì giữ nguyên hành vi cũ (không gán, không bịa số).
     */
    @Transactional(readOnly = false)
    public int updateStatus(String id,String status,String departmentId,String doctorId)
    {
        int res = dao.updateStatus(id,status,departmentId,doctorId);
        if (res > 0 && StringUtils.isNotBlank(doctorId)) {
            Registration existing = dao.get(id);
            if (existing != null && existing.getQueueNumber() == null) {
                String companyId = existing.getCompany() != null ? existing.getCompany().getId() : null;
                if (StringUtils.isBlank(companyId)) {
                    companyId = SessionUtils.getLoginTenantId();
                }
                if (StringUtils.isNotBlank(companyId)) {
                    String queueDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString();
                    registrationDao.advanceQueueCounter(companyId, doctorId, queueDate);
                    Integer number = registrationDao.getQueueCounter(companyId, doctorId, queueDate);
                    if (number != null) {
                        registrationDao.updateQueueNumber(id, number);
                    }
                }
            }
        }
        return res;
    }
    @Transactional(readOnly = false)
    public int updateStatusByCompanyId(String companyId){
        //Tínhhết hạnThời gian(24nhỏ giờtrước)
        long time = new Date().getTime();
        Date date = new Date(time - 24 * 60 * 60 * 1000);
        return dao.updateStatusByCompanyId(companyId, date, date);
    }

    /**
     * Từ  phúttrangKết quả của Dòng mộtmụcBản ghi trong đề xuấtlấycompanyId, vàCập nhậthết hạnTrạng thái
     * Xóa bỏ Controller  trong trùng lặp của  companyId đề xuấtlấylogic
     */
    @Transactional(readOnly = false)
    public <T> void updateExpiredStatusFromPage(Page<T> result, java.util.function.Function<T, String> companyIdExtractor) {
        if (result != null && result.getTotal() > 0 && result.getRows() != null && !result.getRows().isEmpty()) {
            String companyId = companyIdExtractor.apply(result.getRows().get(0));
            if (companyId != null) {
                updateStatusByCompanyId(companyId);
            }
        }
    }
    /**
     * hủy số khám
     * @author lin.zeng
     **/
    @Transactional(readOnly = false)
    public int refundRegistrationPay(String id,String status,
                              String refundRegistrationPayType, String refundRegistrationRemarks,Date exitNumberDate
    )
    {
        int i = dao.refundRegistrationPay(id, status, refundRegistrationPayType, refundRegistrationRemarks, exitNumberDate);
        if(i>0){

        }

        return i;
    }

    public Page<Registration> listConditionPage(String param, int offset, int limit) {
        return paginate(
            () -> this.dao.countConditionList(param, SessionUtils.getLoginTenantId()),
            () -> this.dao.conditionList(param, SessionUtils.getLoginTenantId(), limit, offset)
        );
    }
    @Transactional(readOnly = false)
    public void updateRecipeStatus(String id) {
        registrationDao.updateRecipeStatus(id);
    }

    @Transactional(readOnly = true)
    public Page<Registration> listPages(PageRegistration pageRegistration) {
        if (!pageRegistration.isValidColumnName()) {
            throw new IllegalArgumentException("Invalid column name: " + pageRegistration.getColumnName());
        }
        List<String> strings = this.dao.countId(pageRegistration);
        int total=strings.size();
        List<Registration> list = null;
        if (total > 0) {

            List<String> strings1 = this.dao.listPages(pageRegistration);
            if(strings1.size()>0){
               list = this.dao.findById(strings1,pageRegistration);
            }
        }

        return new Page<>((long)total, list);
    }


    public List<String> whetherTheDispensing(PageRegistration pageRegistration) {
        List<String> strings = this.dao.countId(pageRegistration);
        return strings;
    }

    public List<String> whetherChargeOrNot(PageRegistration pageRegistration) {
        List<String> strings = this.dao.countId(pageRegistration);
        return strings;
    }

    @Transactional(readOnly = false)
    public Registration wxSave(Registration entity) {
        if (StringUtils.isBlank(entity.getId())) {
            if (entity.getQueueNumber() == null) {
                assignQueueNumber(entity);
            }
            entity.preInsert();
            this.doInsert(entity);
            Action created = this.createAction("created", entity);
            created.setCreateBy("WeChat");
            created.setUpdateBy("WeChat");
            this.saveAction(created);
        } else {
            entity.preUpdate();
            this.doUpdate(entity);
            this.saveAction(this.createAction("updated", entity));
        }
        return entity;
    }

    @Transactional(readOnly = true)
    public Page<Registration> getRegistrationByOpenId(List<Parameter> parameters, int offset, int limit, String orderby) {
        PageRequest pageRequest = new PageRequest(offset, limit, parameters, orderby);
        List<Parameter> params = pageRequest.getParams();
        String openId = (String) params.get(1).getValue();
        String companyId = (String) params.get(0).getValue();

        // sử dụngDữ liệukho phúttrang, Tránhbộ nhớ phúttrang và N+1Truy vấn
        return paginate(
            () -> this.dao.countByOpenId(openId, companyId),
            () -> this.dao.listByOpenId(openId, companyId, offset, limit)
        );
    }

    @Transactional(readOnly = false)
    public int wxReturnPay(Registration registrations) {
        int i = dao.wxReturnPay(registrations);

        return i;
    }


    public Page<ReceptionEvt> wxListPages(PageRegistration pageRegistration) {
        if (!pageRegistration.isValidColumnName()) {
            throw new IllegalArgumentException("Invalid column name: " + pageRegistration.getColumnName());
        }
        List<String> strings = this.dao.wxCount(pageRegistration);
        int total=strings.size();
        List<Registration> list = null;
        List<ReceptionEvt> lists = new ArrayList<>();
        if (total > 0) {

            List<String> strings1 = this.dao.wxListPages(pageRegistration);

            if(strings1.size()>0){
               // list = this.dao.findById(strings1,pageRegistration);
                for (String s : strings1) {
                    ReceptionEvt receptionEvt = medicalRecordService.allNewQuery(s, null);
                    lists.add(receptionEvt);
                }
            }
        }

        return new Page<>((long)total, lists);
    }

    public Page<ReceptionEvt> wxDispensingListPages(PageRegistration pageRegistration) {
        if (!pageRegistration.isValidColumnName()) {
            throw new IllegalArgumentException("Invalid column name: " + pageRegistration.getColumnName());
        }
        List<String> strings = this.dao.wxDispensingCount(pageRegistration);
        int total=strings.size();
        List<Registration> list = null;
        List<ReceptionEvt> lists = new ArrayList<>();
        if (total > 0) {

            List<String> strings1 = this.dao.wxDispensingListPages(pageRegistration);

            if(strings1.size()>0){
                // list = this.dao.findById(strings1,pageRegistration);
                for (String s : strings1) {
                    ReceptionEvt receptionEvt = medicalRecordService.allNewQuery(s, null);
                    lists.add(receptionEvt);
                }
            }
        }

        return new Page<>((long)total, lists);
    }

    @Transactional(readOnly = false)
    public int registrationupdate(Registration registrations) {
        int i = dao.registrationupdate(registrations);

        return i;
    }

    @Transactional(readOnly = false)
    public int medicalRecordInserts(MedicalRecord medicalRecord) {
        medicalRecord.setDiagnose("");
        medicalRecord.setId(IdGen.uuid());
        medicalRecord.setCompany(SessionUtils.getUser().getCompany());
        medicalRecord.setCreateBy(SessionUtils.getUser().getUpdateBy());
        medicalRecord.setCreateDate(SessionUtils.getUser().getUpdateDate());
        medicalRecord.setUpdateBy(SessionUtils.getUser().getUpdateBy());
        medicalRecord.setUpdateDate(SessionUtils.getUser().getUpdateDate());
        medicalRecord.setDelFlag("0");
        int result = medicalRecordDao.insert(medicalRecord);
        return result;
    }

}