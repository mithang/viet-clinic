package com.geeke.sys.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.sys.dao.QueryConditionDao;
import com.geeke.sys.entity.QueryCondition;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * Điều kiện truy vấnService
 * @author lys
 * @version 2021-07-05
 */
 
@Service("queryConditionService")
public class QueryConditionService extends CrudService<QueryConditionDao, QueryCondition>{

    /**
     * DS112: CrudService.save re nhanh update khi id khac rong, doUpdate
     * khong dong den dong nao neu id khong ton tai ma van tra 200 (probe
     * C1; ho silent no-op DS94/DS99). Chi chap nhan update khi dong da ton
     * tai; tao moi phai gui id rong. Kiem tra bang dao.get: mapper get()
     * la SELECT don bang sys_condition, khong JOIN nen dang tin.
     * validateRequired o day de bulkInsert/bulkUpdate fail-closed 400
     * (bulk khong qua @Valid nen conditions/routerId/name thieu se 500 o
     * cot NOT NULL, probe C10/C11/C12); single save da 400 nho @NotNull
     * (DS79) nhung giu validate o service cho dong nhat voi caller noi
     * bo. Chi kiem tra TON TAI, khong kiem tra so huu: san 403 theo user
     * nam o QueryConditionController (iter68/DS47/DS48) chay truoc service
     * va giu nguyen (probe C16). bulkDelete id khong ton tai giu 200 rows
     * 0 IDEMPOTENT (DS99 G11).
     */
    private void validateRequired(QueryCondition entity) {
        if (entity == null) {
            throw new ServiceException("Điều kiện truy vấn không được để trống");
        }
        if (entity.getUserId() == null) {
            throw new ServiceException("Người dùng của điều kiện truy vấn không được để trống");
        }
        if (entity.getRouterId() == null) {
            throw new ServiceException("Định tuyến của điều kiện truy vấn không được để trống");
        }
        if (StringUtils.isBlank(entity.getName())) {
            throw new ServiceException("Tên điều kiện truy vấn không được để trống");
        }
        if (StringUtils.isBlank(entity.getConditions())) {
            throw new ServiceException("Điều kiện truy vấn không được để trống");
        }
    }

    @Override
    public QueryCondition save(QueryCondition entity) {
        validateRequired(entity);
        if (StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Điều kiện truy vấn không tồn tại: "
                    + entity.getId());
        }
        return super.save(entity);
    }

    @Override
    public List<String> bulkInsert(List<QueryCondition> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách điều kiện truy vấn thêm mới không được để trống");
        }
        for (QueryCondition entity : entitys) {
            validateRequired(entity);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    public List<String> bulkUpdate(List<QueryCondition> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách điều kiện truy vấn cập nhật không được để trống");
        }
        for (QueryCondition entity : entitys) {
            validateRequired(entity);
            if (StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của điều kiện truy vấn đã tồn tại");
            }
            if (dao.get(entity.getId()) == null) {
                throw new ServiceException("Điều kiện truy vấn không tồn tại: "
                        + entity.getId());
            }
        }
        return super.bulkUpdate(entitys);
    }

    /**
     * DS124 A: single delete fail-closed 400. CrudService.delete chi chan
     * qua stored-row tenant check ma check do bo qua stored==null, nen
     * delete id ma tra 200 data=0 im lang; mapper delete la UPDATE
     * del_flag khong loc del_flag nen delete dong da xoa mem tra 200
     * rows=1 + audit DELETED trung. Kiem tra ton tai len dau qua dao.get
     * (mapper get khong loc del_flag nen phai kiem tra del_flag='0').
     * San 403 theo user giu o controller.
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(QueryCondition entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được điều kiện truy vấn cần xóa.");
        }
        QueryCondition stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Điều kiện truy vấn không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
    }

    @Override
    public int bulkDelete(List<QueryCondition> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách điều kiện truy vấn xóa không được để trống");
        }
        return super.bulkDelete(entitys);
    }

}
