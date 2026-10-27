package com.geeke.stock.service;

import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.persistence.DataEntity;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;

import com.geeke.outpatient.entity.PresDrug;
import com.geeke.outpatient.service.PresDrugService;
import com.geeke.stock.dao.MedicinalStockControlDao;
import com.geeke.stock.entity.Drug;
import com.geeke.stock.entity.MedicinalStockControl;
import com.geeke.stock.entity.Stuff;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Thuốc/Kiểm soát tổng tồn kho vật tưService
 * @author hl
 * @version 2022-09-26
 */

@Service("medicinalStockControlService")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MedicinalStockControlService extends CrudService<MedicinalStockControlDao, MedicinalStockControl>{
    private final MedicinalStockControlDao dao;

    /** ThuốcTồn khokiểm soát là Cơ sởTổng hưởngDữ liệu: Cho phép phòng khám thuộc cùng cơ sở truy cập dữ liệu phòng khám của cơ sở(Với danh sách #{institution} SQL Nhất quán) */
    @Override
    protected boolean isInstitutionShared() {
        return true;
    }

    private final DrugService drugService;

    private final StuffService stuffService;

    private final PresDrugService presDrugService;


    public static final Boolean enable = Boolean.TRUE;

    /**
     * Thêm mớiThuốc hoặc Vật tưcơ bảnchínhDữ liệu giờ, Khởi tạo dữ liệu bản ghi kiểm soát tổng tồn kho
     * @param dataEntity
     */
    @Transactional(readOnly = false)
    public void initStock(DataEntity<?> dataEntity)
    {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        List<Parameter> parameters = SearchParamsBuilder.create()
                .eq("drug_stuff_id", dataEntity.getId())
                .build();
        List<MedicinalStockControl> list = super.listAll(parameters, "");
        MedicinalStockControl medicinalStockControl = CollectionUtils.isEmpty(list) ? new MedicinalStockControl() : list.get(0);

        if (dataEntity instanceof Drug)
        {
            Drug drug = drugService.get(dataEntity.getId());
            if (Objects.isNull(drug) || StringUtils.isBlank(drug.getId()))
            {
                throw new ServiceException("Khởi tạo[ThuốcTồn kho]Khi kiểm soát tổng,Không lấy được thông tin bảo trì cơ bản của thuốc!");
            }

            medicinalStockControl.setDrugStuffName(drug.getGoodsName());
            if (CollectionUtils.isEmpty(list))
            {
                medicinalStockControl.setCompany(drug.getCompany());
                medicinalStockControl.setDrugStuffId(drug.getId());
                medicinalStockControl.setType(1);
                medicinalStockControl.setStorageStock(BigDecimal.ZERO);
                medicinalStockControl.setUsedStock(BigDecimal.ZERO);
                medicinalStockControl.setOccupyStock(BigDecimal.ZERO);
                medicinalStockControl.setSurplusStock(BigDecimal.ZERO);
                medicinalStockControl.setReimburseStock(BigDecimal.ZERO);
            }
        }
        else if (dataEntity instanceof Stuff)
        {
            Stuff stuff = stuffService.get(dataEntity.getId());
            if (Objects.isNull(stuff) || StringUtils.isBlank(stuff.getId()))
            {
                throw new ServiceException("Khởi tạo[Vật tưTồn kho]Khi kiểm soát tổng,Không lấy được thông tin bảo trì cơ bản của vật tư!");
            }

            medicinalStockControl.setDrugStuffName(stuff.getName());
            if (CollectionUtils.isEmpty(list))
            {
                medicinalStockControl.setCompany(stuff.getCompany());
                medicinalStockControl.setDrugStuffId(stuff.getId());
                medicinalStockControl.setType(2);
                medicinalStockControl.setStorageStock(BigDecimal.ZERO);
                medicinalStockControl.setUsedStock(BigDecimal.ZERO);
                medicinalStockControl.setOccupyStock(BigDecimal.ZERO);
                medicinalStockControl.setSurplusStock(BigDecimal.ZERO);
                medicinalStockControl.setReimburseStock(BigDecimal.ZERO);
            }
        }

        super.save(medicinalStockControl);
    }

    /**
     * Thêm mớiThuốc hoặc Vật tưTồn kho giờ, Khởi tạo dữ liệu bản ghi kiểm soát tổng tồn kho
     * @param dataEntity
     */
    @Transactional(readOnly = false)
    public void initStockTo(DataEntity<?> dataEntity)
    {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }
        List<Parameter> parameters = SearchParamsBuilder.create()
                .eq("drug_stuff_id", dataEntity.getId())
                .build();
        List<MedicinalStockControl> list = super.listAll(parameters, "");
        for (MedicinalStockControl list1 : list){
            list1.getCompany().setId(dataEntity.getCompany().getId());
        }
        MedicinalStockControl medicinalStockControl = CollectionUtils.isEmpty(list) ? new MedicinalStockControl() : list.get(0);
        medicinalStockControl.setId(null);

        if (dataEntity instanceof Drug)
        {
            Drug drug = drugService.get(dataEntity.getId());
            if (Objects.isNull(drug) || StringUtils.isBlank(drug.getId()))
            {
                throw new ServiceException("Khởi tạo[ThuốcTồn kho]Khi kiểm soát tổng,Không lấy được thông tin bảo trì cơ bản của thuốc!");
            }

            medicinalStockControl.setDrugStuffName(drug.getGoodsName());
            if (CollectionUtils.isEmpty(list))
            {
                medicinalStockControl.setCompany(drug.getCompany());
                medicinalStockControl.setDrugStuffId(drug.getId());
                medicinalStockControl.setType(1);
                medicinalStockControl.setStorageStock(BigDecimal.ZERO);
                medicinalStockControl.setUsedStock(BigDecimal.ZERO);
                medicinalStockControl.setOccupyStock(BigDecimal.ZERO);
                medicinalStockControl.setSurplusStock(BigDecimal.ZERO);
                medicinalStockControl.setReimburseStock(BigDecimal.ZERO);
            }
        }
        else if (dataEntity instanceof Stuff)
        {
            Stuff stuff = stuffService.get(dataEntity.getId());
            if (Objects.isNull(stuff) || StringUtils.isBlank(stuff.getId()))
            {
                throw new ServiceException("Khởi tạo[Vật tưTồn kho]Khi kiểm soát tổng,Không lấy được thông tin bảo trì cơ bản của vật tư!");
            }

            medicinalStockControl.setDrugStuffName(stuff.getName());
            if (CollectionUtils.isEmpty(list))
            {
                medicinalStockControl.setCompany(stuff.getCompany());
                medicinalStockControl.setDrugStuffId(stuff.getId());
                medicinalStockControl.setType(2);
                medicinalStockControl.setStorageStock(BigDecimal.ZERO);
                medicinalStockControl.setUsedStock(BigDecimal.ZERO);
                medicinalStockControl.setOccupyStock(BigDecimal.ZERO);
                medicinalStockControl.setSurplusStock(BigDecimal.ZERO);
                medicinalStockControl.setReimburseStock(BigDecimal.ZERO);
            }
        }
        medicinalStockControl.setStorageStock(BigDecimal.valueOf(0));
        medicinalStockControl.setUsedStock(BigDecimal.valueOf(0));
        medicinalStockControl.setOccupyStock(BigDecimal.valueOf(0));
        medicinalStockControl.setSurplusStock(BigDecimal.valueOf(0));
        medicinalStockControl.setReimburseStock(BigDecimal.valueOf(0));
        super.save(medicinalStockControl);
    }

    /**
     * hủyXóaThuốc hoặc Vật tưchínhDữ liệu giờ, Kiểm traTồn khoCó hay khôngsử dụng, NếuNếu đang sử dụng thì không cho phép hủyXóachínhDữ liệu; NếuChưasử dụng, thìcho phéphủyXóachínhDữ liệu, Đồng thời đánh dấu hủy/xóa cho dữ liệu kiểm soát tồn kho
     * @param dataEntity
     */
    @Transactional(readOnly = false)
    public void delAllStock(DataEntity<?> dataEntity)
    {
        if (!MedicinalStockControlService.enable)
        {
            return;
        }



        List<Parameter> parameters = SearchParamsBuilder.create()
                .eq("drug_stuff_id", dataEntity.getId())
                .build();
        List<MedicinalStockControl> list = super.listAll(parameters, "");


        if (CollectionUtils.isEmpty(list))
        {
            //TODO: tạiBậtđộngTồn khokiểm soátchức năng giờ, củatrước của lịch sửsảnsinhDữ liệuNhư 何Xử lý???   khôngquản lý, còn có 手công việc补录Dữ liệuĐếnDữ liệukho
            return;
        }else {
            throw new ServiceException("đóThuốcTồn tạiTồn khoKhông thểXóa!");
        }

//        MedicinalStockControl medicinalStockControl = list.get(0);
//        BigDecimal storageStock = medicinalStockControl.getStorageStock();    //Tổng lượng nhập kho
//        BigDecimal surplusStock = medicinalStockControl.getSurplusStock();    //Số lượng tồn kho còn khả dụng
//        if (storageStock.compareTo(surplusStock) != 0)
//        {
//            throw new RuntimeException("độngTồn khokiểm soátĐãphátthay đổi, Không thểhủyXóacơ bảncấu hìnhDữ liệu!");
//        }
//
//        //tổngTồn khokiểm soátDữ liệuđánhtrênhủyXóađánh dấu
//        super.delete(medicinalStockControl);
//
//        //TODO:Có cần xóa chi tiết thao tác tồn kho động không
    }

    @Transactional(readOnly = true)
    public List<MedicinalStockControl> getByDrugOrStuffId(String drugOrStuffId, String companyId) {
        return this.dao.getByDrugOrStuffId(drugOrStuffId,companyId);
    }

    //Truy vấntenantdướiTồn kho
    public List<MedicinalStockControl> listAlls(List<Parameter> parameters, String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(parameters, orderby);

        return dao.listAlls(pageRequest);
    }

    /**
     * chỉTruy vấnnàyPhòng khámdướiTồn kho 2025.4.2
     * @param parameters
     * @param orderby
     * @return
     */
    public List<MedicinalStockControl> listAllByCompany(List<Parameter> parameters, String orderby) {

        PageRequest pageRequest = new PageRequest(parameters, orderby, SessionUtils.getLoginTenantId());

        return dao.listAlls(pageRequest);
    }



    public List<MedicinalStockControl>  listPreAll(List<Parameter> parameters,String orderby) {
        PageRequest pageRequest = buildTenantPageRequest(parameters, orderby);
        return dao.listPreAll(pageRequest);
    }


}