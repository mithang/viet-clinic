package com.geeke.toll.dao;

import com.geeke.common.data.PageRequest;
import com.geeke.toll.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;

import java.util.List;

/**
 * Quản lý thu phíDAOGiao diện
 * @author lc
 * @version 2022-06-16
 */
@Mapper
public interface TollInfoDao extends CrudDao<TollInfo> {

    /** N3a: company của phiếu toll cha, tra trực tiếp một cột (không qua
     * get() — mapper get() của TollDetail đã chứng minh không đáng tin
     * vì lỗi tollId.name, và lookup một cột tránh mọi JOIN thừa). */
    String getTollInfoCompanyId(@Param("id") String id);

    /** DS60: locked read of one toll slip for cancelUnpaidToll. SELECT ...
     * FOR UPDATE inside the service's @Transactional method serializes
     * concurrent cancels of the same slip: the loser blocks until the
     * winner commits, then re-reads del_flag='1' and is refused with the
     * existing 400, so only one CANCEL audit row is ever written. An
     * UPDATE-claim on del_flag (the DS52-DS58 pattern) does NOT fit here:
     * the claim would set del_flag='1' first, and the body's
     * CrudService.delete() would then find no changed column whenever
     * update_date falls in the same second (no CLIENT_FOUND_ROWS in the
     * JDBC URL), return 0 rows and silently drop the sys_action DELETED
     * row the cancel currently writes. Precedent for a FOR UPDATE claim
     * read: sequenceDao.xml. */
    TollInfo getForUpdate(@Param("id") String id);

    List<TollInfo> tollTotalForm(PageRequest var1);

    List<TollInfo> tollDetailForm(PageRequest var1);

    int formTollCount(PageRequest var1);

    int formDetailCount(PageRequest var1);

    TollVo tollTotalAllPayments(PageRequest var1);

    TollVo tollDetailAmountReceivedAble(PageRequest var1);

    TollInfo getTollInfoByRegistrationId(String registrationId);

    List<TollInfo> getCreateBy(PageRequest var1);

    List<WorkLoad> countWorkload(PageRequest var1);

    List<WorkLoad> getWorkload(PageRequest var1);

    List<WorkLoad>  getCount(PageRequest var1);

    WorkLoadStat  getCounts(PageRequest var1);

    List<WorkLoad> getTemporaryCost(PageRequest var1);

    /**
     * hàng loạtTruy vấnnhiềuCáiBác sĩ của lâm sàng giờChi phí
     * @param pageRequest  phúttrangVui lòngyêu cầu(bao gồmChungqualọcĐiều kiện)
     * @param doctorNames Bác sĩTênDanh sách
     * @return lâm sàng giờChi phíDanh sách
     */
    List<WorkLoad> getTemporaryCostBatch(@Param("pageRequest") PageRequest pageRequest, @Param("doctorNames") List<String> doctorNames);

    WorkLoadStat getWorkLoadStat(PageRequest var1);

    List<WorkLoadStat> getWorkLoadStats(PageRequest var1);

    List<WorkLoadStat> getTemporaryCostStat(PageRequest var1);

    List<DrugSales> getDrugSales(PageRequest pageRequest);

    List<DrugSales> countDrugSales(PageRequest pageRequest);

    DrugSales getDrugSalesStat(PageRequest pageRequest);


    List<Ypjxc> getypjxcmanagement(YpjxcRc ypjxcRc);
    List<Ypjxc> getypjxcmanagementsums(YpjxcRc ypjxcRc);
    int countYpjxcManagement(YpjxcRc ypjxcRc);

    List<Stuffsalessummary> getStuffsalessummarylist(StuffsalessummaryRc stuffsalessummaryRc);
    List<Stuffsalessummary> getStuffsalessummarysums(StuffsalessummaryRc stuffsalessummaryRc);
    int countStuffSalesSummary(StuffsalessummaryRc stuffsalessummaryRc);

    List<Ypjxc> getcljxcmanagement(YpjxcRc ypjxcRc);
    List<Ypjxc> getcljxcmanagementsums(YpjxcRc ypjxcRc);
    int countCljxcManagement(YpjxcRc ypjxcRc);

    List<Ypclrkcx> getypclrkcxlist(YpjxcRc ypjxcRc);
    List<Ypclrkcx> getypclrkcxsums(YpjxcRc ypjxcRc);
    int countYpclrkcx(YpjxcRc ypjxcRc);

    List<Ypjxc> getjglist(YpjxcRc ypjxcRc);

}