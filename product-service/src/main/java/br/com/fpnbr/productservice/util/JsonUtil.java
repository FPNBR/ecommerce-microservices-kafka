package br.com.fpnbr.productservice.util;

import br.com.fpnbr.productservice.dto.EventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Erro ao converter para JSON: {}", e.getMessage());
            return "";
        }
    }

    public EventDTO toEventDTO(String json) {
        try {
            return objectMapper.readValue(json, EventDTO.class);
        } catch (Exception e) {
            log.error("Erro ao converter para Objeto: {}", e.getMessage());
            return null;
        }
    }
}
