package ru.novikov.random.generator.service.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.novikov.random.generator.models.model.DistribObject;
import ru.novikov.random.generator.service.GenerationService;

@Slf4j
@Service
public class GenerationServiceImpl implements GenerationService, MessageListener<DistribObject> {

    @Autowired
    public GenerationServiceImpl(HazelcastInstance hzInstance) {
        ITopic<DistribObject> topic = hzInstance.getTopic("generator");
        topic.addMessageListener(this);
    }

    @Override
    public String generateString() {
        return "random string";
    }

    @Override
    public void onMessage(Message<DistribObject> message) {

    }
}
