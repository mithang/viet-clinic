package com.geeke.stock.service;

import java.util.List;
import java.util.Map;

import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.stock.entity.DispensingReportEvt;
import com.geeke.stock.entity.DispensingReportTotalEvt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.stock.dao.DispensingDao;
import com.geeke.stock.entity.Dispensing;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;

/**
 * Chi tiết phát thuốcService
 * @author txl
 * @version 2022-08-11
 */
 
@Service("dispensingService")
@Transactional(readOnly = true)
public class DispensingService extends CrudService<DispensingDao, Dispensing>{

    public Page<DispensingReportEvt> reportList(SearchParams searchParams) {
        // Z2a: strip company_id body + ep session tenant (mau buildTenantPageRequest);
        // SQL DispensingDao.xml loc them theo PageRequest id/institution.
        PageRequest pageRequest = buildTenantPageRequest(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return paginate(
            () -> this.dao.reportListCount(pageRequest),
            () -> this.dao.reportList(pageRequest)
        );
    }

    public DispensingReportTotalEvt reportAmount(SearchParams searchParams) {
        // Z2a: nhu reportList.
        PageRequest pageRequest = buildTenantPageRequest(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return this.dao.reportAmount(pageRequest);
    }

    public void updateDelFlag(String recipelInfoId) {
        this.dao.updateDelFlag(recipelInfoId);
    }

    @Transactional(readOnly = true)
    public Page<DispensingReportEvt> getOrganizationList(SearchParams searchParams) {
        // Z2a: nhu reportList.
        PageRequest pageRequest = buildTenantPageRequest(searchParams.getParams(), searchParams.getOffset(), searchParams.getLimit(), searchParams.getOrderby());
        return paginate(
            () -> this.dao.getOrganizationListCount(pageRequest),
            () -> this.dao.getOrganizationList(pageRequest)
        );
    }
}