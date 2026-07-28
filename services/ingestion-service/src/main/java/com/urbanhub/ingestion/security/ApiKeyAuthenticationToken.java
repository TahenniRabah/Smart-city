package com.urbanhub.ingestion.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class ApiKeyAuthenticationToken
        extends AbstractAuthenticationToken {

    private final String principal;

    public ApiKeyAuthenticationToken(String principal) {
        super(List.of(
                new SimpleGrantedAuthority("ROLE_SENSOR")
        ));

        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
