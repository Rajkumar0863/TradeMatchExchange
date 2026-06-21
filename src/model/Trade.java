package model;

import java.time.LocalDateTime;

public class Trade {

    private String tradeId;
    private String buyOrderId;
    private String sellOrderId;
    private int quantity;
    private double executionPrice;
    private LocalDateTime timestamp;

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