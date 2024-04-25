package com.ares.login.service;

import com.ares.login.bean.LoginRequest;
import com.ares.login.bean.LoginResponse;
import com.ares.login.bean.SdkLoginRequest;
import com.ares.login.bean.SdkLoginResponse;

public interface LoginService {
    SdkLoginResponse sdkLogin(SdkLoginRequest sdkLoginRequest);

    LoginResponse login(LoginRequest loginRequest);
}
