package model;

import java.time.LocalDateTime;

public class Order {

    private String orderId;
    private String stockSymbol;
    private int quantity;
    private double price;
    private OrderType orderType;
    private OrderExecutionType executionType;
    private LocalDateTime timestamp;
    public boolean isMarketOrder() {

        return executionType ==
                OrderExecutionType.MARKET;
    }

    public boolean isLimitOrder() {

        return executionType ==
                OrderExecutionType.LIMIT;
    }
    public Order(
            String orderId,
            String stockSymbol,
            int quantity,
            double price,
            OrderType orderType,
            OrderExecutionType executionType) {

        this.orderId = orderId;
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.price = price;
        this.orderType = orderType;
        this.executionType = executionType;
        this.timestamp = LocalDateTime.now();
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

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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