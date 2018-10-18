package ru.novikov.random.server.service;

import com.hazelcast.core.IQueue;
import ru.novikov.random.generator.models.model.DistribObject;

/**
 * @author anovikov
 * @date 17.07.18
 */
public interface QueueListener {
    void startListening();

    void stopListening();

    IQueue<DistribObject> getQueue();
}
