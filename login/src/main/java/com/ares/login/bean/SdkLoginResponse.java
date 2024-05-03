package com.ares.login.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SdkLoginResponse {
    private String accountId;
    private long uid;
    private int areaId;
    private String secret; //server  token
}
