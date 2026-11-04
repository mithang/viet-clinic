package com.geeke.outpatient.dao;

import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.persistence.CrudDao;
import com.geeke.outpatient.entity.RecipelInfoReview;
import com.geeke.outpatient.vo.PrescriptionStatisticsVO;
import com.geeke.outpatient.vo.StatementVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Đơn thuốcxem xétgiao diện
 * @author lc
 */
@Mapper
public interface RecipelInfoReviewDao extends CrudDao<RecipelInfoReview> {
    RecipelInfoReview getByRecipelInfoId(String recipelInfoId);

    /** N1: recipel_info_id của một review row (lookup trực tiếp, không qua
     * get() nhiều JOIN — get() INNER JOIN registration nên trả null khi
     * registration cha thiếu, đã chứng minh live). */
    String getRecipelInfoIdByReviewId(String reviewId);

    /** Y1: parent_id của một company (để ép phạm vi thống kê theo session). */
    String getCompanyParentId(@Param("id") String companyId);

    List<StatementVO> listPageStatement(@Param("params") List<Parameter> params,@Param("groupByColumns") List<String> groupByColumns);

    PrescriptionStatisticsVO prescriptionStatistics(String id,String startTime,String endTime);
}