package ru.novikov.random;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.TopicConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
                .setGroupConfig(new GroupConfig("random"))
                .setNetworkConfig(new NetworkConfig()
                        .setPort(5701)
                        .setJoin(new JoinConfig()
                                .setTcpIpConfig(new TcpIpConfig()
                                        .setEnabled(true)
                                        .setMembers(Lists.newArrayList("192.168.1.1"))))));
    }
}
