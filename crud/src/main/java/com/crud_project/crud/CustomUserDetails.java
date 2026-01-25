package com.crud_project.crud;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.annotation.Nullable;
import lombok.Builder;

public class CustomUserDetails implements UserDetails, CredentialsContainer {

    private final Integer id;
    private final String username;
    private @Nullable String password;
    private final Collection<? extends GrantedAuthority> authorities;

    @Builder
    public CustomUserDetails(
            Integer id,
            String username,
            String password,
            Collection<String> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = getSetOfAuthorities(Collections.unmodifiableSet((Set<String>) authorities));
    }

    @Override
    public void eraseCredentials(){
        this.password = null;
    }

    public Integer getId() {
        return this.id;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override public String getPassword() {
        return this.password;
    }

    @Override public String getUsername() {
        return this.username;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    public static class CustomGrantedAuthority implements GrantedAuthority {
        
        private final String authority;
        public CustomGrantedAuthority(String authority) {
            this.authority = authority;
        }
        @Override
        @Nullable public String getAuthority(){
            return this.authority;
        }
    }

    /**
     * Prepends ROLE_ to each list element
     * @return
     */
    public static Set<CustomGrantedAuthority> getSetOfAuthorities(Set<String> collection){
        return collection.stream()
            .filter(role -> !role.isEmpty())
            .map(role -> new CustomGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toSet());
    }
}
