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

    // K3 (iteration 58): resolve chu so huu tenant cua mot object_id qua
    // tat ca namespace ghi sys_file (xem SysFileService
    // .verifySysFileDeleteOwnership): registration id, medical_record
    // .file_id, org_company.file_id (chinh company do la tenant),
    // sys_user id (avatar), inspection_check_info id,
    // feedbacktable id (myTask), taskmanagement id. Tra ve danh sach
    // company_id (chuoi, CAST de dong nhat bigint/varchar); rong neu
    // khong resolve duoc namespace nao. Khong loc del_flag: chu so huu
    // khong doi khi dong cha bi xoa mem. Bang clinic loai khoi UNION:
    // khong co cot company_id va khong co code ghi sys_file bang
    // Clinic.fileId (xem ghi chu trong mapper).
    java.util.List<String> getOwnerCompanyIdsByObjectId(@Param("objectId") String objectId);
}