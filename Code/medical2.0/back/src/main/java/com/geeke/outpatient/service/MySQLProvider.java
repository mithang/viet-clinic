package com.geeke.outpatient.service;

import com.bstek.ureport.exception.ReportException;
import com.bstek.ureport.provider.report.ReportFile;
import com.bstek.ureport.provider.report.ReportProvider;
import com.geeke.outpatient.entity.UReportFile;
import com.geeke.org.entity.Company;
import com.geeke.utils.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySQLProvider implements ReportProvider {

    /**
     * lưu trữthiết bịTên
     */
    private static final String NAME = "Newtouch-provider";

    /**
     * báo cáoTên tệptiền tố
     */
    private String prefix = "Newtouch:";

    /**
     * Có hay khôngVô hiệu hóa
     */
    private boolean disabled = false;

    @Autowired
    private ReportFileService reportFileService;

    @Autowired
    private PatientService patientService;

    /**
     * TheoTên báo cáo tải báo cáoTệp
     *
     * @param file Tên báo cáo
     * @return Trả về của InputStream
     */
    @Override
    public InputStream loadReport(String file) {
        UReportFile reportFile = reportFileService.getReportFileByName(getCorrectName(file));
        ByteArrayInputStream stream = null;
        if (null != reportFile) {
            try {
                stream = new ByteArrayInputStream(reportFile.getContent().getBytes(StandardCharsets.ISO_8859_1));
            } catch (Exception e) {
                throw new ReportException(e);
            }
        }
        return stream;
    }

    /**
     * Theobáo cáotên, Xóachỉchỉ định của báo cáoTệp
     *
     * @param file Tên báo cáo
     */
    @Override
    public void deleteReport(String file) {
        UReportFile reportFile = reportFileService.getReportFileByName(getCorrectName(file));
        if (null != reportFile) {
            reportFileService.delete(reportFile);
        }
    }

    /**
     * Lấytất cả của báo cáoTệp
     *
     * @return Trả vềbáo cáoTệpDanh sách
     */
    @Override
    public List<ReportFile> getReportFiles() {
        List<UReportFile> list = reportFileService.getListAll();
        List<ReportFile> reportList = new ArrayList<>();
        for (UReportFile file : list) {
            reportList.add(new ReportFile(file.getName(), file.getUpdateDate()));
        }
        return reportList;
    }

    /**
     * Lưubáo cáoTệp
     *
     * @param file Tên báo cáo
     * @param content báo cáo của XMLnội dung
     */
    @Override
    public void saveReport(String file, String content) {
        file = getCorrectName(file);
        UReportFile reportFile = reportFileService.getReportFileByName(file);
        if(null == reportFile){
            reportFile = new UReportFile();
            reportFile.setName(file);
            reportFile.setContent(content);
            reportFile.setCreateBy("Kiểm thử");
            reportFile.setUpdateBy("Kiểm thử");
            // Đặttenant
            Company company = new Company();
            company.setId(SessionUtils.getLoginTenantId());
            reportFile.setCompany(company);
            reportFileService.save(reportFile);
        }else{
            reportFile.setContent(content);
            reportFileService.save(reportFile);
        }
    }

    /**
     * @return Trả vềlưu trữthiết bịTên
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * @return Trả vềCó hay khôngVô hiệu hóa
     */
    @Override
    public boolean disabled() {
        return disabled;
    }

    /**
     * @return Trả vềbáo cáoTên tệptiền tố
     */
    @Override
    public String getPrefix() {
        return prefix;
    }

    /**
     * LấyKhông cótiền tố của Tên tệp
     *
     * @param name Tên báo cáo
     */
    private String getCorrectName(String name){
        if(name.startsWith(prefix)){
            name = name.substring(prefix.length());
        }
        return name;
    }
}
