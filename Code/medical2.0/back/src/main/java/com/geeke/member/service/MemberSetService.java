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
           memberSet.preInsert();
           int i = this.dao.insert(memberSet);
           if(i>0){
               List<MemberItem> memberItemList = memberSet.getMemberItem();
               for (MemberItem memberItem : memberItemList) {
                   CostItem costItem = new CostItem();
                   costItem.setId(memberItem.getName());
                   memberItem.setCostItem(costItem);
                   memberItem.setMember(memberSet);
                   memberItemService.save(memberItem);
               }
           }
       }else {
           memberSet.preUpdate();
           //Khi sửa số lượng thẻ hội viên cần kiểm tra số lượng thẻ đã phát hành có lớn hơn số lượng phát hành không
           List<MemberManagement> memberManagements = memberManagementService.getByMemberSetId(memberSet.getId());

           if(memberManagements.size()>memberSet.getAmount()){
               throw new ServiceException("Số lượng thẻ hội viên sau khi sửa không được thấp hơn số lượng thẻ hội viên đã phát hành!");
           }
           int i = this.dao.update(memberSet);
           if(i>0){
               //trướcSẽ gốcđến của hạng mụctiến hành dòngXóatạiThêm
               int delete=memberItemService.deleteByMemberSet(memberSet.getId());

                   List<MemberItem> memberItemList = memberSet.getMemberItem();
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
        return memberSet;
    }

    @Transactional
    public int updateNumber(String id) {
        //LấyhiệncóHội viênthẻSố lượng
        MemberSet memberSet = this.dao.get(id);
        if(memberSet.getNumber()==0){
            throw new ServiceException("Hội viênthẻSố lượng còn lại là 0, Không thểlạiThêmHội viên!");

        }else {
            int newNumber=memberSet.getNumber()-1;
            int i = this.dao.updateNumber(id,newNumber);
            return i;
        }

    }
    @Transactional
    public int addNumber(String id) {
        //LấyhiệncóHội viênthẻSố lượng
        MemberSet memberSet = this.dao.get(id);
        int newNumber = memberSet.getNumber()+1;
        int i = this.dao.updateNumber(id, newNumber);
        return i;
    }
}