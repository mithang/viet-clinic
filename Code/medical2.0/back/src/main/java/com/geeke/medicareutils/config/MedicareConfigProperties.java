package com.geeke.medicareutils.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * BHYTgiao diệncấu hìnhTham số
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.medicare")
public  class MedicareConfigProperties {
    //Có hay khôngmởBHYTgiao diện
    private String check;
    //Tỉnhphần
    private String province;
    //BHYTgiao diệnĐịa chỉ
    private String url;
    //BHYT trong giữatệpgiao diệnĐịa chỉ
    private String webUrl;
    //APPID
    private String appId;
    //nêndùngkhóa bí mật
    private String appSecret;
    //khóa riêng
    private String privateKey;

    //đầumiệngsố
    private String webPort;

    private String isDemo;
}
