package com.ares.discovery;

import com.ares.discovery.lock.EtcdLock;
import com.ares.discovery.utils.SequenceUtils;
import com.ares.transport.bean.ServerNodeInfo;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiFunction;

@Slf4j
public class DiscoveryServiceImpl implements DiscoveryService, ApplicationRunner {
    private Client etcdClient;
    private EtcdRegister etcdRegister;
    private EtcdDiscovery etcdDiscovery;
    private BiFunction<WatchEvent.EventType, ServerNodeInfo, Void> onNodeChangeFun;
    private List<String> watchServicePrefix;
    private static final long LOCK_TIMEOUT_SECONDS = 5;

    public void init(String[] endpoints, int serverType, String appName, int port, int groupId, List<String> watchServicePrefix, BiFunction<WatchEvent.EventType, ServerNodeInfo, Void> onNodeChangeFun) {
        etcdClient = Client.builder().keepaliveTime(null).endpoints(endpoints).build();
        etcdRegister = new EtcdRegister(etcdClient, serverType, appName, port, groupId);
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
    public int genNextSeqNum(String myServiceId, String serviceName) throws Exception {
        String serviceNodeIdKey = "/nodes_id/" + serviceName + "/";
        //first get myself  node id
        int existKeyNum = getExistKeyNum(myServiceId, serviceNodeIdKey);
        if (existKeyNum > 0) {
            return existKeyNum;
        }

        String strLockKey = serviceName + "_lock";
        EtcdLock etcdLock = new EtcdLock(etcdClient, strLockKey);
        boolean ret = etcdLock.acquireLock(3, 10);
        if (!ret) {
            return 0;
        }
        try {
            GetResponse getResponse = etcdClient.getKVClient().get(SequenceUtils.bytesOf(serviceNodeIdKey), GetOption.builder().isPrefix(true).build()).get();
            List<KeyValue> kvs = getResponse.getKvs();
            int indexId = 0;
            if (getResponse.getCount() == 0) {
                indexId++;
                putKeyNumber(myServiceId, indexId, serviceNodeIdKey);
                return indexId;
            }

            for (KeyValue keyValue : kvs) {
                indexId++;
                int workId = SequenceUtils.toInt(keyValue.getValue());
                String serviceId = SequenceUtils.toString(keyValue.getKey());
               // log.info("serviceId = {} index ={}", serviceId, workId);
                if (serviceId.equals(myServiceId)) {
                    return workId;
                }
            }
            indexId = (int) getResponse.getCount() + 1;
            putKeyNumber(myServiceId, indexId, serviceNodeIdKey);
        } catch (Exception e) {
            log.error("XXXXX errro ", e);
        } finally {
            etcdLock.releaseLock();
        }
        return 0;
    }

    private int getExistKeyNum(String serviceId, String nodesKeyPre) throws Exception {
        String serviceNodeIdKey = nodesKeyPre + serviceId;
        GetResponse getResponse = etcdClient.getKVClient().get(ByteSequence.from(serviceNodeIdKey, StandardCharsets.UTF_8)).get();
        if (getResponse.getCount() == 0) {
            return 0;
        }
        List<KeyValue> kvs = getResponse.getKvs();
        return Integer.parseInt(kvs.get(0).getValue().toString(StandardCharsets.UTF_8));
    }

    public void putKeyNumber(String serviceId, int num, String nodesKeyPre) throws Exception {
        String serviceNodeIdKey = nodesKeyPre + serviceId;
        PutResponse putResponse = etcdClient.getKVClient().put(SequenceUtils.bytesOf(serviceNodeIdKey), SequenceUtils.bytesOf(num)).get();
        if (putResponse.hasPrevKv()) {
            ByteSequence value = putResponse.getPrevKv().getValue();
            log.error("service ={} have exist value={} now value ={}", serviceId, SequenceUtils.toString(value), num);
        }
        log.info("+++++++  putkey ={} value ={}", serviceNodeIdKey, num);
    }

    @Override
    public int registerMyWorkId() throws Exception {
        ServerNodeInfo myselfNodeInfo = etcdRegister.getMyselfNodeInfo();
        int workId = genNextSeqNum(myselfNodeInfo.getServiceId(), myselfNodeInfo.getServiceName());
        myselfNodeInfo.setId(workId);
        return workId;
    }
}


