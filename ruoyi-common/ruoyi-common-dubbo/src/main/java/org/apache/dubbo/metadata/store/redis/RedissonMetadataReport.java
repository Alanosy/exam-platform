package org.apache.dubbo.metadata.store.redis;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.config.configcenter.ConfigItem;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.apache.dubbo.metadata.MappingChangedEvent;
import org.apache.dubbo.metadata.MappingListener;
import org.apache.dubbo.metadata.MetadataInfo;
import org.apache.dubbo.metadata.report.identifier.*;
import org.apache.dubbo.metadata.report.support.AbstractMetadataReport;
import org.apache.dubbo.rpc.RpcException;
import org.dromara.common.core.utils.SpringUtils;
import org.redisson.api.RScript;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.dubbo.common.constants.CommonConstants.*;
import static org.apache.dubbo.metadata.MetadataConstants.META_DATA_STORE_TAG;
import static org.apache.dubbo.metadata.ServiceNameMapping.DEFAULT_MAPPING_GROUP;
import static org.apache.dubbo.metadata.ServiceNameMapping.getAppNames;
import static org.apache.dubbo.metadata.report.support.Constants.DEFAULT_METADATA_REPORT_CYCLE_REPORT;

/**
 * 使用 Redisson 重新实现元数据中心
 */
@Slf4j
public class RedissonMetadataReport extends AbstractMetadataReport {

    // Lua script for atomic CAS on a hash field:
    // If the current value equals ticket (or field is absent, or ticket is empty), update and return 1; else return 0.
    private static final String CAS_LUA ="""
            local old = redis.call('HGET', KEYS[1], ARGV[1])
            if old == false or ARGV[3] == '' or old == ARGV[3] then
                redis.call('HSET', KEYS[1], ARGV[1], ARGV[2])
                return 1
            end
            return 0""";

    private final String root;
    private final long ttlMs;

    // Lazily initialized — Dubbo SPI creates this class before Spring is fully ready
    private volatile RedissonClient redissonClient;

    // topic key → RTopic (keeps the subscription alive)
    private final ConcurrentHashMap<String, RTopic> topicMap = new ConcurrentHashMap<>();
    // serviceKey → listeners (for dispatching mapping change events)
    private final ConcurrentHashMap<String, Set<MappingListener>> listenerMap = new ConcurrentHashMap<>();

    public RedissonMetadataReport(URL url) {
        super(url);
        this.root = url.getGroup(DEFAULT_ROOT);
        this.ttlMs = url.getParameter(CYCLE_REPORT_KEY, DEFAULT_METADATA_REPORT_CYCLE_REPORT)
            ? ONE_DAY_IN_MILLISECONDS * 2L
            : 0L;
    }

    // -------------------------------------------------------------------------
    // Lazy RedissonClient accessor
    // -------------------------------------------------------------------------

    private RedissonClient getRedisson() {
        if (redissonClient == null) {
            synchronized (this) {
                if (redissonClient == null) {
                    redissonClient = SpringUtils.getBean(RedissonClient.class);
                }
            }
        }
        return redissonClient;
    }

    // -------------------------------------------------------------------------
    // AbstractMetadataReport — provider / consumer metadata
    // -------------------------------------------------------------------------

    @Override
    protected void doStoreProviderMetadata(MetadataIdentifier id, String serviceDefinitions) {
        storeMetadata(id.getUniqueKey(KeyTypeEnum.UNIQUE_KEY), serviceDefinitions, true);
    }

    @Override
    protected void doStoreConsumerMetadata(MetadataIdentifier id, String value) {
        storeMetadata(id.getUniqueKey(KeyTypeEnum.UNIQUE_KEY), value, true);
    }

    @Override
    protected void doSaveMetadata(ServiceMetadataIdentifier id, URL url) {
        storeMetadata(id.getIdentifierKey() + META_DATA_STORE_TAG, URL.encode(url.toFullString()), false);
    }

    @Override
    protected void doRemoveMetadata(ServiceMetadataIdentifier id) {
        deleteMetadata(id.getIdentifierKey() + META_DATA_STORE_TAG);
    }

    @Override
    protected List<String> doGetExportedURLs(ServiceMetadataIdentifier id) {
        String content = getMetadata(id.getIdentifierKey() + META_DATA_STORE_TAG);
        if (content == null || content.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(URL.decode(content));
    }

    @Override
    protected void doSaveSubscriberData(SubscriberMetadataIdentifier id, String urlListStr) {
        storeMetadata(id.getUniqueKey(KeyTypeEnum.UNIQUE_KEY), urlListStr, false);
    }

    @Override
    protected String doGetSubscribedURLs(SubscriberMetadataIdentifier id) {
        return getMetadata(id.getUniqueKey(KeyTypeEnum.UNIQUE_KEY));
    }

    @Override
    public String getServiceDefinition(MetadataIdentifier id) {
        return getMetadata(id.getUniqueKey(KeyTypeEnum.UNIQUE_KEY));
    }

    // -------------------------------------------------------------------------
    // App-level metadata (Dubbo 3.x application-level service discovery)
    // -------------------------------------------------------------------------

    @Override
    public void publishAppMetadata(SubscriberMetadataIdentifier id, MetadataInfo metadataInfo) {
        storeMetadata(id.getUniqueKey(KeyTypeEnum.UNIQUE_KEY), metadataInfo.getContent(), false);
    }

    @Override
    public MetadataInfo getAppMetadata(SubscriberMetadataIdentifier id, Map<String, String> instanceMetadata) {
        String content = getMetadata(id.getUniqueKey(KeyTypeEnum.UNIQUE_KEY));
        return org.apache.dubbo.common.utils.JsonUtils.toJavaObject(content, MetadataInfo.class);
    }

    @Override
    public void unPublishAppMetadata(SubscriberMetadataIdentifier id, MetadataInfo metadataInfo) {
        deleteMetadata(id.getUniqueKey(KeyTypeEnum.UNIQUE_KEY));
    }

    // -------------------------------------------------------------------------
    // Service-to-application mapping
    // -------------------------------------------------------------------------

    @Override
    public boolean registerServiceAppMapping(
        String serviceInterface, String defaultMappingGroup, String newConfigContent, Object ticket) {
        try {
            if (ticket != null && !(ticket instanceof String)) {
                throw new IllegalArgumentException("Redis CAS requires a String ticket");
            }
            return storeMappingWithCas(
                buildMappingKey(defaultMappingGroup),
                serviceInterface,
                newConfigContent,
                (String) ticket);
        } catch (Exception e) {
            log.warn("registerServiceAppMapping failed.", e);
            return false;
        }
    }

    @Override
    public ConfigItem getConfigItem(String serviceKey, String group) {
        String key = buildMappingKey(group);
        String content = getMappingField(key, serviceKey);
        return new ConfigItem(content, content);
    }

    @Override
    public Set<String> getServiceAppMapping(String serviceKey, MappingListener listener, URL url) {
        String pubSubKey = buildPubSubKey();
        // Register the RTopic listener once per pubSubKey
        topicMap.computeIfAbsent(pubSubKey, k -> {
            RTopic topic = getRedisson().getTopic(k, StringCodec.INSTANCE);
            topic.addListener(String.class, (channel, msg) -> {
                String applicationNames = getMappingField(buildMappingKey(DEFAULT_MAPPING_GROUP), msg);
                MappingChangedEvent event = new MappingChangedEvent(msg, getAppNames(applicationNames));
                Set<MappingListener> ls = listenerMap.get(msg);
                if (!CollectionUtils.isEmpty(ls)) {
                    ls.forEach(l -> l.onEvent(event));
                }
            });
            return topic;
        });
        listenerMap.computeIfAbsent(serviceKey, k -> new ConcurrentHashSet<>()).add(listener);
        return getServiceAppMapping(serviceKey, url);
    }

    @Override
    public Set<String> getServiceAppMapping(String serviceKey, URL url) {
        return getAppNames(getMappingField(buildMappingKey(DEFAULT_MAPPING_GROUP), serviceKey));
    }

    @Override
    public void removeServiceAppMappingListener(String serviceKey, MappingListener listener) {
        Set<MappingListener> ls = listenerMap.get(serviceKey);
        if (ls != null) {
            ls.remove(listener);
            if (ls.isEmpty()) {
                listenerMap.remove(serviceKey);
                // If no listeners remain for any key, remove the topic subscription
                if (listenerMap.isEmpty()) {
                    RTopic topic = topicMap.remove(buildPubSubKey());
                    if (topic != null) {
                        topic.removeAllListeners();
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Internal Redis helpers
    // -------------------------------------------------------------------------

    private void storeMetadata(String key, String value, boolean ephemeral) {
        try {
            if (ephemeral && ttlMs > 0) {
                getRedisson().<String>getBucket(key, StringCodec.INSTANCE).set(value, Duration.ofMillis(ttlMs));
            } else {
                getRedisson().<String>getBucket(key, StringCodec.INSTANCE).set(value);
            }
        } catch (Exception e) {
            String msg = "Failed to store metadata key=" + key + ", cause: " + e.getMessage();
            log.error(msg, e);
            throw new RpcException(msg, e);
        }
    }

    private String getMetadata(String key) {
        try {
            return getRedisson().<String>getBucket(key, StringCodec.INSTANCE).get();
        } catch (Exception e) {
            String msg = "Failed to get metadata key=" + key + ", cause: " + e.getMessage();
            log.error(msg, e);
            throw new RpcException(msg, e);
        }
    }

    private void deleteMetadata(String key) {
        try {
            getRedisson().getBucket(key, StringCodec.INSTANCE).delete();
        } catch (Exception e) {
            String msg = "Failed to delete metadata key=" + key + ", cause: " + e.getMessage();
            log.error(msg, e);
            throw new RpcException(msg, e);
        }
    }

    private String getMappingField(String key, String field) {
        try {
            return getRedisson().<String, String>getMap(key, StringCodec.INSTANCE).get(field);
        } catch (Exception e) {
            String msg = "Failed to get mapping key=" + key + " field=" + field + ", cause: " + e.getMessage();
            log.error(msg, e);
            throw new RpcException(msg, e);
        }
    }

    /**
     * Atomic CAS on a hash field via Lua script.
     * Updates field to newValue only when the current value equals ticket (or field is absent / ticket is null).
     * On success, publishes a change notification to the pub/sub channel.
     */
    private boolean storeMappingWithCas(String key, String field, String newValue, String ticket) {
        try {
            Long result = getRedisson().getScript(StringCodec.INSTANCE).eval(
                RScript.Mode.READ_WRITE,
                CAS_LUA,
                RScript.ReturnType.INTEGER,
                Collections.singletonList(key),
                field, newValue, ticket == null ? "" : ticket
            );
            if (Long.valueOf(1L).equals(result)) {
                getRedisson().getTopic(buildPubSubKey(), StringCodec.INSTANCE).publish(field);
                return true;
            }
            return false;
        } catch (Exception e) {
            String msg = "Failed to store mapping key=" + key + " field=" + field + ", cause: " + e.getMessage();
            log.error(msg, e);
            throw new RpcException(msg, e);
        }
    }

    private String buildMappingKey(String mappingGroup) {
        return this.root + GROUP_CHAR_SEPARATOR + mappingGroup;
    }

    private String buildPubSubKey() {
        return buildMappingKey(DEFAULT_MAPPING_GROUP) + GROUP_CHAR_SEPARATOR + QUEUES_KEY;
    }
}
