package cn.edu.sdu.java.server.exception;

/**
 * 业务异常，由 Service 主动抛出，统一由 GlobalExceptionHandler 转成 DataResponse。
 */
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        this(1, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
