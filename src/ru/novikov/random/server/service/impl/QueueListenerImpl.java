package ru.novikov.random.server.service.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.novikov.random.generator.models.model.DistribObject;
import ru.novikov.random.server.service.QueueListener;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author anovikov
 * @date 17.07.18
 */
@Lazy
@Slf4j
@Service
@Scope("prototype")
public class QueueListenerImpl implements QueueListener {

    private final IQueue<DistribObject> queue;
    private final AtomicBoolean listening = new AtomicBoolean(false);
    private final AtomicInteger listeningCount = new AtomicInteger(0);
    private volatile ThreadPoolTaskExecutor executor;

    @Autowired
    public QueueListenerImpl(HazelcastInstance hzInstance) {
        queue = hzInstance.getQueue("mainQueue");
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
                Object object = queue.poll(5L, TimeUnit.SECONDS);
                listeningCount.decrementAndGet();
                if (object != null) {
                    if (executor.getActiveCount() < executor.getMaxPoolSize()) {
                        executor.execute(this::listen);
                    }
                    //todo: do some stuff
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
        boolean needToShutdown = listening.getAndSet(false);
        while (executor.getActiveCount() > 0) ;
        if (needToShutdown) executor.shutdown();
    }
}
