package com.geeke.taskmanagement.service;


import com.geeke.common.data.Page;
import com.geeke.outpatient.entity.Blmb;
import com.geeke.sys.entity.SysFile;
import com.geeke.sys.service.SysFileService;
import com.geeke.common.service.ServiceException;
import com.geeke.taskmanagement.dao.TaskmangementDao;
import com.geeke.taskmanagement.dao.taskexecutorDao;
import com.geeke.taskmanagement.entity.*;
import com.geeke.utils.IdGen;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service("taskmanageService")
@Transactional(readOnly = true)
public class TaskmanageService {

    @Autowired
    private TaskmangementDao taskmangementDao;

    @Autowired
    private SysFileService sysFileService;

    @Autowired
    private com.geeke.taskmanagement.dao.taskexecutorDao taskexecutorDao;

    /*
     * Z3f: fail-closed ownership check for writes touching an existing task.
     * release/delete/updateaudit/storage UPDATE by id without checking the
     * old row's company_id, so any permitted token could rewrite another
     * tenant's task. Missing row keeps legacy behaviour (DAO returns 0);
     * empty session keeps legacy behaviour, same pattern as Z3a-Z3e.
     */
    private void checkTaskOwnership(String id) {
        if (id == null || id.isEmpty()) {
            return;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (tenantId == null || tenantId.isEmpty() || "null".equals(tenantId)) {
            return;
        }
        taskmanagement old = taskmangementDao.getById(id);
        if (old != null && !tenantId.equals(old.getCompanyId())) {
            throw new ServiceException("Task không thuộc phòng khám hiện tại");
        }
    }

    @Transactional
    public int inserttask(taskmanagement task)  {
        task.setID(IdGen.uuid());
        task.setCompanyId(SessionUtils.getUser().getCompany().getId());
        task.setCreatedBy(SessionUtils.getUserJson().getString("name"));
        task.setCreatedTime(new Date());
        task.setUpdatedBy(SessionUtils.getUserJson().getString("name"));
        task.setUpdatedTime(new Date());
        task.setDelFlag("0");
//        //Lưu tệp đã tải lên
//        List<SysFile> sysFiles = sysFileService.changeAndSaveSysFileList(fileIdUploads, task.getID());
//        if(sysFiles!=null&&sysFiles.size()>0){
//            task.setTaskaccessory(sysFiles.get(0).getId());
//        }
        return taskmangementDao.insertask(task);
    }

    public Page<taskmanagement> selecttasklist(gettaskrcvo rcvo){
        // Z3e: force tenant filter from session (DAO filter was commented out,
        // so any tenant listed every tenant's tasks). Empty session keeps
        // legacy behaviour, same pattern as Z1/Z2/Z3a-Z3d.
        String tenantId = SessionUtils.getLoginTenantId();
        if (tenantId != null && !tenantId.isEmpty() && !"null".equals(tenantId)) {
            rcvo.setCompanyId(tenantId);
        }
        List<taskmanagement> listtotal=taskmangementDao.selecttasktotal(rcvo);
        int total=listtotal.size();
        List<taskmanagement> list=null;
        if(total>0){
            list=taskmangementDao.selecttasklist(rcvo);
        }
        if(!CollectionUtils.isEmpty(list)){
            for (taskmanagement taskmanagement : list) {
                List<taskexecutor> list1 = taskexecutorDao.list(taskmanagement.getID());
                taskmanagement.setTaskexecutors(list1);
            }
        }
        return new Page<>((long) total,list);
    }

    @Transactional
    public taskmanagement save(taskmanagement task,String[] deleteIds, MultipartFile[] fileIdUploads,String counId) throws IOException {
        if(task.getID()!=null && !"".equals(task.getID())){
            taskmanagement storage = storage(task, fileIdUploads, deleteIds, counId);
            taskmanagement release = release(task);
            return release;
        }else {
            if(Objects.equals("0",counId)){
                task.setID(IdGen.uuid());
                task.setCompanyId(SessionUtils.getUser().getCompany().getId());
                task.setCreatedBy(SessionUtils.getUserJson().getString("name"));
                task.setCreatedTime(new Date());
                task.setTaskstatus("1");//Đãphátphát hành
                task.setUpdatedBy(SessionUtils.getUserJson().getString("name"));
                task.setUpdatedTime(new Date());
                task.setDelFlag("0");
                int cutor=savetaskexecutor(task);//Thêm người thực hiện
                int fee=savefeedbacktable(task);//Thêm mớiphản hồi

                //Lưu tệp đã tải lên
                List<SysFile> sysFiles = sysFileService.changeAndSaveSysFileList(fileIdUploads, task.getID());
                if(sysFiles!=null&&sysFiles.size()>0){
                    task.setTaskaccessory(sysFiles.get(0).getId());
                }
                // Theo tệp đính kèmIDXóa thông tin tệp đính kèm
                if (null != deleteIds && deleteIds.length > 0) {
                    sysFileService.delete(deleteIds);
                }
                taskmangementDao.insertask(task);
                return task;
            }else {
                // Z3f: file attach targets an existing task, so check its owner.
                checkTaskOwnership(counId);
                taskmanagement byId = this.taskmangementDao.getById(counId);
                //Lưu tệp đã tải lên
                List<SysFile> sysFiles = sysFileService.changeAndSaveSysFileList(fileIdUploads, byId.getID());
                if(sysFiles!=null&&sysFiles.size()>0){
                    task.setTaskaccessory(sysFiles.get(0).getId());
                }
                // Theo tệp đính kèmIDXóa thông tin tệp đính kèm
                if (null != deleteIds && deleteIds.length > 0) {
                    sysFileService.delete(deleteIds);
                }

                return byId;
            }
        }
    }

    @Transactional(readOnly = false)
    public int savetaskexecutor(taskmanagement task){
        int result=0;
        for(String itme:task.getTaskregion()){
            taskexecutor cutor=new taskexecutor();
            cutor.setID(IdGen.uuid());
            cutor.setCompanyId(SessionUtils.getUser().getCompanyId());
            cutor.setCreatedBy(SessionUtils.getUserJson().getString("name"));
            cutor.setCreatedTime(new Date());
            cutor.setDelFlag("0");
            cutor.setTaskmanagementid(task.getID());
            cutor.setClinic_id(itme);
            result=taskmangementDao.savetaskexecutor(cutor);
        }
        return result;
    }

    /*
    Lấyphản hồitình hình
     */
    public List<feedbacktable> getfeedbacktable(gettaskrcvo rcvo){
        // Z3g: feedback rows are keyed only by taskmanagement_id, so any
        // permitted token could read another tenant's task feedback.
        // Reuse the Z3f ownership check: a foreign task fails closed
        // (ServiceException -> 400); a missing id or empty session keeps
        // legacy behaviour, same pattern as Z3a-Z3f.
        checkTaskOwnership(rcvo.getTaskmanagementid());
        return taskmangementDao.getfeedbacktable(rcvo);
    }

    /*
    Thêm mớiphản hồi
     */
    @Transactional(readOnly = false)
    public int savefeedbacktable(taskmanagement task){
        int result=0;
        for(String itme:task.getTaskregion()) {
            feedbacktable fee = new feedbacktable();
            fee.setId(IdGen.uuid());
            fee.setTaskmanagementid(task.getID());
            fee.setCompanyId(itme);

            fee.setCreatedBy(task.getCreatedBy());
            fee.setCreatedTime(new Date());
            fee.setUpdatedBy(task.getCreatedBy());
            fee.setUpdatedTime(new Date());
            fee.setDelFlag("0");
            fee.setExecuteschedule("0");
            fee.setAuditType("0");
            result=taskmangementDao.savefeedbacktable(fee);
        }
        return result;
    }

    /*
    tác vụxét duyệt
     */
    // Z3f: updateaudit had no @Transactional override, so it inherited the
    // class-level readOnly=true and every legitimate audit failed with a
    // read-only-connection 500; the foreign-row guard above runs first.
    @Transactional
    public int updateaudit(taskmanagement task){
        checkTaskOwnership(task.getID());
        task.setUpdatedBy(SessionUtils.getUser().getUpdateBy());
        task.setUpdatedTime(new Date());
        task.setTaskstatus("1");
        return taskmangementDao.updateaudit(task);
    }



    /**
     * Lấyvăn bảnhồ sơdanh mục của câyhìnhcấu trúc
     */

    public List<Sysusertreeentity> getusertree(gettaskrcvo gettaskrcvo) {
//        QueryWrapper<FileCatalog> wrapper = new QueryWrapper<>();
//        wrapper.eq("panels_id", panelsId);
        // Z3g: the SQL builds the sibling tree from the BODY companyId, so
        // any permitted token could list another tenant group's companies.
        // Force the session tenant (Z3e pattern); the legitimate caller
        // (addtaskForm) already sends currentUser.company.id, so its tree
        // is unchanged. Empty session keeps legacy behaviour.
        String tenantId = SessionUtils.getLoginTenantId();
        if (tenantId != null && !tenantId.isEmpty() && !"null".equals(tenantId)) {
            gettaskrcvo.setCompanyId(tenantId);
        }
        gettaskrcvo rcvo=new gettaskrcvo();
        rcvo.setCompanyId(gettaskrcvo.getCompanyId());
        List<Sysusertreeentity> fileCatalogs = taskmangementDao.getusertree(rcvo);
        // AA2: a tenant whose parent_id is NULL (or with no sibling rows)
        // yields an empty list; the old get(0) crashed with 500. An empty
        // tree is the correct answer, not an exception.
        if (fileCatalogs == null || fileCatalogs.isEmpty()) {
            return new ArrayList<>();
        }
        List<Sysusertreeentity> tree = createTree(fileCatalogs, fileCatalogs.get(0).getPatid());
        return tree;
    }

    /**
     * Đệ quy xây dựng cấu trúc cây (thuật toán)
     * @param lists
     * @param pid
     */
    private List<Sysusertreeentity> createTree(List<Sysusertreeentity> lists, String pid) {
        List<Sysusertreeentity> tree = new ArrayList<>();
        for (Sysusertreeentity catelog : lists) {
            // AA2: patid/id are Strings from the DB (distinct objects), so
            // == compared references and children were always empty. Use
            // null-safe value equality; a null patid matches a null pid.
            if (Objects.equals(catelog.getPatid(), pid)) {
                catelog.setChildren(createTree(lists,catelog.getId()));
                tree.add(catelog);
            }
        }
        return tree;
    }

    @Transactional
    public taskmanagement storage(taskmanagement task, MultipartFile[] fileIdUploads,String[] deleteIds,String countId) throws IOException {



        if(task.getID()!=null&&!"".equals(task.getID())){
            if("0".equals(countId)){
                // Z3f: update branch rewrites the task row + its executors.
                checkTaskOwnership(task.getID());
                //Lưu tệp đã tải lên
                if(null!=fileIdUploads && fileIdUploads.length>0){
                    List<SysFile> sysFiles = sysFileService.changeAndSaveSysFileList(fileIdUploads, task.getID());
                    if(sysFiles!=null&&sysFiles.size()>0){
                        task.setTaskaccessory(sysFiles.get(0).getId());
                    }
                }
                // Theo tệp đính kèmIDXóa thông tin tệp đính kèm
                if (null != deleteIds && deleteIds.length > 0) {
                    sysFileService.delete(deleteIds);
                }

                task.setUpdatedBy(SessionUtils.getUserJson().getString("name"));
                task.setUpdatedTime(new Date());
                int cutor=updatetaskexecutor(task);//Thêm người thực hiện
                taskmangementDao.update(task);
                return task;
            }else {
                // Z3f: file attach targets an existing task, so check its owner.
                checkTaskOwnership(countId);
                taskmanagement byId = this.taskmangementDao.getById(countId);
                //Lưu tệp đã tải lên
                if(null!=fileIdUploads && fileIdUploads.length>0){
                    List<SysFile> sysFiles = sysFileService.changeAndSaveSysFileList(fileIdUploads, byId.getID());
                    if(sysFiles!=null&&sysFiles.size()>0){
                        task.setTaskaccessory(sysFiles.get(0).getId());
                    }
                }
                // Theo tệp đính kèmIDXóa thông tin tệp đính kèm
                if (null != deleteIds && deleteIds.length > 0) {
                    sysFileService.delete(deleteIds);
                }
                return byId;
            }

       }else {
           if(Objects.equals("0",countId)){
               task.setID(IdGen.uuid());
               //Lưu tệp đã tải lên
               if(null!=fileIdUploads && fileIdUploads.length>0){
                   List<SysFile> sysFiles = sysFileService.changeAndSaveSysFileList(fileIdUploads, task.getID());
                   if(sysFiles!=null&&sysFiles.size()>0){
                       task.setTaskaccessory(sysFiles.get(0).getId());
                   }
               }
               // Theo tệp đính kèmIDXóa thông tin tệp đính kèm
               if (null != deleteIds && deleteIds.length > 0) {
                   sysFileService.delete(deleteIds);
               }
               task.setCompanyId(SessionUtils.getUser().getCompany().getId());
               task.setCreatedBy(SessionUtils.getUserJson().getString("name"));
               task.setCreatedTime(new Date());
             //  task.setTaskstatus("0");//chờhành nghề dòng
               task.setUpdatedBy(SessionUtils.getUserJson().getString("name"));
               task.setUpdatedTime(new Date());
               task.setDelFlag("0");
               task.setTaskstatus("0");  //tạm giờLưu
               int cutor=savetaskexecutor(task);//Thêm người thực hiện
               taskmangementDao.insertask(task);
               return task;
           }else {
               // Z3f: file attach targets an existing task, so check its owner.
               checkTaskOwnership(countId);
               taskmanagement byId = this.taskmangementDao.getById(countId);
               //Lưu tệp đã tải lên
               if(null!=fileIdUploads && fileIdUploads.length>0){
                   List<SysFile> sysFiles = sysFileService.changeAndSaveSysFileList(fileIdUploads, byId.getID());
                   if(sysFiles!=null&&sysFiles.size()>0){
                       task.setTaskaccessory(sysFiles.get(0).getId());
                   }
               }
               // Theo tệp đính kèmIDXóa thông tin tệp đính kèm
               if (null != deleteIds && deleteIds.length > 0) {
                   sysFileService.delete(deleteIds);
               }
               return byId;
           }
       }



    }

    private int updatetaskexecutor(taskmanagement task) {
        //trướcXóaliên kếtchỉ định của Phòng khám
        taskexecutorDao.delete(task);

        //XóaxongsauLạiThêm
        return savetaskexecutor(task);//Thêm người thực hiện
    }

    /**
     * tác vụSửaLưusautrực tiếpphátphát hành
     * @param taskmanagement
     * @return
     */
    @Transactional
    public taskmanagement release(taskmanagement taskmanagement) {
        checkTaskOwnership(taskmanagement.getID());
        int i =this.taskmangementDao.release(taskmanagement);
        if(i>0){
            //tác vụphátphát hành
            List<taskexecutor> list = taskexecutorDao.list(taskmanagement.getID());

            List<String> list1 = new ArrayList<>();

            for (taskexecutor taskexecutor : list) {
                list1.add(taskexecutor.getCompany().getId());
            }
           // list.stream().forEach(item->list1.add(item.getClinic_id()));

            taskmanagement.setTaskregion(list1);

            savefeedbacktable(taskmanagement);


            return taskmanagement;
        }
        return null;
    }

    @Transactional
    public taskmanagement delete(taskmanagement taskmanagement) {
        checkTaskOwnership(taskmanagement.getID());
        int i = taskmangementDao.delete(taskmanagement);

        if(i>0){
            return taskmanagement;
        }
        return null;
    }

}
