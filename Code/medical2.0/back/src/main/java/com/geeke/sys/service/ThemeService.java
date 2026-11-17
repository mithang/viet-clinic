package com.geeke.sys.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.sys.dao.ThemeDao;
import com.geeke.sys.entity.Theme;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * Chủ đề hệ thốngService
 * @author lys
 * @version 2021-09-19
 */
 
@Service("themeService")
public class ThemeService extends CrudService<ThemeDao, Theme>{

    // DS107 B1/B10-B12: save/bulkInsert/bulkUpdate truoc day de lo:
    // save voi id khong ton tai roi qua CrudService.save -> doUpdate 0
    // dong roi van 200 silent no-op (verifyTenantOwnershipAgainstStoredRow
    // bo qua khi stored==null, cung mau DS94/DS99); bulkInsert/bulkUpdate
    // bypass @Valid nen name/theme thieu van ghi duoc (name/theme NOT NULL
    // trong DB, @NotNull DS80 chi co tac dung qua @Valid o save don) ->
    // 500. Kiem tra ton tai + truong bat buoc truoc moi ghi.
    // @Transactional (readOnly = false) tuong minh (mau DS105/DS106).
    // sys_theme KHONG CO cot company_id (bang toan cuc) nen khong validate
    // company o day; quyen ghi da gat o controller (theme:create/update/
    // delete, iter68).
    @Override
    @Transactional(readOnly = false)
    public Theme save(Theme entity) {
        if (entity != null && StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Chủ đề không tồn tại: "
                    + entity.getId());
        }
        if (entity != null && StringUtils.isBlank(entity.getName())) {
            throw new ServiceException("Tên chủ đề không được để trống");
        }
        if (entity != null && StringUtils.isBlank(entity.getTheme())) {
            throw new ServiceException("Chủ đề không được để trống");
        }
        checkThemeIsDefaultValues(entity);
        return super.save(entity);
    }

    // DS107 B3-B6/B10-B11: bulkInsert truoc day de lo: null/rong roi xuong
    // MyBatis <foreach> rong -> 500; thieu/rong name/theme -> 500 NOT
    // NULL. Chan het o service de 400 ro nghia.
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<Theme> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chủ đề không được để trống");
        }
        for (Theme entity : entitys) {
            checkThemeRequired(entity);
            checkThemeIsDefaultValues(entity);
        }
        return super.bulkInsert(entitys);
    }

    // DS107 B2/B3/B5/B9/B12: bulkUpdate truoc day de lo: id khong ton tai
    // -> 200 silent no-op; batch hon hop [valid, nonexistent] doi ca dong
    // hop le truoc khi cham id ma; theme bi drop -> CASE set theme=NULL
    // -> 500 NOT NULL. Tien kiem TOAN BO list truoc moi ghi (fail-closed
    // ca list, mau N1).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<Theme> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chủ đề không được để trống");
        }
        for (Theme entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Chủ đề không được để trống");
            }
            if (StringUtils.isNotBlank(entity.getId())
                    && dao.get(entity.getId()) == null) {
                throw new ServiceException("Chủ đề không tồn tại: "
                        + entity.getId());
            }
            checkThemeRequired(entity);
            checkThemeIsDefaultValues(entity);
        }
        return super.bulkUpdate(entitys);
    }

    // DS107 B7-B8: bulkDelete null/rong -> 400 ro nghia thay vi roi xuong
    // MyBatis <foreach> rong. bulkDelete id khong ton tai GIU NGUYEN
    // 200 rows 0 idempotent (DS99 G11) -- khong chan o day.
    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<Theme> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chủ đề không được để trống");
        }
        return super.bulkDelete(entitys);
    }

    // DS131 B: ThemeService truoc day KHONG CO delete() override nen
    // single delete roi thang xuong CrudService.delete: ghost id -> 200
    // rows 0 im lang, blank id -> 200 rows 0 im lang, xoa lap -> 200
    // rows 1 (mapper soft-delete khong loc del_flag) -- cung lop
    // DS123/DS129/DS130. Chan truoc bang dao.get (khong loc del_flag
    // nen tra ca dong da xoa mem, delFlag phan biet) theo mau
    // CodeRuleService.delete (DS129 A); KHONG tai dung checkExists
    // (DS103): no cho qua blank va khong check delFlag, lai dung chung
    // voi save/bulkUpdate. sys_theme KHONG CO cot company_id (bang toan
    // cuc) nen khong co guard tenant o day. bulkDelete ghost GIU NGUYEN
    // 200 rows 0 (DS99/DS106/DS126 B idempotency).
    @Override
    @Transactional(readOnly = false)
    public int delete(Theme entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được chủ đề cần xóa.");
        }
        Theme stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Chủ đề không tồn tại: "
                    + entity.getId());
        }
        return super.delete(entity);
    }

    // DS377: isDefault chi co nghia '0'/'1' (themeForm.vue:28-29 switch
    // active '1'/inactive '0', mac dinh 1). Entity chi co
    // @Length(max=1) nen gia tri gia 1 ky tu ('9'/'2'/'x') qua @Valid
    // o save don, va ca 3 duong bulk deu bi bean-validate theo
    // @Length nen '-1' (2 ky tu) da 400 san — lo hong that la gia tri
    // gia 1 ky tu qua ca 3 duong save/bulkInsert/bulkUpdate, mapper
    // ghi verbatim. Doc that: AuthController.getPersonalTheme:353
    // login chon theme dau tien co isDefault=='1', khong co thi
    // fallback themes.get(0) — gia '9' len theme mac dinh duy nhat
    // (id 532442783541223425) lam moi user chua dat PersonalTheme
    // dang nhap nhan theme sai. Chan gia tri khac null/'' ma ngoai
    // {'0','1'} -> 400 fail-closed; null van qua (cot nullable).
    private static void checkThemeIsDefaultValues(Theme entity) {
        if (entity == null) {
            return;
        }
        String isDefault = entity.getIsDefault();
        if (StringUtils.isNotBlank(isDefault)
                && !"0".equals(isDefault) && !"1".equals(isDefault)) {
            throw new ServiceException("Trạng thái mặc định của chủ đề không hợp lệ: " + isDefault);
        }
    }

    private void checkThemeRequired(Theme entity) {
        if (entity == null) {
            throw new ServiceException("Chủ đề không được để trống");
        }
        if (StringUtils.isBlank(entity.getName())) {
            throw new ServiceException("Tên chủ đề không được để trống");
        }
        if (StringUtils.isBlank(entity.getTheme())) {
            throw new ServiceException("Chủ đề không được để trống");
        }
    }

}