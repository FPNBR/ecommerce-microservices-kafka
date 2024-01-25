package br.com.fpnbr.orderservice.controller;

import br.com.fpnbr.orderservice.domain.document.Event;
import br.com.fpnbr.orderservice.dto.EventFiltersDTO;
import br.com.fpnbr.orderservice.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/event")
public class EventController {

    private final EventService eventService;

    @GetMapping
    public Event findByFilters(EventFiltersDTO eventFiltersDTO) {
        return eventService.findByFilters(eventFiltersDTO);
    }

    @GetMapping("/all")
    public List<Event> findAllEvents() {
        return eventService.findAllEvents();
    }
}
