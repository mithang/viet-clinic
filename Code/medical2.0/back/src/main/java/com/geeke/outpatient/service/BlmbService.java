package com.geeke.outpatient.service;

import com.geeke.common.data.Page;
import com.geeke.outpatient.dao.BlmbDao;
import com.geeke.outpatient.entity.Blmb;
import com.geeke.outpatient.entity.Blmcxrc;
import com.geeke.utils.IdGen;
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
        blmcxrc.setUpdatedTime(new Date());
        return blmbdao.deletembbm(blmcxrc);
    }

    public List<Blmb> selectmbbm(Blmcxrc blmcxrc){
        forceSessionTenant(blmcxrc);
        return blmbdao.selectmbbm(blmcxrc);
    }

    // Z1b: dung chung cho getblmblist/selectmbbm (xem ghi chu o getblmblist).
    // Khong ap cho save/deletembbm/updatembbm — duong ghi, de nhip Z3.
    private void forceSessionTenant(Blmcxrc blmcxrc) {
        String tenant = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (tenant != null && !tenant.isEmpty() && !"null".equals(tenant)) {
            blmcxrc.setCompanyId(tenant);
        }
    }
    @Transactional(readOnly = false)
    public int updatembbm(Blmb blmb){
        blmb.setUpdatedTime(new Date());
        return blmbdao.updatembbm(blmb);
    }
}
