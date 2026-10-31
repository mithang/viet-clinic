package com.geeke.sys.entity;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Cài đặt hệ thốngEntity
 * @author szy
 * @version 2021-08-26
 */
public class SysSeting extends DataEntity<SysSeting> {

	private static final long serialVersionUID = 1L;
	private String sysName;		// Hệ thốngTên
	private String sysAbbrname;		// Hệ thốnggiới thiệu
	private String loginBgcolor;		// Màu nền đăng nhập
	private String loginLayout;		// đăng nhậpphát hànhcục
	private SysFile projectLogo;      // Cờ hạng mục 
	private SysFile loginLogo;      // Cờ đăng nhập 
	private SysFile sysLogo;      // Cờ hệ thống 
	private SysFile favicon;      // Biểu tượng hệ thống 
	private SysFile loginBg;      // Ảnh nền đăng nhập 
	private SysFile loginGraph;      // Đăng nhậpphốihình 
    private Integer projectLogoIndex;      // Cờ hạng mục
    private Integer loginLogoIndex;      // Cờ đăng nhập
    private Integer sysLogoIndex;      // Cờ hệ thống
    private Integer faviconIndex;      // Biểu tượng hệ thống
    private Integer loginBgIndex;      // Ảnh nền đăng nhập
    private Integer loginGraphIndex;      // Đăng nhậpphốihình

    public Integer getProjectLogoIndex() {
        return projectLogoIndex;
    }

    public void setProjectLogoIndex(Integer projectLogoIndex) {
        this.projectLogoIndex = projectLogoIndex;
    }

    public Integer getLoginLogoIndex() {
        return loginLogoIndex;
    }

    public void setLoginLogoIndex(Integer loginLogoIndex) {
        this.loginLogoIndex = loginLogoIndex;
    }

    public Integer getSysLogoIndex() {
        return sysLogoIndex;
    }

    public void setSysLogoIndex(Integer sysLogoIndex) {
        this.sysLogoIndex = sysLogoIndex;
    }

    public Integer getFaviconIndex() {
        return faviconIndex;
    }

    public void setFaviconIndex(Integer faviconIndex) {
        this.faviconIndex = faviconIndex;
    }

    public Integer getLoginBgIndex() {
        return loginBgIndex;
    }

    public void setLoginBgIndex(Integer loginBgIndex) {
        this.loginBgIndex = loginBgIndex;
    }

    public Integer getLoginGraphIndex() {
        return loginGraphIndex;
    }

    public void setLoginGraphIndex(Integer loginGraphIndex) {
        this.loginGraphIndex = loginGraphIndex;
    }

    public SysSeting() {
		super();
	}

	public SysSeting(String id){
		super(id);
	}
	

	@Length(min=0, max=255, message="Hệ thốngTênĐộ dài phải nằm trong khoảng 0 đến 255 ")
	public String getSysName() {
		return sysName;
	}
	public void setSysName(String sysName) {
		this.sysName = sysName;
	}
	
	
	@Length(min=0, max=128, message="Hệ thốnggiới thiệuĐộ dài phải nằm trong khoảng 0 đến 128 ")
	public String getSysAbbrname() {
		return sysAbbrname;
	}
	public void setSysAbbrname(String sysAbbrname) {
		this.sysAbbrname = sysAbbrname;
	}
	
	
	@Length(min=0, max=20, message="Độ dài màu nền đăng nhập phải nằm trong khoảng 0 đến 20 ")
	public String getLoginBgcolor() {
		return loginBgcolor;
	}
	public void setLoginBgcolor(String loginBgcolor) {
		this.loginBgcolor = loginBgcolor;
	}
	
	
	@Length(min=0, max=20, message="đăng nhậpphát hànhcụcĐộ dài phải nằm trong khoảng 0 đến 20 ")
	public String getLoginLayout() {
		return loginLayout;
	}
	public void setLoginLayout(String loginLayout) {
		this.loginLayout = loginLayout;
	}
	
	
    public SysFile getProjectLogo() {
        return projectLogo;
    }

    public void setProjectLogo(SysFile projectLogo) {
        this.projectLogo = projectLogo;
    }
    
    public SysFile getLoginLogo() {
        return loginLogo;
    }

    public void setLoginLogo(SysFile loginLogo) {
        this.loginLogo = loginLogo;
    }
    
    public SysFile getSysLogo() {
        return sysLogo;
    }

    public void setSysLogo(SysFile sysLogo) {
        this.sysLogo = sysLogo;
    }
    
    public SysFile getFavicon() {
        return favicon;
    }

    public void setFavicon(SysFile favicon) {
        this.favicon = favicon;
    }
    
    public SysFile getLoginBg() {
        return loginBg;
    }

    public void setLoginBg(SysFile loginBg) {
        this.loginBg = loginBg;
    }
    
    public SysFile getLoginGraph() {
        return loginGraph;
    }

    public void setLoginGraph(SysFile loginGraph) {
        this.loginGraph = loginGraph;
    }
    
	
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_seting";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "592905357446275093";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}