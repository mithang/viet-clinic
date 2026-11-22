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
import com.geeke.common.service.ServiceException;
import com.geeke.org.entity.Company;
import org.springframework.context.annotation.Lazy;
import com.geeke.org.service.CompanyService;
import com.geeke.stock.dao.StuffDao;
import com.geeke.stock.entity.MedicalProject;
import com.geeke.stock.entity.Stuff;
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
     * ITER65 item 5-followup: bản không phân trang trước đây lấy id từ company_id
     * trong body (Parameter.extractAndRemoveCompanyId trả về giá trị body), nên
     * StuffService.listAlls rò tenant lạ qua exportStuff / listAll / salesStat.
     * Ép session tenant như bản có phân trang: strip company_id body, lấy
     * SessionUtils.getLoginTenantId(), institution từ stored company đó.
     * Các caller đều là frontend-driven với company = session tenant nên hành
     * vi hợp lệ không đổi; chỉ chặn company lạ trong body.
     */
    private PageRequest buildStuffTenantPageRequest(List<Parameter> params, String orderby) {
        // Loại bỏ ở frontend company_id(Ngăn vượt quyền), Thống nhất lấy tenant đăng nhập trong phiên làm ranh giới cách ly
        Parameter.extractAndRemoveCompanyId(params);
        String id = SessionUtils.getLoginTenantId();
        if ("null".equals(id)) {
            id = null;
        }
        String institution = companyService.getInstitution(id);
        return new PageRequest(params, orderby, id, institution);
    }

    @Override
    @Transactional(readOnly = false)
    public Stuff save(Stuff stuff) {
        // DS105 B1: save voi id khong ton tai truoc day roi qua
        // CrudService.save -> doUpdate 0 dong roi van 200 silent no-op,
        // roi initStock con tao them dong medicinal_stock_control ma cho
        // id ma do (verifyTenantOwnershipAgainstStoredRow bo qua khi
        // stored==null, cung mau DS94/DS99). Kiem tra ton tai truoc moi
        // ghi, truoc ca repeat() va initStock.
        if (StringUtils.isNotBlank(stuff.getId())
                && stuffDao.get(stuff.getId()) == null) {
            throw new ServiceException("Vật tư không tồn tại: "
                    + stuff.getId());
        }
        // Kiểm traVật tưCó hay khôngtrùng lặp
        final int repeat = this.repeat(stuff);
        if (repeat!=0){
            throw new NegativeArraySizeException("Thông tin vật tư trùng lặp");

        }
        // Khi thêm mới, Xử lý trường tự động đánh số
        if (StringUtils.isBlank(stuff.getId())) {
            String sn = sequenceService.generate(SessionUtils.getUser().getCompanyId(), "stuff_code", stuff);
            stuff.setCode(sn);
        }

        // DS149 B: chan do dai truoc moi ghi (mau DS148 A). bulk* bypass
        // @Valid nen truong co @Length van 500 o mapper khi qua bulk
        // (name 64/pinyinCode 64 sau khi sua annotation nhom A,
        // commonName/code 45, barCode/registrationName/registrationCode 64,
        // specifications 45, isOutSell/isUnpackSell 2, status 1); truong
        // KHONG co @Length: type/nature .value -> varchar(64),
        // minUnit/packUnit .value -> varchar(45), locationNumber/
        // inventoryLimit/inventoryFloor/indate -> varchar(255), remarks
        // -> varchar(255). Gioi han theo cot DB; null-safe.
        checkStuffLengths(stuff);
        // DS223: gia ban ra ngoai / gia ban le am truoc day ghi thang
        // (probe pre 19/19: save/bulkInsert/bulkUpdate/save-update deu
        // 100 va luu am that). Gia am chay vao ke don nhu gia thuoc
        // (MedicalRecordService getDrugStuffEvt), lam giam tien phieu,
        // cung ho DS215 (CostItem) / DS209-DS214. Khong co @Min/@Positive
        // tren entity, cot decimal co dau + nullable nen DB chap nhan.
        // Chan fail-closed 400 truoc moi ghi; null giu nguyen, 0 giu nguyen.
        checkStuffNonNegative(stuff);
        checkStuffStatusValues(stuff); // DS324
        checkStuffOutSellValues(stuff); // DS365
        checkStuffUnpackSellValues(stuff); // DS367
        checkStuffPackUnitValues(stuff); // DS461
        checkStuffMinUnitValues(stuff); // DS502
        checkStuffTypeValues(stuff); // DS475
        checkStuffNatureValues(stuff); // DS491
        checkStuffInventoryThresholdOrder(stuff); // DS303
        Stuff stuffTemp = super.save(stuff);

        //LưuđộngTồn kho
        this.medicinalStockControlService.initStock(stuffTemp);
        return stuffTemp;
    }

    // DS125 A: single delete fail-closed 400 (mau DS123/DS124) -- truoc day
    // delete id ma / dong da xoa mem deu 200 silent (CrudService.delete chi
    // chan qua stored-row tenant check ma check do bo qua stored==null;
    // mapper delete khong loc del_flag nen double-delete tra 200 rows=1).
    // Kiem tra ton tai + con live (del_flag='0') truoc moi xoa; khong co
    // caller noi bo nao goi delete() cua service nay.
    @Override
    @Transactional(readOnly = false)
    public int delete(Stuff stuff) {
        if (stuff == null || StringUtils.isBlank(stuff.getId())) {
            throw new ServiceException("Không xác định được vật tư cần xóa.");
        }
        Stuff stored = stuffDao.get(stuff.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Vật tư không tồn tại: " + stuff.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(stuff);
        return super.delete(stuff);
    }

    // DS105 B3-B8/B10-B12: bulkInsert/bulkUpdate/bulkDelete generic truoc
    // day de lo: null/rong roi xuong MyBatis <foreach> rong -> 500 SQL
    // syntax, thieu truong NOT NULL (name/pinyin_code) -> 500
    // DataIntegrityViolation, bulkUpdate id khong ton tai -> 200 silent
    // no-op. Chan het o service de 400 ro nghia, kiem tra truoc moi ghi
    // de batch hon hop khong doi dong hop le (B9). @Transactional
    // (readOnly = false) tuong minh vi class-level la readOnly = true
    // (mau SupplierService DS104).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<Stuff> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách vật tư không được để trống");
        }
        for (Stuff stuff : entitys) {
            if (StringUtils.isBlank(stuff.getName())) {
                throw new ServiceException("Tên vật tư không được để trống");
            }
            if (StringUtils.isBlank(stuff.getPinyinCode())) {
                throw new ServiceException("Mã pinyin không được để trống");
            }
            checkStuffLengths(stuff);
            checkStuffNonNegative(stuff);
            checkStuffStatusValues(stuff); // DS324
            checkStuffOutSellValues(stuff); // DS365
            checkStuffUnpackSellValues(stuff); // DS367
            checkStuffPackUnitValues(stuff); // DS461
        checkStuffMinUnitValues(stuff); // DS502
            checkStuffTypeValues(stuff); // DS475
            checkStuffNatureValues(stuff); // DS491
            checkStuffInventoryThresholdOrder(stuff); // DS303
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<Stuff> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách vật tư không được để trống");
        }
        for (Stuff stuff : entitys) {
            if (StringUtils.isNotBlank(stuff.getId())
                    && stuffDao.get(stuff.getId()) == null) {
                throw new ServiceException("Vật tư không tồn tại: "
                        + stuff.getId());
            }
            if (StringUtils.isBlank(stuff.getName())) {
                throw new ServiceException("Tên vật tư không được để trống");
            }
            if (StringUtils.isBlank(stuff.getPinyinCode())) {
                throw new ServiceException("Mã pinyin không được để trống");
            }
            checkStuffLengths(stuff);
            checkStuffNonNegative(stuff);
            checkStuffStatusValues(stuff); // DS324
            checkStuffOutSellValues(stuff); // DS365
            checkStuffUnpackSellValues(stuff); // DS367
            checkStuffPackUnitValues(stuff); // DS461
        checkStuffMinUnitValues(stuff); // DS502
            checkStuffTypeValues(stuff); // DS475
            checkStuffNatureValues(stuff); // DS491
            checkStuffInventoryThresholdOrder(stuff); // DS303
        }
        return super.bulkUpdate(entitys);
    }

    // DS149 B: xem ghi chu o save().
    private static void checkStuffLengths(Stuff stuff) {
        if (stuff == null) {
            return;
        }
        checkLength(stuff.getName(), 64, "Tên vật tư độ dài phải nằm trong khoảng 1 đến 64");
        checkLength(stuff.getCommonName(), 45, "thường dùng tên Độ dài phải nằm trong khoảng 0 đến 45");
        checkLength(stuff.getPinyinCode(), 64, "Pinyin mã Độ dài phải nằm trong khoảng 0 đến 64");
        checkLength(stuff.getCode(), 45, "Vật tư Độ dài mã phải nằm trong khoảng 0 đến 45");
        checkLength(stuff.getBarCode(), 64, "mã vạch Độ dài phải nằm trong khoảng 0 đến 64");
        checkLength(stuff.getRegistrationName(), 64, "đăng ký chứng Tên Độ dài phải nằm trong khoảng 0 đến 64");
        checkLength(stuff.getRegistrationCode(), 64, "đăng ký chứng số Độ dài phải nằm trong khoảng 0 đến 64");
        checkLength(stuff.getSpecifications(), 45, "Quy cách Độ dài phải nằm trong khoảng 0 đến 45");
        checkLength(stuff.getIsOutSell(), 2, "Cho phép bán ra ngoài Độ dài phải nằm trong khoảng 0 đến 2");
        checkLength(stuff.getIsUnpackSell(), 2, "Độ dài cho phép bán lẻ phải nằm trong khoảng 0 đến 2");
        checkLength(stuff.getStatus(), 1, "Cờ bật Độ dài phải nằm trong khoảng 1 đến 1");
        checkLength(stuff.getLocationNumber(), 255, "Số vị trí hàng Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(stuff.getInventoryLimit(), 255, "Giới hạn trên tồn kho Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(stuff.getInventoryFloor(), 255, "Giới hạn dưới tồn kho Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(stuff.getIndate(), 255, "Thời hạn hiệu lực Độ dài phải nằm trong khoảng 0 đến 255");
        checkLength(stuff.getRemarks(), 255, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
        checkDictLength(stuff.getType(), 64, "Loại vật tư Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(stuff.getNature(), 64, "Tính chất Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(stuff.getMinUnit(), 45, "Đơn vị nhỏ nhất Độ dài phải nằm trong khoảng 0 đến 45");
        checkDictLength(stuff.getPackUnit(), 45, "Đơn vị đóng gói Độ dài phải nằm trong khoảng 0 đến 45");
    }

    // DS223: xem ghi chu o save(). Chi 2 truong gia chay vao tien ke
    // don: priceOutSell, retailPrice. Null-safe (null giu nguyen), 0 giu
    // nguyen; am -> ServiceException (400).
    // DS242: mo rong them inventory (int nguyen thuy) + packNumber
    // (Integer nullable) -- ca hai deu duoc mapper insert/update/bulk*
    // ghi thang khong dieu kien (StuffDao.xml pack_number/inventory);
    // rieng bulkUpdate khong co trim inventory nen chi packNumber toi
    // duoc qua duong do, nhung guard dat o service nen phu ca 3 duong
    // save/bulkInsert/bulkUpdate. Ton kho am / so luong dong goi am ve
    // vat ly khong the co; am -> 400, 0 giu nguyen (quy uoc DS219),
    // packNumber null giu nguyen.
    private static void checkStuffNonNegative(Stuff stuff) {
        if (stuff == null) {
            return;
        }
        if (stuff.getPriceOutSell() != null
                && stuff.getPriceOutSell().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Giá bán ra ngoài không được âm");
        }
        if (stuff.getRetailPrice() != null
                && stuff.getRetailPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("Giá bán lẻ không được âm");
        }
        if (stuff.getInventory() < 0) {
            throw new ServiceException("Tồn kho không được âm");
        }
        if (stuff.getPackNumber() != null && stuff.getPackNumber() < 0) {
            throw new ServiceException("Số lượng đóng gói không được âm");
        }
    }

    // DS324: co bat (status) chi co 2 gia tri co nghia trong toan bo
    // codebase: '0' = Khong, '1' = Co (stuff/index.vue search options,
    // stuffForm.vue switch mac dinh '1'; import Excel anh xa "是" -> '1',
    // khac -> '0', StuffService:605-608). Guard hien co chi chan do dai
    // <= 1 (@Length + checkStuffLengths) va trong (@NotNull o save) nen
    // '9'/'2'/'x' deu lot qua duong tao (save-create, bulkInsert) va
    // mapper ghi thang #{status} (StuffDao.xml insert/bulkInsert).
    // Vat tu status '9' khong kich hoat ma cung khong tat: truy van ke
    // don / y te loc status='1' (StuffDao.xml :202/:224) khong thay no,
    // bo loc tat cung khong nhan ra no — trang thai ma (mau DS323 Drug).
    // '0'/'1' giu 200 (tao + chuyen doi 2 chieu); null/blank cung 400 o
    // day cho duong bulk (khong qua @Valid), cung hanh vi @Valid o save.
    // Dat canh checkStuffNonNegative o save/bulkInsert/bulkUpdate.
    // Luu y: probe pre phat hien mapper update/bulkUpdate KHONG co cot
    // status (chi insert/bulkInsert co) nen doi trang thai qua save-update
    // / bulkUpdate truoc day 200 nhung giu nguyen '1' — switch kich hoat
    // tren form sua khong co tac dung; fix kem cot status vao ca 2
    // mapper update de chuyen doi hop le '0'<->'1' that su luu duoc.
    // An toan: moi duong ghi deu qua guard nay (save co @Valid @NotNull,
    // bulk* co guard) nen status luon '0'/'1' khi xuong mapper; caller
    // noi bo duy nhat goi save() la import Excel (da chuan hoa '0'/'1').
    private static void checkStuffStatusValues(Stuff stuff) {
        if (stuff == null) {
            return;
        }
        if (!"0".equals(stuff.getStatus()) && !"1".equals(stuff.getStatus())) {
            throw new ServiceException(
                    "Trạng thái vật tư chỉ nhận giá trị 0 (Không) hoặc 1 (Có)");
        }
    }

    // DS365: co ban ra ngoai (isOutSell) chi co 2 gia tri co nghia trong
    // toan bo codebase: '0' = Khong, '1' = Co (stuffForm.vue switch
    // active-value="1" / inactive-value="0", mac dinh "0"; index.vue
    // search options chi '0'/'1'; import Excel anh xa "是" -> '1',
    // khac -> '0', StuffService). Guard hien co chi chan do dai <= 2
    // (@Length + checkStuffLengths) nen '9'/'2'/'x' deu lot qua ca 3
    // duong ghi (save-create, save-update, bulkInsert, bulkUpdate) va
    // mapper ghi thang #{isOutSell} (StuffDao.xml insert/update/
    // bulkInsert/bulkUpdate CASE) -- khac status truoc DS324, cot nay
    // CO trong ca update/bulkUpdate nen gia tri gia luu duoc ca khi sua.
    // Vat tu isOutSell '9' khong ban ra ngoai ma cung khong cam: form
    // hien switch OFF nhu '0' nhung priceOutSell van mo (chi disabled
    // khi === '0'), kiem ke ton kho (inventoryVerificationForm.vue:814)
    // bo qua nhanh gia ban ra ngoai, loc tim kiem is_out_sell '=' khong
    // thay no -- trang thai ma (mau DS364 Drug.isUnpackSell).
    // '0'/'1' giu 200 (tao + chuyen doi 2 chieu); null/'' giu hop le
    // (cot nullable, khong @NotNull; mau DS359/DS364).
    // Dat canh checkStuffStatusValues o save/bulkInsert/bulkUpdate.
    // An toan: caller noi bo duy nhat goi save() la import Excel (da
    // chuan hoa '0'/'1'); syncToClinic dung super.save nen khong bi
    // anh huong (mau DS323/DS364).
    private static void checkStuffOutSellValues(Stuff stuff) {
        if (stuff == null) {
            return;
        }
        String value = stuff.getIsOutSell();
        if (StringUtils.isBlank(value)) {
            return;
        }
        if (!"0".equals(value) && !"1".equals(value)) {
            throw new ServiceException(
                    "Cho phép bán ra ngoài chỉ nhận giá trị 0 (Không) "
                            + "hoặc 1 (Có)");
        }
    }

    // DS367: cho phep ban le tach hop (isUnpackSell) chi co 2 gia tri co
    // nghia trong toan bo codebase: '0' = Khong, '1' = Co
    // (stuffForm.vue:323-326 switch active-value="1" /
    // inactive-value="0", mac dinh "0" (:532/:625); retailPrice chi
    // disabled khi isUnpackSell === '0' (:346); import Excel anh xa
    // "是" -> '1', khac -> '0', StuffService:671-676). Guard hien co
    // chi chan do dai <= 2 (@Length(min=0,max=2) tren getter
    // Stuff.java:243-244 + checkStuffLengths) nen '9'/'2'/'x' deu lot
    // qua ca 3 duong ghi (save-create, save-update, bulkInsert,
    // bulkUpdate) va mapper ghi thang #{isUnpackSell} (StuffDao.xml
    // insert :318 / update :351 / bulkInsert :427 / bulkUpdate CASE
    // :525) -- cot nay CO trong ca update/bulkUpdate nen gia tri gia
    // luu duoc ca khi sua (mau DS365 isOutSell).
    // Vat tu isUnpackSell '9' khong ban le ma cung khong cam: form
    // hien switch OFF nhu '0' nhung retailPrice van mo (chi disabled
    // khi === '0'), ke don (DrugStuffEvt.isUnpackSell) va mau phieu
    // (recipetemplateForm gate === '1') khong nhan ra no, loc tim kiem
    // is_unpack_sell '=' khong thay no -- trang thai ma (mau DS364
    // Drug.isUnpackSell, DS365 Stuff.isOutSell).
    // '0'/'1' giu 200 (tao + chuyen doi 2 chieu); null/'' giu hop le
    // (cot nullable, khong @NotNull; mau DS359/DS364/DS365).
    // Dat canh checkStuffOutSellValues o save/bulkInsert/bulkUpdate.
    // An toan: caller noi bo duy nhat goi save() la import Excel (da
    // chuan hoa '0'/'1'); syncToClinic dung super.save nen khong bi
    // anh huong (mau DS323/DS364).
    private static void checkStuffUnpackSellValues(Stuff stuff) {
        if (stuff == null) {
            return;
        }
        String value = stuff.getIsUnpackSell();
        if (StringUtils.isBlank(value)) {
            return;
        }
        if (!"0".equals(value) && !"1".equals(value)) {
            throw new ServiceException(
                    "Cho phép bán lẻ tách hộp chỉ nhận giá trị 0 (Không) "
                            + "hoặc 1 (Có)");
        }
    }

    // DS461: don vi dong goi (packUnit) chi co 10 gia tri co nghia trong
    // dict 'medicalPackUnit' (del_flag='0'): _0 Goi, _1 Tui, _2 Hop,
    // _5 Vi, _6 Lon, _7 Ong, _8 Cai, _9 Chai, _10 g, _11 Cuon
    // (_3 'Thung' da xoa, _4 chua tung ton tai). Guard hien co chi chan
    // do dai <= 45 (checkStuffLengths) nen 'x'/'_99'/'_4' deu lot qua ca
    // 3 duong ghi (save-create, save-update, bulkInsert, bulkUpdate) va
    // mapper ghi thang #{packUnit.value} (StuffDao.xml insert :315 /
    // update :348 / bulkInsert :424 / bulkUpdate CASE :510). Vat tu
    // packUnit gia hien ten rong: stuff/index.vue:148 chuoi ton kho in
    // packUnit.name rong, cac trang ke/duyet don in stuff.packUnit.name
    // rong (recipetemplateForm.vue:1286/1316, reviewForm.vue:160/299,
    // medicalOutpatientRecord/index.vue:948/1949/1980/2412/2606/2758).
    // Cot pack_unit varchar(45) NOT NULL + @NotNull o entity nen null /
    // blank cung 400 o day (khac DS451 Drug.pack: cot NULL-able, null /
    // blank qua); duong save da 400 qua @Valid, duong bulk truoc day
    // 500 DB NOT NULL, gio 400 ro nghia. Live stuff 16 co 3 dong legacy
    // ngoai mien ('Bo' x2 + 'FXS4508' x1, chua tung la dict item) — guard
    // chi chan ghi MOI, khong dung hang legacy. Dat canh
    // checkStuffUnpackSellValues o save/bulkInsert/bulkUpdate. An toan:
    // khong caller noi bo nao setPackUnit ngoai setter; import Excel qua
    // this.save() nen duoc guard phu; syncToClinic dung super.save nen
    // khong bi anh huong (mau DS323/DS364).
    private static void checkStuffPackUnitValues(Stuff stuff) {
        if (stuff == null) {
            return;
        }
        DictItem packUnit = stuff.getPackUnit();
        if (packUnit == null || StringUtils.isBlank(packUnit.getValue())) {
            throw new ServiceException(
                    "Đơn vị đóng gói không được để trống");
        }
        String value = packUnit.getValue();
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
                    "Đơn vị đóng gói không hợp lệ: " + value);
        }
    }

    // DS502: don vi nho nhat vat tu (minUnit) chi co 13 gia tri co nghia
    // trong dict 'medicalPreparationUnit' (del_flag='0'):
    // _0 'Vien', _1 'Vien', _2 'Tui', _3 'g', _4 'Cai', _5 'Ong',
    // _6 'ml', _7 'mm', _8 'mg', _9 'Chai', _10 'Cuon', _11 'ug',
    // _12 'Vien'. UI writer duy nhat stuffForm.vue:662-664 nap
    // minUnit_List tu DICT_CODE.MEDICAL_PREPARATION_UNIT nen chi ghi
    // 13 gia tri do; gia tri gia mao 'x'/'medicalPreparationUnit_99'/
    // 'medicalPreparationUnit_13' truoc day ghi thang qua mapper
    // (#{minUnit.value} verbatim: StuffDao.xml insert :314 / update
    // :347 / bulkInsert :423 / bulkUpdate CASE :503-505) roi hien ten
    // rong o danh sach vat tu (list SELECT LEFT JOIN sys_dict_item
    // minUnit + IFNULL(minUnit.name,''), StuffDao.xml :25-26/:68) va
    // mat don vi trong chuoi ton kho (stuff/index.vue:148). Cot
    // stuff.min_unit varchar(45) NOT NULL + @NotNull (Stuff.java:205)
    // nen null/blank cung 400 (tien le DS461 packUnit, khac DS475
    // type NULL-able). Chi chan ghi MOI, khong dung hang legacy (live
    // 16 dong: 13 trong mien, 3 ngoai mien 'B?' x2 +
    // 'medicalPackUnit_0' x1). Dat canh checkStuffPackUnitValues o
    // save/bulkInsert/bulkUpdate. An toan: khong caller noi bo nao
    // setMinUnit tren Stuff; import Excel qua this.save() nen duoc
    // guard phu; syncToClinic dung super.save nen khong bi anh huong
    // (mau DS323/DS364).
    private static void checkStuffMinUnitValues(Stuff stuff) {
        if (stuff == null) {
            return;
        }
        DictItem minUnit = stuff.getMinUnit();
        if (minUnit == null || StringUtils.isBlank(minUnit.getValue())) {
            throw new ServiceException(
                    "Đơn vị nhỏ nhất không được để trống");
        }
        String value = minUnit.getValue();
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
                    "Đơn vị nhỏ nhất không hợp lệ: " + value);
        }
    }

    // DS475: loai vat tu (type) chi co 2 gia tri co nghia trong dict
    // 'stuffType' (del_flag='0'): stuffType_0 'Vat tu y te',
    // stuffType_1 'Vat tu phi y te'. UI writer duy nhat
    // stuffForm.vue:73-96 nap type_List tu dict nay nen chi ghi 2
    // gia tri do; gia tri gia mao 'x'/'stuffType_99'/'stuffType_2'
    // truoc day ghi thang qua mapper (#{type.value} verbatim:
    // StuffDao.xml insert :306 / update :339 / bulkInsert :415 /
    // bulkUpdate CASE :463-465) roi hien ten rong o danh sach vat tu
    // (list SELECT LEFT JOIN sys_dict_item type + IFNULL(type.name,''),
    // StuffDao.xml :65/:69), phieu kiem ke
    // (InventoryVerificationDetailService.java:382) va danh sach canh
    // bao (WarningService.java:155/202) -- cung ho DS461. Cot
    // stuff.type varchar(64) NULL-able, live 16 dong co 11 NULL nen
    // null/blank giu hop le (khac DS461 packUnit NOT NULL + @NotNull).
    // Chi chan ghi MOI, khong dung hang legacy. Dat canh
    // checkStuffPackUnitValues o save/bulkInsert/bulkUpdate. An toan:
    // khong caller noi bo nao setType tren Stuff (chi Drug.setType o
    // MedicalRecordService, entity khac); import Excel qua this.save()
    // nen duoc guard phu; syncToClinic dung super.save nen khong bi
    // anh huong (mau DS323/DS364).
    private static void checkStuffTypeValues(Stuff stuff) {
        if (stuff == null) {
            return;
        }
        DictItem type = stuff.getType();
        if (type == null || StringUtils.isBlank(type.getValue())) {
            return;
        }
        String value = type.getValue();
        if (!"stuffType_0".equals(value)
                && !"stuffType_1".equals(value)) {
            throw new ServiceException(
                    "Loại vật tư không hợp lệ: " + value);
        }
    }

    // DS491: tinh chat vat tu (nature) gia mao ngoai mien 2 item dict
    // 'medicalNature' con hoat dong (_0 'Trong nuoc', _1 'Nhap khau')
    // -- truoc day chi co guard do dai <= 64 (checkStuffLengths) va
    // mapper ghi thang #{nature.value} (StuffDao.xml insert :308 /
    // update :341 / bulkInsert :417 / bulkUpdate CASE :473-475).
    // Vat tu gia hien ten 'Tinh chat' rong o list (LEFT JOIN khong
    // loc dict_type) + khi mo lai form. '_0'/'_1' hop le giu 200;
    // null/blank van qua nhu cu o day (nature khong @NotNull, cot
    // varchar(64) NULL-able, form init null; tien le DS422/DS475).
    // Khong guard syncToClinic (super.save) -- cung pham vi nhu
    // DS324/DS365/DS367/DS461/DS475.
    private static void checkStuffNatureValues(Stuff stuff) {
        if (stuff == null) {
            return;
        }
        DictItem nature = stuff.getNature();
        if (nature == null || StringUtils.isBlank(nature.getValue())) {
            return;
        }
        String value = nature.getValue();
        if (!"medicalNature_0".equals(value)
                && !"medicalNature_1".equals(value)) {
            throw new ServiceException(
                    "Tính chất vật tư không hợp lệ: " + value);
        }
    }

    // DS303: nguong ton kho toi thieu (inventoryFloor) vuot nguong ton kho
    // toi da (inventoryLimit) — cau hinh mau thuan (form stuffForm.vue:
    // "Nguong ton kho toi thieu" / "Nguong ton kho toi da"). Floor la
    // nguong canh bao ton kho thap that (StuffDao.xml getCountWarning /
    // getStuffInventoryWarning: ton kho con lai <= inventory_floor).
    // Ca hai cot la varchar(255) nhap tu do nen chi guard khi ca hai
    // parse duoc thanh so (mau parseDecimal DS96 o WarningService /
    // DS302 Drug); trong / null / khong phai so giu hop le, khong pha
    // luu cu. == giu hop le, < giu hop le; chi floor > limit moi 400.
    // Dat canh checkStuffNonNegative o save/bulkInsert/bulkUpdate.
    // Luu y: bulkUpdate mapper khong co CASE cho 2 cot nay nen duong
    // do khong doi duoc nguong, guard van dat de phong thu sau.
    private static void checkStuffInventoryThresholdOrder(Stuff stuff) {
        if (stuff == null) {
            return;
        }
        BigDecimal floor = parseDecimal(stuff.getInventoryFloor());
        BigDecimal limit = parseDecimal(stuff.getInventoryLimit());
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

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<Stuff> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách vật tư không được để trống");
        }
        return super.bulkDelete(entitys);
    }

    public List<MedicalProject> listPageForMedical(List<Parameter> parameters, String orderby) {
        // DS5: dung builder tenant-session nhu listAlls (da sua o ffab9c4),
        // thay cho PageRequest tran truoc day. PageRequest tran de id/institution
        // NULL nen predicate tenant cua StuffDao.count/listAll luon NULL -> duong
        // nay tra [] cho moi session (probe ds5: ca company OWN + name ds5 cung
        // []). Chi dung ensureCompanyFilter nhu CostItem (iter65 item 4) thi KHONG
        // du: whereParam company_id=session van bi AND voi predicate NULL -> van [].
        // Builder strip company_id body, lay id = session tenant + institution tu
        // stored company do, nen tra dung dong OWN (va dong institution-shared neu
        // co) ma khong bao gio tra dong tenant la theo body — cung mau da kiem
        // chung o listAlls/exportStuff. Khong doi mapper, khong doi mapping "".
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        PageRequest pageRequest = buildStuffTenantPageRequest(parameters, orderby);
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

    // Đãhủy bỏbỏ — stuffDao.updateInventory Phương thứcKhông tồn tại, vàtất cảgọiĐãchúgiải thích
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

    // DS97: chuỗi tồn kho tính từ BigDecimal có thể thập phân ("10.5");
    // parseInt vỡ cả trang, intValue() cắt phần thập phân, lỗi khác -> 0.
    private static int parseInventory(String inventory) {
        try {
            return new BigDecimal(inventory).intValue();
        } catch (Exception e) {
            return 0;
        }
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
                        // DS97: tồn kho thập phân ("10.5") làm Integer.parseInt NFE
                        // -> 500 cả trang; dùng BigDecimal.intValue() như
                        // DrugService.getDrug (cắt phần thập phân, không 500).
                        stuff.setInventory(parseInventory(inventory));
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
            throw new Exception("Tải lên Tệp định dạng không đúng");
        }

        int chengGong = 0;
        int siBai = 0;
        StringBuilder mistake = new StringBuilder();

        // Step 1: Sử dụng ExcelImportBuilder đọcvàKiểm tra Excel Dữ liệu
        ExcelImportResult<Stuff> result;
        try (InputStream is = file.getInputStream()) {
            result = new ExcelImportBuilder<>(Stuff.class, is)
                    .dictTypeService(dictTypeService)
                    // DS150 C: skipRows(2) nuot dong du lieu dau (dong 3):
                    // template co 2 dong header nhung EasyExcel da tieu
                    // thu dong 1 lam header rieng, nen chi can bo dong 2.
                    // Dong 3 bi mat tren MOI import + dataList lech 1 so
                    // voi nameList o Step 2/4 (doc tu dong 3) nen dong 4
                    // luu duoi ten dong 3.
                    .skipRows(1)
                    .read();
        }

        // Step 2: Lạimởcông việclàmsổ, đọcVật tưTên(Cột0) và Tên nhà sản xuất(Cột4)
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
            rowMsg.append("Dữ liệu Kiểm tra Thất bại:");
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

                // Xử lý có bán ra ngoài không: giá trị khẳng định trong file Excel -> "1", khác -> "0"
                if ("是".equals(stuff.getIsOutSell())) {
                    stuff.setIsOutSell("1");
                } else {
                    stuff.setIsOutSell("0");
                }

                // Xử lý có bán lẻ không: giá trị khẳng định trong file Excel -> "1", khác -> "0"
                if ("是".equals(stuff.getIsUnpackSell())) {
                    stuff.setIsUnpackSell("1");
                } else {
                    stuff.setIsUnpackSell("0");
                }

                // Xử lý trạng thái: giá trị khẳng định trong file Excel -> "1", khác -> "0"
                if ("是".equals(stuff.getStatus())) {
                    stuff.setStatus("1");
                } else {
                    stuff.setStatus("0");
                }

                this.save(stuff).getId();
                chengGong++;
            } catch (Exception e) {
                logger.warn("Nhập Dòng {} dòng thất bại", rowNum, e);
                String a = "";
                if (e.getMessage() != null) {
                    a = e.getMessage().equals("Thông tin vật tư trùng lặp") ? "Thông tin vật tư trùng lặp," : "";
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
        // DS232: nhu DrugService.syncToClinic — danh sach null / phan tu
        // null truoc day NPE 500 o entitys.forEach (lambda :621); fail-closed
        // 400 truoc moi doc, mau guard bulkInsert DS107. Danh sach rong
        // van 200 [] (no-op, giu hanh vi cu).
        if (entitys == null) {
            throw new ServiceException("Danh sách vật tư đồng bộ không được để trống");
        }
        for (Stuff stuff : entitys) {
            if (stuff == null) {
                throw new ServiceException("Vật tư đồng bộ không được để trống");
            }
        }
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
                checkStuffOutSellValues(stuff); // DS560
                // DS579: syncToClinic bo qua guard packUnit DS461
                // cua save() -- packUnit gia mao ngoai mien
                // 10 gia tri dict 'medicalPackUnit' luu thang
                // qua mapper (#{packUnit.value} verbatim).
                // Chan 400 ngay sau checkStuffOutSellValues,
                // truoc super.save; null/blank cung 400
                // (NOT NULL + @NotNull, tien le DS461).
                checkStuffPackUnitValues(stuff); // DS579
                // DS580: syncToClinic bo qua guard minUnit DS502
                // cua save() -- minUnit gia mao ngoai mien
                // 13 gia tri dict 'medicalPreparationUnit' luu thang
                // qua mapper (#{minUnit.value} verbatim).
                // Chan 400 ngay sau checkStuffPackUnitValues,
                // truoc super.save; null/blank cung 400
                // (NOT NULL + @NotNull, tien le DS461/DS579).
                checkStuffMinUnitValues(stuff); // DS580
                // DS581: syncToClinic bo qua guard type DS475
                // cua save() -- type gia mao ngoai mien
                // 2 gia tri dict 'stuffType' luu thang
                // qua mapper (#{type.value} verbatim).
                // Chan 400 ngay sau checkStuffMinUnitValues,
                // truoc super.save; null/blank qua
                // (tien le DS475, type nullable).
                checkStuffTypeValues(stuff); // DS581
                // DS582: syncToClinic bo qua guard nature DS491
                // cua save() -- nature gia mao ngoai mien
                // 2 gia tri dict 'medicalNature' luu thang
                // qua mapper (#{nature.value} verbatim).
                // Chan 400 ngay sau checkStuffTypeValues,
                // truoc super.save; null/blank qua
                // (tien le DS491, nature nullable).
                checkStuffNatureValues(stuff); // DS582
                // DS583: syncToClinic bo qua guard threshold order DS303
                // cua save() -- inventoryFloor > inventoryLimit
                // (nguong toi thieu vuot toi da) luu thang qua
                // mapper (#{inventoryFloor}/#{inventoryLimit}
                // verbatim). Chan 400 ngay sau
                // checkStuffNatureValues, truoc super.save;
                // null/blank/khong phai so qua, bang qua
                // (tien le DS303, DS578 Drug).
                checkStuffInventoryThresholdOrder(stuff); // DS583
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