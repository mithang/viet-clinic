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
        // DS80: nhu GenTableService — gen_table_column khong co cot company
        // nen controller save khong co @Valid (them @Valid se bat @NotNull
        // ke thua tren DataEntity.getCompany ma form UI khong gui, lam
        // hong moi luu hop le). Kiem tra fail-closed 400 o day: null/rong
        // qua validation se 500 o cot NOT NULL; comments qua 64 ky tu
        // (DB varchar 64) se 500 kieu Data too long.
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
}