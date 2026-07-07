package com.morningstar.old.system.controller;

import com.morningstar.old.system.pojo.vo.req.LoginRequestVo;
import com.morningstar.old.system.pojo.vo.resp.LoginResponseVo;
import com.morningstar.old.infra.response.R;
import com.morningstar.old.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "用户相关接口定义")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "登入")
    @PostMapping("/auth/login")
    public R<LoginResponseVo> login(@Valid @RequestBody LoginRequestVo vo) {
        return R.ok(userService.login(vo));
    }
}
