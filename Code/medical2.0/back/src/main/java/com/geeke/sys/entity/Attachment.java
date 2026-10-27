package com.geeke.sys.entity;

import java.io.IOException;
import java.io.InputStream;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.common.persistence.DataEntity;
/**
 * Tệptệp đính kèmEntity
 * @author lys
 * @version 2021-04-28
 */
public class Attachment extends DataEntity<Attachment> {

	private static final long serialVersionUID = 1L;
	private String bizId;		// nghiệp vụđịnh danh
	private String type;		// Loại tệp
	private byte[] fileContent;		// Tệpnội dung

	
	public Attachment() {
		super();
	}

	public Attachment(String id){
		super(id);
	}
	
	public Attachment(String bizId, MultipartFile file) throws IOException {
		super();
		
		this.bizId = bizId;
		this.name = file.getOriginalFilename();
		this.type = file.getContentType();
		InputStream is = file.getInputStream();
		byte[] fContent = new byte[(int)file.getSize()];
		is.read(fContent);
		this.fileContent = fContent;
	}

	@Length(min=1, max=20, message="nghiệp vụđịnh danhĐộ dài phải nằm trong khoảng 1 đến 20 ")
	public String getBizId() {
		return bizId;
	}
	public void setBizId(String bizId) {
		this.bizId = bizId;
	}
	
	
	@Length(min=0, max=20, message="Độ dài loại tệp phải nằm trong khoảng 0 đến 20 ")
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
	public byte[] getFileContent() {
		return fileContent;
	}
	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}
	
	
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_attachment";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "414950311024263169";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}