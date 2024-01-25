package br.com.fpnbr.orchestratorservice.saga;

import br.com.fpnbr.orchestratorservice.dto.EventDTO;
import br.com.fpnbr.orchestratorservice.enums.TopicsEnum;
import br.com.fpnbr.orchestratorservice.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static br.com.fpnbr.orchestratorservice.saga.SagaHandler.*;
import static java.lang.String.format;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@AllArgsConstructor
@Component
public class SagaExecutionController {

    private static final String SAGA_LOG_ID = "ORDER ID: %s | TRANSACTION ID: %s | EVENT ID: %s";

    public TopicsEnum getNextTopic(EventDTO eventDTO) {
        if (isEmpty(eventDTO.getSource()) || isEmpty(eventDTO.getStatus())) {
            throw new ValidationException("Source or Status must be informed!");
        }
        var topic = findTopicBySourceAndStatus(eventDTO);
        logCurrentSagaStatus(eventDTO, topic);
        return topic;
    }

    private TopicsEnum findTopicBySourceAndStatus(EventDTO eventDTO) {
        return (TopicsEnum) (Arrays.stream(SAGA_HANDLER)
                .filter(row -> isEventSourceAndStatusValid(eventDTO, row))
                .map(i -> i[TOPIC_INDEX])
                .findFirst()
                .orElseThrow(() -> new ValidationException("Topic not found!")));
    }

    private boolean isEventSourceAndStatusValid(EventDTO eventDTO, Object[] row) {
        var source = row[EVENT_SOURCE_INDEX];
        var status = row[SAGA_STATUS_INDEX];

        return eventDTO.getSource().equals(source) && eventDTO.getStatus().equals(status);
    }

    private void logCurrentSagaStatus(EventDTO eventDTO, TopicsEnum topic) {
        var sagaId = createSagaId(eventDTO);
        var source = eventDTO.getSource();

        switch (eventDTO.getStatus()) {
            case SUCCESS -> log.info("### CURRENT SAGA {} | SUCCESS | NEXT TOPIC: {} | {}", source, topic, sagaId);
            case ROLLBACK_PENDING -> log.info("### CURRENT SAGA {} | SENDING TO ROLLBACK PREVIOUS SERVICE | NEXT TOPIC: {} | {}", source, topic, sagaId);
            case FAILURE -> log.info("### CURRENT SAGA {} | FAILURE | NEXT TOPIC: {} | {}", source, topic, sagaId);
        }
    }

    private String createSagaId(EventDTO eventDTO) {
        return format(SAGA_LOG_ID, eventDTO.getPayload().getId(), eventDTO.getTransactionId(), eventDTO.getId());
    }
}
