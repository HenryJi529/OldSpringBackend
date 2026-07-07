package com.morningstar.old.system.service.impl;

import com.morningstar.old.system.pojo.bo.LoginUser;
import com.morningstar.old.system.pojo.po.User;
import com.morningstar.old.system.properties.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        User user = authProperties.getUsers().stream().filter(item -> item.getAccount().equals(account)).findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("用户名不存在"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        List<String> roleTags = authProperties.getUserRoles().entrySet().stream()
                .filter(entry -> entry.getKey().equals(user.getAccount()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(Collections.emptyList());
        List<String> permissionTags = authProperties.getRolePermissions().entrySet().stream()
                .filter(entry -> roleTags.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream()).collect(Collectors.toList());

        return new LoginUser(user, Stream.concat(roleTags.stream().map(item -> "ROLE_" + item), permissionTags.stream()).collect(Collectors.toSet()));
    }
}
