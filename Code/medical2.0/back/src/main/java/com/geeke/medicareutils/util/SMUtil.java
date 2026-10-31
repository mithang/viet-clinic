package com.geeke.medicareutils.util;

import com.alibaba.fastjson.JSONObject;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SMUtil {

    /**
     * Mã hóa
     *
     * @param data
     * @param appId
     * @param appSecret
     * @return
     */
    public static String encrypt(String data, String appId, String appSecret) {
        // dùng appId Mã hóa appSecret Lấymớibí mậtkhóa
        byte[] appSecretEncData = EasyGmUtils.sm4Encrypt(appId.substring(0, 16).getBytes(StandardCharsets.UTF_8), appSecret.getBytes(StandardCharsets.UTF_8));
        // mớibí mậtkhóachuỗi
        byte[] secKey = Hex.toHexString(appSecretEncData).toUpperCase().substring(0, 16).getBytes(StandardCharsets.UTF_8);
        // Mã hóaDữ liệu
        return Hex.toHexString(EasyGmUtils.sm4Encrypt(secKey, data.getBytes(StandardCharsets.UTF_8))).toUpperCase();
    }

    /**
     * Giải mã
     *
     * @param data
     * @param appId
     * @param appSecret
     * @return
     */
    public static String decrypt(String data, String appId, String appSecret) {
        byte[] appSecretEncDataDecode = EasyGmUtils.sm4Encrypt(appId.substring(0, 16).getBytes(StandardCharsets.UTF_8), appSecret.getBytes(StandardCharsets.UTF_8));
        byte[] secKeyDecode = Hex.toHexString(appSecretEncDataDecode).toUpperCase().substring(0, 16).getBytes(StandardCharsets.UTF_8);
        return new String(EasyGmUtils.sm4Decrypt(secKeyDecode, Hex.decode(data)), StandardCharsets.UTF_8);
    }

    /**
     * chữ ký
     *
     * @param jsonObject
     * @param appSecret
     * @param privateKey
     * @return
     */
    public static String sign(JSONObject jsonObject, String appSecret, String privateKey) {
        // Lấychữ kýchuỗi
        byte[] signText = SignUtil.getSignText(jsonObject, appSecret).getBytes(StandardCharsets.UTF_8);
        byte[] userId = appSecret.getBytes(StandardCharsets.UTF_8);
        byte[] prvkey = Base64.getDecoder().decode(privateKey);
        return Base64.getEncoder().encodeToString(EasyGmUtils.signSm3WithSm2(signText, userId, prvkey));
    }

    /**
     * Xác minh chữ ký
     *
     * @param jsonObject
     * @param appSecret
     * @param publicKey
     * @param responseSign
     * @return
     */
    public static Boolean verify(JSONObject jsonObject, String appSecret, String publicKey, String responseSign) {
        // Xác minh chữ ký
        byte[] msg = SignUtil.getSignText(jsonObject, appSecret).getBytes(StandardCharsets.UTF_8);
        byte[] userIdDecode = appSecret.getBytes(StandardCharsets.UTF_8);
        byte[] pubkey = Base64.getDecoder().decode(publicKey);
        byte[] signData = Base64.getDecoder().decode(responseSign);
        return EasyGmUtils.verifySm3WithSm2(msg, userIdDecode, signData, pubkey);
    }

    /**
     * chữ ký
     *
     * @param jsonString
     * @param appSecret
     * @param privateKey
     * @return
     */
    public static String sign(String jsonString, String appSecret, String privateKey) {
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        return sign(jsonObject, appSecret, privateKey);
    }

    /**
     * Xác minh chữ ký
     *
     * @param jsonString
     * @param appSecret
     * @param publicKey
     * @param responseSign
     * @return
     */
    public static Boolean verify(String jsonString, String appSecret, String publicKey, String responseSign) {
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        return verify(jsonObject, appSecret, publicKey, responseSign);
    }
}
