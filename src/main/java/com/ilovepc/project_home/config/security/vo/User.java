package com.ilovepc.project_home.config.security.vo;

import lombok.Builder;

import java.util.List;

public class User {
    private Long userNo;
    private String username;
    private String password;
    private List<Role> roles;

    @Builder
    public User(String username, String password, List<Role> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }
}
