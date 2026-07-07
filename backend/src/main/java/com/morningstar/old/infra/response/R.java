package com.morningstar.old.infra.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 标准响应对象
 *
 * @param <T>
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "标准响应对象")
public class R<T> implements Serializable {
    // 响应码
    @Schema(description = "响应码")
    private String code;
    // 消息
    @Schema(description = "消息")
    private String msg;
    // 数据
    @Schema(description = "数据")
    private T data;

    private R(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private R(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(ResponseCode.SUCCESS.name(), ResponseCode.SUCCESS.getMessage());
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResponseCode.SUCCESS.name(), ResponseCode.SUCCESS.getMessage(), data);
    }

    public static <T> R<T> error() {
        return new R<>(ResponseCode.ERROR.name(), ResponseCode.ERROR.getMessage());
    }

    public static <T> R<T> error(String msg) {
        return new R<>(ResponseCode.ERROR.name(), msg);
    }

    public static <T> R<T> error(ResponseCode res) {
        return new R<>(res.name(), res.getMessage());
    }

    public static <T> R<T> error(String code, String msg) {
        return new R<>(code, msg);
    }
}