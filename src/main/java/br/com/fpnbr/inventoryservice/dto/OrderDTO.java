package br.com.fpnbr.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

    private String id;
    private List<OrderProductsDTO> products;
    private LocalDateTime createdAt;
    private String transactionId;
    private BigDecimal totalAmount;
    private Long totalItems;
}
