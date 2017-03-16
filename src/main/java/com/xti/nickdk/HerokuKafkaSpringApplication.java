package com.xti.nickdk;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
@ComponentScan(basePackages="com.xti.nickdk")
public class HerokuKafkaSpringApplication implements AcknowledgingMessageListener<String, String>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(HerokuKafkaSpringApplication.class);

    @Value("${kafka.topic:heroku-kafka-test-topic}")
    private String kafkaTopic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

	public static void main(String[] args) {
		SpringApplication.run(HerokuKafkaSpringApplication.class, args);
	}


	@PostConstruct
    public void produceSomeMessages() throws ExecutionException, InterruptedException {
	    for(int i=0; i<10; i++) {
	        LOGGER.info("Sending message {}", i);
            ListenableFuture<SendResult<String, String>> kafkaFuture = kafkaTemplate.send(kafkaTopic, "Heroku kafka test message " + i);
            kafkaTemplate.flush();
            kafkaFuture.get();
        }
    }

	@KafkaListener(topics = "${kafka-topic:heroku-kafka-test-topic}")
	@Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay=1000, maxDelay=30000, multiplier = 2))
	@Override
	public void onMessage(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
		try {
			String eventString = record.value();
			LOGGER.info("Received message: {}", eventString);

			acknowledgment.acknowledge();
		} catch (Throwable e) {
			LOGGER.error("Failed to process. Will NOT ack this message!", e);

			throw e;
		}
	}
}
