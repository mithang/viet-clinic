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
     * xungđúngDữ liệugiao dịch
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
        adminfo.put("illhis_stte_name", patientMdData.getPsnName()); // bệnhsửtrình bàyngườiTên Mặc địnhnàyngười
        adminfo.put("illhis_stte_rl", ""); // trình bàyngười và Bệnh nhânquan hệmã
        if (Objects.nonNull(patient.getWithPatientNexus())) {
            //cóngười giám hộtrình bày
            adminfo.put("illhis_stte_name", patient.getGuardianName()); // bệnhsửtrình bàyngườiTên Mặc địnhnàyngười
            adminfo.put("illhis_stte_rl",patient.getWithPatientNexus() ); // trình bàyngười và Bệnh nhânquan hệmã
        }
        adminfo.put("stte_rele", ""); // trình bàynội dungCó hay khôngcó thểdựađịnh danh
        adminfo.put("chfcomp",medicalRecord.getPatientTell()); // Lý do khám chính
        adminfo.put("dise_now", medicalRecord.getNowHistory()); // Bệnh sử hiện tại
        adminfo.put("hlcon", ""); // sức khỏetrạngtình hình
        adminfo.put("dise_his", medicalRecord.getDiseaseHistory()); // Tiền sử bệnh(chứangoàichấn thương)
        adminfo.put("ifet",registration.getInfectType()); // TODO khôngtừ điển Bệnh nhântruyềnnhiễmtínhcờ
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
        adminfo.put("physexm_pule", registration.getPulse()); // Khám thực thể -- mạchtỷ lệ
        adminfo.put("physexm_vent_frc", registration.getBreathe()); // Khám thực thể--hô hấptần suấttỷ lệ
        //TODO   thểôKiểm trahiệnHệ thống là nguyênthểNhập
        adminfo.put("physexm_systolic_pre", ""); // Khám thực thể -- thucoáp (mmHg)
        adminfo.put("physexm_dstl_pre", ""); // Khám thực thể -- giãntrươngáp (mmHg)
        adminfo.put("physexm_height", ""); // Khám thực thể--thâncao (cm)
        adminfo.put("physexm_wt", ""); // Khám thực thể--thểtrọng (kg)
        adminfo.put("physexm_ordn_stas", ""); // Khám thực thể -- mộtthông thườngtrạng tình hình Kiểm traKết quả
        adminfo.put("physexm_skin_musl", ""); // Khám thực thể -- da và niêm mạcKiểm traKết quả
        adminfo.put("physexm_spef_lymph", ""); // Khám thực thể -- Hạch bạch huyết nông toàn thânKiểm traKết quả
        adminfo.put("physexm_head", ""); // Khám thực thể -- tiêu đềbộvàcủa nóthiết bịcơ quanKiểm traKết quả
        adminfo.put("physexm_neck", ""); // Khám thực thể -- cổbộKiểm traKết quả
        adminfo.put("physexm_chst", ""); // Khám thực thể -- ngựcbộKiểm traKết quả
        adminfo.put("physexm_abd", ""); // Khám thực thể -- bụngbộKiểm traKết quả
        adminfo.put("physexm_finger_exam", ""); // Khám thực thể -- hậu mônchỉkhámKiểm traKết quảMô tả
        adminfo.put("physexm_genital_area", ""); // Khám thực thể -- ngoàisinhsinh dụcthiết bịKiểm traKết quả
        adminfo.put("physexm_spin", ""); // Khám thực thể -- cột sốngKiểm traKết quả
        adminfo.put("physexm_all_fors", ""); // Khám thực thể -- tứ chiKiểm traKết quả
        adminfo.put("nersys", ""); // Khám thực thể -- thầnquaHệ thốngKiểm traKết quả
        adminfo.put("spcy_info", ""); // chuyên khoatình hình
        adminfo.put("asst_exam_rslt", ""); // Kiểm tra hỗ trợKết quả
        adminfo.put("tcm4d_rslt", ""); // Đông y“bốnkhám”quan sátKết quảMô tả
        adminfo.put("syddclft", ""); // biệnchứng phútkiểumã
        adminfo.put("syddclft_name", ""); // biệnchứng phútkiểuTên
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
        adminfo.put("apgr", ""); // đánh giá phútGiá trị(Apgar)
        adminfo.put("diet_info", ""); // ăn uốngtình hình
        adminfo.put("growth_deg", ""); // phátsinhquy trìnhđộ
        adminfo.put("mtl_stas_norm", ""); // tinh thầnTrạng tháibình thườngcờ
        adminfo.put("slep_info", ""); // giấc ngủtrạngtình hình
        adminfo.put("sp_info", ""); // đặc biệttình hình
        adminfo.put("mind_info", ""); // tâmlýTrạng thái
        adminfo.put("nurt", ""); // dinh dưỡngTrạng thái
        adminfo.put("self_ablt", ""); // tựlýcó thểlực
        adminfo.put("nurscare_obsv_item_name", ""); // Hạng mục quan sát điều dưỡngTên
        adminfo.put("nurscare_obsv_rslt", ""); // điều dưỡngquan sátKết quả
        adminfo.put("smoke", ""); // hút thuốccờ
        adminfo.put("stop_smok_days", ""); // dừngdừnghút thuốc ngàysố
        adminfo.put("smok_info", ""); // hút thuốctrạngtình hình
        adminfo.put("smok_day", ""); // Ngàyhút thuốclượng(chi)
        adminfo.put("drnk", ""); // uống rượucờ
        adminfo.put("drnk_frqu", ""); // uống rượutần suấttỷ lệ
        adminfo.put("drnk_day", ""); // Ngàyuống rượulượng(mL)
        adminfo.put("eval_time", ""); // đánh giáNgàyThời gian
        adminfo.put("resp_nurs_code", ""); // trách nhiệmY táMã
        adminfo.put("resp_nurs_name", ""); // trách nhiệmY táTên
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
        coursrinfo.put("cas_ftur", ""); // ca bệnhđặc biệtđiểm
        coursrinfo.put("tcm4d_rslt", ""); // Đông y“bốnkhám”quan sátKết quả
        coursrinfo.put("dise_evid", ""); // Chẩn đoántheodữ liệu
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
        //Phẫu thuậtBản ghi, bệnhtìnhcấp cứu, chếtvongBản ghi, xuất việnnhỏkếtxây dựng TODO Phòng khámtạmvàtrướcgiữ
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
