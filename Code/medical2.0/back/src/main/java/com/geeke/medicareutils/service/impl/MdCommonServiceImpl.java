package com.geeke.medicareutils.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geeke.admin.entity.User;
import com.geeke.medicareutils.service.MdCommonService;
import com.geeke.medicareutils.util.MdRequestUtil;
import com.geeke.medicareutils.util.YbWebApiUtil;
import com.geeke.org.entity.ClinicOffice;
import com.geeke.outpatient.entity.MedicalRecord;
import com.geeke.outpatient.entity.Patient;
import com.geeke.outpatient.entity.PatientMdData;
import com.geeke.outpatient.entity.Registration;
import com.geeke.outpatient.service.MedicalRecordService;
import com.geeke.outpatient.service.PatientMdDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MdCommonServiceImpl implements MdCommonService {

     //private final MdRequestUtil mdRequestUtil;

     private  final YbWebApiUtil mdRequestUtil;

     private  final MedicalRecordService medicalRecordService;

     private final PatientMdDataService patientMdDataService;


    /**
     * 冲đúngDữ liệugiao dịch
     * Description
     * khả dụnggiao dịchmã: [2101]Bác sĩQuyết toán, [2102]nhà thuốcQuyết toán, [2103]nhà thuốcQuyết toánhủy, [2207]Quyết toán ngoại trú, 
     * [2208]Hủy quyết toán ngoại trú, [2304]Quyết toán nhập viện, [2305]Quyết toán nhập việnhủy, [2401]nhập việnlàm thủ tục, 
     * [2304A]Quyết toán nhập viện; 
     * @param psnNo Mã nhân viên
     * @param omsgid gốcBên gửibáovăn bảnid
     * @param oinfno gốcgiao dịchmã số
     */
    @Override
    public String reversalData_2601(String psnNo, String omsgid, String oinfno) {
        JSONObject data =  new JSONObject();
        data.put("psn_no",psnNo);
        data.put("omsgid",omsgid);
        data.put("oinfno",oinfno);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data",data);
        return mdRequestUtil.getMedicareData("2601",jsonObject.toJSONString());
    }

    @Override
    public String upMedicalRecord_4701(Registration registration) {
        //LấyThông tin bệnh án của nhân viên
        List< MedicalRecord> record = medicalRecordService.getByOrder(registration.getId());
        MedicalRecord medicalRecord = record.get(0);
        //nhân viênBHYTThông tin
        PatientMdData patientMdData = patientMdDataService.getOne(new LambdaQueryWrapper<PatientMdData>().eq(PatientMdData::getPatientId, registration.getPatientId().getId()));
        //Thông tin khoa
        ClinicOffice clinicOffice = registration.getClinicOffice();
        //nhân viênThông tin
        Patient patient = registration.getPatientId();
        //Bác sĩThông tin
        User doctor = registration.getDoctor();

        //nhập việnThông tinxây dựng
        JSONObject adminfo =  new JSONObject();
        adminfo.put("mdtrt_sn", registration.getId()); // Số thứ tự khám bệnh
        adminfo.put("mdtrt_id", registration.getMdtrtId()); // Khám bệnh ID
        adminfo.put("psn_no", patientMdData.getPsnNo()); // Mã nhân viên
        adminfo.put("mdtrtsn", ""); // Nhập việnsố
        adminfo.put("name", patientMdData.getPsnName()); // Họ tên
        adminfo.put("gend", patientMdData.getGend()); // Giới tính
        adminfo.put("age", patientMdData.getAge()); // Tuổi
        adminfo.put("adm_rec_no","" ); // nhập việnBản ghisố thứ tự
        adminfo.put("wardarea_name", ""); // bệnhQuậnTên
        adminfo.put("dept_code", clinicOffice.getCode()); // Mã khoa
        adminfo.put("dept_name", clinicOffice.getName()); // Tên khoa
        adminfo.put("bedno", ""); // bệnhgiườngsố
        adminfo.put("adm_time", registration.getCreateDate()); // nhập việnThời gian
        adminfo.put("illhis_stte_name", patientMdData.getPsnName()); // bệnh史trình bàyngườiTên Mặc địnhnàyngười
        adminfo.put("illhis_stte_rl", ""); // trình bàyngười và Bệnh nhânquan hệmã
        if (Objects.nonNull(patient.getWithPatientNexus())) {
            //cóngười giám hộtrình bày
            adminfo.put("illhis_stte_name", patient.getGuardianName()); // bệnh史trình bàyngườiTên Mặc địnhnàyngười
            adminfo.put("illhis_stte_rl",patient.getWithPatientNexus() ); // trình bàyngười và Bệnh nhânquan hệmã
        }
        adminfo.put("stte_rele", ""); // trình bàynội dungCó hay khôngcó thể靠định danh
        adminfo.put("chfcomp",medicalRecord.getPatientTell()); // Lý do khám chính
        adminfo.put("dise_now", medicalRecord.getNowHistory()); // Bệnh sử hiện tại
        adminfo.put("hlcon", ""); // sức khỏetrạngtình hình
        adminfo.put("dise_his", medicalRecord.getDiseaseHistory()); // Tiền sử bệnh(chứangoài伤)
        adminfo.put("ifet",registration.getInfectType()); // TODO khôngtừ điển Bệnh nhântruyền染tínhcờ
        adminfo.put("ifet_his", medicalRecord.getInfectiousHistory()); // Tiền sử bệnh truyền nhiễm
        adminfo.put("prev_vcnt", ""); // Tiền sử tiêm chủng phòng ngừa
        adminfo.put("oprn_his", medicalRecord.getSurgeryHistory()); // Tiền sử phẫu thuật
        adminfo.put("bld_his",medicalRecord.getTransfusionHistory()); // Tiền sử truyền máu
        adminfo.put("algs_his", medicalRecord.getAllergyHistory()); // Tiền sử dị ứng
        adminfo.put("psn_his", medicalRecord.getPersonalHistory()); // Tiền sử cá nhân
        adminfo.put("mrg_his", medicalRecord.getPregnancyHistory()); // Tiền sử hôn nhân và sinh đẻ
        adminfo.put("mena_his", medicalRecord.getLunariaHistory()); // Tiền sử kinh nguyệt
        adminfo.put("fmhis", medicalRecord.getFamilyHistory()); // Tiền sử gia đình
        adminfo.put("physexm_tprt", registration.getTemperature()); // Khám thực thể--Nhiệt độ cơ thể (℃)
        adminfo.put("physexm_pule", registration.getPulse()); // Khám thực thể -- 脉tỷ lệ
        adminfo.put("physexm_vent_frc", registration.getBreathe()); // Khám thực thể--hô hấptần suấttỷ lệ
        //TODO   thểôKiểm trahiệnHệ thống là nguyênthểNhập
        adminfo.put("physexm_systolic_pre", ""); // Khám thực thể -- 收缩压 (mmHg)
        adminfo.put("physexm_dstl_pre", ""); // Khám thực thể -- 舒张压 (mmHg)
        adminfo.put("physexm_height", ""); // Khám thực thể--thâncao (cm)
        adminfo.put("physexm_wt", ""); // Khám thực thể--thểtrọng (kg)
        adminfo.put("physexm_ordn_stas", ""); // Khám thực thể -- mộtthông thườngtrạng tình hình Kiểm traKết quả
        adminfo.put("physexm_skin_musl", ""); // Khám thực thể -- 皮肤 và 黏膜Kiểm traKết quả
        adminfo.put("physexm_spef_lymph", ""); // Khám thực thể -- Hạch bạch huyết nông toàn thânKiểm traKết quả
        adminfo.put("physexm_head", ""); // Khám thực thể -- tiêu đềbộvàcủa nóthiết bị官Kiểm traKết quả
        adminfo.put("physexm_neck", ""); // Khám thực thể -- 颈bộKiểm traKết quả
        adminfo.put("physexm_chst", ""); // Khám thực thể -- 胸bộKiểm traKết quả
        adminfo.put("physexm_abd", ""); // Khám thực thể -- 腹bộKiểm traKết quả
        adminfo.put("physexm_finger_exam", ""); // Khám thực thể -- 肛门chỉkhámKiểm traKết quảMô tả
        adminfo.put("physexm_genital_area", ""); // Khám thực thể -- ngoàisinh殖thiết bịKiểm traKết quả
        adminfo.put("physexm_spin", ""); // Khám thực thể -- 脊柱Kiểm traKết quả
        adminfo.put("physexm_all_fors", ""); // Khám thực thể -- 四肢Kiểm traKết quả
        adminfo.put("nersys", ""); // Khám thực thể -- 神quaHệ thốngKiểm traKết quả
        adminfo.put("spcy_info", ""); // 专科tình hình
        adminfo.put("asst_exam_rslt", ""); // Kiểm tra hỗ trợKết quả
        adminfo.put("tcm4d_rslt", ""); // Đông y“四khám”quan sátKết quảMô tả
        adminfo.put("syddclft", ""); // 辨chứng phútkiểumã
        adminfo.put("syddclft_name", ""); // 辩chứng phútkiểuTên
        adminfo.put("prnp_trt", ""); // điều trịthìđiều trịphương pháp

        adminfo.put("rec_doc_code", doctor.getUserExt().getPracPsnCode()); // tiếp nhậnBác sĩmã số
        adminfo.put("rec_doc_name", doctor.getUserExt().getName()); // tiếp nhậnBác sĩTên
        adminfo.put("ipdr_code", ""); // Nhập việnBác sĩmã số
        adminfo.put("ipdr_name", ""); // Nhập việnBác sĩTên
        adminfo.put("chfdr_code", ""); // Bác sĩ trưởng khoamã số
        adminfo.put("chfdr_name", ""); // Bác sĩ trưởng khoaTên
        adminfo.put("chfpdr_code", doctor.getUserExt().getPracPsnCode()); // chẩn đoán chínhBác sĩmã
        adminfo.put("chfpdr_name", doctor.getUserExt().getName()); // chẩn đoán chínhBác sĩTên
        adminfo.put("main_symp", ""); // chínhcầntriệu chứng
        adminfo.put("adm_rea", ""); // nhập việngốcdo
        adminfo.put("adm_way", ""); // nhập việnđường
        adminfo.put("apgr", ""); // 评 phútGiá trị(Apgar)
        adminfo.put("diet_info", ""); // 饮食tình hình
        adminfo.put("growth_deg", ""); // phátsinhquy trìnhđộ
        adminfo.put("mtl_stas_norm", ""); // 精神Trạng tháibình thườngcờ
        adminfo.put("slep_info", ""); // 睡眠trạngtình hình
        adminfo.put("sp_info", ""); // đặc biệttình hình
        adminfo.put("mind_info", ""); // 心lýTrạng thái
        adminfo.put("nurt", ""); // 营养Trạng thái
        adminfo.put("self_ablt", ""); // tựlýcó thể力
        adminfo.put("nurscare_obsv_item_name", ""); // Hạng mục quan sát điều dưỡngTên
        adminfo.put("nurscare_obsv_rslt", ""); // điều dưỡngquan sátKết quả
        adminfo.put("smoke", ""); // hút thuốccờ
        adminfo.put("stop_smok_days", ""); // dừngdừnghút thuốc ngàysố
        adminfo.put("smok_info", ""); // hút thuốctrạngtình hình
        adminfo.put("smok_day", ""); // Ngàyhút thuốclượng(支)
        adminfo.put("drnk", ""); // uống rượucờ
        adminfo.put("drnk_frqu", ""); // uống rượutần suấttỷ lệ
        adminfo.put("drnk_day", ""); // Ngàyuống rượulượng(mL)
        adminfo.put("eval_time", ""); // 评估NgàyThời gian
        adminfo.put("resp_nurs_code", ""); // 责任Y táMã
        adminfo.put("resp_nurs_name", ""); // 责任Y táTên
        adminfo.put("vali_flag", ""); // Cờ hiệu lực

        //Thông tin chẩn đoánxây dựng
        JSONObject diseinfo = new JSONObject();
        diseinfo.put("inout_diag_type", ""); // ranhập việnLoại chẩn đoán
        diseinfo.put("maindiag_flag", ""); // Cờ chẩn đoán chính
        diseinfo.put("diag_seq", ""); // Chẩn đoánthứ tựCộtsố
        diseinfo.put("diag_time", ""); // Thời gian chẩn đoán
        diseinfo.put("wm_diag_code", ""); // Mã chẩn đoán Tây y
        diseinfo.put("wm_diag_name", ""); // Tên chẩn đoán Tây y
        diseinfo.put("tcm_dise_code", ""); // Mã tên bệnh Đông y
        diseinfo.put("tcm_dise_name", ""); // Tên bệnh Đông y
        diseinfo.put("tcmsymp_code", ""); // Mã chứng hậu Đông y
        diseinfo.put("tcmsymp", ""); // Chứng hậu Đông y
        diseinfo.put("vali_flag", ""); // Cờ hiệu lực
        //bệnhquy trìnhBản ghixây dựng
        JSONObject coursrinfo = new JSONObject();
        coursrinfo.put("codse_rcd_id", ""); // bệnhquy trìnhBản ghimã số
        coursrinfo.put("dept_code", ""); // Mã khoa
        coursrinfo.put("dept_name", ""); // Tên khoa
        coursrinfo.put("wardarea_name", ""); // bệnhQuậnTên
        coursrinfo.put("bedno", ""); // bệnhgiườngsố
        coursrinfo.put("rcd_time", ""); // Bản ghiNgàyThời gian
        coursrinfo.put("chfcomp", ""); // Lý do khám chính
        coursrinfo.put("cas_ftur", ""); // ca bệnh特điểm
        coursrinfo.put("tcm4d_rslt", ""); // Đông y“四khám”quan sátKết quả
        coursrinfo.put("dise_evid", ""); // Chẩn đoán依dữ liệu
        coursrinfo.put("prel_wm_diag_code", ""); // Chẩn đoán sơ bộ-Mã chẩn đoán Tây y
        coursrinfo.put("prel_wm_dise_name", ""); // Chẩn đoán sơ bộ-Tên chẩn đoán Tây y
        coursrinfo.put("prel_tcm_diag_code", ""); // Chẩn đoán sơ bộ-Mã tên bệnh Đông y
        coursrinfo.put("prel_tcm_dise_name", ""); // Chẩn đoán sơ bộ-Tên bệnh Đông y
        coursrinfo.put("prel_tcmsymp_code", ""); // Chẩn đoán sơ bộ-Mã chứng hậu Đông y
        coursrinfo.put("prel_tcmsymp", ""); // Chẩn đoán sơ bộ-Chứng hậu Đông y
        coursrinfo.put("finl_wm_diag_code", ""); // Chẩn đoán phân biệt-Mã chẩn đoán Tây y
        coursrinfo.put("finl_wm_diag_name", ""); // Chẩn đoán phân biệt-Tên chẩn đoán Tây y
        coursrinfo.put("finl_tcm_dise_code", ""); // Chẩn đoán phân biệt-Mã tên bệnh Đông y
        coursrinfo.put("finl_tcm_dise_name", ""); // Chẩn đoán phân biệt-Tên bệnh Đông y
        coursrinfo.put("finl_tcmsymp_code", ""); // Chẩn đoán phân biệt-Mã chứng hậu Đông y
        coursrinfo.put("finl_tcmsymp", ""); // Chẩn đoán phân biệt-Chứng hậu Đông y
        coursrinfo.put("dise_plan", ""); // khámtrị liệukế hoạch
        coursrinfo.put("prnp_trt", ""); // điều trịthìđiều trịphương pháp
        coursrinfo.put("ipdr_code", ""); // Nhập việnBác sĩmã số
        coursrinfo.put("ipdr_name", ""); // Nhập việnBác sĩTên
        coursrinfo.put("prnt_doc_name", ""); // trêncấpBác sĩTên
        coursrinfo.put("vali_flag", ""); // Cờ hiệu lực
        //Phẫu thuậtBản ghi, bệnh情抢救, chết亡Bản ghi, xuất việnnhỏ结xây dựng TODO Phòng khámtạmvàtrước留
        JSONObject oprninfo = new JSONObject();
        JSONObject rescinfo = new JSONObject();
        JSONObject dieinfo = new JSONObject();
        JSONObject dscginfo  =  new JSONObject();


        JSONObject jsonObject = new JSONObject();
        //nhập việnThông tin
        jsonObject.put("adminfo",adminfo);
        //Thông tin chẩn đoán
        jsonObject.put("diseinfo",diseinfo);
        //bệnhquy trìnhBản ghi
        jsonObject.put("coursrinfo",coursrinfo);

        return mdRequestUtil.getMedicareData("4701",jsonObject.toJSONString());
    }


}
