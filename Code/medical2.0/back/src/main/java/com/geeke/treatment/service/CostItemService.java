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

    /**
     * DS146 A: @Length tren entity (itemCode/itemName/isUse/isDeduct/
     * itemCountryCode/pinyinCode) + remarks theo gioi han cot DB 255 (mau
     * DS141: truong khong @Length thi guard o gioi han DB). Generic /save
     * co @Valid o CrudController, nhung /saveDto goi thang service.save()
     * va bulk* bo qua ca save() lan @Valid nen gia tri qua dai 500 o DB
     * (probe A3/A4: itemName 65). Guard o day dung chung cho ca 3 duong.
     */
    private static void checkCostItemLengths(CostItem costItem) {
        if (costItem == null) {
            return;
        }
        checkLength(costItem.getItemCode(), 64, "hạng mục Độ dài mã phải nằm trong khoảng 0 đến 64");
        checkLength(costItem.getItemName(), 64, "hạng mục Tên Độ dài phải nằm trong khoảng 1 đến 64");
        checkLength(costItem.getIsUse(), 2, "Độ dài trạng thái phải nằm trong khoảng 0 đến 2 ");
        checkLength(costItem.getIsDeduct(), 2, "Độ dài có thực hiện khấu trừ phải nằm trong khoảng 0 đến 2 ");
        checkLength(costItem.getItemCountryCode(), 64, "quốc gia Độ dài mã phải nằm trong khoảng 0 đến 64");
        checkLength(costItem.getPinyinCode(), 64, "Độ dài phải nằm trong khoảng 1 đến 64 ");
        checkLength(costItem.getRemarks(), 255, "Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
        // DS147 B: gia tri DictItem unit/itemType viet thang vao cot varchar(64)
        // (mapper #{entity.unit.value} / #{entity.itemType.value}) -- @Length(128)
        // cua DictItem chi guard bang dict, khong guard cot dich (probe B2/B3:
        // unit/itemType value 65 deu 500). Null-safe: DictItem null hoac value
        // null thi bo qua nhu cu (mapper viet NULL).
        checkDictLength(costItem.getUnit(), 64, "Đơn vị Độ dài phải nằm trong khoảng 0 đến 64");
        checkDictLength(costItem.getItemType(), 64, "Loại hạng mục Độ dài phải nằm trong khoảng 0 đến 64");
    }

    private static void checkDictLength(com.geeke.sys.entity.DictItem item, int max, String message) {
        if (item != null) {
            checkLength(item.getValue(), max, message);
        }
    }

    private static void checkLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ServiceException(message);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public CostItem save(CostItem costItem) {
        // DS146 A: chan do dai truoc moi xu ly (ca duong /saveDto khong @Valid).
        checkCostItemLengths(costItem);
        // DS78: is_package is NOT NULL DEFAULT '0', but the insert
        // lists the column unconditionally, so a generic /save body
        // without isPackage 500s ("Column 'is_package' cannot be
        // null") instead of taking the default. The real UI path
        // (/saveDto) always sets isPackage, so this only repairs the
        // generic /save + API-client path; default here mirrors the
        // DB default and saveDto's single-item "0".
        if (StringUtils.isBlank(costItem.getIsPackage())) {
            costItem.setIsPackage("0");
        }
        // Khi thêm mới, Xử lý trường tự động đánh số
        if (StringUtils.isBlank(costItem.getId())){
            String sn = sequenceService.generate(SessionUtils.getLoginTenantId(), "treatment_item_code", costItem);
            costItem.setItemCode(sn);
        }
        String id = this.dao.GetIDByName(costItem.getItemName(),SessionUtils.getLoginTenantId());
        if(null!=id && !id.equals(costItem.getId()))
        {
            throw new ServiceException("Đã Tồn tại một Cái giống hệt của hạng mục tên, Vui lòng nhập lại!");
        }
        CostItem costItemTemp = super.save(costItem);
        return costItemTemp;
    }

    // DS125 A: single delete fail-closed 400 (mau DS123/DS124) -- truoc day
    // delete id ma / dong da xoa mem deu 200 silent (CrudService.delete chi
    // chan qua stored-row tenant check ma check do bo qua stored==null;
    // mapper delete khong loc del_flag nen double-delete tra 200 rows=1).
    // Kiem tra ton tai + con live (del_flag='0') truoc moi xoa; khong co
    // caller noi bo nao goi delete() cua service nay.
    @Override
    @Transactional(readOnly = false)
    public int delete(CostItem costItem) {
        if (costItem == null || StringUtils.isBlank(costItem.getId())) {
            throw new ServiceException("Không xác định được hạng mục cần xóa.");
        }
        CostItem stored = dao.get(costItem.getId());
        if (stored == null || !"0".equals(stored.getDelFlag())) {
            throw new ServiceException("Hạng mục không tồn tại: " + costItem.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(costItem);
        return super.delete(costItem);
    }

    /**
     * DS145 A: CrudController bulk routes goi thang CrudService.bulkInsert/
     * bulkUpdate, bo qua moi guard cua save(): bulkInsert thieu itemName
     * 500 NOT NULL (probe A2), bulkUpdate id ma 200 im lang (probe A3),
     * bulkUpdate itemName null 500 (probe A4), bulkInsert 2 ten trung nhau
     * 200 + 2 dong (probe A6 -- save() chan trung ten qua GetIDByName).
     * Ghi de theo mau DS99/DS108: null/rong -> 400, entry null -> 400,
     * truong bat buoc theo @NotNull entity (company/itemName/pinyinCode)
     * -> 400, id ma / dong da xoa mem o bulkUpdate -> 400, trung ten
     * (trong list + voi dong da luu, dung GetIDByName nhu save()) -> 400.
     * Mirror save(): isPackage trong -> '0', itemCode trong khi tao moi
     * -> sinh qua sequence. bulkDelete ghost giu 200 rows 0 theo parity
     * DS99 G11/DS126 B (probe A5).
     */
    private void checkCostItemRequired(CostItem costItem) {
        if (costItem == null) {
            throw new ServiceException("Hạng mục không được để trống");
        }
        if (costItem.getCompany() == null || StringUtils.isBlank(costItem.getCompany().getId())) {
            throw new ServiceException("Phòng khám id Không được để trống");
        }
        if (StringUtils.isBlank(costItem.getItemName())) {
            throw new ServiceException("Hạng mục tên không được để trống");
        }
        if (StringUtils.isBlank(costItem.getPinyinCode())) {
            throw new ServiceException("Mã pinyin không được để trống");
        }
    }

    private void checkCostItemDuplicate(CostItem costItem, java.util.Set<String> namesInList) {
        if (!namesInList.add(costItem.getItemName())) {
            throw new ServiceException("Đã Tồn tại một Cái giống hệt của hạng mục tên, Vui lòng nhập lại!");
        }
        String dupId = this.dao.GetIDByName(costItem.getItemName(), SessionUtils.getLoginTenantId());
        if (dupId != null && !dupId.equals(costItem.getId())) {
            throw new ServiceException("Đã Tồn tại một Cái giống hệt của hạng mục tên, Vui lòng nhập lại!");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<CostItem> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách hạng mục thêm mới không được để trống");
        }
        java.util.Set<String> namesInList = new java.util.HashSet<>();
        for (CostItem costItem : entitys) {
            checkCostItemRequired(costItem);
            // DS146 A: bulk cung chan do dai nhu save().
            checkCostItemLengths(costItem);
            if (StringUtils.isBlank(costItem.getIsPackage())) {
                costItem.setIsPackage("0");
            }
            if (StringUtils.isBlank(costItem.getId()) && StringUtils.isBlank(costItem.getItemCode())) {
                costItem.setItemCode(sequenceService.generate(SessionUtils.getLoginTenantId(), "treatment_item_code", costItem));
            }
            checkCostItemDuplicate(costItem, namesInList);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<CostItem> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách hạng mục cập nhật không được để trống");
        }
        java.util.Set<String> namesInList = new java.util.HashSet<>();
        for (CostItem costItem : entitys) {
            if (costItem == null) {
                throw new ServiceException("Hạng mục không được để trống");
            }
            if (StringUtils.isBlank(costItem.getId())) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của hạng mục đã tồn tại");
            }
            CostItem stored = dao.get(costItem.getId());
            if (stored == null || !"0".equals(stored.getDelFlag())) {
                throw new ServiceException("Hạng mục không tồn tại: " + costItem.getId());
            }
            checkCostItemRequired(costItem);
            // DS146 A: bulk cung chan do dai nhu save().
            checkCostItemLengths(costItem);
            if (StringUtils.isBlank(costItem.getItemCode())) {
                throw new ServiceException("Mã hạng mục không được để trống");
            }
            if (StringUtils.isBlank(costItem.getIsPackage())) {
                costItem.setIsPackage("0");
            }
            checkCostItemDuplicate(costItem, namesInList);
        }
        return super.bulkUpdate(entitys);
    }

    @Transactional(readOnly = true)
    public List<MedicalProject> listPageForMedical(List<Parameter> parameters, String orderby) {
        // ITER65 item 4: tenant guard kieu W2 cho duong listForMedical (probe-proven:
        // params rong tra 26 dong moi tenant, company=F1 tra dung 1 dong la).
        // Phai chat hon listPage/listAll: ensureCompanyFilter return som khi null,
        // ma PageRequest bien null thanh list rong -> params-null hien ro toan bo
        // tenant, nen null phai thanh list rong TRUOC khi goi filter.
        // KHONG ngoai le institution rieng: listPage anh em cung chi ep session
        // tenant; hang institution lay qua listByInstitution (khong doi).
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        ensureCompanyFilter(parameters);
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