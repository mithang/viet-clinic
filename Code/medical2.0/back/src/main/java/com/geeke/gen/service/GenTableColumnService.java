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