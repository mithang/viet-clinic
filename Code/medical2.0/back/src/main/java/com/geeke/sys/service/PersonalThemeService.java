package com.geeke.sys.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.sys.dao.PersonalThemeDao;
import com.geeke.sys.entity.PersonalTheme;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * Chủ đề hệ thốngService
 * @author lys
 * @version 2021-07-16
 */
 
@Service("personalThemeService")
public class PersonalThemeService extends CrudService<PersonalThemeDao, PersonalTheme>{

    /**
     * DS112: CrudService.save re nhanh update khi id khac rong, doUpdate
     * khong dong den dong nao neu id khong ton tai ma van tra 200 (probe
     * B1; ho silent no-op DS94/DS99). Chi chap nhan update khi dong da ton
     * tai; tao moi phai gui id rong. Kiem tra bang dao.get: mapper get()
     * la SELECT don bang sys_personal_theme, khong JOIN nen dang tin.
     * validateRequired o day de bulkInsert/bulkUpdate fail-closed 400
     * (bulk khong qua @Valid nen theme/userId thieu se 500 o cot NOT NULL,
     * probe B10/B11/B12); single save da 400 nho @NotNull (DS79) nhung
     * giu validate o service cho dong nhat voi caller noi bo. Chi kiem tra
     * TON TAI, khong kiem tra so huu: san 403 theo user nam o
     * PersonalThemeController (iter68/DS47/DS48) chay truoc service va
     * giu nguyen (probe B16/B17). Delete la XOA CUNG (mapper DELETE FROM)
     * nen bulkDelete id khong ton tai giu 200 rows 0 IDEMPOTENT (DS99 G11).
     * user_id UNIQUE: moi user toi da 1 dong.
     */
    private void validateRequired(PersonalTheme entity) {
        if (entity == null) {
            throw new ServiceException("Chủ đề cá nhân không được để trống");
        }
        if (entity.getUserId() == null) {
            throw new ServiceException("Người dùng của chủ đề không được để trống");
        }
        if (StringUtils.isBlank(entity.getTheme())) {
            throw new ServiceException("Chủ đề không được để trống");
        }
    }

    @Override
    public PersonalTheme save(PersonalTheme entity) {
        validateRequired(entity);
        if (StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Chủ đề cá nhân không tồn tại: "
                    + entity.getId());
        }
        return super.save(entity);
    }

    @Override
    public List<String> bulkInsert(List<PersonalTheme> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chủ đề cá nhân thêm mới không được để trống");
        }
        for (PersonalTheme entity : entitys) {
            validateRequired(entity);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    public List<String> bulkUpdate(List<PersonalTheme> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chủ đề cá nhân cập nhật không được để trống");
        }
        for (PersonalTheme entity : entitys) {
            validateRequired(entity);
            if (StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của chủ đề cá nhân đã tồn tại");
            }
            if (dao.get(entity.getId()) == null) {
                throw new ServiceException("Chủ đề cá nhân không tồn tại: "
                        + entity.getId());
            }
        }
        return super.bulkUpdate(entitys);
    }

    /**
     * DS124 A: single delete fail-closed 400. CrudService.delete chi chan
     * qua stored-row tenant check ma check do bo qua stored==null, nen
     * delete id ma tra 200 data=0 im lang. Delete la XOA CUNG (mapper
     * DELETE FROM) nen chi can kiem tra ton tai qua dao.get; khong co
     * del_flag de kiem tra them. San 403 theo user giu o controller.
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(PersonalTheme entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được chủ đề cá nhân cần xóa.");
        }
        PersonalTheme stored = dao.get(entity.getId());
        if (stored == null) {
            throw new ServiceException("Chủ đề cá nhân không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
    }

    @Override
    public int bulkDelete(List<PersonalTheme> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chủ đề cá nhân xóa không được để trống");
        }
        return super.bulkDelete(entitys);
    }

}
