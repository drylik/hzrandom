package ru.novikov.random.server.service.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
import ru.novikov.random.generator.models.model.DistribObject;
import ru.novikov.random.generator.service.GenerationService;
import ru.novikov.random.server.service.RestService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RestServiceImpl implements RestService {

    private Map<Long, DeferredResult> resultMap;
    private Map<Long, Object> generated = new ConcurrentHashMap<>();

    private final ITopic<DistribObject> topic;

    @Autowired
    public RestServiceImpl(@Lazy @Qualifier("hzInstance") HazelcastInstance hazelcastInstance) {
        topic = hazelcastInstance.getTopic("generate");
        resultMap = ExpiringMap.builder()
                .asyncExpirationListener((key, value) -> {
                    String seed = (String) generated.remove(key);

                    ((DeferredResult)value).setResult(seed + "gen");
                })
                .expiration(4, TimeUnit.SECONDS)
                .build();
    }

    @Async
    @Override
    public <T> void generateRequest(Class<T> clazz, String clientSeed, DeferredResult<T> result) {
        log.info("id of current thread: {}", Thread.currentThread().getId());
        if (String.class.equals(clazz)) {
            long timestamp = System.currentTimeMillis();
            topic.publish(new DistribObject(timestamp, clientSeed));
            resultMap.put(timestamp, result);
        } else {
            result.setErrorResult("not implemented yet");
        }
    }
}
