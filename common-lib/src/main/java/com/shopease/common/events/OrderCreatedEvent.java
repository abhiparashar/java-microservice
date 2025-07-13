package com.shopease.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;

import java.math.BigDecimal;
import java.util.List;

/**
 * Event published when an order is created
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedEvent extends DomainEvent{
    private String orderId;
    private String customerId;
    private BigDecimal totalAmount;
    private List<OrderItem> items;
    private String paymentMethod;
    private ShippingAddress shippingAddress;
    private String orderStatus;
    @Override
    public String getEventType() {
        return "ORDER_CREATED";
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItem{
        private String productId;
        private String productName;
        private BigDecimal price;
        private BigDecimal totalPrice;
        private int quantity;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShippingAddress{
        private String street;
        private String city;
        private String state;
        private String zipcode;
        private String country;
    }
}

/**
 * Event published when an order is cancelled
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
class OrderCancelledEvent extends DomainEvent{
    private String orderId;
    private String customerId;
    private String reason;
    private BigDecimal refundAmount;
    @Override
    public String getEventType() {
        return "ORDER_CANCELLED";
    }
}

/**
 * Event published when payment is processed
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
class PaymentProcessedEvent extends DomainEvent{
    private String paymentId;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId;
    private String status;
    @Override
    public String getEventType() {
        return "PAYMENT_PROCESSED";
    }
}

/**
 * Event published when payment fails
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
class PaymentFailedEvent extends DomainEvent{
    private String paymentId;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String paymentMethod;
    private String reason;
    private String errorCode;
    @Override
    public String getEventType() {
        return "PAYMENT_FAILED";
    }
}

/**
 * Event published when inventory is reserved
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
class InventoryReservedEvent  extends DomainEvent{
    private String orderId;
    private String productId;
    private int quantity;
    private String reservationId;
    private String warehouseId;

    @Override
    public String getEventType() {
        return "INVENTORY_RESERVED";
    }
}

/**
 * Event published when inventory is released
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
class InventoryReleasedEvent extends DomainEvent{
    private String reservationId;
    private String orderId;
    private String productId;
    private int quantity;
    private String reason;
    @Override
    public String getEventType() {
        return "INVENTORY_RELEASED";
    }
}

/**
 * Event published when user registers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
class UserRegisteredEvent extends DomainEvent {
    private String email;
    private String firstName;
    private String lastName;
    private String customerTier;
    private boolean emailVerified;

    @Override
    public String getEventType() {
        return "USER_REGISTERED";
    }
}

/**
 * Event published when product is created
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
class ProductCreatedEvent extends DomainEvent {
    private String productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String brand;
    private String sku;

    @Override
    public String getEventType() {
        return "PRODUCT_CREATED";
    }
}

/**
 * Event published when product is updated
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
class ProductUpdatedEvent extends DomainEvent {
    private String productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String brand;
    private String sku;

    @Override
    public String getEventType() {
        return "PRODUCT_UPDATED";
    }
}

/**
 * Event published when stock is updated
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
class StockUpdatedEvent extends DomainEvent {
    private String productId;
    private int quantity;
    private int previousQuantity;
    private String operation; // INCREASE, DECREASE, RESERVE, RELEASE
    private String warehouseId;

    @Override
    public String getEventType() {
        return "STOCK_UPDATED";
    }
}

/**
 * Event published for notifications
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
class NotificationEvent extends DomainEvent {
    private String recipientId;
    private String recipientEmail;
    private String notificationType; // EMAIL, SMS, PUSH
    private String subject;
    private String message;
    private String templateId;
    private String priority; // LOW, MEDIUM, HIGH, URGENT

    @Override
    public String getEventType() {
        return "NOTIFICATION";
    }
}
