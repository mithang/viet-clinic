package com.geeke.member.service;

import java.util.List;
import java.util.Map;

import com.geeke.member.entity.MemberItem;
import com.geeke.member.entity.MemberManagement;
import com.geeke.treatment.entity.CostItem;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.common.service.ServiceException;
import com.geeke.member.dao.MemberSetDao;
import com.geeke.member.entity.MemberSet;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

import javax.swing.text.html.parser.Entity;

/**
 * Cài đặt thẻ hội viênService
 * @author rys
 * @version 2022-10-25
 */
 
@Service("memberSetService")
@Transactional(readOnly = true)
public class MemberSetService extends CrudService<MemberSetDao, MemberSet>{
    @Autowired
    private MemberItemService memberItemService;

    @Autowired
    private MemberManagementService memberManagementService;

    @Transactional(readOnly = false)
    public MemberSet save(MemberSet memberSet){
       if(StringUtils.isBlank(memberSet.getId())){
           // ITER64 item 1: this insert branch bypassed super.save, so the
           // generic insert guard does not cover it. Verify BEFORE
           // dao.insert (same check as the update branch below); a foreign
           // payload company used to plant a member_set row into a foreign
           // tenant (probe-proven). Child MemberItem rows saved afterwards
           // go through memberItemService.save, covered by the generic guard.
           verifyTenantOwnership(memberSet);
           memberSet.preInsert();
           // DS42: so luong con lai (number) mac dinh bang tong so (amount)
           // khi payload khong gui number — neu khong, set co number NULL
           // va phat the sau do NPE o updateNumber (unbox null) 500.
           if (memberSet.getNumber() == null && memberSet.getAmount() != null) {
               memberSet.setNumber(memberSet.getAmount());
           }
           int i = this.dao.insert(memberSet);
           if(i>0){
               // DS42: payload khong co memberItem (tao set chua co hang muc)
               // truoc day getMemberItem() null -> NPE 500 o vong for ben
               // duoi. Khong co hang muc thi khong co gi de luu.
               List<MemberItem> memberItemList = memberSet.getMemberItem();
               if (memberItemList != null) {
               for (MemberItem memberItem : memberItemList) {
                   CostItem costItem = new CostItem();
                   costItem.setId(memberItem.getName());
                   memberItem.setCostItem(costItem);
                   memberItem.setMember(memberSet);
                   memberItemService.save(memberItem);
               }
               }
           }
       }else {
           // ITER36 N2: this override bypassed the generic save guards
           // (M1/M2) by calling dao.update directly, so a foreign set id
           // + own-tenant payload company overwrote + re-homed the set
           // (reproduced live). Same two checks as generic save.
           verifyTenantOwnership(memberSet);
           verifyTenantOwnershipAgainstStoredRow(memberSet);
           // DS105 C1: save voi id khong ton tai truoc day roi qua
           // verifyTenantOwnershipAgainstStoredRow (bo qua khi stored==null,
           // cung mau DS94/DS99) roi dao.update 0 dong nhung van 200 silent
           // no-op, lai xoa + tao lai MemberItem cho id ma. Kiem tra ton
           // tai truoc moi ghi.
           if (this.dao.get(memberSet.getId()) == null) {
               throw new ServiceException("Cài đặt thẻ hội viên không tồn tại: "
                       + memberSet.getId());
           }
           memberSet.preUpdate();
           //Khi sửa số lượng thẻ hội viên cần kiểm tra số lượng thẻ đã phát hành có lớn hơn số lượng phát hành không
           List<MemberManagement> memberManagements = memberManagementService.getByMemberSetId(memberSet.getId());

           // DS42: amount null (cot amount nullable) truoc day unbox NPE
           // 500 o phep so sanh; chi kiem tra khi amount co gia tri.
           if(memberSet.getAmount() != null && memberManagements.size()>memberSet.getAmount()){
               throw new ServiceException("Số lượng thẻ hội viên sau khi sửa không được thấp hơn số lượng thẻ hội viên đã phát hành!");
           }
           int i = this.dao.update(memberSet);
           if(i>0){
               //trướcSẽ gốcđến của hạng mụctiến hành dòngXóatạiThêm
               int delete=memberItemService.deleteByMemberSet(memberSet.getId());

                   // DS42: nhu nhanh insert — payload khong co memberItem
                   // thi khong co gi de luu lai (truoc day NPE 500).
                   List<MemberItem> memberItemList = memberSet.getMemberItem();
                   if (memberItemList != null) {
                   for (MemberItem memberItem : memberItemList) {
                       memberItem.setId(null);
                       CostItem costItem = new CostItem();
                       costItem.setId(memberItem.getName());
                       memberItem.setCostItem(costItem);
                       memberItem.setMember(memberSet);
                       memberItemService.save(memberItem);

                   }
                   }

           }
       }
        return memberSet;
    }

    // DS105 C3-C8/C10-C12: bulkInsert/bulkUpdate/bulkDelete generic truoc
    // day de lo: null/rong roi xuong MyBatis <foreach> rong -> 500 SQL
    // syntax, thieu truong NOT NULL (name/type/money) -> 500
    // DataIntegrityViolation, bulkUpdate id khong ton tai -> 200 silent
    // no-op (hoac 500 do bind #{entity.type}). Chan het o service de 400
    // ro nghia, kiem tra truoc moi ghi de batch hon hop khong doi dong hop
    // le (C9). @Transactional (readOnly = false) tuong minh vi class-level
    // la readOnly = true (mau SupplierService DS104).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<MemberSet> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách cài đặt thẻ hội viên không được để trống");
        }
        for (MemberSet memberSet : entitys) {
            validateRequiredFields(memberSet);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<MemberSet> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách cài đặt thẻ hội viên không được để trống");
        }
        for (MemberSet memberSet : entitys) {
            if (StringUtils.isNotBlank(memberSet.getId())
                    && this.dao.get(memberSet.getId()) == null) {
                throw new ServiceException("Cài đặt thẻ hội viên không tồn tại: "
                        + memberSet.getId());
            }
            validateRequiredFields(memberSet);
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<MemberSet> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách cài đặt thẻ hội viên không được để trống");
        }
        return super.bulkDelete(entitys);
    }

    // DS105 C10-C12: bulkInsert/bulkUpdate bo qua @Valid, truong NOT NULL
    // thieu se 500 o mapper. type la DictItem: chi value moi co nghia.
    private void validateRequiredFields(MemberSet memberSet) {
        if (StringUtils.isBlank(memberSet.getName())) {
            throw new ServiceException("Tên cài đặt thẻ hội viên không được để trống");
        }
        if (memberSet.getType() == null
                || StringUtils.isBlank(memberSet.getType().getValue())) {
            throw new ServiceException("Loại thẻ hội viên không được để trống");
        }
        if (memberSet.getMoney() == null) {
            throw new ServiceException("Số tiền thẻ hội viên không được để trống");
        }
    }

    @Transactional
    public int updateNumber(String id) {
        // ITER36 N2: dao.get skipped the tenant check, so decrementing a
        // foreign set's stock was possible via any caller passing its id
        // (reproduced live through deleting an own card linked to a
        // foreign set). get() verifies the stored set's tenant.
        MemberSet memberSet = this.get(id);
        // DS42: number NULL (set tao qua API khong gui number, hoac dong
        // cu) truoc day unbox NPE 500. Fail-closed: khong tu suy ra ton
        // kho (set co the da phat the), bao loi ro rang thay vi NPE.
        if (memberSet == null || memberSet.getNumber() == null) {
            throw new ServiceException("Thẻ hội viên chưa có số lượng còn lại, không thể phát hành thêm!");
        }
        if(memberSet.getNumber()==0){
            throw new ServiceException("Hội viênthẻSố lượng còn lại là 0, Không thểlạiThêmHội viên!");

        }else {
            // DS61: tru tuyet doi number = number - 1 WHERE number > 0.
            // Truoc day get() -> number-1 trong Java -> UPDATE tuyet doi:
            // phat the song song cho cac benh nhan khac nhau deu doc cung
            // N va ghi cung N-1, ban vuot so the con lai. Ke thua cuoc dua
            // (rows == 0) bi tu choi nhu truong hop het the, khong ghi de.
            int i = this.dao.decrementNumber(id);
            if (i == 0) {
                throw new ServiceException("Hội viênthẻSố lượng còn lại là 0, Không thểlạiThêmHội viên!");
            }
            return i;
        }

    }
    @Transactional
    public int addNumber(String id) {
        // ITER36 N2: dao.get skipped the tenant check, so crediting a
        // foreign set's stock was possible via deleting an own card linked
        // to it (reproduced live: foreign stock 50 -> 51). get() verifies
        // the stored set's tenant.
        MemberSet memberSet = this.get(id);
        // DS42: nhu updateNumber — number NULL truoc day unbox NPE 500.
        if (memberSet == null || memberSet.getNumber() == null) {
            throw new ServiceException("Thẻ hội viên chưa có số lượng còn lại, không thể hoàn trả!");
        }
        // DS61: cong tuyet doi number = number + 1. Truoc day get() ->
        // number+1 trong Java -> UPDATE tuyet doi: huy the song song
        // mat luot cong tra theo cung mau nhu updateNumber.
        int i = this.dao.incrementNumber(id);
        if (i == 0) {
            throw new ServiceException("Thẻ hội viên chưa có số lượng còn lại, không thể hoàn trả!");
        }
        return i;
    }
}