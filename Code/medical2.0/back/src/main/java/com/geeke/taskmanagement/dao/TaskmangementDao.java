package com.geeke.taskmanagement.dao;

import com.geeke.taskmanagement.entity.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TaskmangementDao {
    /*
    Thêm tác vụ mới
     */
    int insertask(taskmanagement taskmanagement);
    /*
    Lấytác vụsố dòng
     */
    List<taskmanagement> selecttasktotal(gettaskrcvo rcvo);
    /*
    Theo phúttrangLấyDữ liệu
     */
    List<taskmanagement> selecttasklist(gettaskrcvo rcvo);

    List<Sysusertreeentity> getusertree(gettaskrcvo rcvo);

    int savetaskexecutor(taskexecutor taskexecutor);

    List<feedbacktable> getfeedbacktable(gettaskrcvo rcvo);

    int updateaudit(taskmanagement task);

    int savefeedbacktable(feedbacktable fee);

    int update(taskmanagement task);

    int release(taskmanagement taskmanagement);

    int delete(taskmanagement taskmanagement);

    taskmanagement getById(String countId);

}
