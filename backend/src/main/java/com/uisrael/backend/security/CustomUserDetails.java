package com.uisrael.backend.security;

import com.uisrael.backend.entity.TblUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final TblUser user;

    public CustomUserDetails(TblUser user) {
        this.user = user;
    }

    public Long getIdUser() {
        return user.getIdUser();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String rol = user.getRol() != null ? user.getRol().getNombre() : "USER";
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
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
        return Boolean.TRUE.equals(user.getActivo());
    }
}
