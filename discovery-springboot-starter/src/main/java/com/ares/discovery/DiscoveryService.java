package com.ares.discovery;

import io.etcd.jetcd.Client;


public interface DiscoveryService {
    Client getEtcdClient();

    EtcdDiscovery getEtcdDiscovery();

    EtcdRegister getEtcdRegister();

    int genNextSeqNum(String strKey, String lock) throws Exception;

    int getMyWorkId() throws Exception;

}
