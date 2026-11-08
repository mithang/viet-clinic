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
        // DS141 C: defence-in-depth (framework 400 JSON-null truoc khi
        // toi service, nhu DS118 C1); guard that giu hanh vi khi service
        // duoc goi noi bo.
        if (genTable == null) {
            throw new ServiceException("Bảng không được để trống");
        }
        // DS80: gen_table la metadata toan cuc, khong co cot company nen
        // controller save KHONG co @Valid — them @Valid se bat ca @NotNull
        // ke thua tren DataEntity.getCompany (form UI khong gui company)
        // va lam hong moi luu GenTable hop le. Kiem tra fail-closed 400
        // o day thay vi @Valid: null/rong qua validation se 500 o cot
        // NOT NULL, chi con service check moi chan duoc.
        if (StringUtils.isBlank(genTable.getName())) {
            throw new ServiceException("Tên bảng không được để trống");
        }
        if (StringUtils.isBlank(genTable.getClassName())) {
            throw new ServiceException("Tên lớp thực thể không được để trống");
        }
        if (StringUtils.isBlank(genTable.getSource())) {
            throw new ServiceException("Nguồn không được để trống");
        }
        if (StringUtils.isBlank(genTable.getIsAssociation())) {
            throw new ServiceException("Liên kết bảng không được để trống");
        }
        // DS141 C: khong co @Valid nen moi @Length tren entity deu chet --
        // gia tri qua dai 500 DataIntegrityViolationException o DB.
        // Gioi han lay min(entity max, DB max): comments entity 500
        // nhung DB varchar(64); parentTableFk entity 100 nhung DB
        // varchar(64); className/filter lay entity max (DB rong hon);
        // orderColumns khong co @Length, lay DB varchar(2048).
        if (genTable.getName().length() > 128) {
            throw new ServiceException("Tên Độ dài phải nằm trong khoảng 1 đến 128");
        }
        if (genTable.getComments() != null && genTable.getComments().length() > 64) {
            throw new ServiceException("Giải thích Độ dài phải nằm trong khoảng 0 đến 64");
        }
        if (genTable.getSource().length() > 128) {
            throw new ServiceException("đến nguồn Độ dài phải nằm trong khoảng 0 đến 128");
        }
        if (genTable.getClassName().length() > 100) {
            throw new ServiceException("thực thể Lớp Tên Độ dài phải nằm trong khoảng 1 đến 100");
        }
        if (genTable.getParentTableFk() != null && genTable.getParentTableFk().length() > 64) {
            throw new ServiceException("cha bảng ngoài khóa Độ dài phải nằm trong khoảng 0 đến 64");
        }
        if (genTable.getIsAssociation().length() > 1) {
            throw new ServiceException("có liên kết bảng Độ dài phải nằm trong khoảng 1 đến 1");
        }
        if (genTable.getFilter() != null && genTable.getFilter().length() > 2000) {
            throw new ServiceException("lọc Điều kiện Độ dài phải nằm trong khoảng 0 đến 2000");
        }
        if (genTable.getOrderColumns() != null && genTable.getOrderColumns().length() > 2048) {
            throw new ServiceException("sắp xếp Trường Độ dài phải nằm trong khoảng 0 đến 2048");
        }
        if (genTable.getRemarks() != null && genTable.getRemarks().length() > 255) {
            throw new ServiceException("Ghi chú Độ dài phải nằm trong khoảng 0 đến 255");
        }
        // DS99: Jackson dat null tuong minh (constraintList/genTableColumnList
        // : null trong JSON) ghi de Lists.newArrayList() khoi tao o field,
        // roi vong lap ben duoi NPE -> 500 (probe G5). Null o day nghia la
        // "không có dòng con", nhu khi bo qua truong: chuan hoa ve rong.
        if (genTable.getConstraintList() == null) {
            genTable.setConstraintList(Lists.newArrayList());
        }
        if (genTable.getGenTableColumnList() == null) {
            genTable.setGenTableColumnList(Lists.newArrayList());
        }
        // DS141 C14: cot con gui kem duoc luu xuong qua
        // genTableColumnDao.bulkInsert/bulkUpdate TRUC TIEP, bo qua moi
        // guard cua GenTableColumnService.save -- ten cot con qua dai
        // 500 o bulkInsert. Kiem tra do dai cot con o day, TRUOC
        // super.save, de ca bang cha cung khong duoc tao khi cot con sai.
        // (Chi kiem do dai: guard null/blank cua cot con thuoc ve
        // GenTableColumnService.save, khong ap cho duong luu kem nay.)
        for (GenTableColumn genTableColumn : genTable.getGenTableColumnList()) {
            if (genTableColumn != null) {
                GenTableColumnService.checkLengths(genTableColumn);
            }
        }
        // DS99: CrudService.save rẽ nhánh update khi id khác rỗng, doUpdate
        // khong dong den dong nao neu id khong ton tai ma van tra 200
        // (probe G5: save id la + null lists het NPE nhung van 200 trong
        // khi khong co dong nao duoc tao; cung ho silent no-op nhu DS94
        // MyTask). Chi chap nhan update khi dong da ton tai; tao moi phai
        // gui id rong (UI tao moi khong gui id, sua gui id lay tu list).
        if (StringUtils.isNotBlank(genTable.getId()) && super.get(genTable.getId()) == null) {
            throw new ServiceException("Bảng không tồn tại: " + genTable.getId());
        }

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
     * DS99: bulkInsert/bulkUpdate/bulkDelete ke thua tu CrudService di
     * thang qua dao, bo qua cac guard null cua save() (DS80) nen hang
     * thieu truong NOT NULL 500 o DB (probe G1/G2/G4/G13), va bulkUpdate
     * id khong ton tai tra 200 im lang du mapper khong dong den dong nao
     * (probe G3; tien le DS94 chi sua MyTask). Ghi de o day de fail-closed
     * 400 truoc moi ghi: danh sach null/rong, truong bat buoc, id rong
     * hoac khong ton tai. bulkDelete giu tinh idempotent: id khong ton tai
     * van 200 rows 0 (probe G11), chi chan danh sach null/rong (null
     * truoc day NPE 500 o vong lap CrudService, rong da 400 nho handler
     * BadSqlGrammar DS94 nhung khong co message ro).
     */
    private void validateRequired(GenTable genTable) {
        if (genTable == null) {
            throw new ServiceException("Bảng nghiệp vụ không được để trống");
        }
        if (StringUtils.isBlank(genTable.getName())) {
            throw new ServiceException("Tên bảng không được để trống");
        }
        if (StringUtils.isBlank(genTable.getClassName())) {
            throw new ServiceException("Tên lớp thực thể không được để trống");
        }
        if (StringUtils.isBlank(genTable.getSource())) {
            throw new ServiceException("Nguồn không được để trống");
        }
        if (StringUtils.isBlank(genTable.getIsAssociation())) {
            throw new ServiceException("Liên kết bảng không được để trống");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<GenTable> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách bảng thêm mới không được để trống");
        }
        for (GenTable genTable : entitys) {
            validateRequired(genTable);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<GenTable> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách bảng cập nhật không được để trống");
        }
        for (GenTable genTable : entitys) {
            validateRequired(genTable);
            if (StringUtils.isBlank(genTable.getId())) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của bảng đã tồn tại");
            }
            if (super.get(genTable.getId()) == null) {
                throw new ServiceException("Bảng không tồn tại: " + genTable.getId());
            }
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<GenTable> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách bảng xóa không được để trống");
        }
        return super.bulkDelete(entitys);
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