package com.geeke.org.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Thông tin khoaEntity
 * @author ch
 * @version 2022-06-14
 */
@Data
public class ClinicOffice extends DataEntity<ClinicOffice> {

    private static final long serialVersionUID = 1026680647570219053L;
    private Company company;      // thuộc vềPhòng khám
    private String code;		// Khoamã số
    private String isLocked;		// Vô hiệu hóa(0: ChưaVô hiệu hóa; 1: Vô hiệu hóa)
    private String sort;		// Sắp xếp
    private String category;		// KhoalớnLớp
    private String address;		// Địa chỉ
    private String itro;         //giới thiệu
    private String deptResperName;//Khoangười phụ trách
    private String deptResperTel;//Khoangười phụ tráchSố điện thoại
    private LocalDateTime deptEstbdat;//KhoathànhlậpNgày
    private String aprvBedCnt;//lôchuẩngiườngvịSố lượng
    private String poolareaNo;//Mã vùng điều phối
    private String drPsncnt;//Bác sĩsố người
    private String pharPsncnt;//Dược sĩsố người
    private String nursPsncnt;//Y tásố người
    private String tecnPsncnt;//kỹ thuật viênsố người

    private String isDefault;   //Mặc địnhKhoa
    private String isRegister;   //Có hay không là đăng kýKhoa

    public ClinicOffice() {
        super();
    }

    public ClinicOffice(String id){
        super(id);
    }


    @NotNull(message="thuộc vềPhòng khámKhông thể là trống")
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    @Length(min=1, max=64, message="Khoamã sốĐộ dài phải nằm trong khoảng 1 đến 64 ")
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }


    @Length(min=1, max=1, message="Vô hiệu hóa(0: ChưaVô hiệu hóa; 1: Vô hiệu hóa)Độ dài phải từ 1  và  1 Giữa")
    public String getIsLocked() {
        return isLocked;
    }
    public void setIsLocked(String isLocked) {
        this.isLocked = isLocked;
    }


    @Length(min=0, max=21, message="sắp xếpĐộ dài phải nằm trong khoảng 0 đến 21 ")
    public String getSort() {
        return sort;
    }
    public void setSort(String sort) {
        this.sort = sort;
    }


    @Length(min=0, max=128, message="KhoalớnLớpĐộ dài phải nằm trong khoảng 0 đến 128 ")
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }


    @Length(min=0, max=255, message="Độ dài địa chỉ phải nằm trong khoảng 0 đến 255 ")
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getIsDefault(){return isDefault;}
    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    public String getIsRegister(){return isRegister;}
    public void setIsRegister(String isRegister) {
        this.isRegister = isRegister;
    }

    /**
     * Lấy tên bảng tương ứng của thực thể
     */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "clinic_office";
    }

    /**
     * Lấy thực thể tương ứngId
     */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "1026680647570219053";
    }


    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
        return true;
    }
}
