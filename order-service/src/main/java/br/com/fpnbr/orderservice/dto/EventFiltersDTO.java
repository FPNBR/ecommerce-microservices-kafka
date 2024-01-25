package br.com.fpnbr.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventFiltersDTO {

    private String orderId;
    private String transactionId;
}
