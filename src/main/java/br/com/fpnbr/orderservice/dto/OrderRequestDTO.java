package br.com.fpnbr.orderservice.dto;

import br.com.fpnbr.orderservice.domain.document.OrderProducts;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderRequestDTO {

    private List<OrderProducts> products;
}
