package com.services.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@RequiredArgsConstructor
public class LdapConfig {

    private final LdapProperties ldapProperties;

    @Bean
    @ConditionalOnProperty(name = "ldap.enabled", havingValue = "true")
    public LdapContextSource ldapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapProperties.getUrls());
        contextSource.setBase(ldapProperties.getBaseDn());
        if (ldapProperties.getManagerDn() != null && !ldapProperties.getManagerDn().isBlank()) {
            contextSource.setUserDn(ldapProperties.getManagerDn());
            contextSource.setPassword(ldapProperties.getManagerPassword());
        }
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Bean
    @ConditionalOnProperty(name = "ldap.enabled", havingValue = "true")
    public LdapTemplate ldapTemplate(LdapContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }
}
