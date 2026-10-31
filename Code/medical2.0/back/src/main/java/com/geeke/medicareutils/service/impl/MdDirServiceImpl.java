package com.geeke.medicareutils.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.geeke.medicareutils.service.MdDirService;
import com.geeke.medicareutils.util.MdRequestUtil;
import com.geeke.sys.utils.SessionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MdDirServiceImpl implements MdDirService {

    private  final MdRequestUtil mdRequestUtil;

    /**
     * danh mụcđối vớichiếuTải lên
     *
     * @return
     */
    @Override
    public JSONObject upDirData_3301() {
        JSONObject data = new JSONObject();
        data.put("fixmedins_hilist_id", ""); // Mã danh mục của cơ sở y tế được chỉ định
        data.put("fixmedins_hilist_name", ""); // Tên danh mục của cơ sở y tế được chỉ định
        data.put("list_type", ""); // danh mụcLớploại
        data.put("med_list_codg", ""); // Mã danh mục y tế
        data.put("begndate", ""); // Ngày bắt đầu
        data.put("enddate", ""); // Ngày kết thúc
        data.put("aprvno", ""); // Số phê duyệt
        data.put("dosform", ""); // liềukiểu
        data.put("exct_cont", ""); // trừngoàinội dung
        data.put("item_cont", ""); // hạng mụctrongbao hàm
        data.put("prcunt", ""); // tínhgiáĐơn vị
        data.put("spec", ""); // Quy cách
        data.put("pacspec", ""); // Quy cách đóng gói
        data.put("memo", ""); // Ghi chú
        data.put("scdz", ""); // sảnđịa phương
        data.put("prdr_name", ""); // Tên nhà sản xuất
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data",data);
        return mdRequestUtil.getMedicareData("3301",jsonObject);
    }

    /**
     * Hủy đối chiếu danh mục
     *
     * @return
     */
    @Override
    public JSONObject delDirData_3302() {
        JSONObject data = new JSONObject();
        data.put("fixmedins_code", SessionUtils.getUserJson().getJSONObject("company").getString("fixmedinsCode"));
        data.put("fixmedins_hilist_name", "");
        data.put("list_type", "");
        data.put("med_list_codg", "");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data",data);
        return mdRequestUtil.getMedicareData("3302",jsonObject);
    }
}
