package com.ares.login.controller;


import com.ares.core.service.AresController;
import com.ares.login.bean.LoginRequest;
import com.ares.login.bean.LoginResponse;
import com.ares.login.bean.SdkLoginRequest;
import com.ares.login.bean.SdkLoginResponse;
import com.ares.login.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController implements AresController {
    @Autowired
    private LoginService  loginService;

    @RequestMapping("/sdkLogin")
    @ResponseBody
    public SdkLoginResponse sdkLogin(@RequestBody SdkLoginRequest sdkLoginRequest) {
        return loginService.sdkLogin(sdkLoginRequest);
    }

    @RequestMapping("/login")
    @ResponseBody
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return  loginService.login(loginRequest);
    }
}
