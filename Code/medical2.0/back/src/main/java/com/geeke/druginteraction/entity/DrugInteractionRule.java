package com.geeke.druginteraction.entity;

import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.hibernate.validator.constraints.Length;

/**
 * Quy tắc tương tác thuốc do chính phòng khám tự cấu hình (G5 research loop).
 *
 * Đây KHÔNG phải cơ sở dữ liệu tương tác lâm sàng hoàn chỉnh: bảng được
 * tạo trống, không seed quy tắc nào. Cảnh báo ở màn hình kê đơn chỉ bao
 * phủ đúng các quy tắc phòng khám đã tự nhập qua API; chưa cấu hình thì
 * không có cảnh báo nào, và đó là trạng thái trung thực, không phải lỗi.
 * Cảnh báo chỉ mang tính tham khảo, không chặn lưu đơn — bác sĩ quyết định.
 */
public class DrugInteractionRule extends DataEntity<DrugInteractionRule> {

    private static final long serialVersionUID = 1L;

    private Company company;
    private String drugAName;
    private String drugBName;
    private String severity;
    private String message;

    public DrugInteractionRule() {
        super();
    }

    public DrugInteractionRule(String id) {
        super(id);
    }

    public Company getCompany() {
        if (company == null) {
            String tenantId = SessionUtils.getLoginTenantId();
            if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
                Company tenant = new Company();
                tenant.setId(tenantId);
                return tenant;
            }
        }
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Length(min = 1, max = 100, message = "Tên thuốc A độ dài phải nằm trong khoảng 1 đến 100")
    public String getDrugAName() {
        return drugAName;
    }

    public void setDrugAName(String drugAName) {
        this.drugAName = drugAName;
    }

    @Length(min = 1, max = 100, message = "Tên thuốc B độ dài phải nằm trong khoảng 1 đến 100")
    public String getDrugBName() {
        return drugBName;
    }

    public void setDrugBName(String drugBName) {
        this.drugBName = drugBName;
    }

    @Length(min = 1, max = 20, message = "Mức độ độ dài phải nằm trong khoảng 1 đến 20")
    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @Length(min = 1, max = 500, message = "Nội dung cảnh báo độ dài phải nằm trong khoảng 1 đến 500")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
