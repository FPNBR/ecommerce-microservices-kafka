package br.com.fpnbr.orderservice.service;

import br.com.fpnbr.orderservice.domain.document.Event;
import br.com.fpnbr.orderservice.dto.EventFiltersDTO;
import br.com.fpnbr.orderservice.exception.ValidationException;
import br.com.fpnbr.orderservice.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@AllArgsConstructor
@Service
public class EventService {

    private final EventRepository eventRepository;

    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public void notifyEnding(Event event) {
        event.setOrderId(event.getOrderId());
        event.setCreatedAt(LocalDateTime.now());
        eventRepository.save(event);
        log.info("Order {} with saga notified! TransactionId: {}", event.getOrderId(), event.getTransactionId());
    }

    public List<Event> findAllEvents() {
        return eventRepository.findAllByOrderByCreatedAtDesc();
    }

    public Event findByFilters(EventFiltersDTO eventFiltersDTO) {
        validateEmptyFilters(eventFiltersDTO);
        if (!isEmpty(eventFiltersDTO.getOrderId())) {
            return findByOrderId(eventFiltersDTO.getOrderId());
        } else {
            return findByTransactionId(eventFiltersDTO.getTransactionId());
        }
    }

    private Event findByOrderId(String orderId) {
        return eventRepository
                .findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ValidationException("Event not found for OrderID: " + orderId));
    }

    private Event findByTransactionId(String transactionId) {
        return eventRepository
                .findFirstByTransactionIdOrderByCreatedAtDesc(transactionId)
                .orElseThrow(() -> new ValidationException("Event not found for TransactionID: " + transactionId));
    }

    private void validateEmptyFilters(EventFiltersDTO eventFiltersDTO) {
        if (isEmpty(eventFiltersDTO.getOrderId()) && isEmpty(eventFiltersDTO.getTransactionId())) {
            throw new ValidationException("OrderID or TransactionID must be informed!");
        }
    }
}
