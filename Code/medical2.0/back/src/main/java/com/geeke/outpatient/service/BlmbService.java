package com.geeke.outpatient.service;

import com.geeke.common.data.Page;
import com.geeke.common.service.ServiceException;
import com.geeke.outpatient.dao.BlmbDao;
import com.geeke.outpatient.entity.Blmb;
import com.geeke.outpatient.entity.Blmcxrc;
import com.geeke.utils.IdGen;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service("blmbService")
@Transactional(readOnly = true)
public class BlmbService {

    @Autowired
    private BlmbDao blmbdao;

    /**
     * Thêmbệnh ánmẫuDữ liệu
     * @param blmb
     * @return
     */
    @Transactional(readOnly = false)
    public int insert(Blmb blmb){
        // DS117: null body — framework chan JSON-null o tang controller
        // ("Required request body is missing" -> 400) nhung service tu
        // ve cho caller truc tiep: truoc day blmb.getMbmc() NPE 500.
        if (blmb == null) {
            throw new ServiceException("Mẫu bệnh án không được để trống");
        }
        // DS82: direct-DAO insert into casehistorymbgl; mbmc/mblx/bllx/
        // CREATED_BY/CREATED_ID/del_flag are all NOT NULL but the service
        // set only mbbm + createdTime, so a body missing any of the rest
        // 500d. No live row carries a blank in any of them, so blank is
        // rejected for the three form fields; createdBy/createdID/delFlag
        // are system-populated (the real form sends the session user +
        // '0'), so blank ones are filled here instead of 400ing — same
        // values the form computes, never overriding a caller value.
        if (StringUtils.isBlank(blmb.getMbmc())) {
            throw new ServiceException("Tên mẫu không được để trống");
        }
        if (StringUtils.isBlank(blmb.getMblx())) {
            throw new ServiceException("Loại mẫu không được để trống");
        }
        if (StringUtils.isBlank(blmb.getBllx())) {
            throw new ServiceException("Loại bệnh án không được để trống");
        }
        if (StringUtils.isBlank(blmb.getCreatedBy()) || StringUtils.isBlank(blmb.getCreatedID())) {
            com.geeke.admin.entity.User user = SessionUtils.getUser();
            if (user == null || StringUtils.isBlank(user.getId()) || StringUtils.isBlank(user.getName())) {
                throw new ServiceException("Người tạo không được để trống");
            }
            if (StringUtils.isBlank(blmb.getCreatedBy())) {
                blmb.setCreatedBy(user.getName() + "(" + user.getLoginName() + ")");
            }
            if (StringUtils.isBlank(blmb.getCreatedID())) {
                blmb.setCreatedID(user.getId());
            }
        }
        if (StringUtils.isBlank(blmb.getDelFlag())) {
            blmb.setDelFlag("0");
        }
        // Q3 (iteration 40): ep session tenant cho duong ghi, nhu
        // getblmblist/selectmbbm — companyId la bi ghi de bang session
        // tenant nen khong chen duoc mau vao tenant la.
        forceSessionTenant(blmb);
        blmb.setMbbm(IdGen.uuid());
        blmb.setCreatedTime(new Date());
        return blmbdao.insert(blmb);
    }

    public Page<Blmb> getblmblist(Blmcxrc blmcxrc){
        // Z1b: SQL loc a.company_id=#{companyId} truc tiep tu body — body
        // tenant la truoc day tra ve mau benh an cua tenant do. Ghi de
        // bang session tenant; session rong/"null" giu hanh vi cu.
        forceSessionTenant(blmcxrc);
        List<Blmb> listtotal=blmbdao.getblmblisttotal(blmcxrc);
        int total=listtotal.size();
        List<Blmb> list=null;
        if(total>0){
            list=blmbdao.getblmblist(blmcxrc);
        }
        return new Page<>((long) total,list);
    }

    @Transactional(readOnly = false)
    public int deletembbm(Blmcxrc blmcxrc){
        // Q3 (iteration 40): nhu insert — companyId la bi ghi de bang
        // session tenant nen WHERE khong khop dong la.
        forceSessionTenant(blmcxrc);
        blmcxrc.setUpdatedTime(new Date());
        return blmbdao.deletembbm(blmcxrc);
    }

    public List<Blmb> selectmbbm(Blmcxrc blmcxrc){
        forceSessionTenant(blmcxrc);
        return blmbdao.selectmbbm(blmcxrc);
    }

    // Z1b: dung chung cho getblmblist/selectmbbm (xem ghi chu o getblmblist);
    // tu Q3 (iteration 40) cung ap cho save/deletembbm/updatembbm —
    // duong ghi truoc day de nhip Z3.
    // DS117: null param — framework chan JSON-null o tang controller
    // nhung service tu ve cho caller truc tiep (truoc day NPE 500).
    private void forceSessionTenant(Blmcxrc blmcxrc) {
        if (blmcxrc == null) {
            throw new ServiceException("Tham số truy vấn không được để trống");
        }
        String tenant = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (tenant != null && !tenant.isEmpty() && !"null".equals(tenant)) {
            blmcxrc.setCompanyId(tenant);
        }
    }
    // Q3: ban nang cho Blmb (save/updatembbm) — cung quy tac session
    // rong/"null" giu hanh vi cu nhu ban Blmcxrc.
    private void forceSessionTenant(Blmb blmb) {
        if (blmb == null) {
            throw new ServiceException("Mẫu bệnh án không được để trống");
        }
        String tenant = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (tenant != null && !tenant.isEmpty() && !"null".equals(tenant)) {
            blmb.setCompanyId(tenant);
        }
    }
    @Transactional(readOnly = false)
    public int updatembbm(Blmb blmb){
        // Q3 (iteration 40): nhu deletembbm — companyId la bi ghi de
        // bang session tenant nen WHERE khong khop dong la.
        forceSessionTenant(blmb);
        // DS117: mapper SET tat ca cac cot nen mblx/bllx bi bo se NULL
        // hoa cot NOT NULL -> 500; mbbm la thi WHERE khong khop dong
        // nao -> 200 rows 0 silent. Ca hai deu 400 fail-closed.
        if (StringUtils.isBlank(blmb.getMbbm())) {
            throw new ServiceException("Mã mẫu không được để trống");
        }
        if (StringUtils.isBlank(blmb.getMbmc())) {
            throw new ServiceException("Tên mẫu không được để trống");
        }
        if (StringUtils.isBlank(blmb.getMblx())) {
            throw new ServiceException("Loại mẫu không được để trống");
        }
        if (StringUtils.isBlank(blmb.getBllx())) {
            throw new ServiceException("Loại bệnh án không được để trống");
        }
        blmb.setUpdatedTime(new Date());
        int rows = blmbdao.updatembbm(blmb);
        if (rows == 0) {
            throw new ServiceException("Mẫu bệnh án không tồn tại");
        }
        return rows;
    }
}
