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
        checkIndateValue(drug.getIndate()); // DS601
        // DS223: gia ban / gia ban le am truoc day ghi thang (probe pre
        // 19/19: save/bulkInsert/bulkUpdate/save-update deu 100 va luu
        // am that). Gia am chay vao ke don (MedicalRecordService
        // getDrugStuffEvt lay drug.getPrice() lam don gia -> allFee ->
        // tong phieu thu), lam giam tien phieu (money leak), cung ho
        // DS215 (CostItem) / DS209-DS214. Khong co @Min/@Positive tren
        // entity, cot decimal co dau + nullable nen DB chap nhan. Chan
        // fail-closed 400 truoc moi ghi; null giu nguyen (cot nullable),
        // 0 giu nguyen.
        checkDrugNonNegative(drug);
        checkDrugStatusValues(drug); // DS323
        checkDrugUnpackSellValues(drug); // DS364
        checkDrugChineseMedicineUseValues(drug); // DS440
        checkDrugPackValues(drug); // DS451
        checkDrugPreparationUnitValues(drug); // DS453
        checkDrugDaysValues(drug); // DS455
        checkDrugFrequencyValues(drug); // DS533
        checkDrugWesternMedicineUseValues(drug); // DS458
        checkDrugTypeValues(drug); // DS468
        checkDrugNatureValues(drug); // DS491
        checkDrugInventoryThresholdOrder(drug); // DS302
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

    // DS601: indate la so ngay canh bao han su dung (UI chi cho nhap
    // chu so + dau cham; gia tri live '1'/'90'; cau truy van canh bao
    // DATEDIFF(end_date, ...) <= drug.indate ep chuoi thanh so nen
    // 'abc' -> 0, '-5' -> -5 lam hong canh bao). Truoc day khong co
    // guard so nao cho indate tren bat ky duong ghi nao (chi
    // checkLength 255) nen 'abc'/'-5' luu thang qua save/bulk/
    // syncToClinic/updateAllIndate. null/blank cho qua (cot NULL).
    private static void checkIndateValue(String indate) {
        if (StringUtils.isBlank(indate)) {
            return;
        }
        if (!indate.trim().matches("\\d+(\\.\\d*)?")) {
            throw new ServiceException("Thời hạn hiệu lực phải là số ngày không âm");
        }
    }

    // DS223: xem ghi chu o save(). Chi 2 truong gia chay vao tien ke
    // don: price, retailPrice. Null-safe (null giu nguyen), 0 giu
    // nguyen; am -> ServiceException (400).
    // DS242: mo rong them inventory (int nguyen thuy, khong null duoc)
    // -- ton kho am ve vat ly khong the co nhung mapper insert/update/
    // bulk* ghi thang #{inventory} khong dieu kien (DrugDao.xml
    // :485-525/:563-571/:617-661/:826-838); DS69/DS132 chi chan bang
    // tong ton kho (medicinal_stock_control), khong chan cot catalog
    // nay; DS233/DS234 chi chan so luong dong nhap kho, khong chan
    // inventory catalog. Am -> 400, 0 giu nguyen (quy uoc DS219).
    private static void checkDrugNonNegative(Drug drug) {
        if (drug == null) {
            return;
        }
        if (drug.getPrice() != null
                && drug.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Giá bán không được âm");
        }
        if (drug.getRetailPrice() != null
                && drug.getRetailPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Giá bán lẻ không được âm");
        }
        if (drug.getInventory() < 0) {
            throw new ServiceException("Tồn kho không được âm");
        }
        // DS243: lieu dung moi lan + tong so luong am truoc day ghi thang
        // xuong DB qua save/bulkInsert/bulkUpdate (mapper ghi khong dieu
        // kien); DS209/DS214/DS239 chan cac truong cung ten o bang khac,
        // khong chan 2 truong nay cua Drug. Am -> 400, null giu nguyen
        // (nullable), 0 giu nguyen (quy uoc DS219).
        if (drug.getSingleDosage() != null && drug.getSingleDosage() < 0) {
            throw new ServiceException("Liều dùng mỗi lần không được âm");
        }
        if (drug.getTotal() != null && drug.getTotal() < 0) {
            throw new ServiceException("Tổng số lượng không được âm");
        }
    }

    // DS323: co bat (status) chi co 2 gia tri co nghia trong toan bo
    // codebase: '0' = Khong, '1' = Co (drug/index.vue search options,
    // drugForm.vue switch mac dinh '1'). Guard hien co chi chan do dai
    // <= 1 (@Length + checkDrugLengths) va trong (@NotNull /
    // checkDrugRequired) nen '9'/'2'/'x' deu lot qua ca 3 duong ghi va
    // mapper ghi thang #{status} (DrugDao.xml insert/update/bulkInsert/
    // bulkUpdate). Thuoc status '9' khong kich hoat ma cung khong tat:
    // truy van ke don loc status='1' (DrugDao.xml :299/:406) khong thay
    // no, bo loc tat cung khong nhan ra no — trang thai ma. '0'/'1'
    // giu 200 (tao + chuyen doi 2 chieu); null/blank cung 400 o day
    // cho duong bulk (khong qua @Valid), cung hanh vi @Valid o save.
    private static void checkDrugStatusValues(Drug drug) {
        if (drug == null) {
            return;
        }
        if (!"0".equals(drug.getStatus()) && !"1".equals(drug.getStatus())) {
            throw new ServiceException(
                    "Trạng thái thuốc chỉ nhận giá trị 0 (Không) hoặc 1 (Có)");
        }
    }

    // DS364: cho phep ban le (isUnpackSell) chi co 2 gia tri co nghia
    // trong toan bo codebase: '0' = Khong, '1' = Co (switch
    // drugForm.vue / drugNew/drugForm.vue active-value='1' /
    // inactive-value='0'; Excel import normalize "是"->"1" else "0").
    // Guard hien co chi chan do dai <= 45 (@Length + checkDrugLengths)
    // nen '9'/'2'/'x' deu lot qua ca 3 duong ghi va mapper ghi thang
    // #{isUnpackSell} (DrugDao.xml insert/update/bulkInsert/bulkUpdate).
    // Thuoc isUnpackSell '9' hien switch OFF nhu '0', list hien don vi
    // dong goi thay vi don vi bao che (DrugController:53/77/100 chi
    // "1".equals moi lay preparationUnit), tong ton kho
    // sum(case is_unpack_sell when 1 ...) (DrugDao.xml :130/216/343/375)
    // tinh sai nhanh, gate gia ban le (drugForm.vue === '1') tat ngam --
    // trang thai ma khong UI nao tao duoc. '0'/'1' giu 200; null/'' van
    // qua nhu cu (cot nullable, khong @NotNull; mau DS359).
    // Khong guard addDurg (caller noi bo MedicalRecordService:1494
    // copy gia tri tu xa) hay syncToClinic (super.save) -- cung pham
    // vi nhu DS323.
    private static void checkDrugUnpackSellValues(Drug drug) {
        if (drug == null) {
            return;
        }
        String isUnpackSell = drug.getIsUnpackSell();
        if (StringUtils.isBlank(isUnpackSell)) {
            return;
        }
        if (!"0".equals(isUnpackSell) && !"1".equals(isUnpackSell)) {
            throw new ServiceException(
                    "Cho phép bán lẻ chỉ nhận giá trị 0 (Không) hoặc 1 (Có)");
        }
    }

    // DS440: cach dung thuoc Dong y (chineseMedicineUse) chi co 2 gia tri
    // co nghia trong dict 'chineseMedicineUse' (sys_dict_type.code,
    // id 1014474470772900058): '_0' = Sac truoc nua gio, '_1' = Sac truoc
    // mot gio. Frontend chi set tu dict kin nay (DICT_CODE.
    // CHINESE_MEDICINE_USE, drugForm.vue:552-577 hien select 'Cach sac
    // thuoc' khi type = medicalType_1). Guard hien co chi chan do dai
    // <= 255 (@Length + checkDrugLengths :218) nen '_9'/'_2'/'x' deu lot
    // qua ca 3 duong ghi va mapper ghi thang #{chineseMedicineUse.value}
    // (DrugDao.xml insert :527 / update :565 / bulkInsert :663 /
    // bulkUpdate CASE :796-798). Thuoc mang gia tri gia mo man hinh
    // kham copy sang chi tiet don khi them thuoc (medicalOutpatientRecord
    // /index.vue:7521-7525) -> MdMedicationOrderImpl:242 medcWayCodg
    // (ma duong dung thuoc BAT BUOC cua don thuoc BHYT) thanh ma khong
    // ton tai; list LEFT JOIN hien ten trong; mo lai form thuoc Dong y
    // thay select 'Cach sac thuoc' trong. '_0'/'_1' giu 200; null/''
    // van qua nhu cu (cot varchar(255) NULL-able, khong @NotNull; mau
    // DS364/DS419/DS422/DS425/DS433). Khong guard addDurg (caller noi
    // bo MedicalRecordService copy gia tri tu xa) hay syncToClinic
    // (super.save) -- cung pham vi nhu DS323/DS364.
    private static void checkDrugChineseMedicineUseValues(Drug drug) {
        if (drug == null) {
            return;
        }
        DictItem chineseMedicineUse = drug.getChineseMedicineUse();
        if (chineseMedicineUse == null
                || StringUtils.isBlank(chineseMedicineUse.getValue())) {
            return;
        }
        String value = chineseMedicineUse.getValue();
        if (!"chineseMedicineUse_0".equals(value)
                && !"chineseMedicineUse_1".equals(value)) {
            throw new ServiceException(
                    "Cách dùng thuốc Đông y chỉ nhận giá trị chineseMedicineUse_0 (Sắc trước nửa giờ) hoặc chineseMedicineUse_1 (Sắc trước một giờ)");
        }
    }

    // DS451: pack gia mao ngoai mien 'medicalPackUnit' (10 item
    // del_flag='0': _0 Goi, _1 Tui, _2 Hop, _5 Vi, _6 Lo, _7 Ong,
    // _8 Cai, _9 Chai, _10 g, _11 Cuon; _3 Thung da xoa, _4 khong
    // ton tai) truoc day lot qua ca 3 duong ghi vi chi co guard do
    // dai <= 64 (checkDrugLengths :217) va mapper ghi thang
    // #{pack.value} (DrugDao.xml insert :519 / update :558 /
    // bulkInsert :655 / bulkUpdate CASE :767). Thuoc gia hien ten
    // dong goi rong o list (LEFT JOIN khong loc dict_type) va chay
    // vao chuoi so luong ton kho (CurrentInventoryService:400/405/
    // 439/473/478 noi getPack().getName()), xuat kiem ke
    // (InventoryVerificationDetailService:375/456-457) va su kien
    // thuoc khi ke don (MedicalRecordService:1479/:1677 copy pack).
    // '_0'..'_11' hop le giu 200; null/blank van qua nhu cu o day
    // (guard bat buoc san co xu ly: @NotNull o save qua @Valid,
    // checkDrugRequired o bulk*). Khong guard addDurg hay
    // syncToClinic (super.save) -- cung pham vi nhu DS323/DS364/
    // DS440.
    private static void checkDrugPackValues(Drug drug) {
        if (drug == null) {
            return;
        }
        DictItem pack = drug.getPack();
        if (pack == null || StringUtils.isBlank(pack.getValue())) {
            return;
        }
        String value = pack.getValue();
        if (!"medicalPackUnit_0".equals(value)
                && !"medicalPackUnit_1".equals(value)
                && !"medicalPackUnit_2".equals(value)
                && !"medicalPackUnit_5".equals(value)
                && !"medicalPackUnit_6".equals(value)
                && !"medicalPackUnit_7".equals(value)
                && !"medicalPackUnit_8".equals(value)
                && !"medicalPackUnit_9".equals(value)
                && !"medicalPackUnit_10".equals(value)
                && !"medicalPackUnit_11".equals(value)) {
            throw new ServiceException(
                    "Đóng gói không hợp lệ: " + value);
        }
    }

    // DS453: preparationUnit gia mao ngoai mien 'medicalPreparationUnit'
    // (13 item del_flag='0': _0 Vien, _1 Vien, _2 Tui, _3 g, _4 Cai,
    // _5 Ong, _6 ml, _7 mm, _8 mg, _9 Chai, _10 Cuon, _11 ug, _12 Vien;
    // khong item da xoa trong dict nay) truoc day lot qua ca 3 duong ghi
    // vi chi co guard do dai <= 64 (checkDrugLengths) va mapper ghi thang
    // #{preparationUnit.value} (DrugDao.xml insert :517 / update :556 /
    // bulkInsert :653 / bulkUpdate CASE :755-757). Thuoc gia hien ten don
    // vi bao che rong o list (LEFT JOIN khong loc dict_type) va chay vao
    // chuoi quy cach / so luong don (DataBean.java:244-245 setNorms,
    // setNum; :322/327 truyen dich), ton kho
    // (CurrentInventoryService.java:401-402/439/474-475) va su kien thuoc
    // khi ke don (MedicalRecordService:1478/1706 copy preparationUnit).
    // '_0'..'_12' hop le giu 200; null/blank van qua nhu cu o day
    // (guard bat buoc san co xu ly: @NotNull o save qua @Valid,
    // checkDrugRequired o bulk*). Khong guard addDurg hay syncToClinic
    // (super.save) -- cung pham vi nhu DS323/DS364/DS440/DS451.
    private static void checkDrugPreparationUnitValues(Drug drug) {
        if (drug == null) {
            return;
        }
        DictItem preparationUnit = drug.getPreparationUnit();
        if (preparationUnit == null || StringUtils.isBlank(preparationUnit.getValue())) {
            return;
        }
        String value = preparationUnit.getValue();
        if (!"medicalPreparationUnit_0".equals(value)
                && !"medicalPreparationUnit_1".equals(value)
                && !"medicalPreparationUnit_2".equals(value)
                && !"medicalPreparationUnit_3".equals(value)
                && !"medicalPreparationUnit_4".equals(value)
                && !"medicalPreparationUnit_5".equals(value)
                && !"medicalPreparationUnit_6".equals(value)
                && !"medicalPreparationUnit_7".equals(value)
                && !"medicalPreparationUnit_8".equals(value)
                && !"medicalPreparationUnit_9".equals(value)
                && !"medicalPreparationUnit_10".equals(value)
                && !"medicalPreparationUnit_11".equals(value)
                && !"medicalPreparationUnit_12".equals(value)) {
            throw new ServiceException(
                    "Đơn vị bào chế không hợp lệ: " + value);
        }
    }

    // DS455: days gia mao ngoai mien 'recipelDetailDays'
    // (7 item del_flag='0': _0 '1', _1 '2', _2 '3', _3 '4', _4 '5',
    // _5 '6', _6 '7'; khong item da xoa trong dict nay) truoc day lot
    // qua ca 3 duong ghi vi chi co guard do dai <= 255 (checkDrugLengths)
    // va mapper ghi thang #{days.value} (DrugDao.xml insert :531 /
    // update :569 / bulkInsert :667 / bulkUpdate CASE :816-818).
    // Thuoc gia hien ten 'So ngay' rong o list (LEFT JOIN khong loc
    // dict_type), select 'So ngay' trong khi mo lai form thuoc, va
    // phep tinh tong luong theo huong dan o drugForm.vue:919-924/:946
    // (multipliedBy(days.name)) mat tac dung (ten rong -> NaN, bo qua
    // chan 'Gioi han tong luong nho hon tong luong tinh theo huong
    // dan cong dung'). '_0'..'_6' hop le giu 200; null/blank van qua
    // nhu cu o day (days khong @NotNull, khong trong checkDrugRequired).
    // Khong guard addDurg hay syncToClinic (super.save) -- cung pham vi
    // nhu DS323/DS364/DS440/DS451/DS453.
    private static void checkDrugDaysValues(Drug drug) {
        if (drug == null) {
            return;
        }
        DictItem days = drug.getDays();
        if (days == null || StringUtils.isBlank(days.getValue())) {
            return;
        }
        String value = days.getValue();
        if (!"recipelDetailDays_0".equals(value)
                && !"recipelDetailDays_1".equals(value)
                && !"recipelDetailDays_2".equals(value)
                && !"recipelDetailDays_3".equals(value)
                && !"recipelDetailDays_4".equals(value)
                && !"recipelDetailDays_5".equals(value)
                && !"recipelDetailDays_6".equals(value)) {
            throw new ServiceException(
                    "Số ngày không hợp lệ: " + value);
        }
    }

    // DS533: tan suat va lieu dung (frequency) gia mao ngoai mien 5
    // item dict 'chineseMedicineRecipelTakeFrequency' con hoat dong
    // (_0.5 QID, _1 QD, _1.5 Q2D, _2 BID, _3 TID) -- truoc day chi
    // co guard do dai <= 255 (checkDrugLengths) va mapper ghi thang
    // #{frequency.value} (DrugDao.xml insert / update / bulkInsert /
    // bulkUpdate CASE). Thuoc gia hien ten 'Tan suat' rong o list
    // (LEFT JOIN khong loc dict_type) + khi mo lai form; man kham
    // copy drug.frequency vao phep tinh gioi han tong luong
    // (medicalOutpatientRecord/index.vue:6897-6910 split("_")[1])
    // -> gia tri gia cho undefined -> NaN -> chan bi bo qua lang.
    // Gia tri luu la FULL dict value (form bind ca DictItem,
    // drugForm.vue:661-677); hau to rut gon ('_1', '_2', ...) KHONG
    // hop le. '_0.5'..'_3' hop le giu 200; null/blank van qua nhu
    // cu o day (frequency khong @NotNull, khong trong
    // checkDrugRequired, cot varchar(255) NULL-able; tien le
    // DS422/DS455). Khong guard addDurg hay syncToClinic
    // (super.save) -- cung pham vi nhu DS323/DS364/DS440/DS451/
    // DS453/DS455/DS458/DS468/DS491.
    private static void checkDrugFrequencyValues(Drug drug) {
        if (drug == null) {
            return;
        }
        DictItem frequency = drug.getFrequency();
        if (frequency == null || StringUtils.isBlank(frequency.getValue())) {
            return;
        }
        String value = frequency.getValue();
        if (!"chineseMedicineRecipelTakeFrequency_0.5".equals(value)
                && !"chineseMedicineRecipelTakeFrequency_1".equals(value)
                && !"chineseMedicineRecipelTakeFrequency_1.5".equals(value)
                && !"chineseMedicineRecipelTakeFrequency_2".equals(value)
                && !"chineseMedicineRecipelTakeFrequency_3".equals(value)) {
            throw new ServiceException(
                    "Tần suất và liều dùng không hợp lệ: " + value);
        }
    }

    // DS458: cach dung thuoc Tay (westernMedicineUse) gia mao ngoai
    // mien 8 item dict 'westernMedicineUse' con hoat dong (_0 'Uong'
    // .. _7 'Truyen tinh mach) — truoc day chi co guard do dai <= 255
    // (checkDrugLengths) va mapper ghi thang #{westernMedicineUse.value}
    // (DrugDao.xml insert :526 / update :564 / bulkInsert :662 /
    // bulkUpdate CASE :793). Thuoc gia hien ten 'Cach dung thuoc Tay'
    // rong o list (LEFT JOIN khong loc dict_type), select trong khi mo
    // lai form thuoc; man kham copy drug.westernMedicineUse vao chi
    // tiet don (medicalOutpatientRecord/index.vue:7488-7493) ->
    // MdMedicationOrderImpl:272 medcWayCodg — ma duong dung thuoc bat
    // buoc cua don BHYT thanh ma khong ton tai. '_0'..'_7' hop le giu
    // 200; null/blank van qua nhu cu o day (westernMedicineUse khong
    // @NotNull, khong trong checkDrugRequired). Khong guard addDurg
    // hay syncToClinic (super.save) -- cung pham vi nhu DS323/DS364/
    // DS440/DS451/DS453/DS455.
    private static void checkDrugWesternMedicineUseValues(Drug drug) {
        if (drug == null) {
            return;
        }
        DictItem wmu = drug.getWesternMedicineUse();
        if (wmu == null || StringUtils.isBlank(wmu.getValue())) {
            return;
        }
        String value = wmu.getValue();
        if (!"westernMedicineUse_0".equals(value)
                && !"westernMedicineUse_1".equals(value)
                && !"westernMedicineUse_2".equals(value)
                && !"westernMedicineUse_3".equals(value)
                && !"westernMedicineUse_4".equals(value)
                && !"westernMedicineUse_5".equals(value)
                && !"westernMedicineUse_6".equals(value)
                && !"westernMedicineUse_7".equals(value)) {
            throw new ServiceException(
                    "Cách dùng thuốc Tây không hợp lệ: " + value);
        }
    }

    // DS468: loai thuoc (type) gia mao ngoai mien 3 item dict
    // 'medicalType' con hoat dong (_0 Thuoc Tay, _1 Thuoc Dong y,
    // _2 Vat tu y te) — truoc day chi co guard do dai <= 64
    // (checkDrugLengths) va mapper ghi thang #{type.value}
    // (DrugDao.xml insert / update / bulkInsert / bulkUpdate).
    // Thuoc gia hien ten 'Loai thuoc' rong o list (LEFT JOIN khong
    // loc dict_type), select trong khi mo lai form, va chay vao
    // phan biet Tay/Dong y o ke don (drugForm.vue hien select
    // 'Cach sac thuoc' khi type = medicalType_1; MedicalRecordService
    // copy type khi them thuoc vao don). KHAC cac vong nullable
    // (DS364/DS440/DS451/DS453/DS455/DS458): cot drug.type
    // varchar(64) NOT NULL + @NotNull o getType() nen null/blank/''
    // deu 400 o day — bang chung probe: P6 omit -> 400 san qua
    // @Valid o save, P6b value '' -> 200 + luu '' (BUG, fix phai
    // chan ''); duong bulk* khong qua @Valid nen guard nay cung la
    // chan duy nhat cho null/'' o bulk. '_0'..'_2' hop le giu 200.
    // Khong guard addDurg hay syncToClinic (super.save) -- cung pham
    // vi nhu DS323/DS364/DS440/DS451/DS453/DS455/DS458.
    private static void checkDrugTypeValues(Drug drug) {
        if (drug == null) {
            return;
        }
        DictItem type = drug.getType();
        if (type == null || StringUtils.isBlank(type.getValue())) {
            throw new ServiceException("Loại thuốc không được để trống");
        }
        String value = type.getValue();
        if (!"medicalType_0".equals(value)
                && !"medicalType_1".equals(value)
                && !"medicalType_2".equals(value)) {
            throw new ServiceException(
                    "Loại thuốc không hợp lệ: " + value);
        }
    }

    // DS491: tinh chat thuoc (nature) gia mao ngoai mien 2 item dict
    // 'medicalNature' con hoat dong (_0 'Trong nuoc', _1 'Nhap khau')
    // -- truoc day chi co guard do dai <= 64 (checkDrugLengths) va
    // mapper ghi thang #{nature.value} (DrugDao.xml insert :504 /
    // update :546 / bulkInsert :640 / bulkUpdate CASE :705-707).
    // Thuoc gia hien ten 'Tinh chat' rong o list (LEFT JOIN khong
    // loc dict_type) + khi mo lai form. '_0'/'_1' hop le giu 200;
    // null/blank van qua nhu cu o day (nature khong @NotNull, khong
    // trong checkDrugRequired, cot varchar(64) NULL-able, form init
    // null; tien le DS422/DS475). Khong guard addDurg hay
    // syncToClinic (super.save) -- cung pham vi nhu DS323/DS364/
    // DS440/DS451/DS453/DS455/DS458/DS468.
    private static void checkDrugNatureValues(Drug drug) {
        if (drug == null) {
            return;
        }
        DictItem nature = drug.getNature();
        if (nature == null || StringUtils.isBlank(nature.getValue())) {
            return;
        }
        String value = nature.getValue();
        if (!"medicalNature_0".equals(value)
                && !"medicalNature_1".equals(value)) {
            throw new ServiceException(
                    "Tính chất thuốc không hợp lệ: " + value);
        }
    }

    // DS302: nguong ton kho toi thieu (inventoryFloor) vuot nguong ton kho
    // toi da (inventoryLimit) — cau hinh mau thuan (form drugForm.vue:
    // "Nguong ton kho toi thieu" / "Nguong ton kho toi da"). Floor la
    // nguong canh bao ton kho thap that (DrugDao.xml countInventory /
    // getDrugInventoryWarning: ton kho con lai <= floor * preparation).
    // Ca hai cot la varchar(255) nhap tu do nen chi guard khi ca hai
    // parse duoc thanh so (mau parseDecimal DS96 o WarningService);
    // trong / null / khong phai so giu hop le, khong pha luu cu.
    // == giu hop le, < giu hop le; chi floor > limit moi 400.
    private static void checkDrugInventoryThresholdOrder(Drug drug) {
        if (drug == null) {
            return;
        }
        BigDecimal floor = parseDecimal(drug.getInventoryFloor());
        BigDecimal limit = parseDecimal(drug.getInventoryLimit());
        if (floor != null && limit != null && floor.compareTo(limit) > 0) {
            throw new ServiceException("Ngưỡng tồn kho tối thiểu không được vượt quá ngưỡng tồn kho tối đa");
        }
    }

    private static BigDecimal parseDecimal(String value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
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
            checkIndateValue(drug.getIndate()); // DS601
            checkDrugNonNegative(drug);
            checkDrugStatusValues(drug); // DS323
            checkDrugUnpackSellValues(drug); // DS364
            checkDrugChineseMedicineUseValues(drug); // DS440
            checkDrugPackValues(drug); // DS451
            checkDrugPreparationUnitValues(drug); // DS453
            checkDrugDaysValues(drug); // DS455
            checkDrugFrequencyValues(drug); // DS533
            checkDrugWesternMedicineUseValues(drug); // DS458
            checkDrugTypeValues(drug); // DS468
            checkDrugNatureValues(drug); // DS491
            checkDrugInventoryThresholdOrder(drug); // DS302
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
            checkIndateValue(drug.getIndate()); // DS601
            checkDrugNonNegative(drug);
            checkDrugStatusValues(drug); // DS323
            checkDrugUnpackSellValues(drug); // DS364
            checkDrugChineseMedicineUseValues(drug); // DS440
            checkDrugPackValues(drug); // DS451
            checkDrugPreparationUnitValues(drug); // DS453
            checkDrugDaysValues(drug); // DS455
            checkDrugFrequencyValues(drug); // DS533
            checkDrugWesternMedicineUseValues(drug); // DS458
            checkDrugTypeValues(drug); // DS468
            checkDrugNatureValues(drug); // DS491
            checkDrugInventoryThresholdOrder(drug); // DS302
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
        // DS193: duong chen thang qua dao.insert, khong qua save() nen
        // truoc day khong co checkDrugLengths; ten thuoc tu xa > 128 ky
        // tu roi xuong DB moi vo o cot goods_name varchar(128)
        // (MysqlDataTruncation, chi ve 400 chung chung nho handler
        // DS160). Guard tai day nhu save() :107 / syncToClinic :716 /
        // bulk :247/:276; chi caller la MedicalRecordService:1404.
        checkDrugLengths(drug);
        checkIndateValue(drug.getIndate()); // DS601
        // DS594: gia ban / gia ban le am truoc day luu thang qua
        // addRecipel (MedicalRecordService:1494) vi addDurg chi co
        // checkDrugLengths + checkIndateValue, khong co
        // checkDrugNonNegative nhu save() :118. Chan fail-closed 400
        // nhu save(), sau checkDrugLengths.
        checkDrugNonNegative(drug);
        checkDrugPackValues(drug); // DS607: pack gia mao ngoai mien medicalPackUnit qua addRecipel (mau DS451/DS604)
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
        // DS176 A: indate varchar(255) — khong guard thi chuoi >255 qua
        // thang DAO roi 500 MysqlDataTruncation tren UPDATE toan cong ty.
        checkLength(indate, 255, "Thời hạn hiệu lực Độ dài phải nằm trong khoảng 0 đến 255");
        checkIndateValue(indate); // DS601
        int i = this.dao.updateAllIndate(indate, companyId);
        return i;
    }

    @Transactional(readOnly = false)
    public int updateAllInventory(String inventoryFloor, String companyId) {
        // DS176 A: nhu updateAllIndate (inventory_floor varchar(255)).
        checkLength(inventoryFloor, 255, "Giới hạn dưới tồn kho Độ dài phải nằm trong khoảng 0 đến 255");
        // DS599: floor moi vuot limit cua bat ky thuoc nao trong cong ty
        // -> 400 (guard DS302 chi o save/bulkInsert/bulkUpdate/
        // syncToClinic). Floor khong parse duoc/blank/null cho qua
        // nhu guard goc (parseDecimal tra null).
        if (parseDecimal(inventoryFloor) != null
                && this.dao.countLimitBelowFloor(inventoryFloor, companyId) > 0) {
            throw new ServiceException("Ngưỡng tồn kho tối thiểu không được vượt quá ngưỡng tồn kho tối đa");
        }
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
        // DS165 B: truoc day lay institution qua companyService.get(id)
        // .getParent().getId() — session he thong (tenant "0", khong co dong
        // org_company) hoac company khong co parent -> get() null -> NPE 500
        // o POST /stock/drug/inventory. Dung getInstitution(id) null-safe nhu
        // CrudService.buildTenantPageRequest (DAO: parent NULL/0 -> chinh id;
        // id la -> null); cung gia tri cho phong kham thuong (parent cua OWN).
        String institution = companyService.getInstitution(id);
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
        // DS232: danh sach null / phan tu null truoc day NPE 500 o
        // entitys.forEach (lambda :742, log backend) — fail-closed 400
        // truoc moi doc, mau guard bulkInsert DS122/DS107. Danh sach
        // rong van 200 [] (no-op, giu hanh vi cu).
        if (entitys == null) {
            throw new ServiceException("Danh sách thuốc đồng bộ không được để trống");
        }
        for (Drug drug : entitys) {
            if (drug == null) {
                throw new ServiceException("Thuốc đồng bộ không được để trống");
            }
        }
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
                // DS176 A: syncToClinic dung super.save nen vong qua
                // checkDrugLengths cua save() — chuoi qua dai se 500 o
                // mapper; chan 400 truoc khi ghi (khong doi sang this.save
                // vi save() ghi de code + tinh lai ypgg).
                checkDrugLengths(drug);
                checkIndateValue(drug.getIndate()); // DS601
                // DS585: syncToClinic cung vong qua checkDrugRequired
                // cua bulkInsert/bulkUpdate (chi goi o :681/:722, khong
                // o save() -- save dua @Valid -- va khong o day) --
                // goodsName rong, preparationUnit null/rong, pack
                // null/rong luu thang qua mapper (probe pre: sync
                // goodsName '' / omit unit / omit pack deu 200 va luu
                // that; type null / goodsName null da 400 nho DB NOT
                // NULL, status rong da 400 nho checkDrugStatusValues).
                // Chan 400 ngay sau checkDrugLengths, truoc super.save;
                // nhanh skip trung code giu nguyen.
                checkDrugRequired(drug);
                // DS554: syncToClinic cung vong qua checkDrugStatusValues
                // (DS323) cua save() — status '9'/'2'/'x'/'' luu thang
                // qua mapper thanh hang ma (reader loc status='1').
                // Chan 400 ngay sau checkDrugLengths, truoc super.save
                // (mau DS509); nhanh skip trung code giu nguyen.
                checkDrugStatusValues(drug);
                // DS564: syncToClinic cung vong qua checkDrugTypeValues
                // (DS468) cua save() — type 'medicalType_9'/'3'/'x'/''
                // luu thang qua mapper (list LEFT JOIN dict hien ten
                // rong, form roi nham nhanh thuoc Tay). Chan 400 ngay
                // sau checkDrugStatusValues, truoc super.save; thuoc
                // hop le tu cong ty me luon co type (cot NOT NULL).
                checkDrugTypeValues(drug);
                // DS567: syncToClinic cung vong qua
                // checkDrugChineseMedicineUseValues (DS440) cua save() —
                // cmu 'chineseMedicineUse_9'/'_2'/'x' luu thang qua
                // mapper (trang kham copy sang chi tiet don thanh
                // medcWayCodg khong ton tai). Chan 400 ngay sau
                // checkDrugTypeValues, truoc super.save; null/blank
                // qua (cot varchar(255) NULL-able, ngu nghia DS440).
                checkDrugChineseMedicineUseValues(drug);
                // DS569: syncToClinic cung vong qua
                // checkDrugWesternMedicineUseValues (DS458) cua save() —
                // wmu 'westernMedicineUse_9'/'_8'/'x' luu thang qua
                // mapper (trang kham copy sang chi tiet don thanh
                // medcWayCodg khong ton tai). Chan 400 ngay sau
                // checkDrugChineseMedicineUseValues, truoc super.save;
                // null/blank qua (cot varchar(255) NULL-able, ngu nghia
                // DS458).
                checkDrugWesternMedicineUseValues(drug);
                // DS571: syncToClinic cung vong qua
                // checkDrugPackValues (DS451) cua save() — pack
                // 'medicalPackUnit_3'/'_12'/'x' luu thang qua mapper
                // (list hien ten dong goi rong, chay vao chuoi so luong
                // ton kho). Chan 400 ngay sau
                // checkDrugWesternMedicineUseValues, truoc super.save;
                // null/blank qua (cot varchar(255) NULL-able, ngu nghia
                // DS451).
                checkDrugPackValues(drug);
                // DS573: syncToClinic cung vong qua
                // checkDrugPreparationUnitValues (DS453) cua save() —
                // preparationUnit 'medicalPreparationUnit_13'/'_99'/'x'
                // luu thang qua mapper (don thuoc hien don vi che bien
                // khong ton tai). Chan 400 ngay sau checkDrugPackValues,
                // truoc super.save; null/blank qua (cot varchar(64)
                // NULL-able, ngu nghia DS453).
                checkDrugPreparationUnitValues(drug);
                // DS574: syncToClinic cung vong qua
                // checkDrugDaysValues (DS455) cua save() — days
                // 'recipelDetailDays_7'/'_99'/'x' luu thang qua mapper
                // (list hien 'So ngay' rong, drugForm tinh tong luong
                // theo huong dan -> NaN, mat tac dung chan). Chan 400
                // ngay sau checkDrugPreparationUnitValues, truoc
                // super.save; null/blank qua (cot varchar(255)
                // NULL-able, ngu nghia DS455).
                checkDrugDaysValues(drug);
                // DS575: syncToClinic cung vong qua
                // checkDrugFrequencyValues (DS533) cua save() —
                // frequency 'chineseMedicineRecipelTakeFrequency_4'/
                // '_99'/'x' luu thang qua mapper (list hien ten
                // 'Tan suat' rong, ke don copy ma tan suat khong ton
                // tai sang chi tiet don). Chan 400 ngay sau
                // checkDrugDaysValues, truoc super.save; null/blank
                // qua (cot varchar(255) NULL-able, ngu nghia DS533).
                checkDrugFrequencyValues(drug);
                // DS576: syncToClinic cung vong qua
                // checkDrugNatureValues (DS491) cua save() —
                // nature 'medicalNature_2'/'_99'/'x' luu thang qua
                // mapper (list LEFT JOIN dict hien ten 'Tinh chat'
                // rong, ke don copy ma tinh chat khong ton tai).
                // Chan 400 ngay sau checkDrugFrequencyValues,
                // truoc super.save; null/blank qua (cot varchar(255)
                // NULL-able, ngu nghia DS491).
                checkDrugNatureValues(drug);
                // DS577: syncToClinic cung vong qua
                // checkDrugNonNegative (DS209/DS243) cua save() —
                // price/retailPrice/inventory/singleDosage/total am
                // luu thang qua mapper (gia am lam sai tinh tien,
                // ton kho am lam sai kiem ke, lieu dung am lam sai
                // ke don). Chan 400 ngay sau checkDrugNatureValues,
                // truoc super.save; null qua (nullable), 0 qua
                // (quy uoc DS219).
                checkDrugNonNegative(drug);
                // DS578: syncToClinic cung vong qua
                // checkDrugInventoryThresholdOrder (DS302) cua save() —
                // inventoryFloor > inventoryLimit luu thang qua mapper
                // (nguong canh bao ton kho dao nguoc, logic canh bao
                // o DrugDao.xml :420/:439 dung inventory_floor sai).
                // Chan 400 ngay sau checkDrugNonNegative, truoc
                // super.save; null/khong parse duoc qua (ngu nghia
                // DS302), bang nhau qua.
                checkDrugInventoryThresholdOrder(drug);
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