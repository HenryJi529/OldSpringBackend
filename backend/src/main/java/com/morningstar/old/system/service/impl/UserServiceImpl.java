package com.morningstar.old.system.service.impl;

import com.morningstar.old.infra.constant.RedisConstant;
import com.morningstar.old.infra.exception.BaseException;
import com.morningstar.old.infra.response.ResponseCode;
import com.morningstar.old.system.pojo.vo.req.LoginRequestVo;
import com.morningstar.old.system.pojo.vo.resp.LoginResponseVo;
import com.morningstar.old.system.properties.JwtProperties;
import com.morningstar.old.system.service.UserService;
import com.morningstar.old.system.pojo.bo.LoginUser;
import com.morningstar.old.system.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProperties jwtProperties;
    private final JwtUtil jwtUtil;

    public LoginResponseVo login(LoginRequestVo vo) {
        // 验证密码
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(vo.getAccount(), vo.getPassword());
        Authentication authenticate;
        try {
            authenticate = authenticationManager.authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            if (e instanceof BadCredentialsException) {
                throw new BaseException(ResponseCode.USERNAME_OR_PASSWORD_ERROR);
            } else if (e instanceof LockedException) {
                throw new BaseException(ResponseCode.ACCOUNT_LOCKED);
            } else {
                throw new BaseException(ResponseCode.AUTHENTICATION_FAILED);
            }
        }

        // 获取登录用户信息，并存入redis
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        redisTemplate.opsForValue().set(RedisConstant.AUTH_LOGIN + RedisConstant.KEY_SEPARATOR + loginUser.getUser().getAccount(), loginUser, jwtProperties.getTtl(), TimeUnit.MILLISECONDS);

        // 组装登录成功数据
        return new LoginResponseVo(loginUser.getUser().getAccount(), jwtUtil.create(loginUser.getUser().getAccount(), loginUser.getUsername()));
    }
}
