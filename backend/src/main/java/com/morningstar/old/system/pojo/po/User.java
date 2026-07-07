package com.morningstar.old.system.pojo.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "用户对象")
public class User {
    /**
     * 工号
     */
    @Schema(description = "工号")
    private String account;

    /**
     * 姓名
     */
    @Schema(description = "姓名")
    private String name;

    /**
     * 密码
     */
    @Schema(description = "密码")
    private String password;
}