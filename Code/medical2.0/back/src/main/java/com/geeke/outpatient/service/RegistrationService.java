package com.geeke.outpatient.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
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
    /**
     * DS146 B: tao moi (id trong) phai co it nhat mot trong ba: benh nhan,
     * bac si, hoac ngay hen dat lich. Truoc day save {} / bulkInsert [{}]
     * tra 200 va tao hang rac (patient/doctor/subscribe deu NULL, probe B1/B2
     * -- them 1 dong rac moi lan goi, chi de lai 1 dong cu tu 15:34).
     * Quyet dinh dua tren audit TOAN BO caller cua save() (xem report):
     * moi luot tao hop le deu co patient va/hoac doctor -- TRU BookingRequest
     * Service.confirm (G2) tao luot khong patient khong doctor nhung LUON co
     * subscribeDate (+ subscribeProject/remarks); PatientController inquire/add,
     * MedicalRecordService instant/retail, TollInfo addRetailRegistration deu co
     * patient (+ doctor). Chi ap cho TAO MOI: update (id khac rong) khong bi
     * anh huong -- cac luot setTreatmentDate/status-only (RecipelDetailService,
     * SupplierStockService, TollInfoService, MedicalRecordService) giu nguyen.
     * wxSave la method rieng, khong qua day, khong doi.
     */
    private static void checkRegistrationHasSubject(Registration entity) {
        if (entity == null || StringUtils.isNotBlank(entity.getId())) {
            return;
        }
        boolean hasPatient = entity.getPatientId() != null
                && StringUtils.isNotBlank(entity.getPatientId().getId());
        boolean hasDoctor = entity.getDoctor() != null
                && StringUtils.isNotBlank(entity.getDoctor().getId());
        boolean hasSubscribeDate = entity.getSubscribeDate() != null;
        if (!hasPatient && !hasDoctor && !hasSubscribeDate) {
            throw new ServiceException("Lượt đăng ký khám phải có bệnh nhân, bác sĩ hoặc ngày hẹn đặt lịch");
        }
    }

    /**
     * DS146 A: @Length tren entity (pulse/breathe/temperature 45,
     * isGoHigharea 64, highArea 128, freeRegistrationFee 64,
     * dispensingStatus/chargeStatus 1) + remarks/subscribeProject theo
     * gioi han cot DB 255 (mau DS141: truong khong @Length thi guard o
     * gioi han DB). RegistrationController.save KHONG co @Valid va
     * service save() truoc day khong guard nen gia tri qua dai 500 o DB
     * (probe A7/A8/A9: pulse 46). Guard o day dung chung cho save()
     * (funnel duy nhat, truoc ca assignQueueNumber de khong dot so
     * thu tu cua luot bi tu choi) va bulk*.
     */
    private static void checkRegistrationLengths(Registration entity) {
        if (entity == null) {
            return;
        }
        checkLength(entity.getPulse(), 45, "mạch Độ dài phải nằm trong khoảng 0 đến 45");
        checkLength(entity.getBreathe(), 45, "hô hấp Độ dài phải nằm trong khoảng 0 đến 45");
        checkLength(entity.getTemperature(), 45, "Nhiệt độ cơ thể Độ dài phải nằm trong khoảng 0 đến 45");
        checkLength(entity.getIsGoHigharea(), 64, "Có hay không đi qua dịch Độ dài quận phải nằm trong khoảng 0 đến 64");
        checkLength(entity.getHighArea(), 128, "dịch Quận địa phương điểm Độ dài phải nằm trong khoảng 0 đến 128");
        checkLength(entity.getFreeRegistrationFee(), 64, "miễn Phí đăng ký khám Độ dài phải nằm trong khoảng 0 đến 64");
        checkLength(entity.getDispensingStatus(), 1, "phát thuốc Độ dài trạng thái phải nằm trong khoảng 0 đến 1");
        checkLength(entity.getChargeStatus(), 1, "Thu phí Độ dài trạng thái phải nằm trong khoảng 0 đến 1");
        checkLength(entity.getRemarks(), 255, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(entity.getSubscribeProject(), 255, "Đặt lịch hạng mục Độ dài phải nằm trong khoảng 0 đến 255");
        // DS147 A: cac truong String khong @Length nhung mapper co viet
        // (bulkInsert/bulkUpdate/save-insert) -- gioi han theo cot DB.
        // refundRegistrationRemarks/mdtrtCertNo KHONG viet o bat ky mapper
        // nao (mdtrtCertNo khong co cot DB) nen khong guard; isPre la Boolean.
        checkLength(entity.getBloodPressure(), 100, "Huyết áp Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getGuardianName(), 64, "Tên người giám hộ Độ dài phải nằm trong khoảng 0 đến 64");
        checkLength(entity.getGuardianPhone(), 64, "Số điện thoại người giám hộ Độ dài phải nằm trong khoảng 0 đến 64");
        checkLength(entity.getWorkplace(), 255, "Nơi làm việc Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(entity.getNation(), 255, "Dân tộc Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(entity.getOccupation(), 255, "Nghề nghiệp Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(entity.getMdtrtId(), 30, "Mã giao dịch BHYT Độ dài phải nằm trong khoảng 0 đến 30");
        checkLength(entity.getIptOtpNo(), 100, "Số khám ngoại trú Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(entity.getSetlId(), 64, "Mã quyết toán BHYT Độ dài phải nằm trong khoảng 0 đến 64");
        checkLength(entity.getPracticalRegistrationPayRemarks(), 255, "Ghi chú thanh toán thực tế Độ dài phải nằm trong khoảng 0 đến 255");
        // DS147 B: gia tri DictItem viet thang vao cot varchar (mapper
        // #{entity.xxx.value}) -- @Length(128) cua DictItem chi guard bang
        // dict, khong guard cot dich (med/cardType varchar(10), cac cot
        // con lai varchar(64)). Null-safe: DictItem null hoac value null
        // thi bo qua nhu cu (mapper viet NULL).
        checkDictLength(entity.getMedType(), 10, "Loại hình y tế Độ dài phải nằm trong khoảng 0 đến 10");
        checkDictLength(entity.getCardType(), 10, "Loại giấy tờ khám bệnh Độ dài phải nằm trong khoảng 0 đến 10");
        checkDictLength(entity.getPayType(), 64, "Hình thức thanh toán Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(entity.getTreatType(), 64, "Loại khám Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(entity.getInfectType(), 64, "Loại lây nhiễm Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(entity.getSource(), 64, "Nguồn đăng ký Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(entity.getStatus(), 64, "Trạng thái đăng ký Độ dài phải nằm trong khoảng 0 đến 64");
    }

    private static void checkDictLength(com.geeke.sys.entity.DictItem item, int max, String message) {
        if (item != null) {
            checkLength(item.getValue(), max, message);
        }
    }

    private static void checkLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ServiceException(message);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Registration save(Registration entity) {
        // DS146 A: chan do dai truoc khi gan so thu tu.
        checkRegistrationLengths(entity);
        // DS146 B: chan hang rac {} o tang save (bulkInsert mirror ben duoi).
        checkRegistrationHasSubject(entity);
        if (StringUtils.isBlank(entity.getId()) && entity.getQueueNumber() == null) {
            assignQueueNumber(entity);
        }
        // DS163 A4: save voi id khong ton tai truoc day roi qua super.save
        // -> doUpdate 0 dong roi van 200 silent no-op (lop DS94/DS99; probe
        // A4; ca audit UPDATED ma cho id ma). Chi chap nhan update khi
        // dong da ton tai; tao moi phai gui id rong.
        if (StringUtils.isNotBlank(entity.getId())
                && super.get(entity.getId()) == null) {
            throw new ServiceException("Đăng ký khám không tồn tại: " + entity.getId());
        }
        return super.save(entity);
    }

    /**
     * DS145 B: CrudController bulk routes goi thang CrudService.bulkInsert/
     * bulkUpdate, bo qua moi guard cua save()/delete(): bulkUpdate id ma
     * 200 im lang (probe B1 -- CrudService stored-row check bo qua
     * stored==null theo thiet ke ITER35); bulkInsert luot co bac si
     * khong duoc gan so thu tu (probe B4 -- save() gan qua
     * assignQueueNumber, bulk thi khong). Ghi de theo mau DS99/DS108:
     * null/rong -> 400, entry null -> 400, bulkUpdate id trong / dong
     * khong ton tai / da xoa mem -> 400; bulkInsert gan so nhu save().
     * KHONG them rule bat buoc patient/doctor: BookingRequestService.
     * confirm tao luot khong patient khong doctor mot cach hop le (G2),
     * va single save {} -> 200 nhu bulk (do o tang save, follow-up,
     * khong phai lo bulk). bulkDelete ghost giu 200 rows 0 theo parity
     * DS99 G11/DS126 B.
     */
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<Registration> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách lượt đăng ký thêm mới không được để trống");
        }
        for (Registration entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Lượt đăng ký không được để trống");
            }
            // DS146 A: bulk cung chan do dai nhu save().
            checkRegistrationLengths(entity);
            // DS146 B: chan hang rac {} qua bulk nhu save().
            checkRegistrationHasSubject(entity);
            if (StringUtils.isBlank(entity.getId()) && entity.getQueueNumber() == null) {
                assignQueueNumber(entity);
            }
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<Registration> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách lượt đăng ký cập nhật không được để trống");
        }
        for (Registration entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Lượt đăng ký không được để trống");
            }
            if (StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của lượt đăng ký đã tồn tại");
            }
            Registration stored = dao.get(entity.getId());
            if (stored == null || !"0".equals(stored.getDelFlag())) {
                throw new ServiceException("Lượt đăng ký không tồn tại: " + entity.getId());
            }
            // DS146 A: bulk cung chan do dai nhu save().
            checkRegistrationLengths(entity);
        }
        return super.bulkUpdate(entitys);
    }

    /**
     * DS119 A: mapper <update id="delete"> co nhanh <otherwise> xoa mem
     * theo patient_id khi id rong -- delete khong id + patientId se xoa
     * sach moi luot dang ky cua benh nhan. Chan o service: chi xoa khi
     * co id; khong caller noi bo nao dung duong khong id (kiem tra
     * 2026-11-07).
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(Registration entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được lượt đăng ký cần xóa.");
        }
        // DS134 C: ghost / da xoa mem (del_flag '1') -> 400 fail-closed
        // (truoc day: ghost -> 200 data 0 im lang, xoa lan 2 -> 200 rows 1
        // vi mapper soft-delete khong loc del_flag; CrudService chi
        // check tenant tren stored row, stored null thi bo qua -- lop
        // DS123). checkExists (DS103) KHONG tai dung: cho qua blank id,
        // khong check delFlag. bulkDelete ghost CO TINH GIU 200 rows 0
        // (DS99/DS106/DS126 B idempotency).
        Registration stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Lượt đăng ký không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
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
     * O1: blank departmentId/doctorId được chuẩn hóa thành NULL trước khi
     * gọi DAO, vì @Update của DAO luôn set cả 2 cột số và MySQL báo 500
     * "Incorrect integer value: ''" khi nhận chuỗi rỗng. NULL mang ngữ
     * nghĩa "gỡ khoa/bác sĩ", không im lặng giữ giá trị cũ như <if> động.
     */
    @Transactional(readOnly = false)
    public int updateStatus(String id,String status,String departmentId,String doctorId)
    {
        // ITER37 O1: kiểm tra dòng DB đã lưu trước khi ghi, theo mẫu M1.
        // Trước đây updateStatus gọi thẳng dao.updateStatus (WHERE id,
        // không lọc company) nên token tenant 309 đổi được lượt khám của
        // tenant 297 và còn cấp số từ counter của tenant lạ (tái hiện
        // tools/iter37-o1-repro.py). Probe chỉ mang id: helper tự đọc
        // dòng đã lưu qua dao.get, dòng lạ (không phải tenant đăng nhập
        // và không phải institution-shared) → ServiceException 400,
        // transaction rollback nên không ghi gì, không tăng counter.
        // Id rỗng / dòng không tồn tại / company rỗng vẫn qua như
        // ngữ nghĩa cũ của helper (ghi 0 dòng).
        Registration probe = new Registration();
        probe.setId(id);
        verifyTenantOwnershipAgainstStoredRow(probe);
        if (StringUtils.isBlank(departmentId)) {
            departmentId = null;
        }
        if (StringUtils.isBlank(doctorId)) {
            doctorId = null;
        }
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
        // O4-37: kiem tra tenant dong DB da luu truoc khi dao ghi (nhu
        // updateStatus O1-37) — truoc day controller chi get() o nhanh
        // remarks = ma da hoan phi, nhanh else goi thang dao (WHERE id,
        // khong loc company) nen token tenant 309 huy duoc luot kham
        // tenant 297. Dong la -> ServiceException -> 400, khong ghi gi;
        // id rong/khong ton tai/company rong van qua nhu cu (ghi 0 dong).
        Registration probe = new Registration();
        probe.setId(id);
        verifyTenantOwnershipAgainstStoredRow(probe);
        // DS64: hoan phi bang atomic claim thay cho dao
        // refundRegistrationPay vo dieu kien (xem wxReturnPay ben duoi):
        // guard status o controller chi chay khi remarks == _2, client
        // that gui remarks="" nen guard khong bao gio chay — tuan tu thu
        // 2 van duoc nhan 200 data 1 va client tao them phieu hoan phi.
        // Claim 0 dong -> 400 nhu truong hop tuan tu tuong duong (wx).
        Registration claim = new Registration();
        claim.setId(id);
        com.geeke.sys.entity.DictItem statusItem = new com.geeke.sys.entity.DictItem();
        statusItem.setValue(status);
        claim.setStatus(statusItem);
        com.geeke.sys.entity.DictItem payTypeItem = new com.geeke.sys.entity.DictItem();
        payTypeItem.setValue(refundRegistrationPayType);
        claim.setRefundRegistrationPayType(payTypeItem);
        claim.setRefundRegistrationRemarks(refundRegistrationRemarks);
        claim.setExitNumberDate(exitNumberDate);
        int i = dao.claimRefund(claim);
        if (i != 1) {
            // Dong khong ton tai / id rong: giu hanh vi cu (tra 0, khong
            // nem) nhu dao cu ghi 0 dong; dong da ton tai nhung het
            // trang thai duoc hoan -> 400 nhu truong hop tuan tu (wx).
            Registration stored = this.get(id);
            if (stored == null) {
                return 0;
            }
            throw new com.geeke.common.service.ServiceException("bạn Đã lấy hủy đặt lịch, không có thể lại lần lấy hủy!");
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
        // Z1b: companyId tu body khong duoc phep lai tenant khac — SQL
        // countId/listPages loc a.company_id=#{companyId} truc tiep, nen
        // body tenant la truoc day tra ve dang ky cua tenant do (duong
        // patidwsflist/dispensing//v2/list dung chung method nay).
        // Session rong/\"null\" giu hanh vi cu nhu verifyTenantOwnership.
        forceSessionTenant(pageRegistration);
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

    // Z1b: ghi de companyId body bang session tenant (dung chung cho
    // listPages; xem ghi chu o listPages).
    private void forceSessionTenant(PageRegistration pageRegistration) {
        String tenant = SessionUtils.getLoginTenantId();
        if (tenant != null && !tenant.isEmpty() && !"null".equals(tenant)) {
            pageRegistration.setCompanyId(tenant);
        }
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
        // C1 (iteration 50): wxSave truoc day goi doInsert/doUpdate truc tiep,
        // ne ca verifyTenantOwnership lan stored-row check cua CrudService.save
        // (mapper update chi WHERE id) — token tenant A tao/ghi de luot tenant B.
        // Ap dung dung ngu nghia CrudService.save: payload company phai khop
        // session tenant (company rong/session rong giu hanh vi cu); update
        // them stored-row check de id dong la + company own khong qua duoc.
        verifyTenantOwnership(entity);
        if (StringUtils.isNotBlank(entity.getId())) {
            verifyTenantOwnershipAgainstStoredRow(entity);
        }
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
        // DS154 C1/C2: caller gui < 2 params -> params.get(1)
        // IndexOutOfBounds 500 (service doc params[0]=companyId,
        // params[1]=openId theo vi tri). Fail-closed 400 truoc moi doc.
        if (params == null || params.size() < 2) {
            throw new ServiceException("Thiếu tham số truy vấn đăng ký (company_id, open_id)");
        }
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
        // DS64: hoan phi bang atomic claim (mau DS52-DS63) thay cho dao
        // wxReturnPay vo dieu kien. Controller da kiem tra status truoc
        // (read-then-act) nen tuan tu thu 2 bi 400, nhung 8 song song co
        // 5 duoc nhan (probe): cac luong deu doc qua guard truoc khi
        // luong thang commit. Claim chi lat khi dong van o trang thai
        // duoc hoan (NULL/_3/_5, xem RegistrationDao.xml claimRefund);
        // 0 dong -> ServiceException 400 voi dung thong diep tu choi
        // tuan tu, khong ghi gi, thu lai tren luot kham moi thi duoc.
        int i = dao.claimRefund(registrations);
        if (i != 1) {
            throw new com.geeke.common.service.ServiceException("bạn Đã lấy hủy đặt lịch, không có thể lại lần lấy hủy!");
        }

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
        // ITER62 P2: verify stored-row company before writing, never trust
        // payload company.id. The mapper WHERE is company_id=#{company.id}
        // from the payload, so a caller could previously write a foreign
        // visit by carrying its company id (reproduced live:
        // POST registationupdate with a foreign id + foreign company
        // changed that visit's pulse NULL -> 99). The helper reads the
        // stored row via dao.get (which selects a.company_id), so a
        // foreign stored row -> ServiceException 400 before any write,
        // while blank id / missing row / blank stored company keep the
        // legacy 0-row behaviour, exactly like updateStatus (ITER37 O1).
        // An own stored row with a mismatched payload company still
        // writes 0 rows via the mapper WHERE, unchanged from before.
        if (registrations != null) {
            verifyTenantOwnershipAgainstStoredRow(registrations);
        }
        int i = dao.registrationupdate(registrations);

        return i;
    }

    @Transactional(readOnly = false)
    public int medicalRecordInserts(MedicalRecord medicalRecord) {
        // DS82: direct-DAO insert bypasses the DS81 MedicalRecordService.save
        // guard (no @Valid runs on this path — the controller parses the
        // entity from JSON and calls straight here), so a null patientTell /
        // doctor id / registration id 500d on the NOT NULL columns, and a
        // >128-char patientTell 500d under STRICT_TRANS_TABLES. Fail closed
        // 400 here, in the same words as the DS81 guard. Null-only for
        // patientTell: live rows carry '' (inserted by this same path), so
        // blank is NOT rejected; length is capped at the DB 128.
        if (medicalRecord.getPatientTell() == null) {
            throw new com.geeke.common.service.ServiceException("Lý do khám chính không được để trống");
        }
        if (medicalRecord.getPatientTell().length() > 128) {
            throw new com.geeke.common.service.ServiceException("Lý do khám chính độ dài phải nằm trong khoảng 1 đến 128");
        }
        if (medicalRecord.getDoctor() == null || medicalRecord.getDoctor().getId() == null) {
            throw new com.geeke.common.service.ServiceException("Bác sĩ không được để trống");
        }
        if (medicalRecord.getRegistration() == null || medicalRecord.getRegistration().getId() == null) {
            throw new com.geeke.common.service.ServiceException("Lượt đăng ký khám không được để trống");
        }
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