package com.morningstar.old.system.properties;

import com.morningstar.old.system.pojo.po.Permission;
import com.morningstar.old.system.pojo.po.Role;
import com.morningstar.old.system.pojo.po.User;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.auth")
@Data
public class AuthProperties {
    private List<User> users;
    private List<Role> roles;
    private List<Permission> permissions;
    private Map<String, List<String>> rolePermissions;
    private Map<String, List<String>> userRoles;
}
