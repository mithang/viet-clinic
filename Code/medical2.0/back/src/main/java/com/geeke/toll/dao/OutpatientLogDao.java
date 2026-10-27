package com.geeke.toll.dao;

import com.geeke.common.data.PageRequest;
import com.geeke.common.persistence.CrudDao;
import com.geeke.outpatient.entity.Patient;
import com.geeke.toll.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Nhật ký khám ngoại trúDAOGiao diện
 * @author lc
 * @version 2022-06-15
 */
@Mapper
public interface OutpatientLogDao extends CrudDao<OutpatientLog> {
    public OutpatientLog getLogs(String id);
    /** Truy vấn */
    int logCount(PageRequest var1);

    List<OutpatientLog> getOutpatientLog(OutpatientLogRc outpatientLogRc);

    /** Truy vấntất cảtập hợp */
    List<OutpatientLog> getOutpatientlist(OutpatientLogRc outpatientLogRc);

    /** SửaNgàynhật kýThông tin */
    int updateLog(OutpatientLog outpatientLogRc);

    List<Patient> getPovert(Patient patient);

    List<Patient> getPoverty(String patientId);

}