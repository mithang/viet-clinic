package com.geeke.sys.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.geeke.sys.dao.SysFileContentDao;
import com.geeke.sys.entity.SysFile;
import com.geeke.sys.entity.SysFileContent;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geeke.common.service.CrudService;
import com.geeke.sys.dao.SysSetingDao;
import com.geeke.sys.entity.SysSeting;
import org.springframework.web.multipart.MultipartFile;

/**
 * Cài đặt hệ thốngService
 *
 * @author szy
 * @version 2021-08-26
 */

@Service("sysSetingService")
public class SysSetingService extends CrudService<SysSetingDao, SysSeting> {


    @Autowired
    private SysFileContentService sysFileContentService; //Hệ thốngTệplưu trữbytedịch vụ

    @Autowired
    private SysFileService sysFileService;  //Hệ thốngTệpdịch vụ


    /**
     * LưuCài đặt hệ thống
     *
     * @param entity
     * @return
     */

    @Transactional(readOnly = false)
    public SysSeting save(SysSeting entity, MultipartFile[] multipartFiles) {

        //lưu trữCài đặt hệ thốngcơ bảnThông tin
        String id = super.save(entity).getId();
        try {
            if (StringUtils.isNotBlank(id)) {
                //Cờ hạng mục
                Integer projectLogoIndex = entity.getProjectLogoIndex();
                if(projectLogoIndex >= 0 ) {
                    SysFile sysFile = entity.getProjectLogo();
                    sysFile = setFileAndFileByte(multipartFiles[projectLogoIndex], id, sysFile);
                    entity.setProjectLogo(sysFile);
                }else {
                    entity.setProjectLogo(entity.getProjectLogo());
                }
                //Cờ đăng nhập
                Integer loginLogoIndex = entity.getLoginLogoIndex();
                if(loginLogoIndex >= 0 ) {
                    SysFile sysFile = entity.getLoginLogo();
                    sysFile = setFileAndFileByte(multipartFiles[loginLogoIndex], id, sysFile);
                    entity.setLoginLogo(sysFile);
                }else {
                    entity.setLoginLogo(entity.getLoginLogo());
                }
                // Cờ hệ thống
                Integer sysLogoIndex = entity.getSysLogoIndex();
                if(sysLogoIndex >= 0 ) {
                    SysFile sysFile = entity.getSysLogo();
                    sysFile = setFileAndFileByte(multipartFiles[sysLogoIndex], id, sysFile);
                    entity.setSysLogo(sysFile);
                }else {
                    entity.setSysLogo(entity.getSysLogo());
                }
                // Biểu tượng hệ thống
                Integer faviconIndex = entity.getFaviconIndex();
                if(faviconIndex >= 0 ) {
                    SysFile sysFile = entity.getFavicon();
                    sysFile = setFileAndFileByte(multipartFiles[faviconIndex], id, sysFile);
                    entity.setFavicon(sysFile);
                }else {
                    entity.setFavicon(entity.getFavicon());
                }

                //  Ảnh nền đăng nhập
                Integer loginBgIndex = entity.getLoginBgIndex();
                if(loginBgIndex >= 0 ) {
                    SysFile sysFile = entity.getLoginBg();
                    sysFile = setFileAndFileByte(multipartFiles[loginBgIndex], id, sysFile);
                    entity.setLoginBg(sysFile);
                }else {
                    entity.setLoginBg(entity.getLoginBg());
                }

                //  Ảnh nền đăng nhập
                Integer loginGraphIndex = entity.getLoginGraphIndex();
                if(loginGraphIndex >= 0 ) {
                    SysFile sysFile = entity.getLoginGraph();
                    sysFile = setFileAndFileByte(multipartFiles[loginGraphIndex], id, sysFile);
                    entity.setLoginGraph(sysFile);
                }else {
                    entity.setLoginGraph(entity.getLoginGraph());
                }
                super.save(entity);
            } else {
                logger.info("Cài đặt hệ thốngLưuThất bại");
            }
        } catch (Exception e) {
            logger.error("Cài đặt hệ thốngLưungoại lệ", e);
        }
        return entity;
    }

    /**
     *
     * @param multipartFile TệpĐối tượng
     * @param id  Cài đặt hệ thốngid
     * @param sysFile Tệp
     * @return
     * @throws IOException
     */
    private SysFile setFileAndFileByte(MultipartFile multipartFile, String id, SysFile sysFile) throws IOException {
        List<SysFileContent>  sysFileContentList= Lists.newArrayList();
        if(sysFile == null || StringUtils.isBlank(sysFile.getId())) {
            sysFile = new SysFile();
            sysFileContentList.add(new SysFileContent());
        }else {
            sysFileContentList.add(sysFileContentService.get(sysFile.getId()));
        }
        MultipartFile file = multipartFile;

        sysFile.setObjectId(Long.parseLong(id));
        sysFile.setName(file.getOriginalFilename());
        sysFile.setFileType(file.getContentType()) ;
        sysFile.setFileSize(file.getSize());
        InputStream is = file.getInputStream();
        byte[] fContent = new byte[(int) file.getSize()];
        is.read(fContent);

        sysFileContentList.get(0).setFileByte(fContent);
        sysFileContentList.get(0).setFileType(file.getContentType());
        sysFile.setSysFileContentList(sysFileContentList);

        //Trả vềTệpid
        SysFile sysFileTemp =  sysFileService.save(sysFile);
        return sysFileTemp;
    }
}