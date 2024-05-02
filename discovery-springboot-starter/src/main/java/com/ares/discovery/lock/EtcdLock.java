package com.ares.discovery.lock;

import com.ares.discovery.utils.SequenceUtils;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Lock;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lock.LockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class EtcdLock {
    private static final Logger log = LoggerFactory.getLogger(EtcdLock.class);
    private ByteSequence lockKey;
    private long leaseId;
    private Lock lockClient;
    private Lease leaseClient;
    private ByteSequence gottenKey;

    public EtcdLock(Client etcdClient, String lockKey) {
        this.lockClient = etcdClient.getLockClient();
        this.leaseClient = etcdClient.getLeaseClient();
        this.lockKey = SequenceUtils.bytesOf(lockKey);

    }

    public boolean acquireLock(int seconds) {
        try {
            log.info("acquired lock: {} begin ", SequenceUtils.toString(lockKey));
            LeaseGrantResponse leaseGrantResponse = leaseClient.grant(seconds).get();
            leaseId = leaseGrantResponse.getID();
            // 尝试获取锁
            CompletableFuture<LockResponse> lockResponseFuture = lockClient.lock(lockKey, leaseId);
            // 设置超时时间
            LockResponse lockResponse = lockResponseFuture.get(seconds - 1, TimeUnit.SECONDS);
            gottenKey = lockResponse.getKey();

            log.info("acquired lock: {} id={}  lock_success  leaseId={} ", SequenceUtils.toString(lockKey), SequenceUtils.toString(gottenKey), leaseId);
            return true;

        } catch (Exception e) {
            log.error(" acquired lock:{} leaseId={}  failed: ", SequenceUtils.toString(lockKey), leaseId, e);
            //  releaseLock();
        }
        return false;
    }

    public boolean acquireLock(int seconds, int maxTryTimes) {
        for (int i = 0; i < maxTryTimes; ++i) {
            if (acquireLock(seconds)) {
                return true;
            }
        }
        return false;
    }

    public void releaseLock() {
        try {
            leaseClient.revoke(leaseId).get();
            lockClient.unlock(gottenKey).get();
            log.info("release lock: key={} id = {}= leasedId= {}  ", lockKey, SequenceUtils.toString(gottenKey), leaseId);
        } catch (Exception e) {
            log.error("xxxx releaseLock error", e);
        }
    }
}
