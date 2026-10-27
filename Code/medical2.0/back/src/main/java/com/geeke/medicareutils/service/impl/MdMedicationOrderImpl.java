package com.geeke.medicareutils.service.impl;
import com.geeke.common.constants.BizConstants;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geeke.admin.entity.User;
import com.geeke.admin.service.UserService;
import com.geeke.medicareutils.config.MedicareConfigProperties;
import com.geeke.medicareutils.db.entity.DzcfD001Output;
import com.geeke.medicareutils.db.entity.DzcfD002Output;
import com.geeke.medicareutils.db.entity.DzcfD003Output;
import com.geeke.medicareutils.db.service.DzcfD001OutputService;
import com.geeke.medicareutils.db.service.DzcfD002OutputService;
import com.geeke.medicareutils.db.service.DzcfD003OutputService;
import com.geeke.medicareutils.service.MdMedicationOrderService;
import com.geeke.medicareutils.util.MdRequestUtil;
import com.geeke.outpatient.entity.*;

import com.geeke.outpatient.service.*;

import com.geeke.utils.SessionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.context.WebServerApplicationContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * @Description điện tửĐơn thuốcluồngchuyểnTriển khaiLớp
 * @Author Hzx
 * @Date 2024/10/30
 */
@Service
@RequiredArgsConstructor
public class MdMedicationOrderImpl implements MdMedicationOrderService {


    private  final MdRequestUtil mdRequestUtil;

    private  final WebServerApplicationContext webServerApplicationContext;
    private final static String PRESCRIPTION_URL_XML = "/api/ureport/pdf?_u=Newtouch:chronicDisease.ureport.xml&_t=0&recipelInfoId=";

    private final PresDrugService presDrugService;

    private  final RegistrationService registrationService;

    private  final RecipelDetailService recipelDetailService;

    private final PatientMdDataService patientMdDataService;

    private final XtZdService xtZdService;

    private final XtZyzhService xtZyzhService;

    private  final DzcfD001OutputService dzcfD001OutputService;

    private  final DzcfD002OutputService dzcfD002OutputService;

    private  final DzcfD003OutputService dzcfD003OutputService;

    private final UserService userService;

    private final MedicareConfigProperties  properties ;


    /**[Ld7801]Tải lên đơn thuốc điện tửkiểm tra trước
     * @return
     */
    @Override
    public JSONObject validateElectronicPrescriptionUpload_Ld7801(RecipelInfoReview recipelInfoReview) {
        JSONObject data = new JSONObject(true);
        RecipelInfo recipelInfo = recipelInfoReview.getRecipelInfo();
        Registration registration = recipelInfo.getRegistration();
        MedicalRecord medicalRecord = recipelInfoReview.getMedicalRecord();
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        User doctor = userService.get(registration.getDoctor().getId());
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        List<RecipelDetail> recipelDetail = recipelDetailService.getRecipelDetail(recipelInfo.getId());
        data.put("mdtrtCertType", registration.getCardType());  // Loại giấy tờ khám bệnh, Giá trịcó thể là  01, 02  hoặc  03
        // Số giấy tờ khám bệnh
        data.put("mdtrtCertNo", registration.getCard());  // Số giấy tờ khám bệnh, nếuchứng từLoại là 03thì là Số thẻ an sinh xã hội
        // thẻ识loạimã(điện tửchứng từToken)
        data.put("cardSn", "");  // TODO Bắt buộc, điện tửchứng từToken, độ dài32Ký tự
        // Xác minh danh tính cho trường hợp ngoại tuyến của chứng từ điện tử TODO
        data.put("ecToken", "");  // Bắt buộc, Trường hợp trực tuyến qua internetBệnh viện问khám giờsử dụng
        // Số thứ tự
        data.put("authNo", "");  // Bắt buộc, dùng đểthânphần核验, Loại là số thứ tự, 100Ký tự
        // nghiệp vụLoạimã
        data.put("bizTypeCode", "01");  // Bắt buộc, 01 là y tế được chỉ địnhCơ sởkhám bệnh, 02 là 互联网Bệnh viện问khám
        // tham gia bảo hiểmđịa phươngmã số, Người tham gia bảo hiểm khác địa phương bắt buộc phải truyền
        data.put("insuPlcNo", patientMdData.getInsuplcAdmdvs());  // Bắt buộc, tham gia bảo hiểmđịa phươngmã số, 6Ký tự
        // y tế được chỉ địnhCơ sởĐơn thuốcmã số
        data.put("hospRxno", recipelInfo.getCode());  // Bắt buộc, y tế được chỉ địnhCơ sở của Đơn thuốcmã số, Lớn nhất40Ký tự
        // kê tiếp đơn của gốcĐơn thuốcmã số
        data.put("initRxno", "");  // không phảibắt buộc, kê tiếp đơn của gốcĐơn thuốcmã số, Lớn nhất40Ký tự
        // Đơn thuốcLớpmã loại TODO trước留cụ thểPhòng khámcụ thểhơnsửa
        if(BizConstants.RECIPEL_TYPE_WESTERN.equals(recipelInfo.getRecipelType().getValue())){
            //Thuốc tây
            data.put("rxTypeCode", "1");
            // ThuốcLớpmụcsố(liềusố)
            data.put("rxDrugCnt",recipelDetail.size() );  // Bắt buộc, Khi không phải thuốc Đông y thì là số lượng danh mục thuốc trong đơn,  trong thuốc giờ là Thuốctổngliềusố
        } else if (BizConstants.RECIPEL_TYPE_CHINESE.equals(recipelInfo.getRecipelType().getValue())) {
            //Thuốc đông y
            data.put("rxTypeCode", "2");
            // ThuốcLớpmụcsố(liềusố)
            data.put("rxDrugCnt", recipelInfo.getDosage());  // Bắt buộc, Khi không phải thuốc Đông y thì là số lượng danh mục thuốc trong đơn,  trong thuốc giờ là Thuốctổngliềusố
        }
        // Thời gian kê đơn
        data.put("prscTime", recipelInfo.getCreateDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime().format(outputFormatter));  // Bắt buộc, Thời gian kê đơn, NgàyThời gianđịnh dạng: yyyy-MM-dd HH:mm:ss
        // Đơn thuốcnguyên liềudùngphương phápmã số
        data.put("rxUsedWayCodg", "");  // Bắt buộc, Tham khảo mã đường dùng thuốc(drug_medc_way_code)
        // Đơn thuốcnguyên liềudùngphương phápTên
        data.put("rxUsedWayName", "");  // Bắt buộc, Đơn thuốcnguyên liềudùngphương pháp của Tên
        // Đơn thuốcnguyên liềutần suất lầnmã số
        data.put("rxFrquCodg", "");  // Bắt buộc, tham khảosử dụngtần suất lần(used_frqu)
        // Đơn thuốcnguyên liềutần suất lầnTên
        data.put("rxFrquName", "");  // Bắt buộc, Đơn thuốcnguyên liềutần suất lầnTên
        // Đơn thuốcnguyên liềuĐơn vị liều lượng
        data.put("rxDosunt", "");  // Bắt buộc, Đơn thuốcnguyên liều của Đơn vị liều lượng
        // Đơn thuốcnguyên liềuđơn lầnliềulượngsố
        data.put("rxDoscnt", "");  // Bắt buộc, Đơn thuốcnguyên liềuđơn lầnliềulượngsố
        // Đơn thuốcnguyên liềuY lệnhGiải thích
        data.put("rxDrordDscr", "");  // Bắt buộc, Đơn thuốcnguyên liều của Y lệnhGiải thích
        // Đơn thuốcHiệu lực ngàysố
        data.put("valiDays", "2");  // Bắt buộc, Đơn thuốc của Hiệu lực ngàysố
        // Đơn thuốcHiệu lựccắtdừngThời gian
        data.put("valiEndTime", "");  // Bắt buộc, TínhđượcĐến của Hiệu lựccắtdừngThời gian(Thời gian kê đơn+Hiệu lực ngàysố)
        // lạidùng(nhiều lần)sử dụngcờ
        data.put("reptFlag", "");  // Bắt buộc, 0 là không, 1 là có, cho biếtCó hay khôngcó thểlạidùng
        // Lớn nhấtsử dụng lầnsố
        data.put("maxReptCnt", "");  // Bắt buộc, Đơn thuốc của Lớn nhấtsử dụng lầnsố
        // Đãsử dụng lầnsố
        data.put("reptdCnt", "");  // Bắt buộc, Đãsử dụng lầnsố
        // sử dụngNhỏ nhấtgiữacách(Số ngày)
        data.put("minInrvDays", "");  // Bắt buộc, cho biếtsử dụngĐơn thuốc của Nhỏ nhấtThời giangiữacách(Số ngày)
        // kê tiếp đơncờ
        data.put("rxCotnFlag", "");  // Bắt buộc, 0 là không, 1 là có, cho biếtCó hay không là kê tiếp đơn
        // dàikỳĐơn thuốccờ
        data.put("longRxFlag","");  // Bắt buộc, 0 là không, 1 là có, cho biếtCó hay không là dàikỳĐơn thuốc

        //Đơn thuốcChi tiếtChi tiết
        JSONArray rxdrugdetail = new JSONArray();
        recipelDetail.forEach(item -> {
            //LấyThuốc trong đơn thuốc điện tửThông tin
             PresDrug presDrug = presDrugService.getById(item.getDrugStuffId().getDrugStuffId());
             item.setPresDrug(presDrug);
             JSONObject rxdrug = new JSONObject(true);
            // Mã danh mục y tế
            rxdrug.put("medListCodg", presDrug.getMedlistcodg());  // Bắt buộc, BHYTMã thuốc
            // Mã danh mục của cơ sở y tế được chỉ định
            rxdrug.put("fixmedinsHilistId", "1");  // Bắt buộc, 院trongMã thuốc
            // y tếCơ sởbào chếcờ
            rxdrug.put("hospPrepFlag", "");  // Bắt buộc, 0- không , 1- có , Mặc định không 
            if(BizConstants.RECIPEL_TYPE_WESTERN.equals(recipelInfo.getRecipelType().getValue())){
                //Thuốc tây
                rxdrug.put("rxItemTypeCode", "11");
                rxdrug.put("medcWayCodg",item.getWesternMedicineUse() );  // Bắt buộc, Tham khảo mã đường dùng thuốc
                //Thuốc thành phẩm đông y
            } else if (BizConstants.RECIPEL_TYPE_CHINESE.equals(recipelInfo.getRecipelType().getValue())) {
                //Thuốc đông y
                rxdrug.put("rxItemTypeCode", "13");  // Bắt buộc, tham khảoĐơn thuốchạng mục phútLớpmã
                rxdrug.put("tcmdrugTypeCode", "9");  //TODO đối vớitiếpsử dụng Bắt buộc, tham khảoĐơn thuốchạng mục phútLớpmã,  trong thuốcĐơn thuốcbắt buộc
                rxdrug.put("medcWayCodg",item.getChineseMedicineUse() );  // Bắt buộc, Tham khảo mã đường dùng thuốc
            }
            // Tên phân loại hạng mục đơn thuốc
            rxdrug.put("rxItemTypeName", "");  // Bắt buộc, Tên phân loại hạng mục đơn thuốc
            // Tên loại thuốc Đông y
            rxdrug.put("tcmdrugTypeName", "");  // Bắt buộc, Tên loại thuốc Đông y
            // Chú thích cuối trang về thảo dược
            rxdrug.put("tcmherbFoote", "");  // Chú thích cuối trang về thảo dược
            // thuốcLoạimã
            rxdrug.put("mednTypeCode", "");  // Bắt buộc, tham khảothuốcLoạimã
            // thuốcLoạiTên
            rxdrug.put("mednTypeName", presDrug.getDosformname());  // thuốcLoạiTên
            // Cờ thuốc dùng chính
            rxdrug.put("mainMedcFlag", "");  // Bắt buộc, 0- không , 1- có 
            // thêm急cờ
            rxdrug.put("urgtFlag", "");  // Bắt buộc, 0- không , 1- có 
            // Cờ thuốc cơ bản
            rxdrug.put("basMednFlag", "");  // Bắt buộc, 0- không , 1- có 
            // Có hay khôngtiến hành口Thuốc
            rxdrug.put("impDrugFlag", "");  // Bắt buộc, 0- không , 1- có 
            // Tên thương mại của thuốc
            rxdrug.put("drugProdname","");  // Tên thương mại của thuốc
            // ThuốcTên chung
            rxdrug.put("drugGenname", item.getPresDrug().getGenname());  // Bắt buộc, ThuốcTên chung
            // Dạng bào chế của thuốc
            rxdrug.put("drugDosform", item.getPresDrug().getDosformname());  // Bắt buộc, Dạng bào chế của thuốc
            // Quy cách thuốc
            rxdrug.put("drugSpec", item.getPresDrug().getSpecname() +'*'+item.getPresDrug().getMinpaccnt()+item.getPresDrug().getMinprepunt()+'/'+item.getPresDrug().getMinpacunt());  // Bắt buộc, Quy cách thuốc
            // sinhxưởngxưởng家
            rxdrug.put("prdrName", item.getPresDrug().getPrdrname());  // sinhxưởngxưởng家
            rxdrug.put("medcWayCodg", item.getWesternMedicineUse().getValue());  // Mô tả đường dùng thuốc
            // Mô tả đường dùng thuốc
            rxdrug.put("medcWayDscr", item.getWesternMedicineUse().getName());  // Mô tả đường dùng thuốc
            // dùngthuốcThời gian bắt đầu
            rxdrug.put("medcBegntime", item.getCreateDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime().format(outputFormatter));  // Bắt buộc, yyyy-MM-dd HH:mm:ss Định dạng
            // dùngthuốcThời gian kết thúc
           LocalDateTime startTime = LocalDateTime.parse( item.getCreateDate().toInstant()
                   .atZone(ZoneId.systemDefault())
                   .toLocalDateTime().format(outputFormatter), outputFormatter);
            LocalDateTime endTime = startTime.plusDays(Long.parseLong(item.getDays().getName()));
            rxdrug.put("medcEndtime", endTime.toString());  // Bắt buộc, yyyy-MM-dd HH:mm:ss Định dạng
            // Số ngày dùng thuốc
            rxdrug.put("medcDays", item.getDays().getName());  // Bắt buộc, Số ngày dùng thuốc
            // Thuốcđơn giá
            rxdrug.put("drugPric", "");  // Thuốcđơn giá, theophát thuốcĐơn vịtínhgiá
            // ThuốctổngSố tiền
            rxdrug.put("drugSumamt", "");  // ThuốctổngSố tiền
            // Thuốcphát thuốctổng lượng
            rxdrug.put("drugCnt", item.getTotal());  // Bắt buộc, Thuốcphát thuốctổng lượng
            // Thuốcphát thuốcĐơn vị
            rxdrug.put("drugDosunt",item.getPresDrug().getMinpaccnt() );  // Bắt buộc, phát thuốcĐơn vị
            // tổng lượng thuốc dùng
            rxdrug.put("drugTotlcnt", "");  // Bắt buộc, tổng lượng thuốc dùng
            // tổng lượng thuốc dùngĐơn vị
            rxdrug.put("drugTotlcntEmp", "");  // Bắt buộc, tổng lượng thuốc dùngĐơn vị
            // Liều dùng mỗi lần
            rxdrug.put("sinDoscnt", item.getSingleDosage());  // Bắt buộc, Liều dùng mỗi lần
            // đơn lầnĐơn vị liều lượng
            rxdrug.put("sinDosunt",item.getPresDrug().getMinprepunt());  // Bắt buộc, đơn lầnĐơn vị liều lượng
            // sử dụngtần suất lầnMã
            rxdrug.put("usedFrquCodg", item.getFrequency().getValue());  // Bắt buộc, tham khảosử dụngtần suất lần
            // sử dụngtần suất lầnTên
            rxdrug.put("usedFrquName", item.getFrequency().getName());  // Bắt buộc, sử dụngtần suất lầnTên
            // Bệnh việnphê duyệtcờ
            rxdrug.put("hospApprFlag", "1");  // Bắt buộc, Bệnh việnphê duyệtcờ
            rxdrugdetail.add(rxdrug);

        });
        //khám bệnhThông tin
        JSONObject mdtrtinfo = new JSONObject(true);
        //Thông tin chẩn đoán
        JSONObject diseinfo  = new JSONObject(true);
        String FixmedinsCode = SessionUtils.getUserJson().getJSONObject("company").getString("fixmedinsCode");
        String FixmedinsName = SessionUtils.getUserJson().getJSONObject("company").getString("name");
        mdtrtinfo.put("fixmedinsName",FixmedinsName);        // Tên cơ sở y tế được chỉ định (Kiểu ký tự 200)
        mdtrtinfo.put("fixmedinsCode", FixmedinsCode);        // y tế được chỉ địnhCơ sởmã số (Kiểu ký tự 20)
        mdtrtinfo.put("mdtrtId", registration.getMdtrtId());              // BHYTkhám bệnhID (Kiểu ký tự 30)
        mdtrtinfo.put("medType", registration.getMedType());              // Loại hình y tế (Kiểu ký tự 6)
        mdtrtinfo.put("iptOtpNo", registration.getIptOtpNo());             // Nhập viện/Số khám ngoại trú (Kiểu ký tự 30)
        mdtrtinfo.put("otpIptFlag", "1");           // ngoại trúNhập việnđịnh danh (Kiểu ký tự 3)
        mdtrtinfo.put("psnNo", patientMdData.getPsnNo());                // BHYTMã nhân viên (Kiểu ký tự 30)
        mdtrtinfo.put("patnName", patientMdData.getPsnName());             // Tên bệnh nhân (Kiểu ký tự 40)
        mdtrtinfo.put("psnCertType", registration.getCardType());          // Loại giấy tờ nhân viên (Kiểu ký tự 6)
        mdtrtinfo.put("certno", patientMdData.getCertno());               // Số giấy tờ (Kiểu ký tự 50)
        mdtrtinfo.put("patnAge", patientMdData.getAge());             // Tuổi (Kiểu số 4,1)
        mdtrtinfo.put("patnHgt", "");             // Bệnh nhânthâncao (Kiểu số 6,2)
        mdtrtinfo.put("patnWt", "");              // Bệnh nhânthểtrọng (Kiểu số 6,2)
        mdtrtinfo.put("gend", patientMdData.getGend());                 // Giới tính (Kiểu ký tự 6)
        mdtrtinfo.put("gesoVal", "");               // 妊娠(孕tuần) (Kiểu số 2)
        mdtrtinfo.put("nwbFlag", "");              // mớitrẻ sơ sinhcờ (Kiểu ký tự 3)
        mdtrtinfo.put("nwbAge", "");               // mớitrẻ sơ sinhNgày, Tháng龄 (Kiểu ký tự 20)
        mdtrtinfo.put("suckPrdFlag", "");           // 哺乳kỳcờ (Kiểu số 3)
        mdtrtinfo.put("algsHis", "");              // Tiền sử dị ứng (Kiểu ký tự 1000)
        mdtrtinfo.put("prscDeptName",registration.getClinicOffice().getName() );         // kê đơnTên khoa (Kiểu ký tự 50)
        mdtrtinfo.put("prscDeptCode", registration.getClinicOffice().getCode());         // kê đơnKhoamã số (Kiểu ký tự 30)
        mdtrtinfo.put("drCode", doctor.getUserExt().getPracPsnCode());               // kê đơnBHYTBác sĩmã (Kiểu ký tự 20)
        mdtrtinfo.put("prscDrName", doctor.getName());           // Tên bác sĩ kê đơn (Kiểu ký tự 50)
        mdtrtinfo.put("prscDrCertType", "");       // Loại giấy tờ của bác sĩ kê đơn (Kiểu ký tự 6)
        mdtrtinfo.put("prscDrCertno", "");         // Số giấy tờ của bác sĩ kê đơn (Kiểu ký tự 50)
        // Bác sĩchức vụgọi làMã (Kiểu ký tự 20)
        mdtrtinfo.put("drProfttlCodg", doctor.getUserExt().getPost());
        switch (doctor.getUserExt().getPost()){
            case  "231":
                mdtrtinfo.put("drProfttlName", "Bác sĩ trưởng khoa");
                break;
            case  "232":
                mdtrtinfo.put("drProfttlName", "Phó bác sĩ trưởng khoa");
                break;
            case  "233":
                mdtrtinfo.put("drProfttlName", "chẩn đoán chínhBác sĩ");
                break;
            case  "234":
                mdtrtinfo.put("drProfttlName", "Bác sĩ");
                break;
            case  "235":
                mdtrtinfo.put("drProfttlName", "Y sĩ");
                break;
            default:   mdtrtinfo.put("drProfttlName", "");
        }
        mdtrtinfo.put("drDeptCode",doctor.getUserExt().getOfficeCode());           // Bác sĩKhoaMã (Kiểu ký tự 30)
        mdtrtinfo.put("drDeptName", doctor.getUserExt().getOffice());           // Bác sĩTên khoa (Kiểu ký tự 50)
        mdtrtinfo.put("mdtrtTime", registration.getCreateDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime().format(outputFormatter));            // khám bệnhThời gian (Kiểu ngày giờ yyyy-MM-dd HH:mm:ss)
        mdtrtinfo.put("diseCodg", "");             // Mã loại bệnh (Kiểu ký tự 30)
        mdtrtinfo.put("diseName", "");             // Tên loại bệnh (Kiểu ký tự 500)
        mdtrtinfo.put("spDiseFlag", "");           // đặc biệtbệnhloạicờ (Kiểu ký tự 3)
        XtZd xtZd = xtZdService.getById(medicalRecord.getDiseaseId());
        if(BizConstants.RECIPEL_TYPE_WESTERN.equals(recipelInfo.getRecipelType().getValue())){
            //Thuốc tây
            diseinfo.put("diagType", "1");        // Loại chẩn đoán (Chẩn đoánLoại, Kiểu ký tự, Độ dài tối đa3)
            diseinfo.put("tcmDiseCode", "");     // Mã tên bệnh Đông y (Kiểu ký tự, Độ dài tối đa9)
            diseinfo.put("tcmDiseName", "");     // Tên bệnh Đông y (Kiểu ký tự, Độ dài tối đa100)
            diseinfo.put("tcmsympCode", "");     //  trong chứng bệnhchờmã (Kiểu ký tự, Độ dài tối đa9)
            diseinfo.put("tcmsymp", "");         //  trong chứng bệnhchờ (Kiểu ký tự, Độ dài tối đa100)

        } else if (BizConstants.RECIPEL_TYPE_CHINESE.equals(recipelInfo.getRecipelType().getValue())) {
            //Thuốc đông y
            XtZyzh xtZyzh = xtZyzhService.getById(medicalRecord.getSyndromeId());
            diseinfo.put("diagType", "2");        // Loại chẩn đoán (Chẩn đoánLoại, Kiểu ký tự, Độ dài tối đa3)
            diseinfo.put("tcmDiseCode", xtZd.getIcd10());     // Mã tên bệnh Đông y (Kiểu ký tự, Độ dài tối đa9)
            diseinfo.put("tcmDiseName", xtZd.getZdmc());     // Tên bệnh Đông y (Kiểu ký tự, Độ dài tối đa100)
            diseinfo.put("tcmsympCode", xtZyzh.getZhcode());     //  trong chứng bệnhchờmã (Kiểu ký tự, Độ dài tối đa9)
            diseinfo.put("tcmsymp",xtZyzh.getZhmc() );         //  trong chứng bệnhchờ (Kiểu ký tự, Độ dài tối đa100)
        }
        mdtrtinfo.put("diseCondDscr", "");         // Bệnhbệnh情Mô tả (Kiểu ký tự 2000)
        mdtrtinfo.put("hiFeesetlType", "");        // Loại quyết toán chi phí BHYT (Kiểu ký tự 6)
        mdtrtinfo.put("hiFeesetlName", "");        // BHYTChi phíLớpTên khácgọi là (Kiểu ký tự 20)
        mdtrtinfo.put("rgstFee", "");             // Phí đăng ký khám (Kiểu số 16,2)
        mdtrtinfo.put("medfeeSumamt", "");        // y tếphítổngsố tiền (Kiểu số 16,2)
        mdtrtinfo.put("fstdiagFlag", "");          // Có hay khôngKhám lần đầu (Kiểu ký tự 3)
        mdtrtinfo.put("extras", "");               // mở rộngDữ liệu (Đối tượngkiểu)
      //Chẩn đoánNút
        diseinfo.put("maindiagFlag", "1");    // Cờ chẩn đoán chính (0- không , 1- có , Kiểu ký tự, Độ dài tối đa3)
        diseinfo.put("diagSrtNo", "1");       // Chẩn đoán排số thứ tự (Kiểu số, Độ dài tối đa2)
        diseinfo.put("diagCode", xtZd.getIcd10());        // Mã chẩn đoán (sử dụngquốc giaBHYTMã chẩn đoán, Kiểu ký tự, Độ dài tối đa30)
        diseinfo.put("diagName", xtZd.getZdmc());        // Tên chẩn đoán (Kiểu ký tự, Độ dài tối đa100)
        diseinfo.put("diagDept", registration.getClinicOffice().getName());        // Chẩn đoánKhoa (Kiểu ký tự, Độ dài tối đa50)
        diseinfo.put("diagDrNo", doctor.getUserExt().getPracPsnCode());        // Chẩn đoánBác sĩMã (quốc giaBHYTBác sĩmã, Kiểu ký tự, Độ dài tối đa30)
        diseinfo.put("diagDrName", doctor.getName());      // Chẩn đoánBác sĩTên (Kiểu ký tự, Độ dài tối đa50)
        diseinfo.put("diagTime", medicalRecord.getCreateDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime().format(outputFormatter));        // Thời gian chẩn đoán (Kiểu ngày giờ, Định dạng: yyyy-MM-dd HH:mm:ss)
        data.put("rxdrugdetail",rxdrugdetail);
        data.put("mdtrtinfo",mdtrtinfo);
        data.put("diseinfo",diseinfo);
        JSONObject jsonObject= new JSONObject(true);
        jsonObject.put("data",data);

        JSONObject ld7801 = JSONObject.parseObject("{\"rxtracecode\":12345678901234567890,\"hirxno\":987654321098765432109876543210}");

        //JSONObject ld7801 = mdRequestUtil.dzcfUpload("Ld7801", jsonObject);
        //Ngàynhật ký落địa phươngbảng
        DzcfD001Output dzcfD001Output = new DzcfD001Output();
        dzcfD001Output.setMzh(registration.getId());
        dzcfD001Output.setCfh(recipelInfo.getId());
        dzcfD001Output.setOrganizeid(SessionUtils.getUserJson().getJSONObject("company").getString("id"));
        dzcfD001Output.setInputcontent(jsonObject.getString("data"));
        dzcfD001Output.setRxtracecode(ld7801.getString("rxtracecode"));
        dzcfD001Output.setHirxno(ld7801.getString("hirxno"));
        dzcfD001Output.setCzydm(SessionUtils.getUserJson().getString("loginName"));
        dzcfD001Output.setCzrq(LocalDateTime.now());
        dzcfD001Output.setZt(1);
        dzcfD001OutputService.save(dzcfD001Output);
        return ld7801 ;
    }

    /**
     * @return [Ld7802]điện tửĐơn thuốcBHYTđiện tửchữ ký
     */
    @Override
    @Transactional
    public JSONObject signElectronicPrescriptionWithInsurance_Ld7802(RecipelInfoReview recipelInfoReview, String ysId) {
        RecipelInfo recipelInfo = recipelInfoReview.getRecipelInfo();
        Registration registration = recipelInfo.getRegistration();
        MedicalRecord medicalRecord = recipelInfoReview.getMedicalRecord();
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        User doctor = userService.get(registration.getDoctor().getId());
        User ysDoctor = userService.get(ysId);
        String FixmedinsCode = SessionUtils.getUserJson().getJSONObject("company").getString("fixmedinsCode");
        String FixmedinsName = SessionUtils.getUserJson().getJSONObject("company").getString("name");
        //TheoĐơn thuốcidLấy đượcnốiĐơn thuốcPDFđường dẫn
        JSONObject data = new JSONObject();
        String url = "http://localhost:" +properties.getWebPort() +PRESCRIPTION_URL_XML+ recipelInfoReview.getRecipelInfo().getId();
        //Tệpbase64
         byte[] bytes = HttpUtil.downloadBytes(url);
        String pdfInfo = Base64.getEncoder().encodeToString(bytes);
        data.put("originalRxFile", pdfInfo);
        //Tải lên đơn thuốc điện tửTham số1-20JsonString của Base64chữký tựGiá trị
        JSONObject top20  = new JSONObject();
         DzcfD001Output one = dzcfD001OutputService.getOne(new LambdaQueryWrapper<DzcfD001Output>().eq(DzcfD001Output::getCfh, recipelInfo.getId()).eq(DzcfD001Output::getOrganizeid, SessionUtils.getLoginTenantId()));
        top20.put("rxTraceCode",one.getRxtracecode() );               // Đơn thuốctruy xuấtmã, Kiểu ký tự
        top20.put("hiRxno", one.getHirxno());                    // BHYTĐơn thuốcmã số, Kiểu ký tự
        top20.put("mdtrtId", registration.getMdtrtId());                   // BHYTkhám bệnhID, Kiểu ký tự
        top20.put("patnName", patientMdData.getPsnName());                  // Tên bệnh nhân, Kiểu ký tự
        top20.put("psnCertType", patientMdData.getPsnCertType());               // Loại giấy tờ nhân viên, Kiểu ký tự
        top20.put("certno", patientMdData.getCertno());                    // Số giấy tờ, Kiểu ký tự
        top20.put("fixmedinsName", FixmedinsName );             // Tên cơ sở y tế được chỉ định
        top20.put("fixmedinsCode",  FixmedinsCode);             // y tế được chỉ địnhCơ sởmã số
        top20.put("drCode", doctor.getUserExt().getPracPsnCode());                    // kê đơnBHYTBác sĩmã
        top20.put("prscDrName", doctor.getName());                // Tên bác sĩ kê đơn
        top20.put("pharDeptName", ysDoctor.getDepartment().getCode() );              // thẩm định đơnDược sĩTên khoa
        top20.put("pharDeptCode", ysDoctor.getUserExt().getOfficeCode());              // thẩm định đơnDược sĩKhoamã số
        top20.put("hosp_dept_codg", registration.getClinicOffice().getCode());            // Bệnh việnKhoaMã
        top20.put("pharProfttlCodg", ysDoctor.getUserExt().getPost());
        // thẩm định đơnDược sĩchức vụgọi làMã
        switch (ysDoctor.getUserExt().getPost()){
            case  "1":
                top20.put("drProfttlName", "hành nghềnghiệp vụDược sĩ");
                break;
            case  "2":
                top20.put("drProfttlName", "Chức danh kỹ thuật y tế");
                break;
            case  "2.1":
                top20.put("drProfttlName", "Bác sĩ trưởng khoa");
                break;
            case  "2.2":
                top20.put("drProfttlName", "Phó bác sĩ trưởng khoa");
                break;
            case  "2.3":
                top20.put("drProfttlName", "chínhquản lýDược sĩ");
                break;
            case  "2.4":
                top20.put("drProfttlName", "Dược sĩ");
                break;
            case  "2.5":
                top20.put("drProfttlName", "Y sĩ");
                break;
            default:
                top20.put("drProfttlName", "");
        }
        top20.put("pharCode", ysDoctor.getUserExt().getPost());                  // thẩm định đơnBHYTDược sĩmã
        top20.put("pharCertType", "");              // thẩm định đơnLoại giấy tờ của dược sĩ
        top20.put("pharCertno", "");                // thẩm định đơnSố giấy tờ của dược sĩ
        top20.put("pharName", ysDoctor.getName());                  // thẩm định đơnTên dược sĩ
        top20.put("pharPracCertNo", ysDoctor.getUserExt().getPracPsnCode());            // thẩm định đơnSố chứng chỉ hành nghề của dược sĩ
        top20.put("pharChkTime", recipelInfoReview.getCreateDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime().format(outputFormatter));               // thẩm định đơnDược sĩthẩm định đơnThời gian, Kiểu ngày giờ, Mặc định là trốngchuỗi
        String top20base64 = Base64.getEncoder().encodeToString(top20.toString().getBytes());
        data.put("originalValue", top20base64);
        //y tếCơ sởmã
        data.put("fixmedinsCode", SessionUtils.getUserJson().getJSONObject("company").getString("fixmedinsCode"));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data",data);
        DzcfD002Output dzcfD002Output = new DzcfD002Output();
//        JSONObject ld7802 = JSON.parseObject("{\n" +
//                "  \"rxFile\": \"UEsDBBQAAAAIAI6gO5bPUc3nqs2SOmeKb4hVV3zRGkA5uAY9S5sPtm2EzKItUEz0X0MNRVBeVJtSBRZ6gdlwsOdQFk8NdhNBZeaSHIQFBvjoHaVBpP1fZ0PlHKhqP0RZ8wQZlV2J+3H0BEPDNkweXvTxF3akbbYN3pCRgBqa6xfqujlRaxDXnGG8J2UdhPuwmPOG1G9IhpkI+xP2rZz3IEp5VyzKSCxM6Wcvqqzt1p7s4zo5bXx/J0iq1lVXkmG7e6Nho2VHJGG0SZnGYoS0=\", \n" +
//                "  \"signDigest\": \"8ac8e8327f2b38be283cd9032f2074f43b87c9a50637a589ec7c8039c36e69b9\", \n" +
//                "  \"signCertSn\": \"1234567890ABCDE1234567890ABCDE1234567890ABCDE1234567890ABCDE12345\", \n" +
//                "  \"signCertDn\": \"C=CN,O=NHSA,ST=15,L=00,L=00,OU=02,T=91150500MA0NFP6N44,CN=通辽博德 trong Kết hợp y học Mông Cổ và Tây yBệnh viện\"\n" +
//                "}");
        JSONObject ld7802 =   mdRequestUtil.dzcfUpload("Ld7802", jsonObject);
        dzcfD002Output.setMzh(registration.getId());
        dzcfD002Output.setCfh(recipelInfoReview.getRecipelInfo().getId());
        dzcfD002Output.setOrganizeid(SessionUtils.getLoginTenantId());
        dzcfD002Output.setOriginalvalue(top20base64);
        dzcfD002Output.setInputcontent(jsonObject.getString("data"));
        dzcfD002Output.setOriginalrxfile(pdfInfo);
        dzcfD002Output.setRxfile(ld7802.getString("rxFile"));
        dzcfD002Output.setSigndigest(ld7802.getString("signDigest"));
        dzcfD002Output.setSigncertsn(ld7802.getString("signCertSn"));
        dzcfD002Output.setSigncertdn(ld7802.getString("signCertDn"));
        dzcfD002Output.setCzydm(SessionUtils.getUserJson().getString("id"));
        dzcfD002Output.setCzrq(new Date());
        dzcfD002Output.setZt(1);
        dzcfD002OutputService.save(dzcfD002Output);

        return ld7802;
    }

    /**[Ld7101]Tải lên đơn thuốc điện tử
     * @return
     */
    @Override
    public JSONObject uploadElectronicPrescription_Ld7101(RecipelInfoReview recipelInfoReview,String ysId) {
        String FixmedinsCode = SessionUtils.getUserJson().getJSONObject("company").getString("fixmedinsCode");
        String FixmedinsName = SessionUtils.getUserJson().getJSONObject("company").getString("name");
        RecipelInfo recipelInfo = recipelInfoReview.getRecipelInfo();
        Registration registration = recipelInfo.getRegistration();
        DzcfD001Output output001 = dzcfD001OutputService.getOne(new LambdaQueryWrapper<DzcfD001Output>().eq(DzcfD001Output::getMzh, recipelInfo.getRegistration().getId()).eq(DzcfD001Output::getOrganizeid, SessionUtils.getLoginTenantId()));
        DzcfD002Output output002 = dzcfD002OutputService.getOne(new LambdaQueryWrapper<DzcfD002Output>().eq(DzcfD002Output::getMzh, recipelInfo.getRegistration().getId()).eq(DzcfD002Output::getOrganizeid, SessionUtils.getLoginTenantId()));
        MedicalRecord medicalRecord = recipelInfoReview.getMedicalRecord();
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        User doctor = userService.get(registration.getDoctor().getId());
        User ysDoctor = userService.get(ysId);
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        JSONObject data = new JSONObject(true);
        data.put("rxTraceCode",output001.getRxtracecode());
        data.put("hiRxno",output001.getHirxno());
        data.put("mdtrtId",registration.getMdtrtId());
        data.put("patnName", patientMdData.getPsnName());
        data.put("psnCertType", registration.getCardType());          // Loại giấy tờ nhân viên (Kiểu ký tự 6)
        data.put("certno", patientMdData.getCertno());
        data.put("fixmedinsName", FixmedinsName );             // Tên cơ sở y tế được chỉ định
        data.put("fixmedinsCode",  FixmedinsCode);
        data.put("drCode",doctor.getUserExt().getPracPsnCode());
        data.put("prscDrName", doctor.getName());
        data.put("pharDeptName", ysDoctor.getDepartment().getCode() );              // thẩm định đơnDược sĩTên khoa
        data.put("pharDeptCode", ysDoctor.getUserExt().getOfficeCode());
        switch (ysDoctor.getUserExt().getPost()){
            case  "1":
                data.put("drProfttlName", "hành nghềnghiệp vụDược sĩ");
                break;
            case  "2":
                data.put("drProfttlName", "Chức danh kỹ thuật y tế");
                break;
            case  "2.1":
                data.put("drProfttlName", "Bác sĩ trưởng khoa");
                break;
            case  "2.2":
                data.put("drProfttlName", "Phó bác sĩ trưởng khoa");
                break;
            case  "2.3":
                data.put("drProfttlName", "chínhquản lýDược sĩ");
                break;
            case  "2.4":
                data.put("drProfttlName", "Dược sĩ");
                break;
            case  "2.5":
                data.put("drProfttlName", "Y sĩ");
                break;
            default:
                data.put("drProfttlName", "");
        }
        data.put("pharCode", ysDoctor.getUserExt().getPost());                  // thẩm định đơnBHYTDược sĩmã
        data.put("pharCertType", "");              // thẩm định đơnLoại giấy tờ của dược sĩ
        data.put("pharCertno", "");                // thẩm định đơnSố giấy tờ của dược sĩ
        data.put("pharName", ysDoctor.getName());                  // thẩm định đơnTên dược sĩ
        data.put("pharPracCertNo", ysDoctor.getUserExt().getPracPsnCode());            // thẩm định đơnSố chứng chỉ hành nghề của dược sĩ
        data.put("pharChkTime", recipelInfoReview.getCreateDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime().format(outputFormatter));
         data.put("rxFile",output002.getRxfile());
         data.put("signDIgest", output002.getSigndigest());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        DzcfD003Output dzcfD003Output = new DzcfD003Output();
        dzcfD003Output.setMzh(registration.getId());
        dzcfD003Output.setCfh(recipelInfo.getId());
        dzcfD003Output.setOrganizeid(SessionUtils.getLoginTenantId());
        dzcfD003Output.setInputcontent(jsonObject.getString("data"));
        dzcfD003Output.setRxtracecode(output001.getRxtracecode());
        dzcfD003Output.setHirxno(output001.getHirxno());
        JSONObject ld7101 =   mdRequestUtil.dzcfUpload("Ld7101",jsonObject);
        dzcfD003Output.setRxtracecode(ld7101.getString("rxtracecode"));
        dzcfD003Output.setRxstasname(ld7101.getString("rxstasname"));
        dzcfD003OutputService.save(dzcfD003Output);
        return ld7101;
    }

    /**Hủy đơn thuốc điện tử
     * @return
     */
    @Override
    public JSONObject cancelElectronicPrescription_Ld7104(RecipelInfoReview recipelInfoReview, String undoRea) {
           JSONObject data = new JSONObject();
           DzcfD001Output one001 = dzcfD001OutputService.getOne(new LambdaQueryWrapper<DzcfD001Output>().eq(DzcfD001Output::getCfh, recipelInfoReview.getRecipelInfo().getId()).eq(DzcfD001Output::getOrganizeid, SessionUtils.getLoginTenantId()));
           String fixmedinsCode = SessionUtils.getUserJson().getJSONObject("company").getString("fixmedinsCode");
           RecipelInfo recipelInfo = recipelInfoReview.getRecipelInfo();
           User doctor = userService.get(recipelInfo.getRegistration().getDoctor().getId());
           data.put("hiRxno", one001.getHirxno());
           data.put("fixmedinsCode", fixmedinsCode);
           data.put("drCode",doctor.getUserExt().getPracPsnCode());
           data.put("drName",doctor.getName());
           data.put("undoRea", undoRea);
           data.put("undoTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
           JSONObject jsonObject = new JSONObject();
           jsonObject.put("data", data);
           JSONObject ld7104 = mdRequestUtil.dzcfUpload("Ld7104", jsonObject);
           DzcfD003Output dzcfD003Output = dzcfD003OutputService.getOne(new LambdaQueryWrapper<DzcfD003Output>().eq(DzcfD003Output::getCfh, recipelInfo.getId()));
           dzcfD003Output.setCxyy(undoRea);
           dzcfD003Output.setCxsj(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
           dzcfD003Output.setRxstascodg(ld7104.getString("rxstascodg"));
           dzcfD003Output.setRxstasname(ld7104.getString("rxstasname"));
           dzcfD003OutputService.saveOrUpdate(dzcfD003Output);
        return ld7104;
    }

    /**
     * @return
     */
    @Override
    public JSONObject queryElectronicPrescriptionInfo_Ld7202() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public JSONObject feedbackPrescriptionReviewResult_Ld7102() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public JSONObject queryElectronicPrescriptionReviewResult_Ld7805(RecipelInfoReview recipelInfoReview) {
        JSONObject data  = new JSONObject(true);
        Registration registration = recipelInfoReview.getRecipelInfo().getRegistration();
        DzcfD001Output one001 = dzcfD001OutputService.getOne(new LambdaQueryWrapper<DzcfD001Output>().eq(DzcfD001Output::getCfh, recipelInfoReview.getRecipelInfo().getId()).eq(DzcfD001Output::getOrganizeid, SessionUtils.getLoginTenantId()));
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        String fixmedinsCode = SessionUtils.getUserJson().getJSONObject("company").getString("fixmedinsCode");
        data.put("hiRxno",one001.getHirxno());
        data.put("fixmedinsCode",fixmedinsCode);
        data.put("mdtrtId",registration.getMdtrtId());
        data.put("psnName",patientMdData.getPsnName());
        data.put("psnCertType",patientMdData.getPsnCertType());
        data.put("psnCertno",patientMdData.getCertno());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data",data);
        return mdRequestUtil.dzcfUpload("[Ld7804",jsonObject);
    }

    /**
     * @return lấy thuốcTruy vấn
     */
    @Override
    public JSONObject queryPrescriptionDispensingResult_Ld7804(RecipelInfoReview recipelInfoReview) {
        JSONObject data  = new JSONObject(true);
        Registration registration = recipelInfoReview.getRecipelInfo().getRegistration();
        DzcfD001Output one001 = dzcfD001OutputService.getOne(new LambdaQueryWrapper<DzcfD001Output>().eq(DzcfD001Output::getCfh, recipelInfoReview.getRecipelInfo().getId()).eq(DzcfD001Output::getOrganizeid, SessionUtils.getLoginTenantId()));
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        String fixmedinsCode = SessionUtils.getUserJson().getJSONObject("company").getString("fixmedinsCode");
        data.put("hiRxno",one001.getHirxno());
        data.put("fixmedinsCode",fixmedinsCode);
        data.put("mdtrtId",registration.getMdtrtId());
        data.put("psnName",patientMdData.getPsnName());
        data.put("psnCertType",patientMdData.getPsnCertType());
        data.put("psnCertno",patientMdData.getCertno());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data",data);
        return mdRequestUtil.dzcfUpload("[Ld7804",jsonObject);
    }

    /**
     * @return Thuốc trong đơn thuốc điện tửThông tinTruy vấnsauLưu
     */
    @Override
    public Boolean queryPrescriptionDrugDirectory_Ld7806() {
        JSONObject data = new JSONObject();
        data.put("fixmedinsCode", SessionUtils.getUserJson().getJSONObject("company").getString("fixmedinsCode"));
        data.put("medListCodg","");
        data.put("medListCodgs","");
        data.put("begntime","");
        data.put("endtime", "");
        //Sau khi tra ra tất cả thì lưu vào cục bộDữ liệukho
        data.put("pageSize", Long.MAX_VALUE);
        data.put("pageNum", "1");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data",data);
        return presDrugService
                .saveBatch(JSONArray.parseArray(mdRequestUtil.dzcfUpload("Ld7806",jsonObject).getString("list"), PresDrug.class));
    }



}
