package com.geeke.hosdata.util;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.ServiceException;
import com.geeke.hosdata.config.HosConfigProperties;
import com.geeke.hosdata.constant.ApiUrl;
import com.geeke.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vui lòngyêu cầubản việnDữ liệuđóng gói
 */
@Component
@RequiredArgsConstructor
public class HttpUtil {

    private final HosConfigProperties hosConfigProperties ;

    private  final RestTemplate restTemplate;

    /** nàyđịa phươngTokenBộ nhớ đệm(thaythayRedis) */
    private String tokenCache;
    private LocalDateTime tokenExpireTime;


    public  JSONObject getHosData(SearchParams params, String apiUrl){
        // DS176 B2: HIS chua cau hinh (hos-data-server-url rong, mac dinh trong application.yml) thi postForObject truoc day 500 IllegalArgumentException/NPE. Fail-closed 400.
        if (StringUtils.isBlank(hosConfigProperties.getHosDataServerUrl())) {
            throw new ServiceException("HIS chưa được cấu hình, không thể lấy dữ liệu bệnh viện");
        }
        Map<String, Object> data = new HashMap<>();
        Map<String, Object>  searchParams = new HashMap<>();
        Map<String, Object>  data2 = new HashMap<>();
        // DS178: SearchParams gui {} (params null) truoc day NPE o vong
        // for — coi nhu khong co dieu kien loc (giong "params": []).
        List<Parameter> newParams = params.getNewParams();
        if (newParams == null) {
            newParams = Collections.emptyList();
        }
        for (Parameter param : newParams){
            searchParams.put(param.getColumnName(),param.getValue());
        }
        data2.put("offset",params.getOffset());
        //tạm giờTruy vấnTất cả
        data2.put("limit",params.getLimit());
        data2.put("queryParams",searchParams);
        data.put("Data",data2);
        data.put("AppId",hosConfigProperties.getAppId());
        data.put("OrganizeId", hosConfigProperties.getOrganizeId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Thêm token ĐếnVui lòngtiêu đề yêu cầu
        String token = getToken();
        headers.set("Authorization", "Bearer " + token);
        // Tạo HttpEntity, vàĐặtVui lòngthân yêu cầu và Vui lòngtiêu đề yêu cầu
        HttpEntity<Map<String,Object>> request = new HttpEntity<>(data, headers);
        String response = restTemplate.postForObject(hosConfigProperties.getHosDataServerUrl()+apiUrl, request,String.class);
        // DS176 B2: HIS khong tra ve (response null/rong) thi parseObject tra null roi controller NPE o getJSONObject("BusData") -> 500. Fail-closed 400.
        if (StringUtils.isBlank(response)) {
            throw new ServiceException("HIS không trả về dữ liệu bệnh viện");
        }
        JSONObject jsonObject = JSONObject.parseObject(response);
        if (jsonObject == null) {
            throw new ServiceException("HIS trả về dữ liệu bệnh viện không hợp lệ");
        }
        // DS178: getHosToken (B2) da guard BusData/data, nhung getHosData
        // chi guard JSON top-level — HIS tra envelope loi hop le
        // ({"code":500,...} hoac {"BusData":{}}) thi 4 call site deref
        // getJSONObject("BusData").getJSONObject("data") -> NPE 500.
        // Fail-closed 400 ngay tai day.
        JSONObject busData = jsonObject.getJSONObject("BusData");
        if (busData == null || busData.getJSONObject("data") == null) {
            throw new ServiceException("HIS trả về dữ liệu bệnh viện không hợp lệ (thiếu BusData/data)");
        }
        return jsonObject;
    }


    /**
     * Sửaranhập khoTrạng thái
     * @param apiUrl
     * @param crkmxId
     * @return
     */
     public  Boolean UpdateHosData(String apiUrl,String crkmxId){
         // xây dựngVui lòngyêu cầuJSONDữ liệu
         JSONObject requestData = new JSONObject();
         JSONObject data = new JSONObject();
         data.put("crkmxId",crkmxId);
         requestData.put("Data", data);
         requestData.put("AppId", hosConfigProperties.getAppId());
         requestData.put("OrganizeId", hosConfigProperties.getOrganizeId());

         // xây dựngHttpEntityĐối tượng
         // Đặt tiêu đề yêu cầu
         HttpHeaders headers = new HttpHeaders();
         String token = getToken();
         headers.set("Authorization", "Bearer " + token);
         headers.setContentType(MediaType.APPLICATION_JSON);
         HttpEntity<String> requestEntity = new HttpEntity<>(requestData.toString(), headers);
         ResponseEntity<String> responseEntity = restTemplate.postForEntity(hosConfigProperties.getHosDataServerUrl()+apiUrl, requestEntity, String.class);
         return responseEntity.getStatusCodeValue() == 200;
     }



    public  String getHosToken(){
        // DS176 B2: HIS chua cau hinh (hos-token-url rong) thi postForObject truoc day 500 (URL null + path / RestTemplate). Fail-closed 400.
        if (StringUtils.isBlank(hosConfigProperties.getHosTokenUrl())) {
            throw new ServiceException("HIS chưa được cấu hình, không thể lấy token bệnh viện");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("AppId",hosConfigProperties.getAppId());
        data.put("Data", "API.Manage");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Tạo HttpEntity, vàĐặtVui lòngthân yêu cầu và Vui lòngtiêu đề yêu cầu
        HttpEntity<Map<String,Object>> request = new HttpEntity<>(data, headers);
        String response = restTemplate.postForObject(hosConfigProperties.getHosTokenUrl()+ApiUrl.HOS_GET_TOKEN, request,String.class);
        // DS176 B2: HIS tra ve rong / sai dinh dang thi parseObject null roi getJSONObject("BusData") NPE -> 500. Fail-closed 400.
        if (StringUtils.isBlank(response)) {
            throw new ServiceException("HIS không trả về token bệnh viện");
        }
        JSONObject jsonObject = JSONObject.parseObject(response);
        if (jsonObject == null || jsonObject.getJSONObject("BusData") == null
                || jsonObject.getJSONObject("BusData").getJSONObject("data") == null) {
            throw new ServiceException("HIS trả về token bệnh viện không hợp lệ");
        }
        JSONObject busData = jsonObject.getJSONObject("BusData").getJSONObject("data");
        String token = busData.getString("Token");
        if (StringUtils.isBlank(token)) {
            throw new ServiceException("HIS trả về token bệnh viện không hợp lệ");
        }
        // Bộ nhớ đệmToken 20 phútphúthết hạn
        tokenCache = token;
        tokenExpireTime = LocalDateTime.now().plusMinutes(20);
        return token;
    }

    /** LấyToken, Bộ nhớ đệmHiệu lựctrực tiếpTrả về, Nếu khôngLạiLấy */
    private String getToken() {
        if (tokenCache != null && tokenExpireTime != null && LocalDateTime.now().isBefore(tokenExpireTime)) {
            return tokenCache;
        }
        return getHosToken();
    }


}
