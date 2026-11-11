package com.geeke.stock.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.org.entity.Company;
import com.geeke.org.service.CompanyService;
import com.geeke.stock.entity.*;
import com.geeke.utils.IdGen;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.excel.ExcelExportBuilder;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import com.geeke.stock.dao.InventoryVerificationDetailDao;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;

/**
 * Chi tiết kiểm kê tồn khoService
 * @author Quản trị viên cấp cao
 * @version 2022-11-02
 */
 
@Service("inventoryVerificationDetailService")
@Transactional(readOnly = true)
public class InventoryVerificationDetailService extends CrudService<InventoryVerificationDetailDao, InventoryVerificationDetail>{

    /** kiểm kêChi tiết là Cơ sởTổng hưởngDữ liệu: Cho phép phòng khám thuộc cùng cơ sở truy cập dữ liệu phòng khám của cơ sở(Với danh sách #{institution} SQL Nhất quán) */
    @Override
    protected boolean isInstitutionShared() {
        return true;
    }


    // DS125 C: single delete truoc day de lo: ghost id / dong da xoa roi
    // van 200 silent no-op (verifyTenantOwnershipAgainstStoredRow bo qua
    // khi stored==null, cung mau DS123/DS124/DS125A). Bang KHONG CO cot
    // del_flag, delete la HARD DELETE nen chi can kiem tra ton tai qua
    // dao.get (mau DS124C InventoryVerification). Chan o service de 400
    // ro nghia truoc khi xoa. Khong caller noi bo nao goi delete() nay
    // (InventoryVerificationService.delete dung dao-level
    // deleteByInventoryVerificationId); endpoint co expose qua
    // CrudController + api export du UI khong co nut xoa. Xoa hang live
    // van 200 nhu cu.
    @Override
    @Transactional(readOnly = false)
    public int delete(InventoryVerificationDetail entity) {
        if (entity == null || StringUtils.isBlank(entity.getId())) {
            throw new com.geeke.common.service.ServiceException("Không xác định được chi tiết kiểm kê cần xóa.");
        }
        InventoryVerificationDetail stored = dao.get(entity.getId());
        if (stored == null) {
            throw new com.geeke.common.service.ServiceException("Chi tiết kiểm kê không tồn tại: " + entity.getId());
        }
        verifyTenantOwnershipAgainstStoredRow(entity);
        return super.delete(entity);
    }


    @Autowired
    private InventoryVerificationService inventoryVerificationService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private StuffService stuffService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private MedicinalStockControlService medicinalStockControlService;

    // DS191: overload listPage 5 doi so (params, offset, limit, orderby,
    // type) DA XOA — dead code, 0 caller (controller list/{type} goi
    // listPages; exportExcel goi super.listPage 4 doi so cua CrudService).
    // DAO listPage(pageRequest, type) 2 doi so cung da xoa theo. Mapper
    // <select id="listPage"> GIU LAI (bind CrudDao.listPage 1 doi so,
    // live qua exportExcel — xem comment trong XML). Chi giu listPages
    // (DS23, tenant-scoped) + listPage generic cua CrudService.
    // DS23: listPages dung buildTenantPageRequest + dao.listPages (cau lenh
    // listPages co san, gioi han theo co so) thay cho PageRequest tran +
    // dao.listPage khong pham vi — truoc day bat ky session nao co quyen
    // cung doc duoc chi tiet kiem ke cua tenant khac bang cach bo hoac doi
    // company_id do client gui (frontend gui company_id + phieu cha).
    // Parity DrugService.listPages / StuffService.listPages (isInstitutionShared).
    public Page<InventoryVerificationDetail> listPages(List<Parameter> parameters, int offset, int limit, String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(parameters, offset, limit, orderby);
        return paginate(() -> dao.count(pageRequest), () -> dao.listPages(pageRequest));
    }

    @Transactional(readOnly = false)
    public void save(InventoryVerification inventoryVerification, String variety, List<MedicinalStorageControl> medicinalStorageControls) {
        if(ObjectUtils.equals("0",variety)){
            //LưuThuốc
            for (MedicinalStorageControl medicinalStorageControl : medicinalStorageControls) {
                medicinalStorageControl.setInventoryVerification(inventoryVerification);
                medicinalStorageControl.setInventoryVerificationDetailId(IdGen.uuid());
                medicinalStorageControl.setStorageStock(medicinalStorageControl.getStorageStock().subtract((medicinalStorageControl.getUsedStock().add(medicinalStorageControl.getReimburseStock()))));
            }
            this.dao.saveDrug(medicinalStorageControls);
        }
    }

    @Transactional
    public String saveAll(List<List<InventoryVerificationDetail>> inventoryVerificationDetailDTOS) {
        int i=-1;
        if(!CollectionUtils.isEmpty(inventoryVerificationDetailDTOS)){
            // L1 (iteration 34): kiem tra chu so huu TRUOC KHI ghi, doi chieu theo
            // dong DB da luu chu khong theo company payload (id la + company OWN
            // van lot neu chi kiem tra payload). Dong la -> 400 + rollback toan bo.
            for (List<InventoryVerificationDetail> group : inventoryVerificationDetailDTOS) {
                if (CollectionUtils.isEmpty(group)) {
                    continue;
                }
                for (InventoryVerificationDetail detail : group) {
                    if (detail == null) {
                        continue;
                    }
                    verifyTenantOwnership(detail);
                    if (StringUtils.isNotBlank(detail.getId())) {
                        InventoryVerificationDetail dbRow = this.dao.getOwnership(detail.getId());
                        if (dbRow != null) {
                            verifyTenantOwnership(dbRow);
                            if (detail.getCompany() != null
                                    && StringUtils.isNotBlank(detail.getCompany().getId())
                                    && dbRow.getCompany() != null
                                    && !detail.getCompany().getId().equals(dbRow.getCompany().getId())) {
                                throw new com.geeke.common.service.ServiceException("Không có quyền truy cập tenant khác Dữ liệu");
                            }
                        }
                    }
                }
            }
            // O2-37 (iteration 37): kiem tra MOI phieu cha xuat hien trong payload,
            // theo dong DB cua phieu, voi cung ngu nghia institution-shared nhu L1.
            // L1 chi xet phan tu dau cua nhom khong rong dau tien, trong khi
            // totalParentId lay detail dau tien CO phieu trong toan bo payload va
            // bulkUpdate ghi inventory_verification_id theo tung detail, nen
            // payload [detail own mang phieu own, detail own mang phieu la] van
            // lot: detail sau bi re-parent sang phieu la (200). Phieu khong ton
            // tai (getRaw null) van cho qua nhu ngu nghia L1 cu.
            for (List<InventoryVerificationDetail> group : inventoryVerificationDetailDTOS) {
                if (CollectionUtils.isEmpty(group)) {
                    continue;
                }
                for (InventoryVerificationDetail detail : group) {
                    if (detail == null || detail.getInventoryVerification() == null
                            || StringUtils.isBlank(detail.getInventoryVerification().getId())) {
                        continue;
                    }
                    InventoryVerification parent = inventoryVerificationService.getRaw(detail.getInventoryVerification().getId());
                    if (parent != null) {
                        verifyTenantOwnership(parent);
                    }
                }
            }
            // O5-37 (iteration 37): moi detail can ghi BAT BUOC mang phieu cha
            // va company, neu khong 400 truoc khi ghi. Ly do: bulkUpdate ghi
            // inventory_verification_id/company_id theo payload, nen detail
            // thieu phieu/company se bi ghi NULL; hien tai chi fail-closed
            // nho NOT NULL + STRICT_TRANS_TABLES (500 xau + rollback, da
            // chung minh o repro O5), mong manh neu sql_mode doi. UI that
            // khong bi anh huong: hang UI luon lay tu listPage loc theo
            // phieu + company nen luon mang ca hai id (cot listPage
            // company.id / inventoryVerification.id, ca hai NOT NULL).
            // Chi caller la form kiem ke Vue, khong co luong tao detail
            // tay khong phieu/company. Null trong nhom van duoc bo qua
            // nhu L2 (khong phai detail can ghi).
            for (List<InventoryVerificationDetail> group : inventoryVerificationDetailDTOS) {
                if (CollectionUtils.isEmpty(group)) {
                    continue;
                }
                for (InventoryVerificationDetail detail : group) {
                    if (detail == null) {
                        continue;
                    }
                    if (detail.getInventoryVerification() == null
                            || StringUtils.isBlank(detail.getInventoryVerification().getId())
                            || detail.getCompany() == null
                            || StringUtils.isBlank(detail.getCompany().getId())) {
                        throw new com.geeke.common.service.ServiceException("Dòng chi tiết thiếu phiếu kiểm kê hoặc phòng khám");
                    }
                }
            }
            // L2 (iteration 34): chong crash ho get(0)/null o duong ghi:
            // - loc phan tu null truoc khi preUpdate/bulkUpdate (null trong
            //   nhom truoc day NPE, rollback ca save);
            // - gia lai/lo null tinh nhu 0 thay vi NPE (duong ghi bi chan oan);
            // - tong tien lay theo detail dau tien CO phieu cha trong toan bo
            //   payload, khong phai DTOS.get(0).get(0) (nhom dau rong hoac
            //   phan tu dau null truoc day IndexOutOfBounds/NPE);
            // - neu khong detail nao mang phieu cha thi 400 truoc khi ghi
            //   (khong the xac dinh phieu nao nhan tong tien, fail-closed).
            String totalParentId = null;
            boolean hasWritableDetail = false;
            for (List<InventoryVerificationDetail> group : inventoryVerificationDetailDTOS) {
                if (CollectionUtils.isEmpty(group)) {
                    continue;
                }
                for (InventoryVerificationDetail detail : group) {
                    if (detail == null) {
                        continue;
                    }
                    hasWritableDetail = true;
                    if (totalParentId == null && detail.getInventoryVerification() != null
                            && StringUtils.isNotBlank(detail.getInventoryVerification().getId())) {
                        totalParentId = detail.getInventoryVerification().getId();
                    }
                }
            }
            if (hasWritableDetail && totalParentId == null) {
                throw new com.geeke.common.service.ServiceException("Thiếu phiếu kiểm kê của dòng chi tiết");
            }
            // DS65: khoa phieu cha TRUOC bulkUpdate (xem lockById): tuan tu
            // hoa cac saveAll cung phieu. Khong khoa o day thi 2 saveAll
            // song song giu khoa X tren cac dong detail khac nhau roi cung
            // cho SUM ... FOR UPDATE -> deadlock; khoa phieu truoc thi
            // luong sau doi den khi luong truoc commit roi moi doc.
            if (totalParentId != null) {
                inventoryVerificationService.lockById(totalParentId);
            }
            for (List<InventoryVerificationDetail> inventoryVerificationDetailDTO : inventoryVerificationDetailDTOS) {
                if(!CollectionUtils.isEmpty(inventoryVerificationDetailDTO)){
                    List<InventoryVerificationDetail> rows = new ArrayList<>();
                    for (InventoryVerificationDetail inventoryVerificationDetail : inventoryVerificationDetailDTO) {
                        if (inventoryVerificationDetail != null) {
                            rows.add(inventoryVerificationDetail);
                        }
                    }
                    if (rows.isEmpty()) {
                        continue;
                    }
                    for (InventoryVerificationDetail inventoryVerificationDetail : rows) {
                        inventoryVerificationDetail.preUpdate();
                    }
                    i = this.dao.bulkUpdate(rows);

                }
            }
            // DS65: tong tien KHONG cong tu payload nua (payload cua UI that
            // chi chua cac trang da mo -> tong cu la tong cua mot phan
            // phieu; 2 luong song song sua 2 trang khac nhau thi luong
            // commit sau ghi de tong cua luong truoc). Tinh lai tu DB:
            // SUM ... FOR UPDATE tren toan bo detail cua phieu trong cung
            // transaction (current read, thay ca thay doi detail da commit
            // cua saveAll song song, va giu khoa den het transaction).
            //Sau khi thực thi vòng lặp thành công, lưu tổng số tiền vào bảng chính
            if(i>0 && totalParentId != null){
                BigDecimal dbAllPrice = this.dao.sumProfitAndLossPriceForUpdate(totalParentId);
                inventoryVerificationService.updateAllPrice(totalParentId, dbAllPrice == null ? BigDecimal.ZERO : dbAllPrice);
            }
            return "hành nghề dòng Thành công";
        }else {
            return "Dữ liệu là trống";
        }
    }

    @Transactional
    public void saveStuff(InventoryVerification inventoryVerification, String variety, List<MedicinalStorageControl> medicinalStorageControls) {
        if(ObjectUtils.equals("1",variety)){
            //LưuVật tư
            for (MedicinalStorageControl medicinalStorageControl : medicinalStorageControls) {
                medicinalStorageControl.setInventoryVerification(inventoryVerification);
                medicinalStorageControl.setInventoryVerificationDetailId(IdGen.uuid());
                medicinalStorageControl.setStorageStock(medicinalStorageControl.getStorageStock().subtract((medicinalStorageControl.getUsedStock().add(medicinalStorageControl.getReimburseStock()))));
            }
            this.dao.saveStuff(medicinalStorageControls);
        }
    }

    /*XuấtKiểm kê tồn kho*/
    @Transactional
    public void exportExcel(InventoryVerification inventoryVerification, HttpServletResponse response) throws IOException {
        // LấyDữ liệukhoDữ liệu
        List<Parameter> parameters = SearchParamsBuilder.create()
                .eq("inventory_verification_id", inventoryVerification.getId())
                .eq("company_id", inventoryVerification.getCompany().getId())
                .build();
        // DS203: listPage tran limit 10000 (PageRequest.MAX_PAGE_SIZE) nen
        // khong the lay het 10000000 dong mot lan nhu truoc — lay theo
        // trang 10000 dong roi gop lai; file xuat van du tat ca cac dong.
        List<InventoryVerificationDetail> rows = new ArrayList<>();
        int exportOffset = 0;
        while (true) {
            Page<InventoryVerificationDetail> inventoryVerificationDetailPage;
            if ("0".equals(inventoryVerification.getType())) {
                inventoryVerificationDetailPage = super.listPage(parameters, exportOffset, PageRequest.MAX_PAGE_SIZE, "");
            } else {
                inventoryVerificationDetailPage = super.listPage(parameters, exportOffset, PageRequest.MAX_PAGE_SIZE, "stuff");
            }
            if (CollectionUtils.isEmpty(inventoryVerificationDetailPage.getRows())) {
                break;
            }
            rows.addAll(inventoryVerificationDetailPage.getRows());
            if (inventoryVerificationDetailPage.getRows().size() < PageRequest.MAX_PAGE_SIZE) {
                break;
            }
            exportOffset += PageRequest.MAX_PAGE_SIZE;
        }
        for (InventoryVerificationDetail detail : rows) {
            if (!StringUtils.isNullOrEmpty(detail.getDrug())) {
                Drug drug = drugService.get(detail.getDrug().getId());
                detail.setDrug(drug);
            } else if (!StringUtils.isNullOrEmpty(detail.getStuff())) {
                // DS92: dòng không có cả drug lẫn stuff (dữ liệu cũ) thì
                // nhánh else cũ gọi getStuff().getId() -> NPE, vỡ toàn bộ
                // file xuất; để cả hai null, các cột thuốc/vật tư render
                // trống ở dưới.
                Stuff stuff = stuffService.get(detail.getStuff().getId());
                detail.setStuff(stuff);
            }
        }

        // trướcTínhcác dòngDữ liệu(drug/stuff phútchilogic, Quy cáchnối, Tồn khoTính v.v. )
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Map<String, Object>> exportData = new ArrayList<>();
        for (InventoryVerificationDetail detail : rows) {
            Map<String, Object> row = new LinkedHashMap<>();
            // DS92: dòng không có cả drug lẫn stuff, hoặc drug đã bị xóa
            // (get() trả null) thì nhánh else cũ deref stuff null -> NPE;
            // các cột thuốc/vật tư để trống thay vì vỡ file xuất.
            if (!StringUtils.isNullOrEmpty(detail.getDrug())) {
                Drug drug = detail.getDrug();
                row.put("code", drug.getCode());
                row.put("name", drug.getGoodsName());
                row.put("type", drug.getType() == null ? "" : drug.getType().getName());
                String spec = drug.getDosis() + (drug.getDosisUnit() == null ? "" : drug.getDosisUnit().getName()) + "/"
                        + drug.getPreparation() + (drug.getPreparationUnit() == null ? "" : drug.getPreparationUnit().getName()) + "/"
                        + (drug.getPack() == null ? "" : drug.getPack().getName());
                row.put("specification", spec);
                row.put("factory", drug.getFactory() == null ? "" : drug.getFactory().getName());
            } else if (!StringUtils.isNullOrEmpty(detail.getStuff())) {
                Stuff stuff = detail.getStuff();
                row.put("code", stuff.getCode());
                row.put("name", stuff.getName());
                row.put("type", stuff.getType() == null ? "" : stuff.getType().getName());
                row.put("specification", stuff.getSpecifications());
                row.put("factory", stuff.getFactory() == null ? "" : stuff.getFactory().getName());
            } else {
                row.put("code", "");
                row.put("name", "");
                row.put("type", "");
                row.put("specification", "");
                row.put("factory", "");
            }
            // DS92: supplierStock.endDate NULL (dòng không có phiếu nhập
            // tương ứng) thì sdf.format(null) -> NPE; nhà cung cấp/lô/
            // hạn để trống thay vì vỡ file xuất.
            SupplierStock ss = detail.getSupplierStock();
            row.put("supplier", ss == null || ss.getSupplierId() == null ? "" : ss.getSupplierId().getName());
            row.put("norms", ss == null ? "" : ss.getNorms());
            row.put("endDate", ss == null || ss.getEndDate() == null ? "" : sdf.format(ss.getEndDate()));
            row.put("currentInventory", getCurrentInventory(detail));
            row.put("checkInventory", getCheckInventory(detail));
            row.put("profitAndLoss", getProfitAndLoss(detail));
            row.put("remarks", detail.getRemarks());
            row.put("profitAndLossPrice", detail.getProfitAndLossPrice());
            exportData.add(row);
        }

        new ExcelExportBuilder("Kiểm kê tồn khobảng")
                .addColumns(
                        new ExcelExportBuilder.Column("Mã thuốc", "code"),
                        new ExcelExportBuilder.Column("Tên thuốc", "name"),
                        new ExcelExportBuilder.Column("Loại", "type"),
                        new ExcelExportBuilder.Column("Quy cách", "specification"),
                        new ExcelExportBuilder.Column("Nhà sản xuất", "factory"),
                        new ExcelExportBuilder.Column("Nhà cung cấp", "supplier"),
                        new ExcelExportBuilder.Column("Số lô", "norms"),
                        new ExcelExportBuilder.Column("Thời hạn hiệu lực", "endDate"),
                        new ExcelExportBuilder.Column("Hiện tại Số lượng tồn kho", "currentInventory"),
                        new ExcelExportBuilder.Column("Tồn kho kiểm kê", "checkInventory"),
                        new ExcelExportBuilder.Column("Lãi lỗ kiểm kê", "profitAndLoss"),
                        new ExcelExportBuilder.Column("Ghi chú", "remarks"),
                        new ExcelExportBuilder.Column("lãi lỗ Số tiền ( đồng)", "profitAndLossPrice")
                )
                .data(exportData)
                .write(response, "Kiểm kê tồn kho.xlsx");
    }

    // DS92: hệ số đổi đơn vị cho các cột số lượng đã đổi của file xuất
    // kiểm kê (drug.preparation / stuff.packNumber); null, không phải
    // số, <= 0, hoặc dòng không có cả drug lẫn stuff -> null, caller
    // trả số lượng gốc dạng chuỗi thay vì NPE/chia 0.
    private BigDecimal exportPreparation(InventoryVerificationDetail d) {
        try {
            BigDecimal p;
            if (!StringUtils.isNullOrEmpty(d.getDrug())) {
                if (d.getDrug().getPreparation() == null) { return null; }
                p = new BigDecimal(d.getDrug().getPreparation().toString());
            } else if (!StringUtils.isNullOrEmpty(d.getStuff())) {
                if (d.getStuff().getPackNumber() == null) { return null; }
                p = new BigDecimal(d.getStuff().getPackNumber().toString());
            } else {
                return null;
            }
            return p.compareTo(BigDecimal.ZERO) > 0 ? p : null;
        } catch (Exception e) {
            return null;
        }
    }

    // DS92: tên đơn vị [đơn vị đóng gói, đơn vị nhỏ nhất] cho cột số
    // lượng đã đổi; tên dict rỗng (mapper IFNULL) hay dòng không có cả
    // drug lẫn stuff đều trả "" chứ không null.
    private String[] exportUnitNames(InventoryVerificationDetail d) {
        String packName = "";
        String minName = "";
        if (!StringUtils.isNullOrEmpty(d.getDrug())) {
            if (d.getDrug().getPack() != null && d.getDrug().getPack().getName() != null) {
                packName = d.getDrug().getPack().getName();
            }
            if (d.getDrug().getPreparationUnit() != null && d.getDrug().getPreparationUnit().getName() != null) {
                minName = d.getDrug().getPreparationUnit().getName();
            }
        } else if (!StringUtils.isNullOrEmpty(d.getStuff())) {
            if (d.getStuff().getPackUnit() != null && d.getStuff().getPackUnit().getName() != null) {
                packName = d.getStuff().getPackUnit().getName();
            }
            if (d.getStuff().getMinUnit() != null && d.getStuff().getMinUnit().getName() != null) {
                minName = d.getStuff().getMinUnit().getName();
            }
        }
        return new String[]{packName, minName};
    }

    //TínhTồn kho hiện tại
    public String getCurrentInventory(InventoryVerificationDetail inventoryVerificationDetail){
        String inventory = "";
        // DS92: currentInventory NULL (cột nullable) -> toString() NPE,
        // vỡ toàn bộ file xuất; không có số lượng thì để trống.
        if (inventoryVerificationDetail.getCurrentInventory() == null) {
            return "";
        }
        BigDecimal currentInventory = new BigDecimal(inventoryVerificationDetail.getCurrentInventory().toString());
        BigDecimal preparation = exportPreparation(inventoryVerificationDetail);
        if (preparation == null) {
            return String.valueOf(inventoryVerificationDetail.getCurrentInventory());
        }
        BigDecimal divide = currentInventory.divide(preparation,BigDecimal.ROUND_CEILING);
        BigDecimal bigDecimal = divide.setScale(0, BigDecimal.ROUND_DOWN);
        BigDecimal subtract = divide.subtract(bigDecimal);
        int i = bigDecimal.intValue();
        int i1 = subtract.multiply(preparation).intValue();
        String[] units = exportUnitNames(inventoryVerificationDetail);
        if((i-i1)>0){
            inventory=(i+units[0])+(i1+units[1]);
            return inventory;
        }else {
            inventory=i+units[0];

            return inventory;
        }
    }

    //Tồn kho kiểm kê
    public String getCheckInventory(InventoryVerificationDetail inventoryVerificationDetail){
        String inventory = "";
        // DS92: checkInventory NULL -> toString() NPE, vỡ toàn bộ file xuất.
        if (inventoryVerificationDetail.getCheckInventory() == null) {
            return "";
        }
        BigDecimal currentInventory = new BigDecimal(inventoryVerificationDetail.getCheckInventory().toString());
        BigDecimal preparation = exportPreparation(inventoryVerificationDetail);
        if (preparation == null) {
            return String.valueOf(inventoryVerificationDetail.getCheckInventory());
        }

        BigDecimal divide = currentInventory.divide(preparation,BigDecimal.ROUND_CEILING);
        BigDecimal bigDecimal = divide.setScale(0, BigDecimal.ROUND_DOWN);
        BigDecimal subtract = divide.subtract(bigDecimal);
        int i = bigDecimal.intValue();
        int i1 = subtract.multiply(preparation).intValue();
        String[] units = exportUnitNames(inventoryVerificationDetail);
        if((i-i1)>0){
            inventory=(i+units[0])+(i1+units[1]);
            return inventory;
        }else {
            inventory=i+units[0];

            return inventory;
        }
    }

    //Lãi lỗ kiểm kê
    public String getProfitAndLoss(InventoryVerificationDetail inventoryVerificationDetail){
        String checkInventory="";
        // DS92: checkInventory/profitAndLoss NULL -> unbox/toString NPE,
        // vỡ toàn bộ file xuất; không có số liệu thì để trống.
        if (inventoryVerificationDetail.getCheckInventory() == null
                || inventoryVerificationDetail.getProfitAndLoss() == null) {
            return "";
        }
        if(inventoryVerificationDetail.getCheckInventory()>0){

            BigDecimal bigDecimal = new BigDecimal(inventoryVerificationDetail.getProfitAndLoss().toString());
            BigDecimal bigDecimal1 = exportPreparation(inventoryVerificationDetail);
            if (bigDecimal1 == null) {
                return String.valueOf(inventoryVerificationDetail.getProfitAndLoss());
            }
            BigDecimal divide = bigDecimal.divide(bigDecimal1,BigDecimal.ROUND_CEILING);
            BigDecimal bigDecimal2 = divide.setScale(0, BigDecimal.ROUND_DOWN);
            BigDecimal subtract = divide.subtract(bigDecimal2);
            int i = bigDecimal2.intValue();
            int i1 = subtract.multiply(bigDecimal1).intValue();
            String[] units = exportUnitNames(inventoryVerificationDetail);
            if(i1>0){
                checkInventory=(i+units[0])+(i1+units[1]);
              //  checkInventory=(i+inventoryVerificationDetail.getDrug().getPack().getName())+(i1+inventoryVerificationDetail.getDrug().getPreparationUnit().getName());
                return checkInventory;
            }else {
                checkInventory=i+units[0];
               // checkInventory=i+inventoryVerificationDetail.getDrug().getPack().getName();
                return checkInventory;
            }
        }else if(inventoryVerificationDetail.getCheckInventory()<0){
            //trướcChuyển đổi thànhđúngsố
            Integer i=~(inventoryVerificationDetail.getProfitAndLoss()-1);
            BigDecimal bigDecimal = new BigDecimal(i.toString());
           // BigDecimal bigDecimal1 = new BigDecimal(inventoryVerificationDetail.getDrug().getPreparation().toString());
            BigDecimal bigDecimal1 = exportPreparation(inventoryVerificationDetail);
            if (bigDecimal1 == null) {
                return String.valueOf(i);
            }
            BigDecimal divide = bigDecimal.divide(bigDecimal1,BigDecimal.ROUND_CEILING);
            BigDecimal bigDecimal2 = divide.setScale(0, BigDecimal.ROUND_DOWN);
            BigDecimal subtract = divide.subtract(bigDecimal2);
            int i2 = bigDecimal2.intValue();
            int i3 = subtract.multiply(bigDecimal1).intValue();
            String[] units = exportUnitNames(inventoryVerificationDetail);
            if(i3>0){
                checkInventory="-"+(i2+units[0])+(i3+units[1]);
               //checkInventory="-"+(i2+inventoryVerificationDetail.getDrug().getPack().getName())+(i3+inventoryVerificationDetail.getDrug().getPreparationUnit().getName());
                return checkInventory;
            }else {
                checkInventory="-"+i2+units[0];
                //checkInventory="-"+i2+inventoryVerificationDetail.getDrug().getPack().getName();
                return checkInventory;
            }
        } else {
            // DS92: chk == 0 -> "0" + đơn vị đóng gói; nhánh cũ deref
            // getStuff() khi dòng không có cả drug lẫn stuff -> NPE.
            return "0" + exportUnitNames(inventoryVerificationDetail)[0];

        }
    }
    @Transactional(readOnly = false)
    public List<InventoryVerificationDetail> getByInfoId(String infoId) {
        List<InventoryVerificationDetail> list = this.dao.getByInfoId(infoId);
        return list;
    }

    @Transactional
    public void deleteByInventoryVerificationId(String inventoryVerificationId) {
        this.dao.deleteByInventoryVerificationId(inventoryVerificationId);
    }
}