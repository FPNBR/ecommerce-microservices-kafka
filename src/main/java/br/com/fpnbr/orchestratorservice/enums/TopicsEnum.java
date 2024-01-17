package br.com.fpnbr.orchestratorservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TopicsEnum {

    START_SAGA("start-saga"),
    BASE_ORCHESTRATOR("orchestrator"),
    FINISH_SUCCESS("finish-success"),
    FINISH_FAILURE("finish-failure"),
    PRODUCT_SUCCESS("product-success"),
    PRODUCT_FAILURE("product-failure"),
    PAYMENT_SUCCESS("payment-success"),
    PAYMENT_FAILURE("payment-failure"),
    INVENTORY_SUCCESS("inventory-success"),
    INVENTORY_FAILURE("inventory-failure"),
    NOTIFY_ENDING("notify-ending");

    private String topic;
}
