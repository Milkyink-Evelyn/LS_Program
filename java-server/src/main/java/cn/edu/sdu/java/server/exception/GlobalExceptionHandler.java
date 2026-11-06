package cn.edu.sdu.java.server.exception;

import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.util.CommonMethod;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 统一异常处理：所有业务接口都返回 { code, msg, data } 结构。
 * code=0 成功，code=1 业务错误；登录失败和权限不足保留 HTTP 401/403。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public DataResponse handleBusinessException(BusinessException e) {
        return new DataResponse(e.getCode(), null, e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<DataResponse> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new DataResponse(1, null, "用户名或密码错误"));
    }

    /**
     * Token 格式错误、签名错误或已过期时，按未登录处理，避免前端拿到 200 的业务错误。
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<DataResponse> handleJwtException(JwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new DataResponse(1, null, "登录状态已失效，请重新登录"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<DataResponse> handleAccessDeniedException(AccessDeniedException e) {
        String msg = e.getMessage();
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new DataResponse(1, null,
                        msg == null || msg.isBlank() ? "无权限访问" : msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public DataResponse handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(m -> m != null && !m.isBlank())
                .distinct()
                .collect(Collectors.joining("；"));
        return CommonMethod.getReturnMessageError(msg.isEmpty() ? "请求参数不合法" : msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public DataResponse handleMessageNotReadable(HttpMessageNotReadableException e) {
        return CommonMethod.getReturnMessageError("请求参数格式错误");
    }

    /**
     * 查询参数类型不匹配，例如 date 传成了 2026/11/06。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public DataResponse handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        if ("date".equals(e.getName())) {
            return CommonMethod.getReturnMessageError("日期格式错误，应为 yyyy-MM-dd");
        }
        return CommonMethod.getReturnMessageError("参数格式错误：" + e.getName());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public DataResponse handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("数据完整性校验失败", e);
        return CommonMethod.getReturnMessageError("数据存在关联或重复，无法完成操作");
    }

    @ExceptionHandler(Exception.class)
    public DataResponse handleException(Exception e) {
        log.error("未处理的服务器异常", e);
        return CommonMethod.getReturnMessageError("服务器内部错误");
    }
}
