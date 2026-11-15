package com.geeke.member.service;

import java.util.List;
import java.util.Map;

import com.geeke.common.service.ServiceException;
import com.geeke.treatment.entity.CostItem;
import com.geeke.treatment.service.CostItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.member.dao.MemberItemDao;
import com.geeke.member.entity.MemberItem;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * Chi tiết thẻ hội viênService
 * @author rys
 * @version 2022-10-25
 */
 
@Service("memberItemService")
@Transactional(readOnly = true)
public class MemberItemService extends CrudService<MemberItemDao, MemberItem>{
    @Autowired
    private CostItemService costItemService;

    // DS125 B: single delete truoc day de lo: ghost id / dong da xoa mem
    // roi van 200 silent no-op (verifyTenantOwnershipAgainstStoredRow bo
    // qua khi stored==null, cung mau DS123/DS124/DS125A; mapper delete
    // khong loc del_flag). Chan o service de 400 ro nghia truoc khi xoa.
    // Khong caller noi bo nao goi delete() nay; UI xoa hang live van 200
    // nhu cu.
    @Override
    @Transactional(readOnly = false)
    public int delete(MemberItem entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được chi tiết thẻ hội viên cần xóa.");
        }
        MemberItem stored = dao.get(entity.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Chi tiết thẻ hội viên không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
    }

    // DS107 A1/A10-A12: save/bulkInsert/bulkUpdate truoc day de lo:
    // save voi id khong ton tai roi qua CrudService.save -> doUpdate 0
    // dong roi van 200 silent no-op (verifyTenantOwnershipAgainstStoredRow
    // bo qua khi stored==null, cung mau DS94/DS99); bulkInsert/bulkUpdate
    // bypass @Valid nen member/costItem thieu van ghi duoc (member_id /
    // cost_item_id NOT NULL trong DB) -> 500. Kiem tra ton tai + FK truoc
    // moi ghi. @Transactional (readOnly = false) tuong minh vi class-level
    // la readOnly = true (mau DS105/DS106). Luu y: member_item KHONG CO
    // cot name (entity name la transient, chi tam luu) nen khong validate
    // name o day.
    @Override
    @Transactional(readOnly = false)
    public MemberItem save(MemberItem entity) {
        // DS158 C: single save() never called checkMemberItemRequired
        // (bulkInsert/bulkUpdate do, DS107). The direct /memberItem/save
        // endpoint is still 400 via @Valid @NotNull on costItem, but the
        // MemberSet save cascade calls this service method INTERNALLY
        // (MemberSetService:92/:136), building costItem from the item's
        // transient `name` -- a nameless item yields costItem id null ->
        // DAO insert on the NOT NULL cost_item_id column -> 500. Call the
        // check here so API + internal callers fail closed 400 alike.
        checkMemberItemRequired(entity);
        checkMemberItemNonNegative(entity);
        checkMemberItemUseNotExceedNumber(entity);
        if (entity != null && StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Chi tiết thẻ hội viên không tồn tại: "
                    + entity.getId());
        }
        return super.save(entity);
    }

    // DS107 A3-A6/A10-A11: bulkInsert truoc day de lo: null/rong roi xuong
    // MyBatis <foreach> rong -> 500 (hien tai global handler anh xa mot
    // so loi thanh 400, nhung van chan ro o service de hanh vi khong phu
    // thuoc vao exception mapping); thieu member/costItem -> 500 NOT NULL.
    // Chan het o service de 400 ro nghia.
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<MemberItem> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chi tiết thẻ hội viên không được để trống");
        }
        for (MemberItem entity : entitys) {
            checkMemberItemRequired(entity);
            checkMemberItemNonNegative(entity);
            checkMemberItemUseNotExceedNumber(entity);
        }
        return super.bulkInsert(entitys);
    }

    // DS107 A2/A3/A5/A9/A12: bulkUpdate truoc day de lo: id khong ton tai
    // -> 200 silent no-op; batch hon hop [valid, nonexistent] doi ca dong
    // hop le truoc khi cham id ma; member bi drop -> CASE set
    // member_id=NULL -> 500 NOT NULL. Tien kiem TOAN BO list truoc moi ghi
    // (fail-closed ca list, mau N1).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<MemberItem> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chi tiết thẻ hội viên không được để trống");
        }
        for (MemberItem entity : entitys) {
            if (entity == null) {
                throw new ServiceException("Chi tiết thẻ hội viên không được để trống");
            }
            if (StringUtils.isNotBlank(entity.getId())
                    && dao.get(entity.getId()) == null) {
                throw new ServiceException("Chi tiết thẻ hội viên không tồn tại: "
                        + entity.getId());
            }
            checkMemberItemRequired(entity);
            checkMemberItemNonNegative(entity);
            checkMemberItemUseNotExceedNumber(entity);
        }
        return super.bulkUpdate(entitys);
    }

    // DS107 A7-A8: bulkDelete null/rong -> 400 ro nghia thay vi roi xuong
    // MyBatis <foreach> rong. bulkDelete id khong ton tai GIU NGUYEN
    // 200 rows 0 idempotent (DS99 G11) -- khong chan o day.
    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<MemberItem> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách chi tiết thẻ hội viên không được để trống");
        }
        return super.bulkDelete(entitys);
    }

    // DS216: chan so am (number/useNumber) truoc moi ghi -- 0/null van
    // cho phep (cot nullable, useNumber mac dinh 0); am -> 400
    // fail-closed, mau DS209-DS215 (ho "so am"). Goi trong save() (bao
    // ca cascade tu MemberSetService) + vong lap bulkInsert/bulkUpdate.
    // DS297: chan so luong da su dung vuot so luong tang (useNumber >
    // number) truoc moi ghi -- cung hinh dang DS296 (MemberSet.number
    // > amount) va co tien le chan cung phep so sanh o bang em:
    // MemberManagementDetail.updateUseNumber (DS62/DS63) chan
    // useNumber > number bang atomic claim ("So lan su dung vuot qua
    // so luong the"). MemberItemService truoc day chi chan am (DS216).
    // Ca 2 cot deu duoc ghi o insert/update/bulkInsert/bulkUpdate
    // (MemberItemDao.xml :111/:132-135/:159/CASE :211) nen guard so
    // sanh truc tiep payload la du, khong can gop stored. Khong caller
    // noi bo nao set MemberItem.useNumber (chi API save/bulk* + cascade
    // tu MemberSetService truyen payload qua save()); luong dung the
    // that cap nhat MemberManagementDetail.useNumber, khong cham day.
    // Bound: ca 2 co gia tri va useNumber > number -> 400; bang nhau
    // giu hop le (dung het), nho hon giu hop le; mot trong hai null
    // giu hop le (cot nullable; NULL number = khong gioi han theo
    // DS154 A4 nhu DS219). Goi trong save() (bao ca cascade tu
    // MemberSetService) + vong lap bulkInsert/bulkUpdate.
    private static void checkMemberItemUseNotExceedNumber(MemberItem entity) {
        if (entity == null) {
            return;
        }
        if (entity.getNumber() != null && entity.getUseNumber() != null
                && entity.getUseNumber() > entity.getNumber()) {
            throw new ServiceException("Số lượng đã sử dụng không được vượt quá số lượng tặng");
        }
    }

    private static void checkMemberItemNonNegative(MemberItem entity) {
        if (entity == null) {
            return;
        }
        if (entity.getNumber() != null && entity.getNumber() < 0) {
            throw new ServiceException("Số lượng tặng không được âm");
        }
        if (entity.getUseNumber() != null && entity.getUseNumber() < 0) {
            throw new ServiceException("Số lượng đã sử dụng không được âm");
        }
    }

    private void checkMemberItemRequired(MemberItem entity) {
        if (entity == null) {
            throw new ServiceException("Chi tiết thẻ hội viên không được để trống");
        }
        if (entity.getMember() == null
                || StringUtils.isBlank(entity.getMember().getId())) {
            throw new ServiceException("Thẻ hội viên không được để trống");
        }
        if (entity.getCostItem() == null
                || StringUtils.isBlank(entity.getCostItem().getId())) {
            throw new ServiceException("Hạng mục khám chữa bệnh không được để trống");
        }
    }

    @Transactional(readOnly = false)
    public List<MemberItem> getByMemberSetId(String memberSetId) {
        List<MemberItem> memberItemList=this.dao.getByMemberSetId(memberSetId);
        for (MemberItem memberItem : memberItemList) {
            memberItem.setName(memberItem.getCostItem().getId());
            //Theohạng mục khám chữa bệnhidLấyhạng mục khám chữa bệnh
            CostItem costItem = costItemService.get(memberItem.getCostItem().getId());
            memberItem.setCostItem(costItem);
        }
        return memberItemList;
    }
    @Transactional(readOnly = false)
    public int deleteByMemberSet(String memberSetId) {
        int i=this.dao.deleteByMemberSet(memberSetId);
        return i;
    }
}