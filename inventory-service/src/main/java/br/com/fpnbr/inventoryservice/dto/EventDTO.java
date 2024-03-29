package br.com.fpnbr.inventoryservice.dto;

import br.com.fpnbr.inventoryservice.enums.SagaStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {

    private String id;
    private String transactionId;
    private String orderId;
    private OrderDTO payload;
    private String source;
    private SagaStatusEnum status;
    private List<HistoryDTO> eventHistory;
    private LocalDateTime createdAt;

    public void addToHistory(HistoryDTO history) {
        if (isEmpty(eventHistory)) {
            eventHistory = new ArrayList<>();
        }
        eventHistory.add(history);
    }
}
