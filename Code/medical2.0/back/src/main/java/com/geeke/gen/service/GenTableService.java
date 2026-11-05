package com.geeke.gen.service;

import com.geeke.common.constants.ActionConstants;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.gen.dao.ConstraintDao;
import com.geeke.gen.dao.GenTableColumnDao;
import com.geeke.gen.dao.GenTableDao;
import com.geeke.gen.entity.Constraint;
import com.geeke.gen.entity.GenTable;
import com.geeke.gen.entity.GenTableColumn;
import com.geeke.sys.entity.Action;
import com.geeke.sys.entity.ActionRecycle;
import com.geeke.utils.Reflections;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Quản lý bảng nghiệp vụService
 * @author lys
 * @version 2021-08-26
 */
 
@Service("genTableService")
@Transactional(readOnly = true)
public class GenTableService extends CrudService<GenTableDao, GenTable>{

    @Autowired
    private ConstraintDao constraintDao;
    @Autowired
    private GenTableColumnDao genTableColumnDao;
    
    @Override
    public GenTable get(String id) {
        GenTable genTable = super.get(id);
        // DS42: super.get() tra null khi dong khong ton tai (hop dong cua
        // CrudService.get); truoc day genTable.getId() ben duoi NPE 500 o
        // GET /gen/genTable/{id} voi id la (cung mau NPE ma ITER67 (8c)
        // da tranh o importJson bang super.get()). Tra null nhu get() goc.
        if (genTable == null) {
            return null;
        }

        List<Parameter> params = null;
        PageRequest pageRequest;
        /*Lấy danh sách bảng con   Trường ràng buộc*/
        params = Lists.newArrayList();
        params.add(new Parameter("gen_table_id", "=", genTable.getId()));
        pageRequest = new PageRequest(params);
        genTable.setConstraintList(constraintDao.listAll(pageRequest));        
        /*Lấy danh sách bảng con   Trường bảng nghiệp vụ*/
        params = Lists.newArrayList();
        params.add(new Parameter("gen_table_id", "=", genTable.getId()));
        pageRequest = new PageRequest(params);
        genTable.setGenTableColumnList(genTableColumnDao.listAll(pageRequest));        
        return genTable;
    }

    @Override
    @Transactional(readOnly = false)
    public GenTable save(GenTable genTable) {

        String id = super.save(genTable).getId();
        if (StringUtils.isNoneBlank(id)) {

            List<Parameter> params = null;
            PageRequest pageRequest;
            /* Xử lý bảng con     Trường ràng buộc */
            params = Lists.newArrayList();
            params.add(new Parameter("gen_table_id", "=", genTable.getId()));
            pageRequest = new PageRequest(params);
            List<Constraint> list_Constraint = constraintDao.listAll(pageRequest);            
            List<Constraint> deleteConstraints = Lists.newArrayList(); // Danh sách xóa
            List<Constraint> insertConstraints = Lists.newArrayList(); // Danh sách thêm
            List<Constraint> updateConstraints = Lists.newArrayList(); // Danh sách cập nhật
            for(Constraint constraintSaved: list_Constraint) {
                boolean found = false;   
                for (Constraint constraint : genTable.getConstraintList()){
                   if(constraintSaved.getId().equals(constraint.getId())){
                       found = true;
                       break;
                   }
                }
                if(!found) {
                   deleteConstraints.add(constraintSaved);
                }
            }
            for (Constraint constraint : genTable.getConstraintList()){

                if (StringUtils.isBlank(constraint.getId())) {
                    constraint.setGenTable(genTable);
                    constraint.preInsert();
                    insertConstraints.add(constraint);
                } else {
                    constraint.preUpdate();
                    updateConstraints.add(constraint);
                }

            }
            if(deleteConstraints.size() > 0) {
            	constraintDao.bulkDelete(deleteConstraints);
            }
            if(updateConstraints.size() > 0) {
            	constraintDao.bulkUpdate(updateConstraints);
            }
            if(insertConstraints.size() > 0) {
            	constraintDao.bulkInsert(insertConstraints);
            }
            /* Xử lý bảng con     Trường bảng nghiệp vụ */
            params = Lists.newArrayList();
            params.add(new Parameter("gen_table_id", "=", genTable.getId()));
            pageRequest = new PageRequest(params);
            List<GenTableColumn> list_GenTableColumn = genTableColumnDao.listAll(pageRequest);            
            List<GenTableColumn> deleteGenTableColumns = Lists.newArrayList(); // Danh sách xóa
            List<GenTableColumn> insertGenTableColumns = Lists.newArrayList(); // Danh sách thêm
            List<GenTableColumn> updateGenTableColumns = Lists.newArrayList(); // Danh sách cập nhật
            for(GenTableColumn genTableColumnSaved: list_GenTableColumn) {
                boolean found = false;   
                for (GenTableColumn genTableColumn : genTable.getGenTableColumnList()){
                   if(genTableColumnSaved.getId().equals(genTableColumn.getId())){
                       found = true;
                       break;
                   }
                }
                if(!found) {
                   deleteGenTableColumns.add(genTableColumnSaved);
                }
            }
            for (GenTableColumn genTableColumn : genTable.getGenTableColumnList()){

                if (StringUtils.isBlank(genTableColumn.getId())) {
                    genTableColumn.setGenTable(genTable);
                    genTableColumn.preInsert();
                    insertGenTableColumns.add(genTableColumn);
                } else {
                    genTableColumn.preUpdate();
                    updateGenTableColumns.add(genTableColumn);
                }

            }
            if(deleteGenTableColumns.size() > 0) {
            	genTableColumnDao.bulkDelete(deleteGenTableColumns);
            }
            if(updateGenTableColumns.size() > 0) {
            	genTableColumnDao.bulkUpdate(updateGenTableColumns);
            }
            if(insertGenTableColumns.size() > 0) {
            	genTableColumnDao.bulkInsert(insertGenTableColumns);
            }
        }
        return genTable;
    }


    /**
     * Xóa
     * @param genTable
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(GenTable genTable) {
        List<Parameter> params = null;
        PageRequest pageRequest;
        /* Xử lý bảng con     Trường ràng buộc */
        params = Lists.newArrayList();
        params.add(new Parameter("gen_table_id", "=", genTable.getId()));
        pageRequest = new PageRequest(params);
        genTable.setConstraintList(constraintDao.listAll(pageRequest));        
        /* Xử lý bảng con     Trường bảng nghiệp vụ */
        params = Lists.newArrayList();
        params.add(new Parameter("gen_table_id", "=", genTable.getId()));
        pageRequest = new PageRequest(params);
        genTable.setGenTableColumnList(genTableColumnDao.listAll(pageRequest));        
        if(genTable.getConstraintList() != null && genTable.getConstraintList().size() > 0) {
        	constraintDao.bulkDelete(genTable.getConstraintList());
        }
        if(genTable.getGenTableColumnList() != null && genTable.getGenTableColumnList().size() > 0) {
        	genTableColumnDao.bulkDelete(genTable.getGenTableColumnList());
        }

        int rows = super.delete(genTable);
        return rows;
    }

    @Transactional(readOnly = false)
    public GenTable importJson(GenTable genTable) {
        // ITER67 item 1 (8c), phuong an B, gop chung 1 commit voi san moi:
        // import chi cap nhat dong da ton tai (file export genTable.json
        // luon co id). Code cu: get(id) qua GenTableService.get -> goi
        // getId() tren null -> NPE 500 o ca nhanh id trong lan id khong
        // ton tai; nhanh insert cu lai save them 1 lan ngay sau do
        // (double-save/audit). Nay: id trong hoac khong ton tai -> 400
        // tuong minh qua ServiceException (GlobalExceptionHandler), khong
        // NPE, khong tao dong; chi 1 lan save -> 1 audit.
        if (genTable == null || StringUtils.isBlank(genTable.getId())) {
            throw new ServiceException("Import JSON yêu cầu id của bảng đã tồn tại; không tạo bảng mới qua import");
        }
        if (super.get(genTable.getId()) == null) {
            throw new ServiceException("Import JSON: bảng không tồn tại: " + genTable.getId());
        }
        return this.save(genTable);
    }

    /**
     * Tạo nhật ký thao tác
     * @param actionTypeId  Loại thao tácId
     * @param entity        Đối tượng thực thể được thao tác
     * @return
     */
    @Override
    protected Action createAction(String actionTypeId, GenTable entity) {
        Action action = super.createAction(actionTypeId, entity);
        if(action == null) {
            return null;
        }
        // Khi xóa, ghi lại và lưu dữ liệu đã lưu vào thùng rác
        if(ActionConstants.ACTION_DELETED.equals(actionTypeId)) {   
            for(Constraint child: entity.getConstraintList()) {
                ActionRecycle recycle = new ActionRecycle();
                recycle.setTableName(child.getBusTableName());
                recycle.setObjectId(child.getId());
                recycle.setObjectName((String)Reflections.invokeGetter(child, "name"));
                action.getActionRecycleList().add(recycle);            
            }
            for(GenTableColumn child: entity.getGenTableColumnList()) {
                ActionRecycle recycle = new ActionRecycle();
                recycle.setTableName(child.getBusTableName());
                recycle.setObjectId(child.getId());
                recycle.setObjectName((String)Reflections.invokeGetter(child, "name"));
                action.getActionRecycleList().add(recycle);            
            }
        }
        return action;
    }
}