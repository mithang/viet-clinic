package com.geeke.gen.service;

import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.gen.dao.GenTableColumnDao;
import com.geeke.gen.entity.GenTableColumn;
import com.geeke.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * nghiệp vụTrườngbảngService
 * @author lys
 * @version 2019-08-31
 */
 
@Service("genTableColumnService")
@Transactional(readOnly = true)
public class GenTableColumnService extends CrudService<GenTableColumnDao, GenTableColumn>{

    @Override
    @Transactional(readOnly = false)
    public GenTableColumn save(GenTableColumn genTableColumn) {
        // DS118 C: fail-closed them cho cac lo DS80/DS99 chua bit:
        // - null entity -> truoc day NPE 500 o getName() (controller
        //   khong co @Valid, JSON-null toi thang service).
        // - javaType rong -> truoc day 500 NOT NULL (java_type NOT NULL;
        //   @NotNull tren entity chet vi khong co @Valid).
        // - width rong -> truoc day 500 NOT NULL (width int NOT NULL).
        // - genTable id ma -> truoc day 200 + dong mo coi (khong co FK
        //   tren gen_table_id; chi kiem tra genTable non-null).
        if (genTableColumn == null) {
            throw new ServiceException("Trường bảng không được để trống");
        }
        if (StringUtils.isBlank(genTableColumn.getName())) {
            throw new ServiceException("Tên cột không được để trống");
        }
        if (StringUtils.isBlank(genTableColumn.getJavaField())) {
            throw new ServiceException("Tên thuộc tính không được để trống");
        }
        if (StringUtils.isBlank(genTableColumn.getJdbcType())) {
            throw new ServiceException("Loại vật lý không được để trống");
        }
        if (StringUtils.isBlank(genTableColumn.getComments())) {
            throw new ServiceException("Giải thích không được để trống");
        }
        if (genTableColumn.getComments().length() > 64) {
            throw new ServiceException("Giải thích độ dài phải nằm trong khoảng 0 đến 64 ");
        }
        if (StringUtils.isBlank(genTableColumn.getIsEdit())) {
            throw new ServiceException("Có thể biên không được để trống");
        }
        if (genTableColumn.getGenTable() == null
                || StringUtils.isBlank(genTableColumn.getGenTable().getId())) {
            throw new ServiceException("Bảng mã số không thể là trống");
        }
        if (dao.countGenTableById(genTableColumn.getGenTable().getId()) == 0) {
            throw new ServiceException("Bảng mã số không tồn tại");
        }
        if (genTableColumn.getJavaType() == null
                || StringUtils.isBlank(genTableColumn.getJavaType().getValue())) {
            throw new ServiceException("JAVA Loại Không thể là trống");
        }
        if (genTableColumn.getWidth() == null) {
            throw new ServiceException("Cột rộng Không thể là trống");
        }
        // DS141 A: controller khong co @Valid nen moi @Length tren entity
        // deu chet -- gia tri qua dai 500 DataIntegrityViolationException
        // o DB (xem checkLengths: gioi han lay min(entity max, DB max);
        // jdbcType entity 100 nhung DB varchar(64); javaType/queryType/
        // showType/customType khong co @Length, DB varchar(64)).
        checkLengths(genTableColumn);
        // DS99: nhu GenTableService.save -- CrudService.save re nhanh update
        // khi id khac rong, doUpdate khong dong den dong nao neu id khong
        // ton tai ma van tra 200 (probe G5c: column save id la -> 200 im
        // lang, khong co dong nao duoc tao). Chi chap nhan update khi dong
        // da ton tai; tao moi phai gui id rong.
        if (StringUtils.isNotBlank(genTableColumn.getId())
                && super.get(genTableColumn.getId()) == null) {
            throw new ServiceException("Trường bảng không tồn tại: " + genTableColumn.getId());
        }
        return super.save(genTableColumn);
    }

    private static boolean tooLong(String value, int max) {
        return value != null && value.length() > max;
    }

    /**
     * DS141 A: kiem tra do dai moi truong chuoi duoc persist, theo dung
     * gioi han ma DB chap nhan (min cua @Length tren entity va do dai
     * cot DB; noi khong co @Length thi lay do dai cot DB). Chi fail khi
     * gia tri qua dai -- null duoc bo qua o day (cac guard null/blank
     * rieng o save() lo phan do; GenTableService chi dung ham nay de
     * kiem tra cot con truoc bulkInsert). Public static de
     * GenTableService.save tai dung cho cot con gui kem (luu con qua
     * DAO bulkInsert truc tiep, bo qua moi guard cua save() nay).
     */
    public static void checkLengths(GenTableColumn genTableColumn) {
        if (tooLong(genTableColumn.getName(), 128)) {
            throw new ServiceException("Tên Độ dài phải nằm trong khoảng 1 đến 128");
        }
        if (tooLong(genTableColumn.getJdbcType(), 64)) {
            throw new ServiceException("vật lý Loại Độ dài phải nằm trong khoảng 1 đến 64");
        }
        if (tooLong(genTableColumn.getJavaField(), 200)) {
            throw new ServiceException("thuộc tính tên Độ dài phải nằm trong khoảng 1 đến 200");
        }
        if (tooLong(genTableColumn.getDefVal(), 2000)) {
            throw new ServiceException("Mặc định Giá trị Độ dài phải nằm trong khoảng 0 đến 2000");
        }
        if (tooLong(genTableColumn.getQueryVal(), 2000)) {
            throw new ServiceException("Điều kiện truy vấn Độ dài phải nằm trong khoảng 0 đến 2000");
        }
        if (tooLong(genTableColumn.getInteraction(), 255)) {
            throw new ServiceException("liên động thay đổi lượng Độ dài phải nằm trong khoảng 0 đến 255");
        }
        if (tooLong(genTableColumn.getInteractionVal(), 2048)) {
            throw new ServiceException("liên động Giá trị Độ dài phải nằm trong khoảng 0 đến 2048");
        }
        if (tooLong(genTableColumn.getParameters(), 2000)) {
            throw new ServiceException("liên động Tham số Độ dài phải nằm trong khoảng 0 đến 2000");
        }
        if (tooLong(genTableColumn.getSettings(), 2000)) {
            throw new ServiceException("của nó nó Đặt (mở rộng Trường JSON) Độ dài phải từ 0 và 2000 Giữa");
        }
        if (tooLong(genTableColumn.getRemarks(), 255)) {
            throw new ServiceException("Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
        }
        if (genTableColumn.getJavaType() != null
                && tooLong(genTableColumn.getJavaType().getValue(), 64)) {
            throw new ServiceException("JAVA Loại Độ dài phải nằm trong khoảng 1 đến 64");
        }
        if (genTableColumn.getQueryType() != null
                && tooLong(genTableColumn.getQueryType().getValue(), 64)) {
            throw new ServiceException("Truy vấn cách Độ dài phải nằm trong khoảng 0 đến 64");
        }
        if (genTableColumn.getShowType() != null
                && tooLong(genTableColumn.getShowType().getValue(), 64)) {
            throw new ServiceException("điều khiển Độ dài phải nằm trong khoảng 0 đến 64");
        }
        if (genTableColumn.getCustomType() != null
                && tooLong(genTableColumn.getCustomType().getId(), 64)) {
            throw new ServiceException("tùy chỉnh Loại Độ dài phải nằm trong khoảng 0 đến 64");
        }
        // Co 1 ky tu (DB varchar(1), isEdit varchar(2) nhung entity max 1)
        // va NOT NULL: null 500 vi insert liet ke cot tuong minh.
        if (genTableColumn.getIsPk() == null) {
            throw new ServiceException("chính khóa không được để trống");
        }
        if (genTableColumn.getIsPk().length() > 1) {
            throw new ServiceException("chính khóa Độ dài phải nằm trong khoảng 0 đến 1");
        }
        if (genTableColumn.getIsNull() == null) {
            throw new ServiceException("có thể trống không được để trống");
        }
        if (genTableColumn.getIsNull().length() > 1) {
            throw new ServiceException("có thể trống Độ dài phải nằm trong khoảng 0 đến 1");
        }
        if (genTableColumn.getIsInsert() == null) {
            throw new ServiceException("có thể chèn không được để trống");
        }
        if (genTableColumn.getIsInsert().length() > 1) {
            throw new ServiceException("có thể chèn Độ dài phải nằm trong khoảng 0 đến 1");
        }
        if (genTableColumn.getIsEdit() != null && genTableColumn.getIsEdit().length() > 1) {
            throw new ServiceException("Có thể biên Độ dài phải nằm trong khoảng 0 đến 1");
        }
        if (genTableColumn.getIsList() == null) {
            throw new ServiceException("Cột hiển thị không được để trống");
        }
        if (genTableColumn.getIsList().length() > 1) {
            throw new ServiceException("Cột hiển thị Độ dài phải nằm trong khoảng 0 đến 1");
        }
        if (genTableColumn.getIsQuery() == null) {
            throw new ServiceException("Truy vấn không được để trống");
        }
        if (genTableColumn.getIsQuery().length() > 1) {
            throw new ServiceException("Truy vấn Độ dài phải nằm trong khoảng 0 đến 1");
        }
    }

    /**
     * DS118 C: chan xoa khong id o service.
     * - null entity -> 400 (truoc day NPE 500 o CrudService.delete).
     * - id rong -> 400: mapper <delete> co nhanh <otherwise> xoa theo
     *   gen_table_id khi id rong (lop DS51) -- POST delete
     *   {genTable:{id:T}} xoa mem TOAN BO cot cua bang T (probe live
     *   C8b: 200 rows 2). Khong caller noi bo nao dung duong nay
     *   (GenTableService.delete xoa cot qua bulkDelete theo danh sach
     *   id); chi co controller nay goi delete().
     * DS132 A: dao quyet dinh giu id khong ton tai 200 rows 0 cua DS118
     * C (comment cu: "Id không tồn tại giữ 200 rows 0 idempotent như
     * cũ") -- DS128 da chung minh quyet giu parity 200 cua DS127 la
     * NGUOC: DS123 pha parity (ghost -> 400), DS124/DS125/DS129/
     * DS130/DS131 mo rong cho ~20 service; chi bulkDelete ghost giu
     * 200 rows 0 (DS99 G11 / DS126 B). O day cung khong co endpoint
     * bulkDelete (controller nay extends BaseController, khong phai
     * CrudController) nen khong co ngoai le bulk nao de giu. Ghost
     * va xoa lap (del_flag='1') -> 400 theo mau CodeRule/DS129 A:
     * dao.get khong loc del_flag nen tra ca dong da xoa mem, delFlag
     * phan biet; checkExists (DS103) KHONG tai dung (cho qua blank,
     * khong check delFlag, dung chung voi save/bulkUpdate).
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(GenTableColumn genTableColumn) {
        if (genTableColumn == null || StringUtils.isBlank(genTableColumn.getId())) {
            throw new ServiceException("Không xác định được trường bảng cần xóa.");
        }
        GenTableColumn stored = dao.get(genTableColumn.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Trường bảng không tồn tại: " + genTableColumn.getId());
        }
        return super.delete(genTableColumn);
    }
}