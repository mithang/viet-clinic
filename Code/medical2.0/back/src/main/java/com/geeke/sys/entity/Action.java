package com.geeke.sys.entity;

import com.geeke.sys.entity.DictItem;
import com.geeke.gen.entity.GenTable;
import com.geeke.org.entity.Company;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import com.google.common.collect.Lists;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.geeke.common.persistence.DataEntity;
/**
 * Nhật ký thao tácEntity
 * @author lys
 * @version 2019-12-11
 */
public class Action extends DataEntity<Action> {

	private static final long serialVersionUID = 1L;
	private DictItem actionType;      // Thao tác 
	private GenTable objectType;      // Đối tượngLoại 
	private String objectId;		// Đối tượng nghiệp vụ
	private String objectName;		// Tên đối tượnggọi là
	private String status;		// khôi phục

	// DS1: tenant cua DOI TUONG bi thao tac (sys_action.company_id,
	// migration v15). Khai rieng o day (khong dung ke thua) vi
	// CrudService.ensureCompanyFilter chi ap khi entity class co
	// DECLARED field "company" (Reflections.getDeclaredField) — neu
	// khong, list/listAll cua audit khong loc gi ca va moi session
	// co >=1 resource deu doc het dong cua tenant khac. Cung mau
	// nhu Patient/ClinicOffice/Department (khai company rieng du
	// DataEntity da co). Gia tri do CrudService.createAction dat tu
	// company cua entity bi thao tac; dong toan cuc (router,
	// resource, Company...) de NULL: fail-closed (an voi clinic,
	// hien voi admin nho bypass o ActionService).
	private Company company;

	private List<ActionRecycle> actionRecycleList = Lists.newArrayList();		// Danh sách bảng con
	
	public Action() {
		super();
	}

	public Action(String id){
		super(id);
	}
	

    public DictItem getActionType() {
        return actionType;
    }

    public void setActionType(DictItem actionType) {
        this.actionType = actionType;
    }
    
    public GenTable getObjectType() {
        return objectType;
    }

    public void setObjectType(GenTable objectType) {
        this.objectType = objectType;
    }
    
	@Length(min=0, max=40, message="Đối tượng nghiệp vụ Độ dài phải nằm trong khoảng 0 đến 40")
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	
	
	@Length(min=0, max=512, message="Tên đối tượng gọi là Độ dài phải nằm trong khoảng 0 đến 512")
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	
	
	@Length(min=1, max=1, message="khôi phục Độ dài phải nằm trong khoảng 1 đến 1")
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
	// DS1: override de field rieng o tren thang (xem comment o field).
	// Giu @NotNull nhu DataEntity.getCompany de khong thay doi ngu
	// nghia validation hien co. Luu y: cac SELECT cua ActionDao dung
	// resultType auto-mapping nen khong map company_id ve day — loc
	// list chay o muc SQL (param company_id), con get() dung lookup
	// rieng getCompanyIdById o ActionService (mau N1).
	@Override
	@NotNull(message="Phòng khám ID Không được để trống")
	public Company getCompany() {
		return company;
	}

	@Override
	public void setCompany(Company company) {
		this.company = company;
	}
	
	
	
	public List<ActionRecycle> getActionRecycleList() {
		return actionRecycleList;
	}

	public void setActionRecycleList(List<ActionRecycle> actionRecycleList) {
		this.actionRecycleList = actionRecycleList;
	}
	
	
	/**
     * Lấy tên bảng tương ứng của thực thể
    */
    @Override
    @JsonIgnore
    public String getBusTableName() {
        return "sys_action";
    }
    
    /**
     * Lấy thực thể tương ứngId
    */
    @Override
    @JsonIgnore
    public String getBusTableId() {
        return "4000";
    }
    
    
    /**
     * Kiểm tra thực thể có tồn tại trong bảng khôngdel_flagTrường
     */
    @JsonIgnore
    public boolean getBusTableHasDelFlag() {
            return true;
    }
}