package com.ares.discovery;

import com.ares.core.utils.JsonUtil;
import com.ares.discovery.utils.SequenceUtils;
import com.ares.discovery.utils.NetUtils;
import com.ares.transport.bean.ServerNodeInfo;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.PutOption;
import io.grpc.stub.CallStreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class EtcdRegister {
    private final Client client;
    private final String appName;
    private final int port;
    private final int groupId;
    private final int serveType;
    private ServerNodeInfo serverNodeInfo;

    public EtcdRegister(Client client, int serverType, String appName, int port, int groupId) {
        this.client = client;
        this.appName = appName;
        this.port = port;
        this.groupId = groupId;
        this.serveType = serverType;

        String addr = NetUtils.getIpAddress().get(0);
        String serviceId = NetUtils.createServiceId(appName, addr, port, groupId);
        serverNodeInfo = new ServerNodeInfo();
        serverNodeInfo.setIp(addr);
        serverNodeInfo.setPort(port);
        serverNodeInfo.setServiceId(serviceId);
        serverNodeInfo.setServiceName(appName);
        serverNodeInfo.setServerType(serveType);
        serverNodeInfo.setGroupId(groupId);
    }

    public void startRegister() {
        log.info("#### start register me:{}", serverNodeInfo);
        putWithLease(serverNodeInfo.getServiceId(), JsonUtil.toJsonString(serverNodeInfo));
    }

    public ServerNodeInfo getMyselfNodeInfo() {
        return serverNodeInfo;
    }

    public void updateServerNodeInfo(ServerNodeInfo serverNodeInfo) {
        putWithLease(serverNodeInfo.getServiceId(), JsonUtil.toJsonString(serverNodeInfo));
    }

    public void updateServerNodeInfo(Map<String, String> metadata) {
        serverNodeInfo.getMetaData().putAll(metadata);
        updateServerNodeInfo(serverNodeInfo);

    }

    private Client getClient() {
        return client;
    }
    private void putWithLease(String key, String value) {
        Lease leaseClient = getClient().getLeaseClient();

        leaseClient.grant(60).thenAccept(result -> {
            // 租约ID
            long leaseId = result.getID();

            // 准备好put操作的client
            KV kvClient = getClient().getKVClient();

            // put操作时的可选项，在这里指定租约ID
            PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();

            // put操作
            kvClient.put(SequenceUtils.bytesOf(key), SequenceUtils.bytesOf(value), putOption)
                    .thenAccept(putResponse -> {
                        // put操作完成后，再设置无限续租的操作
                        leaseClient.keepAlive(leaseId, new CallStreamObserver<LeaseKeepAliveResponse>() {
                            @Override
                            public boolean isReady() {
                                return false;
                            }

                            @Override
                            public void setOnReadyHandler(Runnable onReadyHandler) {

                            }

                            @Override
                            public void disableAutoInboundFlowControl() {

                            }

                            @Override
                            public void request(int count) {
                            }

                            @Override
                            public void setMessageCompression(boolean enable) {

                            }

                            /**
                             * 每次续租操作完成后，该方法都会被调用
                             * @param value
                             */
                            @Override
                            public void onNext(LeaseKeepAliveResponse value) {
                                // System.out.println("续租完成");
                            }

                            @Override
                            public void onError(Throwable t) {
                                log.error("error", t);
                            }

                            @Override
                            public void onCompleted() {
                            }
                        });
                    });
        });
    }
}
