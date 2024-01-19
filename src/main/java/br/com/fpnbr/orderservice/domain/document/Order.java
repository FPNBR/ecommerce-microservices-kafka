package br.com.fpnbr.orderservice.domain.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "order")
public class Order {

    @Id
    private String id;

    private List<OrderProducts> products;

    private LocalDateTime createdAt;

    private String transactionId;

    private BigDecimal totalAmount;

    private Long totalItems;
}
