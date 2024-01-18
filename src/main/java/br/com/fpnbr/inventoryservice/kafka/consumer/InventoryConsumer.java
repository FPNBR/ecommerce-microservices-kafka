package br.com.fpnbr.inventoryservice.kafka.consumer;

import br.com.fpnbr.inventoryservice.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class InventoryConsumer {

    private final JsonUtil jsonUtil;

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id", topics = "${spring.kafka.topic.inventory-success}")
    public void consumeInventorySuccessEvent(String payload) {
        log.info("Consuming success event: {} from inventory-success topic", payload);
        var event = jsonUtil.toEventDTO(payload);
        log.info("Event: {}", event);
    }

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id", topics = "${spring.kafka.topic.inventory-failure}")
    public void consumeInventoryFailureEvent(String payload) {
        log.info("Consuming rollback event: {} from inventory-failure topic", payload);
        var event = jsonUtil.toEventDTO(payload);
        log.info("Event: {}", event);
    }
}
