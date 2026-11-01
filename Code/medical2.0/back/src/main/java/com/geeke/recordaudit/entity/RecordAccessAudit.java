package com.geeke.recordaudit.entity;

import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import org.hibernate.validator.constraints.Length;

/**
 * Một dòng nhật ký truy cập hồ sơ bệnh nhân (G8 research loop).
 *
 * Append-only: backend chỉ INSERT (khi xem/sửa hồ sơ) và SELECT (khi xem
 * nhật ký); không có endpoint xóa hay sửa dòng audit. patientId NULL
 * nghĩa là lượt xem DANH SÁCH (LIST) — ghi đúng 1 dòng cho cả lần mở,
 * không ghi theo từng bệnh nhân trong danh sách. patientName là ảnh chụp
 * tại thời điểm ghi để nhật ký không phụ thuộc vào join về sau.
 */
public class RecordAccessAudit extends DataEntity<RecordAccessAudit> {

    private static final long serialVersionUID = 1L;

    private Company company;
    private String userId;
    private String userLoginName;
    private String userName;
    private String patientId;
    private String patientName;
    private String action;

    public RecordAccessAudit() {
        super();
    }

    public RecordAccessAudit(String id) {
        super(id);
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Length(min = 0, max = 64, message = "ID người truy cập độ dài phải nằm trong khoảng 0 đến 64")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Length(min = 0, max = 100, message = "Tên đăng nhập độ dài phải nằm trong khoảng 0 đến 100")
    public String getUserLoginName() {
        return userLoginName;
    }

    public void setUserLoginName(String userLoginName) {
        this.userLoginName = userLoginName;
    }

    @Length(min = 0, max = 100, message = "Họ tên người truy cập độ dài phải nằm trong khoảng 0 đến 100")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    @Length(min = 0, max = 100, message = "Tên bệnh nhân độ dài phải nằm trong khoảng 0 đến 100")
    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    @Length(min = 1, max = 20, message = "Hành động độ dài phải nằm trong khoảng 1 đến 20")
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
