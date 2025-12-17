package com.music.common;

import java.io.Serializable;

/**
 * 统一响应结果类
 * 用于封装API返回数据
 *
 * @param <T> 数据类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 无参构造函数
     */
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 全参构造函数
     */
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应 - 无数据
     */
    public static <T> Result<T> success() {
        return new Result<>(Constants.ResponseCode.SUCCESS, Constants.ResponseMessage.SUCCESS, null);
    }

    /**
     * 成功响应 - 有数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(Constants.ResponseCode.SUCCESS, Constants.ResponseMessage.SUCCESS, data);
    }

    /**
     * 成功响应 - 自定义消息
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(Constants.ResponseCode.SUCCESS, message, data);
    }

    /**
     * 失败响应 - 默认消息
     */
    public static <T> Result<T> error() {
        return new Result<>(Constants.ResponseCode.ERROR, Constants.ResponseMessage.ERROR, null);
    }

    /**
     * 失败响应 - 自定义消息
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(Constants.ResponseCode.ERROR, message, null);
    }

    /**
     * 失败响应 - 自定义状态码和消息
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应 - 完整参数
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 未授权响应
     */
    public static <T> Result<T> unauthorized() {
        return new Result<>(Constants.ResponseCode.UNAUTHORIZED, Constants.ResponseMessage.UNAUTHORIZED, null);
    }

    /**
     * 无权限响应
     */
    public static <T> Result<T> forbidden() {
        return new Result<>(Constants.ResponseCode.FORBIDDEN, Constants.ResponseMessage.FORBIDDEN, null);
    }

    /**
     * 资源不存在响应
     */
    public static <T> Result<T> notFound() {
        return new Result<>(Constants.ResponseCode.NOT_FOUND, Constants.ResponseMessage.NOT_FOUND, null);
    }

    /**
     * 参数错误响应
     */
    public static <T> Result<T> badRequest(String message) {
        return new Result<>(Constants.ResponseCode.BAD_REQUEST, message, null);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return Constants.ResponseCode.SUCCESS.equals(this.code);
    }

    // Getters and Setters
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}
