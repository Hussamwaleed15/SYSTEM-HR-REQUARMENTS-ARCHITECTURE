package com.services.auth.service;

import com.services.auth.config.LdapProperties;
import com.services.auth.dto.LdapUserData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LdapAuthService {

    private final LdapProperties ldapProperties;
    private final ObjectProvider<LdapTemplate> ldapTemplateProvider;

    public Optional<LdapUserData> authenticate(String username, String password) {
        if (!ldapProperties.isEnabled()) {
            return Optional.empty();
        }

        LdapTemplate ldapTemplate = ldapTemplateProvider.getIfAvailable();
        if (ldapTemplate == null) {
            log.warn("LDAP is enabled in properties, but LdapTemplate bean is not available.");
            return Optional.empty();
        }

        if (password == null || password.isEmpty()) {
            log.warn("LDAP authentication failed: empty password for user '{}'", username);
            return Optional.empty();
        }

        try {
            String base = ldapProperties.getUserSearchBase() != null ? ldapProperties.getUserSearchBase() : "";
            String rawFilter = ldapProperties.getUserSearchFilter() != null ? ldapProperties.getUserSearchFilter() : "(uid={0})";
            String formattedFilter = rawFilter.replace("{0}", username);

            log.info("Attempting LDAP authentication for user '{}' with base '{}' and filter '{}'", username, base, formattedFilter);

            boolean authenticated = ldapTemplate.authenticate(base, formattedFilter, password);
            if (!authenticated) {
                log.warn("LDAP authentication failed for user '{}': invalid credentials", username);
                return Optional.empty();
            }

            log.info("LDAP authentication successful for user '{}'", username);

            List<LdapUserData> users = ldapTemplate.search(
                    base,
                    formattedFilter,
                    (AttributesMapper<LdapUserData>) attributes -> mapAttributesToUserData(username, attributes)
            );

            if (users.isEmpty()) {
                LdapUserData defaultData = LdapUserData.builder()
                        .username(username)
                        .email(username + "@company.com")
                        .firstName(username)
                        .lastName("")
                        .department("General")
                        .ldapDn("uid=" + username + "," + ldapProperties.getBaseDn())
                        .build();
                return Optional.of(defaultData);
            }

            return Optional.of(users.get(0));

        } catch (Exception e) {
            log.error("LDAP authentication encountered an error for user '{}': {}", username, e.getMessage());
            return Optional.empty();
        }
    }

    private LdapUserData mapAttributesToUserData(String username, Attributes attributes) throws NamingException {
        String email = getAttributeValue(attributes, "mail");
        if (email == null || email.isBlank()) {
            email = getAttributeValue(attributes, "userPrincipalName");
        }
        if (email == null || email.isBlank()) {
            email = username + "@company.com";
        }

        String firstName = getAttributeValue(attributes, "givenName");
        if (firstName == null || firstName.isBlank()) {
            firstName = username;
        }

        String lastName = getAttributeValue(attributes, "sn");
        if (lastName == null) {
            lastName = "";
        }

        String department = getAttributeValue(attributes, "department");
        if (department == null || department.isBlank()) {
            department = getAttributeValue(attributes, "ou");
        }
        if (department == null) {
            department = "General";
        }

        String distinguishedName = getAttributeValue(attributes, "distinguishedName");
        if (distinguishedName == null) {
            distinguishedName = "uid=" + username + "," + ldapProperties.getBaseDn();
        }

        return LdapUserData.builder()
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .department(department)
                .ldapDn(distinguishedName)
                .build();
    }

    private String getAttributeValue(Attributes attributes, String attributeName) {
        try {
            if (attributes != null && attributes.get(attributeName) != null && attributes.get(attributeName).get() != null) {
                return attributes.get(attributeName).get().toString();
            }
        } catch (NamingException ignored) {
        }
        return null;
    }
}
