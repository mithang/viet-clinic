package com.geeke.toll.service;



import com.geeke.common.data.Page;
import com.geeke.common.service.ServiceException;
import com.geeke.outpatient.entity.Patient;
import com.geeke.toll.dao.OutpatientLogDao;
import com.geeke.toll.entity.*;
import com.geeke.utils.StringUtils;
import com.geeke.utils.excel.ExcelExportBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nhật ký khám ngoại trúService
 * @author lc
 * @version 2022-06-15
 */
 
@Service("outpatientLogService")
@Transactional(readOnly = true)
public class OutpatientLogService {
    @Autowired
    private OutpatientLogDao outpatientLogDao;

    public OutpatientLog getLogs(String id) {
        return outpatientLogDao.getLogs(id);
    }

    /** X1: company của lượt khám / bệnh nhân, phục vụ kiểm tra sở hữu ở controller. */
    public String getRegistrationCompanyId(String id) {
        return outpatientLogDao.getRegistrationCompanyId(id);
    }

    public String getPatientCompanyId(String patientId) {
        return outpatientLogDao.getPatientCompanyId(patientId);
    }

    public Page<OutpatientLog> listLog(OutpatientLogRc outpatientLogRc){
        // DS204: chan limit vuot tran 10000 / limit am / offset am
        // (PageRequest.MAX_PAGE_SIZE) — truoc day limit client di thang
        // vao LIMIT #{limit} o getOutpatientLog/getOutpatientlist khong
        // co guard nao.
        if (!outpatientLogRc.isValidLimit()) {
            throw new IllegalArgumentException("Invalid limit: " + outpatientLogRc.getLimit());
        }
        if (!outpatientLogRc.isValidOffset()) {
            throw new IllegalArgumentException("Invalid offset: " + outpatientLogRc.getOffset());
        }
        List<OutpatientLog> list=outpatientLogDao.getOutpatientLog(outpatientLogRc);
        int total=list.size();
        List<OutpatientLog> list2=null;
        if(total>0){
            List<OutpatientLog> outpatientlist = outpatientLogDao.getOutpatientlist(outpatientLogRc);
            Patient patient = new Patient();
            List<Patient> povert = outpatientLogDao.getPovert(patient);
            for (OutpatientLog student : outpatientlist) {
                for (Patient po : povert) {
                    if (student.getCertificate().equals(po.getCard())) {
                        student.setPoverty(po.getPoverty());
                    }
                }
            }
            list2=outpatientlist;

            // list2=outpatientLogDao.getOutpatientlist(outpatientLogRc);
        }
        return new Page<>((long) total,list2);
    }

    @Transactional(readOnly = false)
    public int updateLog(OutpatientLog outpatientLogRc) {
        // DS117: null body — framework chan JSON-null o tang controller
        // nhung service tu ve (truoc day controller NPE o getId() truoc
        // ca khi vao service). Id la thi WHERE khong khop; id khong ton
        // tai thi UPDATE join khop 0 dong — ca hai 400 thay vi 200 rows
        // 0 silent. Chi kiem tra registration TON TAI (qua
        // getRegistrationCompanyId), khong dung rows==0: UPDATE join
        // registration+patient+medical_record+sys_user nen dong dang ky
        // that nhung thieu patient/doctor van tra 0 rows hop le (vd.
        // registration seed chi co 1 dong, patient/doctor NULL).
        if (outpatientLogRc == null) {
            throw new ServiceException("Nhật ký khám ngoại trú không được để trống");
        }
        if (StringUtils.isBlank(outpatientLogRc.getId())) {
            throw new ServiceException("Đăng ký khám không được để trống");
        }
        if (outpatientLogDao.getRegistrationCompanyId(outpatientLogRc.getId()) == null) {
            throw new ServiceException("Đăng ký khám không tồn tại");
        }
        return outpatientLogDao.updateLog(outpatientLogRc);
    }

    public List<Patient> getPoverty(String patientId) {
        List<Patient> poverty = outpatientLogDao.getPoverty(patientId);
        return poverty;
    }

    public void exportExcel(OutpatientLogRc outpatientLogRc, HttpServletResponse response) throws Exception {
        // DS204: export lay theo trang 10000 dong roi gop (DS203 pattern)
        // thay cho limit cua client — truoc day exportExcel dung
        // getOutpatientLog voi limit client nen limit=1000000 lay het bang
        // mot lan. Ghi de limit/offset cua body o day; listLog van giu
        // guard rieng o tren.
        List<OutpatientLog> list = new ArrayList<>();
        int exportOffset = 0;
        while (true) {
            outpatientLogRc.setOffset(exportOffset);
            outpatientLogRc.setLimit(com.geeke.common.data.PageRequest.MAX_PAGE_SIZE);
            List<OutpatientLog> page = outpatientLogDao.getOutpatientLog(outpatientLogRc);
            list.addAll(page);
            if (page.size() < com.geeke.common.data.PageRequest.MAX_PAGE_SIZE) {
                break;
            }
            exportOffset += com.geeke.common.data.PageRequest.MAX_PAGE_SIZE;
        }
        // ITER66 item 2: EasyExcel kèm POI cũ không có
        // Cell.setCellValue(LocalDateTime); EasyExcel tự đổi ô java.util.Date
        // sang LocalDateTime rồi ném NoSuchMethodError giữa chừng khi ghi
        // (response đã commit nên client chỉ thấy 200 + body rỗng — đã probe
        // pre-fix). Vì vậy chuyển 2 cột ngày sang chuỗi định dạng trước khi
        // ghi, để EasyExcel chỉ ghi ô chuỗi; không đổi phiên bản POI/EasyExcel,
        // không đổi ExcelExportBuilder (ảnh hưởng mọi đường xuất khác).
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> rows = new ArrayList<>();
        for (OutpatientLog log : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("patientName", log.getPatientName());
            row.put("patriarchName", log.getPatriarchName());
            row.put("sex", log.getSex());
            row.put("age", log.getAge());
            row.put("nation", log.getNation());
            row.put("occupation", log.getOccupation());
            row.put("address", log.getAddress());
            row.put("visitDate", log.getVisitDate() != null ? dateFmt.format(log.getVisitDate()) : "");
            row.put("initialVisit", log.getInitialVisit());
            row.put("bloodPressure", log.getBloodPressure());
            row.put("symptom", log.getSymptom());
            row.put("fever", log.getFever());
            row.put("epidemicDisease", log.getEpidemicDisease());
            row.put("westernDiagnose", log.getWesternDiagnose());
            row.put("chinaDiagnose", log.getChinaDiagnose());
            row.put("infect", log.getInfect());
            row.put("handle", log.getHandle());
            row.put("certificate", log.getCertificate());
            row.put("unit", log.getUnit());
            row.put("signature", log.getSignature());
            row.put("poverty", log.getPoverty());
            row.put("telephone", log.getTelephone());
            row.put("morbidityDate", log.getMorbidityDate() != null ? dateFmt.format(log.getMorbidityDate()) : "");
            row.put("positiveResult", log.getPositiveResult());
            row.put("healthEducation", log.getHealthEducation());
            rows.add(row);
        }
        new ExcelExportBuilder("Nhật ký khám ngoại trú")
            .addColumns(
                new ExcelExportBuilder.Column("Tên bệnh nhân", "patientName"),
                new ExcelExportBuilder.Column("Tên phụ huynh", "patriarchName"),
                new ExcelExportBuilder.Column("Giới tính", "sex"),
                new ExcelExportBuilder.Column("Tuổi bệnh nhân", "age"),
                new ExcelExportBuilder.Column("Dân tộc", "nation"),
                new ExcelExportBuilder.Column("Nghề nghiệp", "occupation"),
                new ExcelExportBuilder.Column("Địa chỉ chi tiết", "address"),
                new ExcelExportBuilder.Column("Ngày khám bệnh", "visitDate"),
                new ExcelExportBuilder.Column("ban đầu/Tái khám", "initialVisit"),
                new ExcelExportBuilder.Column("Huyết áp", "bloodPressure"),
                new ExcelExportBuilder.Column("Triệu chứng lâm sàng", "symptom"),
                new ExcelExportBuilder.Column("Sốt, nhiệt độ cơ thể tăng", "fever"),
                new ExcelExportBuilder.Column("Tiền sử dịch tễ", "epidemicDisease"),
                new ExcelExportBuilder.Column("Chẩn đoán tây y", "westernDiagnose"),
                new ExcelExportBuilder.Column("Chẩn đoán đông y", "chinaDiagnose"),
                new ExcelExportBuilder.Column("Bệnh truyền nhiễm", "infect"),
                new ExcelExportBuilder.Column("Tình hình xử lý", "handle"),
                new ExcelExportBuilder.Column("Số giấy tờ hiệu lực", "certificate"),
                new ExcelExportBuilder.Column("Đơn vị công tác", "unit"),
                new ExcelExportBuilder.Column("Chữ ký bác sĩ", "signature"),
                new ExcelExportBuilder.Column("Cờ hộ nghèo", "poverty"),
                new ExcelExportBuilder.Column("Số điện thoại liên hệ", "telephone"),
                new ExcelExportBuilder.Column("Ngày phát bệnh", "morbidityDate"),
                new ExcelExportBuilder.Column("Kết quả dương tính trong phòng thí nghiệm", "positiveResult"),
                new ExcelExportBuilder.Column("Giáo dục sức khỏe cá thể hóa", "healthEducation")
            )
            .data(rows)
            .write(response, "Nhật ký khám ngoại trú.xlsx");
    }
}