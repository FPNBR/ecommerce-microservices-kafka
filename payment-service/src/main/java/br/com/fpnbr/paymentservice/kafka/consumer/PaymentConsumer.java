package br.com.fpnbr.paymentservice.kafka.consumer;

import br.com.fpnbr.paymentservice.service.PaymentService;
import br.com.fpnbr.paymentservice.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class PaymentConsumer {

    private final PaymentService paymentService;
    private final JsonUtil jsonUtil;

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id", topics = "${spring.kafka.topic.payment-success}")
    public void consumePaymentSuccessEvent(String payload) {
        log.info("Consuming success event: {} from payment-success topic", payload);
        var event = jsonUtil.toEventDTO(payload);
        paymentService.realizePayment(event);
    }

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id", topics = "${spring.kafka.topic.payment-failure}")
    public void consumePaymentFailureEvent(String payload) {
        log.info("Consuming rollback event: {} from payment-failure topic", payload);
        var event = jsonUtil.toEventDTO(payload);
        paymentService.realizeRefund(event);
    }
}
