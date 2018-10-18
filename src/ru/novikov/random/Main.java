package ru.novikov.random;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.novikov.random.generator.models.factories.DistribObjectDataSerializableFactory;

import java.util.List;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        return executor;
    }

    @Bean("hzInstance")
    public HazelcastInstance hazelcastInstance() {
        return Hazelcast.newHazelcastInstance(new Config()
                .setSerializationConfig(new SerializationConfig()
                        .addDataSerializableFactoryClass(DistribObjectDataSerializableFactory.ID, DistribObjectDataSerializableFactory.class))
                .setGroupConfig(new GroupConfig("random"))
                .setNetworkConfig(new NetworkConfig()
                        .setPort(5701)
                        .setJoin(new JoinConfig()
                                .setMulticastConfig(new MulticastConfig()
                                        .setEnabled(false))
                                .setTcpIpConfig(new TcpIpConfig()
                                        .setEnabled(true)
                                        .setMembers(List.of("192.168.1.1"))))));
    }
}
