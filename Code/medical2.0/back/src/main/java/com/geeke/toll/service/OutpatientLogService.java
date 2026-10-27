package com.geeke.toll.service;



import com.geeke.common.data.Page;
import com.geeke.outpatient.entity.Patient;
import com.geeke.toll.dao.OutpatientLogDao;
import com.geeke.toll.entity.*;
import com.geeke.utils.excel.ExcelExportBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

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

    public Page<OutpatientLog> listLog(OutpatientLogRc outpatientLogRc){
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
        return outpatientLogDao.updateLog(outpatientLogRc);
    }

    public List<Patient> getPoverty(String patientId) {
        List<Patient> poverty = outpatientLogDao.getPoverty(patientId);
        return poverty;
    }

    public void exportExcel(OutpatientLogRc outpatientLogRc, HttpServletResponse response) throws Exception {
        List<OutpatientLog> list = outpatientLogDao.getOutpatientLog(outpatientLogRc);
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
            .data(list)
            .write(response, "Nhật ký khám ngoại trú.xlsx");
    }
}