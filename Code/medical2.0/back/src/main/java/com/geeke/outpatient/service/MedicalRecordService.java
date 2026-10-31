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
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.cure.entity.InspectionCheck;
import com.geeke.cure.entity.InspectionCheckInfo;
import com.geeke.cure.service.InspectionCheckDetailService;
import com.geeke.cure.service.InspectionCheckInfoService;
import com.geeke.cure.service.InspectionCheckService;
import com.geeke.org.entity.Company;
import com.geeke.outpatient.dao.MedicalRecordDao;
import com.geeke.outpatient.entity.*;
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

    @Transactional(readOnly = false)
    public MedicalRecord save(MedicalRecord medicalRecord,
                              MultipartFile[] fileIdUploads,
                              String[] deleteIds) throws java.io.IOException {
        if (StringUtils.isBlank(medicalRecord.getFileId())) {
            medicalRecord.setFileId(IdGen.uuid());
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
        return medicalRecordDao.getByOrder(registration);
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
        MedicalRecord medicalRecordSave = this.save(medicalRecord);
        //Lấy thông tin đơn thuốc
        List<RecipelInfoEvt> recipelInfoEvtList = medicalRecipelEvt.getRecipelInfoEvtList();
        //dùng đểLưuThu phí
        List<RecipelInfo> recipelInfos = new ArrayList<>();
        if (null != recipelInfoEvtList && !recipelInfoEvtList.isEmpty()) {
            for (RecipelInfoEvt recipelInfoEvt : recipelInfoEvtList) {
                //Lưu thông tin đơn thuốc
                RecipelInfo recipelInfo = recipelInfoEvt.getRecipelInfo();
                //recipelInfo.setMedicalRecord(medicalRecordSave);
                recipelInfo.setRegistration(bindedRegistration);
                //hoàn thànhkhám bệnh của  giờchờđềuKhông cóthanh toán
                recipelInfo.setIsPay("0");
                //Các trường sau đã được frontend hoặc giá trị mặc định xử lý
//                recipelInfo.setSeq(0);
//                DictItem dictItem = new DictItem();
//                dictItem.setValue("smallType_0");
//                recipelInfo.setSmallType(dictItem);
//                recipelInfo.setIsFollowUp(0);

                //ĐặtMã
                String recipelNo = serialNoUtils.generateSerialNo("");
                recipelInfo.setCode(recipelNo);
                RecipelInfo recipelInfoSave = recipelInfoService.save(recipelInfo);
                List<RecipelDetail> recipelDetailEvtList = recipelInfoEvt.getRecipelDetailEvtList();
                if (null != recipelDetailEvtList && !recipelDetailEvtList.isEmpty()) {
                    //duyệtĐơn thuốc
                    for (RecipelDetail recipelDetail : recipelDetailEvtList) {
                        //LưuĐơn thuốcChi tiết
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
                        throw new ServiceException("Hiện tạivật liệukhông ởDữ liệubảng trong . ");
                    }
                    BigDecimal surplusStock = stockList.get(0).getSurplusStock();
                    if (surplusStock == null || surplusStock.compareTo(new BigDecimal(total)) < 0) {
                        throw new ServiceException("Hiện tạivật liệuTồn khokhông đủ, khôngphương pháplàm thủ tục. ");
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
        MedicalRecord medicalRecord = medicalRecords.get(0);
        outEvt.setMedicalRecord(medicalRecord);
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
                throw new ServiceException("đăng kýtiếp nhậnThông tinKhông thể là trống.");
            }
            Registration registration = this.registrationService.get(receptionEvt.getId());
            if (Objects.isNull(registration)) {
                throw new ServiceException("đăng kýtiếp nhậnThông tinKhông tồn tại.");
            }
            if (!(Objects.equals(registration.getStatus().getValue(), BizConstants.REG_STATUS_PENDING) || Objects.equals(registration.getStatus().getValue(), BizConstants.REG_STATUS_VISITED))) {
                throw new ServiceException("đăng kýtiếp nhậnThông tinkhông có [chờtiếp nhận] hoặc [Đãhoàn thành tiếp nhận]Trạng thái.");
            }
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
                    throw new ServiceException("[Bệnh nhâncơ bảnThông tin]Không được để trống.");
                }
                //Xử lýcơ bảnThông tin(tức làThông tin bệnh nhân)
                if (!org.springframework.util.StringUtils.hasText(receptionEvt.getPatient().getId()) || Objects.isNull(this.patientService.get(receptionEvt.getPatient().getId()))) {
                    receptionEvt.getPatient().setId(null);
                    receptionEvt.setPatient(this.patientService.saveSuper(receptionEvt.getPatient()));
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
            }

            if (InstantPatient.INSTANT.getCode() == receptionEvt.getType()) {
                registration.setTreatType(receptionEvt.getRegistration().getTreatType());
                registration.setInfectType(receptionEvt.getRegistration().getInfectType());
                receptionEvt.setRegistration(this.registrationService.save(registration));

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
            //throw new RuntimeException("Đơn thuốcThông tinKhông thể là trống.");
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
                //Lưu bảng chính của đơn thuốc
                RecipelInfo recipelInfoEntity = this.recipelInfoService.save(recipelInfo);

                List<RecipelDetail> recipelDetails = this.recipelDetailService.getByRecipelInfoId(recipelInfoEntity.getId());
                if (!CollectionUtils.isEmpty(recipelDetails)) {
                    this.recipelDetailService.batchDelete(recipelDetails);
                }
                for (RecipelDetail recipelDetail : recipelDetailEvtList) {
                    recipelDetail.setId(null);
                    recipelDetail.setRecipelInfo(recipelInfoEntity);
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
        /*
         * Đơn thuốcThông tin là trống, trực tiếpkhôngdùngLưu
         * */
        if (CollectionUtils.isEmpty(recipelInfoEvtList)) {
            //return;
           throw new ServiceException("Đơn thuốcThông tinKhông thể là trống.");
        }
        String registrationId = entity.getRegistrationId();
        // Lấyđăng kýThông tin
        Registration registration = this.registrationService.get(registrationId);

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
                    drug.setCreateBy("từ xaBệnh viện");
                    drug.setUpdateBy("từ xaBệnh viện");
                    drugService.addDurg(drug);
                }

                recipelDetail.setCompany(entity.getCompany());
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

    public DrugStuffEvt getDrugStuffEvt(RecipelDetail recipelDetail) {
        DrugStuffEvt drugStuffEvt = new DrugStuffEvt();
        String stuffType = recipelDetail.getStuffType();        // Loại vật liệu:0-Thuốc tây 1-Thuốc đông y  2-Truyền dịch 3-Hạng mục khám chữa bệnh  4-Vật tư
        if ("0".equals(stuffType) || "1".equals(stuffType) || "2".equals(stuffType)) {
            //Thuốc đông y, Thuốc tây, Đơn truyền dịch tra cứu thông tin thuốc
            PresDrug presDrug = presDrugService.getById(recipelDetail.getDrugStuffId().getDrugStuffId());
            if( recipelDetail.getRecipelInfo().getIsPre() != null &&
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
                Drug drug = this.drugService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
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
            CostItem costItem = this.costItemService.get(recipelDetail.getDrugStuffId().getDrugStuffId());


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
            Stuff stuff = this.stuffService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
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
    public List<RecipelInfoDTO> getByRegistrationId(List<Parameter> parameters,int limit,int offset,String order) {
        PageRequest pageRequest = new PageRequest(offset, limit, parameters, order);
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