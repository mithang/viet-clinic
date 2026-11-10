package com.geeke.medicareutils.util;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.geeke.common.service.ServiceException;
import com.geeke.medicareutils.config.MedicareConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @Description BHYT trong giữatruy cập tệpgiao diện
 * @Author
 * @Date 2025/3/26
 */
@Component
@RequiredArgsConstructor
public class YbWebApiUtil {

    private final MedicareConfigProperties medicareConfigProperties;

    private static  final  String YB_URL = "/getMedicareData";

    private static  final  String DZ_CF_URL = "/getDzcfData";

    public String getMedicareData(String infoNo, String jsonObject){
        return postToMiddleware(YB_URL, infoNo, jsonObject);
    }

    public String getDzcfData(String infoNo, String jsonObject){
        return postToMiddleware(DZ_CF_URL, infoNo, jsonObject);
    }

    /**
     * DS181: fail-closed 400 thay vi 500 khi goi BHYT trung gian.
     * Truoc day: webUrl trong -> HttpUtil.post("null/getMedicareData")
     * nem HttpException 500; middleware chet (khong co gi nghe o 8101)
     * -> HttpException (Connection refused) 500; middleware tra ve
     * rong -> caller parseObject(null) NPE 500 (MdPsnDataServiceImpl:55);
     * tra ve khong phai JSON -> caller JSONException 500. Tat ca deu
     * la loi phia BHYT/trung gian, khong phai loi du lieu nguoi dung
     * goi sai -- nhung 500 khong noi ro, nen 400 voi message BHYT.
     * Chuoi tra ve duoc kiem tra la JSON hop le o day de cac caller
     * parseObject khong bao gio gap JSONException nua.
     */
    private String postToMiddleware(String path, String infoNo, String jsonObject) {
        String webUrl = medicareConfigProperties.getWebUrl();
        if (webUrl == null || webUrl.trim().isEmpty()) {
            throw new ServiceException("BHYT chưa cấu hình địa chỉ trung gian (spring.medicare.web-url)");
        }
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("infoNo", infoNo);
        paramMap.put("jsonObject", jsonObject);
        String response;
        try {
            response = HttpUtil.post(webUrl + path, paramMap);
        } catch (Exception e) {
            throw new ServiceException("Không kết nối được tới cổng BHYT trung gian (" + e.getMessage() + ")");
        }
        if (response == null || response.trim().isEmpty()) {
            throw new ServiceException("Cổng BHYT trung gian trả về phản hồi rỗng");
        }
        try {
            JSON.parse(response);
        } catch (JSONException e) {
            throw new ServiceException("Cổng BHYT trung gian trả về dữ liệu không phải JSON hợp lệ");
        }
        return response;
    }

}
