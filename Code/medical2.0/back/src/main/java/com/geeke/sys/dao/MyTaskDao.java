package com.geeke.sys.dao;

import com.geeke.common.persistence.CrudDao;
import com.geeke.sys.entity.Attachment;
import com.geeke.sys.entity.MyTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MyTaskDao extends CrudDao<MyTask> {
    // Q4 (iteration 40): lean ownership lookup — chi lay company_id cua
    // dong feedbacktable, khong dung get() vi get() join taskmanagement
    // va map IFNULL(tm.created_time,'') sang taskManagement.updateDate
    // (Date) nen dong khong co taskmanagement se loi chuyen LONG ->
    // Timestamp va lam guard 500 thay vi 400.
    String getCompanyIdById(String id);
}
