package com.geeke.largeScreen.dao;

import com.geeke.largeScreen.entity.Basics;
import org.apache.ibatis.annotations.Mapper;

/**
 * Màn hình dữ liệu lớnDAOGiao diện
 * @author zhanghan
 * @version 2023-09-05
 */
@Mapper
public interface ScreenDao {

    /** Thêm tác vụ mới */
    public String getQuest(String parentId);

    /** Số người đăng ký khám hôm nay */
    public String getRegister(String parentId);

    /** Số lượt tiếp nhận hôm nay */
    public String getReceivePatients(String parentId);

    /** Thu nhập hôm nay */
    public String getIncome(String parentId);

    /** Số lượt ký mới hôm nay */
    public String getSigning(String parentId);

}