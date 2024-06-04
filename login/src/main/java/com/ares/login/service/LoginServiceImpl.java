package com.ares.login.service;

import com.ares.common.bean.ServerType;
import com.ares.core.exception.AresBaseException;
import com.ares.core.utils.IdUtils;
import com.ares.dal.game.UserTokenService;
import com.ares.discovery.DiscoveryService;
import com.ares.login.bean.LoginRequest;
import com.ares.login.bean.LoginResponse;
import com.ares.login.bean.SdkLoginRequest;
import com.ares.login.bean.SdkLoginResponse;
import com.ares.login.dal.AccountDAO;
import com.ares.login.dal.DO.AccountDO;
import com.ares.login.discovery.OnDiscoveryWatchService;
import com.ares.login.uidgenerator.UidGenerator;
import com.ares.transport.bean.ServerNodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginServiceImpl implements LoginService {
    @Lazy
    @Autowired
    private DiscoveryService discoveryService;
    @Autowired
    private OnDiscoveryWatchService onDiscoveryWatch;
    @Autowired
    private AccountDAO accountDAO;
    @Autowired
    private UserTokenService userTokenService;
    @Autowired
    private UidGenerator uidGenerator;

    @Override
    public SdkLoginResponse sdkLogin(SdkLoginRequest sdkLoginRequest) {

        return null;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String accountIndex = createAccountIndex(loginRequest);
        AccountDO accountDO = accountDAO.getSingle(accountIndex);
        if (accountDO == null) {
            long uid = uidGenerator.getNextId(List.of(1));
            accountDO = new AccountDO();
            accountDO.setId(accountIndex);
            accountDO.setUid(uid);
            accountDO.setChannel(loginRequest.getChannel());
            accountDAO.insert(accountDO);
        }
        ServerNodeInfo lowerLoadGameServer = onDiscoveryWatch.getLowerLoadGameServer(ServerType.GATEWAY);
        if (lowerLoadGameServer == null) {
            throw new AresBaseException(-1, "XXXXXXXXX login request = " + loginRequest + " not found gateway");
        }
        LoginResponse LoginResponse = new LoginResponse();
        LoginResponse.setAccountId(loginRequest.getAccountId());
        LoginResponse.setPort(lowerLoadGameServer.getPort());
        LoginResponse.setServerIp(lowerLoadGameServer.getIp());
        LoginResponse.setUid(accountDO.getUid());
        String gameLoginToken = IdUtils.generate();
        LoginResponse.setToken(gameLoginToken);
        userTokenService.saveToken(accountDO.getUid(), gameLoginToken);
        return LoginResponse;
    }

    private String createAccountIndex(LoginRequest loginRequest) {
        return loginRequest.getAccountId() + loginRequest.getChannel();
    }
}
