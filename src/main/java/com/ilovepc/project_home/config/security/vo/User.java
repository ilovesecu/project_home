package com.ilovepc.project_home.config.security.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class User {
    private Long userNo;
    private String email;
    private String password;
    private String nickname;
    private String createdAt;
    private String updatedAt;

    private List<Role> roles;

    @Builder
    public User(String email, String password, List<Role> roles) {
        this.email = email;
        this.password = password;
        this.roles = roles;
    }
}
