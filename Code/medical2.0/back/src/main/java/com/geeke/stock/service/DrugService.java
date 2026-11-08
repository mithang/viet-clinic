package com.geeke.stock.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.geeke.common.constants.BizConstants;
import com.geeke.basicdata.dao.ManufactureFactoryDao;
import com.geeke.basicdata.entity.ManufactureFactory;
import com.geeke.basicdata.service.ManufactureFactoryService;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.sequence.service.SequenceService;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.org.entity.Company;
import com.geeke.org.service.CompanyService;
import com.geeke.stock.dao.DrugDao;
import com.geeke.stock.entity.Drug;
import com.geeke.sys.entity.DictItem;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Thông tin thuốcService
 *
 * @author txl
 * @version 2022-06-07
 */

@Service("drugService")
@Transactional(readOnly = true)
public class DrugService extends CrudService<DrugDao, Drug>  {
    private static final Logger logger = LoggerFactory.getLogger(DrugService.class);

    /** Thuốc là Cơ sởtừ điển dùng chung: Cho phép phòng khám thuộc cùng cơ sở truy cập dữ liệu phòng khám của cơ sở(Với danh sách #{institution} SQL Nhất quán) */
    @Override
    protected boolean isInstitutionShared() {
        return true;
    }

    @Autowired
    SequenceService sequenceService;
    @Autowired
    DrugDao drugDao;
    @Autowired
    private MedicinalStockControlService medicinalStockControlService;
    @Autowired
    private MedicinalStorageControlService medicinalStorageControlService;
    @Autowired
    private DictTypeService dictTypeService;
    @Autowired
    private ManufactureFactoryService manufactureFactoryService;
    @Autowired
    private CompanyService companyService;

    @Autowired
    private ManufactureFactoryDao manufactureFactoryDao;

    @Override
    @Transactional(readOnly = false)
    public Drug save(Drug drug) {
        // DS122 A: id khong ton tai truoc day roi qua super.save -> doUpdate
        // 0 dong roi chi 400 nho initStock resolve (sau khi super.save da
        // ghi audit UPDATED, duoc rollback theo transaction); chan som
        // truoc moi ghi, cung lop DS94/DS99.
        if (StringUtils.isNotBlank(drug.getId()) && drugDao.get(drug.getId()) == null) {
            throw new ServiceException("Thuốc không tồn tại: " + drug.getId());
        }
        // Kiểm traThuốcCó hay khôngtrùng lặp
        int repeat = this.repeat(drug);
        if (repeat!=0){
            throw new ServiceException("Thông tin thuốc trùng lặp");

        }
        // Khi thêm mới, Xử lý trường tự động đánh số
        if (StringUtils.isBlank(drug.getId())){
            String sn = sequenceService.generate(SessionUtils.getUser().getCompanyId(), "medical_code", drug);
            drug.setCode(sn);
            //ThêmThuốcggTrường
        }
        // DS148 A: chan do dai truoc moi xu ly (ca duong save don voi
        // truong khong @Length nhu pinyinCode, va duong bulk* goi save
        // dau -- bulkInsert/bulkUpdate goi helper rieng duoi day).
        checkDrugLengths(drug);
        // DS79: ypgg truoc day deref thang getName() tren dosisUnit /
        // preparationUnit / pack nen null NPE 400 chung chung. Form bat
        // buoc preparationUnit + pack (da @NotNull), nhung dosisUnit
        // khong bat buoc nen ypgg phai null-safe thay vi NPE.
        String dosisUnitName = drug.getDosisUnit() != null && drug.getDosisUnit().getName() != null
                ? drug.getDosisUnit().getName() : "";
        String preparationUnitName = drug.getPreparationUnit() != null && drug.getPreparationUnit().getName() != null
                ? drug.getPreparationUnit().getName() : "";
        String packName = drug.getPack() != null && drug.getPack().getName() != null
                ? drug.getPack().getName() : "";
        String ypgg = drug.getDosis() + dosisUnitName + "*" + drug.getPreparation() + preparationUnitName + "/" + packName;
        drug.setYpgg(ypgg);
        // DS148 A: ypgg tinh toan co the vuot varchar(100) ngay ca khi
        // tung truong dau vao deu trong gioi han (dosis 45 + preparation
        // 45 + ten don vi dai) -- truoc day 500/400-lo-SQL o mapper
        // (probe A6: ypgg 212). Chan sach o day thay vi de DB nem.
        checkLength(ypgg, 100, "Quy cách thuốc Độ dài phải nằm trong khoảng 0 đến 100");
        Drug drugTemp = super.save(drug);
        this.medicinalStockControlService.initStock(drugTemp);
        return drugTemp;
    }

    /**
     * DS122 A: bulk* fail-closed 400. bulkInsert/bulkUpdate khong qua
     * save() nen khong co repeat(), initStock hay @Valid (CrudController
     * bulk* khong @Valid): dong thieu goodsName/status -> 500 NOT NULL,
     * id ma -> 200 silent no-op, [null] -> NPE 500. Validate toan bo list
     * truoc moi ghi (1 dong loi thi khong dong nao duoc ghi, cung
     * transaction): null/empty/[null] -> 400; thieu truong @NotNull
     * (goodsName, type, status, preparationUnit, pack) -> 400; trung lap
     * (repeat() voi DB + trung trong list) -> 400; bulkUpdate/bulkDelete
     * id khong ton tai -> 400 (kiem tra stored-row tenant truoc repeat()
     * vi repeat() ghi de company theo session). Sau ghi, initStock tung
     * dong nhu save() de giu bat bien thuoc <-> medicinal_stock_control.
     */
    private void checkDrugRequired(Drug drug) {
        if (drug == null) {
            throw new ServiceException("Thông tin thuốc không được để trống");
        }
        if (StringUtils.isBlank(drug.getGoodsName())
                || drug.getType() == null
                || StringUtils.isBlank(drug.getType().getValue())
                || StringUtils.isBlank(drug.getStatus())
                || drug.getPreparationUnit() == null
                || StringUtils.isBlank(drug.getPreparationUnit().getValue())
                || drug.getPack() == null
                || StringUtils.isBlank(drug.getPack().getValue())) {
            throw new ServiceException("Tên thuốc, loại thuốc, trạng thái, đơn vị bào chế và đóng gói không được để trống");
        }
    }

    /**
     * DS148 A: chan do dai qua dai qua bulk/save. bulkInsert/bulkUpdate
     * khong qua @Valid (CrudController bulk* khong @Valid) nen cac truong
     * co @Length (goodsName 128, brandName 128, code 64, source 128,
     * standardCode/bitCode/barCode/insuranceCode 64, dosis/preparation/
     * isUnpackSell 45, status 1) van 500 o mapper khi qua bulk (probe
     * A1/A4); cac truong KHONG co @Length: pinyinCode(64),
     * locationNumber/inventoryLimit/inventoryFloor/indate(255), ypgg(100)
     * 500 ca duong save don (probe A2: single save pinyinCode 65 ve 400
     * nhung msg van lo SQL tho qua DS94 handler, khong phai 400 sach).
     * Gia tri DictItem type/nature/dosisUnit/preparationUnit/pack viet
     * thang vao varchar(64), western/chinese use, frequency, days vao
     * varchar(255) -- @Length(128) cua DictItem chi guard bang dict
     * (probe A3: type value 65 -> 500). Gioi han lay theo cot DB, giong
     * mau checkCostItemLengths (DS146 A / DS147 B). Null-safe: truong
     * null thi bo qua nhu cu (mapper viet NULL).
     */
    private static void checkDrugLengths(Drug drug) {
        if (drug == null) {
            return;
        }
        checkLength(drug.getGoodsName(), 128, "Thuốc Tên Độ dài phải nằm trong khoảng 1 đến 128");
        checkLength(drug.getBrandName(), 128, "Tên hàng hóa Độ dài phải nằm trong khoảng 0 đến 128");
        checkLength(drug.getCode(), 64, "Thuốc Độ dài mã phải nằm trong khoảng 0 đến 64");
        checkLength(drug.getSource(), 128, "Nguồn thuốc Độ dài phải nằm trong khoảng 0 đến 128");
        checkLength(drug.getStandardCode(), 64, "Độ dài số chuẩn thuốc quốc gia phải nằm trong khoảng 0 đến 64");
        checkLength(drug.getBitCode(), 64, "này vị mã Độ dài phải nằm trong khoảng 0 đến 64");
        checkLength(drug.getBarCode(), 64, "mã vạch Độ dài phải nằm trong khoảng 0 đến 64");
        checkLength(drug.getInsuranceCode(), 64, "BHYT Độ dài mã phải nằm trong khoảng 0 đến 64");
        checkLength(drug.getDosis(), 45, "liều lượng Độ dài phải nằm trong khoảng 0 đến 45");
        checkLength(drug.getPreparation(), 45, "bào chế Độ dài phải nằm trong khoảng 0 đến 45");
        checkLength(drug.getIsUnpackSell(), 45, "Độ dài cho phép bán lẻ phải nằm trong khoảng 0 đến 45");
        checkLength(drug.getStatus(), 1, "Cờ bật Độ dài phải nằm trong khoảng 0 đến 1");
        checkLength(drug.getPinyinCode(), 64, "Mã pinyin Độ dài phải nằm trong khoảng 0 đến 64");
        checkLength(drug.getLocationNumber(), 255, "Số vị trí hàng Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(drug.getInventoryLimit(), 255, "Giới hạn trên tồn kho Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(drug.getInventoryFloor(), 255, "Giới hạn dưới tồn kho Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(drug.getIndate(), 255, "Thời hạn hiệu lực Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(drug.getYpgg(), 100, "Quy cách thuốc Độ dài phải nằm trong khoảng 0 đến 100");
        checkLength(drug.getRemarks(), 255, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
        checkDictLength(drug.getType(), 64, "Loại thuốc Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(drug.getNature(), 64, "Tính chất Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(drug.getDosisUnit(), 64, "Đơn vị liều lượng Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(drug.getPreparationUnit(), 64, "Đơn vị bào chế Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(drug.getPack(), 64, "Đóng gói Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(drug.getWesternMedicineUse(), 255, "Cách dùng thuốc Tây Độ dài phải nằm trong khoảng 0 đến 255");
        checkDictLength(drug.getChineseMedicineUse(), 255, "Cách dùng thuốc Đông y Độ dài phải nằm trong khoảng 0 đến 255");
        checkDictLength(drug.getFrequency(), 255, "Tần suất và liều dùng Độ dài phải nằm trong khoảng 0 đến 255");
        checkDictLength(drug.getDays(), 255, "Số ngày Độ dài phải nằm trong khoảng 0 đến 255");
    }

    private static void checkDictLength(com.geeke.sys.entity.DictItem item, int max, String message) {
        if (item != null) {
            checkLength(item.getValue(), max, message);
        }
    }

    private static void checkLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ServiceException(message);
        }
    }

    private void checkDrugRepeatInList(List<Drug> entitys) {
        Set<String> seen = new HashSet<>();
        for (Drug drug : entitys) {
            if (drug == null) {
                continue;
            }
            String key = String.valueOf(drug.getGoodsName()) + "|"
                    + String.valueOf(drug.getDosis()) + "|"
                    + String.valueOf(drug.getPreparation()) + "|"
                    + String.valueOf(drug.getPrice());
            if (!seen.add(key)) {
                throw new ServiceException("Thông tin thuốc trùng lặp");
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<Drug> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách thuốc không được để trống");
        }
        checkDrugRepeatInList(entitys);
        for (Drug drug : entitys) {
            checkDrugRequired(drug);
            checkDrugLengths(drug);
            // repeat() ep company theo session + kiem tra trung voi DB
            // (nhu save()); 1 dong trung thi ca list dung truoc moi ghi.
            if (this.repeat(drug) != 0) {
                throw new ServiceException("Thông tin thuốc trùng lặp");
            }
        }
        List<String> ids = super.bulkInsert(entitys);
        for (Drug drug : entitys) {
            this.medicinalStockControlService.initStock(drug);
        }
        return ids;
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<Drug> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách thuốc không được để trống");
        }
        for (Drug drug : entitys) {
            if (drug == null || StringUtils.isBlank(drug.getId())) {
                throw new ServiceException("Thông tin thuốc không được để trống");
            }
            if (drugDao.get(drug.getId()) == null) {
                throw new ServiceException("Thuốc không tồn tại: " + drug.getId());
            }
            verifyTenantOwnershipAgainstStoredRow(drug);
            checkDrugRequired(drug);
            checkDrugLengths(drug);
            // repeat() id-aware: dong trung voi chinh no tra 0; dong trung
            // voi thuoc khac -> 400.
            if (this.repeat(drug) != 0) {
                throw new ServiceException("Thông tin thuốc trùng lặp");
            }
        }
        List<String> ids = super.bulkUpdate(entitys);
        for (Drug drug : entitys) {
            this.medicinalStockControlService.initStock(drug);
        }
        return ids;
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<Drug> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách thuốc không được để trống");
        }
        for (Drug drug : entitys) {
            if (drug == null || StringUtils.isBlank(drug.getId())) {
                throw new ServiceException("Thông tin thuốc không được để trống");
            }
            if (drugDao.get(drug.getId()) == null) {
                throw new ServiceException("Thuốc không tồn tại: " + drug.getId());
            }
            verifyTenantOwnershipAgainstStoredRow(drug);
        }
        return super.bulkDelete(entitys);
    }

    /**
     * DS123 A: single delete fail-closed 400. CrudService.delete chi
     * chan qua stored-row tenant check, ma check do bo qua stored==null
     * nen delete id ma tra 200 data=0 (DS94 class, nhu bulkDelete truoc
     * DS122); mapper delete khong loc del_flag nen xoa dong da xoa mem
     * tra 200 rows=1 va ghi them 1 audit DELETED (probe A4). Kiem tra
     * ton tai truoc: id rong/id ma/dong da xoa mem (del_flag != '0')
     * -> 400. Khong co caller noi bo nao goi delete() cua service nay.
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(Drug drug) {
        if (drug == null || StringUtils.isBlank(drug.getId())) {
            throw new ServiceException("Không xác định được thuốc cần xóa.");
        }
        Drug stored = drugDao.get(drug.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Thuốc không tồn tại: " + drug.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(drug);
        return super.delete(drug);
    }


    public int addDurg(Drug drug) {
        return dao.insert(drug);
    }

    public Page<Drug> listPages(List<Parameter> parameters, int offset, int limit, String order) {
        PageRequest pageRequest = buildTenantPageRequest(parameters, offset, limit, order);
        return paginate(
            () -> dao.count(pageRequest),
            () -> drugDao.listPages(pageRequest)
        );
    }

    /**
     * nhập kho giờTruy vấnbản thânPhòng khámtất cảThông tin thuốc 2025.4.2
     * @param parameters
     * @param orderby
     * @return
     */
    public List<Drug> listAllDrug(List<Parameter> parameters, String orderby) {
        // Z2b: ép tenant session (trước đây PageRequest trần, listAlls chỉ lọc khi institution != null)
        PageRequest pageRequest = buildTenantPageRequest(parameters, orderby);

        return dao.listAlls(pageRequest);
    }


    public List<Drug> listAlls(List<Parameter> parameters, String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(parameters, orderby);
        return dao.listAlls(pageRequest);
    }

    public List<Drug> listAllStock(List<Parameter> parameters, String orderby) {
        // Z2b: ép tenant session (trước đây PageRequest trần, SQL không có lọc tenant nào)
        PageRequest pageRequest = buildTenantPageRequest(parameters, orderby);
        return this.dao.listAllStock(pageRequest);
    }

    public List<Drug> listAllStock2(List<Parameter> parameters, String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(parameters, orderby);
        return this.dao.listAllStock2(pageRequest);
    }

    public List<Drug> getAll(String companyId, String type) {
        List<Drug> drugs = this.dao.getAll(companyId, type);
        return drugs;
    }

    /**
     * 2025.4.2 @update
     * LấynàyPhòng khámdướitất cảTồn kho
     * @return
     */
    public Page<Drug> getDrugNew(List<Parameter> params, int offset, int limit, String orderby) {
        // Z2b: ép tenant session đầy đủ (trước đây chỉ truyền id, không strip body,
        // listPages/count chỉ lọc khi institution != null nên testfull không được lọc)
        PageRequest pageRequest = buildTenantPageRequest(params, offset, limit, orderby);
        return paginate(
            () -> this.dao.count(pageRequest),
            () -> {
                List<Drug> list = this.dao.listPages(pageRequest);
                //Sau khi lấy được thuốc hiện tại, lấy giá nhập từ chi tiết tồn kho và tính tổng chi phí
                for (Drug drug : list) {
                    //Lấy tổng chi phí theo tồn kho động
                    BigDecimal totalCost = medicinalStorageControlService.getByDrugOrStuffId(drug.getId());
                    if (!Objects.isNull(drug.getStock()) && !Objects.isNull(drug.getStock().getStorageStock())) {
                        BigDecimal inventory =
                                drug.getStock().getStorageStock().subtract(drug.getStock().getUsedStock().add(drug.getStock().getReimburseStock()));
                        drug.setInventory(inventory.intValue());
                    } else {
                        drug.setInventory(0);
                    }
                    if (!Objects.isNull(totalCost)) {
                        drug.setBid(totalCost);
                    } else {
                        drug.setBid(new BigDecimal("0"));
                    }
                }
                return list;
            }
        );
    }

    /**
     * Lấytenanttất cảTồn kho Phòng khámTổng dùng
     * @param params
     * @param offset
     * @param limit
     * @param orderby
     * @return
     */
    public Page<Drug> getDrug(List<Parameter> params, int offset, int limit, String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(params, offset, limit, orderby);
        return paginate(
            () -> this.dao.count(pageRequest),
            () -> {
                List<Drug> list = this.dao.listPages(pageRequest);
                //Sau khi lấy được thuốc hiện tại, lấy giá nhập từ chi tiết tồn kho và tính tổng chi phí
                for (Drug drug : list) {
                    //Lấy tổng chi phí theo tồn kho động
                    BigDecimal totalCost = medicinalStorageControlService.getByDrugOrStuffId(drug.getId());
                    if (!Objects.isNull(drug.getStock()) && !Objects.isNull(drug.getStock().getStorageStock())) {
                        BigDecimal inventory =
                                drug.getStock().getStorageStock().subtract(drug.getStock().getUsedStock().add(drug.getStock().getReimburseStock()));
                        drug.setInventory(inventory.intValue());
                    } else {
                        drug.setInventory(0);
                    }
                    if (!Objects.isNull(totalCost)) {
                        drug.setBid(totalCost);
                    } else {
                        drug.setBid(new BigDecimal("0"));
                    }
                }
                return list;
            }
        );
    }

    @Transactional(readOnly = false)
    public int updateAllIndate(String indate, String companyId) {
        int i = this.dao.updateAllIndate(indate, companyId);
        return i;
    }

    @Transactional(readOnly = false)
    public int updateAllInventory(String inventoryFloor, String companyId) {
        int i = this.dao.updateAllInventory(inventoryFloor, companyId);
        return i;
    }

    public Page<Drug> getDrugInventoryWarning(List<Parameter> parameters, int offset, int limit, String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(parameters, offset, limit, orderby);
        return paginate(
            () -> this.dao.countInventory(pageRequest),
            () -> this.dao.getDrugInventoryWarning(pageRequest)
        );
    }

    public List<Drug> inventory(List<Parameter> parameters, String orderby) {
        // Loại bỏ ở frontend company_id(Ngăn vượt quyền), Thống nhất lấy tenant đăng nhập trong phiên làm ranh giới cách ly
        Parameter.extractAndRemoveCompanyId(parameters);
        String id = SessionUtils.getLoginTenantId();
        if ("null".equals(id)) {
            id = null;
        }
        String institution = null;
        if (id != null) {
            Company company = companyService.get(id);
            institution = company.getParent().getId();
        }
        PageRequest pageRequest = new PageRequest(parameters, orderby, id, institution);
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
            throw new Exception("Tải lên Tệp định dạng không đúng");
        }

        int chengGong = 0;
        int siBai = 0;
        StringBuilder mistake = new StringBuilder();

        // Step 1: Sử dụng ExcelImportBuilder đọcvàKiểm tra Excel Dữ liệu
        ExcelImportResult<Drug> result;
        try (InputStream is = file.getInputStream()) {
            result = new ExcelImportBuilder<>(Drug.class, is)
                    .dictTypeService(dictTypeService)
                    // DS151 B: skipRows(2) nuot dong du lieu dau (dong 3)
                    // (cung loi DS150 C da sua o StuffService): template
                    // co 2 dong header nhung EasyExcel da tieu thu dong
                    // 1 lam header rieng, nen chi can bo dong 2. Dong 3
                    // bi mat tren MOI import + manufacturerNames/
                    // remarksList o Step 2 (doc tu dong 3) lech 1 so voi
                    // dataList nen thuoc dong 4 luu duoi factory +
                    // remarks cua dong 3.
                    .skipRows(1)
                    .read();
        }

        // Step 2: Lạimởcông việclàmsổ, đọcTên nhà sản xuất(Cột6) và Ghi chú(Cột20)
        List<String> manufacturerNames = new ArrayList<>();
        List<String> remarksList = new ArrayList<>();
        try (InputStream is2 = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(is2)) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();
            for (int i = 2; i < rows; i++) {
                XSSFRow row = sheet.getRow(i);
                String mfgName = null;
                if (row != null && row.getCell(6) != null) {
                    mfgName = row.getCell(6).getStringCellValue();
                }
                manufacturerNames.add(mfgName);

                String remark = null;
                if (row != null && row.getCell(20) != null) {
                    remark = row.getCell(20).getStringCellValue();
                }
                remarksList.add(remark);
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
            rowMsg.append("Dữ liệu Kiểm tra Thất bại:");
            for (ImportError err : entry.getValue()) {
                rowMsg.append(err.getMessage()).append("; ");
            }
            rowMsg.append("Vui lòng đối chiếu rồi nhập lại\n");
            mistake.append(rowMsg);
        }

        // Step 4: Xử lýKiểm traQua của  dòng, từng dòngLưu
        List<Drug> dataList = result.getDataList();
        for (int i = 0; i < dataList.size(); i++) {
            Drug drug = dataList.get(i);
            int rowNum = i + 3; // Excel Số dòng(Bỏ qua2 dòngbảngtiêu đề, Số dòngTừ 1Bắt đầu)
            String drugName = drug.getGoodsName();
            try {
                // ĐặtPhòng khám
                drug.setCompany(company);

                // Xử lýNhà sản xuất(Cột6): theoTêntìm kiếm, Không tồn tạithìtạo
                if (i < manufacturerNames.size() && StringUtils.isNotBlank(manufacturerNames.get(i))) {
                    String mfgName = manufacturerNames.get(i);
                    ManufactureFactory manufactureFactory = new ManufactureFactory();
                    String existingId = manufactureFactoryDao.findBy(mfgName, companyId);
                    if (existingId != null) {
                        manufactureFactory.setId(existingId);
                    } else {
                        manufactureFactory.setName(mfgName);
                        manufactureFactory.setType("1");
                        manufactureFactory.setCompany(company);
                        ManufactureFactory savedFactory = manufactureFactoryService.save(manufactureFactory);
                        manufactureFactory.setId(savedFactory.getId());
                    }
                    drug.setFactory(manufactureFactory);
                }

                // Xử lýGhi chú(Cột20)
                if (i < remarksList.size() && remarksList.get(i) != null) {
                    drug.setRemarks(remarksList.get(i));
                }

                // Xử lý có bán lẻ không: giá trị khẳng định trong file Excel -> "1", khác -> "0"
                if ("是".equals(drug.getIsUnpackSell())) {
                    drug.setIsUnpackSell("1");
                } else {
                    drug.setIsUnpackSell("0");
                }

                // Xử lý trạng thái: giá trị khẳng định trong file Excel -> "1", khác -> "0"
                if ("是".equals(drug.getStatus())) {
                    drug.setStatus("1");
                } else {
                    drug.setStatus("0");
                }

                this.save(drug).getId();
                chengGong++;
            } catch (Exception e) {
                logger.warn("Nhập Dòng {} dòng thất bại", rowNum, e);
                String a = "";
                if (e.getMessage() != null) {
                    a = e.getMessage().equals("Thông tin thuốc trùng lặp") ? "Thông tin thuốc trùng lặp," : "";
                }
                siBai++;
                mistake.append("Bảng dòng ").append(rowNum).append(" dòng[").append(drugName).append("]Thông tin thuốcngoại lệ, ").append(a).append("Vui lòng đối chiếu rồi nhập lại\n");
            }
        }

        List<String> error = new ArrayList<>();
        error.add(0, String.valueOf(chengGong));
        error.add(1, String.valueOf(siBai));
        error.add(2, mistake.toString());
        return error;
    }

    // ThuốcThêm mớitratrọng
    public int repeat(Drug drug) {

        String companyId = com.geeke.sys.utils.SessionUtils.getUserJson().getString("companyId");
        Company company = companyService.get(companyId);
        drug.setCompany(company);
        List<Drug> drugList = this.dao.repeat(drug);

        //NếuThêm mớithì là 0, Sửasố dòng có 1nhưng có IDNếu khác nhau thì không trùng lặp
        if (drugList.size() == 1 && drugList.get(0).getId().equals(drug.getId())) {
            return 0;
        } else {
            return drugList.size();
        }

    }

    // ThuốcTênLấyDữ liệu
    public String getId(String name,String company) {
        String drugs = this.dao.getId(name,company);
        return drugs;
    }


    public Page<Drug> listByCompany(List<Parameter> params, int offset, int limit, String orderby) {
        PageRequest pageRequest = new PageRequest(offset, limit, params, orderby,SessionUtils.getLoginTenantId());
        return paginate(
            () -> dao.count(pageRequest),
            () -> {
                List<Drug> list = dao.listPages(pageRequest);
                //Sau khi lấy được thuốc hiện tại, lấy giá nhập từ chi tiết tồn kho và tính tổng chi phí
                for (Drug drug : list) {
                    //Lấy tổng chi phí theo tồn kho động
                    BigDecimal totalCost = medicinalStorageControlService.getByDrugOrStuffId(drug.getId());
                    if (!Objects.isNull(drug.getStock()) && !Objects.isNull(drug.getStock().getStorageStock())) {
                        BigDecimal inventory =
                                drug.getStock().getStorageStock().subtract(drug.getStock().getUsedStock().add(drug.getStock().getReimburseStock()));
                        drug.setInventory(inventory.intValue());
                    } else {
                        drug.setInventory(0);
                    }
                    if (!Objects.isNull(totalCost)) {
                        drug.setBid(totalCost);
                    } else {
                        drug.setBid(new BigDecimal("0"));
                    }
                }
                return list;
            }
        );
    }

    public Page<Drug> listByInstitution(List<Parameter> params, int offset, int limit, String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(params, offset, limit, orderby);
        return paginate(
            () -> dao.countByInstitution(pageRequest),
            () -> dao.listByInstitution(pageRequest)
        );
    }


    @Transactional(readOnly = false)
    public List<String> syncToClinic(List<Drug> entitys) {
        SessionUserDto userDto = SessionUtils.getUserDto();
        SessionUserDto.CompanyDTO companyDto = userDto.getCompany();
        Company company = BeanUtil.copyProperties(companyDto, Company.class);

        List<String> list = new ArrayList<>();

        entitys.forEach(drug -> {
            drug.setCompany(company);
            String oldId = drug.getId();
            int repeat = repeatByCode(drug);
            if (repeat == 0) {
                drug.setId(null);
                drug.setSyncId(oldId);
                super.save(drug);
                this.medicinalStockControlService.initStock(drug);
                list.add(drug.getId());
            }

        });

        return list;
    }

    private int repeatByCode(Drug drug) {
        String companyId = SessionUtils.getUserDto().getCompanyId();
        String code = drug.getCode();
        return this.dao.checkCompanyAndCode(companyId, code);

    }

    public Drug getByCode(String ypdm, String companyId) {
        return  drugDao.getByCode(ypdm,companyId);
    }

    public Drug getByStandardCode(String standardCode, String companyId) {
        return  drugDao.getByStandardCode(standardCode,companyId);
    }


    /**
     * TheoTên và bán lẻgiáTruy vấnThuốcNhập
     * @param name
     * @param retailPrice
     * @return
     */
    public Drug getByNameAndPrice(String name, BigDecimal retailPrice) {
        return  drugDao.getByNameAndPrice(name,retailPrice, SessionUtils.getUserJson().getString("companyId"));
    }
}