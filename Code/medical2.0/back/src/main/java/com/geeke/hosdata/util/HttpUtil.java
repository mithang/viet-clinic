package com.geeke.hosdata.util;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.controller.SearchParams;
import com.geeke.common.data.Parameter;
import com.geeke.hosdata.config.HosConfigProperties;
import com.geeke.hosdata.constant.ApiUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
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
        Map<String, Object> data = new HashMap<>();
        Map<String, Object>  searchParams = new HashMap<>();
        Map<String, Object>  data2 = new HashMap<>();
        for (Parameter param : params.getNewParams()){
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
        JSONObject jsonObject = JSONObject.parseObject(response);
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
        Map<String, Object> data = new HashMap<>();
        data.put("AppId",hosConfigProperties.getAppId());
        data.put("Data", "API.Manage");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Tạo HttpEntity, vàĐặtVui lòngthân yêu cầu và Vui lòngtiêu đề yêu cầu
        HttpEntity<Map<String,Object>> request = new HttpEntity<>(data, headers);
        String response = restTemplate.postForObject(hosConfigProperties.getHosTokenUrl()+ApiUrl.HOS_GET_TOKEN, request,String.class);
        JSONObject jsonObject = JSONObject.parseObject(response);
        JSONObject busData = jsonObject.getJSONObject("BusData").getJSONObject("data");
        String token = busData.getString("Token");
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
