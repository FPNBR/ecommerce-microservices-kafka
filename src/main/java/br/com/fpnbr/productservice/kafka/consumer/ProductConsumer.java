package br.com.fpnbr.productservice.kafka.consumer;

import br.com.fpnbr.productservice.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class ProductConsumer {

    private final JsonUtil jsonUtil;

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id", topics = "${spring.kafka.topic.product-success}")
    public void consumeProductSuccessEvent(String payload) {
        log.info("Consuming success event: {} from product-success topic", payload);
        var event = jsonUtil.toEventDTO(payload);
        log.info("Event: {}", event);
    }

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id", topics = "${spring.kafka.topic.product-failure}")
    public void consumeProductFailureEvent(String payload) {
        log.info("Consuming rollback event: {} from product-failure topic", payload);
        var event = jsonUtil.toEventDTO(payload);
        log.info("Event: {}", event);
    }
}
