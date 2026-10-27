package com.geeke.clinic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
/**
 * Phiên bản phòng khámEntity
 * @author txl
 * @version 2022-05-23
 */
public class ClinicVersion extends DataEntity<ClinicVersion> {

	private static final long serialVersionUID = 987744398207139863L;
	private String code;
	private String roleId;
	private BigDecimal price;		// phiên bảnGiá
	private String description;		// phiên bảnMô tả　
	
	public ClinicVersion() {
		super();
	}

	public ClinicVersion(String id){
		super(id);
	}

	@NotNull(message="phiên bảnMãKhông thể là trống")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	@NotNull(message="phiên bảnVai tròKhông thể là trống")
	public String getRoleId() {
		return roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	@NotNull(message="phiên bảnGiáKhông thể là trống")
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}


	@Length(min=0, max=200, message="phiên bảnMô tảĐộ dài phải nằm trong khoảng 0 đến 200 ")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "clinic_version";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "987744398207139863";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}