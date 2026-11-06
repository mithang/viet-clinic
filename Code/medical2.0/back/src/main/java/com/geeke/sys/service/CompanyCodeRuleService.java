package com.geeke.sys.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.data.Page;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.sys.dao.CompanyCodeRuleDao;
import com.geeke.sys.entity.CompanyCodeRule;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Quy tắc mã công tyService
 * @author lys
 * @version 2021-05-17
 */

@Service("companyCodeRuleService")
@Transactional(readOnly = true)
public class CompanyCodeRuleService extends CrudService<CompanyCodeRuleDao, CompanyCodeRule>{

    /**
     * N3d: chặn ghi quy tắc mã của tenant khác theo chuỗi companyId.
     * CompanyCodeRule KHÔNG có company dạng đối tượng được mapper map
     * (mapper chỉ map chuỗi a.company_id AS "companyId", không map
     * company.id), nên cả hai guard generic của CrudService đều luôn
     * pass ở đây: verifyTenantOwnership đọc entity.getCompany() (null
     * với dòng load từ DB, hoặc object do caller tự gắn vào body) và
     * verifyTenantOwnershipAgainstStoredRow đọc stored.getCompany()
     * (luôn null) — probe N3 đã chứng minh: save-update/delete/get/
     * listPage đều xuyên tenant. Guard này vì vậy KHÔNG BAO GIỜ tin
     * company object, chỉ so chuỗi companyId với tenant phiên:
     * - Body: companyId khác rỗng mà khác tenant phiên → 400. Phủ cả
     *   insert (companyId FOREIGN) và update mang companyId FOREIGN.
     * - Stored row (id khác rỗng): đọc trực tiếp cột company_id bằng
     *   dao.getCompanyIdById (SELECT một bảng, không JOIN) thay vì
     *   dao.get() — mapper get() INNER JOIN sys_code_rule nên trả
     *   null với dòng mồ côi (live có 2 dòng trỏ tới sys_rule đã
     *   xóa/mất), trong khi update/delete chỉ cần WHERE id nên vẫn
     *   ghi được dòng đó; chỉ kiểm tra dao.get() sẽ bỏ lọt đúng 2
     *   dòng này (bài học N1: đối chiếu trực tiếp cột DB). Stored
     *   companyId khác tenant → 400. Phủ cả save khi body không mang
     *   companyId và delete khi body chỉ có id — hai đường mà chỉ
     *   kiểm tra body sẽ bỏ lọt.
     * - Re-home KHÔNG cần chặn riêng: mapper update chỉ set rule_def,
     *   remarks — không bao giờ đổi company_id (probe N3 R1c: update
     *   dòng lạ với body companyId OWN → 200 nhưng company_id giữ
     *   FOREIGN; update dòng own với body FOREIGN cũng giữ OWN).
     *   Chặn update/delete dòng lạ là đủ; body companyId FOREIGN trên
     *   dòng own cũng bị chặn (nghiêm hơn hành vi cũ, nhưng form thật
     *   chỉ gửi companyId của chính công ty phiên — xem B2).
     * - Insert/update với companyId rỗng, id không tồn tại (lookup
     *   null), stored companyId rỗng, hoặc phiên không session
     *   (tenant rỗng/"null", luồng nội bộ): cho qua như cũ — parity
     *   pre-fix (save thiếu company object vốn 400 @Valid; get/delete
     *   id không tồn tại vốn 200 data null/0; guard không biến các
     *   trường hợp đó thành 400 mới mang nhãn tenant, và không chặn
     *   dòng mồ côi theo semantics cũ khi nó không gắn tenant nào).
     * - Bulk: tiền kiểm TOÀN BỘ list trước khi ghi dòng nào —
     *   fail-closed cả list (mẫu N1/N3e): một dòng lạ thì không dòng
     *   nào được ghi. DS102: bulkUpdate/bulkDelete nay CÓ mapper
     *   statement (mirror update/delete đơn), nên guard tenant chạy
     *   trước SQL thật thay vì trước lỗi 500 toàn cục như trước.
     * Ném ServiceException (400) như verifyTenantOwnership của
     * CrudService. Không sửa CrudService generic, không sửa mapper
     * update/delete (đúng lập luận L1/M1: sửa generic rủi ro phá
     * dict dùng chung và luồng nội bộ); chỉ thêm một SELECT trực
     * tiếp cột company_id phục vụ guard.
     */
    public void checkRuleWriteTenant(CompanyCodeRule entity) {
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
            String storedCompanyId = dao.getCompanyIdById(entity.getId());
            if (StringUtils.isNotBlank(storedCompanyId)
                    && !tenantId.equals(storedCompanyId)) {
                throw new ServiceException("Không có quyền truy cập tenant khác");
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public CompanyCodeRule save(CompanyCodeRule companyCodeRule) {
        checkRuleWriteTenant(companyCodeRule);
        Map<String, String> colMaps = Maps.newHashMap();
        // company_id
        colMaps.clear();
        colMaps.put("company_id", "companyId");
        colMaps.put("code", "code");

        if(exists(dao, companyCodeRule, colMaps)) {
            throw new ServiceException("Đã tồn tại dữ liệu giống hệt(Công ty,Mã)");
        }

        // DS101: CrudService.save rẽ nhánh update khi id khác rỗng,
        // doUpdate không động đến dòng nào nếu id không tồn tại mà vẫn
        // trả 200 (probe U3; cùng họ silent no-op DS94/DS99). Chỉ chấp
        // nhận update khi dòng đã tồn tại; tạo mới phải gửi id rỗng.
        // Kiểm tra bằng dao.getCompanyIdById (SELECT trực tiếp cột
        // company_id) thay vì super.get(): mapper get() INNER JOIN
        // sys_code_rule nên trả null cả với dòng mồ côi còn ghi được
        // (bài học N3d), trong khi company_id NOT NULL nên lookup này
        // chỉ null khi dòng thật sự không tồn tại.
        if (StringUtils.isNotBlank(companyCodeRule.getId())
                && dao.getCompanyIdById(companyCodeRule.getId()) == null) {
            throw new ServiceException("Quy tắc mã công ty không tồn tại: "
                    + companyCodeRule.getId());
        }

        CompanyCodeRule companyCodeRuleTemp = super.save(companyCodeRule);
        return companyCodeRuleTemp;
    }

    @Override
    @Transactional(readOnly = false)
    public int delete(CompanyCodeRule entity) {
        checkRuleWriteTenant(entity);
        return super.delete(entity);
    }

    /**
     * DS101: bulkInsert kế thừa từ CrudService đi thẳng qua dao, bỏ qua
     * các guard @NotNull của save() (company_id/sys_rule_id/code/name/
     * rule_def đều NOT NULL ở sys_company_code_rule; mapper bulkInsert
     * ghi trực tiếp) nên hàng thiếu trường 500 ở DB (probe U1/U2;
     * tiền lệ DS99/DS100). Ghi đè ở đây để fail-closed 400 trước mọi
     * ghi: danh sách null/rỗng, trường bắt buộc. (DS102: bulkUpdate/
     * bulkDelete nay CÓ mapper statement — xem bulkUpdate/bulkDelete
     * bên dưới; ghi chú "không đổi" cũ ở đây đã hết hiệu lực.)
     */
    private void validateRequired(CompanyCodeRule companyCodeRule) {
        if (companyCodeRule == null) {
            throw new ServiceException("Quy tắc mã công ty không được để trống");
        }
        if (StringUtils.isBlank(companyCodeRule.getCompanyId())) {
            throw new ServiceException("Công ty của quy tắc mã không được để trống");
        }
        if (companyCodeRule.getSysRule() == null
                || StringUtils.isBlank(companyCodeRule.getSysRule().getId())) {
            throw new ServiceException("Quy tắc mã hệ thống không được để trống");
        }
        if (StringUtils.isBlank(companyCodeRule.getCode())) {
            throw new ServiceException("Mã không được để trống");
        }
        if (StringUtils.isBlank(companyCodeRule.getName())) {
            throw new ServiceException("Tên quy tắc mã công ty không được để trống");
        }
        if (StringUtils.isBlank(companyCodeRule.getRuleDef())) {
            throw new ServiceException("Quy tắc không được để trống");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<CompanyCodeRule> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách quy tắc mã thêm mới không được để trống");
        }
        if (entitys != null) {
            for (CompanyCodeRule entity : entitys) {
                checkRuleWriteTenant(entity);
                validateRequired(entity);
            }
        }
        return super.bulkInsert(entitys);
    }

    /**
     * DS102: bulkUpdate nay CÓ mapper statement (mirror update đơn: chỉ
     * rule_def + remarks), nên phải tiền kiểm như bulkInsert DS101 trước
     * khi chạm SQL: danh sách null/rỗng, id rỗng/không tồn tại (mapper
     * CASE chạm 0 dòng mà vẫn 200 — họ silent no-op DS94/DS99), ruleDef
     * rỗng (rule_def NOT NULL — CASE ghi NULL sẽ 500 ở DB). code/name/
     * sysRule không kiểm tra vì mapper không ghi chúng. Tiền kiểm toàn
     * bộ list trước khi ghi dòng nào — một dòng lỗi thì không dòng nào
     * được ghi (mẫu N1/N3e; probe A5).
     */
    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<CompanyCodeRule> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách quy tắc mã cập nhật không được để trống");
        }
        for (CompanyCodeRule entity : entitys) {
            checkRuleWriteTenant(entity);
            if (StringUtils.isBlank(entity.getId())) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của quy tắc mã đã tồn tại");
            }
            if (dao.getCompanyIdById(entity.getId()) == null) {
                throw new ServiceException("Quy tắc mã công ty không tồn tại: "
                        + entity.getId());
            }
            if (StringUtils.isBlank(entity.getRuleDef())) {
                throw new ServiceException("Quy tắc không được để trống");
            }
        }
        return super.bulkUpdate(entitys);
    }

    /**
     * DS102: bulkDelete nay CÓ mapper statement (xóa mềm del_flag = 1,
     * mirror delete đơn). Chỉ chặn danh sách null/rỗng; id không tồn tại
     * giữ nguyên 200 rows 0 IDEMPOTENT theo tiền lệ DS99 G11 (bulkDelete
     * không kiểm tra tồn tại như bulkUpdate).
     */
    @Override
    @Transactional(readOnly = false)
    public int bulkDelete(List<CompanyCodeRule> entitys) {
        if (entitys == null || entitys.isEmpty()) {
            throw new ServiceException("Danh sách quy tắc mã xóa không được để trống");
        }
        if (entitys != null) {
            for (CompanyCodeRule entity : entitys) {
                checkRuleWriteTenant(entity);
            }
        }
        return super.bulkDelete(entitys);
    }

    /**
     * N3d: chặn đọc dòng lạ — generic get() chỉ verify trên company
     * object (null với entity này) nên luôn pass, và mapper get() trả
     * null với dòng mồ côi; kiểm tra thêm company_id trực tiếp từ DB
     * để mọi dòng lạ đều 400, kể cả dòng mồ côi. id không tồn tại
     * (lookup null) hoặc phiên không session: cho qua như cũ
     * (200 data null).
     */
    @Override
    public CompanyCodeRule get(String id) {
        if (StringUtils.isNotBlank(id)) {
            String tenantId = SessionUtils.getLoginTenantId();
            if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
                String storedCompanyId = dao.getCompanyIdById(id);
                if (StringUtils.isNotBlank(storedCompanyId)
                        && !tenantId.equals(storedCompanyId)) {
                    throw new ServiceException("Không có quyền truy cập tenant khác");
                }
            }
        }
        return super.get(id);
    }

    /**
     * N3d: ép lọc đọc theo session tenant — ensureCompanyFilter của
     * CrudService chỉ áp khi entity khai báo field "company" riêng,
     * mà CompanyCodeRule chỉ có chuỗi companyId nên listPage/listAll
     * trước đây trả toàn bộ tenant (probe N3 R5b: 48 dòng gồm 7
     * company; và client còn tự chọn được company_id FOREIGN qua
     * params). Cách làm: loại bỏ mọi company_id do client gửi rồi
     * thêm lại đúng tenant phiên — luồng trang thật (gửi company_id
     * = công ty mình) không đổi kết quả.
     * Không có nhánh dùng chung cho entity này: 47 dòng live đều mang
     * company_id tenant cụ thể, không có dòng NULL/0; quy tắc dùng
     * chung nằm ở bảng catalogue sys_code_rule, còn sys_company_code_rule
     * là bản sao theo từng tenant do initCompanyCodeRule tạo cho chính
     * company phiên — nên lọc own-only là đúng, không áp
     * institution-shared và không tự quyết policy dùng chung nào.
     * Phiên không session: giữ hành vi cũ (không lọc) để không phá
     * luồng nội bộ. initCompanyCodeRule của controller vẫn chạy trước
     * list (tạo rule own nếu thiếu) và đi qua save() đã guard — rule
     * own nên pass như cũ.
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
    public Page<CompanyCodeRule> listPage(List<Parameter> parameters, int offset, int limit, String orderby) {
        return super.listPage(tenantFilteredParams(parameters), offset, limit, orderby);
    }

    @Override
    public List<CompanyCodeRule> listAll(List<Parameter> parameters, String orderby) {
        return super.listAll(tenantFilteredParams(parameters), orderby);
    }

}
