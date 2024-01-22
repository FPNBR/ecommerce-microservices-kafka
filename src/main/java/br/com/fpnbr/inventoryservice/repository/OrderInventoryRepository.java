package br.com.fpnbr.inventoryservice.repository;

import br.com.fpnbr.inventoryservice.domain.OrderInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderInventoryRepository extends JpaRepository<OrderInventory, Long> {
    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);
    List<OrderInventory> findByOrderIdAndTransactionId(String orderId, String transactionId);
}
