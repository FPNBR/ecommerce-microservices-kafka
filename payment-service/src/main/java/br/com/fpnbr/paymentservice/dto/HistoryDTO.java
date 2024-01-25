package br.com.fpnbr.paymentservice.dto;

import br.com.fpnbr.paymentservice.enums.SagaStatusEnum;
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

    private String source;
    private SagaStatusEnum status;
    private String message;
    private LocalDateTime createdAt;
}
