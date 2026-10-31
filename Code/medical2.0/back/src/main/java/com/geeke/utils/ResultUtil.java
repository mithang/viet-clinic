package com.geeke.utils;

import com.alibaba.fastjson.JSONObject;
import com.geeke.common.data.R;
import com.geeke.utils.constants.Constants;
import com.geeke.utils.constants.ErrorEnum;

/**
 * @author: lys
 * @description: saumáydịch vụTrả vềGiá trịXử lýLớp công cụ
 * @date: 2010/10/24 10:12
 */
public class ResultUtil {

    /**
     * Trả vềmộtCáireturnData là trốngĐối tượng của Thành côngthông báo của json
     *
     * @return
     */
    public static JSONObject successJson() {
        return successJson(new JSONObject());
    }

    /**
     * Trả về một mã trả về là100 của json
     *
     * @param returnData jsontrong của chínhcầnnội dung
     * @return
     */
    public static JSONObject successJson(Object returnData) {
        return successJson(Constants.SUCCESS_MSG, returnData);
    }

    /**
     * bản nhápLưuThành công giờTrả vềThông tin
     * @param returnData
     * @return
     */
    public static JSONObject successSaveDraft(Object returnData) {
        return successSaveDraft(Constants.SUCCESS_SAVE_DRAFT, returnData);
    }

    /**
     * Trả về một mã trả về là100 của json
     * @param msg
     * @param returnData
     * @return
     */
    public static JSONObject successSaveDraft(String msg, Object returnData) {
        JSONObject resultJson = new JSONObject();
        resultJson.put(Constants.RETURN_MSG_TYPE, Constants.MSG_TYPE_SUCCESS);
        resultJson.put(Constants.RETURN_CODE, Constants.SUCCESS_CODE);
        resultJson.put(Constants.RETURN_MSG, msg);
        resultJson.put(Constants.RETURN_DATA, returnData);
        return resultJson;
    }
    /**
     * Trả về một mã trả về là100 của json
     * @param msg Thông tin
     * @param returnData jsontrong của chínhcầnnội dung
     * @return
     */
    public static JSONObject successJson(String msg, Object returnData) {
        JSONObject resultJson = new JSONObject();
        resultJson.put(Constants.RETURN_MSG_TYPE, Constants.MSG_TYPE_SUCCESS);
        resultJson.put(Constants.RETURN_CODE, Constants.SUCCESS_CODE);
        resultJson.put(Constants.RETURN_MSG, msg);
        resultJson.put(Constants.RETURN_DATA, returnData);
        return resultJson;
    } 
    
    /**
     * Trả vềcảnh báoThông tinJSON
     *
     * @param errorEnum Của mã lỗierrorEnum
     * @return
     */
    public static JSONObject warningJson(ErrorEnum errorEnum) {
        JSONObject resultJson = new JSONObject();
        resultJson.put(Constants.RETURN_MSG_TYPE, Constants.MSG_TYPE_WARNING);
        resultJson.put(Constants.RETURN_CODE, errorEnum.getErrorCode());
        resultJson.put(Constants.RETURN_MSG, errorEnum.getErrorMsg());
        return resultJson;
    }    

    /**
     * Trả vềcảnh báoThông tinJSON
     * @param errorEnum Của mã lỗierrorEnum
     * @param msg tùy chỉnh của Thông tin
     * @return
     */
    public static JSONObject warningJson(ErrorEnum errorEnum, String msg) {
        JSONObject resultJson = new JSONObject();
        resultJson.put(Constants.RETURN_MSG_TYPE, Constants.MSG_TYPE_WARNING);
        resultJson.put(Constants.RETURN_CODE, errorEnum.getErrorCode());
        resultJson.put(Constants.RETURN_MSG, msg);
        return resultJson;
    }     
    
    /**
     * Trả về thông tin lỗiJSON
     *
     * @param errorEnum Của mã lỗierrorEnum
     * @return
     */
    public static JSONObject errorJson(ErrorEnum errorEnum) {
        JSONObject resultJson = new JSONObject();
        resultJson.put(Constants.RETURN_MSG_TYPE, Constants.MSG_TYPE_ERROR);
        resultJson.put(Constants.RETURN_CODE, errorEnum.getErrorCode());
        resultJson.put(Constants.RETURN_MSG, errorEnum.getErrorMsg());
        return resultJson;
    }
    
    /**
     * Trả về thông tin lỗiJSON
     * @param errorEnum Của mã lỗierrorEnum
     * @param msg tùy chỉnh của Thông tin
     * @return
     */
    public static JSONObject errorJson(ErrorEnum errorEnum, String msg) {
        JSONObject resultJson = new JSONObject();
        resultJson.put(Constants.RETURN_MSG_TYPE, Constants.MSG_TYPE_ERROR);
        resultJson.put(Constants.RETURN_CODE, errorEnum.getErrorCode());
        resultJson.put(Constants.RETURN_MSG, msg);
        return resultJson;
    }

    // ==================== R<T> genericphản hồicầutiếpPhương thức ====================
    // Code mới nên dùng trực tiếp R.ok() / R.warning() / R.error()
    // nàymột sốPhương thứclàm là quachuyển tiếpkỳ của tương thíchcầutiếp

    /**
     * Thành côngphản hồi(Bản generic)
     */
    public static <T> R<T> rOk(T data) {
        return R.ok(data);
    }

    /**
     * cảnh báophản hồi(Bản generic)
     */
    public static <T> R<T> rWarning(ErrorEnum errorEnum) {
        return R.warning(errorEnum);
    }

    /**
     * Lỗiphản hồi(Bản generic)
     */
    public static <T> R<T> rError(ErrorEnum errorEnum) {
        return R.error(errorEnum);
    }
}
