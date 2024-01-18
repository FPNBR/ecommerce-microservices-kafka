package br.com.fpnbr.orchestratorservice.kafka.consumer;

import br.com.fpnbr.orchestratorservice.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class SagaOrchestratorConsumer {

    private final JsonUtil jsonUtil;

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id", topics = "${spring.kafka.topic.start-saga}")
    public void consumeStartSagaEvent(String payload) {
        log.info("Consuming event: {} from start-saga topic", payload);
        var event = jsonUtil.toEventDTO(payload);
        log.info("Event: {}", event);
    }

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id", topics = "${spring.kafka.topic.orchestrator}")
    public void consumeOrchestratorSagaEvent(String payload) {
        log.info("Consuming event: {} from orchestrator topic", payload);
        var event = jsonUtil.toEventDTO(payload);
        log.info("Event: {}", event);
    }

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id", topics = "${spring.kafka.topic.finish-success}")
    public void consumeFinishSuccessEvent(String payload) {
        log.info("Consuming event: {} from finish-success topic", payload);
        var event = jsonUtil.toEventDTO(payload);
        log.info("Event: {}", event);
    }

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id", topics = "${spring.kafka.topic.finish-failure}")
    public void consumeFinishFailureEvent(String payload) {
        log.info("Consuming event: {} from finish-failure topic", payload);
        var event = jsonUtil.toEventDTO(payload);
        log.info("Event: {}", event);
    }
}
