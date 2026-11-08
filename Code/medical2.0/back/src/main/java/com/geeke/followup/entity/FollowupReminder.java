package com.geeke.followup.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Nhắc lịch tái khám nội bộ (G1 research loop).
 *
 * Một bản ghi = một lịch hẹn tái khám cần nhắc cho một bệnh nhân.
 * Nguồn sự thật cho danh sách đến hạn/quá hạn và job tạo thông báo nội
 * bộ hằng ngày. Không gửi SMS/Zalo, không ra ngoài hệ thống.
 *
 * Trạng thái: '0' = chờ xử lý (còn nằm trong danh sách đến hạn),
 * '1' = đã xác nhận (rời khỏi danh sách đến hạn).
 * lastNoticeDate: ngày đã tạo thông báo nội bộ gần nhất — job chỉ tạo
 * tối đa một thông báo cho một nhắc lịch trong một ngày.
 */
public class FollowupReminder extends DataEntity<FollowupReminder> {

    private static final long serialVersionUID = 1L;

    private Company company;
    private Long patientId;
    private String patientName;
    private String followupType;
    private Date nextDate;
    private String status;
    private Date lastNoticeDate;

    /**
     * Số ngày quá hạn tính tại thời điểm truy vấn (chỉ dùng để hiển thị,
     * không lưu DB, không nằm trong INSERT/UPDATE). 0 = đến hạn hôm nay.
     */
    private Integer daysOverdue;

    public FollowupReminder() {
        super();
        this.status = "0";
    }

    public FollowupReminder(String id) {
        super(id);
    }

    @NotNull(message = "Phòng khám ID Không được để trống")
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

    @NotNull(message = "Bệnh nhân không được để trống")
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    // DS79: @Length(min=1) cho phep null lot qua @Valid roi 500 o cot NOT NULL.
    @NotNull(message = "Tên bệnh nhân không được để trống")
    @Length(min = 1, max = 100, message = "Tên bệnh nhân độ dài phải nằm trong khoảng 1 đến 100")
    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    // DS79: xem getPatientName.
    @NotNull(message = "Loại tái khám không được để trống")
    @Length(min = 1, max = 50, message = "Loại tái khám độ dài phải nằm trong khoảng 1 đến 50")
    public String getFollowupType() {
        return followupType;
    }

    public void setFollowupType(String followupType) {
        this.followupType = followupType;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getNextDate() {
        return nextDate;
    }

    public void setNextDate(Date nextDate) {
        this.nextDate = nextDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    public Date getLastNoticeDate() {
        return lastNoticeDate;
    }

    public void setLastNoticeDate(Date lastNoticeDate) {
        this.lastNoticeDate = lastNoticeDate;
    }

    public Integer getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(Integer daysOverdue) {
        this.daysOverdue = daysOverdue;
    }

    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "followup_reminder";
    }

    @Override
    @JsonIgnore
    public String getBusTableId() {
        // Không có router riêng: UI nằm trên trang workbench của bác sĩ
        // (code outpatient/workbench), nên log thao tác gắn vào router đó.
        return "1258355633108674017";
    }

    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
        return true;
    }
}
