package br.com.fpnbr.orchestratorservice.service;

import br.com.fpnbr.orchestratorservice.dto.EventDTO;
import br.com.fpnbr.orchestratorservice.dto.HistoryDTO;
import br.com.fpnbr.orchestratorservice.enums.TopicsEnum;
import br.com.fpnbr.orchestratorservice.kafka.producer.SagaOrchestratorProducer;
import br.com.fpnbr.orchestratorservice.saga.SagaExecutionController;
import br.com.fpnbr.orchestratorservice.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.fpnbr.orchestratorservice.enums.EventSourceEnum.ORCHESTRATOR;
import static br.com.fpnbr.orchestratorservice.enums.SagaStatusEnum.FAILURE;
import static br.com.fpnbr.orchestratorservice.enums.SagaStatusEnum.SUCCESS;

@Slf4j
@AllArgsConstructor
@Service
public class OrchestratorService {

    private final SagaOrchestratorProducer sagaOrchestratorProducer;
    private final SagaExecutionController sagaExecutionController;
    private final JsonUtil jsonUtil;

    public void startSataga(EventDTO eventDTO) {
        eventDTO.setSource(ORCHESTRATOR);
        eventDTO.setStatus(SUCCESS);
        var topic = getTopic(eventDTO);
        log.info("SAGA STARTED!");
        addHistory(eventDTO, "Saga started!");
        sendToProducerWithTopic(eventDTO, topic);
    }

    public void continueSaga(EventDTO eventDTO) {
        var topic = getTopic(eventDTO);
        log.info("SAGA CONTINUED FOR EVENT: {}", eventDTO.getId());
        sendToProducerWithTopic(eventDTO, topic);
    }

    public void finishSagaSuccess(EventDTO eventDTO) {
        eventDTO.setSource(ORCHESTRATOR);
        eventDTO.setStatus(SUCCESS);
        log.info("SAGA FINISHED SUCCESSFULLY FOR EVENT: {}", eventDTO.getId());
        addHistory(eventDTO, "Saga finished successfully!");
        notifyFinishedSaga(eventDTO);
    }

    public void finishSagaFailure(EventDTO eventDTO) {
        eventDTO.setSource(ORCHESTRATOR);
        eventDTO.setStatus(FAILURE);
        log.info("SAGA FINISHED WITH ERRORS FOR EVENT: {}", eventDTO.getId());
        addHistory(eventDTO, "Saga finished with errors!");
        notifyFinishedSaga(eventDTO);
    }

    private TopicsEnum getTopic(EventDTO eventDTO) {
        return sagaExecutionController.getNextTopic(eventDTO);
    }

    private void addHistory(EventDTO eventDTO, String message) {
        var history = HistoryDTO.builder()
                .source(eventDTO.getSource())
                .status(eventDTO.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        eventDTO.addToHistory(history);
    }

    private void notifyFinishedSaga(EventDTO eventDTO) {
        sendToProducerWithTopic(eventDTO, TopicsEnum.NOTIFY_ENDING);
    }

    private void sendToProducerWithTopic(EventDTO eventDTO, TopicsEnum topic) {
        sagaOrchestratorProducer.sendEvent(jsonUtil.toJson(eventDTO), topic.getTopic());
    }
}
