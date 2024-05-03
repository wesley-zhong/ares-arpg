package com.ares.login.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String accountId;
    private  long uid;
    private String token;
    private String serverIp;
    private int port;
}
