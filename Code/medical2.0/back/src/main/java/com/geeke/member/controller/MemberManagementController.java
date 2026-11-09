package com.geeke.member.controller;

import java.util.List;

import com.geeke.member.entity.MemberItem;
import com.geeke.member.entity.MemberManagementDetail;
import com.geeke.member.entity.MemberSet;
import com.geeke.member.service.MemberItemService;
import com.geeke.member.service.MemberManagementDetailService;
import com.geeke.member.service.MemberSetService;
import com.geeke.outpatient.entity.RecipelInfoEvt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
import com.geeke.member.entity.MemberManagement;
import com.geeke.member.service.MemberManagementService;
import com.geeke.common.controller.CrudController;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.StringUtils;

/**
 * Quản lý thẻ hội viênController
 * @author rys
 * @version 2022-10-26
 */
@RestController
@RequestMapping(value = "/member/memberManagement")
public class MemberManagementController extends CrudController<MemberManagementService, MemberManagement> {

	@Autowired
	protected MemberManagementService memberManagementService;

	@Autowired
    private MemberSetService memberSetService;

	@Autowired
    private MemberItemService memberItemService;

	@Autowired
    private MemberManagementDetailService memberManagementDetailService;

    @Override
    protected MemberManagementService getService() {
        return memberManagementService;
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<JSONObject> getById(@PathVariable("id") String id) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }

        MemberManagement entity = memberManagementService.get(id);
        // DS42: id la -> service.get tra null; truoc day getMemberSet()
        // tren null NPE 500. Tra 200 data null nhu CrudController goc.
        if (entity == null) {
            return ResponseEntity.ok(ResultUtil.successJson(entity));
        }
        // DS160 C: set da xoa (member_set_id tro toi id khong ton tai) thi
        // get() tra null; truoc day getId() tren null NPE 500 o :66, ca
        // form the vo theo. Day la loi DOC (the van co that: benh nhan,
        // so the, trang thai) nen tra 200 voi memberSet null + chi tiet
        // van load nhu cu (tien le DS42 ghost id -> 200 data null, U2
        // placeholder khi doc gap du lieu hong) thay vi 400 chan ca form.
        MemberSet memberSet = null;
        if (entity.getMemberSet() != null
                && StringUtils.isNotBlank(entity.getMemberSet().getId())) {
            memberSet = memberSetService.get(entity.getMemberSet().getId());
        }
        if (memberSet != null) {
            List<MemberItem> byMemberSetId = memberItemService.getByMemberSetId(memberSet.getId());
            memberSet.setMemberItem(byMemberSetId);
        }
        entity.setMemberSet(memberSet);
        //TheoQuản lýidLấyChi tiếtid
        List<MemberManagementDetail> memberManagementDetails = memberManagementDetailService.getByManagementId(id);
        entity.setMemberManagementDetails(memberManagementDetails);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    @Override
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody MemberManagement entity) {
        if (!hasAnySessionPermission()) {
            return forbidden();
        }

        // ITER36 N1: the set id used to come from the payload, so deleting
        // an own card with a foreign set id credited the FOREIGN set's
        // stock (reproduced live: 49 -> 50). Take the set from the stored
        // card instead; get() also tenant-checks the card itself.
        String storedSetId = null;
        if (entity != null && StringUtils.isNotBlank(entity.getId())) {
            MemberManagement stored = memberManagementService.get(entity.getId());
            if (stored != null && stored.getMemberSet() != null) {
                storedSetId = stored.getMemberSet().getId();
            }
        }
        // ITER37 O3: delete and addNumber run in two separate
        // transactions (the controller is not transactional), so the
        // old order — delete first, credit the set after — committed
        // the card void (status 0 -> 2) before addNumber threw 400 for
        // a foreign set: the API failed but the card was already gone
        // (reproduced live). Check the stored set's tenant BEFORE
        // deleting: get() throws for a foreign set, so nothing is
        // written; an own set passes and the old delete + credit order
        // is unchanged. A missing set (get() null) keeps the old path
        // and is out of scope for this fix.
        if (StringUtils.isNotBlank(storedSetId)) {
            memberSetService.get(storedSetId);
        }
        int rows = memberManagementService.delete(entity);
        //Vô hiệu hóathẻThành côngsauCầnSẽ thẻcòntrả lạiHội viênthẻ
        if(rows>0 && StringUtils.isNotBlank(storedSetId)){
            memberSetService.addNumber(storedSetId);
        }
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    //Lấy thông tin thẻ hội viên theo chi tiết đơn thuốc và chi tiết đơn thuốc
    @PostMapping(value = "getMember")
    public ResponseEntity<JSONObject> getMember(@RequestBody RecipelInfoEvt recipelInfoEvt){
        if (!hasAnySessionPermission()) {
            return forbidden();
        }

        List<MemberManagement> member = memberManagementService.getMember(recipelInfoEvt);
        return ResponseEntity.ok(ResultUtil.successJson(member));
    }

    @GetMapping(value = "getByPatientId/{patientId}")
    public ResponseEntity<JSONObject> getByPatientId(@PathVariable("patientId") String patientId){
        if (!hasAnySessionPermission()) {
            return forbidden();
        }

        List<MemberManagement> memberManagements=memberManagementService.getByPatientId(patientId);
        return ResponseEntity.ok(ResultUtil.successJson(memberManagements));
    }
}
