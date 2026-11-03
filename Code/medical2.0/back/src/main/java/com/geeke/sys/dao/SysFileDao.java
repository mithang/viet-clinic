package com.geeke.sys.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.geeke.common.persistence.CrudDao;
import com.geeke.sys.entity.SysFile;

/**
 * Tệp đính kèm hệ thốngDAOGiao diện
 * @author szy
 * @version 2021-09-22
 */
@Mapper
public interface SysFileDao extends CrudDao<SysFile> {
    // TheoIDmảngXóa
    int deleteByIds(@Param("ids") String[] ids);

    // R3 (iteration 41): lookup gon object_id cua file da luu — sys_file
    // KHONG co cot company_id nen khong kiem tra tenant truc tiep duoc;
    // guard deleteIds trong saveWithFile bang object_id (file cua form
    // nay luon co object_id = id dong info, do uploadFiles = info.id).
    // CAST sang CHAR de so sanh chuoi, tranh ep kieu so BIGINT.
    String getObjectIdById(@Param("id") String id);
}