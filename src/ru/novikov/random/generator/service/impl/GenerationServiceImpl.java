package ru.novikov.random.generator.service.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.novikov.random.generator.models.model.DistribObject;
import ru.novikov.random.generator.service.GenerationService;
import ru.novikov.random.server.service.QueueListener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.Charset;
import java.util.Random;

@Slf4j
@Service
public class GenerationServiceImpl implements GenerationService, MessageListener<DistribObject> {

    private final QueueListener queueListener;
    private final HazelcastInstance hazelcastInstance;

    @Autowired
    public GenerationServiceImpl(@Lazy HazelcastInstance hzInstance, @Lazy QueueListener queueListener) {
        this.hazelcastInstance = hzInstance;
        this.queueListener = queueListener;
    }

    @PostConstruct
    private void init() {
        ITopic<DistribObject> topic = hazelcastInstance.getTopic("generate");
        topic.addMessageListener(this);
        queueListener.startListening();
    }

    @PreDestroy
    private void destroy() {
        queueListener.stopListening();
    }

    @Override
    public String generateString(long seedLng) {
        var rnd = new Random(seedLng);
        var generatedBytes = new byte[8];
        rnd.nextBytes(generatedBytes);
        return new String(generatedBytes, Charset.forName("UTF-8"));
    }

    @Override
    public void onMessage(Message<DistribObject> message) {
        var distribObject = message.getMessageObject();
        log.info("received: distribObject");
        distribObject.setMySeed(generateString(distribObject.getTimestamp()));
        hazelcastInstance.getQueue(distribObject.getQueueName()).add(distribObject);
    }
}
