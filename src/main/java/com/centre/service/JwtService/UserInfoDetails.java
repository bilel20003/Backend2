package com.centre.service.JwtService;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.centre.service.model.UserInfo;

public class UserInfoDetails implements UserDetails {

    private String name;
    private String password;
    private String status;
    private List<GrantedAuthority> authorities;

    public UserInfoDetails(UserInfo userInfo) {
        this.name = userInfo.getName();
        this.password = userInfo.getPassword();
        this.status = userInfo.getStatus();
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

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Compte non expiré par défaut
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Compte non bloqué par défaut
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Informations d'identification non expirées par défaut
    }

    @Override
    public boolean isEnabled() {
        return "true".equalsIgnoreCase(status); // Compte activé si le statut est "true"
    }
}