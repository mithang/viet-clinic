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