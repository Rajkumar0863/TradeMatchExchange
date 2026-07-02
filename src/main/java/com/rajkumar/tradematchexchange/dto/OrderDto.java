package com.rajkumar.tradematchexchange.dto;

import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;

import java.time.LocalDateTime;

public class OrderDto {

    private String orderId;
    private String stockSymbol;
    private int quantity;
    private double price;
    private OrderType orderType;
    private OrderExecutionType executionType;
    private LocalDateTime timestamp;

    public OrderDto() {
    }

    public OrderDto(Order order) {
        this.orderId = order.getOrderId();
        this.stockSymbol = order.getStockSymbol();
        this.quantity = order.getQuantity();
        this.price = order.getPrice();
        this.orderType = order.getOrderType();
        this.executionType = order.getExecutionType();
        this.timestamp = order.getTimestamp();
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

    public double getPrice() {
        return price;
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
}