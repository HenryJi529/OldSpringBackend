package com.morningstar.old.system.util;

import com.morningstar.old.system.pojo.bo.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (LoginUser) authentication.getPrincipal();
    }
    public static String getUserId() {
        return getLoginUser().getUser().getAccount();
    }
}
