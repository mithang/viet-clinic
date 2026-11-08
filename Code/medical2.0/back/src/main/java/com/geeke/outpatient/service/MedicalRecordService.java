package com.geeke.outpatient.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.geeke.basicdata.entity.ManufactureFactory;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.persistence.DataEntity;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.cure.entity.InspectionCheck;
import com.geeke.cure.entity.InspectionCheckInfo;
import com.geeke.cure.service.InspectionCheckDetailService;
import com.geeke.cure.service.InspectionCheckInfoService;
import com.geeke.cure.service.InspectionCheckService;
import com.geeke.org.entity.Company;
import com.geeke.org.service.CompanyService;
import com.geeke.outpatient.dao.MedicalRecordDao;
import com.geeke.outpatient.entity.*;
import com.geeke.stock.dao.DrugDao;
import com.geeke.stock.dao.StuffDao;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.InstantPatient;
import com.geeke.stock.entity.MedicinalStockControl;
import com.geeke.stock.dao.MedicinalStockControlDao;
import com.geeke.stock.entity.InventoryVerification;
import com.geeke.stock.entity.Stuff;
import com.geeke.stock.service.DrugService;
import com.geeke.stock.service.InventoryVerificationService;
import com.geeke.stock.service.MedicinalStorageControlService;
import com.geeke.stock.service.StuffService;
import com.geeke.sys.entity.DictItem;
import com.geeke.sys.service.SysFileService;
import com.geeke.toll.service.TollInfoService;
import com.geeke.treatment.dao.CostItemDao;
import com.geeke.treatment.entity.CostItem;
import com.geeke.treatment.service.CostItemService;
import com.geeke.treatment.service.impl.CostItemPackageService;
import com.geeke.utils.IdGen;
import com.geeke.utils.SerialNoUtils;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Điền bệnh ánService
 *
 * @author txl
 * @version 2022-06-13
 */

@Service("medicalRecordService")
@Transactional(readOnly = true)
public class MedicalRecordService extends CrudService<MedicalRecordDao, MedicalRecord> {

    @Lazy
    @Autowired
    private RegistrationService registrationService;

    @Lazy
    @Autowired
    private RecipelInfoService recipelInfoService;

    @Autowired
    private RecipelDetailService recipelDetailService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private CostItemService costItemService;

    @Autowired
    private StuffService stuffService;

    // DS140 A v2: DAO đọc thẳng để kiểm tenant TRƯỚC khi enrich (xem
    // getDrugStuffEvt). Không dùng service.get ở đây.
    @Autowired
    private DrugDao drugDao;

    @Autowired
    private CostItemDao costItemDao;

    @Autowired
    private StuffDao stuffDao;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private MedicalRecordDao medicalRecordDao;

    @Autowired
    private SysFileService sysFileService;

    @Lazy
    @Autowired
    private TollInfoService tollInfoService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private SerialNoUtils serialNoUtils;

    @Autowired
    private MedicinalStorageControlService medicinalStorageControlService;

    @Autowired
    private MedicinalStockControlDao medicinalStockControlDao;

    @Autowired
    private InspectionCheckService inspectionCheckService;

    @Autowired
    private InspectionCheckInfoService inspectionCheckInfoService;

    @Autowired
    private InspectionCheckDetailService inspectionCheckDetailService;

    @Autowired
    private CostItemPackageService costItemPackageService;

    @Autowired
    private InventoryVerificationService inventoryVerificationService;

    @Autowired
    private RemoteDiagnosisTreatmentService remoteDiagnosisTreatmentService;

    @Autowired
    private PresDrugService presDrugService;

    /**
     * DS81: patientTell is NOT NULL in the DB but the entity had only
     * @Length(min=1) (null passes validation) and no reachable save path
     * runs @Valid — saveWithFile/saveAdd/registerSave/allSave all parse
     * the entity from a String and funnel here, so a null patientTell
     * 500d on the insert (probe-proven). Fail closed 400 here, in the
     * single-arg funnel every real save path uses. Null only: 7 live
     * rows carry '' patientTell (registration flow inserts '' via the
     * direct-DAO medicalRecordInserts path), so blank is NOT rejected.
     */
    @Override
    @Transactional(readOnly = false)
    public MedicalRecord save(MedicalRecord medicalRecord) {
        if (medicalRecord.getPatientTell() == null) {
            throw new ServiceException("Lý do khám chính không được để trống");
        }
        return super.save(medicalRecord);
    }

    /**
     * DS119 A: mapper <update id="delete"> co nhanh <otherwise> xoa mem
     * theo registration_id khi id rong -- delete khong id + registration
     * se xoa sach moi benh an cua luot kham. Chan o service: chi xoa
     * khi co id; khong caller noi bo nao dung duong khong id (kiem tra
     * 2026-11-07).
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(MedicalRecord entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được bệnh án cần xóa.");
        }
        // DS134 A: ghost / da xoa mem (del_flag '1') -> 400 fail-closed
        // (truoc day: ghost -> 200 data 0 im lang, xoa lan 2 -> 200 rows 1
        // vi mapper soft-delete khong loc del_flag; CrudService chi
        // check tenant tren stored row, stored null thi bo qua -- lop
        // DS123). checkExists (DS103) KHONG tai dung: cho qua blank id,
        // khong check delFlag. bulkDelete ghost CO TINH GIU 200 rows 0
        // (DS99/DS106/DS126 B idempotency).
        MedicalRecord stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Bệnh án không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
    }

    @Transactional(readOnly = false)
    public MedicalRecord save(MedicalRecord medicalRecord,
                              MultipartFile[] fileIdUploads,
                              String[] deleteIds) throws java.io.IOException {
        if (StringUtils.isBlank(medicalRecord.getFileId())) {
            medicalRecord.setFileId(IdGen.uuid());
        }
        // J2 (iteration 57): v1 saveWithFile la diem xoa file thu 4 ngoai v2
        // (guardExamDeleteFileIds cua I1 chi gan trong allNewSave) — truoc day
        // sysFileService.delete(deleteIds) chay tran, nen benh an OWN hop le
        // + deleteIds chua file tenant la -> xoa that file + content cua
        // tenant la (tai hien live). Tai dung guard I1 tai day, TRUOC moi
        // upload/xoa/ghi: tap hop hop le = registration id + fileId cua
        // benh an da luu cho luot nay + fileId dang gui; file khong ton tai
        // giu hanh vi cu (bo qua); file la -> 400 va giao dich rollback
        // toan bo. Khong sua SysFileService.delete generic (caller khac dung
        // object_id theo nghia rieng, bai hoc I1).
        if (null != deleteIds && deleteIds.length > 0) {
            String j2RegistrationId = medicalRecord.getRegistration() != null
                    ? medicalRecord.getRegistration().getId() : null;
            this.guardExamDeleteFileIds(deleteIds, null, j2RegistrationId, medicalRecord.getFileId());
        }
        // Lưutệp đính kèm
        sysFileService.changeAndSaveSysFileList(fileIdUploads, medicalRecord.getFileId());
        // Theo tệp đính kèmIDXóa thông tin tệp đính kèm
        if (null != deleteIds && deleteIds.length > 0) {
            sysFileService.delete(deleteIds);
        }
        return save(medicalRecord);
    }

    /**
     Đăng ký khámidTruy vấnca bệnhThông tin
     */
    public List<MedicalRecord> getByOrder(String registration) {
        // T2 (a): truoc day dao chi WHERE registration id, khong loc tenant
        // (khac getPatientByPhone truyen company session). Nay ep company
        // session nhu mau do; session rong ("null"/blank, luong noi bo
        // khong co session) thi SQL khong them dieu kien, giu hanh vi cu.
        return medicalRecordDao.getByOrder(registration, SessionUtils.getLoginTenantId());
    }

    @Transactional(readOnly = false)
    public Registration allSave(MedicalRecipelEvt medicalRecipelEvt, MultipartFile[] fileIdUploads, String strDeleteIds) {
        //Lưubệnh án
        return saveAllRecordInfo(medicalRecipelEvt);
    }

    private Registration saveAllRecordInfo(MedicalRecipelEvt medicalRecipelEvt) {
        Registration bindedRegistration;
        MedicalRecord medicalRecord = medicalRecipelEvt.getMedicalRecord();
        List<RecipelStastics> recipelStasticsList = new ArrayList<>();
        // có nhanhtốc
        if (InstantPatient.INSTANT.getCode() == medicalRecipelEvt.getIsInstant()) {
            //mộtkhóalưu vàoNgười dùngThông tin
            Patient patient = medicalRecipelEvt.getPatient();
            // F1 (iteration 53): allSave nhan entity dang String nen khong @Valid,
            // va saveAllRecordInfo goi thang patientService.save — truoc day
            // company lay thang tu payload: token tenant A gui company tenant B
            // thi tao that benh nhan o B (CrudService.save nhanh insert khong
            // verify tenant), roi luot kham o A (SessionUtils.getLoginTenant)
            // tro sang benh nhan B; benh nhan khong company + SDT/CCCD thi
            // PatientService:64/:74 deref getCompany().getId() NPE 500. Nay ep
            // company theo session tenant nhu mau C3/C4/D1/E1 (session co tenant
            // thi bo qua payload); session rong + thieu company thi 400 ro
            // rang kieu C2, khong de roi toi deref. Chi sua nhanh instant nay,
            // khong sua PatientService.save generic (quyet E2/plan iter53).
            if (patient == null) {
                throw new ServiceException("Thiếu thông tin bệnh nhân, không thể tạo hồ sơ tiếp nhận nhanh");
            }
            String f1Tenant = SessionUtils.getLoginTenantId();
            if (f1Tenant != null && !f1Tenant.trim().isEmpty() && !"null".equals(f1Tenant)) {
                patient.setCompany(new Company(f1Tenant));
            } else if (patient.getCompany() == null || patient.getCompany().getId() == null
                    || patient.getCompany().getId().trim().isEmpty()) {
                throw new ServiceException("Thiếu thông tin phòng khám (company), không xác định được tenant");
            }
            Patient savedPatient = patientService.save(patient);

            //mộtkhóaĐăng ký khám
            Registration registration = new Registration();
            registration.setRegistrationFee(BigDecimal.ZERO);
            registration.setCompany(SessionUtils.getLoginTenant());
            registration.setDoctor(SessionUtils.getUser());
            DictItem dictItem = new DictItem();
            dictItem.setValue(BizConstants.TREAT_TYPE_NORMAL);
            registration.setTreatType(dictItem);
            DictItem dictItemTo = new DictItem();
            dictItemTo.setValue(BizConstants.INFECT_TYPE_NONE);
            registration.setInfectType(dictItemTo);
            registration.setPatientId(savedPatient);
            bindedRegistration = registrationService.save(registration);

            //mớitạo của Đăng ký khámđơnliên kếtchỉ địnhtrên
            medicalRecord.setRegistration(bindedRegistration);
        } else {
            bindedRegistration = registrationService.get(medicalRecipelEvt.getMedicalRecord().getRegistration().getId());
        }
        //Lưuca bệnhThông tin
        medicalRecord.setDiagnoseDate(new Date());
        // J1 (iteration 57): v1 allSave giong v2 truoc I2 — CrudService.save
        // nhanh insert khong verify tenant, nen medicalRecord/recipelInfo/
        // recipelDetail mang company tenant la + id trong se tao that dong
        // o tenant la gan luot OWN (tai hien live: ca 3 deu ve OTHER).
        // Ep company theo bindedRegistration (luot da verify OWN o nhanh
        // thuong, luot moi tao theo session tenant o nhanh instant), CHI
        // khi insert (id trong): nhanh update giu nguyen guard verify
        // payload + stored-row (update don/case/chi tiet + company la da
        // 400 san, khong ghi de thanh 200 am tham). Detail o v1 khong
        // null id nhu v2 (saveRecipelInfoEvt) nen khong ep luon nhu I2:
        // ep luon se vo hieu guard 400 cua update detail.
        if (StringUtils.isBlank(medicalRecord.getId()) && bindedRegistration.getCompany() != null) {
            medicalRecord.setCompany(bindedRegistration.getCompany());
        }
        MedicalRecord medicalRecordSave = this.save(medicalRecord);
        //Lấy thông tin đơn thuốc
        List<RecipelInfoEvt> recipelInfoEvtList = medicalRecipelEvt.getRecipelInfoEvtList();
        //dùng đểLưuThu phí
        List<RecipelInfo> recipelInfos = new ArrayList<>();
        if (null != recipelInfoEvtList && !recipelInfoEvtList.isEmpty()) {
            for (int recipelIdx = 0; recipelIdx < recipelInfoEvtList.size(); recipelIdx++) {
                RecipelInfoEvt recipelInfoEvt = recipelInfoEvtList.get(recipelIdx);
                //Lưu thông tin đơn thuốc
                RecipelInfo recipelInfo = recipelInfoEvt.getRecipelInfo();
                //recipelInfo.setMedicalRecord(medicalRecordSave);
                recipelInfo.setRegistration(bindedRegistration);
                //hoàn thànhkhám bệnh của  giờchờđềuKhông cóthanh toán
                recipelInfo.setIsPay("0");
                //K3: frontend FinishVisit không gửi seq/isFollowUp, nhưng 2 cột
                //này NOT NULL (seq không có default) nên insert vỡ 500 với
                //Column 'seq' cannot be null. Đặt mặc định ở server khi thiếu,
                //giữ nguyên giá trị nếu caller đã gửi (không ghi đè):
                //seq theo vị trí đơn trong danh sách (như các đường save khác
                //đặt setSeq(i + 1)), isFollowUp = 0 (đúng default của cột).
                //smallType vẫn để nguyên: cột nullable, không gây lỗi, và dữ
                //liệu hiện hữu không thống nhất giá trị mặc định (39/40 dòng
                //là recipelSmallType_1, chỉ 1 dòng smallType_0) nên không bịa.
                if (null == recipelInfo.getSeq()) {
                    recipelInfo.setSeq(recipelIdx + 1);
                }
                if (null == recipelInfo.getIsFollowUp()) {
                    recipelInfo.setIsFollowUp(0);
                }

                //ĐặtMã
                String recipelNo = serialNoUtils.generateSerialNo("");
                recipelInfo.setCode(recipelNo);
                // J1: chi khi insert (xem ghi chu o medicalRecord tren).
                if (StringUtils.isBlank(recipelInfo.getId()) && bindedRegistration.getCompany() != null) {
                    recipelInfo.setCompany(bindedRegistration.getCompany());
                }
                RecipelInfo recipelInfoSave = recipelInfoService.save(recipelInfo);
                List<RecipelDetail> recipelDetailEvtList = recipelInfoEvt.getRecipelDetailEvtList();
                if (null != recipelDetailEvtList && !recipelDetailEvtList.isEmpty()) {
                    //duyệtĐơn thuốc
                    for (RecipelDetail recipelDetail : recipelDetailEvtList) {
                        //LưuĐơn thuốcChi tiết
                        // J1: chi khi insert (xem ghi chu o medicalRecord tren).
                        if (StringUtils.isBlank(recipelDetail.getId()) && bindedRegistration.getCompany() != null) {
                            recipelDetail.setCompany(bindedRegistration.getCompany());
                        }
                        recipelDetail.setRecipelInfo(recipelInfoSave);
                        // Nếu frontend không truyềnminTotal(Tổng lượng đơn vị bào chế), Thì dùngtotalLàm giá trị mặc định
                        if (recipelDetail.getMinTotal() == null || recipelDetail.getMinTotal() == 0) {
                            recipelDetail.setMinTotal(recipelDetail.getTotal());
                        }
                        //Các trường sau đã được frontend hoặc giá trị mặc định xử lý
//                        recipelDetail.setIsUnpackSell(0);
//                        recipelDetail.setStuffType("0");

                        recipelDetailService.save(recipelDetail);
                        //tìmtậpĐơn thuốcThông tin
                        String value = recipelDetail.getStuffType();
                        RecipelStastics recipelStastics = RecipelStastics.builder().id(recipelDetail.getDrugStuffId().getDrugStuffId())
                                .stuffType(value)
                                .occupy(recipelDetail.getMinTotal() == null ? 0 : recipelDetail.getMinTotal()).build();
                        recipelStasticsList.add(recipelStastics);
                    }
                }
                recipelInfos.add(recipelInfoSave);
            }
        }

        //SửaĐăng ký khámTrạng thái là Đãkhám bệnh
        DictItem dictItem = new DictItem();
        dictItem.setValue(BizConstants.REG_STATUS_VISITED);
        bindedRegistration.setStatus(dictItem);
        bindedRegistration.setReceptionEndDate(new Date());
        bindedRegistration.setTreatType(medicalRecipelEvt.getMedicalRecord().getRegistration().getTreatType());
        bindedRegistration.setInfectType(medicalRecipelEvt.getMedicalRecord().getRegistration().getInfectType());
        //Q2: hang sinh hieu cu (BasicInfoModel) duoc nap TU registration nen
        //chi registration moi la noi luu that (bang patient khong co 4 cot
        //nay, mapper PatientDao cung khong co). Nhanh thuong truoc day
        //khong copy nen sua o FinishVisit (v1 allSave) mat lang du luu
        //200 va chuyen Da kham; nhanh v2 allNewSave da copy dung kieu
        //nay (registration.setTemperature/Pulse/Breathe/BloodPressure tu
        //patient). Chi copy o nhanh thuong: nhanh instant tao registration
        //moi rieng, khong dung bindedRegistration nay. Guard patient null
        //de caller khong gui patient khong NPE; gia tri null/xoa cung duoc
        //copy nhu v2 (xoa sinh hieu la y do hop le cua nguoi nhap).
        if (null != medicalRecipelEvt.getPatient()) {
            bindedRegistration.setTemperature(medicalRecipelEvt.getPatient().getTemperature());
            bindedRegistration.setPulse(medicalRecipelEvt.getPatient().getPulse());
            bindedRegistration.setBreathe(medicalRecipelEvt.getPatient().getBreathe());
            bindedRegistration.setBloodPressure(medicalRecipelEvt.getPatient().getBloodPressure());
        }
        registrationService.save(bindedRegistration);

        //Kiểm traCó hay khôngđủđủ
        //isEnough(recipelStasticsList);
        return bindedRegistration;
    }

    private void isEnough(String companyId, List<RecipelStastics> recipelStasticsList) {
        List<RecipelStastics> occupied = recipelDetailService.getDetailStasticsForOccupy(30);
        Map<String, List<RecipelStastics>> collect = recipelStasticsList.stream()
                .collect(Collectors.groupingBy(RecipelStastics::getId));
        List<RecipelStastics> need = new ArrayList<>();
        collect.forEach((k, v) -> {
            int all = v.stream().mapToInt(RecipelStastics::getOccupy).sum();
            RecipelStastics one = RecipelStastics.builder().id(k).occupy(all).stuffType(v.get(0).getStuffType()).build();
            need.add(one);
        });
        for (RecipelStastics now : need) {
            String nowId = now.getId();
            for (RecipelStastics oc : occupied) {
                String ocId = oc.getId();
                int total = oc.getOccupy() + now.getOccupy();
                if (ocId.equals(nowId)) {
                    // Từ mớiTồn khoHệ thốngđọckhả dụngTồn kho(MedicinalStockControl.surplusStock)
                    List<MedicinalStockControl> stockList = medicinalStockControlDao.inventory(companyId, ocId);
                    if (stockList == null || stockList.isEmpty()) {
                        throw new ServiceException("Hiện tại vật liệu không ở Dữ liệu bảng trong .");
                    }
                    BigDecimal surplusStock = stockList.get(0).getSurplusStock();
                    if (surplusStock == null || surplusStock.compareTo(new BigDecimal(total)) < 0) {
                        throw new ServiceException("Hiện tại vật liệu Tồn kho không đủ, không phương pháp làm thủ tục.");
                    }
                }
            }
        }
    }

    public MedicalRecipelEvt allQuery(Registration registration, String chargeStatus) {
        MedicalRecipelEvt outEvt = new MedicalRecipelEvt();
        List<RecipelInfoEvt> recipelInfoEvtList = new ArrayList<>();
        //Truy vấnbệnh ánThông tin
        List<Parameter> parameters = SearchParamsBuilder.create()
                .eq("company_id", registration.getCompany().getId())
                .eq("doctor_id", registration.getDoctor().getId())
                .eq("registration_id", registration.getId())
                .build();
        List<MedicalRecord> medicalRecords = super.listAll(parameters, "");
        // C3: luot kham chua co ho so -> list rong, khong get(0) (500 truoc sua);
        // de medicalRecord null nhu nhanh allNewQuery da guard, frontend I2 da chiu null.
        if (!CollectionUtils.isEmpty(medicalRecords)) {
            outEvt.setMedicalRecord(medicalRecords.get(0));
        }
        //Truy vấnĐơn thuốcThông tin
        SearchParamsBuilder recipelParams = SearchParamsBuilder.create()
                .eq("company_id", registration.getCompany().getId())
                .eq("registration_id", registration.getId());
        if (!StringUtils.isNullOrEmpty(chargeStatus)) {
            recipelParams.eq("charge_status", chargeStatus);
        }
        List<RecipelInfo> recipelInfos = recipelInfoService.listAll(recipelParams.build(), "");
        if (!CollectionUtils.isEmpty(recipelInfos)) {
            // Truy vấn hàng loạt tất cả chi tiết đơn thuốc(Tối ưu hóa N+1 Truy vấn)
            List<String> recipelInfoIds = recipelInfos.stream()
                    .map(RecipelInfo::getId)
                    .collect(Collectors.toList());
            Map<String, List<RecipelDetail>> detailsMap = recipelDetailService.getByRecipelInfoIds(recipelInfoIds);

            for (RecipelInfo recipelInfo : recipelInfos) {
                //ĐặtĐơn thuốcThông tin
                RecipelInfoEvt recipelInfoEvt = new RecipelInfoEvt();
                recipelInfoEvt.setRecipelInfo(recipelInfo);
                //Lấy chi tiết đơn thuốc từ kết quả truy vấn hàng loạt
                List<RecipelDetail> recipelDetails = detailsMap.getOrDefault(recipelInfo.getId(), Collections.emptyList());
                if (!CollectionUtils.isEmpty(recipelDetails)) {
                    for (RecipelDetail recipelDetail : recipelDetails) {
                        recipelDetail.setRecipelInfo(recipelInfo);
                        recipelDetail.setDrugStuffId(this.getDrugStuffEvt(recipelDetail));
                    }
                }
                recipelInfoEvt.setRecipelDetailEvtList(recipelDetails);
                recipelInfoEvtList.add(recipelInfoEvt);
            }
        }

        outEvt.setRecipelInfoEvtList(recipelInfoEvtList);
        logger.info("in ấnratham:{}", JSONObject.toJSON(outEvt));
        return outEvt;
    }

    public RecipelInfoEvt queryByInfoId(String reciptInfoId) {
        List<Parameter> parameters = SearchParamsBuilder.createWithTenant(SessionUtils.getLoginTenantId())
                .eq("recipel_info_id", reciptInfoId)
                .build();
        List<RecipelDetail> recipelDetails = recipelDetailService.listAll(parameters, "");
        RecipelInfo recipelInfo = recipelInfoService.get(reciptInfoId);
        //ĐặtĐơn thuốcThông tin
        RecipelInfoEvt recipelInfoEvt = new RecipelInfoEvt();
        recipelInfoEvt.setRecipelInfo(recipelInfo);
        if (null != recipelDetails && !recipelDetails.isEmpty()) {
            for (RecipelDetail recipelDetail : recipelDetails) {

                //Truy vấnThông tin thuốc
                DrugStuffEvt drugStuffEvt = new DrugStuffEvt();
                if (BizConstants.RECIPEL_TYPE_WESTERN.equals(recipelInfo.getRecipelType().getValue()) || BizConstants.RECIPEL_TYPE_CHINESE.equals(recipelInfo.getRecipelType().getValue()) || BizConstants.RECIPEL_TYPE_INFUSION.equals(recipelInfo.getRecipelType().getValue())) {
                    //Thuốc đông y, Thuốc tây, Đơn truyền dịch tra cứu thông tin thuốc
                    Drug drug = drugService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                    drugStuffEvt.setDrugStuffId(drug.getId());
                    drugStuffEvt.setName(drug.getGoodsName());
                    drugStuffEvt.setPrice(drug.getPrice());
                    drugStuffEvt.setDosisUnit(drug.getDosisUnit());
                    drugStuffEvt.setPack(drug.getPack());
                    drugStuffEvt.setDrug(drug);
                } else if (BizConstants.RECIPEL_TYPE_OTHER.equals(recipelInfo.getRecipelType().getValue())) {
                    //Truy vấn thông tin hạng mục khám chữa bệnh
                    CostItem costItem = costItemService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                    drugStuffEvt.setDrugStuffId(costItem.getId());
                    drugStuffEvt.setName(costItem.getItemName());
                    drugStuffEvt.setPrice(costItem.getSalePrice());
                    drugStuffEvt.setDosisUnit(costItem.getUnit());
                    drugStuffEvt.setCostItem(costItem);
                } else if (BizConstants.RECIPEL_TYPE_PATENT.equals(recipelInfo.getRecipelType().getValue())) {
                    //Truy vấnThông tin vật tư&Thông tin thuốc là khác của Thông tin
                    Drug drug = drugService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                    if (null == drug) {
                        Stuff stuff = stuffService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                        drugStuffEvt.setDrugStuffId(stuff.getId());
                        drugStuffEvt.setName(stuff.getName());
                        drugStuffEvt.setPrice(stuff.getPriceOutSell());
                        drugStuffEvt.setDosisUnit(stuff.getPackUnit());
                        drugStuffEvt.setPack(stuff.getPackUnit());
                        drugStuffEvt.setStuff(stuff);
                    } else {
                        drugStuffEvt.setDrugStuffId(drug.getId());
                        drugStuffEvt.setName(drug.getGoodsName());
                        drugStuffEvt.setPrice(drug.getPrice());
                        drugStuffEvt.setDosisUnit(drug.getDosisUnit());
                        drugStuffEvt.setPack(drug.getPack());
                        drugStuffEvt.setDrug(drug);
                    }
                } else if (BizConstants.RECIPEL_TYPE_EXTERNAL.equals(recipelInfo.getRecipelType().getValue())) {
                    //bán lẻĐơn thuốc
                    String retailType = recipelDetail.getRetailType();
                    if ("0".equals(retailType) || "1".equals(retailType) || "2".equals(retailType)) {
                        //Thuốc đông y, Thuốc tây, Đơn truyền dịch tra cứu thông tin thuốc
                        Drug drug = drugService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                        drugStuffEvt.setDrugStuffId(drug.getId());
                        drugStuffEvt.setName(drug.getGoodsName());
                        drugStuffEvt.setPrice(drug.getPrice());
                        drugStuffEvt.setDosisUnit(drug.getDosisUnit());
                        drugStuffEvt.setPack(drug.getPack());
                        drugStuffEvt.setDrug(drug);
                    } else if ("3".equals(retailType)) {
                        //Truy vấn thông tin hạng mục khám chữa bệnh
                        CostItem costItem = costItemService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                        drugStuffEvt.setDrugStuffId(costItem.getId());
                        drugStuffEvt.setName(costItem.getItemName());
                        drugStuffEvt.setPrice(costItem.getSalePrice());
                        drugStuffEvt.setDosisUnit(costItem.getUnit());
                        drugStuffEvt.setCostItem(costItem);
                    } else if ("4".equals(retailType)) {
                        //Truy vấnThông tin vật tư&Thông tin thuốc là khác của Thông tin
                        Drug drug = drugService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                        if (null == drug) {
                            Stuff stuff = stuffService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                            drugStuffEvt.setDrugStuffId(stuff.getId());
                            drugStuffEvt.setName(stuff.getName());
                            drugStuffEvt.setPrice(stuff.getPriceOutSell());
                            drugStuffEvt.setDosisUnit(stuff.getPackUnit());
                            drugStuffEvt.setPack(stuff.getPackUnit());
                            drugStuffEvt.setStuff(stuff);
                        } else {
                            drugStuffEvt.setDrugStuffId(drug.getId());
                            drugStuffEvt.setName(drug.getGoodsName());
                            drugStuffEvt.setPrice(drug.getPrice());
                            drugStuffEvt.setDosisUnit(drug.getDosisUnit());
                            drugStuffEvt.setPack(drug.getPack());
                            drugStuffEvt.setDrug(drug);
                        }
                    }
                }
                recipelDetail.setDrugStuffId(drugStuffEvt);
            }
        }
        recipelInfoEvt.setRecipelDetailEvtList(recipelDetails);
        logger.info("in ấnratham:{}", JSONObject.toJSON(recipelInfoEvt));
        return recipelInfoEvt;
    }

    public List<RecipelInfoEvt> getRecipelInfoEvts(List<RecipelInfo> recipelInfos) {
        List<RecipelInfoEvt> recipelInfoEvtList = new ArrayList<>();
        if (null != recipelInfos && !recipelInfos.isEmpty()) {
            // Truy vấn hàng loạt tất cả chi tiết đơn thuốc(Tối ưu hóa N+1 Truy vấn)
            List<String> recipelInfoIds = recipelInfos.stream()
                    .map(RecipelInfo::getId)
                    .collect(Collectors.toList());
            Map<String, List<RecipelDetail>> detailsMap = recipelDetailService.getByRecipelInfoIds(recipelInfoIds);

            for (RecipelInfo recipelInfo : recipelInfos) {
                RecipelInfoEvt recipelInfoEvt = new RecipelInfoEvt();
                recipelInfoEvt.setRecipelInfo(recipelInfo);
                // Lấy chi tiết đơn thuốc từ kết quả truy vấn hàng loạt
                List<RecipelDetail> recipelDetails = detailsMap.getOrDefault(recipelInfo.getId(), Collections.emptyList());
                if (!CollectionUtils.isEmpty(recipelDetails)) {
                    for (RecipelDetail recipelDetail : recipelDetails) {
                        recipelDetail.setDrugStuffId(this.getDrugStuffEvt(recipelDetail));
                    }
                }
                recipelInfoEvt.setRecipelDetailEvtList(recipelDetails);
                recipelInfoEvtList.add(recipelInfoEvt);
            }
        }
        return recipelInfoEvtList;
    }

    public List<MedicalRecord> listMedicalRecordByChargeStatus(String chargeStatus) {
        return this.dao.listMedicalRecordByChargeStatus(chargeStatus, SessionUtils.getLoginTenantId());
    }

    // I1 (iteration 56): sys_file KHONG co cot company_id nen guard xoa file
    // bang object_id theo mau R3 (iter41), dat TRUOC moi upload/xoa/ghi cua
    // allNewSave de giao dich @Transactional rollback toan bo khi 400.
    // Tap hop hop le cua mot luot kham (xac minh DB live: file that khong
    // con khop benh an hien tai do du lieu cu, nhung code upload chung
    // minh 2 khoa — fileIdUploads luu object_id = medicalRecord.fileId
    // (IdGen.uuid so, Long.parseLong duoc), medicalFiles luu object_id =
    // registration id): fileId dang gui, fileId cua benh an da luu cho
    // luot nay (UI gui lai fileId cu khi xoa file dinh kem cu), va
    // registration id. File la co object_id ngoai tap nay -> 400; file
    // khong ton tai giu hanh vi cu (bo qua, R3). Khong sua
    // SysFileService.delete generic vi cac caller khac (Clinic/Company/
    // User/InspectionCheck) dung object_id theo nghia rieng — chi them
    // SysFileService.verifyDeleteIdsObjectIds moi va goi tai day.
    private void guardExamDeleteFileIds(String[] delFileIds, List<String> deleteFileIdList, String registrationId, String incomingFileId) {
        Set<String> allowed = new HashSet<>();
        if (registrationId != null && !registrationId.isEmpty()) {
            allowed.add(registrationId);
            MedicalRecord stored = medicalRecordDao.getByRegistrationId(registrationId);
            if (stored != null && stored.getFileId() != null && !stored.getFileId().isEmpty()) {
                allowed.add(stored.getFileId());
            }
        }
        if (incomingFileId != null && !incomingFileId.isEmpty()) {
            allowed.add(incomingFileId);
        }
        if (delFileIds != null && delFileIds.length > 0) {
            sysFileService.verifyDeleteIdsObjectIds(Arrays.asList(delFileIds), allowed);
        }
        if (deleteFileIdList != null && !deleteFileIdList.isEmpty()) {
            sysFileService.verifyDeleteIdsObjectIds(deleteFileIdList, allowed);
        }
    }

    @Transactional(readOnly = false)
    public String allNewSave(ReceptionEvt receptionEvt, MultipartFile[] fileIdUploads, String[] delFileIds,MultipartFile[] medicalFiles) throws Exception {

        //kiểm kê giờKhông thểtiến hành dònghoàn thành tiếp nhận của thao tác
        Company company = SessionUtils.getUser().getCompany();
        List<InventoryVerification> inventoryVerifications = inventoryVerificationService.getByCompanyId(company.getId());
        if (!CollectionUtils.isEmpty(inventoryVerifications)) {
            throw new ServiceException("đúngtạihành nghề dòngkiểm kêthao tác,không thể tiến hành dòngtiếp nhận");
        }
        //Tiếp nhận thông thường
        if (InstantPatient.NORMAL.getCode() == receptionEvt.getType()) {
            if (!org.springframework.util.StringUtils.hasText(receptionEvt.getId())) {
                throw new ServiceException("đăng ký tiếp nhận Thông tin Không thể là trống.");
            }
            Registration registration = this.registrationService.get(receptionEvt.getId());
            if (Objects.isNull(registration)) {
                throw new ServiceException("đăng ký tiếp nhận Thông tin Không tồn tại.");
            }
            if (!(Objects.equals(registration.getStatus().getValue(), BizConstants.REG_STATUS_PENDING) || Objects.equals(registration.getStatus().getValue(), BizConstants.REG_STATUS_VISITED))) {
                throw new ServiceException("đăng ký tiếp nhận Thông tin không có [chờ tiếp nhận] hoặc [Đã hoàn thành tiếp nhận] Trạng thái.");
            }
            // I1: guard ca 2 danh sach xoa cua NORMAL (delFileIds param +
            // deleteFileIdList trong entity JSON) TRUOC moi upload/xoa/ghi.
            this.guardExamDeleteFileIds(delFileIds, receptionEvt.getDeleteFileIdList(), receptionEvt.getId(), receptionEvt.getMedicalRecord() == null ? null : receptionEvt.getMedicalRecord().getFileId());
            if (!org.springframework.util.StringUtils.hasText(receptionEvt.getMedicalRecord().getFileId())) {
                receptionEvt.getMedicalRecord().setFileId(IdGen.uuid());
            }
            if (!ArrayUtils.isEmpty(fileIdUploads)) {
                //LưuTải lên tệp đính kèmThông tin
                this.sysFileService.changeAndSaveSysFileList(fileIdUploads, receptionEvt.getMedicalRecord().getFileId());
            }
            if (!ArrayUtils.isEmpty(delFileIds)) {
                //XóaCầnhủy của tệp đính kèmThông tin
                this.sysFileService.delete(delFileIds);
            }
            MedicalRecord medicalRecord = receptionEvt.getMedicalRecord();
            medicalRecord.setRegistration(registration);
            //LưuCập nhậtĐiền ca bệnhThông tin
            medicalRecord = this.save(medicalRecord);
            //Lưubệnh án của tệp đính kèm
            if (CollUtil.isNotEmpty(receptionEvt.getDeleteFileIdList())) {
                this.sysFileService.delete(receptionEvt.getDeleteFileIdList().toArray(new String[]{}));
            }
            if (ArrayUtils.isNotEmpty(medicalFiles)) {
                this.sysFileService.changeAndSaveSysFileList(medicalFiles, receptionEvt.getRegistration().getId());
            }
                //Lưu thông tin đơn thuốc
            this.saveRecipelInfoEvt(receptionEvt.getRecipelInfoEvtList(), receptionEvt.getId());

            //Cập nhậtLưuđăng kýThông tin
            if (Objects.equals(registration.getStatus().getValue(), BizConstants.REG_STATUS_PENDING)) {
                DictItem statusDictItem = new DictItem();
                statusDictItem.setValue(BizConstants.REG_STATUS_VISITED);
                registration.setStatus(statusDictItem);
                registration.setReceptionEndDate(new Date());
            }
            if(!receptionEvt.getRecipelInfoEvtList().isEmpty() && receptionEvt.getRecipelInfoEvtList().get(0).getRecipelInfo().getIsPre()!=null&&receptionEvt.getRecipelInfoEvtList().size() == 1 && receptionEvt.getRecipelInfoEvtList().get(0).getRecipelInfo().getIsPre()){
                //Số đơn, số đơn thuốc điện tử kết thúc quy trình khám bệnh
                registration.setIsPre(true);
            }
            registration.setTreatType(receptionEvt.getRegistration().getTreatType());
            registration.setInfectType(receptionEvt.getRegistration().getInfectType());
            registration.setBreathe(receptionEvt.getPatient().getBreathe());
            registration.setTemperature(receptionEvt.getPatient().getTemperature());
            registration.setPulse(receptionEvt.getPatient().getPulse());
            registration.setBloodPressure(receptionEvt.getPatient().getBloodPressure());
            registration.setMorbidityTime(receptionEvt.getRegistration().getMorbidityTime());
            receptionEvt.setRegistration(this.registrationService.save(registration));
            receptionEvt.setId(receptionEvt.getRegistration().getId());
        } else if (InstantPatient.INSTANT.getCode() == receptionEvt.getType() || InstantPatient.RETAIL.getCode() == receptionEvt.getType()) {
            if (!org.springframework.util.StringUtils.hasText(receptionEvt.getId())) {
                receptionEvt.setId(IdGen.uuid());
            }
            Registration registration = this.registrationService.get(receptionEvt.getId());
            if (Objects.isNull(registration)) {
                if (Objects.isNull(receptionEvt.getPatient())) {
                    throw new ServiceException("[Bệnh nhân cơ bản Thông tin] Không được để trống.");
                }
                //Xử lýcơ bảnThông tin(tức làThông tin bệnh nhân)
                if (!org.springframework.util.StringUtils.hasText(receptionEvt.getPatient().getId()) || Objects.isNull(this.patientService.get(receptionEvt.getPatient().getId()))) {
                    receptionEvt.getPatient().setId(null);
                    // ITER55 H1: truoc day goi saveSuper (= super.save) nen bo qua
                    // kiem tra trung SDT/CCCD cua PatientService.save — tiep nhan
                    // nhanh tao duoc 2 ho so cung SDT/CCCD trong cung tenant,
                    // trong khi v1/allSave (F1) va /patient/save (E1) deu chan.
                    // Nay ep company theo session tenant dung mau F1 (luot kham
                    // phia duoi cung ep session tenant) roi goi save de kiem tra
                    // trung chay dung tenant; session rong + thieu company thi
                    // 400 ro rang kieu C2, khong de tao benh nhan company NULL
                    // nhu pre-fix. Khong doi PatientService.save generic, khong
                    // doi duong luot da co, khong doi guard get() cua F3.
                    String h1Tenant = SessionUtils.getLoginTenantId();
                    if (h1Tenant != null && !h1Tenant.trim().isEmpty() && !"null".equals(h1Tenant)) {
                        receptionEvt.getPatient().setCompany(new Company(h1Tenant));
                    } else if (receptionEvt.getPatient().getCompany() == null || receptionEvt.getPatient().getCompany().getId() == null
                            || receptionEvt.getPatient().getCompany().getId().trim().isEmpty()) {
                        throw new ServiceException("Thiếu thông tin phòng khám (company), không xác định được tenant");
                    }
                    receptionEvt.setPatient(this.patientService.save(receptionEvt.getPatient()));
                }
                receptionEvt.setPatient(this.patientService.get(receptionEvt.getPatient().getId()));

                //Registration registrationEntity = new Registration();
                Registration registrationEntity = receptionEvt.getRegistration();
                registrationEntity.setId(null);
                registrationEntity.setCompany(SessionUtils.getLoginTenant());
                //TODO:KhoaXử lý?  lấyNgười dùngThông tinbên trongNgười dùngcấu hình của Khoa(mụctrướckhôngphương pháptrực tiếpLấy, do là Người dùngbên trong có lưu của Tên khoa, màkhông có KhoaID, Không thể tạo quan hệ ánh xạ)
                registrationEntity.setClinicOffice(null);
                registrationEntity.setDoctor(SessionUtils.getUser());
                registrationEntity.setPatientId(receptionEvt.getPatient());
                if (InstantPatient.INSTANT.getCode() == receptionEvt.getType()) {
                    registrationEntity.setTreatType(receptionEvt.getRegistration().getTreatType());
                    registrationEntity.setInfectType(receptionEvt.getRegistration().getInfectType());
                    DictItem sourceDictItem = new DictItem();
                    sourceDictItem.setValue(BizConstants.REG_SOURCE_QUICK);
                    registrationEntity.setSource(sourceDictItem);
                    registrationEntity.setReceptionStartDate(new Date());
                    registrationEntity.setReceptionEndDate(new Date());
                    registrationEntity.setPulse(receptionEvt.getRegistration().getPulse());
                    registrationEntity.setBreathe(receptionEvt.getRegistration().getBreathe());
                    registrationEntity.setTemperature(receptionEvt.getRegistration().getTemperature());
                    registrationEntity.setFreeRegistrationFee("1");
                    registrationEntity.setRegistrationFee(BigDecimal.ZERO);
                    DictItem statusDictItem = new DictItem();
                    statusDictItem.setValue(BizConstants.REG_STATUS_VISITED);
                    registrationEntity.setStatus(statusDictItem);
                } else if (InstantPatient.RETAIL.getCode() == receptionEvt.getType()) {
                    //TODO: điều trịLoại   Mặc địnhKhám lần đầu
                    DictItem dictItem = new DictItem();
                    dictItem.setValue(BizConstants.TREAT_TYPE_NORMAL);
                    registrationEntity.setTreatType(dictItem);
                    DictItem dictItemTo = new DictItem();
                    dictItemTo.setValue(BizConstants.INFECT_TYPE_NONE);
                    registrationEntity.setInfectType(dictItemTo);
                    DictItem sourceDictItem = new DictItem();
                    sourceDictItem.setValue(BizConstants.REG_SOURCE_RETAIL);
                    registrationEntity.setSource(sourceDictItem);
                    registrationEntity.setReceptionStartDate(new Date());
                    registrationEntity.setReceptionEndDate(new Date());
                    registrationEntity.setPulse(receptionEvt.getRegistration().getPulse());
                    registrationEntity.setBreathe(receptionEvt.getRegistration().getBreathe());
                    registrationEntity.setTemperature(receptionEvt.getRegistration().getTemperature());
                    registrationEntity.setFreeRegistrationFee("1");
                    registrationEntity.setRegistrationFee(BigDecimal.ZERO);
                }

                receptionEvt.setRegistration(this.registrationService.save(registrationEntity));
                // ITER54 G1: khoi INSTANT phia sau dung bien cuc bo registration
                // (deref :647); voi tiep nhan moi thi registration van null neu
                // khong gan lai bang luot vua luu. Chi gan trong khoi isNull
                // nay — duong luot da co giu nguyen dung registration DB.
                registration = receptionEvt.getRegistration();
            }

            if (InstantPatient.INSTANT.getCode() == receptionEvt.getType()) {
                registration.setTreatType(receptionEvt.getRegistration().getTreatType());
                registration.setInfectType(receptionEvt.getRegistration().getInfectType());
                receptionEvt.setRegistration(this.registrationService.save(registration));

                if (!org.springframework.util.StringUtils.hasText(receptionEvt.getMedicalRecord().getFileId())) {
                    receptionEvt.getMedicalRecord().setFileId(IdGen.uuid());
                }
                // I1: guard delFileIds cua INSTANT TRUOC upload/xoa; luot/
                // benh nhan vua tao o tren rollback theo giao dich neu 400.
                // (Nhanh INSTANT khong xoa deleteFileIdList nen chi guard
                // delFileIds; fileId o day la gia tri sau blank-fill.)
                this.guardExamDeleteFileIds(delFileIds, null, registration.getId(), receptionEvt.getMedicalRecord().getFileId());
                if (!ArrayUtils.isEmpty(fileIdUploads)) {
                    //LưuTải lên tệp đính kèmThông tin
                    this.sysFileService.changeAndSaveSysFileList(fileIdUploads, receptionEvt.getMedicalRecord().getFileId());
                }
                if (!ArrayUtils.isEmpty(delFileIds)) {
                    //XóaCầnhủy của tệp đính kèmThông tin
                    this.sysFileService.delete(delFileIds);
                }
                MedicalRecord medicalRecord = receptionEvt.getMedicalRecord();
                medicalRecord.setRegistration(registration);
                //LưuCập nhậtĐiền ca bệnhThông tin
                this.save(medicalRecord);
            }

            //Lưu thông tin đơn thuốc
            this.saveRecipelInfoEvt(receptionEvt.getRecipelInfoEvtList(), receptionEvt.getId());
        }

        return receptionEvt.getId();
    }

    @Transactional(readOnly = false)
    public void saveRecipelInfoEvt(List<RecipelInfoEvt> recipelInfoEvtList, String registrationId) {
        /*
        * Đơn thuốcThông tin là trống, trực tiếpkhôngdùngLưu
        * */
        if (CollectionUtils.isEmpty(recipelInfoEvtList)) {
            return;
            //throw new RuntimeException("Đơn thuốc Thông tin Không thể là trống.");
        }


        Registration registration = this.registrationService.get(registrationId);
        for (int i = 0; i < recipelInfoEvtList.size(); i++) {
            RecipelInfoEvt recipelInfoEvt = recipelInfoEvtList.get(i);
            RecipelInfo recipelInfo = recipelInfoEvt.getRecipelInfo();
            if (ObjectUtil.isNull(recipelInfo.getChronicDisease()) || !StrUtil.equals(BizConstants.RECIPEL_TYPE_WESTERN,recipelInfo.getRecipelType().getValue())) {
                recipelInfo.setChronicDisease(Boolean.FALSE);
            }
            if (ObjectUtil.isNull(recipelInfo.getChronicDisease()) || !StrUtil.equals(BizConstants.RECIPEL_TYPE_WESTERN,recipelInfo.getRecipelType().getValue())) {
                recipelInfo.setIsPre(Boolean.FALSE);
            }
            // DS59: K3-class gap on the v2 path — FinishVisit sends no
            // isFollowUp, but recipel_info.is_follow_up is NOT NULL (the
            // insert always lists the column, so an explicit NULL 500s
            // even though the column has DEFAULT 0). Default to 0 when
            // missing, keep the caller-sent value otherwise (same rule
            // as K3 on v1 allSave; seq is already set i+1 in both
            // branches below, so no seq change here).
            if (null == recipelInfo.getIsFollowUp()) {
                recipelInfo.setIsFollowUp(0);
            }
            if( recipelInfo.getIsPre()!= null && recipelInfo.getIsPre()){
                //điện tửĐơn thuốcđặc biệtXử lý
                List<RecipelDetail> recipelDetailEvtList = recipelInfoEvt.getRecipelDetailEvtList();
                if (CollectionUtils.isEmpty(recipelDetailEvtList)) {
                    throw new ServiceException("[" + recipelInfo.getName() + "]Chi tiết không được để trống.");
                }
                if (org.springframework.util.StringUtils.hasText(recipelInfo.getId())) {
                    RecipelInfo queryRecipelInfo = this.recipelInfoService.get(recipelInfo.getId());
                    if (Objects.isNull(queryRecipelInfo)) {
                        recipelInfo.setId(null);
                    }
                    //Đơn thuốc đã hủy khi sửa và lưu thì không xử lý
                    if (Objects.nonNull(queryRecipelInfo) && Objects.equals(queryRecipelInfo.getStatus(), -1)) {
                        continue;
                    }
                    //Tiếp nhận xong, Và trường hợp đã thu phí xong hoặc đã hoàn phí thì không xử lý
                    if (Objects.nonNull(queryRecipelInfo) && Objects.equals(queryRecipelInfo.getStatus(), 1)
                            && (Objects.equals(queryRecipelInfo.getChargeStatus(), 1) || Objects.equals(queryRecipelInfo.getChargeStatus(), -1))) {
                        continue;
                    }
                }

                if (!org.springframework.util.StringUtils.hasText(recipelInfo.getId())) {
                    String recipelNo = serialNoUtils.generateSerialNo(org.apache.commons.lang3.StringUtils.EMPTY);
                    recipelInfo.setCode(recipelNo);
                }
                recipelInfo.setRegistration(registration);
                recipelInfo.setSeq(i + 1);
                recipelInfo.setStatus(1);
                if (Objects.equals(registration.getSource().getValue(), BizConstants.REG_SOURCE_RETAIL)) {
                    recipelInfo.setChargeStatus(1);
                    recipelInfo.setIsPay("1");  //?Có thanh toán không, có phải frontend truyền không
                } else {
                    recipelInfo.setChargeStatus(0);
                    recipelInfo.setIsPay("0");
                }
                //ĐặtĐơn thuốcThu phí và phát thuốcTrạng thái điện tửĐơn thuốckhôngtheosautiếp tụcluồngquy trình
                recipelInfo.setChargeStatus(2);
                recipelInfo.setIsPay("2");
                recipelInfo.setDispensionStatus(2);

                // I2: don thuoc thuoc ve luot kham: insert moi ep company theo
                // registration (da get-verified OWN); update giu nguyen de guard
                // payload-company cua CrudService.save van 400 nhu cu.
                if (!org.springframework.util.StringUtils.hasText(recipelInfo.getId())
                        && registration.getCompany() != null
                        && org.springframework.util.StringUtils.hasText(registration.getCompany().getId())) {
                    recipelInfo.setCompany(registration.getCompany());
                }
                //Lưu bảng chính của đơn thuốc
                RecipelInfo recipelInfoEntity = this.recipelInfoService.save(recipelInfo);
                //LưuĐơn thuốcChi tiếtbảng
                List<RecipelDetail> recipelDetails = this.recipelDetailService.getByRecipelInfoId(recipelInfoEntity.getId());
                if (!CollectionUtils.isEmpty(recipelDetails)) {
                    this.recipelDetailService.batchDelete(recipelDetails);
                }
                for (RecipelDetail recipelDetail : recipelDetailEvtList) {
                    //nên thangthực thangđều là 0
                    recipelDetail.setActualPayment(BigDecimal.valueOf(0.00));
                    recipelDetail.setAllFee(BigDecimal.valueOf(0.00));
                    recipelDetail.setId(null);
                    recipelDetail.setRecipelInfo(recipelInfoEntity);
                    // I2: chi tiet luon insert (id vua null) nen ep company theo
                    // luot kham, cung mau saveRecipelInfoEvtTo; neu khong, chi
                    // tiet van roi vao tenant la du recipelInfo da ep.
                    if (registration.getCompany() != null
                            && org.springframework.util.StringUtils.hasText(registration.getCompany().getId())) {
                        recipelDetail.setCompany(registration.getCompany());
                    }
                    // Nếu frontend không truyềnminTotal(Tổng lượng đơn vị bào chế), Thì dùngtotalLàm giá trị mặc định
                    if (recipelDetail.getMinTotal() == null || recipelDetail.getMinTotal() == 0) {
                        recipelDetail.setMinTotal(recipelDetail.getTotal());
                    }
                    if (recipelDetail.getInfuseGroup() != null) {
                        recipelDetail.setStuffType("2");
                    }
                    this.recipelDetailService.save(recipelDetail);
                }
            } else {
                // Đặtnên thangGiáMặc định là 0
                for (RecipelDetail recipelDetail : recipelInfoEvt.getRecipelDetailEvtList()) {
                    recipelDetail.setActualPayment(BigDecimal.valueOf(0.00));
                }
                List<RecipelDetail> recipelDetailEvtList = recipelInfoEvt.getRecipelDetailEvtList();
                if (CollectionUtils.isEmpty(recipelDetailEvtList)) {
                    throw new ServiceException("[" + recipelInfo.getName() + "]Chi tiết không được để trống.");
                }
                if (org.springframework.util.StringUtils.hasText(recipelInfo.getId())) {
                    RecipelInfo queryRecipelInfo = this.recipelInfoService.get(recipelInfo.getId());
                    if (Objects.isNull(queryRecipelInfo)) {
                        recipelInfo.setId(null);
                    }
                    //Đơn thuốc đã hủy khi sửa và lưu thì không xử lý
                    if (Objects.nonNull(queryRecipelInfo) && Objects.equals(queryRecipelInfo.getStatus(), -1)) {
                        continue;
                    }
                    //Tiếp nhận xong, Và trường hợp đã thu phí xong hoặc đã hoàn phí thì không xử lý
                    if (Objects.nonNull(queryRecipelInfo) && Objects.equals(queryRecipelInfo.getStatus(), 1)
                            && (Objects.equals(queryRecipelInfo.getChargeStatus(), 1) || Objects.equals(queryRecipelInfo.getChargeStatus(), -1))) {
                        continue;
                    }
                }

                if (!org.springframework.util.StringUtils.hasText(recipelInfo.getId())) {
                    String recipelNo = serialNoUtils.generateSerialNo(org.apache.commons.lang3.StringUtils.EMPTY);
                    recipelInfo.setCode(recipelNo);
                }

                recipelInfo.setRegistration(registration);
                recipelInfo.setSeq(i + 1);
                recipelInfo.setStatus(1);
                if (Objects.equals(registration.getSource().getValue(), BizConstants.REG_SOURCE_RETAIL)) {
                    recipelInfo.setChargeStatus(1);
                    recipelInfo.setIsPay("1");  //?Có thanh toán không, có phải frontend truyền không
                } else {
                    recipelInfo.setChargeStatus(0);
                    recipelInfo.setIsPay("0");
                }
                // I2: nhu nhanh isPre — insert moi ep company theo registration.
                if (!org.springframework.util.StringUtils.hasText(recipelInfo.getId())
                        && registration.getCompany() != null
                        && org.springframework.util.StringUtils.hasText(registration.getCompany().getId())) {
                    recipelInfo.setCompany(registration.getCompany());
                }
                //Lưu bảng chính của đơn thuốc
                RecipelInfo recipelInfoEntity = this.recipelInfoService.save(recipelInfo);

                List<RecipelDetail> recipelDetails = this.recipelDetailService.getByRecipelInfoId(recipelInfoEntity.getId());
                if (!CollectionUtils.isEmpty(recipelDetails)) {
                    this.recipelDetailService.batchDelete(recipelDetails);
                }
                for (RecipelDetail recipelDetail : recipelDetailEvtList) {
                    recipelDetail.setId(null);
                    recipelDetail.setRecipelInfo(recipelInfoEntity);
                    // I2: nhu nhanh isPre — chi tiet luon insert nen ep company theo luot kham.
                    if (registration.getCompany() != null
                            && org.springframework.util.StringUtils.hasText(registration.getCompany().getId())) {
                        recipelDetail.setCompany(registration.getCompany());
                    }
                    // Nếu frontend không truyềnminTotal(Tổng lượng đơn vị bào chế), Thì dùngtotalLàm giá trị mặc định
                    if (recipelDetail.getMinTotal() == null || recipelDetail.getMinTotal() == 0) {
                        recipelDetail.setMinTotal(recipelDetail.getTotal());
                    }
                    if (recipelDetail.getInfuseGroup() != null) {
                        recipelDetail.setStuffType("2");
                    }
                    this.recipelDetailService.save(recipelDetail);
                }

                //TODO:độngTồn khothao tác
                this.medicinalStorageControlService.preOccupyStock(recipelInfoEntity);
            }
        }
    }

    @Transactional(readOnly = false)
    public void saveRecipelInfoEvtTo(List<RecipelInfoEvt> recipelInfoEvtList,String id) {

        // Lấytừ xakhámkhámtrị liệuBản ghi
        RemoteDiagnosisTreatment entity = remoteDiagnosisTreatmentService.diagnosisById(id);
        // DS88: a missing remote row (0 live rows in the DB) NPEd at
        // entity.getRegistrationId() below -> 400 (K4 pattern in saveAdd).
        if (entity == null) {
            throw new ServiceException("Không tìm thấy chẩn đoán từ xa");
        }
        /*
         * Đơn thuốcThông tin là trống, trực tiếpkhôngdùngLưu
         * */
        if (CollectionUtils.isEmpty(recipelInfoEvtList)) {
            //return;
           throw new ServiceException("Đơn thuốc Thông tin Không thể là trống.");
        }
        String registrationId = entity.getRegistrationId();
        // Lấyđăng kýThông tin
        Registration registration = this.registrationService.get(registrationId);
        // DS88: a stale registration_id on the remote row NPEd at
        // registration.getStatus() below -> 400 (same K4 message).
        if (registration == null) {
            throw new ServiceException("Không tìm thấy lượt khám của chẩn đoán từ xa");
        }

        if (Objects.equals(registration.getStatus().getValue(), BizConstants.REG_STATUS_PENDING))
        {
            DictItem statusDictItem = new DictItem();
            statusDictItem.setValue(BizConstants.REG_STATUS_VISITED);
            registration.setStatus(statusDictItem);
            registration.setReceptionEndDate(new Date());
        }
        // Cập nhậtđăng kýThông tin
        this.registrationService.save(registration);

        for (int i = 0; i < recipelInfoEvtList.size(); i++) {
            // Lấy thông tin đơn thuốc và Chi tiết
            RecipelInfoEvt recipelInfoEvt = recipelInfoEvtList.get(i);
            // Lấy thông tin đơn thuốc
            RecipelInfo recipelInfo = recipelInfoEvt.getRecipelInfo();
            recipelInfo.setCompany(entity.getCompany());
            // Đặtnên thangGiáMặc định là 0
            for (RecipelDetail recipelDetail:recipelInfoEvt.getRecipelDetailEvtList()){
                recipelDetail.setActualPayment(BigDecimal.valueOf(0.00));
            }
            // Lấy chi tiết đơn thuốc
            List<RecipelDetail> recipelDetailEvtList = recipelInfoEvt.getRecipelDetailEvtList();
            if (CollectionUtils.isEmpty(recipelDetailEvtList))
            {
                throw new ServiceException("[" + recipelInfo.getName() + "]Chi tiết không được để trống.");
            }
            if (org.springframework.util.StringUtils.hasText(recipelInfo.getId())) {
                RecipelInfo queryRecipelInfo = this.recipelInfoService.get(recipelInfo.getId());
                if (Objects.isNull(queryRecipelInfo)) {
                    recipelInfo.setId(null);
                }
                //Đơn thuốc đã hủy khi sửa và lưu thì không xử lý
                if (Objects.nonNull(queryRecipelInfo) && Objects.equals(queryRecipelInfo.getStatus(), -1))
                {
                    continue;
                }
                //Tiếp nhận xong, Và trường hợp đã thu phí xong hoặc đã hoàn phí thì không xử lý
                if (Objects.nonNull(queryRecipelInfo) && Objects.equals(queryRecipelInfo.getStatus(), 1)
                        && (Objects.equals(queryRecipelInfo.getChargeStatus(), 1) || Objects.equals(queryRecipelInfo.getChargeStatus(), -1))) {
                    continue;
                }
            }

            if (!org.springframework.util.StringUtils.hasText(recipelInfo.getId()))
            {
                String recipelNo = serialNoUtils.generateSerialNo(org.apache.commons.lang3.StringUtils.EMPTY);
                recipelInfo.setCode(recipelNo);
            }

            recipelInfo.setRegistration(registration);
            recipelInfo.setSeq(i+1);
            recipelInfo.setStatus(1);
            if (Objects.equals(registration.getSource().getValue(), BizConstants.REG_SOURCE_RETAIL)) {
                recipelInfo.setChargeStatus(1);
                recipelInfo.setIsPay("1");  //?Có thanh toán không, có phải frontend truyền không
            }
            else
            {
                recipelInfo.setChargeStatus(0);
                recipelInfo.setIsPay("0");
            }

            recipelInfo.setIsDispension("1");
            recipelInfo.setIsPay("0");
            recipelInfo.setDispensionStatus(0);
            if (BizConstants.TREAT_TYPE_NORMAL.equals(registration.getTreatType().getValue())){
                recipelInfo.setIsFollowUp(0);
            }else {
                recipelInfo.setIsFollowUp(1);
            }

            // J3: don tao moi (id rong sau khi doi chieu dong cu) ep company
            // theo luot kham da verify thay vi company dong remote; nhanh
            // update giu company remote nhu cu de CrudService.save verify.
            if (!org.springframework.util.StringUtils.hasText(recipelInfo.getId())
                    && registration.getCompany() != null
                    && org.springframework.util.StringUtils.hasText(registration.getCompany().getId())) {
                recipelInfo.setCompany(registration.getCompany());
            }
            //Lưu bảng chính của đơn thuốc
            RecipelInfo recipelInfoEntity = this.recipelInfoService.save(recipelInfo);

            List<RecipelDetail> recipelDetails = this.recipelDetailService.getByRecipelInfoId(recipelInfoEntity.getId());
            if (!CollectionUtils.isEmpty(recipelDetails)) {
                this.recipelDetailService.batchDelete(recipelDetails);
            }
            for (int k = 0; k < recipelDetailEvtList.size(); k++) {
                RecipelDetail recipelDetail = recipelDetailEvtList.get(k);
                // Truy vấnThông tin thuốc
                final Drug drug1 = drugService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                // ThêmThông tin thuốc
                if (null == drug1){
                    final Drug drug = new Drug();
                    final DictItem dictItem = new DictItem();
                    dictItem.setValue(recipelDetail.getStuffType().equals("2") ? BizConstants.MEDICAL_TYPE_WESTERN : BizConstants.MEDICAL_TYPE_PATENT);
                    drug.setId(recipelDetail.getDrugStuffId().getDrugStuffId());
                    drug.setGoodsName(recipelDetail.getDrugStuffId().getName());
                    drug.setPrice(recipelDetail.getDrugStuffId().getPrice());
                    drug.setRetailPrice(recipelDetail.getDrugStuffId().getRetailPrice());
                    drug.setDosisUnit(recipelDetail.getDrugStuffId().getDosisUnit());
                    drug.setPreparationUnit(recipelDetail.getDrugStuffId().getPreparationUnit());
                    drug.setPack(recipelDetail.getDrugStuffId().getPack());
                    drug.setIsUnpackSell(recipelDetail.getDrugStuffId().getIsUnpackSell());
                    drug.setType(dictItem);
                    drug.setStatus("1");
                    drug.setCreateDate(new Date());
                    drug.setUpdateDate(new Date());
                    drug.setCreateBy("từ xa Bệnh viện");
                    drug.setUpdateBy("từ xa Bệnh viện");
                    // K6: ep company theo luot kham da verify nhu J3 (chi khi
                    // luot co company id, neu khong giu hanh vi cu); khong doi
                    // DrugService.add() (duong tao thuoc chinh).
                    if (registration.getCompany() != null
                            && org.springframework.util.StringUtils.hasText(registration.getCompany().getId())) {
                        drug.setCompany(registration.getCompany());
                    }
                    drugService.addDurg(drug);
                }

                // J3: chi tiet luon insert (id null ngay sau day) nen ep
                // company theo luot kham da verify thay vi dong remote.
                if (registration.getCompany() != null
                        && org.springframework.util.StringUtils.hasText(registration.getCompany().getId())) {
                    recipelDetail.setCompany(registration.getCompany());
                } else {
                    recipelDetail.setCompany(entity.getCompany());
                }
                recipelDetail.setId(null);
                recipelDetail.setSeq(k);
                recipelDetail.setRecipelInfo(recipelInfoEntity);
                recipelDetail.setMinTotal(recipelDetail.getTotal());
                recipelDetail.setIsUnpackSell(1);
                recipelDetail.setIsExtra(0);
                if(recipelDetail.getInfuseGroup()!=null){
                    recipelDetail.setStuffType("2");
                }
                this.recipelDetailService.save(recipelDetail);
            }
        }
    }

    public ReceptionEvt allNewQuery(String registrationId, String[] recipelInfoIds)
    {
        ReceptionEvt receptionEvt = new ReceptionEvt();
        if (org.springframework.util.StringUtils.hasText(registrationId))
        {
            Registration registration = this.registrationService.get(registrationId);
            if (Objects.isNull(registration) || !org.springframework.util.StringUtils.hasText(registration.getId()))
            {
                return receptionEvt;
            }
            if (Objects.nonNull(registration.getPatientId()) && org.springframework.util.StringUtils.hasText(registration.getPatientId().getId()))
            {
                receptionEvt.setPatient(this.patientService.get(registration.getPatientId().getId()));
            }

            //tiếp nhậnLoại:0-Tiếp nhận thông thường  1-Tiếp nhận nhanh  2-Thu phí bán lẻ
            if (Objects.equals(registration.getSource().getValue(), BizConstants.REG_SOURCE_QUICK))
            {
                receptionEvt.setType(1);
            }
            else if (Objects.equals(registration.getSource().getValue(), BizConstants.REG_SOURCE_RETAIL))
            {
                receptionEvt.setType(2);
            }
            else {
                receptionEvt.setType(0);
            }

            receptionEvt.setId(registration.getId());
            receptionEvt.setRegistration(registration);

            List<Parameter> parameters = SearchParamsBuilder.create()
                    .eq("registration_id", registration.getId())
                    .build();
            //Truy vấnbệnh ánThông tin
            List<MedicalRecord> medicalRecordList = super.listAll(parameters, org.apache.commons.lang3.StringUtils.EMPTY);
            if (!CollectionUtils.isEmpty(medicalRecordList))
            {
                receptionEvt.setMedicalRecord(medicalRecordList.get(0));
                receptionEvt.getMedicalRecord().setRegistration(registration);
            }

            List<RecipelInfoEvt> recipelInfoEvtList = new ArrayList<>();
            List<RecipelInfo> recipelInfoList = this.recipelInfoService.listAll(parameters, "a.seq asc");
            if (!CollectionUtils.isEmpty(recipelInfoList))
            {
                // Truy vấn hàng loạt tất cả chi tiết đơn thuốc(Tối ưu hóa N+1 Truy vấn)
                List<String> recipelInfoIdsList = recipelInfoList.stream()
                        .map(RecipelInfo::getId)
                        .collect(Collectors.toList());
                Map<String, List<RecipelDetail>> detailsMap = this.recipelDetailService.getByRecipelInfoIds(recipelInfoIdsList);

                for (int i = 0; i < recipelInfoList.size(); i++) {
                    RecipelInfoEvt recipelInfoEvt = new RecipelInfoEvt();
                    RecipelInfo recipelInfo = recipelInfoList.get(i);
                    recipelInfo.setRegistration(registration);

                    // Lấy chi tiết đơn thuốc từ kết quả truy vấn hàng loạt
                    List<RecipelDetail> recipelDetailList = detailsMap.getOrDefault(recipelInfo.getId(), Collections.emptyList());
                    if (!CollectionUtils.isEmpty(recipelDetailList))
                    {
                        for (RecipelDetail recipelDetail:recipelDetailList)
                        {
                            recipelDetail.setRecipelInfo(recipelInfo);
                            recipelDetail.setDrugStuffId(this.getDrugStuffEvt(recipelDetail));
                        }
                    }

                    recipelInfoEvt.setRecipelInfo(recipelInfo);
                    recipelInfoEvt.setRecipelDetailEvtList(recipelDetailList);
                    recipelInfoEvtList.add(recipelInfoEvt);
                }
            }

            receptionEvt.setRecipelInfoEvtList(recipelInfoEvtList);
        }

        return receptionEvt;
    }

    /**
     * DS140 A v2: bản sao đúng luật của CrudService.verifyTenantOwnership,
     * chỉ dùng để QUYẾT ĐỊNH CÓ ĐỌC hay không trước khi enrich (không ném).
     * Ném ở đây sẽ poison transaction giống service.get nên chỉ trả boolean.
     * Parity: tenant session trống/"null" -> cho qua; company trống -> cho
     * qua; bằng tenant -> cho qua; từ điển dùng chung của cơ sở
     * (institutionShared, vd Thuốc/Vật tư) mà company là institution của
     * tenant -> cho qua; còn lại (tenant khác) -> không đọc, evt rỗng.
     */
    private boolean isReadableDrugStuff(DataEntity<?> entity, boolean institutionShared) {
        if (entity == null || entity.getCompany() == null) {
            return true;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return true;
        }
        String entityCompanyId = entity.getCompany().getId();
        if (StringUtils.isBlank(entityCompanyId)) {
            return true;
        }
        if (tenantId.equals(entityCompanyId)) {
            return true;
        }
        if (institutionShared) {
            String institution = companyService.getInstitution(tenantId);
            if (StringUtils.isNotBlank(institution) && institution.equals(entityCompanyId)) {
                return true;
            }
        }
        return false;
    }

    public DrugStuffEvt getDrugStuffEvt(RecipelDetail recipelDetail) {
        DrugStuffEvt drugStuffEvt = new DrugStuffEvt();
        // I1: chi tiết đơn có stuffType nhưng drugStuffId null (dữ liệu cũ/lệch) trước
        // đây dereference getDrugStuffId().getDrugStuffId() không guard -> NPE -> 500
        // ở mọi đường đọc đơn (controller/review/4 chỗ trong service này). Trả evt rỗng
        // thay vì 500: đường đọc vẫn trả được chi tiết, không bịa tên/giá thuốc.
        if (recipelDetail == null || recipelDetail.getDrugStuffId() == null
                || recipelDetail.getDrugStuffId().getDrugStuffId() == null) {
            return drugStuffEvt;
        }
        String stuffType = recipelDetail.getStuffType();        // Loại vật liệu:0-Thuốc tây 1-Thuốc đông y  2-Truyền dịch 3-Hạng mục khám chữa bệnh  4-Vật tư
        if ("0".equals(stuffType) || "1".equals(stuffType) || "2".equals(stuffType)) {
            //Thuốc đông y, Thuốc tây, Đơn truyền dịch tra cứu thông tin thuốc
            PresDrug presDrug = presDrugService.getById(recipelDetail.getDrugStuffId().getDrugStuffId());
            if( recipelDetail.getRecipelInfo() != null &&
                    recipelDetail.getRecipelInfo().getIsPre() != null &&
                    recipelDetail.getRecipelInfo().getIsPre() &&
                    presDrug != null ){
                //Thuốc trong đơn thuốc điện tử
                drugStuffEvt.setDrugStuffId(presDrug.getId());
                drugStuffEvt.setName(presDrug.getRegname());
                drugStuffEvt.setPrice(BigDecimal.valueOf(0));
                drugStuffEvt.setRetailPrice(BigDecimal.valueOf(0));
                drugStuffEvt.setIsUnpackSell("0");
                DictItem  dosisUnit = new DictItem();
                DictItem  PreparationUnit = new DictItem();
                DictItem  Pack = new DictItem();
                DictItem  type = new DictItem();
                ManufactureFactory factory = new ManufactureFactory();
                type.setValue(BizConstants.MEDICAL_TYPE_WESTERN);
                dosisUnit.setName("");
                dosisUnit.setValue("");
                PreparationUnit.setName(presDrug.getMinprepunt());
                PreparationUnit.setValue("");
                Pack.setName(presDrug.getMinpacunt());
                Pack.setValue("");
                factory.setName(presDrug.getPrdrname());
                drugStuffEvt.setDosisUnit(dosisUnit);
                drugStuffEvt.setPreparationUnit(PreparationUnit);
                drugStuffEvt.setPack(Pack);
                Drug drug  = new Drug();
                drug.setDosis(presDrug.getSpecname());
                drug.setDosisUnit(dosisUnit);
                drug.setPreparationUnit(PreparationUnit);
                drug.setPreparation(presDrug.getMinpaccnt());
                drug.setPack(Pack);
                drug.setFactory(factory);
                drug.setIsUnpackSell("0");
                drug.setType(type);
                drug.setGoodsName(presDrug.getRegname());
                drugStuffEvt.setDrug(drug);
            }else{
                // DS140 A v2: thuốc thuộc tenant khác (dữ liệu legacy/demo
                // lệch tenant) KHÔNG được đọc qua drugService.get: CrudService
                // là @Transactional cấp class nên ServiceException 400 của
                // verifyTenantOwnership đánh dấu transaction rollback-only
                // ngay khi vượt qua proxy get, dù caller có catch — commit
                // sau đó vẫn UnexpectedRollbackException và cả đơn 400.
                // Đọc qua drugDao.get (plain SELECT, không ném) rồi kiểm
                // tenant trước, giống hệt verifyTenantOwnership: evt rỗng
                // như tiền lệ I1 cho thuốc đã xóa; 1 dòng lệch không làm
                // hỏng cả đơn của tenant mình. getById trực tiếp thuốc lệch
                // tenant vẫn 400 (đường đó không đổi).
                Drug drug = this.drugDao.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                // I1: thuốc gốc đã xóa -> drug null, tránh NPE ở drug.getId() dưới đây.
                if (drug == null || !isReadableDrugStuff(drug, true)) {
                    return drugStuffEvt;
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
            }
        } else if ("3".equals(stuffType)) {
            //Truy vấn thông tin hạng mục khám chữa bệnh
            // DS140 A v2: như nhánh thuốc — đọc qua costItemDao.get + kiểm
            // tenant trước, không qua service.get (tránh poison transaction).
            // CostItem không override isInstitutionShared nên false.
            CostItem costItem = this.costItemDao.get(recipelDetail.getDrugStuffId().getDrugStuffId());
            // I1: hạng mục đã xóa -> costItem null, tránh NPE ở costItem.getId() dưới đây.
            if (costItem == null || !isReadableDrugStuff(costItem, false)) {
                return drugStuffEvt;
            }


            //TheoĐơn thuốcChi tiếtidLấycủa nóXét nghiệm và kiểm traKết quả
            List<InspectionCheck> inspectionChecks = inspectionCheckService.getByRecipelDetail(recipelDetail.getId());
            if(!CollectionUtils.isEmpty(inspectionChecks) && "1".equals(inspectionChecks.get(0).getStatus())){
                InspectionCheckInfo byInspecId = inspectionCheckInfoService.getByInspecId(inspectionChecks.get(0).getId());

                drugStuffEvt.setInspectionCheckInfo(byInspecId);
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
            // DS140 A v2: như nhánh thuốc — đọc qua stuffDao.get + kiểm
            // tenant trước, không qua service.get (tránh poison transaction).
            // Stuff là từ điển dùng chung của cơ sở nên institutionShared true.
            Stuff stuff = this.stuffDao.get(recipelDetail.getDrugStuffId().getDrugStuffId());
            if (stuff != null && !isReadableDrugStuff(stuff, true)) {
                return drugStuffEvt;
            }
            if(stuff!=null){
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
        }

        return drugStuffEvt;
    }
    @Transactional(readOnly = false)
    public List<RecipelInfoDTO> getByRegistrationId(List<Parameter> parameters,int limit,int offset,String registrationId) {
        // T2 (b): truoc day params cua caller (gom company_id) day thang
        // xuong dao qua PageRequest, khong qua ensureCompanyFilter nhu
        // listAll/listPage — caller doi company_id la doc duoc don cua
        // tenant khac. Nay ep company session truoc khi tao PageRequest,
        // dung mau CrudService.ensureCompanyFilter (strip company_id cua
        // caller, them company_id = tenant session; MedicalRecord tu khai
        // bao field company nen khong roi vao nhanh early-return).
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        ensureCompanyFilter(parameters);
        // K1: tham so nay KHONG phai menh de ORDER BY ma la id luot kham ma
        // frontend gửi qua trường order (HistoricalCases/historyRecipel đặt
        // order = clickId). Trước đây nó đi qua PageRequest(order) nên
        // sanitizeOrderBy ném IllegalArgumentException với mọi id số
        // (regex đòi ký tự đầu là chữ/_/backtick) -> history/recipel luôn 500.
        // Chuyển id sang khe id sẵn có của PageRequest (constructor 5 tham
        // số), order để "" để không chạm whitelist ORDER BY; SQL dùng #{id}
        // làm tham số ràng buộc (prepared statement) nên không mở injection.
        PageRequest pageRequest = new PageRequest(offset, limit, parameters, "", registrationId);
        List<RecipelInfoDTO> recipelInfos = recipelInfoService.getHistoryRecipel(pageRequest);
        if (CollectionUtils.isEmpty(recipelInfos)) {
            return recipelInfos;
        }

        // Truy vấn hàng loạt tất cả chi tiết đơn thuốc(Tối ưu hóa N+1 Truy vấn)
        List<String> recipelInfoIds = recipelInfos.stream()
                .map(RecipelInfoDTO::getId)
                .collect(Collectors.toList());
        Map<String, List<RecipelDetail>> detailsMap = recipelDetailService.getByRecipelInfoIds(recipelInfoIds);

        // hàng loạtLấytất cả registrationId
        List<String> registrationIds = recipelInfos.stream()
                .map(r -> r.getRegistration().getId())
                .distinct()
                .collect(Collectors.toList());

        // hàng loạtTruy vấnThông tin bệnh nhân
        Map<String, Patient> patientMap = new HashMap<>();
        for (String regId : registrationIds) {
            Patient patient = patientService.getPatientByregistrationId(regId);
            if (patient != null) {
                patientMap.put(regId, patient);
            }
        }

        // hàng loạtTruy vấnbệnh ánThông tin
        Map<String, MedicalRecord> medicalRecordMap = new HashMap<>();
        for (String regId : registrationIds) {
            MedicalRecord medicalRecord = this.dao.getByRegistrationId(regId);
            if (medicalRecord != null) {
                medicalRecordMap.put(regId, medicalRecord);
            }
        }

        // lắp rápDữ liệu
        for (RecipelInfoDTO recipelInfoDTO : recipelInfos) {
            // Lấy chi tiết đơn thuốc từ kết quả truy vấn hàng loạt
            List<RecipelDetail> byRecipelInfoId = detailsMap.getOrDefault(recipelInfoDTO.getId(), Collections.emptyList());
            for (RecipelDetail recipelDetail : byRecipelInfoId) {
                recipelDetail.setDrugStuffId(this.getDrugStuffEvt(recipelDetail));
            }
            recipelInfoDTO.setRecipelDetail(byRecipelInfoId);

            // ĐặtThông tin bệnh nhân
            String regId = recipelInfoDTO.getRegistration().getId();
            recipelInfoDTO.setPatient(patientMap.get(regId));

            // Đặtbệnh ánThông tin
            recipelInfoDTO.setMedicalRecord(medicalRecordMap.get(regId));
        }
        return recipelInfos;
    }

    public List<Recordpatvo> recordpatlist(String id){
        Company company = SessionUtils.getUser().getCompany();
        return medicalRecordDao.recordpatlist(company.getId(),id);
    }

    /**
     * Tiền sử dị ứng mới nhất đã lưu của bệnh nhân trong phòng khám đang
     * đăng nhập (dùng cho banner cảnh báo dị ứng trên màn khám). Trả về
     * null khi bệnh nhân chưa từng có dị ứng được ghi.
     */
    public String getLatestAllergyHistory(String patientId) {
        Company company = SessionUtils.getUser().getCompany();
        return medicalRecordDao.getLatestAllergyHistory(company.getId(), patientId);
    }


}