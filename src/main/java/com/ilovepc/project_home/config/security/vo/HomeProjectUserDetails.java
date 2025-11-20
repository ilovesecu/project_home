package com.ilovepc.project_home.config.security.vo;

import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class HomeProjectUserDetails implements UserDetails {
    //private final User user;

    private int userNo;
    private String email;
    private String password;
    private String nickname;
    private String createdAt;
    private String emailVerifiedYn;
    private List<GrantedAuthority> authorities;

    @Builder
    public HomeProjectUserDetails(int userNo, String email, String nickname, String password, String createdAt, String emailVerifiedYn, List<GrantedAuthority> authorities) {
        this.userNo = userNo;
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.createdAt = createdAt;
        this.emailVerifiedYn = emailVerifiedYn;
        this.authorities = authorities;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        /*Collection<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));

        //이메일 완료된 경우에만 USER부여
        if("y".equals(user.getEmailVerifiedYn())){
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }*/
        return  this.authorities;
        /*return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());*/
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {return this.email;}

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
        return true;
    }
}
