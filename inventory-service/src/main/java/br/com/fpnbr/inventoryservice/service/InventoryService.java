package br.com.fpnbr.inventoryservice.service;

import br.com.fpnbr.inventoryservice.domain.Inventory;
import br.com.fpnbr.inventoryservice.domain.OrderInventory;
import br.com.fpnbr.inventoryservice.dto.EventDTO;
import br.com.fpnbr.inventoryservice.dto.HistoryDTO;
import br.com.fpnbr.inventoryservice.dto.OrderDTO;
import br.com.fpnbr.inventoryservice.dto.OrderProductsDTO;
import br.com.fpnbr.inventoryservice.exception.ValidationException;
import br.com.fpnbr.inventoryservice.kafka.producer.KafkaProducer;
import br.com.fpnbr.inventoryservice.repository.InventoryRepository;
import br.com.fpnbr.inventoryservice.repository.OrderInventoryRepository;
import br.com.fpnbr.inventoryservice.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.fpnbr.inventoryservice.enums.SagaStatusEnum.*;

@Slf4j
@AllArgsConstructor
@Service
public class InventoryService {

    private static final String CURRENT_SOURCE = "INVENTORY_SERVICE";

    private final InventoryRepository inventoryRepository;
    private final OrderInventoryRepository orderInventoryRepository;
    private final KafkaProducer kafkaProducer;
    private final JsonUtil jsonUtil;

    public void updateInventory(EventDTO eventDTO) {
        try {
            checkCurrentValidation(eventDTO);
            createOrderInventory(eventDTO);
            updateInventory(eventDTO.getPayload());
            handleSuccess(eventDTO);
        } catch (Exception e) {
            log.error("Error trying to update inventory! ".concat(e.getMessage()));
            handleFailureCurrentNotExecuted(eventDTO, e.getMessage());
        }

        kafkaProducer.sendEvent(jsonUtil.toJson(eventDTO));
    }

    private void createOrderInventory(EventDTO eventDTO) {
        eventDTO
                .getPayload()
                .getProducts()
                .forEach(product -> {
                    var inventory = findInventoryByProductCode(product.getProduct().getCode());
                    var orderInventory = createOrderInventory(eventDTO, product, inventory);
                    orderInventoryRepository.save(orderInventory);
                });
    }

    private OrderInventory createOrderInventory(EventDTO eventDTO, OrderProductsDTO product, Inventory inventory) {
        return OrderInventory.builder()
                .inventory(inventory)
                .oldQuantity(inventory.getAvailable())
                .orderQuantity(product.getQuantity())
                .newQuantity(inventory.getAvailable() - product.getQuantity())
                .orderId(eventDTO.getPayload().getId())
                .transactionId(eventDTO.getPayload().getTransactionId())
                .build();
    }

    public void updateInventory(OrderDTO orderDTO) {
        orderDTO
                .getProducts()
                .forEach(product -> {
                    var inventory = findInventoryByProductCode(product.getProduct().getCode());
                    checkInventory(inventory.getAvailable(), product.getQuantity());
                    inventory.setAvailable(inventory.getAvailable() - product.getQuantity());
                    inventoryRepository.save(inventory);
                });
    }

    private void checkInventory(Long available, Long orderQuantity) {
        if (orderQuantity > available) {
            throw new ValidationException("Product is out of stock!");
        }
    }

    private void handleSuccess(EventDTO eventDTO) {
        eventDTO.setStatus(SUCCESS);
        eventDTO.setSource(CURRENT_SOURCE);
        addHistory(eventDTO, "Inventory updated successfully!");
    }

    private void handleFailureCurrentNotExecuted(EventDTO eventDTO, String message) {
        eventDTO.setStatus(ROLLBACK_PENDING);
        eventDTO.setSource(CURRENT_SOURCE);
        addHistory(eventDTO, "Failed to update inventory: ".concat(message));
    }


    public void rollbackInventory(EventDTO eventDTO) {
        eventDTO.setStatus(FAILURE);
        eventDTO.setSource(CURRENT_SOURCE);
        try {
            returnInventoryToPreviousValues(eventDTO);
            addHistory(eventDTO, "Rollback executed successfully!");
        } catch (Exception e) {
            addHistory(eventDTO, "Rollback not executed successfully! ".concat(e.getMessage()));
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(eventDTO));
    }

    private void returnInventoryToPreviousValues(EventDTO eventDTO) {
        orderInventoryRepository.findByOrderIdAndTransactionId(
                        eventDTO.getPayload().getId(), eventDTO.getPayload().getTransactionId())
                .forEach(orderInventory -> {
                    var inventory = orderInventory.getInventory();
                    inventory.setAvailable(orderInventory.getOldQuantity());
                    inventoryRepository.save(inventory);
                    log.info("Restored inventory for order {} from quantity {} to {}",
                            eventDTO.getPayload().getId(), orderInventory.getNewQuantity(), inventory.getAvailable());
                });
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

    private Inventory findInventoryByProductCode(String productCode) {
        return inventoryRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ValidationException("Inventory not found by informed product!"));
    }

    private void checkCurrentValidation(EventDTO eventDTO) {
        if (orderInventoryRepository.existsByOrderIdAndTransactionId(
                eventDTO.getPayload().getId(), eventDTO.getPayload().getTransactionId())) {
            throw new ValidationException("There is another transactionID for this validation!");
        }
    }
}
