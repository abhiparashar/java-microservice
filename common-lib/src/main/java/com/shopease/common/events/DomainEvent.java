package com.shopease.common.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Base class for all domain events in the system
 */

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderCreatedEvent.class, name = "ORDER_CREATED"),
        @JsonSubTypes.Type(value = OrderCancelledEvent.class, name = "ORDER_CANCELLED"),
        @JsonSubTypes.Type(value = PaymentProcessedEvent.class, name = "PAYMENT_PROCESSED"),
        @JsonSubTypes.Type(value = PaymentFailedEvent.class, name = "PAYMENT_FAILED"),
        @JsonSubTypes.Type(value = InventoryReservedEvent.class, name = "INVENTORY_RESERVED"),
        @JsonSubTypes.Type(value = InventoryReleasedEvent.class, name = "INVENTORY_RELEASED"),
        @JsonSubTypes.Type(value = UserRegisteredEvent.class, name = "USER_REGISTERED"),
        @JsonSubTypes.Type(value = ProductCreatedEvent.class, name = "PRODUCT_CREATED"),
        @JsonSubTypes.Type(value = ProductUpdatedEvent.class, name = "PRODUCT_UPDATED"),
        @JsonSubTypes.Type(value = StockUpdatedEvent.class, name = "STOCK_UPDATED"),
        @JsonSubTypes.Type(value = NotificationEvent.class, name = "NOTIFICATION")
})

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class DomainEvent {
    private String eventId = UUID.randomUUID().toString();
    private LocalDateTime timestamp = LocalDateTime.now();
    private String aggregateId;
    private String userId;
    private String correlationId;
    private String sagaId;
    private int version = 1;

    public abstract String getEventType();
}
