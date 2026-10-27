package com.geeke.sys.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.constants.ActionConstants;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.service.CrudService;
import com.geeke.sys.entity.Action;
import com.geeke.sys.entity.ActionRecycle;
import com.geeke.sys.entity.SysFile;
import com.geeke.sys.dao.SysFileDao;
import com.geeke.sys.entity.SysFileContent;
import com.geeke.sys.dao.SysFileContentDao;
import com.geeke.utils.Reflections;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.web.multipart.MultipartFile;

/**
 * Tệp đính kèm hệ thốngService
 * @author szy
 * @version 2021-09-22
 */
 
@Service("sysFileService")
@Transactional(readOnly = true)
public class SysFileService extends CrudService<SysFileDao, SysFile>{

    @Autowired
    private SysFileContentDao sysFileContentDao;
    
    @Override
    public SysFile get(String id) {
        SysFile sysFile = super.get(id);

        /*Lấy danh sách bảng con   Bảng byte tệp đính kèm*/
        List<Parameter> params = SearchParamsBuilder.create().eq("id", sysFile.getId()).build();
        PageRequest pageRequest = new PageRequest(params);
        sysFile.setSysFileContentList(sysFileContentDao.listAll(pageRequest));        
        return sysFile;
    }

    @Override
    @Transactional(readOnly = false)
    public SysFile save(SysFile sysFile) {
	
        SysFile sysFileTemp = super.save(sysFile);
        if (StringUtils.isNoneBlank(sysFileTemp.getId())) {

            /* Xử lý bảng con     Bảng byte tệp đính kèm */
            List<Parameter> params = SearchParamsBuilder.create().eq("id", sysFile.getId()).build();
            PageRequest pageRequest = new PageRequest(params);
            List<SysFileContent> list_SysFileContent = sysFileContentDao.listAll(pageRequest);            
            List<SysFileContent> deleteSysFileContents = Lists.newArrayList(); // Danh sách xóa
            List<SysFileContent> insertSysFileContents = Lists.newArrayList(); // Danh sách thêm
            List<SysFileContent> updateSysFileContents = Lists.newArrayList(); // Danh sách cập nhật
            for(SysFileContent sysFileContentSaved: list_SysFileContent) {
                boolean found = false;   
                for (SysFileContent sysFileContent : sysFile.getSysFileContentList()){
                   if(sysFileContentSaved.getId().equals(sysFileContent.getId())){
                       found = true;
                       break;
                   }
                }
                if(!found) {
                   deleteSysFileContents.add(sysFileContentSaved);
                }
            }
            if(deleteSysFileContents.size() > 0) {
                sysFileContentDao.bulkDelete(deleteSysFileContents);
            }
            for (SysFileContent sysFileContent : sysFile.getSysFileContentList()){
            

                if (StringUtils.isBlank(sysFileContent.getId())) {
                    sysFileContent.setId(sysFileTemp.getId());  // Bảng conid và chabảngidNhất quán
                    preInsert(sysFile,sysFileContent);
                    insertSysFileContents.add(sysFileContent);
                } else {
                    preInsert(sysFile,sysFileContent);
                    updateSysFileContents.add(sysFileContent);
                }

            }
            if(updateSysFileContents.size() > 0) {
            	sysFileContentDao.bulkUpdate(updateSysFileContents);
            }
            if(insertSysFileContents.size() > 0) {
            	sysFileContentDao.bulkInsert(insertSysFileContents);
            }
        }
        return sysFileTemp;
    }

    /**
     * multipartFilesTệpchuyển là SysFiletập hợpĐối tượng
     * @param multipartFiles
     * @param objectId
     * @return
     */
    @Transactional(readOnly = false)
    public List<SysFile> changeAndSaveSysFileList(MultipartFile[] multipartFiles, String objectId) throws IOException {
        List<SysFile> fileList = new ArrayList<>(); // Tệptập hợpĐối tượng
        for(MultipartFile file : multipartFiles) {
            List<SysFileContent> fileContentList = new ArrayList<>(); // Tệpnội dungtập hợpĐối tượng
            SysFile sysFile = new SysFile();
            String originalFilename = file.getOriginalFilename();
            int begin = originalFilename.indexOf(".");
            int last = originalFilename.length();
            //Lấy đượcTệpsauhậu tốtên
            String suffix = originalFilename.substring(begin, last);
            sysFile.setFileType(suffix); // Loại tệp

            sysFile.setObjectId(Long.parseLong(objectId)); // Bảng nghiệp vụID
            sysFile.setName(file.getOriginalFilename()); // Tên tệp
            sysFile.setFileSize(file.getSize()); // Tệpkích thước

            SysFileContent fileContent = new SysFileContent();
            InputStream is = file.getInputStream();
            byte[] fContent = new byte[(int) file.getSize()];
            is.read(fContent);
            fileContent.setFileByte(fContent); // byte
            fileContent.setFileType(suffix); // Loại tệp
            fileContentList.add(fileContent); // Tệplưu trữnội dung
            sysFile.setSysFileContentList(fileContentList);
            fileList.add(sysFile);
            save(sysFile);
        }
        return fileList;
    }

    /**
     * TheoobjectIdXóatệp đính kèm và tệp đính kèmlưu trữbảngThông tinThông tin
     * @param objectId
     */
    @Transactional(readOnly = false)
    public void deleteByObjectId(String objectId) {
        List<Parameter> params = SearchParamsBuilder.create().eq("object_id", objectId).build();
        PageRequest pageRequest = new PageRequest(params);
        List<SysFile> sysFiles = dao.listAll(pageRequest);
        for (SysFile file : sysFiles) {
            delete(file); // XóaTệp đính kèm chứa bảng lưu trữThông tin
        }
        /*if (sysFiles.size() > 0) {
            dao.bulkDelete(sysFiles); // Xóatệp đính kèmbảngThông tin
        }*/

    }


    /**
     * @param sysFile
     * @param sysFileContent
     * Để thời gian cập nhật của tệp đính kèm hệ thống và bảng byte hệ thống được nhất quán khi lưu, Người dùngẢnh hiển thị lại làm cho trình duyệtBộ nhớ đệmsử dụng
     * dùnggốcđến của tạotệp đính kèm, hơnsửatệp đính kèmdùng của  có preInsert ,preUpdatePhương pháp mỗi张bảngCập nhậtđềusẽnew date() sẽdẫn đếnThời giankhác nhau
     */

    private void preInsert(SysFile sysFile, SysFileContent sysFileContent) {
        sysFileContent.setCreateDate(sysFile.getCreateDate());
        sysFileContent.setUpdateDate(sysFile.getUpdateDate());
        sysFileContent.setCreateBy(sysFile.getCreateBy());
        sysFileContent.setUpdateBy(sysFile.getUpdateBy());
    }

    /**
     * Xóa
     * @param entity
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(SysFile sysFile) {
        /* Xử lý bảng con     Bảng byte tệp đính kèm */
        List<Parameter> params = SearchParamsBuilder.create().eq("id", sysFile.getId()).build();
        PageRequest pageRequest = new PageRequest(params);
        sysFile.setSysFileContentList(sysFileContentDao.listAll(pageRequest));        

        if(sysFile.getSysFileContentList() != null && sysFile.getSysFileContentList().size() > 0) {
            sysFileContentDao.bulkDelete(sysFile.getSysFileContentList());
        }

        int rows = super.delete(sysFile);
        return rows;
    }

    @Transactional(readOnly = false)
    public void delete(String[] ids) {
        // TheomảngID, Xóatệp đính kèm
        dao.deleteByIds(ids);
        // TheomảngID, Xóatệp đính kèmlưu trữbảng
        sysFileContentDao.deleteByIds(ids);
    }

    /**
     * Tạo nhật ký thao tác
     * @param actionTypeId  Loại thao tácId
     * @param entity        Đối tượng thực thể được thao tác
     * @return
     */
    @Override
    protected Action createAction(String actionTypeId, SysFile entity) {
        Action action = super.createAction(actionTypeId, entity);
        if(action == null) {
            return null;
        }
        // Khi xóa, ghi lại và lưu dữ liệu đã lưu vào thùng rác
        if(ActionConstants.ACTION_DELETED.equals(actionTypeId)) {   
            for(SysFileContent child: entity.getSysFileContentList()) {
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