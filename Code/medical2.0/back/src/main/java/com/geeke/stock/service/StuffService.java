package com.geeke.stock.service;

import cn.hutool.core.bean.BeanUtil;
import com.geeke.basicdata.dao.ManufactureFactoryDao;
import com.geeke.basicdata.entity.ManufactureFactory;
import com.geeke.basicdata.service.ManufactureFactoryService;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.sequence.service.SequenceService;
import com.geeke.common.service.CrudService;
import com.geeke.org.entity.Company;
import org.springframework.context.annotation.Lazy;
import com.geeke.org.service.CompanyService;
import com.geeke.stock.dao.StuffDao;
import com.geeke.stock.entity.MedicalProject;
import com.geeke.stock.entity.Stuff;
import com.geeke.sys.service.DictTypeService;
import com.geeke.sys.utils.SessionUserDto;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.utils.excel.ExcelImportBuilder;
import com.geeke.utils.excel.ExcelImportResult;
import com.geeke.utils.excel.ImportError;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Thông tin vật tưService
 *
 * @author txl
 * @version 2022-06-22
 */

@Service("stuffService")
@Transactional(readOnly = true)
public class StuffService extends CrudService<StuffDao, Stuff> {
    private static final Logger logger = LoggerFactory.getLogger(StuffService.class);

    /** vật tư tiêu hao là Cơ sởtừ điển dùng chung: Cho phép phòng khám thuộc cùng cơ sở truy cập dữ liệu phòng khám của cơ sở(Với danh sách #{institution} SQL Nhất quán) */
    @Override
    protected boolean isInstitutionShared() {
        return true;
    }

    @Autowired
    SequenceService sequenceService;
    @Autowired
    StuffDao stuffDao;
    @Lazy
    @Autowired
    private MedicinalStockControlService medicinalStockControlService;
    @Autowired
    private MedicinalStorageControlService medicinalStorageControlService;
    @Autowired
    private CompanyService companyService;

    @Autowired
    private DictTypeService dictTypeService;

    @Autowired
    private ManufactureFactoryDao manufactureFactoryDao;

    @Autowired
    private ManufactureFactoryService manufactureFactoryService;

    /**
     * Trích xuất từ tham số company_id Và xây dựng kèm tenantThông tin của  PageRequest
     * Sử dụng Parameter.extractAndRemoveCompanyId thống nhấtXử lý
     */
    private PageRequest buildStuffTenantPageRequest(List<Parameter> params, int offset, int limit, String orderby) {
        // Loại bỏ ở frontend company_id(Ngăn vượt quyền), Thống nhất lấy tenant đăng nhập trong phiên làm ranh giới cách ly
        Parameter.extractAndRemoveCompanyId(params);
        String id = SessionUtils.getLoginTenantId();
        if ("null".equals(id)) {
            id = null;
        }
        String institution = companyService.getInstitution(id);
        return new PageRequest(offset, limit, params, orderby, id, institution);
    }

    /**
     * Trích xuất từ tham số company_id Và xây dựng kèm tenantThông tin của  PageRequest(không phúttrang)
     */
    private PageRequest buildStuffTenantPageRequest(List<Parameter> params, String orderby) {
        String id = Parameter.extractAndRemoveCompanyId(params);
        String institution = companyService.getInstitution(id);
        return new PageRequest(params, orderby, id, institution);
    }

    @Override
    @Transactional(readOnly = false)
    public Stuff save(Stuff stuff) {
        // Kiểm traVật tưCó hay khôngtrùng lặp
        final int repeat = this.repeat(stuff);
        if (repeat!=0){
            throw new NegativeArraySizeException("材料信息重复");

        }
        // Khi thêm mới, Xử lý trường tự động đánh số
        if (StringUtils.isBlank(stuff.getId())) {
            String sn = sequenceService.generate(SessionUtils.getUser().getCompanyId(), "stuff_code", stuff);
            stuff.setCode(sn);
        }

        Stuff stuffTemp = super.save(stuff);

        //LưuđộngTồn kho
        this.medicinalStockControlService.initStock(stuffTemp);
        return stuffTemp;
    }

    public List<MedicalProject> listPageForMedical(List<Parameter> parameters, String orderby) {
        PageRequest pageRequest = new PageRequest(parameters, orderby);
        int total = this.dao.count(pageRequest);
        List<Stuff> lis = null;
        List<MedicalProject> list = new ArrayList<>();
        if (total > 0) {
            lis = this.dao.listAll(pageRequest);
            lis.stream().forEach(stuff -> {
                MedicalProject medicalProject = new MedicalProject();
                BeanUtils.copyProperties(stuff, medicalProject);
                medicalProject.setStuffType("4");
                medicalProject.setDosisUnit(stuff.getMinUnit());
                medicalProject.setPack(stuff.getPackUnit());
                list.add(medicalProject);
            });
        }
        return list;
    }

    // Đã废弃 — stuffDao.updateInventory Phương thứcKhông tồn tại, vàtất cảgọiĐã注释
//    @Transactional(readOnly = false)
//    public void updateInventory(int inventory, String id) {
//        Stuff stuff = this.dao.get(id);
//        stuff.setInventory(stuff.getInventory() + inventory);
//        stuffDao.updateInventory(stuff.getId(), stuff.getInventory());
//    }

    @Transactional
    public List<Stuff> getAll(String companyId, String type) {
        List<Stuff> stuffs = this.dao.getAll(companyId, type);
        return stuffs;
    }

    public Page<Stuff> getStuff(List<Parameter> params, int offset, int limit, String orderby) {
        PageRequest pageRequest = buildStuffTenantPageRequest(params, offset, limit, orderby);
        //PageRequest pageRequest = new PageRequest(offset, limit, params, orderby);
        return paginate(
            () -> this.dao.count(pageRequest),
            () -> {
                List<Stuff> list = this.dao.listPages(pageRequest);
                //Sau khi lấy được vật tư hiện tại, lấy giá nhập từ chi tiết tồn kho và tính tổng chi phí
                for (Stuff stuff : list) {
                    //Lấy tổng chi phí theo tồn kho động
                    BigDecimal totalCost = medicinalStorageControlService.getByDrugOrStuffId(stuff.getId());
                    if (!Objects.isNull(stuff.getStock()) && !Objects.isNull(stuff.getStock().getStorageStock())) {
                        String inventory =
                                stuff.getStock().getStorageStock().subtract((stuff.getStock().getUsedStock().add(stuff.getStock().getReimburseStock()))).stripTrailingZeros().toPlainString();
                        stuff.setInventory(Integer.parseInt(inventory));
                    } else {
                        stuff.setInventory(0);
                    }
                    if (!Objects.isNull(totalCost)) {
                        stuff.setBid(totalCost);
                    } else {
                        stuff.setBid(new BigDecimal("0"));
                    }
                }
                return list;
            }
        );
    }

    @Transactional(readOnly = false)
    public int updateAllIndate(String indate, String companyId) {
        return this.dao.updateAllIndate(indate, companyId);
    }

    @Transactional(readOnly = false)
    public int updateAllInventory(String inventoryFloor, String companyId) {
        return this.dao.updateAllInventory(inventoryFloor, companyId);
    }

    public Page<Stuff> getStuffInventoryWarning(List<Parameter> params, int offset, int limit, String orderby) {
        PageRequest pageRequest = buildStuffTenantPageRequest(params, offset, limit, orderby);
        return paginate(
            () -> this.dao.getCountWarning(pageRequest),
            () -> this.dao.getStuffInventoryWarning(pageRequest)
        );
    }

    public Page<Stuff> listPages(List<Parameter> parameters, int offset, int limit, String order) {
        PageRequest pageRequest = buildStuffTenantPageRequest(parameters, offset, limit, order);
        return paginate(
            () -> dao.count(pageRequest),
            () -> stuffDao.listPages(pageRequest)
        );
    }

    public List<Stuff> listAlls(List<Parameter> parameters, String orderby) {
        PageRequest pageRequest = buildStuffTenantPageRequest(parameters, orderby);
        return dao.listAlls(pageRequest);
    }

    public List<Stuff> inventory(List<Parameter> parameters, String orderby) {
        PageRequest pageRequest = buildStuffTenantPageRequest(parameters, orderby);
        return dao.inventory(pageRequest);
    }

    @Transactional(readOnly = false)
    public List<String> excel(MultipartFile file) throws Exception {
        String companyId = com.geeke.sys.utils.SessionUtils.getUserJson().getString("companyId");
        Company company = new Company();
        company.setId(companyId);

        // Kiểm traTệpđịnh dạng
        String fileName = file.getOriginalFilename();
        if (!fileName.matches("^.+\\.(?i)(xls)$") && !fileName.matches("^.+\\.(?i)(xlsx)$")) {
            throw new Exception("Tải lênTệpđịnh dạngkhôngđúng");
        }

        int chengGong = 0;
        int siBai = 0;
        StringBuilder mistake = new StringBuilder();

        // Step 1: Sử dụng ExcelImportBuilder đọcvàKiểm tra Excel Dữ liệu
        ExcelImportResult<Stuff> result;
        try (InputStream is = file.getInputStream()) {
            result = new ExcelImportBuilder<>(Stuff.class, is)
                    .dictTypeService(dictTypeService)
                    .skipRows(2)
                    .read();
        }

        // Step 2: Lạimởcông việclàm簿, đọcVật tưTên(Cột0) và Tên nhà sản xuất(Cột4)
        List<String> nameList = new ArrayList<>();
        List<String> manufacturerNames = new ArrayList<>();
        try (InputStream is2 = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(is2)) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();
            for (int i = 2; i < rows; i++) {
                XSSFRow row = sheet.getRow(i);

                String name = null;
                if (row != null && row.getCell(0) != null) {
                    name = row.getCell(0).getStringCellValue();
                }
                nameList.add(name);

                String mfgName = null;
                if (row != null && row.getCell(4) != null) {
                    mfgName = row.getCell(4).getStringCellValue();
                }
                manufacturerNames.add(mfgName);
            }
        }

        // Step 3: Xử lýKiểm traThất bại của  dòng(ExcelImportBuilder pháthiện của TrườngcấpLỗi)
        java.util.Map<Integer, java.util.List<ImportError>> errorsByRow = new java.util.LinkedHashMap<>();
        for (ImportError err : result.getErrors()) {
            errorsByRow.computeIfAbsent(err.getRowNum(), k -> new java.util.ArrayList<>()).add(err);
        }
        for (java.util.Map.Entry<Integer, java.util.List<ImportError>> entry : errorsByRow.entrySet()) {
            siBai++;
            StringBuilder rowMsg = new StringBuilder();
            rowMsg.append("Bảng dòng ").append(entry.getKey()).append(" dòng");
            for (ImportError err : entry.getValue()) {
                if (err.getFieldName() != null) {
                    rowMsg.append("[").append(err.getFieldName()).append("]");
                }
            }
            rowMsg.append("Dữ liệuKiểm traThất bại: ");
            for (ImportError err : entry.getValue()) {
                rowMsg.append(err.getMessage()).append("; ");
            }
            rowMsg.append("Vui lòng đối chiếu rồi nhập lại\n");
            mistake.append(rowMsg);
        }

        // Step 4: Xử lýKiểm traQua của  dòng, từng dòngLưu
        List<Stuff> dataList = result.getDataList();
        for (int i = 0; i < dataList.size(); i++) {
            Stuff stuff = dataList.get(i);
            int rowNum = i + 3; // Excel Số dòng(Bỏ qua2 dòngbảngtiêu đề, Số dòngTừ 1Bắt đầu)
            String stuffName = (i < nameList.size() && nameList.get(i) != null) ? nameList.get(i) : "";
            try {
                // ĐặtPhòng khám
                stuff.setCompany(company);

                // ĐặtVật tưTên(Cột0)
                if (i < nameList.size() && StringUtils.isNotBlank(nameList.get(i))) {
                    stuff.setName(nameList.get(i));
                }

                // Xử lýNhà sản xuất(Cột4): theoTêntìm kiếm, Không tồn tạithìtạo
                if (i < manufacturerNames.size() && StringUtils.isNotBlank(manufacturerNames.get(i))) {
                    String mfgName = manufacturerNames.get(i);
                    ManufactureFactory manufactureFactory = new ManufactureFactory();
                    String existingId = manufactureFactoryDao.findBy(mfgName, companyId);
                    if (existingId != null) {
                        manufactureFactory.setId(existingId);
                    } else {
                        manufactureFactory.setName(mfgName);
                        manufactureFactory.setType("2");
                        manufactureFactory.setCompany(company);
                        ManufactureFactory savedFactory = manufactureFactoryService.save(manufactureFactory);
                        manufactureFactory.setId(savedFactory.getId());
                    }
                    stuff.setFactory(manufactureFactory);
                }

                // Xử lýCó hay khôngbán ra ngoài: "是" -> "1", Khác -> "0"
                if ("是".equals(stuff.getIsOutSell())) {
                    stuff.setIsOutSell("1");
                } else {
                    stuff.setIsOutSell("0");
                }

                // Xử lýCó bán lẻ không: "是" -> "1", Khác -> "0"
                if ("是".equals(stuff.getIsUnpackSell())) {
                    stuff.setIsUnpackSell("1");
                } else {
                    stuff.setIsUnpackSell("0");
                }

                // Xử lýTrạng thái: "是" -> "1", Khác -> "0"
                if ("是".equals(stuff.getStatus())) {
                    stuff.setStatus("1");
                } else {
                    stuff.setStatus("0");
                }

                this.save(stuff).getId();
                chengGong++;
            } catch (Exception e) {
                logger.warn("NhậpDòng {} dòng thất bại", rowNum, e);
                String a = "";
                if (e.getMessage() != null) {
                    a = e.getMessage().equals("材料信息重复") ? "Thông tin vật tưtrùng lặp," : "";
                }
                siBai++;
                mistake.append("Bảng dòng ").append(rowNum).append(" dòng[").append(stuffName).append("]Thông tin vật tưngoại lệ, ").append(a).append("Vui lòng đối chiếu rồi nhập lại\n");
            }
        }

        List<String> error = new ArrayList<>();
        error.add(0, String.valueOf(chengGong));
        error.add(1, String.valueOf(siBai));
        error.add(2, mistake.toString());
        return error;
    }

    // Vật tưThêm mớitratrọng
    public int repeat(Stuff stuff) {
        String companyId = SessionUtils.getUserDto().getCompanyId();
        Company company = companyService.get(companyId);
        String institution = companyService.getInstitution(companyId);
        company.getParent().setId(institution);
        stuff.setCompany(company);
        return this.dao.repeat(stuff);
    }


    public Page<Stuff> listByCompany(List<Parameter> params, int offset, int limit, String orderby) {
        Parameter.extractAndRemoveCompanyId(params);
        String id = SessionUtils.getLoginTenantId();
        if ("null".equals(id)) {
            id = null;
        }
        PageRequest pageRequest = new PageRequest(offset, limit, params, orderby, id);
        return paginate(
            () -> dao.count(pageRequest),
            () -> dao.listPages(pageRequest)
        );
    }

    public Page<Stuff> listByInstitution(List<Parameter> params, int offset, int limit, String orderby) {
        PageRequest pageRequest = buildStuffTenantPageRequest(params, offset, limit, orderby);
        return paginate(
            () -> dao.countByInstitution(pageRequest),
            () -> dao.listByInstitution(pageRequest)
        );
    }

    @Transactional(readOnly = false)
    public List<String> syncToClinic(List<Stuff> entitys) {
        SessionUserDto userDto = SessionUtils.getUserDto();
        SessionUserDto.CompanyDTO companyDto = userDto.getCompany();
        Company company = BeanUtil.copyProperties(companyDto, Company.class);

        List<String> list = new ArrayList<>();

        entitys.forEach(stuff -> {
            stuff.setCompany(company);
            String oldId = stuff.getId();
            int repeat = repeatInCompany(stuff);
            if (repeat == 0) {
                stuff.setId(null);
                stuff.setSyncId(oldId);
                String sn = sequenceService.generate(company.getId(), "stuff_code", stuff);
                stuff.setCode(sn);
                super.save(stuff);
                //LưuđộngTồn kho
                this.medicinalStockControlService.initStock(stuff);
                list.add(stuff.getId());
            }

        });

        return list;
    }

    private int repeatInCompany(Stuff stuff) {
        String companyId = SessionUtils.getUserDto().getCompanyId();
        Company company = companyService.get(companyId);
        String institution = companyService.getInstitution(companyId);
        company.getParent().setId(institution);
        stuff.setCompany(company);
        int i = this.dao.repeatInCompany(stuff);
        return i;
    }


}