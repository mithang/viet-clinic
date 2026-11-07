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
import com.geeke.common.service.ServiceException;
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

    // DS126 A: live delete is now blocked entirely (DS125 D only closed
    // the silent no-op class: ghost / already-deleted / blank -> 400,
    // but the LIVE fixture row could still be soft-deleted by any token,
    // 200 -- probe-proven in tools/ds126a-verify.py B2). System config:
    // 1 live row (id 613672228698857510, never touched); saveWithFile is
    // the UI path and bulk* already throw. No internal caller calls
    // delete() on this service (verified by grep); the login page only
    // reads listAll. Same fail-closed shape as DS72
    // MedicinalStockRecordService.
    @Override
    @Transactional(readOnly = false)
    public int delete(SysSeting entity) {
        throw new ServiceException("Cấu hình hệ thống chỉ sửa từng mục qua trang Cài đặt, không hỗ trợ xóa trực tiếp!");
    }

    @Transactional(readOnly = false)
    public SysSeting save(SysSeting entity, MultipartFile[] multipartFiles) {
        // DS87: the six *Index fields are transient (no such columns exist
        // in sys_seting; they index into the attachments array). A missing
        // index unboxed to NPE at getProjectLogoIndex() >= 0 -> 500 (inside
        // the try below, rethrown as RuntimeException), and an index past
        // the attachments array AIOOBE'd the same way. Missing means "no
        // file selected for that slot" -> default -1 (keep existing logo);
        // out-of-range is a malformed request -> 400, checked BEFORE any
        // write so the transaction has nothing to roll back.
        if (entity == null) {
            throw new ServiceException("Cài đặt hệ thống không được để trống");
        }
        if (entity.getProjectLogoIndex() == null) {
            entity.setProjectLogoIndex(-1);
        }
        if (entity.getLoginLogoIndex() == null) {
            entity.setLoginLogoIndex(-1);
        }
        if (entity.getSysLogoIndex() == null) {
            entity.setSysLogoIndex(-1);
        }
        if (entity.getFaviconIndex() == null) {
            entity.setFaviconIndex(-1);
        }
        if (entity.getLoginBgIndex() == null) {
            entity.setLoginBgIndex(-1);
        }
        if (entity.getLoginGraphIndex() == null) {
            entity.setLoginGraphIndex(-1);
        }
        if ((entity.getProjectLogoIndex() >= 0
                && (multipartFiles == null || entity.getProjectLogoIndex() >= multipartFiles.length))
                || (entity.getLoginLogoIndex() >= 0
                && (multipartFiles == null || entity.getLoginLogoIndex() >= multipartFiles.length))
                || (entity.getSysLogoIndex() >= 0
                && (multipartFiles == null || entity.getSysLogoIndex() >= multipartFiles.length))
                || (entity.getFaviconIndex() >= 0
                && (multipartFiles == null || entity.getFaviconIndex() >= multipartFiles.length))
                || (entity.getLoginBgIndex() >= 0
                && (multipartFiles == null || entity.getLoginBgIndex() >= multipartFiles.length))
                || (entity.getLoginGraphIndex() >= 0
                && (multipartFiles == null || entity.getLoginGraphIndex() >= multipartFiles.length))) {
            throw new ServiceException("Chỉ số tệp đính kèm vượt quá số tệp đã gửi");
        }

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
            // E3: không nuốt lỗi thành false-success nữa — trước đây NPE ở
            // setFileAndFileByte (content null) vẫn trả 200 success, trong khi
            // logo chưa lưu và lần save thứ hai không chạy. Ném lại để
            // GlobalExceptionHandler trả lỗi 500 và transaction rollback lần
            // save đầu, giữ nguyên trạng thái cài đặt khi lưu file thất bại.
            logger.error("Cài đặt hệ thốngLưungoại lệ", e);
            throw new RuntimeException("Cài đặt hệ thốngLưuthất bại", e);
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
            // E3: SysFile có dòng nhưng sys_file_content có thể mất (0 dòng
            // active sau re-import) → get() trả null, add null rồi get(0)
            // deref NPE ở :144. Thiếu content thì tạo mới như nhánh id trống:
            // SysFileService.save sẽ insert với id = id của SysFile.
            // Kèm theo: preInsert của SysFileService copy create_by từ SysFile
            // đang lưu; SysFile từ JSON chỉ có id nên createBy null → insert
            // content mới vi phạm NOT NULL. Kế thừa audit fields từ chính
            // dòng SysFile đã lưu (dữ liệu của nó, không đổi quyền/tenant).
            // get() không lọc del_flag nên có thể trả dòng đã xóa mềm:
            // bulkUpdate của SysFileContent không ghi del_flag nên tái dùng
            // dòng đó không bao giờ hồi sinh được, còn insert content mới
            // cùng id sẽ đụng PK của dòng đã xóa mềm. Trường hợp này tạo
            // dòng SysFile mới như nhánh id trống; caller cập nhật FK của
            // cài đặt sang file mới, dòng file cũ giữ nguyên không mất.
            SysFileContent existing = sysFileContentService.get(sysFile.getId());
            if (existing != null && "0".equals(existing.getDelFlag())) {
                sysFileContentList.add(existing);
            } else if (existing != null) {
                sysFile = new SysFile();
                sysFileContentList.add(new SysFileContent());
            } else {
                sysFileContentList.add(new SysFileContent());
                SysFile persisted = sysFileService.get(sysFile.getId());
                if (persisted != null) {
                    sysFile.setCreateBy(persisted.getCreateBy());
                    sysFile.setCreateDate(persisted.getCreateDate());
                }
            }
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

    // DS48: bulk* fail-closed 400 (DS40 service-gate pattern), NOT new
    // mapper statements. sys_seting holds exactly 1 live row (singleton
    // global config, no company_id, no per-op resource) and is edited
    // only via save/saveWithFile; bulk semantics are meaningless there,
    // and the generic bulk floor is only hasAnySessionPermission -- so
    // adding statements would open a new working batch-write path to
    // global config for any permissioned session. The generic bulk
    // endpoints stay reachable but now answer an intentional 400
    // instead of an accidental BindingException 500. No internal
    // caller uses bulk* on this service (only the generic controller).
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<SysSeting> entitys) {
        throw new ServiceException("Cấu hình hệ thống chỉ sửa từng mục qua trang Cài đặt, không hỗ trợ thao tác hàng loạt!");
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<SysSeting> entitys) {
        throw new ServiceException("Cấu hình hệ thống chỉ sửa từng mục qua trang Cài đặt, không hỗ trợ thao tác hàng loạt!");
    }

    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<SysSeting> entitys) {
        throw new ServiceException("Cấu hình hệ thống chỉ sửa từng mục qua trang Cài đặt, không hỗ trợ thao tác hàng loạt!");
    }
}