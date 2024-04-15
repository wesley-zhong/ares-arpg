package com.ares.login.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private int areaId;
    private String accountId;
    private  long uid;
    private String secret;
    private String serverAddr;
}
