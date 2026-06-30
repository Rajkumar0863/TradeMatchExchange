package model;

import java.time.LocalDateTime;

public class Trade {

    private final String tradeId;
    private final String buyOrderId;
    private final String sellOrderId;
    private final int quantity;
    private final double executionPrice;
    private final LocalDateTime timestamp;

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