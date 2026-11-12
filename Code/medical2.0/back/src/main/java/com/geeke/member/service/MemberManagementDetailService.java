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

import java.util.ArrayList;
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

    // DS219: chan so am (number/useNumber) truoc moi ghi -- 0/null van
    // cho phep (cot nullable, useNumber mac dinh 0; NULL number = khong
    // gioi han theo DS154 A4); am -> 400 fail-closed, mau DS209-DS218
    // (ho "so am"). Duong ghi duy nhat: MemberManagementService.save
    // (tao the) lap memberSet.memberItem tu payload, dat
    // detail.number = item.getNumber() thang tu payload roi goi
    // memberManagementDetailService.save() -- khong co controller rieng
    // cho detail, khong caller bulkInsert/bulkUpdate nao. So am -5 da
    // luu duoc live (probe pre P2/P7: row (-5, 0)) va lam hong the:
    // getMember yeu cau total > used (total=-5 khong bao gio > 0) nen
    // the khong bao gio dung duoc. Chan o day; ca lan tao the cung
    // transaction nen 400 rollback luon the. updateUseNumber KHONG can
    // sua: claim DS62/DS63 da chan useNumber am (probe pre P5: 400).
    @Override
    @Transactional(readOnly = false)
    public MemberManagementDetail save(MemberManagementDetail entity) {
        checkMemberManagementDetailNonNegative(entity);
        return super.save(entity);
    }

    private static void checkMemberManagementDetailNonNegative(MemberManagementDetail entity) {
        if (entity == null) {
            return;
        }
        if (entity.getNumber() != null && entity.getNumber() < 0) {
            throw new com.geeke.common.service.ServiceException("Số lượng thẻ hội viên không được âm");
        }
        if (entity.getUseNumber() != null && entity.getUseNumber() < 0) {
            throw new com.geeke.common.service.ServiceException("Số lần đã sử dụng không được âm");
        }
    }

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
        // DS62: giu lai gia tri DB cua tung detail de vong ghi claim theo
        // dung gia tri da doc (xem vong ghi ben duoi).
        java.util.Map<String, MemberManagementDetail> dbRows = new java.util.HashMap<>();
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
                        throw new com.geeke.common.service.ServiceException("Không có quyền truy cập tenant khác Dữ liệu");
                    }
                    dbRows.put(memberManagementDetail.getId(), dbRow);
                }
            }
        }

        // DS63: ghi use-count bang atomic claim (mau DS52-DS61), khong con
        // SET mu gia tri tuyet doi tu payload. Frontend gui gia tri tuyet
        // doi moi (= gia tri no doc + so luot dung them), nen:
        // - payload < gia tri DB hien tai: payload stale hoac gia mao doi
        //   use-count ve (nap lai the da dung) -> 400, khong ghi.
        // - payload > allowance number: vuot so luot the cho phep -> 400.
        // - claimUseNumber chi ghi khi use_number van dung gia tri ky
        //   vong va gia tri moi trong [hien tai, number]; luong song
        //   song thua tra 0 dong -> 400 nhu truong hop tuan tu (nap lai
        //   the roi thu lai thi duoc). Toan bo saveToll la mot transaction
        //   nen 400 o day rollback ca phieu thu, don thuoc van thu duoc
        //   o lan thu.
        // - DS63: gia tri ky vong KHONG lay tu DB nua khi frontend da gui
        //   expectedUseNumber (gia tri goc no doc). DS62 lay gia tri DB
        //   vua doc lam expected nen mot lan replay stale co payload
        //   TRUNG gia tri DB hien tai (doc 5, +5 -> gui 10 trong khi DB
        //   da len 10) van duoc nhan nhu no-op delta 0, 5 luot dung bien
        //   mat. Claim theo gia tri goc thi replay stale tra 0 dong ->
        //   400; no-op hop le (expected == DB) van duoc nhan. Khi
        //   expectedUseNumber vang (client cu / caller khac) fallback ve
        //   hanh vi DS62: lay gia tri DB da doc.
        for (MemberManagementDetail memberManagementDetail : memberManagementDetails) {
            if (memberManagementDetail == null || StringUtils.isBlank(memberManagementDetail.getId())) {
                continue;
            }
            MemberManagementDetail dbRow = dbRows.get(memberManagementDetail.getId());
            if (dbRow == null || memberManagementDetail.getUseNumber() == null) {
                continue;
            }
            // DB khong co dong NULL (kiem tra 2026-11-05: 35/35 dong deu co
            // number + use_number); coi NULL nhu 0 de tranh NPE unboxing.
            int dbValue = dbRow.getUseNumber() == null ? 0 : dbRow.getUseNumber();
            int expected = memberManagementDetail.getExpectedUseNumber() != null
                    ? memberManagementDetail.getExpectedUseNumber() : dbValue;
            if (memberManagementDetail.getUseNumber() < dbValue) {
                throw new com.geeke.common.service.ServiceException("Thẻ hội viên đã thay đổi, vui lòng thử lại");
            }
            if (dbRow.getNumber() != null && memberManagementDetail.getUseNumber() > dbRow.getNumber()) {
                throw new com.geeke.common.service.ServiceException("Số lần sử dụng vượt quá số lượng thẻ");
            }
            int rows = this.dao.claimUseNumber(memberManagementDetail.getId(),
                    memberManagementDetail.getUseNumber(), expected);
            if (rows == 0) {
                throw new com.geeke.common.service.ServiceException("Thẻ hội viên đã thay đổi, vui lòng thử lại");
            }
        }

        //Sửa lầnsốsau, CầnSửaHội viênthẻTrạng thái
        memberManagementService.updaStatus(memberManagement);
    }

    @Transactional(readOnly = false)
    public List<MemberManagementDetail> getByManagementId(String managementId) {
        List<MemberManagementDetail> memberManagementDetails = this.dao.getByManagementId(managementId);
        // DS154 A1: MemberSetService.save (update) hard-deletes ALL items of
        // the set (deleteByMemberSet) and re-inserts new ones with fresh ids,
        // so cards issued from the set keep details pointing at the OLD item
        // ids. get() then returns null and memberItem.getCostItem() NPEd 500
        // on GET /member/memberManagement/{card}. Skip orphaned detail rows
        // instead (same filter as RecipetemplateDetailService.
        // getByRecipeTemplateInfoId, which drops details whose drug is gone);
        // a fully-orphaned card reads with an empty detail list, and
        // getMember treats it as not covering (count 0 == size 0 -> skip).
        List<MemberManagementDetail> live = new ArrayList<>();
        for (MemberManagementDetail memberManagementDetail : memberManagementDetails) {
            if (memberManagementDetail.getMemberItem() == null
                    || StringUtils.isBlank(memberManagementDetail.getMemberItem().getId())) {
                continue;
            }
            MemberItem memberItem = memberItemService.get(memberManagementDetail.getMemberItem().getId());
            if (memberItem == null) {
                continue;
            }
            //TheoChi tiết thẻ hội viênLấyhạng mụcnội dung
            CostItem costItem = null;
            if (memberItem.getCostItem() != null
                    && StringUtils.isNotBlank(memberItem.getCostItem().getId())) {
                costItem = costItemService.get(memberItem.getCostItem().getId());
            }
            memberItem.setCostItem(costItem);
            memberManagementDetail.setMemberItem(memberItem);
            live.add(memberManagementDetail);
        }
        return live;
    }


    public MemberManagementDetail getByManagementIdAndMemberItemId(String managementId, String memberItemId) {
       MemberManagementDetail memberManagementDetail = this.dao.getByManagementIdAndMemberItemId(managementId,memberItemId);
       return memberManagementDetail;
    }
}