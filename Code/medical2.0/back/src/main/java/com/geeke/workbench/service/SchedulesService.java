package com.geeke.workbench.service;

import com.geeke.common.data.Page;
import com.geeke.common.service.ServiceException;
import com.geeke.outpatient.entity.PageRegistration;
import com.geeke.outpatient.entity.Registration;
import com.geeke.workbench.dao.SchedulesDao;
import com.geeke.workbench.entity.VisitProgress;
import com.geeke.workbench.entity.Visitprogresspara;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service("schedulesService")
@Transactional(readOnly = true)
public  class SchedulesService  {
    @Autowired
    private SchedulesDao schedulesDao;

    // Z1b: ghi de companyId body bang session tenant — SQL listPages/
    // listPagejyjc/visitprogresspage deu loc company_id=#{companyId}
    // truc tiep tu body. Session rong/"null" giu hanh vi cu.
    private void forceSessionTenant(PageRegistration pageRegistration) {
        String tenant = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (tenant != null && !tenant.isEmpty() && !"null".equals(tenant)) {
            pageRegistration.setCompanyId(tenant);
        }
    }

    private void forceSessionTenant(Visitprogresspara visitprogresspara) {
        String tenant = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (tenant != null && !tenant.isEmpty() && !"null".equals(tenant)) {
            visitprogresspara.setCompanyId(tenant);
        }
    }

    @Transactional(readOnly = false)
    public int updateoverlookid(String strRecipelInfoIds){
        // Z1b: ghi overlook theo id tran — phai thuoc session tenant moi
        // duoc ghi, mau X1 (id khong ton tai -> company null -> cho qua
        // nhu cu, update tra 0; tenant la -> ServiceException -> 400
        // fail-closed, khong ghi). Luong updateoverlookidlist chi dua id
        // tu list da ep tenant nen khong cham nhanh nay khi chay dung.
        String tenant = com.geeke.utils.SessionUtils.getLoginTenantId();
        if (tenant != null && !tenant.isEmpty() && !"null".equals(tenant)) {
            String companyId = schedulesDao.getRegistrationCompanyId(strRecipelInfoIds);
            if (companyId != null && !companyId.isEmpty() && !tenant.equals(companyId)) {
                throw new ServiceException("Không có quyền truy cập tenant khác");
            }
        }
        return schedulesDao.updateoverlookid(strRecipelInfoIds);
    }

        public Page<VisitProgress> visitProgressPage(Visitprogresspara visitprogresspara){
        forceSessionTenant(visitprogresspara);
        List<VisitProgress> list = schedulesDao.visitprogresspagecounts(visitprogresspara);
        int total=list.size();
            List<VisitProgress> list2 = null;
            if(total>0){
                list2 = schedulesDao.visitprogresspage(visitprogresspara);
            }
        return new Page<>((long)total, list2);
    }

    public List<String> listPages(PageRegistration pageRegistration){
        forceSessionTenant(pageRegistration);
        // DS198: columnName tu body di thang vao ORDER BY ${columnName}
        // o SchedulesDao.listPages — khong qua whitelist nhu
        // RegistrationService.listPages/wxListPages/wxDispensingListPages
        // (da check isValidColumnName tu truoc). Bat ky user nao dang
        // nhap cung chen duoc bieu thuc SQL vao ORDER BY qua POST
        // /workbench/Schedules/updateoverlookidlist (probe ds198 pre:
        // cot khong ton tai -> 400 generic BadSqlGrammar; '(SELECT 1)'
        // chap nhan -> 200; SLEEP(3) danh gia that -> 200 sau 3.01s).
        // Blank giu nguyen (UI mac dinh "" va mapper bo ORDER BY khi
        // rong); chi chan gia tri co noi dung ma khong hop le, fail-closed
        // 400 (IllegalArgumentException -> GlobalExceptionHandler).
        // listPagesjyjc khong dung columnName (khong co ORDER BY) nen
        // khong can check o day.
        String columnName = pageRegistration.getColumnName();
        if (com.geeke.utils.StringUtils.isNotBlank(columnName)
                && !pageRegistration.isValidColumnName()) {
            throw new IllegalArgumentException("Invalid column name: " + columnName);
        }
        List<String> list = schedulesDao.listPages(pageRegistration);
        int total=list.size();
        return  list;
    }
    public List<String> listPagesjyjc(PageRegistration pageRegistration){
        forceSessionTenant(pageRegistration);
        List<String> list = schedulesDao.listPagejyjc(pageRegistration);
        int total=list.size();
        return  list;
    }

}
