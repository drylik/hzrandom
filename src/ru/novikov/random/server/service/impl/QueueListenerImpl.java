package ru.novikov.random.server.service.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.novikov.random.generator.models.model.DistribObject;
import ru.novikov.random.server.service.QueueListener;

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
    private final HazelcastInstance hzInstance;

    @Autowired
    public QueueListenerImpl(HazelcastInstance hzInstance) {
        this.hzInstance = hzInstance;
        queue = hzInstance.getQueue("mainQueue");
    }

    @Override
    public void startListening() {
        log.info("starting listening to queue: {}", queue.getName());

    }
}
