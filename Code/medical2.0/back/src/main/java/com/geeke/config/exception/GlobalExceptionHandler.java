package com.geeke.config.exception;

import com.alibaba.fastjson.JSONObject;
import com.geeke.utils.ResultUtil;
import com.geeke.utils.constants.Constants;
import com.geeke.utils.constants.ErrorEnum;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

/**
 * thống nhấtngoại lệchặn
 * @author: hxy
 * @date: 2017/10/24 10:31
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * xây dựngthống nhấtLỗiphản hồi
     */
    private JSONObject buildErrorResponse(String code, String msg, String msgType) {
        JSONObject json = new JSONObject();
        json.put(Constants.RETURN_CODE, code);
        json.put(Constants.RETURN_MSG, msg);
        json.put(Constants.RETURN_MSG_TYPE, msgType);
        json.put(Constants.RETURN_DATA, new JSONObject());
        return json;
    }

    /**
     * Chungngoại lệchặn — khônghướng tớiKhách hàngđầuhiển thị stackThông tin
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<JSONObject> defaultErrorHandler(HttpServletRequest req, Exception e) {
        logger.error("Vui lòngyêu cầungoại lệ: {} {}", req.getMethod(), req.getRequestURI(), e);
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), ErrorEnum.E_400.getErrorMsg(), Constants.MSG_TYPE_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * Chặn ngoại lệ logic nghiệp vụ — Trả vềcụ thểnghiệp vụThông tin lỗichotrướcđầu
     * dùng để Service tầngném ra của nghiệp vụKiểm trangoại lệ
     */
    @ExceptionHandler(com.geeke.common.service.ServiceException.class)
    public ResponseEntity<JSONObject> serviceExceptionHandler(com.geeke.common.service.ServiceException e) {
        logger.warn("nghiệp vụngoại lệ: {}", e.getMessage());
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), e.getMessage(), Constants.MSG_TYPE_WARNING);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * RuntimeException chặn — Trả về thông tin lỗi, khônghiển thị stack
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<JSONObject> runtimeExceptionHandler(RuntimeException e) {
        logger.error("运 dòng giờngoại lệ: {}", e.getMessage(), e);
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), "Hệ thốngtrongbộLỗi", Constants.MSG_TYPE_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * @Valid Tham sốKiểm traThất bạichặn
     * khi @RequestBody trên của thực thểTrườngkhông满đủ @NotNull  v.v. ràng buộc giờkích hoạt
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JSONObject> validationExceptionHandler(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Tham sốKiểm traThất bại");
        logger.warn("Tham sốKiểm traThất bại: {}", message);
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), message, Constants.MSG_TYPE_WARNING);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * GET/POSTVui lòngyêu cầuPhương thứcLỗi của interceptor
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<JSONObject> httpRequestMethodHandler(HttpRequestMethodNotSupportedException e) {
        logger.warn("Vui lòngyêu cầuPhương thứckhônghỗ trợ: {}", e.getMessage());
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), "Vui lòngyêu cầuPhương thứckhônghỗ trợ: " + e.getMethod(), Constants.MSG_TYPE_ERROR);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(result);
    }

    /**
     * Quyềnkhông đủbáo lỗichặn
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<JSONObject> unauthorizedExceptionHandler() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResultUtil.errorJson(ErrorEnum.E_502));
    }

    /**
     * Vui lòngthân yêu cầuJSONđịnh dạngLỗichặn
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<JSONObject> httpMessageNotReadableHandler(HttpMessageNotReadableException e) {
        logger.warn("Định dạng thân yêu cầu sai: {}", e.getMessage());
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), "Định dạng nội dung yêu cầu sai, vui lòng kiểm tra định dạng JSON", Constants.MSG_TYPE_ERROR);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * thiếuVui lòngyêu cầuTham sốchặn
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<JSONObject> missingParamHandler(MissingServletRequestParameterException e) {
        logger.warn("thiếuVui lòngyêu cầuTham số: {}", e.getMessage());
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), "thiếubắt buộcTham số: " + e.getParameterName(), Constants.MSG_TYPE_ERROR);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 404đường dẫnKhông tồn tạichặn
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<JSONObject> noHandlerFoundHandler(NoHandlerFoundException e) {
        logger.warn("đường dẫnKhông tồn tại: {} {}", e.getHttpMethod(), e.getRequestURL());
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), "Vui lòngyêu cầuđường dẫnKhông tồn tại", Constants.MSG_TYPE_ERROR);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

    /**
     * ChưaĐăng nhậpbáo lỗichặn
     */
    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<JSONObject> unauthenticatedException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResultUtil.errorJson(ErrorEnum.E_20011));
    }
}
