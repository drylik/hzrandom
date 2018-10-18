package ru.novikov.random.server.service.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IQueue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.novikov.random.generator.models.model.DistribObject;
import ru.novikov.random.server.service.QueueListener;
import ru.novikov.random.server.service.RestService;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author anovikov
 * @date 17.07.18
 */
@Slf4j
@Service
public class QueueListenerImpl implements QueueListener {

    @Getter
    private final IQueue<DistribObject> queue;
    private final AtomicBoolean listening = new AtomicBoolean(false);
    private final AtomicInteger listeningCount = new AtomicInteger(0);
    private volatile ThreadPoolTaskExecutor executor;
    private final RestService restService;

    @Autowired
    public QueueListenerImpl(HazelcastInstance hzInstance, RestService restService) {
        queue = hzInstance.getQueue("mainQueue" + UUID.randomUUID().toString());
        this.restService = restService;
    }

    /**
     * listening to queue in maximum 30 threads
     */
    @Override
    public void startListening() {
        log.info("starting listening to queue: {}", queue.getName());
        if (executor == null) {
            executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(1);
            executor.setQueueCapacity(0);
            executor.setMaxPoolSize(30);
            executor.setWaitForTasksToCompleteOnShutdown(true);
        }
        executor.initialize();
        listening.set(true);
        executor.execute(this::listen);
    }

    private void listen() {
        while (listening.get()) {
            try {
                listeningCount.incrementAndGet();
                var distribObject = queue.poll(5L, TimeUnit.SECONDS);
                listeningCount.decrementAndGet();
                if (distribObject != null) {
                    if (executor.getActiveCount() < executor.getMaxPoolSize()) {
                        executor.execute(this::listen);
                    }
                    restService.addToGenerated(distribObject.getTimestamp(), distribObject.getMySeed());
                }
                if (listeningCount.get() > 0) {
                    break;
                }
            } catch (HazelcastInstanceNotActiveException e) {
                log.info("hz instance is not active");
                new Thread(this::stopListening).start();
            } catch (InterruptedException e) {
                log.error("Exception during polling the queue {}, ", queue.getName(), e);
            }
        }
    }

    @Override
    public void stopListening() {
        var needToShutdown = listening.getAndSet(false);
        while (executor.getActiveCount() > 0) ;
        if (needToShutdown) executor.shutdown();
    }
}
