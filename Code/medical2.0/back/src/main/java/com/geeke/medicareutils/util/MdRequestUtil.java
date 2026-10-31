package com.geeke.medicareutils.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geeke.medicareutils.db.entity.YbjkLogcontent;
import com.geeke.medicareutils.config.MedicareConfigProperties;
import com.geeke.medicareutils.domain.reqpo.RequestData;
import com.geeke.medicareutils.db.service.YbjkLogcontentService;
import com.geeke.utils.SessionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class MdRequestUtil {
    //Cấu hình BHYTTham số
    private final MedicareConfigProperties medicareConfigProperties;
    //Vui lòngyêu cầuđóng gói
    private  final RestTemplate restTemplate;
    //kýĐếnsố thứ tựBộ nhớ đệm(nàyThay thế bộ nhớ cục bộRedis)
    private String signNoCache;
    private LocalDateTime signNoExpireTime;
    //điện tửĐơn thuốccầnMã hóatập hợp
    private final static List<String> INFO_LIST = Arrays.asList("Ld7801","Ld7802","Ld7101","Ld7104","Ld7202","Ld7102","Ld7805","Ld7804","Ld7806");
    //Ngàynhật kýbảng
    private  final YbjkLogcontentService ybjkLogcontentService;


    /**
     * LấyBHYTThông tin
     * @param infoNo
     * @return
     */
    public JSONObject getMedicareData(String infoNo,JSONObject jsonObject) {
        // Đặt tiêu đề yêu cầu
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestData = createRequestData(infoNo,jsonObject);
        HttpEntity<String> request = new HttpEntity<>(JSON.toJSONString(requestData) ,headers);
        //Ngàynhật kýThông tin
        YbjkLogcontent ybjkLogcontent = new YbjkLogcontent();
        ybjkLogcontent.setTradinumber(infoNo);
        ybjkLogcontent.setHisid(jsonObject.getString(jsonObject.getString("ipt_otp_no")) != null ? jsonObject.getString("ipt_otp_no") : "0");
        ybjkLogcontent.setInhead(requestData);
        ybjkLogcontent.setBegindate(LocalDateTime.now());
        //phátgửiBHYTVui lòngyêu cầu
        String response = restTemplate.postForObject(medicareConfigProperties.getUrl(), request,String.class);
        ybjkLogcontent.setEnddate(LocalDateTime.now());
        ybjkLogcontent.setOuthead(response);
        ybjkLogcontent.setOutcontent(JSONObject.parseObject(response).getString("output"));
        ybjkLogcontent.setErrormsg(JSONObject.parseObject(response).getString("err_msg"));
        //BHYTThông tinNgàynhật kýlưu trữ
        ybjkLogcontentService.save(ybjkLogcontent);
        return JSONObject.parseObject(response).getJSONObject("output");
    }


    /**
     * Tải lên đơn thuốc điện tử
     * @param infoNo
     * @param jsonObject
     * @return
     */
    public JSONObject dzcfUpload(String infoNo,JSONObject jsonObject) {
        // Đặt tiêu đề yêu cầu
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestData = createRequestData(infoNo,jsonObject);
        HttpEntity<String> request = new HttpEntity<>(JSON.toJSONString(requestData) ,headers);
        //phátgửiBHYTVui lòngyêu cầu
        String response = restTemplate.postForObject(medicareConfigProperties.getUrl(), request,String.class);
        //Mã hóamậtvăn bảnTrả vềGiải mã
        String encData=SMUtil.decrypt (JSONObject.parseObject(response).getJSONObject("output").getString("encData"),medicareConfigProperties.getAppId(),medicareConfigProperties.getAppSecret());
        JSONObject.parseObject(response).getJSONObject("output").put("data",encData);
        //BHYTThông tinNgàynhật kýlưu trữ
        return JSONObject.parseObject(response).getJSONObject("output");
    }




    /**
     * LấyBHYTTệpThông tin
     * @param infoNo
     * @return
     */
    public File getMedicareFile(String infoNo, JSONObject object,String fileName) {
        // Đặt tiêu đề yêu cầu
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        String requestData = createRequestData(infoNo,object);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestData, headers);
        File file = new File(fileName);
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(
                medicareConfigProperties.getUrl(),
                HttpMethod.POST,
                requestEntity,
                byte[].class
        );

        HttpStatus statusCode = responseEntity.getStatusCode();
        if (statusCode != HttpStatus.OK) {
            throw new RuntimeException("RestTemplate error status code: " + statusCode);
        }
        String contentType = Objects.requireNonNull(responseEntity.getHeaders().getContentType()).toString();
        if (contentType.contains("application/octet-stream")) {
            byte[] content = responseEntity.getBody();
            if (content != null) {

                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    fileOutputStream.write(content);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return file;
    }



    /**
     * xây dựngchínhthểVui lòngyêu cầuTham số
     * @param infoNo
     * @param
     * @return
     */
    private String createRequestData(String infoNo,JSONObject jsonObject) {
        RequestData requestData = new RequestData();
        //Lấy mã và tên cơ sở y tế được chỉ định
        String FixmedinsCode = SessionUtils.getUserJson().getJSONObject("company").getString("fixmedinsCode");
        String FixmedinsName = SessionUtils.getUserJson().getJSONObject("company").getString("name");
        requestData.setInfno(infoNo);
        //thuậnsố thứ tự(4)
        String orderNum =  "1234";
        //Bên gửiid
        String MsgId =  FixmedinsCode + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+ orderNum;
        requestData.setMsgid(MsgId);
        //Vùng BHYT khám bệnh
        requestData.setMdtrtarea_admvs("value4");
        //Vùng BHYT nơi tham gia bảo hiểm
        requestData.setInsuplc_admdvs("value3");
        //Mã hệ thống bên nhận
        requestData.setRecer_sys_code("value5");
        //Mã thiết bị
        requestData.setDev_no("value6");
        //Thông tin an toàn thiết bị
        requestData.setDev_safe_info("value7");
        //Thông tin chữ ký số của mã an toàn kết nối BHYT
        requestData.setCainfo("value8");
        //Loại chữ ký  Khuyến nghịSM2, SM3
        requestData.setSigntype("value9");
        //Số phiên bản giao diện
        requestData.setInfver("V1.0");
        //1-Người thực hiện; 2-Thiết bị đầu cuối tự phục vụ; 3-Thiết bị đầu cuối di động
        requestData.setOpter_type("value10");
        //Người thực hiện được truyền vào/Mã thiết bị đầu cuối
        requestData.setOpter("000000");
        //Tên người thực hiện
        requestData.setOpter_name("Quản trị viên");
        //Thời gian giao dịch giao diện
        requestData.setInf_time(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        //Đặty tế được chỉ địnhCơ sởmã
        requestData.setFixmedins_code(FixmedinsCode);
        //ĐặtTên cơ sở y tế được chỉ định
        requestData.setFixmedins_name(FixmedinsName);
        //Giao diện check-in
        //Từ nàyđịa phươngBộ nhớ đệm trong LấykýĐếnsố thứ tự
        if(infoNo.equals("9001")){
            requestData.setSign_no("");
        }else{
            String signNo = getSignNoFromCache();
            if (signNo == null) {
                JSONObject signNoData = new JSONObject();
                //TODO kýĐếnTham sốchờchỉ định
                getMedicareSignNo("9001",signNoData);  // gọiPhương thứcTạokýĐếnsố thứ tự
                signNo = getSignNoFromCache();
            }
            requestData.setSign_no(signNo);
        }
        //chínhVui lòngyêu cầuTham số
        JSONObject json = (JSONObject) JSONObject.toJSON(requestData);
        // Cài đặt inputDữ liệu
        if(INFO_LIST.contains(infoNo)){
            //điện tửĐơn thuốccầnMã hóaDữ liệu
            jsonObject.put("appid", medicareConfigProperties.getAppId());
            jsonObject.put("encType","SM4");
            jsonObject.put("signType","SM2");
            //Hiện tạiThời giandấu
            json.put("timestamp",LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            //gốcbắt đầuVui lòngyêu cầuTham số
            JSONObject data = jsonObject.getJSONObject("data");
            String encData = SMUtil.encrypt(data.toJSONString(), medicareConfigProperties.getAppId(), medicareConfigProperties.getAppSecret());
            String signData = SMUtil.sign(jsonObject, medicareConfigProperties.getAppSecret(), medicareConfigProperties.getPrivateKey());
            jsonObject.put("encData",encData);
            jsonObject.put("signData",signData);
            //Xóagốcbắt đầuVui lòngyêu cầuTham số
            jsonObject.remove("data");
        }
        json.put("input",jsonObject);
        return json.toJSONString();
    }



    //kýĐến hoặc kýtrả
    public JSONObject getMedicareSignNo(String infoNo,JSONObject jsonObject) {
        // Đặt tiêu đề yêu cầu
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestData = createRequestData(infoNo,jsonObject);
        HttpEntity<String> request = new HttpEntity<>(requestData ,headers);
        String response = restTemplate.postForObject(medicareConfigProperties.getUrl(), request,String.class);
        if(infoNo.equals("9001")){
            //Lưukhi ngày24điểmcủatrướckýĐếnmã
            signNoCache = JSONObject.parseObject(response).getString("sign_no");
            signNoExpireTime = LocalDateTime.now().toLocalDate().atStartOfDay().plusDays(1);
        }
        if(infoNo.equals("9002")){
            //XóakýĐếnmã
            signNoCache = null;
            signNoExpireTime = null;
        }
        return JSONObject.parseObject(response);
    }

    /** Lấy số thứ tự check-in từ bộ nhớ đệm cục bộ, hết hạnTrả vềnull */
    private String getSignNoFromCache() {
        if (signNoCache != null && LocalDateTime.now().isBefore(signNoExpireTime)) {
            return signNoCache;
        }
        return null;
    }

    public static void main(String[] args) {
RequestData requestData = new RequestData();
    //Lấy mã và tên cơ sở y tế được chỉ định
    //String FixmedinsCode = SessionUtils.getUserJson().getJSONObject("company").getString("fixmedinsCode");
    //String FixmedinsName = SessionUtils.getUserJson().getJSONObject("company").getString("name");
        requestData.setInfno("5101");
    //Bên gửiid
   // String MsgId =  FixmedinsCode + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+ "1234";
     //   requestData.setMsgid(MsgId);
    //Vùng BHYT khám bệnh
        requestData.setMdtrtarea_admvs("value4");
    //Vùng BHYT nơi tham gia bảo hiểm
        requestData.setInsuplc_admdvs("value3");
    //Mã hệ thống bên nhận
        requestData.setRecer_sys_code("value5");
    //Mã thiết bị
        requestData.setDev_no("value6");
    //Thông tin an toàn thiết bị
        requestData.setDev_safe_info("value7");
    //Thông tin chữ ký số của mã an toàn kết nối BHYT
        requestData.setCainfo("value8");
    //Loại chữ ký  Khuyến nghịSM2, SM3
        requestData.setSigntype("value9");
    //Số phiên bản giao diện
        requestData.setInfver("value9");
    //1-Người thực hiện; 2-Thiết bị đầu cuối tự phục vụ; 3-Thiết bị đầu cuối di động
        requestData.setOpter_type("value10");
    //Người thực hiện được truyền vào/Mã thiết bị đầu cuối
        requestData.setOpter("value11");
    //Tên người thực hiện
        requestData.setOpter_name("value12");
    //Thời gian giao dịch giao diện
        requestData.setInf_time("value13");
    //Đặt mã và tên cơ sở y tế được chỉ định
//        requestData.setFixmedins_code(FixmedinsCode);
//        requestData.setFixmedins_name(FixmedinsName);
    //Giao diện check-in
    //Từ redis trong LấykýĐếnsố thứ tự
       // requestData.setSign_no(stringRedisTemplate.opsForValue().get("sign_no"));
//     Map<String, Object> data = BeanUtil.beanToMap(requestData);
//        InputData inputData = new InputData();
//        inputData.setData(data);
//
//        requestData.setInputData(inputData);

        // phân tíchhiệncó của  JSON chuỗi là  JSONObject
        JSONObject jsonObject =  (JSONObject) JSON.toJSON(requestData);

        // phân tíchsố tiềnngoài của  JSON Chuỗi ký tự
        JSONObject additionalData = new JSONObject();
        additionalData.put("data",JSON.toJSON(requestData));

        // Sẽ  additionalData ThêmĐến jsonObject  trong làm là  "input" Nút
        jsonObject.put("input", additionalData);

        // Chuyển đổi thànhnhấtcuối của  JSON Chuỗi ký tự
        String finalJsonString = jsonObject.toJSONString();

        System.err.println(finalJsonString);

        JSONObject data = new JSONObject();
        data.put("hosp_dept_codg","222");
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(data);

        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("data", jsonArray);
        System.err.println(jsonObject1.toJSONString());

        //đơn dòngkhám bệnhThông tin
        JSONObject data1 = new JSONObject();
        //Chẩn đoánnhiều dòngDữ liệu
        JSONArray jsonArray1 = new JSONArray();
        

        //Mô tả chính về tình trạng bệnh TODO
        data1.put("main_cond_dscr", "");
        //Mã loại bệnh    TODO
        data1.put("dise_code", "");
        //Tên loại bệnh TODO
        data1.put("dise_name", "");
        data1.put("birctrl_type", "");
        data1.put("bitctrl_matn_date", "");
        data1.put("matn_type", "");
        data1.put("geso_val", "");
        data1.put("expContent", "");

        //Chẩn đoánTham số
        JSONObject array = new JSONObject();
        //Loại chẩn đoán 1Chẩn đoán tây y 2 trong ychínhbệnhChẩn đoán 3 trong ychínhchứngChẩn đoán 4Phẫu thuậtthao tác 5.Loại bệnh thanh toán theo loại bệnh TODO chỉcho phépmộtCáiChẩn đoán

        //TODO Mặc địnhHiệu lực 1, 0không hợp lệ
        array.put("vali_flag", "1");
        jsonArray1.add(array);
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("mdtrtinfo", data);
        jsonObject2.put("diseinfo", jsonArray1);

        JSONArray array2 = new  JSONArray();
        JSONObject invinfo = new JSONObject();
        invinfo.put("med_list_codg", ""); // Mã danh mục y tế
        invinfo.put("fixmedins_hist_id", ""); // Mã danh mục của cơ sở y tế được chỉ định
        invinfo.put("fixmedins_hist_name", ""); // Tên danh mục của cơ sở y tế được chỉ định
        invinfo.put("rx_flag", ""); // Cờ thuốc kê đơn
        invinfo.put("invdate", ""); // Ngày kiểm kê
        invinfo.put("inv_cnt", ""); // Số lượng tồn kho
        invinfo.put("manu_lotnum", ""); // Số lô sản xuất
        invinfo.put("fixmedins_bchno", ""); // Số thứ tự lô của cơ sở y tế được chỉ định
        invinfo.put("manu_date", ""); // Ngày sản xuất
        invinfo.put("expy_end", ""); // Hết hạn đến
        invinfo.put("memo", ""); // Ghi chú
        invinfo.put("drugtracinfo", ""); // Thông tin nút mã truy xuất
        JSONObject drugtracinfo = new JSONObject();
        drugtracinfo.put("drug_trac_codg","");//Mã truy xuất thuốc
        array2.add(invinfo);
        JSONArray array1 = new JSONArray();
        array1.add(drugtracinfo);
        JSONObject jsonOb = new JSONObject();
        jsonOb.put("invinfo",array2);
        jsonOb.put("drugtracinfo",array1);


    }




}
