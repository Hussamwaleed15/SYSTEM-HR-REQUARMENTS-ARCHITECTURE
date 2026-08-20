package com.services.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ldap")
@Data
public class LdapProperties {
    private boolean enabled = false;
    private String urls = "ldap://localhost:389";
    private String baseDn = "dc=example,dc=com";
    private String userSearchBase = "ou=users";
    private String userSearchFilter = "(uid={0})";
    private String managerDn = "cn=admin,dc=example,dc=com";
    private String managerPassword = "admin";
}
