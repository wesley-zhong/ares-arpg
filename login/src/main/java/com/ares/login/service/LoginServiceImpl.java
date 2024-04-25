package com.ares.login.service;

import com.ares.common.bean.ServerType;
import com.ares.core.exception.AresBaseException;
import com.ares.core.utils.IdUtils;
import com.ares.core.utils.SnowFlake;
import com.ares.discovery.DiscoveryService;
import com.ares.login.bean.LoginRequest;
import com.ares.login.bean.LoginResponse;
import com.ares.login.bean.SdkLoginRequest;
import com.ares.login.bean.SdkLoginResponse;
import com.ares.login.dal.AccountDAO;
import com.ares.login.dal.DO.AccountDO;
import com.ares.login.discovery.OnDiscoveryWatchService;
import com.ares.transport.bean.ServerNodeInfo;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.options.PutOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {
    @Lazy
    @Autowired
    private DiscoveryService discoveryService;
    @Autowired
    private OnDiscoveryWatchService onDiscoveryWatch;
    @Autowired
    private AccountDAO accountDAO;

    @Override
    public SdkLoginResponse sdkLogin(SdkLoginRequest sdkLoginRequest) {

        return null;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        AccountDO accountDO = accountDAO.getSingle(loginRequest.getAccountId());
//        if (accountDO == null) {
//            long uid = SnowFlake.nextId();
//            accountDO = new AccountDO();
//            accountDO.setRoleId(roleId);
//            accountDO.setId(loginRequest.getAccountId());
//            accountDO.setAreaId(loginRequest.getAreaId());
//            accountDAO.upInsert(accountDO);
//        }
//        LoginResponse LoginResponse = new LoginResponse();
//        LoginResponse.setAreaId(loginRequest.getAreaId());
//        LoginResponse.setRoleId(accountDO.getRoleId());
//        LoginResponse.setSecret(IdUtils.generate());
//        LoginResponse.setAccountId(loginRequest.getAccountId());
//        savePlayerSecret(accountDO.getRoleId(), LoginResponse.getSecret());
//
//        ServerNodeInfo lowerLoadServer = onDiscoveryWatch.getLowerLoadGameServer(ServerType.GATEWAY);
//        if (lowerLoadServer == null) {
//            throw new AresBaseException(-1, "XXXXXXXXX login request = " + loginRequest + " not found gateway");
//        }
//        LoginResponse.setServerAddr(lowerLoadServer.getIp() +":" + lowerLoadServer.getPort());
//        return LoginResponse;
        return  null;
    }

    //save player secret for the game server check player secret
    private void savePlayerSecret(long uid, String secret) {
        //this should used a timer out set
        discoveryService.getEtcdClient().getLeaseClient().grant(30).thenAccept(result -> {
            long leaseId = result.getID();
            KV kvClient = discoveryService.getEtcdClient().getKVClient();
            PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
            kvClient.put(ByteSequence.from((uid + "").getBytes()), ByteSequence.from(secret.getBytes()), putOption);
        });
    }
}
