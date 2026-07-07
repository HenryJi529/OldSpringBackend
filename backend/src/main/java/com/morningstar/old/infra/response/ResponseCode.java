package com.morningstar.old.infra.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 业务响应码
 */
@Getter
@Schema(description = "业务响应码")
public enum ResponseCode {
    SUCCESS("操作成功"),
    ERROR("操作失败"),

    CHECK_CODE_TIMEOUT("验证码过期"),
    CHECK_CODE_ERROR("验证码错误"),
    ID_MISMATCH("请求体中的id与路径id不一致"),

    AUTHENTICATION_FAILED("认证失败"),
    TOKEN_INVALID("凭证(令牌)无效"),
    TOKEN_EXPIRED("凭证(令牌)过期"),
    USERNAME_OR_PASSWORD_ERROR("用户名或密码错误"),
    NO_PERMISSION("没有权限访问该资源"),
    ACCOUNT_INFO_ERROR("账号信息错误(可能出现了重复)"),
    USERNAME_ALREADY_EXISTS("用户名已存在"),
    ACCOUNT_NOT_FOUND("账号不存在"),
    EMAIL_ALREADY_EXISTS("邮箱已存在"),
    ACCOUNT_LOCKED("账号已锁定"),
    ROLE_NOT_EXISTS("角色不存在"),

    SYS_PARAM_CREATE_FAILED("系统参数创建失败: %s"),
    SYS_PARAM_UPDATE_FAILED("系统参数[%s]更新失败: %s"),
    SYS_PARAM_DELETE_FAILED("系统参数[%d]删除失败"),
    SYS_PARAM_ID_NOT_FOUND("系统参数[%d]不存在"),
    SYS_PARAM_NAME_NOT_FOUND("系统参数\"%s\"不存在"),

    SYS_PARAM_ACCESS_DENIED("您无权访问系统参数[%s]");

    private final String message;

    ResponseCode(String message) {
        this.message = message;
    }
}
