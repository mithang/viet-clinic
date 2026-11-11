package com.geeke.stock.dao;

import com.geeke.common.data.PageRequest;
import com.geeke.stock.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;

import java.math.BigDecimal;
import java.util.List;

/**
 * Chi tiết kiểm kê tồn khoDAOGiao diện
 * @author Quản trị viên cấp cao
 * @version 2022-11-02
 */
@Mapper
public interface InventoryVerificationDetailDao extends CrudDao<InventoryVerificationDetail> {
    void saveDrug(@Param("medicinalStorageControls") List<MedicinalStorageControl> medicinalStorageControls);

    void saveStuff(@Param("medicinalStorageControls") List<MedicinalStorageControl> medicinalStorageControls);

    // DS191: DAO listPage(pageRequest, type) DA XOA — dead, chi duoc goi
    // boi overload listPage 5 doi so cua service (cung da xoa, 0 caller).
    // Mapper <select id="listPage"> GIU LAI: MyBatis bind theo statement
    // id nen no phuc vu CrudDao.listPage(PageRequest) 1 doi so (live,
    // CrudService.listPage -> exportExcel); xoa no lam export 500
    // (BindingException) khi count > 0. Chi giu listPages (DS23) + listPage
    // generic nay.
    List<InventoryVerificationDetail> getByInfoId(String infoId);

    void deleteByInventoryVerificationId(String inventoryVerificationId);

    /** L1 (iteration 34): chi id + company + phieu cha, tranh mapping rong
     * cua get() (IFNULL end_date '' -> Timestamp) khi dong khong co kho. */
    InventoryVerificationDetail getOwnership(String id);

    /** DS65: tong lai/lo cua TOAN BO detail thuoc phieu, doc khoa
     * (SELECT ... FOR UPDATE) trong cung transaction voi bulkUpdate:
     * current read nen thay ca thay doi detail da commit cua saveAll
     * song song khac, khong phai snapshot REPEATABLE READ; khoa cac
     * dong detail cua phieu. Caller khoa phieu cha truoc (lockById) nen
     * khong deadlock voi bulkUpdate cua luong song song. */
    BigDecimal sumProfitAndLossPriceForUpdate(@Param("inventoryVerificationId") String inventoryVerificationId);

    public List<InventoryVerificationDetail> listPages(PageRequest pageRequest);

}