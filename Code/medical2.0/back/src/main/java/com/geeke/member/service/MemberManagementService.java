package com.geeke.member.service;

import java.util.*;

import com.geeke.member.entity.MemberItem;
import com.geeke.member.entity.MemberManagementDetail;
import com.geeke.member.entity.MemberSet;
import com.geeke.outpatient.entity.RecipelDetail;
import com.geeke.outpatient.entity.RecipelInfoEvt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.common.service.ServiceException;
import com.geeke.member.dao.MemberManagementDao;
import com.geeke.member.entity.MemberManagement;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;

/**
 * Quản lý thẻ hội viênService
 * @author rys
 * @version 2022-10-26
 */
 
@Service("memberManagementService")
@Transactional(readOnly = true)
public class MemberManagementService extends CrudService<MemberManagementDao, MemberManagement>{
        @Autowired
        private MemberManagementDetailService memberManagementDetailService;

        @Autowired
        private MemberSetService memberSetService;

        @Autowired
        private MemberItemService memberItemService;

        @Transactional(readOnly = false)
        public MemberManagement save(MemberManagement memberManagement){
            // Sửa (id đã có) trước đây rơi vào cùng nhánh INSERT với Tạo nên
            // luôn lỗi trùng khóa chính — form Sửa qua UI không bao giờ lưu
            // được dù dialog Sửa đã có sẵn. Nhánh update dùng mapper update
            // (giữ nguyên card, không sinh lại, không trừ số lượng thẻ lại).
            if (StringUtils.isNotBlank(memberManagement.getId())) {
                // ITER36 N1: this override bypassed the generic save guards
                // (M1/M2) by calling dao.update directly, so a foreign card id
                // + own-tenant payload company overwrote + re-homed the card
                // (reproduced live). Same two checks as generic save.
                verifyTenantOwnership(memberManagement);
                verifyTenantOwnershipAgainstStoredRow(memberManagement);
                memberManagement.preUpdate();
                this.dao.update(memberManagement);
                return memberManagement;
            }
            memberManagement.preInsert();
            String id = memberManagement.getId();
            String card = new Date().getTime() + "";
            card = card.substring(card.length()-8);
            memberManagement.setCard(card);
            //Khi liên kết thẻ, kiểm tra người dùng đã làm thẻ chưa
            List<MemberManagement> memberManagements = this.dao.getByPatientAndMember(memberManagement.getPatient().getId(),memberManagement.getMemberSet().getId());
            if(!CollectionUtils.isEmpty(memberManagements)){
                throw new ServiceException("đóNgười dùngĐãliên kếtchỉ địnhrồiđóHội viênthẻ, Không thểtrùng lặpliên kếtchỉ định");
            }
            int insert = this.dao.insert(memberManagement);
            //LưutốtsauSẽ Dữ liệuLưuĐếnChi tiếtbảng trong 

            List<MemberItem> memberItem = memberManagement.getMemberSet().getMemberItem();
            for (MemberItem item : memberItem) {
                MemberManagementDetail memberManagementDetail = new MemberManagementDetail();
                memberManagementDetail.setMemberManagement(memberManagement);
                memberManagementDetail.setMemberItem(item);
                memberManagementDetail.setNumber(item.getNumber());
                memberManagementDetail.setUseNumber(0);
                memberManagementDetail.setCompany(memberManagement.getCompany());
                memberManagementDetailService.save(memberManagementDetail);
            }
            //liên kếtthẻThành côngCầnđikhấu trừHội viênthẻSố lượng
            // ITER36 N1: the set id comes from the payload, so creating a
            // card against a foreign set used to decrement the FOREIGN
            // set's stock (reproduced live: 50 -> 49). get() verifies the
            // stored set's tenant and throws for a foreign set, rolling
            // this whole save back; an own set passes unchanged.
            if(insert>0){
                memberSetService.get(memberManagement.getMemberSet().getId());
                int i = memberSetService.updateNumber(memberManagement.getMemberSet().getId());
            }
            return memberManagement;
        }
    @Transactional(readOnly = false)
    public List<MemberManagement> getByMemberSetId(String memberSetId) {
        List<MemberManagement> memberManagements = this.dao.getByMemberSetId(memberSetId);
        return memberManagements;
    }
    
    @Transactional(readOnly = false)
    public List<MemberManagement> getMember(RecipelInfoEvt recipelInfoEvt) {
            //Lấyhạng mục khám chữa bệnhid
        List<RecipelDetail> recipelDetailEvtList = recipelInfoEvt.getRecipelDetailEvtList();

        //TheoBệnh nhânidLấycủa nóCó hay khôngliên kếtchỉ địnhrồitrải nghiệmthẻ
        List<MemberManagement> memberManagements = filterOwnTenant(this.dao.getByPatientId(recipelInfoEvt.getRecipelInfo().getRegistration().getPatientId().getId()));
        List<MemberManagement> memberManagementList = new ArrayList<>();

        //Theo người dùngliên kếtchỉ địnhthẻ, điLấycủa nóliên kếtchỉ định của hạng mục
        if(!CollectionUtils.isEmpty(memberManagements)){
            for (MemberManagement memberManagement : memberManagements) {
                List<MemberManagementDetail> memberManagementDetails = memberManagementDetailService.getByManagementId(memberManagement.getId());

                int count=0;
                for (MemberManagementDetail memberManagementDetail : memberManagementDetails) {
                    if(memberManagementDetail.getNumber()==memberManagementDetail.getUseNumber()){
                        count++;
                    }
                }
                //NếuđóHội viênthẻsử dụng lầnsốđềusử dụngxong, Kết thúcnày lầnvòng lặp
                if(count==memberManagementDetails.size()){
                    continue;
                }

                //Tồn tạiChưasử dụngxong của Hội viênthẻ giờ, tiến hành dòngKiểm tra
                boolean flage = false;
                MemberSet memberSet = memberSetService.get(memberManagement.getMemberSet().getId());
                //Lấytrải nghiệmthẻChi tiết
                List<MemberItem> memberItems = memberItemService.getByMemberSetId(memberSet.getId());
                for (MemberItem memberItem : memberItems) {
                    for (RecipelDetail recipelDetail : recipelDetailEvtList) {
                            if(Objects.equals(recipelDetail.getDrugStuffId().getCostItem().getId(),memberItem.getCostItem().getId())){
                                //Nếu bằng nhau thì lấy tiếp người dùng đó đã dùng hết chưa
                                MemberManagementDetail memberManagementDetail = memberManagementDetailService.getByManagementIdAndMemberItemId(memberManagement.getId(),memberItem.getId());
                                if(memberManagementDetail.getNumber()>memberManagementDetail.getUseNumber()) {
                                    flage = true;
                                    break;
                                }
                            }
                    }
                    if(flage){
                        break;
                    }
                }
                if(flage){
                    memberManagement.setMemberSet(memberSet);
                    memberManagement.setMemberItems(memberItems);
                    memberManagement.setMemberManagementDetails(memberManagementDetails);
                    memberManagementList.add(memberManagement);
                }
            }

            return memberManagementList;
        }else {
            return new ArrayList<MemberManagement>();
        }

    }

    @Transactional(readOnly = false)
    public void updaStatus(MemberManagement memberManagement) {
            this.dao.updateStatus(memberManagement);
    }

    // Z1a: the khach hang theo patient_id tran — chi giu the thuoc session
    // tenant truoc khi nap chi tiet. Truoc day the tenant la chi bi chan
    // gian tiep (400 tu CrudService.get khi nap MemberItem la), khong chac
    // chan: the khong co chi tiet hoac MemberItem dung chung se lot qua.
    // Session rong/"null" (luong noi bo khong session) giu hanh vi cu,
    // hang company null giu nhu verifyTenantOwnership.
    private List<MemberManagement> filterOwnTenant(List<MemberManagement> rows) {
        String tenant = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (tenant == null || tenant.isEmpty() || "null".equals(tenant) || rows == null) {
            return rows;
        }
        List<MemberManagement> kept = new ArrayList<>();
        for (MemberManagement row : rows) {
            if (row == null || row.getCompany() == null || row.getCompany().getId() == null
                    || row.getCompany().getId().isEmpty() || tenant.equals(row.getCompany().getId())) {
                kept.add(row);
            }
        }
        return kept;
    }

    public List<MemberManagement> getByPatientId(String patientId) {
        List<MemberManagement> memberManagements = filterOwnTenant(this.dao.getByPatientId(patientId));
        List<MemberManagement> memberManagementList = new ArrayList<>();
        for (MemberManagement memberManagement : memberManagements) {
            List<MemberManagementDetail> memberManagementDetails = memberManagementDetailService.getByManagementId(memberManagement.getId());
            int count=0;
            for (MemberManagementDetail memberManagementDetail : memberManagementDetails) {
                if(memberManagementDetail.getNumber()==memberManagementDetail.getUseNumber()){
                    count++;
                }
            }
            if(count<memberManagementDetails.size()){
                memberManagementList.add(memberManagement);
            }
        }
        return memberManagementList;
    }
}