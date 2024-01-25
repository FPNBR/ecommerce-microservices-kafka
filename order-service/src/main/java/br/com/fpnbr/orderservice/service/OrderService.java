package br.com.fpnbr.orderservice.service;

import br.com.fpnbr.orderservice.domain.document.Event;
import br.com.fpnbr.orderservice.domain.document.Order;
import br.com.fpnbr.orderservice.dto.OrderRequestDTO;
import br.com.fpnbr.orderservice.kafka.producer.SagaProducer;
import br.com.fpnbr.orderservice.repository.OrderRepository;
import br.com.fpnbr.orderservice.util.JsonUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Service
public class OrderService {

    private static final String TRANSACTION_ID_PATTERN = "%s_%s";
    private final EventService eventService;
    private final SagaProducer sagaProducer;
    private final JsonUtil jsonUtil;
    private final OrderRepository orderRepository;

    public Order createOrder(OrderRequestDTO orderRequestDTO) {
        var order = Order
                .builder()
                .products(orderRequestDTO.getProducts())
                .createdAt(LocalDateTime.now())
                .transactionId(String.format(TRANSACTION_ID_PATTERN, Instant.now().toEpochMilli(), UUID.randomUUID()))
                .build();

        orderRepository.save(order);
        sagaProducer.sendEvent(jsonUtil.toJson(createPayload(order)));

        return order;
    }

    public Event createPayload(Order order) {
        var event = Event
                .builder()
                .orderId(order.getId())
                .transactionId(order.getTransactionId())
                .payload(order)
                .createdAt(LocalDateTime.now())
                .build();

        eventService.saveEvent(event);

        return event;
    }
}
