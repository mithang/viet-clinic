package com.geeke.sys.service;

import cn.hutool.core.util.ObjectUtil;
import com.geeke.common.data.Page;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.sys.dao.PersonalThemeDao;
import com.geeke.sys.dao.SysParamConfigDao;
import com.geeke.sys.entity.PersonalTheme;
import com.geeke.sys.entity.SysParamConfig;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Hệ thốngTham Sốcấu hìnhService
 * @author lys
 * @version 2021-07-16
 */
 
@Service("sysParamConfigService")
public class SysParamConfigService extends CrudService<SysParamConfigDao, SysParamConfig>{

    /**
     * N3e: chặn ghi cấu hình tham số của tenant khác theo chuỗi companyId.
     * SysParamConfig KHÔNG có company dạng đối tượng được mapper map (mapper
     * chỉ map chuỗi a.company_id AS "companyId", không JOIN company), nên
     * cả hai guard generic của CrudService đều luôn pass ở đây:
     * verifyTenantOwnership đọc entity.getCompany() (DataEntity.company,
     * null khi load từ DB, hoặc object do caller tự gắn vào body) và
     * verifyTenantOwnershipAgainstStoredRow đọc stored.getCompany() (luôn
     * null) — probe N3 đã chứng minh: saveList/save/delete/get/listPage
     * đều xuyên tenant, trong đó case P1c mang company object OWN (decoy)
     * + companyId FOREIGN vẫn 200. Guard này vì vậy KHÔNG BAO GIỜ tin
     * company object, chỉ so chuỗi companyId với tenant phiên:
     * - Body: companyId khác rỗng mà khác tenant phiên → 400. Phủ cả
     *   insert (companyId FOREIGN) và update đổi company_id sang FOREIGN.
     * - Stored row (id khác rỗng): dao.get(id) đọc trực tiếp một bảng,
     *   không JOIN (mapper get() là SELECT đơn bảng sys_param_config nên
     *   đáng tin — đã đối chiếu trực tiếp cột DB company_id với companyId
     *   API trả về, khớp nhau; khác bài học N1 ở review nơi get() INNER
     *   JOIN registration nên trả null), companyId stored khác tenant →
     *   400. Phủ cả saveList/save khi body không mang companyId (P1) và
     *   delete khi body chỉ có id (P2) — hai đường mà chỉ kiểm tra body
     *   sẽ bỏ lọt.
     * - Insert/update với companyId rỗng, id không tồn tại (stored null),
     *   stored companyId rỗng, hoặc phiên không session (tenant rỗng/
     *   "null", luồng nội bộ): cho qua như cũ — parity pre-fix (saveList
     *   thiếu companyId vốn 500 NOT NULL từ DB, save thiếu company object
     *   vốn 400 @Valid; guard không biến các trường hợp đó thành 400 mới
     *   mang nhãn tenant, và không chặn dòng mồ côi theo semantics cũ).
     * - Bulk: tiền kiểm TOÀN BỘ list trước khi ghi dòng nào — fail-closed
     *   cả list (mẫu N1 saveList): một dòng lạ thì không dòng nào được ghi.
     * Ném ServiceException (400) như verifyTenantOwnership của CrudService.
     * Không sửa CrudService generic, không sửa mapper (đúng lập luận
     * L1/M1: sửa generic rủi ro phá dict dùng chung và luồng nội bộ).
     */
    public void checkParamWriteTenant(SysParamConfig entity) {
        if (entity == null) {
            return;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        if (StringUtils.isNotBlank(entity.getCompanyId())
                && !tenantId.equals(entity.getCompanyId())) {
            throw new ServiceException("Không có quyền truy cập tenant khác");
        }
        if (StringUtils.isNotBlank(entity.getId())) {
            SysParamConfig stored = dao.get(entity.getId());
            if (stored != null && StringUtils.isNotBlank(stored.getCompanyId())
                    && !tenantId.equals(stored.getCompanyId())) {
                throw new ServiceException("Không có quyền truy cập tenant khác");
            }
        }
    }

    @Override
    public SysParamConfig save(SysParamConfig entity) {
        // DS247: defence-in-depth guard null (mau DS127 B o
        // RecipelInfoReviewService.save): save/list [null] truoc day
        // NPE 500 o entity.getId() ben duoi; pre-check o controller la
        // guard chinh, guard nay bao ve caller truc tiep/noi bo.
        if (ObjectUtil.isNull(entity)) {
            throw new ServiceException("Cấu hình tham số không được để trống");
        }
        checkParamWriteTenant(entity);
        // DS102: CrudService.save rẽ nhánh update khi id khác rỗng,
        // doUpdate không động đến dòng nào nếu id không tồn tại mà vẫn
        // trả 200 (probe B1; họ silent no-op DS94/DS99). Chỉ chấp nhận
        // update khi dòng đã tồn tại; tạo mới phải gửi id rỗng. Kiểm tra
        // bằng dao.get: mapper get() là SELECT đơn bảng sys_param_config,
        // không JOIN nên đáng tin (khác bài học N1/N3d ở các mapper JOIN).
        if (StringUtils.isNotBlank(entity.getId())
                && dao.get(entity.getId()) == null) {
            throw new ServiceException("Cấu hình tham số không tồn tại: "
                    + entity.getId());
        }
        return super.save(entity);
    }

    @Override
    public int delete(SysParamConfig entity) {
        checkParamWriteTenant(entity);
        return super.delete(entity);
    }

    /**
     * DS102: bulkInsert/bulkUpdate kế thừa từ CrudService đi thẳng qua dao,
     * bỏ qua các guard @NotNull của save() (config_name/company_id đều
     * NOT NULL ở sys_param_config; mapper bulk ghi trực tiếp) nên hàng
     * thiếu trường 500 ở DB (probe B2/B3/B7; tiền lệ DS99/DS100). Ghi đè
     * ở đây để fail-closed 400 trước mọi ghi: danh sách null/rỗng, trường
     * bắt buộc. category/config_value nullable nên không kiểm tra.
     * bulkUpdate thêm kiểm tra id rỗng/không tồn tại (mapper CASE chạm 0
     * dòng mà vẫn 200 — probe B4, họ DS94/DS99). bulkDelete chỉ chặn danh
     * sách null/rỗng; id không tồn tại giữ 200 rows 0 IDEMPOTENT theo
     * tiền lệ DS99 G11. (Danh sách rỗng vốn đã 400 nhờ handler BadSqlGrammar
     * toàn cục DS94 — guard ở service chỉ làm rõ message, không đổi status.)
     */
    private void validateRequired(SysParamConfig entity) {
        if (entity == null) {
            throw new ServiceException("Cấu hình tham số không được để trống");
        }
        if (StringUtils.isBlank(entity.getConfigName())) {
            throw new ServiceException("Tên hạng mục cấu hình không được để trống");
        }
        if (StringUtils.isBlank(entity.getCompanyId())) {
            throw new ServiceException("Công ty của cấu hình tham số không được để trống");
        }
    }

    @Override
    public List<String> bulkInsert(List<SysParamConfig> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách cấu hình tham số thêm mới không được để trống");
        }
        for (SysParamConfig entity : entitys) {
            checkParamWriteTenant(entity);
            validateRequired(entity);
        }
        return super.bulkInsert(entitys);
    }

    @Override
    public List<String> bulkUpdate(List<SysParamConfig> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách cấu hình tham số cập nhật không được để trống");
        }
        for (SysParamConfig entity : entitys) {
            checkParamWriteTenant(entity);
            validateRequired(entity);
            if (StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của cấu hình tham số đã tồn tại");
            }
            if (dao.get(entity.getId()) == null) {
                throw new ServiceException("Cấu hình tham số không tồn tại: "
                        + entity.getId());
            }
        }
        return super.bulkUpdate(entitys);
    }

    @Override
    public int bulkDelete(List<SysParamConfig> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách cấu hình tham số xóa không được để trống");
        }
        if (entitys != null) {
            for (SysParamConfig entity : entitys) {
                checkParamWriteTenant(entity);
            }
        }
        return super.bulkDelete(entitys);
    }

    /**
     * N3e: chặn đọc dòng lạ — generic get() chỉ verify trên company object
     * (null với entity này) nên luôn pass; kiểm tra thêm chuỗi companyId
     * của stored row. id không tồn tại (null) hoặc phiên không session:
     * cho qua như cũ (200 data null).
     */
    @Override
    public SysParamConfig get(String id) {
        SysParamConfig entity = super.get(id);
        if (entity != null && StringUtils.isNotBlank(entity.getCompanyId())) {
            String tenantId = SessionUtils.getLoginTenantId();
            if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)
                    && !tenantId.equals(entity.getCompanyId())) {
                throw new ServiceException("Không có quyền truy cập tenant khác");
            }
        }
        return entity;
    }

    /**
     * N3e: ép lọc đọc theo session tenant — ensureCompanyFilter của
     * CrudService chỉ áp khi entity khai báo field "company" riêng, mà
     * SysParamConfig chỉ có chuỗi companyId nên listPage/listAll trước đây
     * trả toàn bộ tenant (probe P5b: 11 dòng gồm 2 company; và client còn
     * tự chọn được company_id FOREIGN qua params). Cách làm: loại bỏ mọi
     * company_id do client gửi rồi thêm lại đúng tenant phiên — luồng
     * trang thật (gửi company_id = công ty mình) không đổi kết quả.
     * Không có nhánh dùng chung cho entity này: 8 dòng live đều mang
     * company_id tenant cụ thể, không có dòng NULL/0, giá trị khác nhau
     * theo tenant (normalDays 7 vs 10, retailPrice false vs true), SQL
     * list không có OR institution như họ dict dùng chung (L8) — nên lọc
     * own-only là đúng, không áp institution-shared và không tự quyết
     * policy dùng chung nào. Phiên không session: giữ hành vi cũ (không
     * lọc) để không phá luồng nội bộ.
     */
    private List<Parameter> tenantFilteredParams(List<Parameter> parameters) {
        List<Parameter> params = parameters == null
                ? Lists.<Parameter>newArrayList() : Lists.newArrayList(parameters);
        Parameter.extractAndRemoveCompanyId(params);
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
            params.add(new Parameter("company_id", "=", tenantId));
        }
        return params;
    }

    @Override
    public Page<SysParamConfig> listPage(List<Parameter> parameters, int offset, int limit, String orderby) {
        return super.listPage(tenantFilteredParams(parameters), offset, limit, orderby);
    }

    @Override
    public List<SysParamConfig> listAll(List<Parameter> parameters, String orderby) {
        return super.listAll(tenantFilteredParams(parameters), orderby);
    }

}
