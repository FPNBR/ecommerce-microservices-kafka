package br.com.fpnbr.paymentservice.service;

import br.com.fpnbr.paymentservice.domain.Payment;
import br.com.fpnbr.paymentservice.dto.EventDTO;
import br.com.fpnbr.paymentservice.dto.HistoryDTO;
import br.com.fpnbr.paymentservice.dto.OrderProductsDTO;
import br.com.fpnbr.paymentservice.enums.PaymentStatusEnum;
import br.com.fpnbr.paymentservice.enums.SagaStatusEnum;
import br.com.fpnbr.paymentservice.exception.ValidationException;
import br.com.fpnbr.paymentservice.kafka.producer.KafkaProducer;
import br.com.fpnbr.paymentservice.repository.PaymentRepository;
import br.com.fpnbr.paymentservice.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static br.com.fpnbr.paymentservice.enums.SagaStatusEnum.FAILURE;
import static br.com.fpnbr.paymentservice.enums.SagaStatusEnum.ROLLBACK_PENDING;

@Slf4j
@AllArgsConstructor
@Service
public class PaymentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
    private static final Long INITIAL_QUANTITY = 0L;
    private static final BigDecimal MIN_AMOUNT_VALUE = BigDecimal.valueOf(0.1);

    private final PaymentRepository paymentRepository;
    private final KafkaProducer kafkaProducer;
    private final JsonUtil jsonUtil;

    public void realizePayment(EventDTO eventDTO) {
        try {
            checkCurrentValidation(eventDTO);
            createPendingPayment(eventDTO);
            var payment = findByOrderIdAndTransactionId(eventDTO);
            validateAmount(payment.getTotalAmount());
            changePaymentToSuccess(payment);
            handleSuccess(eventDTO);
        } catch (Exception e) {
            log.error("Error trying to make payment!", e);
            handleFailureCurrentNotExecuted(eventDTO, e.getMessage());
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(eventDTO));
    }

    private void checkCurrentValidation(EventDTO eventDTO) {
        if (paymentRepository.existsByOrderIdAndTransactionId(
                eventDTO.getPayload().getId(), eventDTO.getPayload().getTransactionId())) {
            throw new ValidationException("There is already a validation for this order!");
        }
    }

    private void createPendingPayment(EventDTO eventDTO) {
        var totalAmount = calculateTotalAmount(eventDTO);
        var totalItems = calculateTotalItems(eventDTO);
        var payment = Payment.builder()
                .orderId(eventDTO.getPayload().getId())
                .transactionId(eventDTO.getPayload().getTransactionId())
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();

        paymentRepository.save(payment);
        setEventAmountItems(eventDTO, payment);
    }

    private BigDecimal calculateTotalAmount(EventDTO eventDTO) {
        return eventDTO.getPayload().getProducts().stream()
                .map(product -> product.getProduct().getUnitValue().multiply(BigDecimal.valueOf(product.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long calculateTotalItems(EventDTO eventDTO) {
        return eventDTO.getPayload().getProducts().stream()
                .map(OrderProductsDTO::getQuantity)
                .reduce(INITIAL_QUANTITY, Long::sum);
    }

    private void setEventAmountItems(EventDTO eventDTO, Payment payment) {
        eventDTO.getPayload().setTotalAmount(payment.getTotalAmount());
        eventDTO.getPayload().setTotalItems(payment.getTotalItems());
    }

    private void validateAmount(BigDecimal totalAmount) {
        if (totalAmount.compareTo(MIN_AMOUNT_VALUE) < 0) {
            throw new ValidationException("Total amount must be greater than ".concat(MIN_AMOUNT_VALUE.toString()));
        }
    }

    public void changePaymentToSuccess(Payment payment) {
        payment.setStatus(PaymentStatusEnum.SUCCESS);
        paymentRepository.save(payment);
    }

    private void handleSuccess(EventDTO eventDTO) {
        eventDTO.setStatus(SagaStatusEnum.SUCCESS);
        eventDTO.setSource(CURRENT_SOURCE);
        addHistory(eventDTO, "Payment realized successfully!");
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
        addHistory(eventDTO, "Failed to realize payment: ".concat(message));
    }

    public void realizeRefund(EventDTO eventDTO) {
        eventDTO.setStatus(FAILURE);
        eventDTO.setSource(CURRENT_SOURCE);
        try {
            changePaymentStatusToRefund(eventDTO);
            addHistory(eventDTO, "Rollback executed successfully!");
        } catch (Exception e) {
            addHistory(eventDTO, "Rollback not executed successfully! ".concat(e.getMessage()));
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(eventDTO));
    }

    private void changePaymentStatusToRefund(EventDTO eventDTO) {
        var payment = findByOrderIdAndTransactionId(eventDTO);
        payment.setStatus(PaymentStatusEnum.REFUND);
        setEventAmountItems(eventDTO, payment);
        paymentRepository.save(payment);
    }

    private Payment findByOrderIdAndTransactionId(EventDTO eventDTO) {
        return paymentRepository.findByOrderIdAndTransactionId(
                        eventDTO.getPayload().getId(), eventDTO.getPayload().getTransactionId())
                .orElseThrow(() -> new ValidationException("Payment not found by OrderID and TransactionID!"));
    }
}
