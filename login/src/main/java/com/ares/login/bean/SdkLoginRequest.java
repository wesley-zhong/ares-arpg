package com.ares.login.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SdkLoginRequest {
    private String accountId;
    private String token; //sdk token
    private int areaId;
}
