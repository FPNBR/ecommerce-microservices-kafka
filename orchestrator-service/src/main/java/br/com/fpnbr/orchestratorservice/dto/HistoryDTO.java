package br.com.fpnbr.orchestratorservice.dto;

import br.com.fpnbr.orchestratorservice.enums.EventSourceEnum;
import br.com.fpnbr.orchestratorservice.enums.SagaStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistoryDTO {

    private EventSourceEnum source;
    private SagaStatusEnum status;
    private String message;
    private LocalDateTime createdAt;
}
