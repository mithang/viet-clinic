package com.geeke.member.service;

import com.geeke.common.service.CrudService;
import com.geeke.member.dao.MemberManagementDao;
import com.geeke.member.dao.MemberManagementDetailDao;
import com.geeke.utils.StringUtils;
import com.geeke.member.entity.MemberItem;
import com.geeke.member.entity.MemberManagement;
import com.geeke.member.entity.MemberManagementDetail;
import com.geeke.treatment.entity.CostItem;
import com.geeke.treatment.service.CostItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Quản lý thẻ hội viênService
 * @author rys
 * @version 2022-10-26
 */
 
@Service("memberManagementDetailService")
@Transactional(readOnly = true)
public class MemberManagementDetailService extends CrudService<MemberManagementDetailDao, MemberManagementDetail>{
  @Autowired
  private MemberItemService memberItemService;

  @Autowired
  private CostItemService costItemService;

  @Autowired
  private MemberManagementService memberManagementService;

    @Transactional(readOnly = false)
    public void updateUseNumber(MemberManagement memberManagement) {

        List<MemberManagementDetail> memberManagementDetails = memberManagement.getMemberManagementDetails();

        // ITER38 P1: kiem tra tenant TRUOC KHI ghi. Truoc day ca hai lenh ghi
        // duoi day deu theo id tran tu payload: dao.updateUseNumber ghi
        // use_number theo id detail, dao.updateStatus ghi status='1' theo id
        // the — khong load dong DB, khong doi chieu tenant — nen mot don thu
        // phi own kem the + detail cua tenant khac van ghi thang vao the la
        // (tai hien live qua POST /toll/tollInfo/save/toll: the la use_number
        // 5 -> 99, status 0 -> 1). Chan o day, truoc moi lenh ghi:
        // the la -> memberManagementService.get() nem 400; detail la hoac
        // detail khong thuoc the nay -> 400. Id rong / dong khong ton tai
        // van cho qua nhu cu (ghi 0 dong), company DB rong cung cho qua
        // theo ngu nghia verifyTenantOwnership.
        if (memberManagement != null && StringUtils.isNotBlank(memberManagement.getId())) {
            memberManagementService.get(memberManagement.getId());
        }
        if (memberManagementDetails != null) {
            for (MemberManagementDetail memberManagementDetail : memberManagementDetails) {
                if (memberManagementDetail == null || StringUtils.isBlank(memberManagementDetail.getId())) {
                    continue;
                }
                MemberManagementDetail dbRow = this.dao.getOwnership(memberManagementDetail.getId());
                if (dbRow != null) {
                    verifyTenantOwnership(dbRow);
                    if (memberManagement != null && StringUtils.isNotBlank(memberManagement.getId())
                            && dbRow.getMemberManagement() != null
                            && StringUtils.isNotBlank(dbRow.getMemberManagement().getId())
                            && !memberManagement.getId().equals(dbRow.getMemberManagement().getId())) {
                        throw new com.geeke.common.service.ServiceException("Không có quyền truy cập tenant khácDữ liệu");
                    }
                }
            }
        }

        for (MemberManagementDetail memberManagementDetail : memberManagementDetails) {
            this.dao.updateUseNumber(memberManagementDetail);
        }

        //Sửa lầnsốsau, CầnSửaHội viênthẻTrạng thái
        memberManagementService.updaStatus(memberManagement);
    }

    @Transactional(readOnly = false)
    public List<MemberManagementDetail> getByManagementId(String managementId) {
        List<MemberManagementDetail> memberManagementDetails = this.dao.getByManagementId(managementId);
        for (MemberManagementDetail memberManagementDetail : memberManagementDetails) {
            MemberItem memberItem = memberItemService.get(memberManagementDetail.getMemberItem().getId());
            //TheoChi tiết thẻ hội viênLấyhạng mụcnội dung
            CostItem costItem = costItemService.get(memberItem.getCostItem().getId());
            memberItem.setCostItem(costItem);
            memberManagementDetail.setMemberItem(memberItem);
        }
        return memberManagementDetails;
    }


    public MemberManagementDetail getByManagementIdAndMemberItemId(String managementId, String memberItemId) {
       MemberManagementDetail memberManagementDetail = this.dao.getByManagementIdAndMemberItemId(managementId,memberItemId);
       return memberManagementDetail;
    }
}