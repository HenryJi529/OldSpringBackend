package com.morningstar.old.demo.controller;

import com.morningstar.old.system.util.AuthUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "示例相关接口定义")
@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
@Slf4j
public class DemoController {
    @GetMapping("/system")
    @PreAuthorize("hasAuthority('sys:user:manage')")
    public String system() {
        log.info("用户工号: {}", AuthUtil.getUserId());
        return "Hello, you are system admin!";
    }

    @GetMapping("/survey")
    @PreAuthorize("hasAuthority('sys:survey:add')")
    public String survey() {
        return "Hello, you are survey admin!";
    }

    @GetMapping("/authed")
    public String authed() {
        return "Hello, you are authed!";
    }

    @GetMapping("/white")
    public String white() {
        return "Hello, all users!";
    }
}
