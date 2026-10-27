package com.geeke.common.controller;

import java.util.List;

import com.geeke.utils.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.data.Page;
import com.geeke.common.persistence.DataEntity;
import com.geeke.common.service.CrudService;
import com.geeke.sys.controller.BaseController;
import com.geeke.utils.ResultUtil;

/**
 * ControllerLớp cơ sở - cung cấptiêu chuẩnCRUDGiao diện
 * @param <S> ServiceLoại
 * @param <T> Loại thực thể
 */
public abstract class CrudController<S extends CrudService<?, T>, T extends DataEntity<T>> extends BaseController {

    /**
     * conLớpPhảikhai báonàyTrườngvàThêm @Autowired annotation, Để chỉ định rõ cần inject của  Service. 
     * Ví dụ: @Autowired protected SupplierService service;
     * khôngcầntạiLớp cơ sởsử dụng @Autowired, Nếu khôngnhiềuCái CrudService conLớpSẽ gây mơ hồ. 
     */
    protected S service;

    /**
     * LấyServiceinstance - Lớp con có thể cung cấp cách riêng của mình bằng cách ghi đè phương thức nàyService
     */
    protected S getService() {
        return service;
    }

    /**
     * TheoIDLấythực thể
     */
    @GetMapping("/{id}")
    public ResponseEntity<JSONObject> getById(@PathVariable("id") String id) {
        T entity = getService().get(id);
        return ResponseEntity.ok(ResultUtil.successJson(entity));
    }

    /**
     * Truy vấn phân trang
     */
    @PostMapping(value = {"list", ""})
    public ResponseEntity<JSONObject> listPage(@RequestBody SearchParams searchParams) {
        Page<T> result = getService().listPage(searchParams.getParams(), searchParams.getOffset(),
                searchParams.getLimit(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Truy vấnTất cảDữ liệu
     */
    @PostMapping(value = "listAll")
    public ResponseEntity<JSONObject> listAll(@RequestBody SearchParams searchParams) {
        List<T> result = getService().listAll(searchParams.getParams(), searchParams.getOrderby());
        return ResponseEntity.ok(ResultUtil.successJson(result));
    }

    /**
     * Lưutrướchookcon — conLớpcó thểghi đèbằngThêmtrướcXử lýlogic(Như TệpXử lý, Tham sốbổ sung)
     * Quay lạitruetiếp tụcLưu, Quay lạifalse trong dừngLưu
     */
    protected boolean beforeSave(T entity) {
        return true;
    }

    /**
     * Lưusauhookcon — Lớp con có thể ghi đè để thêm logic hậu xử lý(Như phátgửi通biết, đồng bộDữ liệu)
     */
    protected void afterSave(T entity) {
    }

    /**
     * Xóatrướchookcon — conLớpcó thểghi đèbằngThêmXóatrướcKiểm tra
     * Quay lạitruetiếp tụcXóa, Quay lạifalse trong dừngXóa
     */
    protected boolean beforeDelete(T entity) {
        return true;
    }

    /**
     * Xóasauhookcon — Lớp con có thể ghi đè để thêm logic hậu xử lý
     */
    protected void afterDelete(T entity) {
    }

    /**
     * Lưuthực thể(Thêm mới hoặc Cập nhật)
     */
    @PostMapping(value = "save")
    public ResponseEntity<JSONObject> save(@Valid @RequestBody T entity) {
        if (!beforeSave(entity)) {
            return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50002, "LưutrướcKiểm traChưaQua"));
        }
        String id = getService().save(entity).getId();
        afterSave(entity);
        return ResponseEntity.ok(ResultUtil.successJson(id));
    }

    /**
     * Xóathực thể
     */
    @PostMapping(value = "delete")
    public ResponseEntity<JSONObject> delete(@RequestBody T entity) {
        if (entity == null) {
            return ResponseEntity.ok(ResultUtil.errorJson(ErrorEnum.E_400));
        }
        if (!beforeDelete(entity)) {
            return ResponseEntity.ok(ResultUtil.warningJson(ErrorEnum.E_50002, "XóatrướcKiểm traChưaQua"));
        }
        int rows = getService().delete(entity);
        afterDelete(entity);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }

    /**
     * hàng loạtThêm mới
     */
    @PostMapping(value = "bulkInsert")
    public ResponseEntity<JSONObject> bulkInsert(@RequestBody List<T> entitys) {
        List<String> ids = getService().bulkInsert(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }

    /**
     * hàng loạtCập nhật
     */
    @PostMapping(value = "bulkUpdate")
    public ResponseEntity<JSONObject> bulkUpdate(@RequestBody List<T> entitys) {
        List<String> ids = getService().bulkUpdate(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(ids));
    }

    /**
     * Xóa hàng loạt
     */
    @PostMapping(value = "bulkDelete")
    public ResponseEntity<JSONObject> bulkDelete(@RequestBody List<T> entitys) {
        int rows = getService().bulkDelete(entitys);
        return ResponseEntity.ok(ResultUtil.successJson(rows));
    }
}
