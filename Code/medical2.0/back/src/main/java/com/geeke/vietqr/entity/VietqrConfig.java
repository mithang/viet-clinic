package com.geeke.vietqr.entity;

import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import org.hibernate.validator.constraints.Length;

/**
 * Cấu hình tài khoản nhận VietQR của một phòng khám (G4 research loop).
 *
 * Đây KHÔNG phải credential: số tài khoản nhận tiền là thông tin công khai
 * với người chuyển khoản (in trên mã QR để bệnh nhân quét), không có mật
 * khẩu, token hay khóa nào được lưu ở đây. Mặc định bảng trống = chưa cấu
 * hình; frontend khi đó không vẽ mã QR nào.
 *
 * Một tenant chỉ có một dòng del_flag = '0' đang hiệu lực; service luôn
 * UPDATE dòng đó thay vì INSERT thêm.
 */
public class VietqrConfig extends DataEntity<VietqrConfig> {

    private static final long serialVersionUID = 1L;

    private Company company;
    private String bankBin;
    private String bankName;
    private String accountNo;
    private String accountName;

    public VietqrConfig() {
        super();
    }

    public VietqrConfig(String id) {
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

    @Length(min = 6, max = 6, message = "Mã BIN ngân hàng phải đúng 6 chữ số")
    public String getBankBin() {
        return bankBin;
    }

    public void setBankBin(String bankBin) {
        this.bankBin = bankBin;
    }

    @Length(min = 0, max = 50, message = "Tên ngân hàng độ dài phải nằm trong khoảng 0 đến 50")
    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    @Length(min = 6, max = 20, message = "Số tài khoản độ dài phải nằm trong khoảng 6 đến 20")
    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    @Length(min = 1, max = 50, message = "Tên chủ tài khoản độ dài phải nằm trong khoảng 1 đến 50")
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
