package com.geeke.common.service;

import java.util.Collections;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.persistence.TreeDao;
import com.geeke.common.persistence.TreeEntity;
import com.geeke.utils.Reflections;
import com.geeke.utils.StringUtils;
import com.google.common.collect.Lists;



/**
 * ServiceLớp cơ sở
 * @author lys
 * @version 2014-05-16
 */
@Transactional(readOnly = true)
public abstract class TreeService<D extends TreeDao<T>, T extends TreeEntity<T>> extends CrudService<D, T> {
	
	
	@Override
	public T get(String id) {
		T entity = super.get(id);
		if(entity != null && entity.getParent() != null && StringUtils.isBlank(entity.getParent().getId())) {
			entity.setParent(null);
		}
		return entity;
	}
	
	@Override
	@Transactional(readOnly = false)
	public T save(T entity) {
		
		@SuppressWarnings("unchecked")
		Class<T> entityClass = Reflections.getClassGenricType(getClass(), 1);
		
		// NếuKhông cóĐặtchaNút, thìthaybảng là theoNút, cóthìLấychaNútthực thể
		if (entity.getParent() == null || StringUtils.isBlank(entity.getParent().getId())){
			T parentEntity = null;
			try {
				parentEntity = entityClass.newInstance();
			} catch (Exception e) {
				throw new ServiceException(e);
			}
			parentEntity.setIds("0.");
			parentEntity.setId("0");
			entity.setParent(parentEntity);
		} else if(entity.getParent().getId().equals("0")) {	// gốcNútĐối tượng
			entity.getParent().setIds("0.");
		} else {	// gốcNútĐối tượng
			entity.setParent(super.get(entity.getParent().getId()));
		}

		
		// LấySửatrước của parentIds, dùng đểCập nhậtconNút của parentIds, Nếu có Thêm mớimẫukiểudưới, bỏ quanó của ids
		// ITER36 N3: oldIds phải lấy từ dòng DB đã lưu, KHÔNG lấy từ payload.
		// Payload ids điều khiển được tiền tố truy vấn con (ids right_like,
		// không lọc company): lưu 1 dòng own kèm ids lạ đã viết lại ids của
		// cả cây con tenant khác (tái hiện live /org/department/save).
		// Dòng không tồn tại -> oldIds null -> bỏ qua khối con (đóng luôn
		// biến thể phantom-id); guard isNotBlank bên dưới giữ nguyên.
		String oldIds = null;
		if (StringUtils.isNotBlank(entity.getId())) {
			T stored = dao.get(entity.getId());
			if (stored != null) {
				oldIds = stored.getIds();
			}
		}
		
	
		// Lưu hoặc Cập nhậtthực thể
		String id = super.save(entity).getId();
		
		// Sửa
		if(StringUtils.isNotBlank(oldIds)) {
	    	List<Parameter> parms = Lists.newArrayList();
	    	parms.add(new Parameter("ids", "right_like", oldIds));
	    	parms.add(new Parameter("id", "<>", entity.getId()));
	    	List<T> children = dao.listAll(new PageRequest(parms));
	    	for(T child : children) {
	    		child.setIds(child.getIds().replace(oldIds, entity.getIds()));
	    		dao.update(child);
	    	}
		}
		
		return entity;
	}
	
	/**
	 * Truy vấnsốbảngDữ liệu
	 * @param entity
	 * @return
	 */
	public List<T> tree(List<Parameter> parameters, String orderby) {
		ensureCompanyFilter(parameters);
		PageRequest pageRequest = new PageRequest(parameters, orderby);
		List<T> list = dao.listAll(pageRequest);
		return bulid(list);
	}
	
    /**
     * haivòng lặp theo tầngTriển khaixây dựngcây
     * @param list truyền vào của câyNútDanh sách
     * ĐưalistDữ liệuQua id,  và parentIdđủgây ramộtCáicây
     * @return
     */
    private List<T> bulid(List<T> list) {
        List<Integer> childIndex = Lists.newArrayList();
        for (T entity : list) {
        	for(int i = 0; i <= list.size() - 1; i++) {
        		T it = list.get(i);
                if (it.getParent() != null && entity.getId().equals(it.getParent().getId())) {
                    if (entity.getChildren() == null) {
                    	entity.setChildren(Lists.newArrayList());
                    }                	
                	entity.getChildren().add(it);
                	childIndex.add(i);
                }
            }
        }
        Collections.sort(childIndex); 
        for(int i = childIndex.size() - 1; i >= 0; i--) {
        	int idx = (int)childIndex.get(i);
        	list.remove(idx);
        }
        return list;
    }

}
