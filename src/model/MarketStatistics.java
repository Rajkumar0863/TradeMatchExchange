package model;

import java.util.List;

public class MarketStatistics {

    private int totalTrades;
    private int totalVolume;
    private double highestPrice;
    private double lowestPrice;
    private double averagePrice;

    public MarketStatistics(List<Trade> trades) {

        totalTrades = trades.size();

        if (trades.isEmpty()) {
            return;
        }

        highestPrice = Double.MIN_VALUE;
        lowestPrice = Double.MAX_VALUE;

        double totalPrice = 0;

        for (Trade trade : trades) {

            totalVolume += trade.getQuantity();

            double price = trade.getExecutionPrice();

            highestPrice = Math.max(highestPrice, price);
            lowestPrice = Math.min(lowestPrice, price);

            totalPrice += price;
        }

        averagePrice = totalPrice / totalTrades;
    }

    @Override
    public String toString() {

        return "\n===== MARKET STATISTICS =====" +
                "\nTotal Trades: " + totalTrades +
                "\nTotal Volume: " + totalVolume +
                "\nHighest Price: " + highestPrice +
                "\nLowest Price: " + lowestPrice +
                "\nAverage Price: " + averagePrice;
    }
}