package com.geeke.outpatient.entity;

import com.geeke.common.persistence.DataEntity;
import com.geeke.org.entity.Company;
import com.geeke.sys.entity.DictItem;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecipeTemplateDetail extends DataEntity<RecipeTemplateDetail> {
    private String id;
    private Company company;
    private RecipeTemplateInfo recipeTemplateInfo;      // mẫuĐơn thuốcThông tin
    private DrugStuffEvt drugStuffId;		// ThuốcVật tưid
    private Integer singleDosage;		// Liều dùng mỗi lần
    private Integer total;		// Tổng số lượng
    private BigDecimal allFee;		// tổng giá
    private DictItem westernMedicineUse;      // Cách dùng thuốc Tây
    private DictItem frequency;      // Tần suất và liều dùng
    private DictItem days;      // Số ngày
    private DictItem chineseMedicineUse;      // Cách dùng thuốc Đông y
    private DictItem infuseUse;      // Cách dùng truyền dịch
    private String drippingSpeed;		// Tốc độ truyền
    private DictItem skinTest;      // Test da
    private Integer infuseGroup;		// nhómsố
    private String retailType;		// retail_type
    private Integer isExtra;		// Có hay khôngphí phụ thêm
    private Integer seq;		// hiển thịthứ tự
    private Integer isUnpackSell;		// Có bán lẻ không
    private BigDecimal unitPrice;		// Đơn giá
    private String unitType;		// đơn giáĐơn vị
    private String stuffType;		// Loại vật liệu
    private Integer minTotal;		// Đơn vị nhỏ nhấttổng lượng
    private int executions;			//Tiến độ thực hiện
    private int infuseType;        //Truyền dịchhành nghề dòngTrạng thái
}
