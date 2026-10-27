package com.geeke.sys.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import java.util.List;
import com.google.common.collect.Lists;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Tệp đính kèm hệ thốngEntity
 * @author szy
 * @version 2021-09-22
 */
public class SysFile extends DataEntity<SysFile> {

	private static final long serialVersionUID = 592905357446275112L;
    @JsonSerialize(using= ToStringSerializer.class)
	private Long objectId;		// Bảng nghiệp vụ
	private String fileType;		// Loại tệp
    @JsonSerialize(using= ToStringSerializer.class)
	private Long fileSize;		// Tệpkích thước
	private String fileUrl;		// Tệpđường dẫn
	private byte[] fileByte;		// ảnhbyte

	private List<SysFileContent> sysFileContentList = Lists.newArrayList();		// Danh sách bảng con
	
	public SysFile() {
		super();
	}

	public SysFile(String id){
		super(id);
	}
	

	@NotNull(message="nghiệp vụbảngKhông thể là trống")
	public Long getObjectId() {
		return objectId;
	}
	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}
	
	
	@Length(min=0, max=64, message="Độ dài loại tệp phải nằm trong khoảng 0 đến 64 ")
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	
	
	public Long getFileSize() {
		return fileSize;
	}
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
	
	
	@Length(min=0, max=255, message="Tệpđường dẫnĐộ dài phải nằm trong khoảng 0 đến 255 ")
	public String getFileUrl() {
		return fileUrl;
	}
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public byte[] getFileByte() {
		return fileByte;
	}
	public void setFileByte(byte[] fileByte) {
		this.fileByte = fileByte;
	}
	
	public List<SysFileContent> getSysFileContentList() {
		return sysFileContentList;
	}

	public void setSysFileContentList(List<SysFileContent> sysFileContentList) {
		this.sysFileContentList = sysFileContentList;
	}
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_file";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "592905357446275112";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}