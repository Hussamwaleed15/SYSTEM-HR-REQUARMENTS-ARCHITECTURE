package com.services.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LdapUserData {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String department;
    private String ldapDn;
}
