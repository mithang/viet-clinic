package com.geeke.outpatient.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import com.geeke.common.constants.BizConstants;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.outpatient.dao.MedicalRecordDao;
import com.geeke.outpatient.dao.RecipelDetailDao;
import com.geeke.outpatient.dao.RegistrationDao;
import com.geeke.outpatient.entity.*;
import com.geeke.outpatient.service.*;
import com.geeke.stock.entity.Dispensing;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.Stuff;
import com.geeke.stock.service.DispensingService;
import com.geeke.stock.service.DrugService;
import com.geeke.stock.service.StuffService;
import com.geeke.toll.entity.TollInfo;
import com.geeke.toll.service.TollInfoService;
import com.geeke.treatment.entity.CostItem;
import com.geeke.treatment.service.CostItemService;
import com.geeke.utils.DateUtils;
import com.geeke.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class DataBean {

    private static final String MEDICAL_REMARK = "Lưu ý thuốc đặc biệt: Theo văn bản số 24 năm 2002 của Bộ Y tế và Cục Quản lý Y dược cổ truyền quốc gia: " +
            " là bảochứngBệnh nhândùngthuốcan toàn, Thuốcmộtquaphátra, khôngđượctrảđổi. ";

    private static final String CHARGE_REMARK = "Lời nhắc thân thiện: \nPhiếu này không dùng để thanh toán bảo hiểm. Vui lòng giữ cẩn thận biên lai nộp phí. Khi khám bệnh, hoàn phí, vui lòng xuất trình biên lai này. " +
            "Nếu mất biên lai, việc hoàn phí sẽ gặp khó khăn và thông tin có thể bị lộ. ";

    private static final String REGISTRATION_REMARK = "Lời nhắc thân thiện: \nPhiếu này không có chức năng thanh toán bảo hiểm, Vui lòng giữ cẩn thận biên lai nộp phí của bạn, Khám bệnh, hoàn phí giờVui lòngrahiển thịđóbằngmục. ";

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Hiển thị phương thức thanh toánTênánh xạ
     */
    private static final java.util.Map<String, String> PAY_TYPE_DISPLAY = new java.util.HashMap<>();
    static {
        PAY_TYPE_DISPLAY.put(BizConstants.PAY_TYPE_CASH, "Tiền mặt");
        PAY_TYPE_DISPLAY.put(BizConstants.PAY_TYPE_ALIPAY, "Alipay");
        PAY_TYPE_DISPLAY.put(BizConstants.PAY_TYPE_WECHAT, "WeChat");
        PAY_TYPE_DISPLAY.put(BizConstants.PAY_TYPE_BANK_CARD, "Thẻ ngân hàng");
        PAY_TYPE_DISPLAY.put(BizConstants.PAY_TYPE_MEDICAL, "BHYT");
        PAY_TYPE_DISPLAY.put(BizConstants.PAY_TYPE_BANK_TRANSFER, "Chuyển khoản ngân hàng");
        PAY_TYPE_DISPLAY.put(BizConstants.PAY_TYPE_EWALLET, "Ví điện tử");
    }

    /**
     * LấyHiển thị phương thức thanh toánTên
     * @param payTypeValue phương thức thanh toántừ điểnGiá trị
     * @return hiển thịTên, Chưabiết giờTrả vềtrốngchuỗi
     */
    private static String getPayTypeDisplayName(String payTypeValue) {
        return PAY_TYPE_DISPLAY.getOrDefault(payTypeValue, "");
    }

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private MedicalRecordDao medicalRecordDao;

    @Autowired
    private RecipelInfoService recipelInfoService;

    @Autowired
    private RecipelDetailDao recipelDetailDao;


    @Autowired
    private TollInfoService tollInfoService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private StuffService stuffService;

    @Autowired
    private CostItemService costItemService;

    @Resource
    private MedicalRecordService medicalRecordService;
    @Resource
    private DispensingService dispensingService;

    @Resource
    private PresDrugService presDrugService;

    /**
     * @return lắp rápĐơn thuốckhácThông tin
     */
    public List<RecipelTempletEvt> buildReport(String dsName, String datasetName, Map<String, Object> parameters) {
        //Đơn thuốcid
        String recipelInfoId = (String) parameters.get("recipelInfoId");
        //Đơn thuốc|Phát thuốc 0|1
        String type = (String) parameters.get("type");
        RecipelInfo recipelInfo = recipelInfoService.get(recipelInfoId);
        List<Parameter> parameters2 = SearchParamsBuilder.create()
                .eq("registration_id", recipelInfo.getRegistration().getId())
                .build();
        PageRequest pageRequest = new PageRequest(parameters2, "");
        List<MedicalRecord> medicalRecords = medicalRecordDao.listAll(pageRequest);
        MedicalRecord medicalRecord = medicalRecords.get(0);
        //MedicalRecord medicalRecord = medicalRecordDao.get(recipelInfo.getMedicalRecord().getId());
        Registration registration = registrationService.get(medicalRecord.getRegistration().getId());
        Patient patient = patientService.get(registration.getPatientId().getId());
        List<TollInfo> byRecipeId = tollInfoService.getByRecipeId(recipelInfoId);
        TollInfo tollInfo = null;
        if (null != byRecipeId && !byRecipeId.isEmpty()) {
            tollInfo = byRecipeId.get(0);
        }
        List<RecipelTempletEvt> outList = new ArrayList<>();
        RecipelTempletEvt recipelTempletEvt = new RecipelTempletEvt();
        recipelTempletEvt.setClinicName(recipelInfo.getCompany().getName());
        recipelTempletEvt.setSmallType(recipelInfo.getSmallType().getName());
        recipelTempletEvt.setOffice("Khoa: " + registration.getClinicOffice().getName());
        recipelTempletEvt.setCode("Số đơn thuốc: " + recipelInfo.getCode());
        recipelTempletEvt.setName("Họ tên: " + patient.getName());
        recipelTempletEvt.setGender("Giới tính: " + patient.getGender().getName());
        recipelTempletEvt.setTreatType(registration.getTreatType().getName());
        String billDate = DateUtils.formatDate(recipelInfo.getCreateDate(), "yyyyNămMMThángddNgày");
        recipelTempletEvt.setBillDate("lập đơnNgày: " + billDate);
        recipelTempletEvt.setAge("Tuổi: " + patient.getAge() + " tuổi");
        recipelTempletEvt.setFeeType("Loại phí: " + (null == tollInfo ? "" : tollInfo.getPaymentType().getName()));
        recipelTempletEvt.setDispenseDate("0".equals(type) ? "" : ("Ngày phát thuốc: " + new SimpleDateFormat(DATE_FORMAT).format(recipelInfo.getDispensionDate())));
        recipelTempletEvt.setIdCard("Số CMND/CCCD: " + patient.getCard());
        recipelTempletEvt.setDiagnose("Chẩn đoán lâm sàng: " + (StringUtils.isNotBlank(medicalRecord.getWesternDiagnose()) ? "Chẩn đoán tây y: " + medicalRecord.getWesternDiagnose() + ";" : "")
                + (StringUtils.isNotBlank(medicalRecord.getChinaDiagnose()) ? "Chẩn đoán đông y: " + medicalRecord.getChinaDiagnose() + ";" : ""));
        recipelTempletEvt.setDoctor("Bác sĩ: " + registration.getDoctor().getName());
        recipelTempletEvt.setFee("thuốcphí: " + recipelInfo.getFee() + " đồng");
        recipelTempletEvt.setEntrust(StringUtils.isBlank(recipelInfo.getEntrust()) ? "" : "Hạng mục y lệnh: " + recipelInfo.getEntrust());
        recipelTempletEvt.setBlack("················Phần dưới để trống················");
        recipelTempletEvt.setApothecary("Duyệt phát: ");
        recipelTempletEvt.setDeploy("Pha chế: ");
        recipelTempletEvt.setMedicalRemark(MEDICAL_REMARK);
        outList.add(recipelTempletEvt);
        return outList;
    }

    /**
     * @return lắp rápTâythuốc
     */
    public List<RecipelDetailEvt> westMedicine(String dsName, String datasetName, Map<String, Object> parameters) {
        List<RecipelDetailEvt> outList = new ArrayList<>();
        //Đơn thuốcid
        String recipelInfoId = (String) parameters.get("recipelInfoId");
        List<RecipelDetail> detailList = recipelDetailDao.getByRecipelInfoId(recipelInfoId);
        RecipelInfo recipelInfo = recipelInfoService.get(recipelInfoId);
        BigDecimal count = new BigDecimal(0);
        if (null != detailList && !detailList.isEmpty()) {
            for (RecipelDetail recipelDetail : detailList) {
                if (1 == recipelDetail.getIsExtra()) {
                    //Tính phí phụ thêm
                    count = count.add(recipelDetail.getAllFee());
                    continue;
                }
                if ("0".equals(recipelDetail.getStuffType())) {
                    if(recipelInfo.getIsPre() != null&&recipelInfo.getIsPre()){
                        //Thuốc trong đơn thuốc điện tử
                         PresDrug drug = presDrugService.getById(recipelDetail.getDrugStuffId().getDrugStuffId());
                        RecipelDetailEvt recipelDetailEvt = new RecipelDetailEvt();
                        recipelDetailEvt.setName(drug.getRegname());
                        recipelDetailEvt.setNorms(drug.getSpecname() +'*'+drug.getMinpaccnt()+drug.getMinprepunt()+'/'+drug.getMinpacunt());
                        recipelDetailEvt.setNum(recipelDetail.getTotal() + drug.getMinpacunt());
                        recipelDetailEvt.setPrice(0 + " đồng");
                        String daysName = recipelDetail.getDays() != null && recipelDetail.getDays().getName() != null
                                ? recipelDetail.getDays().getName()
                                : "0";
                        recipelDetailEvt.setDays(daysName + " ngày");
                        recipelDetailEvt.setFrequency(recipelDetail.getFrequency().getName() + "\t" + (daysName + " ngày"));
                        recipelDetailEvt.setUse("Sig: "+recipelDetail.getWesternMedicineUse().getName());
                        recipelDetailEvt.setUseText("(" + recipelDetail.getSingleDosage() + drug.getMinprepunt()+")/ lần");
                        outList.add(recipelDetailEvt);
                        //khôngdựa vàocủanêu
                        RecipelDetailEvt recipelDetailEvt1 = new RecipelDetailEvt();
                        recipelDetailEvt1.setName(recipelDetailEvt.getUse());
                        recipelDetailEvt1.setNorms(recipelDetailEvt.getUseText());
                        recipelDetailEvt1.setNum(recipelDetailEvt.getFrequency());
                        outList.add(recipelDetailEvt1);
                    }else{
                        Drug drug = drugService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                        RecipelDetailEvt recipelDetailEvt = new RecipelDetailEvt();
                        recipelDetailEvt.setName(drug.getGoodsName());
                        recipelDetailEvt.setNorms("("+drug.getDosis() + drug.getDosisUnit().getName() + "*" + drug.getPreparation() + drug.getPreparationUnit().getName()+")");
                        recipelDetailEvt.setNum(recipelDetail.getTotal() + drug.getPreparationUnit().getName());
                        recipelDetailEvt.setPrice(recipelDetail.getAllFee() + " đồng");
                        String daysName = recipelDetail.getDays() != null && recipelDetail.getDays().getName() != null
                                ? recipelDetail.getDays().getName()
                                : "0";
                        recipelDetailEvt.setDays(daysName + " ngày");
                        recipelDetailEvt.setFrequency(recipelDetail.getFrequency().getName() + "\t" + (daysName + " ngày"));
                        recipelDetailEvt.setUse("Sig: "+recipelDetail.getWesternMedicineUse().getName());
                        recipelDetailEvt.setUseText("(" + recipelDetail.getSingleDosage() + drug.getDosisUnit().getName()+")/ lần");
                        outList.add(recipelDetailEvt);
                        //khôngdựa vàocủanêu
                        RecipelDetailEvt recipelDetailEvt1 = new RecipelDetailEvt();
                        recipelDetailEvt1.setName(recipelDetailEvt.getUse());
                        recipelDetailEvt1.setNorms(recipelDetailEvt.getUseText());
                        recipelDetailEvt1.setNum(recipelDetailEvt.getFrequency());
                        outList.add(recipelDetailEvt1);
                    }

                }
            }
        }
        if (1 == count.compareTo(new BigDecimal(0))) {
            //Lắp ráp phí phụ thêm
            RecipelDetailEvt recipelDetailEvt = new RecipelDetailEvt();
            recipelDetailEvt.setUseText("");
            recipelDetailEvt.setStuffPrice( count + " đồng");
            outList.add(recipelDetailEvt);
        }
        return outList;
    }

    /**
     * @return lắp rápTruyền dịchĐơn thuốc
     */
    public List<InfuseEvt> infuse(String dsName, String datasetName, Map<String, Object> parameters) {
        List<InfuseEvt> outList = new ArrayList<>();
        //Đơn thuốcid
        String recipelInfoId = (String) parameters.get("recipelInfoId");
        List<RecipelDetail> detailList = recipelDetailDao.getInfuseByRecipelInfoId(recipelInfoId);
        InfuseEvt useMethod = new InfuseEvt();
        Integer num = 1;
        BigDecimal count = new BigDecimal(0);
        if (null != detailList && !detailList.isEmpty()) {
            for (RecipelDetail recipelDetail : detailList) {
                if (1 == recipelDetail.getIsExtra()) {
                    //Tính phí phụ thêm
                    count = count.add(recipelDetail.getAllFee());
                    continue;
                }
                if ("2".equals(recipelDetail.getStuffType())) {
                    Integer infuseGroup = recipelDetail.getInfuseGroup();
                    Drug drug = drugService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                    if (infuseGroup > num) {
                        //nốidùngphương pháp
                        InfuseEvt use = new InfuseEvt();
                        BeanUtils.copyProperties(useMethod, use);
                        outList.add(use);
                        num = infuseGroup;
                    }
                    InfuseEvt infuseEvt = new InfuseEvt();
                    infuseEvt.setName("("+infuseGroup+") "+drug.getGoodsName());
                    infuseEvt.setSkinTest(recipelDetail.getSkinTest().getName());
                    infuseEvt.setSingleDosage(recipelDetail.getSingleDosage() + drug.getPreparationUnit().getName());
                    String daysName = recipelDetail.getDays() != null && recipelDetail.getDays().getName() != null
                            ? recipelDetail.getDays().getName()
                            : "0";
                    infuseEvt.setDays("x"+daysName);
                    infuseEvt.setTotalNum("Tổng "+recipelDetail.getTotal() + drug.getPreparationUnit().getName());
                    outList.add(infuseEvt);
                    useMethod.setName("Sig: " + recipelDetail.getInfuseUse().getName());
                    useMethod.setSkinTest(recipelDetail.getFrequency().getRemarks());
                    useMethod.setSingleDosage(recipelDetail.getDrippingSpeed() + "giọt/phút");
                }
            }
            outList.add(useMethod);
        }
        if (1 == count.compareTo(new BigDecimal(0))) {
            //Lắp ráp phí phụ thêm
            InfuseEvt infuseEvt = new InfuseEvt();
            infuseEvt.setStuffPriceName("Phí vật tư");
            infuseEvt.setStuffPrice(count + " đồng");
            outList.add(infuseEvt);
        }
        return outList;
    }

    /**
     * @return lắp ráp trong thuốc
     */
    public List<RecipelDetailEvt> chineseMedicine(String dsName, String datasetName, Map<String, Object> parameters) {
        List<RecipelDetailEvt> outList = new ArrayList<>();
        //Đơn thuốcid
        String recipelInfoId = (String) parameters.get("recipelInfoId");
        RecipelInfo recipelInfo = recipelInfoService.get(recipelInfoId);
        List<RecipelDetail> detailList = recipelDetailDao.getByRecipelInfoId(recipelInfoId);
        List<String> chineseMedicalNames = new ArrayList<>();
        RecipelDetailEvt recipelDetailEvt = new RecipelDetailEvt();
        BigDecimal count = new BigDecimal(0);
        if (null != detailList && !detailList.isEmpty()) {
            for (RecipelDetail recipelDetail : detailList) {
                if (1 == recipelDetail.getIsExtra()) {
                    //Tính phí phụ thêm
                    count = count.add(recipelDetail.getAllFee());
                    continue;
                }
                if ("1".equals(recipelDetail.getStuffType())) {
                    Drug drug = drugService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                    chineseMedicalNames.add(drug.getGoodsName() + " " + recipelDetail.getTotal() + drug.getDosisUnit().getName());
                }
            }
        }
        RecipelDetailEvt reDetailEvt = new RecipelDetailEvt();
        reDetailEvt.setRemarks(StringUtils.isBlank(recipelInfo.getEntrust()) ? "" : "Hạng mục y lệnh:  " + recipelInfo.getEntrust());
        reDetailEvt.setNum("Tổng " + recipelInfo.getDosage()+" thang");
        reDetailEvt.setUse("Cách dùng: " + recipelInfo.getRecipelUse().getName());
        reDetailEvt.setFrequency(recipelInfo.getFrequency().getName()
                +" "+recipelInfo.getTakeFrequency().getName());
        reDetailEvt.setNorms("một lần" + recipelInfo.getSingleDosage() + "ml");
        if (1 == count.compareTo(new BigDecimal(0))) {
            //Lắp ráp phí phụ thêm
            reDetailEvt.setName("Phí vật tư: " + count + " đồng");
        }
        outList.add(reDetailEvt);

        return outList;
    }

    /**
     * @return lắp ráp trong thuốcthành phút
     */
    public List<EachChineseMedicalEvt> eachChineseMedical(String dsName, String datasetName, Map<String, Object> parameters) {
        List<EachChineseMedicalEvt> outList = new ArrayList<>();
        //Đơn thuốcid
        String recipelInfoId = (String) parameters.get("recipelInfoId");
        List<RecipelDetail> detailList = recipelDetailDao.getByRecipelInfoId(recipelInfoId);
        List<String> chineseMedicalNames = new ArrayList<>();
        if (null != detailList && !detailList.isEmpty()) {
            for (RecipelDetail recipelDetail : detailList) {
                if ("1".equals(recipelDetail.getStuffType())) {
                    Drug drug = drugService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                    DecimalFormat decimalFormat = new DecimalFormat("0.##");
                    chineseMedicalNames.add(drug.getGoodsName() + " " + decimalFormat.format(recipelDetail.getSingleDosage()) + drug.getDosisUnit().getName());
                }
            }
        }
        if (!chineseMedicalNames.isEmpty()) {
            List<List<String>> lists = com.google.common.collect.Lists.partition(chineseMedicalNames, 4);
            for (List<String> list : lists) {
                EachChineseMedicalEvt eachChineseMedicalEvt = new EachChineseMedicalEvt();
                for (int i = 0; i < list.size(); i++) {
                    if (0 == i) {
                        eachChineseMedicalEvt.setNameOne(list.get(i));
                    } else if (1 == i) {
                        eachChineseMedicalEvt.setNameTwo(list.get(i));
                    } else if (2 == i) {
                        eachChineseMedicalEvt.setNameThree(list.get(i));
                    } else if (3 == i) {
                        eachChineseMedicalEvt.setNameFour(list.get(i));
                    }
                }
                outList.add(eachChineseMedicalEvt);
            }
        }
        return outList;
    }

    /**
     * @return lắp ráphạng mục khám chữa bệnh
     */
    public List<RecipelDetailEvt> costItem(String dsName, String datasetName, Map<String, Object> parameters) {
        List<RecipelDetailEvt> outList = new ArrayList<>();
        //Đơn thuốcid
        String recipelInfoId = (String) parameters.get("recipelInfoId");
//        RecipelInfo recipelInfo = recipelInfoService.get(recipelInfoId);
        List<RecipelDetail> detailList = recipelDetailDao.getByRecipelInfoId(recipelInfoId);
        BigDecimal count = new BigDecimal(0);
        if (null != detailList && !detailList.isEmpty()) {
            for (RecipelDetail recipelDetail : detailList) {
                if (1 == recipelDetail.getIsExtra()) {
                    //Tính phí phụ thêm
                    count = count.add(recipelDetail.getAllFee());
                    continue;
                }
                if ("3".equals(recipelDetail.getStuffType())) {
                    CostItem costItem = costItemService.get(recipelDetail.getDrugStuffId().getDrugStuffId());
                    RecipelDetailEvt recipelDetailEvt = new RecipelDetailEvt();
                    recipelDetailEvt.setName(costItem.getItemName());
                    recipelDetailEvt.setNum(recipelDetail.getTotal() + "");
                    recipelDetailEvt.setUseText(costItem.getUnit().getName());
                    recipelDetailEvt.setPrice(new DecimalFormat("0.##").format(costItem.getSalePrice()) + " đồng");
                    recipelDetailEvt.setRemarks(costItem.getRemarks());
                    outList.add(recipelDetailEvt);
                }
            }
        }
        if (1 == count.compareTo(new BigDecimal(0))) {
            //Lắp ráp phí phụ thêm
            RecipelDetailEvt recipelDetailEvt = new RecipelDetailEvt();
            recipelDetailEvt.setUseText("");
            recipelDetailEvt.setStuffPrice(count + " đồng");
            outList.add(recipelDetailEvt);
        }
        return outList;
    }

    /**
     * @return lắp rápThu phí
     */
    public List<ChargeTicketEvt> charge(String dsName, String datasetName, Map<String, Object> parameters) {
        List<ChargeTicketEvt> outList = new ArrayList<>();
        //Đơn thuốcid
        String recipelInfoId = (String) parameters.get("recipelInfoId");
        RecipelInfo recipelInfo = recipelInfoService.get(recipelInfoId);
        //MedicalRecord medicalRecord = medicalRecordDao.get(recipelInfo.getMedicalRecord().getId());
        //Registration registration = registrationService.get(medicalRecord.getRegistration().getId());
        Registration registration = registrationService.get(recipelInfo.getRegistration().getId());
        Patient patient = patientService.get(registration.getPatientId().getId());
        List<TollInfo> byRecipeId = tollInfoService.getByRecipeId(recipelInfoId);
        TollInfo tollInfo = null;
        if (null != byRecipeId && !byRecipeId.isEmpty()) {
            tollInfo = byRecipeId.get(0);
        }

        ChargeTicketEvt chargeTicketEvt = new ChargeTicketEvt();
        chargeTicketEvt.setClinicName(recipelInfo.getCompany().getName());
        chargeTicketEvt.setName("Họ tên: " + patient.getName());
        chargeTicketEvt.setGender("Giới tính: " + patient.getGender().getName());
        chargeTicketEvt.setAge("Tuổi: " + patient.getAge());
        chargeTicketEvt.setTip(CHARGE_REMARK);
        if (null != tollInfo) {
            chargeTicketEvt.setCode("Số phiếu thu phí: " + tollInfo.getId());
            chargeTicketEvt.setType("Loại thu phí: " + tollInfo.getPaymentType().getName());
            chargeTicketEvt.setProject("Hạng mục thu phí: " + recipelInfo.getRecipelType().getName());
            String createBy = tollInfo.getCreateBy();
            if (createBy.contains("(")) {
                createBy = createBy.substring(0,createBy.indexOf("("));
            }
            chargeTicketEvt.setPeople("Người thu phí: " + createBy);
            chargeTicketEvt.setDate("Ngày khám bệnh: " + new SimpleDateFormat(DATE_FORMAT).format(recipelInfo.getCreateDate()));
            chargeTicketEvt.setOffice("Khoa khám: " + registration.getClinicOffice().getName());
            chargeTicketEvt.setFee("Số tiền thu phí: " + recipelInfo.getFee());
            String payTypeName = getPayTypeDisplayName(tollInfo.getPaymentType().getValue());
            chargeTicketEvt.setFeeType(payTypeName.isEmpty() ? "" : payTypeName + ": " + recipelInfo.getFee());
        }
        outList.add(chargeTicketEvt);
        return outList;
    }

    /**
     * thông thường/Đơn thuốc bệnh mạn tính
     * @param dsName
     * @param datasetName
     * @param parameters
     * @return
     */
    public List<ChronicDiseaseEvt> chronicDisease(String dsName, String datasetName, Map<String, Object> parameters){
        List<ChronicDiseaseEvt> list = ListUtil.list(false);
        //Đơn thuốcid
        String recipelInfoId = (String) parameters.get("recipelInfoId");
        RecipelInfo recipelInfo = recipelInfoService.get(recipelInfoId);
        List<RecipelDetail> detailList = recipelDetailDao.getByRecipelInfoId(recipelInfoId);
        ReceptionEvt receptionEvt = medicalRecordService.allNewQuery(recipelInfo.getRegistration().getId(), null);
        if (ObjectUtil.isNotNull(receptionEvt)) {
            Patient patient = receptionEvt.getPatient();
            Registration registration = receptionEvt.getRegistration();
            MedicalRecord medicalRecord = receptionEvt.getMedicalRecord();
            ChronicDiseaseEvt evt = new ChronicDiseaseEvt(patient,registration,medicalRecord,recipelInfo,detailList);
            List<TollInfo> tollInfos = tollInfoService.getByRecipeId(recipelInfo.getId());
            if (CollectionUtil.isNotEmpty(tollInfos)) {
                TollInfo tollInfo = tollInfos.get(0);
                String name = tollInfo.getCreateBy().substring(0, tollInfo.getCreateBy().indexOf("("));
                evt.setTollCollector("Nhân viên thu phí: "+name);
                evt.setDeploy("Duyệt/Pha chế: "+name);
            }
            List<Parameter> dispensingParams = SearchParamsBuilder.create()
                    .eq("registration_id", registration.getId())
                    .eq("recipel_info_id", recipelInfoId)
                    .build();
            List<Dispensing> dispensings = dispensingService.listAll(dispensingParams, null);
            if (CollectionUtil.isNotEmpty(dispensings)) {
                String name = dispensings.get(0).getCreateBy()
                        .substring(0, dispensings.get(0).getCreateBy().indexOf("("));
                evt.setDispensing("Đối chiếu/Phát thuốc: " + name);
            }
            list.add(evt);
        }
        return list;
    }

    /**
     * @return lắp rápĐăng ký khám
     */
    public List<ChargeTicketEvt> registration(String dsName, String datasetName, Map<String, Object> parameters) {
        List<ChargeTicketEvt> outList = new ArrayList<>();
        //Đơn thuốcid
        String registrationId = (String) parameters.get("registrationId");
        Registration registration = registrationService.get(registrationId);
        Patient patient = patientService.get(registration.getPatientId().getId());
        ChargeTicketEvt chargeTicketEvt = new ChargeTicketEvt();
        chargeTicketEvt.setClinicName(registration.getCompany().getName() + "Chứng từ đăng ký khám (Liên khách hàng)");
        chargeTicketEvt.setName("Họ tên: " + patient.getName());
        chargeTicketEvt.setGender("Giới tính: " + patient.getGender().getName());
        chargeTicketEvt.setAge("Tuổi: " + patient.getAge());
        chargeTicketEvt.setDate("Đăng ký khámThời gian: " + new SimpleDateFormat(DATE_FORMAT).format(registration.getCreateDate()));
        String createBy = registration.getCreateBy();
        if (createBy.contains("(")) {
            createBy = createBy.substring(0,createBy.indexOf("("));
        }
        chargeTicketEvt.setPeople("Người thu phí: " + createBy);
        chargeTicketEvt.setOffice("Đăng ký khámKhoa: " + registration.getClinicOffice().getName());
        chargeTicketEvt.setProject("Đăng ký khámBác sĩ: " + registration.getDoctor().getName());
        chargeTicketEvt.setFee("Đăng ký khámSố tiền: " + registration.getRegistrationFee() + " đồng");
        String payTypeName = getPayTypeDisplayName(registration.getPayType().getValue());
        chargeTicketEvt.setFeeType(payTypeName.isEmpty() ? "" : payTypeName + ": " + registration.getRegistrationFee() + " đồng");
        chargeTicketEvt.setTip(REGISTRATION_REMARK);
        Date zeroTimeOfDate = DateUtils.getZeroTimeOfDate(registration.getCreateDate());
        String todayStart = DateUtils.formatDateTime(zeroTimeOfDate);
        Date endTimeOfDate = DateUtils.getEndTimeOfDate(registration.getCreateDate());
        String todayEnd = DateUtils.formatDateTime(endTimeOfDate);
        List<Parameter> regParameters = SearchParamsBuilder.create()
                .eq("a.doctor_id", registration.getDoctor().getId())
                .ge("a.create_date", todayStart)
                .le("a.create_date", todayEnd)
                .build();
        List<Registration> registrations = registrationService.listAll(regParameters, "a.create_date asc");
        String todayNum = "0";
        if (CollectionUtils.isNotEmpty(registrations)) {
            String patientId = registration.getPatientId().getId();
            for (int i = 0; i < registrations.size(); i++) {
                Registration docRegistration = registrations.get(i);
                if (patientId.equals(docRegistration.getPatientId().getId())) {
                    todayNum = (i + 1) + "";
                }
            }
        }
        chargeTicketEvt.setNum("nayNgàysố thứ tự: "+todayNum);
        outList.add(chargeTicketEvt);
        return outList;
    }

}
