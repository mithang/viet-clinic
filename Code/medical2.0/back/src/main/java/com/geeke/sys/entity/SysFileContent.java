package com.geeke.sys.entity;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Tệp đính kèm hệ thốngEntity
 * @author szy
 * @version 2021-09-22
 */
public class SysFileContent extends DataEntity<SysFileContent> {

	private static final long serialVersionUID = 592905357446275244L;
	private byte[] fileByte;		// ảnhbyte
	private String fileType;		// Loại tệp
	private String base64Str;
	private String name;
	
	public SysFileContent() {
		super();
	}

	public SysFileContent(String id){
		super(id);
	}
	

	public byte[] getFileByte() {
		return fileByte;
	}
	public void setFileByte(byte[] fileByte) {
		this.fileByte = fileByte;
	}
	
	
	@Length(min=0, max=64, message="Độ dài loại tệp phải nằm trong khoảng 0 đến 64 ")
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getBase64Str() {
		return base64Str;
	}

	public void setBase64Str(String base64Str) {
		this.base64Str = base64Str;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_file_content";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "592905357446275244";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}