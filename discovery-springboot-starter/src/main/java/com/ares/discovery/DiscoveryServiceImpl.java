package com.ares.discovery;

import com.ares.discovery.utils.BytesUtils;
import com.ares.transport.bean.ServerNodeInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.lock.LockResponse;
import io.etcd.jetcd.lock.UnlockResponse;
import io.etcd.jetcd.options.LeaseOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

@Slf4j
public class DiscoveryServiceImpl implements DiscoveryService, ApplicationRunner {
    private Client etcdClient;
    private EtcdRegister etcdRegister;
    private EtcdDiscovery etcdDiscovery;
    private BiFunction<WatchEvent.EventType, ServerNodeInfo, Void> onNodeChangeFun;
    private List<String> watchServicePrefix;
    private static final long LOCK_TIMEOUT_SECONDS = 10;

    public void init(String[] endpoints, int serverType, String appName, int port, int areaId, List<String> watchServicePrefix, BiFunction<WatchEvent.EventType, ServerNodeInfo, Void> onNodeChangeFun) {
        etcdClient = Client.builder().keepaliveTime(null).endpoints(endpoints).build();
        etcdRegister = new EtcdRegister(etcdClient, serverType, appName, port, areaId);
        etcdDiscovery = new EtcdDiscovery(etcdClient, onNodeChangeFun);
        this.watchServicePrefix = watchServicePrefix;
    }

    @Override
    public Client getEtcdClient() {
        return etcdClient;
    }

    @Override
    public EtcdDiscovery getEtcdDiscovery() {
        return etcdDiscovery;
    }

    @Override
    public EtcdRegister getEtcdRegister() {
        return etcdRegister;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        etcdRegister.startRegister();
        etcdDiscovery.watchService(watchServicePrefix);
    }

    @Override
    public int genNextSeqNum(String strKey) {
        try {
            String strLockKey = strKey + "_lock";
            ByteSequence lockKey = BytesUtils.bytesOf(strLockKey);
            ByteSequence bsKey = BytesUtils.bytesOf(strKey);
            // 创建租约
            long leaseId = etcdClient.getLeaseClient().grant(LOCK_TIMEOUT_SECONDS).get().getID();

            // 尝试获取锁
            CompletableFuture<LockResponse> lockResponseFuture = etcdClient.getLockClient().lock(lockKey, leaseId);
            try {
                // 设置超时时间，等待锁
                LockResponse lockResponse = lockResponseFuture.get(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                log.info("Lock acquired: " + lockResponse);
                ByteSequence key1 = lockResponse.getKey();
                GetResponse getResponse = etcdClient.getKVClient().get(bsKey).get();
                List<KeyValue> kvs = getResponse.getKvs();
                if (getResponse.getCount() == 0) {

                }

                for (KeyValue keyValue : kvs) {
                    String string = keyValue.getValue().toString(StandardCharsets.UTF_8);

                }


                // 执行任务（在锁保护的临界区内）
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                // 处理锁超时的情况
                log.error("Failed to acquire lock within timeout: " + e.getMessage());
            } finally {
                // 释放锁
                CompletableFuture<UnlockResponse> unlockResponseFuture = etcdClient.getLockClient().unlock(lockKey);
                unlockResponseFuture.whenComplete((unlockResponse, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to unlock: " + throwable.getMessage());
                    } else {
                        log.info("Lock released: " + unlockResponse);
                    }
                });
                // 关闭租约
                etcdClient.getLeaseClient().revoke(leaseId);
            }
        } catch (Exception e) {
            log.error("====error", e);
        }
        return 0;
    }
}


