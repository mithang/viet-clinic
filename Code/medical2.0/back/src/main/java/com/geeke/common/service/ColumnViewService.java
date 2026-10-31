package com.geeke.common.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.geeke.gen.dao.GenSchemeDao;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.dao.ColumnViewDao;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.entity.ColumnView;
import com.geeke.gen.dao.GenTableColumnDao;
import com.geeke.gen.entity.GenScheme;
import com.geeke.gen.entity.GenTableColumn;
import com.geeke.gen.entity.GenTableColumnEx;
import com.geeke.sys.utils.SessionUtils;
import com.google.common.collect.Lists;

/**
 * bảngCộttiêu đềQuản lýService
 * 
 * @author lys
 * @version 2019/2/2
 */
@Service
@Transactional(readOnly = true)
public class ColumnViewService {

	private static final Logger logger = LoggerFactory.getLogger(ColumnViewService.class);
	
	final private static String OPERATE_COLUMN_NAME = "operate_column";	// cố địnhchỉ định của Cột, dùng đểlưuđặtthao tácCột của Người dùngtùy chỉnhCộtrộng
	
	@Autowired
	private ColumnViewDao dao;

	@Autowired
	private GenTableColumnDao genTableColumnDao;

	@Autowired
	private GenSchemeDao genSchemeDao;

	/**
	 * Lấyhiển thịDanh sách
	 * @param routerId
	 * @param tableId
	 * @return
	 */
	@Transactional(readOnly = false)
	public List<ColumnView> listColumn(String routerId, String tableId ) {
		return listColumn(routerId, tableId, false);
	}
	
	/**
	 * Lấyhiển thịDanh sách
	 * @param routerId
	 * @param tableId
	 * @param isAll	hiển thịTất cảCột	
	 * @return
	 */
	@Transactional(readOnly = false)
	public List<ColumnView> listColumn(String routerId, String tableId, boolean isAll) {
		String userId = SessionUtils.getUserJson().getString("id");
		List<ColumnView> list = Lists.newArrayList();

		// Loại trừ trường không hiển thị trong danh sách
		String[] strings = {"del_flag", "ids"};
		List<Parameter> Params = SearchParamsBuilder.create()
				.eq("gen_table_id", tableId)
				.notIn("name", Arrays.asList(strings))
				.ne("show_type", "'PassWordInput'")
				.ne("show_type", "'MultiFileUpload'")
				.build();

		PageRequest pageRequest = new PageRequest(Params);
		List<GenTableColumn> columns = genTableColumnDao.listAll(pageRequest);

		for (GenTableColumn cc : columns) {
			GenTableColumnEx col = (GenTableColumnEx)cc;
			col.setSort(col.getSort() * 1000);// nghiệp vụbảngĐặt giờ, thứ tựđặtlớn10lần; bảngthứ tự giờCầnđặtlớn10000lần. donàyCầnbổ sung1000lần. 
			if ("Select".equals(col.getShowType().getValue())
					|| "SelectTree".equals(col.getShowType().getValue()) 
					|| "StringMultiSelect".equals(col.getShowType().getValue())) {
				if ("Custom".equals(col.getJavaType().getValue())) {
					GenScheme scheme = genSchemeDao.get(col.getCustomType().getId());
					String tId = scheme.getGenTable().getId(); // tùy chỉnhLoại của bảngID
					String[][] fieldAttrs = col.getJavaFieldAttrs();
					for (int index = 0; index < fieldAttrs.length; index++) {
						String[] attr = fieldAttrs[index];
						GenTableColumnEx c = this.getCustomerColumn(tId, attr[1]);
						c.setSort(col.getSort() + index); // Thứ tự hiển thị theo thứ tự hiển thị của trường bảng chính +
															// 1, 2, 3
						ColumnView column = this.mapForm(c);
						column.setProp(col.getSimpleJavaField() + "." + attr[0]);
						// chỉ có mộtCáithuộc tínhGiá trị của  giờchờ, Trực tiếp dùng tên trường trên bảng chính làm tiêu đề cột; Nếu khôngđịnh dạng là Bảng chínhTên trường.conbảng của Tên trường
						if(fieldAttrs.length == 1) {
							column.setMiniWidth(cc.getWidth());   // chỉ có mộtCáithuộc tính giờ, Độ rộng cột dùng độ rộng cột cấu hình của bảng nghiệp vụ hiện tại; Nếu khôngsử dụnggốcCộtrộng
							column.setLabel(col.getComments());
						} else {
							column.setLabel(col.getComments() + "."
									+ column.getLabel());
						}
						if (!"1".equals(col.getIsList())) { // Đối tượngĐặtkhônghiển thị giờ, Tất cả các thuộc tính đều không hiển thị
							column.setDisplay(false);
						}
						column.setTableId(tableId);
						column.setRouterId(routerId);
						column.setUserId(userId);
						if("name".equals(c.getName())) {
							column.setMiniWidth(cc.getWidth());   // thuộc tínhbảngTrường là name giờ, sử dụngHiện tạibảngcấu hình của Cộtrộng
							// mục từ điển của TênCột, đơn đồngôgiữa trong 
							if("6006".equals(scheme.getId())) {
								column.setAlign("center");
							}
						}
						list.add(column);
					}
				} else if ("This".equals(col.getJavaType().getValue())) {
					String[][] fieldAttrs = col.getJavaFieldAttrs();
					for (int index = 0; index < fieldAttrs.length; index++) {
						String[] attr = fieldAttrs[index];
						GenTableColumnEx c = this.getCustomerColumn(columns,	attr[0]);
						ColumnView column = this.mapForm(c);
						column.setProp(col.getSimpleJavaField() + "." + attr[0]);
						// chỉ có mộtCáithuộc tínhGiá trị của  giờchờ, Trực tiếp dùng tên trường trên bảng chính làm tiêu đề cột; Nếu khôngđịnh dạng là Bảng chínhTên trường.conbảng của Tên trường
						if(fieldAttrs.length == 1) {
							column.setMiniWidth(cc.getWidth());   // chỉ có mộtCáithuộc tính giờ, Độ rộng cột dùng độ rộng cột cấu hình của bảng nghiệp vụ hiện tại; Nếu khôngsử dụnggốcCộtrộng
							column.setLabel(col.getComments());
						} else {
							column.setLabel(col.getComments() + "."
									+ column.getLabel());
						}
						if (!"1".equals(col.getIsList())) { // Đối tượngĐặtkhônghiển thị giờ, Tất cả các thuộc tính đều không hiển thị
							column.setDisplay(false);
						}
						column.setTableId(tableId);
						column.setRouterId(routerId);
						column.setUserId(userId);
						column.setSort(col.getSort() + index + 1 ); // Thứ tự hiển thị theo thứ tự hiển thị của trường bảng chính +1, 2, 3	
						if("name".equals(c.getName())) {
							column.setMiniWidth(cc.getWidth());   // thuộc tínhbảngTrường là name giờ, sử dụngHiện tạibảngcấu hình của Cộtrộng
						}
						list.add(column);
					}
				} else {
					ColumnView column = this.mapForm(col);
					column.setProp(col.getSimpleJavaField());
					column.setTableId(tableId);
					column.setRouterId(routerId);
					column.setUserId(userId);
					list.add(column);
				}
			} else {
				ColumnView column = this.mapForm(col);
				column.setTableId(tableId);
				column.setRouterId(routerId);
				column.setUserId(userId);
				list.add(column);
			}
		}
				
		List<ColumnView> lv = dao.list(userId, routerId, tableId); // Người dùngCáitínhhóacấu hìnhThông tin
		for (ColumnView cv : lv) {
			if(OPERATE_COLUMN_NAME.equals(cv.getProp())) {  // thao tácCột của thuộc tính
				ColumnView operateColumn = new ColumnView();
				operateColumn.setId(cv.getId());
				operateColumn.setSort(list.size() * 100000);
				operateColumn.setProp(OPERATE_COLUMN_NAME);
				operateColumn.setDisplay(cv.getDisplay());
				operateColumn.setWidth(cv.getWidth());
				operateColumn.setTableId(tableId);
				operateColumn.setRouterId(routerId);
				operateColumn.setUserId(userId);
				list.add(operateColumn);
				continue;
			}
			
			boolean found = false;
			for (ColumnView column : list) {
				if (cv.getProp().equals(column.getProp())) {
					column.setId(cv.getId());
					column.setDisplay(cv.getDisplay());
					column.setSort(cv.getSort());
					column.setWidth(cv.getWidth());
					found = true;
					break;
				}
			}
			if (!found) { // TrườngĐãKhông tồn tại
				this.delete(cv.getId());
			}
		}
		Collections.sort(list);
		List<ColumnView> adds = Lists.newArrayList();			// Thêm mớiCột
		List<ColumnView> updates = Lists.newArrayList();		// Cập nhậtCột
		for (int i = 0; i < list.size(); i++) {
			ColumnView cv = list.get(i);
			// trọngđặthiển thịthứ tự
			cv.setSort((i + 1) * 10000);

			if(isAll) {
				cv.setDisplay(true);
			}
			
			if (StringUtils.isBlank(cv.getId())) {
				cv.preInsert();
				adds.add(cv);
			} else {
				updates.add(cv);
			}
		}
		if(updates.size() > 0) {
			dao.bulkUpdate(updates);
		}
		if(adds.size() > 0) {
			dao.bulkInsert(adds);
		}
		
		return list;
	}

	/**
	 * LưuDữ liệu(Chèn hoặc cập nhật)
	 * 
	 * @param cv
	 */
	@Transactional(readOnly = false)
	public int save(ColumnView cv) {
		String userId = SessionUtils.getUserJson().getString("id");
		cv.setUserId(userId);
		int rows = 0;
		if (StringUtils.isBlank(cv.getId())) {
			cv.preInsert();
			rows = dao.insert(cv);
		} else {

			rows = dao.update(cv);
		}
		return rows;
	}

	/**
	 * khôi phụchiển thịMặc địnhCột
	 * 
	 * @param routerId
	 * @param tableId
	 */
	@Transactional(readOnly = false)
	public void reset(String routerId, String tableId) {
		String userId = SessionUtils.getUserJson().getString("id");
		dao.deleteAll(userId, routerId, tableId);
	}

	/**
	 * XóaĐãKhông tồn tại của Cột
	 * 
	 * @param id
	 */
	@Transactional(readOnly = false)
	public void delete(String id) {
		dao.delete(id);
	}

	private ColumnView mapForm(GenTableColumnEx col) {
		ColumnView column = new ColumnView();
		column.setProp(col.getJavaFieldId());
		column.setLabel(col.getComments());
		if ("Switch".equals(col.getShowType().getValue())
				|| "Checkbox".equals(col.getShowType().getValue())
				|| "Radio".equals(col.getShowType().getValue())) {
			column.setAlign("center");
		} else if ("Date".equals(col.getSimpleJavaType())
				|| "Long".equals(col.getSimpleJavaType())
				|| "Integer".equals(col.getSimpleJavaType())
				|| "Float".equals(col.getSimpleJavaType())
				|| "Double".equals(col.getSimpleJavaType())
				|| "BigDecimal".equals(col.getSimpleJavaType())) {
			column.setAlign("right");
		} else {
			column.setAlign("left");
		}
		column.setMiniWidth(col.getWidth());
		column.setDisplay("1".equals(col.getIsList()));
		column.setSort(col.getSort());
		column.setShowType(col.getShowType().getValue());
		column.setJavaType(col.getJavaType().getValue());
		return column;
	}

	private GenTableColumnEx getCustomerColumn(String tableId, String name) {
		List<Parameter> params = SearchParamsBuilder.create()
				.eq("gen_table_id", tableId)
				.eq("name", name)
				.build();

		PageRequest pageRequest = new PageRequest(params);
		List<GenTableColumn> cs = genTableColumnDao.listAll(pageRequest);
		if (cs != null && cs.size() == 1) {
			return (GenTableColumnEx)cs.get(0);
		}
		return null;
	}

	private GenTableColumnEx getCustomerColumn(List<GenTableColumn> genTableColumns, String javaField) {
		for (GenTableColumn cc : genTableColumns) {
			GenTableColumnEx column = (GenTableColumnEx)cc;
			if (javaField.equals(column.getJavaField())) {
				return column;
			}
		}
		return null;
	}
}
