package br.com.fpnbr.orchestratorservice.saga;

import static br.com.fpnbr.orchestratorservice.enums.EventSourceEnum.*;
import static br.com.fpnbr.orchestratorservice.enums.SagaStatusEnum.*;
import static br.com.fpnbr.orchestratorservice.enums.TopicsEnum.*;

public final class SagaHandler {

    private SagaHandler() {
    }

    public static final Object[][] SAGA_HANDLER = {
            {ORCHESTRATOR, SUCCESS, PRODUCT_SUCCESS},
            {ORCHESTRATOR, FAILURE, FINISH_FAILURE},

            {PRODUCT_SERVICE, ROLLBACK_PENDING, PRODUCT_FAILURE},
            {PRODUCT_SERVICE, FAILURE, FINISH_FAILURE},
            {PRODUCT_SERVICE, SUCCESS, PAYMENT_SUCCESS},

            {PAYMENT_SERVICE, ROLLBACK_PENDING, PAYMENT_FAILURE},
            {PAYMENT_SERVICE, FAILURE, PRODUCT_FAILURE},
            {PAYMENT_SERVICE, SUCCESS, INVENTORY_SUCCESS},

            {INVENTORY_SERVICE, ROLLBACK_PENDING, INVENTORY_FAILURE},
            {INVENTORY_SERVICE, FAILURE, PAYMENT_FAILURE},
            {INVENTORY_SERVICE, SUCCESS, FINISH_SUCCESS},
    };

    public static final int EVENT_SOURCE_INDEX = 0;
    public static final int SAGA_STATUS_INDEX = 1;
    public static final int TOPIC_INDEX = 2;
}
