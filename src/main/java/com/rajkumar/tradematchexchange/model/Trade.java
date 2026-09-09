package com.rajkumar.tradematchexchange.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @Column(name = "trade_id", nullable = false, updatable = false)
    private String tradeId;

    @Column(name = "buy_order_id", nullable = false)
    private String buyOrderId;

    @Column(name = "sell_order_id", nullable = false)
    private String sellOrderId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "execution_price", nullable = false)
    private double executionPrice;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected Trade() {
        // Required by JPA
    }

    public Trade(
            String tradeId,
            String buyOrderId,
            String sellOrderId,
            int quantity,
            double executionPrice) {

        this.tradeId = tradeId;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.quantity = quantity;
        this.executionPrice = executionPrice;
        this.timestamp = LocalDateTime.now();
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getBuyOrderId() {
        return buyOrderId;
    }

    public String getSellOrderId() {
        return sellOrderId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getExecutionPrice() {
        return executionPrice;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String toCsvRow() {
        return tradeId + "," +
                buyOrderId + "," +
                sellOrderId + "," +
                quantity + "," +
                executionPrice + "," +
                timestamp;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "tradeId='" + tradeId + '\'' +
                ", buyOrderId='" + buyOrderId + '\'' +
                ", sellOrderId='" + sellOrderId + '\'' +
                ", quantity=" + quantity +
                ", executionPrice=" + executionPrice +
                ", timestamp=" + timestamp +
                '}';
    }
}