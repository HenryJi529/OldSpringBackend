package com.morningstar.old.system.service;

import com.morningstar.old.system.pojo.vo.req.LoginRequestVo;
import com.morningstar.old.system.pojo.vo.resp.LoginResponseVo;

public interface UserService {
    LoginResponseVo login(LoginRequestVo vo);
}
