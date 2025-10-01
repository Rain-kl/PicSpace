package io.ryan.picspace.common;

import io.ryan.picspace.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 全局响应封装类
 *
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

    public static class Results {

        /**
         * 成功
         *
         * @param data 数据
         * @param <T>  数据类型
         * @return 响应
         */
        public static <T> BaseResponse<T> success(T data) {
            return new BaseResponse<>(0, data, "ok");
        }

        /**
         * 失败
         *
         * @param errorCode 错误码
         * @return 响应
         */
        public static BaseResponse<?> error(ErrorCode errorCode) {
            return new BaseResponse<>(errorCode);
        }

        /**
         * 失败
         *
         * @param code    错误码
         * @param message 错误信息
         * @return 响应
         */
        public static BaseResponse<?> error(int code, String message) {
            return new BaseResponse<>(code, null, message);
        }

        /**
         * 失败
         *
         * @param errorCode 错误码
         * @return 响应
         */
        public static BaseResponse<?> error(ErrorCode errorCode, String message) {
            return new BaseResponse<>(errorCode.getCode(), null, message);
        }
    }
}
