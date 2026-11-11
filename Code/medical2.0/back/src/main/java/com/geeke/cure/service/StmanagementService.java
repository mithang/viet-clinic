package com.geeke.cure.service;

import com.geeke.common.data.Page;
import com.geeke.common.service.ServiceException;
import com.geeke.cure.dao.StmanagementDao;
import com.geeke.cure.entity.Stmanagement;
import com.geeke.cure.entity.Stparameter;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service("stmanagementService")
@Transactional(readOnly = true)
public class StmanagementService {

    @Autowired
    private StmanagementDao managementdao;

    public Page<Stmanagement> stmanagementlist(Stparameter stparameter){
        // Z1b: SQL loc a.company_id=#{companyId} truc tiep tu body — ghi
        // de bang session tenant; session rong/"null" giu hanh vi cu.
        forceSessionTenant(stparameter);
        // DS201: escape LIKE wildcards in hzxm/kdys (mirror DS200) —
        // "%" must match literally; same bean feeds total + list.
        stparameter.setHzxm(StringUtils.escapeLike(stparameter.getHzxm()));
        stparameter.setKdys(StringUtils.escapeLike(stparameter.getKdys()));
        List<Stmanagement> list=managementdao.stmanagementtotal(stparameter);
        int total=list.size();
        List<Stmanagement> list2=null;
        if(total>0){
            list2=managementdao.stmanagementlist(stparameter);
        }
        return  new Page<>((long) total,list2);
    }

    @Transactional(readOnly = false)
    public int updatestresult(Stparameter stparameter){
        // Q1 (iteration 40): ep session tenant cho duong ghi, nhu
        // stmanagementlist/selectdetailid — companyId la bi ghi de bang
        // session tenant nen WHERE khong khop dong la.
        forceSessionTenant(stparameter);
        // DS117: detailid la thi WHERE khong khop dong nao -> 200 rows
        // 0 silent; detailid khong ton tai cung vay. Ca hai 400.
        if (StringUtils.isBlank(stparameter.getDetailid())) {
            throw new ServiceException("Chi tiết đơn thuốc không được để trống");
        }
        stparameter.setZxsj(new Date());
        int rows = managementdao.updatestresult(stparameter);
        if (rows == 0) {
            throw new ServiceException("Chi tiết đơn thuốc không tồn tại");
        }
        return rows;
    }
    @Transactional(readOnly = false)
    public int updatesttime(Stparameter stparameter){
        // Q1 (iteration 40): nhu updatestresult.
        forceSessionTenant(stparameter);
        // DS117: nhu updatestresult.
        if (StringUtils.isBlank(stparameter.getDetailid())) {
            throw new ServiceException("Chi tiết đơn thuốc không được để trống");
        }
        int rows = managementdao.updatesttime(stparameter);
        if (rows == 0) {
            throw new ServiceException("Chi tiết đơn thuốc không tồn tại");
        }
        return rows;
    }
    public List<Stmanagement> selectdetailid(Stparameter stparameter){
        forceSessionTenant(stparameter);
        return managementdao.selectdetailid(stparameter);
    }

    // Z1b: dung chung cho stmanagementlist/selectdetailid (xem ghi chu o
    // stmanagementlist); tu Q1 (iteration 40) cung ap cho
    // updatestresult/updatesttime — duong ghi truoc day de nhip Z3.
    // DS117: null param — framework chan JSON-null o tang controller
    // nhung service tu ve cho caller truc tiep (truoc day NPE 500).
    private void forceSessionTenant(Stparameter stparameter) {
        if (stparameter == null) {
            throw new ServiceException("Tham số truy vấn không được để trống");
        }
        String tenant = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (tenant != null && !tenant.isEmpty() && !"null".equals(tenant)) {
            stparameter.setCompanyId(tenant);
        }
    }
}
