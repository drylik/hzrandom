package ru.novikov.random.server.service.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
import ru.novikov.random.generator.models.model.DistribObject;
import ru.novikov.random.generator.service.GenerationService;
import ru.novikov.random.server.service.QueueListener;
import ru.novikov.random.server.service.RestService;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RestServiceImpl implements RestService {

    private Map<Long, DeferredResult> resultMap;
    private Map<Long, List<Object>> generated = new ConcurrentHashMap<>();

    private final QueueListener queueListener;
    private final HazelcastInstance hzInstance;
    private final GenerationService generationService;

    private ITopic<DistribObject> topic;

    @Autowired
    public RestServiceImpl(@Lazy HazelcastInstance hzInstance, @Lazy QueueListener queueListener, GenerationService generationService) {
        this.hzInstance = hzInstance;
        this.queueListener = queueListener;
        this.generationService = generationService;
    }

    @PostConstruct
    private void init() {
        topic = hzInstance.getTopic("generate");
        resultMap = ExpiringMap.builder()
                .asyncExpirationListener((key, value) -> {
                    var seeds = generated.remove(key);
                    var seedLng = 0l;
                    for (Object seed : seeds) {
                        seedLng += bytesToLong(((String) seed).getBytes());
                    }
                    var generated = generationService.generateString(seedLng);
                    ((DeferredResult) value).setResult(generated);
                })
                .expiration(4, TimeUnit.SECONDS)
                .build();
    }

    @Async
    @Override
    public <T> void generateRequest(Class<T> clazz, String clientSeed, DeferredResult<T> result) {
        log.info("id of current thread: {}", Thread.currentThread().getId());
        if (String.class.equals(clazz)) {
            var timestamp = System.currentTimeMillis();
            generated.putIfAbsent(timestamp, new ArrayList<>());
            topic.publish(new DistribObject(timestamp, clientSeed, queueListener.getQueue().getName()));
            resultMap.put(timestamp, result);
        } else {
            result.setErrorResult("not implemented yet");
        }
    }

    private long bytesToLong(byte[] bytes) {
        var buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    @Override
    public void addToGenerated(long key, Object value) {
        generated.computeIfPresent(key, (k, v) -> {
            v.add(value);
            return v;
        });
    }
}
