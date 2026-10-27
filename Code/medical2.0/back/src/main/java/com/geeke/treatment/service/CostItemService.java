package com.geeke.treatment.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.sequence.service.SequenceService;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.org.entity.Company;
import com.geeke.outpatient.entity.RecipelDetail;
import com.geeke.outpatient.entity.RecipelInfo;
import com.geeke.outpatient.service.RecipelDetailService;
import com.geeke.stock.entity.MedicalProject;
import com.geeke.sys.utils.SessionUserDto;
import com.geeke.treatment.dao.CostItemDao;
import com.geeke.treatment.entity.CostItem;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;


import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Hạng mục khám chữa bệnhService
 * @author txl
 * @version 2022-05-30
 */
 
@Service("costItemService")
public class CostItemService extends CrudService<CostItemDao, CostItem>{
    @Autowired
    SequenceService sequenceService;

    /** Chi phíhạng mục là Cơ sởtừ điển dùng chung: Cho phép phòng khám thuộc cùng cơ sở truy cập dữ liệu phòng khám của cơ sở(Với danh sách #{institution} SQL Nhất quán) */
    @Override
    protected boolean isInstitutionShared() {
        return true;
    }
    @Autowired
    RecipelDetailService recipelDetailService;

    @Override
    @Transactional(readOnly = false)
    public CostItem save(CostItem costItem) {
        // Khi thêm mới, Xử lý trường tự động đánh số
        if (StringUtils.isBlank(costItem.getId())){
            String sn = sequenceService.generate(SessionUtils.getLoginTenantId(), "treatment_item_code", costItem);
            costItem.setItemCode(sn);
        }
        String id = this.dao.GetIDByName(costItem.getItemName(),SessionUtils.getLoginTenantId());
        if(null!=id && !id.equals(costItem.getId()))
        {
            throw new ServiceException("ĐãTồn tạimộtCáigiống hệt của hạng mụctên, Vui lòng nhập lại!");
        }
        CostItem costItemTemp = super.save(costItem);
        return costItemTemp;
    }

    @Transactional(readOnly = true)
    public List<MedicalProject> listPageForMedical(List<Parameter> parameters, String orderby) {
        PageRequest pageRequest = new PageRequest(parameters, orderby);
        int total = this.dao.count(pageRequest);
        List<CostItem> lis = null;
        List<MedicalProject> list = new ArrayList<>();
        if (total > 0) {
            lis = this.dao.listAll(pageRequest);
            lis.stream().forEach(costItem -> {
                costItem.setName(costItem.getItemName());
                MedicalProject medicalProject = new MedicalProject();
                BeanUtils.copyProperties(costItem,medicalProject);
                medicalProject.setIsUnpackSell("0");
                medicalProject.setStuffType("3");
                medicalProject.setDosisUnit(costItem.getUnit());
                medicalProject.setPack(costItem.getUnit());
                list.add(medicalProject);});
        }
        return list;
    }
    @Transactional(readOnly = true)
    public List<CostItem> getByRecipelInfo(RecipelInfo recipelInfo) {
        List<RecipelDetail> byRecipelInfoId = recipelDetailService.getByRecipelInfoId(recipelInfo.getId());
        List<CostItem> costItems=new ArrayList<>();
        for (RecipelDetail recipelDetail : byRecipelInfoId) {
            CostItem costItem=this.dao.getByRecipelInfo(recipelDetail.getId());

            if(!ObjectUtils.isEmpty(costItem)){
                costItem.setRecipelDetail(recipelDetail);
                costItems.add(costItem);
            }
        }

        return costItems;
    }

    public Page<CostItem> listByInstitution(List<Parameter> params, int offset, int limit, String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(params, offset, limit, orderby);
        int total = dao.countByInstitution(pageRequest);
        List<CostItem> list = null;
        if (total > 0) {
            list = dao.listByInstitution(pageRequest);
        }

        return new Page<>(total, list);
    }

    @Transactional
    public List<String> syncToClinic(List<CostItem> entitys) {
        SessionUserDto userDto = SessionUtils.getUserDto();
        SessionUserDto.CompanyDTO companyDto = userDto.getCompany();
        Company company = BeanUtil.copyProperties(companyDto, Company.class);

        List<String> list = new ArrayList<>();

        entitys.forEach(costItem -> {
            String oldId = costItem.getId();
            String id = this.dao.GetIDByName(costItem.getItemName(), SessionUtils.getLoginTenantId());
            if (ObjUtil.isNotNull(id)) {
                costItem.setCompany(company);
                costItem.setId(null);
                costItem.setSyncId(oldId);
                String sn = sequenceService.generate(SessionUtils.getLoginTenantId(), "treatment_item_code", costItem);
                costItem.setItemCode(sn);
                super.save(costItem);
                list.add(costItem.getId());
            }
        });
        return list;
    }
}