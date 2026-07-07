package com.morningstar.old.system.pojo.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.morningstar.old.system.pojo.po.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {
    private User user;
    private Set<String> permissions;

    @JsonIgnore
    private Collection<? extends GrantedAuthority> authorities;

    // NOTE: 以下字段必须要创建，否则redis的GenericJackson2JsonRedisSerializer反序列化无法成功
    private boolean isAccountNonExpired = true;
    private boolean isCredentialsNonExpired = true;
    private boolean isEnabled = true;

    public LoginUser(User user, Set<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            authorities = permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
        }
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return user.getAccount();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }
}
