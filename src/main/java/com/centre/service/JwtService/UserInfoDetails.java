package com.centre.service.JwtService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.centre.service.model.UserInfo;

public class UserInfoDetails implements UserDetails {

    private String name;
    private String password;
    private String status;
    private List<GrantedAuthority> authorities;

    public UserInfoDetails(UserInfo userInfo) {
        this.name = userInfo.getEmail(); // utilise bien l'email comme username
        this.password = userInfo.getPassword();
        this.status = userInfo.getStatus();
        this.authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + userInfo.getRole().name()) // ex: ROLE_ADMIN
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "true".equalsIgnoreCase(status);
    }
}
