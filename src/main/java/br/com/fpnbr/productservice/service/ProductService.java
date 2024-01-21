package br.com.fpnbr.productservice.service;

import br.com.fpnbr.productservice.domain.Validation;
import br.com.fpnbr.productservice.dto.EventDTO;
import br.com.fpnbr.productservice.dto.HistoryDTO;
import br.com.fpnbr.productservice.dto.OrderProductsDTO;
import br.com.fpnbr.productservice.exception.ValidationException;
import br.com.fpnbr.productservice.kafka.producer.KafkaProducer;
import br.com.fpnbr.productservice.repository.ProductRepository;
import br.com.fpnbr.productservice.repository.ValidationRepository;
import br.com.fpnbr.productservice.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.fpnbr.productservice.enums.SagaStatusEnum.*;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@AllArgsConstructor
@Service
public class ProductService {

    private static final String CURRENT_SOURCE = "PRODUCT_SERVICE";

    private final ProductRepository productRepository;
    private final ValidationRepository validationRepository;
    private final KafkaProducer kafkaProducer;
    private final JsonUtil jsonUtil;

    public void validateExistingProducts(EventDTO eventDTO) {
        try {
            checkCurrentValidation(eventDTO);
            createValidation(eventDTO, true);
            handleSuccess(eventDTO);
        } catch (Exception e) {
            log.error("Error validating existing products!", e);
            handleFailureCurrentNotExecuted(eventDTO, e.getMessage());
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(eventDTO));
    }

    private void checkCurrentValidation(EventDTO eventDTO) {
        validateProductsInformed(eventDTO);
        verifyExistingValidation(eventDTO);
        eventDTO.getPayload().getProducts().forEach(product -> {
            validateInformedProduct(product);
            validateExistingProduct(product.getProduct().getCode());
        });
    }

    private void validateProductsInformed(EventDTO eventDTO) {
        if (isEmpty(eventDTO.getPayload()) || isEmpty(eventDTO.getPayload().getProducts())) {
            throw new ValidationException("Payload or products is empty!");
        }
        if (isEmpty(eventDTO.getPayload().getId()) || isEmpty(eventDTO.getPayload().getTransactionId())) {
            throw new ValidationException("OrderID and TransactionID must be informed!");
        }
    }

    private void verifyExistingValidation(EventDTO eventDTO) {
        if (validationRepository.existsByOrderIdAndTransactionId(
                eventDTO.getPayload().getId(), eventDTO.getPayload().getTransactionId())) {
            throw new ValidationException("There is already a validation for this order!");
        }
    }

    private void validateInformedProduct(OrderProductsDTO productsDTO) {
        if (isEmpty(productsDTO.getProduct()) || isEmpty(productsDTO.getProduct().getCode())) {
            throw new ValidationException("Product must be informed!");
        }
    }

    private void validateExistingProduct(String productCode) {
        if (!productRepository.existsByCode(productCode)) {
            throw new ValidationException("Product not found!");
        }
    }

    private void createValidation(EventDTO eventDTO, boolean success) {
        var validation = Validation.builder()
                .orderId(eventDTO.getPayload().getId())
                .transactionId(eventDTO.getPayload().getTransactionId())
                .success(success)
                .build();
        validationRepository.save(validation);
    }

    private void handleSuccess(EventDTO eventDTO) {
        eventDTO.setStatus(SUCCESS);
        eventDTO.setSource(CURRENT_SOURCE);
        addHistory(eventDTO, "Products validated successfully!");
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

    private void handleFailureCurrentNotExecuted(EventDTO eventDTO, String message) {
        eventDTO.setStatus(ROLLBACK_PENDING);
        eventDTO.setSource(CURRENT_SOURCE);
        addHistory(eventDTO, "Failed to validate products: ".concat(message));
    }

    public void rollbackEvent(EventDTO eventDTO) {
        changeValidationToFailure(eventDTO);
        eventDTO.setStatus(FAILURE);
        eventDTO.setSource(CURRENT_SOURCE);
        addHistory(eventDTO, "Rollback executed successfully!");
        kafkaProducer.sendEvent(jsonUtil.toJson(eventDTO));
    }

    private void changeValidationToFailure(EventDTO eventDTO) {
        validationRepository.findByOrderIdAndTransactionId(
                eventDTO.getPayload().getId(), eventDTO.getPayload().getTransactionId()).ifPresentOrElse(
                validation -> {
                    validation.setSuccess(false);
                    validationRepository.save(validation);
                },
                () -> createValidation(eventDTO, false));
    }
}
