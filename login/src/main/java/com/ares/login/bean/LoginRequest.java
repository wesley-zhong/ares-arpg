package com.ares.login.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String accountId;
    private String channel;
    private String password;
}
