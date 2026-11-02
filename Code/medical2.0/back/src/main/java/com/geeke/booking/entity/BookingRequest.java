package com.geeke.booking.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Yêu cầu đặt lịch online (G2 research loop).
 *
 * Một bản ghi = một yêu cầu đặt lịch của bệnh nhân, KHÔNG cần tài khoản.
 * Yêu cầu ở trạng thái pending KHÔNG BAO GIỜ ghi dữ liệu khám chữa:
 * intake chỉ INSERT vào bảng này; không chẩn đoán (entity không có
 * trường chẩn đoán nào), không đọc/ghi patient hay registration.
 * Khi lễ tân xác nhận, BookingRequestService tạo một dòng registration
 * mang subscribe_date/subscribe_project (lịch hẹn thật) và ghi
 * registration_id vào đây; khi từ chối, giữ lại reject_reason.
 *
 * Trạng thái: '0' = chờ xử lý (còn trong hàng chờ),
 * '1' = đã xác nhận (đã có lịch hẹn thật), '2' = đã từ chối.
 */
public class BookingRequest extends DataEntity<BookingRequest> {

    private static final long serialVersionUID = 1L;

    private Company company;
    private String patientName;
    private String phone;
    private Date wantedDate;
    private String service;
    private String status;
    private String rejectReason;
    private Long registrationId;

    /**
     * Chỉ dùng ở response intake: true = yêu cầu đã tồn tại (trả lại bản
     * ghi cũ, không tạo dòng mới). Không có cột DB, không nằm trong
     * INSERT/UPDATE của mapper.
     */
    private Boolean duplicate;

    public BookingRequest() {
        super();
        this.status = "0";
    }

    public BookingRequest(String id) {
        super(id);
    }

    @NotNull(message = "Phòng khámIDKhông được để trống")
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

    @Length(min = 1, max = 100, message = "Tên người đặt lịch độ dài phải nằm trong khoảng 1 đến 100")
    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    @Length(min = 1, max = 20, message = "Số điện thoại độ dài phải nằm trong khoảng 1 đến 20")
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    public Date getWantedDate() {
        return wantedDate;
    }

    public void setWantedDate(Date wantedDate) {
        this.wantedDate = wantedDate;
    }

    @Length(min = 0, max = 100, message = "Dịch vụ độ dài phải nằm trong khoảng 0 đến 100")
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Length(min = 0, max = 255, message = "Lý do từ chối độ dài phải nằm trong khoảng 0 đến 255")
    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    // M1: registrationId là Snowflake BIGINT — serialize thành chuỗi
    // như confirm() vốn đã trả String, nếu không JS làm tròn float64
    // và phiếu in lại từ prior sẽ sai mã (đã tái hiện: ...0970 thành
    // ...1000). Tiền lệ: ToStringSerializer ở PersonalTheme/SysFile.
    @JsonSerialize(using = ToStringSerializer.class)
    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public Boolean getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(Boolean duplicate) {
        this.duplicate = duplicate;
    }

    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "booking_request";
    }

    @Override
    @JsonIgnore
    public String getBusTableId() {
        // Không có router riêng: UI nằm trên trang lịch hẹn hiện có
        // (code outpatient/scheduling), nên log thao tác gắn vào router đó.
        return "1258355633108674030";
    }

    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
        return true;
    }
}
