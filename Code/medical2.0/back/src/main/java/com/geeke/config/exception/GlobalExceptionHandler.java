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
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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
        logger.error("Ngoại lệ khi vận hành: {}", e.getMessage(), e);
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), "Hệ thống trong bộ Lỗi", Constants.MSG_TYPE_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * DS94: SQL sai nguphap — trong app nay chi xay ra khi tham so do
     * client gui tao ra cau SQL khong hop le (sort key khong ton tai
     * qua SearchParams.getOrderby, hoac ten cot loc khong ton tai qua
     * Common.whereParams); SQL cua server la XML tinh. Truoc day
     * BadSqlGrammarException roi vao RuntimeException handler -> 500,
     * trong khi day la loi dau vao cua client -> 400. Handler nay dat
     * o mot cho nen bao phu moi duong list (CrudService.listPage/
     * listAll/paginate va cac override goi thang dao.listPage nhu
     * DictItemService) ma khong phai sua tung service. Luu y: neu mot
     * mapper cua server tu hong thi loi do cung ve 400 thay vi 500 --
     * log warn ben duoi van giu message SQL day du de phan biet.
     */
    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<JSONObject> badSqlGrammarExceptionHandler(BadSqlGrammarException e) {
        logger.warn("Câu lệnh SQL không hợp lệ (tham số sắp xếp/lọc): {}", e.getMessage());
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), "Tham số sắp xếp hoặc lọc không hợp lệ", Constants.MSG_TYPE_WARNING);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * @Valid Tham sốKiểm traThất bạichặn
     * khi @RequestBody trên của thực thểTrườngkhôngđủđủ @NotNull  v.v. ràng buộc giờkích hoạt
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JSONObject> validationExceptionHandler(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Tham số Kiểm tra Thất bại");
        logger.warn("Tham số Kiểm tra Thất bại: {}", message);
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), message, Constants.MSG_TYPE_WARNING);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * GET/POSTVui lòngyêu cầuPhương thứcLỗi của interceptor
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<JSONObject> httpRequestMethodHandler(HttpRequestMethodNotSupportedException e) {
        logger.warn("Vui lòng yêu cầu Phương thức không hỗ trợ: {}", e.getMessage());
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), "Vui lòng yêu cầu Phương thức không hỗ trợ:" + e.getMethod(), Constants.MSG_TYPE_ERROR);
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
     * DS96: Content-Type khong dung (gui JSON body kem
     * Content-Type text/plain) -> HttpMediaTypeNotSupportedException,
     * va @RequestParam int nhan gia tri khong phai so (limit=abc)
     * -> MethodArgumentTypeMismatchException. Ca hai truoc day roi
     * vao RuntimeException handler -> 500, trong khi day la loi dau
     * vao cua client -> 415/400. Dat o mot cho nen bao phu moi
     * endpoint, ke ca cac endpoint khong co @RequestBody.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<JSONObject> httpMediaTypeNotSupportedHandler(HttpMediaTypeNotSupportedException e) {
        logger.warn("Loại nội dung yêu cầu không được hỗ trợ: {}", e.getMessage());
        JSONObject result = buildErrorResponse(ErrorEnum.E_415.getErrorCode(), "Loại nội dung yêu cầu không được hỗ trợ", Constants.MSG_TYPE_ERROR);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(result);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<JSONObject> methodArgumentTypeMismatchHandler(MethodArgumentTypeMismatchException e) {
        logger.warn("Tham số yêu cầu không hợp lệ: {}", e.getMessage());
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), "Tham số yêu cầu không hợp lệ", Constants.MSG_TYPE_WARNING);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * thiếuVui lòngyêu cầuTham sốchặn
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<JSONObject> missingParamHandler(MissingServletRequestParameterException e) {
        logger.warn("thiếu Vui lòng yêu cầu Tham số: {}", e.getMessage());
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), "thiếu bắt buộc Tham số:" + e.getParameterName(), Constants.MSG_TYPE_ERROR);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 404đường dẫnKhông tồn tạichặn
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<JSONObject> noHandlerFoundHandler(NoHandlerFoundException e) {
        logger.warn("đường dẫn Không tồn tại: {} {}", e.getHttpMethod(), e.getRequestURL());
        JSONObject result = buildErrorResponse(ErrorEnum.E_400.getErrorCode(), "Vui lòng yêu cầu đường dẫn Không tồn tại", Constants.MSG_TYPE_ERROR);
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
