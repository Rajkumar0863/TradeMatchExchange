package model;

import repository.TradeRepository;

public class MarketStatistics {

    private int totalTrades;
    private int totalVolume;

    private double highestPrice;
    private double lowestPrice;

    private double averagePrice;
    private double totalTradedValue;
    private double vwap;

    public MarketStatistics(TradeRepository repository) {

        calculate(repository);
    }

    /**
     * Calculates all market statistics.
     */
    private void calculate(TradeRepository repository) {

        if (repository == null || repository.isEmpty()) {

            highestPrice = 0.0;
            lowestPrice = 0.0;
            averagePrice = 0.0;
            totalTradedValue = 0.0;
            vwap = 0.0;

            return;
        }

        totalTrades = repository.getTradeCount();

        highestPrice = Double.NEGATIVE_INFINITY;
        lowestPrice = Double.POSITIVE_INFINITY;

        double totalPrice = 0.0;

        for (Trade trade : repository.getTrades()) {

            int quantity = trade.getQuantity();
            double price = trade.getExecutionPrice();

            totalVolume += quantity;

            totalPrice += price;

            totalTradedValue += price * quantity;

            highestPrice = Math.max(highestPrice, price);
            lowestPrice = Math.min(lowestPrice, price);
        }

        averagePrice = totalPrice / totalTrades;

        if (totalVolume > 0) {

            vwap = totalTradedValue / totalVolume;
        }
    }

    public int getTotalTrades() {
        return totalTrades;
    }

    public int getTotalVolume() {
        return totalVolume;
    }

    public double getHighestPrice() {
        return highestPrice;
    }

    public double getLowestPrice() {
        return lowestPrice;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public double getVWAP() {
        return vwap;
    }

    public double getTotalTradedValue() {
        return totalTradedValue;
    }

    public void displayStatistics() {

        System.out.println("\n======================================");
        System.out.println("        MARKET STATISTICS");
        System.out.println("======================================");

        System.out.printf("Total Trades       : %d%n", totalTrades);
        System.out.printf("Total Volume       : %d%n", totalVolume);
        System.out.printf("Highest Price      : %.2f%n", highestPrice);
        System.out.printf("Lowest Price       : %.2f%n", lowestPrice);
        System.out.printf("Average Price      : %.2f%n", averagePrice);
        System.out.printf("VWAP               : %.2f%n", vwap);
        System.out.printf("Total Trade Value  : %.2f%n", totalTradedValue);

        System.out.println("======================================");
    }

    @Override
    public String toString() {

        return "MarketStatistics{" +
                "totalTrades=" + totalTrades +
                ", totalVolume=" + totalVolume +
                ", highestPrice=" + highestPrice +
                ", lowestPrice=" + lowestPrice +
                ", averagePrice=" + averagePrice +
                ", totalTradedValue=" + totalTradedValue +
                ", vwap=" + vwap +
                '}';
    }
}