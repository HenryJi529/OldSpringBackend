package com.morningstar.old.system.pojo.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "登入请求对象")
@Data
public class LoginRequestVo {
    @NotBlank(message = "账号不能为空")
    @Schema(description = "账号")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;
}
