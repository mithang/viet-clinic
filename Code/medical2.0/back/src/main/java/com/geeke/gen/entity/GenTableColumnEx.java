package com.geeke.gen.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Lists;

import java.util.List;
/**
 * Quản lý bảng nghiệp vụEntity
 * @author lys
 * @version 2019-12-06
 */
public class GenTableColumnEx extends GenTableColumn {

	private static final long serialVersionUID = 1L;

	
	/**
	 * Lấychuỗiđộ dài
	 * @return
	 */
	@JsonIgnore
	public String getDataLength(){
		String[] ss = StringUtils.split(StringUtils.substringBetween(getJdbcType(), "(", ")"), ",");
		if (ss != null && ss.length == 1){// && "String".equals(getJavaType())){
			return ss[0];
		}
		return "0";
	}

	/**
	 * đượcKiểu sốnhỏsốđiểmđộ dài
	 * @return
	 */
	public Integer getDecimalLenth(){
		if ("java.math.BigDecimal".equals(getJavaType().getValue())){ // Số tiềnLoại
			return 2;
		} else if ("Long".equals(getJavaType().getValue()) || "Integer".equals(getJavaType().getValue())){ // nguyênsốLoại
			return 0;
		} else if ("Double".equals(getJavaType().getValue()) || "Float".equals(getJavaType().getValue())){ // nhỏsố
			String[] ss = StringUtils.split(StringUtils.substringBetween(getJdbcType(), "(", ")"), ",");
			if (ss != null && ss.length >= 2){
				return Integer.parseInt(ss[1]);
			} else {
				return 2; // Mặc địnhTrả vềhaivịnhỏsố
			}
		}
		return 0;
	}

	/**
	 * Lấyviết tắtJavaLoại
	 * @return
	 */
	@JsonIgnore
	public String getSimpleJavaType(){
		if (getJavaType() != null && "This".equals(getJavaType().getValue())){
			return StringUtils.capitalize(this.getGenTable().getClassName());
		}
		return StringUtils.indexOf(getJavaType().getValue(), ".") != -1 
				? StringUtils.substringAfterLast(getJavaType().getValue(), ".")
						: getJavaType().getValue();
	}
	
	/**
	 * LấyJavaTên trường
	 * @return
	 */
	// @JsonIgnore
	public String getFieldId(){
		return StringUtils.substringAfter(StringUtils.substringBefore(getJavaField(), "|"), ".");
	}

	/**
	 * LấyJavaTên trường
	 * @return
	 */
	public String getFieldName(){
		return StringUtils.substringAfter(getJavaFieldName(), ".");
	}	
	
	/**
	 * Lấyviết tắtJavaTrường
	 * @return
	 */
	public String getSimpleJavaField(){
		return StringUtils.substringBefore(getJavaField(), ".");
	}
	
	/**
	 * LấyJavaTrường, Nếu có Đối tượng, thìLấyĐối tượng.phụ thêmthuộc tính1
	 * @return
	 */
	public String getJavaFieldId(){
		return StringUtils.substringBefore(getJavaField(), "|");
	}
	
	/**
	 * LấyJavaTrường, Nếu có Đối tượng, thìLấyĐối tượng.phụ thêmthuộc tính2
	 * @return
	 */
	@JsonIgnore
	public String getJavaFieldName(){
		String[][] ss = getJavaFieldAttrs();
		return ss.length>0 ? getSimpleJavaField()+"."+ss[0][0] : getJavaFieldId();
	}
	
	/**
	 * LấyJavaTrường, tất cảthuộc tínhtên
	 *   parent.id|name|updateDate Quay lại:  name,name; updateDate,update_date
	 * @return
	 */
	@JsonIgnore
	public String[][] getJavaFieldAttrs(){
		String[] ss = StringUtils.split(StringUtils.substringAfter(getJavaField(), "|"), "|");
		String[][] sss = new String[ss.length][2];
		if (ss!=null){
			for (int i=0; i<ss.length; i++){
				sss[i][0] = ss[i];
				sss[i][1] = StringUtils.toUnderScoreCase(ss[i]).replace(".", "_");  // customer, ThisĐối tượngLoại của groupCust.id|name|code|channel.id thuộc tínhXử lý, tạiTạoDữ liệukhocâu lệnh trong , Đưa.thay thếthành_
			}
		}
		return sss;
	}
	
	/**
	 * LấyJavaTrường, idvàtất cảthuộc tínhtên
	 *   parent.id|name|updateDate Quay lại:  id,id; name,name; updateDate,update_date
	 * @return
	 */
	@JsonIgnore
	public String[][] getJavaFieldIDAttrs(){
		String[] ss = StringUtils.split(StringUtils.substringAfter(getJavaField(), "."), "|");
		String[][] sss = new String[ss.length][2];
		if (ss!=null){
			for (int i=0; i<ss.length; i++){
				sss[i][0] = ss[i];
				sss[i][1] = StringUtils.toUnderScoreCase(ss[i]);
			}
		}
		return sss;
	}	
	
	/**
	 * LấyCộtannotationDanh sách
	 * @return
	 */
	@JsonIgnore
	public List<String> getAnnotationList(){
		List<String> list = Lists.newArrayList();
		if ("java.util.Date".equals(getJavaType().getValue())){
			if("DatePicker".equals(getShowType().getValue())) {
				list.add("com.fasterxml.jackson.annotation.JsonFormat(pattern = \"yyyy-MM-dd\")");
			}else if("TimePicker".equals(getShowType().getValue())){
				list.add("com.fasterxml.jackson.annotation.JsonFormat(pattern = \"HH:mm:ss\")");
			}else {
				list.add("com.fasterxml.jackson.annotation.JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\")");
			}
		}
		// NhậpJSR303Xác thựcpackage phụ thuộc
		if (!"1".equals(getIsNull()) && !"String".equals(getJavaType().getValue())){
			list.add("javax.validation.constraints.NotNull(message=\""+getComments()+"Không được để trống\")");
		}
		else if (!"1".equals(getIsNull()) && "String".equals(getJavaType().getValue()) && !"0".equals(getDataLength())){
			list.add("org.hibernate.validator.constraints.Length(min=1, max="+getDataLength()
					+", message=\""+getComments()+"Độ dài phải từ 1  và  "+getDataLength()+" Giữa\")");
		}
		else if ("String".equals(getJavaType().getValue()) && !"0".equals(getDataLength())){
			list.add("org.hibernate.validator.constraints.Length(min=0, max="+getDataLength()
					+", message=\""+getComments()+"Độ dài phải từ 0  và  "+getDataLength()+" Giữa\")");
		}
		return list;
	}
	
	/**
	 * Lấyviết tắtCộtannotationDanh sách
	 * @return
	 */
	@JsonIgnore
	public List<String> getSimpleAnnotationList(){
		List<String> list = Lists.newArrayList();
		for (String ann : getAnnotationList()){
			list.add(StringUtils.substringAfterLast(ann, "."));
		}
		return list;
	}
	
	/**
	 * Có hay không có Lớp cơ sởTrường
	 * @return
	 */
	@JsonIgnore
	public Boolean getIsNotBaseField(){
		return (!StringUtils.equals(getName(), "id")
				&& !StringUtils.equals(getName(), "name")
				&& !StringUtils.equals(getName(), "remarks")
				&& !StringUtils.equals(getName(), "create_by")
				&& !StringUtils.equals(getName(), "create_date")
				&& !StringUtils.equals(getName(), "update_by")
				&& !StringUtils.equals(getName(), "update_date")
				&& !StringUtils.equals(getName(), "del_flag")
				&& !StringUtils.equals(getName(), "parent_id")
				&& !StringUtils.equals(getName(), "ids")
				&& !StringUtils.equals(getName(), "proc_status")
				&& !StringUtils.equals(getName(), "proc_inst")
				&& !(((GenTableEx)getGenTable()).getIsTreeTable() && StringUtils.equals(getName(), "sort")));
	}

	
	/**
	 * Có hay khôngcó thểchỉnh sửaTrường
	 * @return
	 */
	@JsonIgnore
	public Boolean getIsEditableField(){
		return !(StringUtils.equals(getName(), "id")
				|| StringUtils.equals(getName(), "create_by")
				|| StringUtils.equals(getName(), "create_date")
				|| StringUtils.equals(getName(), "update_by")
				|| StringUtils.equals(getName(), "update_date")
				|| StringUtils.equals(getName(), "del_flag")
				|| StringUtils.equals(getName(), "ids")
				|| StringUtils.equals(getName(), "proc_status")
				|| StringUtils.equals(getName(), "proc_inst"));
	}	
}