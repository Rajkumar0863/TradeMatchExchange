package model;

import java.util.List;

public class MarketStatistics {

    private int totalTrades;
    private int totalVolume;

    private double highestPrice;
    private double lowestPrice;

    private double averagePrice;

    private double totalTradedValue;
    private double vwap;

    public MarketStatistics(List<Trade> trades) {

        totalTrades = trades.size();

        if (trades.isEmpty()) {
            return;
        }

        highestPrice = Double.MIN_VALUE;
        lowestPrice = Double.MAX_VALUE;

        double totalPrice = 0;

        for (Trade trade : trades) {

            int quantity = trade.getQuantity();

            double price = trade.getExecutionPrice();

            totalVolume += quantity;

            highestPrice =
                    Math.max(highestPrice, price);

            lowestPrice =
                    Math.min(lowestPrice, price);

            totalPrice += price;

            totalTradedValue +=
                    (price * quantity);
        }

        averagePrice =
                totalPrice / totalTrades;

        vwap =
                totalTradedValue / totalVolume;
    }

    @Override
    public String toString() {

        return "\n===== MARKET STATISTICS =====" +

                "\nTotal Trades: " + totalTrades +

                "\nTotal Volume: " + totalVolume +

                "\nHighest Price: " + highestPrice +

                "\nLowest Price: " + lowestPrice +

                "\nAverage Price: " + averagePrice +

                "\nVWAP: " +
                String.format("%.2f", vwap) +

                "\nTotal Traded Value: " +
                String.format("%.2f", totalTradedValue);
    }
}