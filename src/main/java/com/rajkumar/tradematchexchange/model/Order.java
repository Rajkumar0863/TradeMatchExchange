package com.rajkumar.tradematchexchange.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(
            name = "order_id",
            nullable = false,
            updatable = false
    )
    private String orderId;

    @Column(
            name = "stock_symbol",
            nullable = false
    )
    private String stockSymbol;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double price;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "order_type",
            nullable = false
    )
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "execution_type",
            nullable = false
    )
    private OrderExecutionType executionType;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Required by JPA.
     */
    protected Order() {
    }

    /**
     * Creates a brand-new order.
     *
     * New orders receive the current timestamp,
     * which establishes their initial time priority.
     */
    public Order(
            String orderId,
            String stockSymbol,
            int quantity,
            double price,
            OrderType orderType,
            OrderExecutionType executionType) {

        this(
                orderId,
                stockSymbol,
                quantity,
                price,
                orderType,
                executionType,
                LocalDateTime.now()
        );
    }

    /**
     * Creates an order with an explicitly supplied
     * timestamp.
     *
     * This is useful when the application needs the
     * exact same order state in both the in-memory
     * order book and persistent storage.
     */
    public Order(
            String orderId,
            String stockSymbol,
            int quantity,
            double price,
            OrderType orderType,
            OrderExecutionType executionType,
            LocalDateTime timestamp) {

        if (timestamp == null) {
            throw new IllegalArgumentException(
                    "Order timestamp cannot be null."
            );
        }

        this.orderId = orderId;
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.price = price;
        this.orderType = orderType;
        this.executionType = executionType;
        this.timestamp = timestamp;
    }

    public boolean isMarketOrder() {

        return executionType
                == OrderExecutionType.MARKET;
    }

    public boolean isLimitOrder() {

        return executionType
                == OrderExecutionType.LIMIT;
    }

    public String getOrderId() {

        return orderId;
    }

    public String getStockSymbol() {

        return stockSymbol;
    }

    public int getQuantity() {

        return quantity;
    }

    public void setQuantity(
            int quantity) {

        this.quantity = quantity;
    }

    public double getPrice() {

        return price;
    }

    public void setPrice(
            double price) {

        this.price = price;
    }

    public OrderType getOrderType() {

        return orderType;
    }

    public OrderExecutionType getExecutionType() {

        return executionType;
    }

    public LocalDateTime getTimestamp() {

        return timestamp;
    }

    /**
     * Updates the order timestamp.
     *
     * A successful order modification receives
     * a new timestamp because it loses its previous
     * time priority.
     */
    public void setTimestamp(
            LocalDateTime timestamp) {

        if (timestamp == null) {
            throw new IllegalArgumentException(
                    "Order timestamp cannot be null."
            );
        }

        this.timestamp = timestamp;
    }

    @Override
    public String toString() {

        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", stockSymbol='" + stockSymbol + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", orderType=" + orderType +
                ", executionType=" + executionType +
                ", timestamp=" + timestamp +
                '}';
    }
}