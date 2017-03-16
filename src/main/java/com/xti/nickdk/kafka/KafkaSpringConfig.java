package com.xti.nickdk.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;

@Configuration
@PropertySource("classpath:kafkaconfig.properties")
@Lazy
public class KafkaSpringConfig {

    @Value("${kafka.trustedCert}")
    private String trustedCert;

    @Value("${kafka.clientCert}")
    private String clientCert;

    @Value("${kafka.clientKey}")
    private String clientKey;

    @Value("${kafka.url}")
    private String kafkaUrl;

    @Value("${kafka.group}")
    private String kafkaGroup;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckOnError(false);
        factory.getContainerProperties().setAckMode(AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(new KafkaConfig(kafkaUrl, trustedCert, clientCert, clientKey, kafkaGroup).buildConsumerDefaults());
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(new KafkaConfig(kafkaUrl, trustedCert, clientCert, clientKey, kafkaGroup).buildProducerDefaults());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(producerFactory());

        return kafkaTemplate;
    }

}
