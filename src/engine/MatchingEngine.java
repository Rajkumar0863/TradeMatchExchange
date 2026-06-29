package engine;

import model.Order;
import model.Trade;
import repository.TradeRepository;

import java.util.PriorityQueue;

public class MatchingEngine {

    private long tradeCounter;

    public MatchingEngine() {

        this.tradeCounter = 1;
    }

    /**
     * Executes price-time priority matching.
     *
     * @param orderBook Active order book
     * @param repository Trade repository
     */
    public void match(
            OrderBook orderBook,
            TradeRepository repository) {

        PriorityQueue<Order> buyOrders =
                orderBook.getBuyOrders();

        PriorityQueue<Order> sellOrders =
                orderBook.getSellOrders();

        while (!buyOrders.isEmpty()
                && !sellOrders.isEmpty()) {

            Order bestBuy = buyOrders.peek();
            Order bestSell = sellOrders.peek();

            /*
             * No price match
             */

            if (bestBuy.getPrice() < bestSell.getPrice()) {
                break;
            }

            int tradedQuantity =
                    Math.min(
                            bestBuy.getQuantity(),
                            bestSell.getQuantity());

            double executionPrice =
                    bestSell.getPrice();

            Trade trade =
                    new Trade(
                            generateTradeId(),
                            bestBuy.getOrderId(),
                            bestSell.getOrderId(),
                            tradedQuantity,
                            executionPrice);

            repository.addTrade(trade);

            System.out.println("\n=================================");
            System.out.println("TRADE EXECUTED");
            System.out.println("=================================");
            System.out.println("Trade ID : " + trade.toString());
            System.out.println("BUY      : " + bestBuy.getOrderId());
            System.out.println("SELL     : " + bestSell.getOrderId());
            System.out.println("Quantity : " + tradedQuantity);
            System.out.println("Price    : " + executionPrice);
            System.out.println("=================================");

            bestBuy.setQuantity(
                    bestBuy.getQuantity()
                            - tradedQuantity);

            bestSell.setQuantity(
                    bestSell.getQuantity()
                            - tradedQuantity);

            if (bestBuy.getQuantity() == 0) {
                buyOrders.poll();
            }

            if (bestSell.getQuantity() == 0) {
                sellOrders.poll();
            }
        }
    }

    /**
     * Generates sequential Trade IDs.
     */
    private String generateTradeId() {

        return String.format(
                "TRD%06d",
                tradeCounter++);
    }
}