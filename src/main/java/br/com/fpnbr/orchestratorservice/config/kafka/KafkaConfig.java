package br.com.fpnbr.orchestratorservice.config.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

import static br.com.fpnbr.orchestratorservice.enums.TopicsEnum.*;

@RequiredArgsConstructor
@EnableKafka
@Configuration
public class KafkaConfig {

    public static final Integer PARTITION_COUNT = 1;
    public static final Integer REPLICATION_COUNT = 1;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootStrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupdId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerProps());
    }

    private Map<String, Object> consumerProps() {
        var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupdId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerProps());
    }

    private Map<String, Object> producerProps() {
        var props = new HashMap<String, Object>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    private NewTopic buildTopic(String name) {
        return TopicBuilder
                .name(name)
                .partitions(PARTITION_COUNT)
                .replicas(REPLICATION_COUNT)
                .build();
    }

    @Bean
    public NewTopic startSagaTopic() {
        return buildTopic(START_SAGA.getTopic());
    }

    @Bean
    public NewTopic orchestratorTopic() {
        return buildTopic(BASE_ORCHESTRATOR.getTopic());
    }

    @Bean
    public NewTopic finishSuccessTopic() {
        return buildTopic(FINISH_SUCCESS.getTopic());
    }

    @Bean
    public NewTopic finishFailureTopic() {
        return buildTopic(FINISH_FAILURE.getTopic());
    }

    @Bean
    public NewTopic notifyEnding() {
        return buildTopic(NOTIFY_ENDING.getTopic());
    }

    @Bean
    public NewTopic productSuccessTopic() {
        return buildTopic(PRODUCT_SUCCESS.getTopic());
    }

    @Bean
    public NewTopic productFailureTopic() {
        return buildTopic(PRODUCT_FAILURE.getTopic());
    }

    @Bean
    public NewTopic paymentSuccessTopic() {
        return buildTopic(PAYMENT_SUCCESS.getTopic());
    }

    @Bean
    public NewTopic paymentFailureTopic() {
        return buildTopic(PAYMENT_FAILURE.getTopic());
    }

    @Bean
    public NewTopic inventorySuccessTopic() {
        return buildTopic(INVENTORY_SUCCESS.getTopic());
    }

    @Bean
    public NewTopic inventoryFailureTopic() {
        return buildTopic(INVENTORY_FAILURE.getTopic());
    }
}
