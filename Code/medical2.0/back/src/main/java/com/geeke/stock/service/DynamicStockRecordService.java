package com.geeke.stock.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * độngTồn khomáysổ sáchBản ghivàkiểm soátXử lý
 * @author helong
 * @date 2022/8/17 14:01
 */
@Service("dynamicStockRecordService")
@Transactional(rollbackFor=Exception.class)
public class DynamicStockRecordService
{
    /**
     * trướcchiếm dụng
     * Hiện tại bao gồm chức năng: Điền ca bệnh-hoàn thành tiếp nhận, Điền ca bệnh-Tiếp nhận nhanh, Điền ca bệnh-Chỉnh sửa sau khi hoàn thành tiếp nhậnLưu
     */
    public void preparedOccupy() {
        //trướcgiải phónglạiLạichiếm dụng

    }

    /**
     * thực tếchiếm dụng
     * Hiện tại bao gồm chức năng: phát thuốchoàn thành
     */
    public void actualOccupy() {
        //trướcgiải phónglạiLạichiếm dụng

        //lạithực tếchiếm dụng
    }

    /**
     * Trả thuốcTồn khothao tác
     * thuốc房thực tếTồn khotăng, khả dụngTồn khotạm giờtrướckhôngtăng,  v.v. hoàn phísaukhả dụngTồn kholạităng
     */
    public void backMedicine() {
        //trực tiếpĐưathực tếchiếm dụng của Tồn khocòntrả lạiđi
    }

    /**
     * Hoàn phí
     */
    public void backCost() {
        //lấyhủytrướcchiếm dụng

        //Nếu có Trả thuốcthìkhả dụngTồn khotăng
    }
}
