package br.com.fpnbr.orderservice.service;

import br.com.fpnbr.orderservice.domain.document.Event;
import br.com.fpnbr.orderservice.repository.EventRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class EventService {

    private final EventRepository eventRepository;

    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }
}
