package com.geeke.sys.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.sys.dao.DictItemDao;
import com.geeke.sys.entity.DictItem;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Lists;

/**
 * Mục từ điểnService
 * @author lys
 * @version 2019-08-23
 */

@Service("dictItemService")
@Transactional(readOnly = true)
public class DictItemService extends CrudService<DictItemDao, DictItem>{

    @Autowired
    private DictItemDao dictItemDao;

    @Override
    @Cacheable(value = "dict:item", key = "#id")
    public DictItem get(String id) {
        return super.get(id);
    }

    /**
     * viết lạiTruy vấn phân trang: Từ điển đã được tối ưu thành dùng chung toàn cục, khôngtheotenantcách ly
     */
    @Override
    public Page<DictItem> listPage(List<Parameter> parameters, int offset, int limit, String orderby) {
        PageRequest pageRequest = new PageRequest(offset, limit, parameters, orderby);
        int total = dao.count(pageRequest);
        List<DictItem> list = total > 0 ? dao.listPage(pageRequest) : java.util.Collections.emptyList();
        return new Page<>(total, list);
    }

    /**
     * viết lạiDanh sáchTruy vấn: Từ điển dùng chung toàn cục, Tất cả tenant đều thấy được
     */
    @Override
    public List<DictItem> listAll(List<Parameter> parameters, String orderby) {
        PageRequest pageRequest = new PageRequest(parameters, orderby);
        return dao.listAll(pageRequest);
    }

    @Override
    @Transactional(readOnly = false)
    @Caching(evict = {
        @CacheEvict(value = "dict:item", key = "#entity.id"),
        @CacheEvict(value = "dict:itemsByCode", allEntries = true)
    })
    public DictItem save(DictItem entity) {
        // Từ điển đã được tối ưu thành dùng chung toàn cục, Không còn cách ly theo tenant, do đókhônglạiĐặt company
        // DS100: CrudService.save rẽ nhánh update khi id khác rỗng,
        // doUpdate không động đến dòng nào nếu id không tồn tại mà vẫn
        // trả 200 (probe S3; cùng họ silent no-op DS94/DS99). Chỉ chấp
        // nhận update khi dòng đã tồn tại; tạo mới phải gửi id rỗng.
        // (Payload save vẫn phải có company: DataEntity.getCompany()
        // @NotNull nên @Valid chặn 400 trước khi tới đây — probe S3/S6
        // trước đây false-pass/false-fail vì thiếu company.)
        if (StringUtils.isNotBlank(entity.getId()) && super.get(entity.getId()) == null) {
            throw new com.geeke.common.service.ServiceException("Mục từ điển không tồn tại: " + entity.getId());
        }
        return super.save(entity);
    }

    /**
     * DS100: bulkInsert/bulkUpdate kế thừa từ CrudService đi thẳng qua
     * dao, bỏ qua các guard @NotNull của save() (dict_type_id/name/
     * value đều NOT NULL ở sys_dict_item; mapper bulkInsert ghi trực
     * tiếp, bulkUpdate ghi CASE không điều kiện cho name/value) nên
     * hàng thiếu trường 500 ở DB (probe S8), và bulkUpdate id không
     * tồn tại trả 200 im lặng dù mapper không động đến dòng nào
     * (probe S4; tiền lệ DS99 chỉ sửa GenTable). Ghi đè ở đây để
     * fail-closed 400 trước mọi ghi: danh sách null/rỗng, trường bắt
     * buộc, id rỗng hoặc không tồn tại. sys_dict_item KHÔNG có cột
     * company_id nên bulk không validate company (chỉ save mới yêu
     * cầu company qua @Valid).
     */
    private void validateRequired(DictItem dictItem) {
        if (dictItem == null) {
            throw new com.geeke.common.service.ServiceException("Mục từ điển không được để trống");
        }
        if (dictItem.getDictType() == null || StringUtils.isBlank(dictItem.getDictType().getId())) {
            throw new com.geeke.common.service.ServiceException("Bảng loại từ điển không được để trống");
        }
        if (StringUtils.isBlank(dictItem.getName())) {
            throw new com.geeke.common.service.ServiceException("Tên mục từ điển không được để trống");
        }
        if (StringUtils.isBlank(dictItem.getValue())) {
            throw new com.geeke.common.service.ServiceException("Giá trị từ điển không được để trống");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<DictItem> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new com.geeke.common.service.ServiceException("Danh sách mục từ điển thêm mới không được để trống");
        }
        for (DictItem dictItem : entitys) {
            validateRequired(dictItem);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<DictItem> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new com.geeke.common.service.ServiceException("Danh sách mục từ điển cập nhật không được để trống");
        }
        for (DictItem dictItem : entitys) {
            // bulkUpdate chỉ ghi name/value (mapper), dictType không
            // được ghi nên không yêu cầu ở đây; name/value NOT NULL.
            if (dictItem == null) {
                throw new com.geeke.common.service.ServiceException("Mục từ điển không được để trống");
            }
            if (StringUtils.isBlank(dictItem.getName())) {
                throw new com.geeke.common.service.ServiceException("Tên mục từ điển không được để trống");
            }
            if (StringUtils.isBlank(dictItem.getValue())) {
                throw new com.geeke.common.service.ServiceException("Giá trị từ điển không được để trống");
            }
            if (StringUtils.isBlank(dictItem.getId())) {
                throw new com.geeke.common.service.ServiceException("Cập nhật hàng loạt yêu cầu id của mục từ điển đã tồn tại");
            }
            if (super.get(dictItem.getId()) == null) {
                throw new com.geeke.common.service.ServiceException("Mục từ điển không tồn tại: " + dictItem.getId());
            }
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    @Caching(evict = {
        @CacheEvict(value = "dict:item", key = "#entity.id"),
        @CacheEvict(value = "dict:itemsByCode", allEntries = true)
    })
    public int delete(DictItem entity) {
        return super.delete(entity);
    }

    /** Theo loại từ điểncodeLấy tất cả mục từ điển(kèmBộ nhớ đệm) */
    @Cacheable(value = "dict:itemsByCode", key = "#code")
    public List<DictItem> listByDictTypeCode(String code) {
        return this.dao.listByDictTypeCode(code);
    }
}