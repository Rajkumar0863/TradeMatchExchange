package engine;

import model.Order;
import model.OrderExecutionType;
import model.Trade;
import repository.TradeRepository;

import java.util.PriorityQueue;

public class MatchingEngine {

    private long tradeCounter;

    public MatchingEngine() {

        tradeCounter = 1;
    }

    /**
     * Matches BUY and SELL orders using
     * Price-Time Priority.
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
             * Check whether the orders
             * are executable.
             */

            if (!canExecute(bestBuy, bestSell)) {
                break;
            }

            int tradedQuantity =
                    Math.min(
                            bestBuy.getQuantity(),
                            bestSell.getQuantity());

            double executionPrice =
                    determineExecutionPrice(
                            bestBuy,
                            bestSell);

            Trade trade =
                    new Trade(
                            generateTradeId(),
                            bestBuy.getOrderId(),
                            bestSell.getOrderId(),
                            tradedQuantity,
                            executionPrice);

            repository.addTrade(trade);

            printTrade(trade);

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
     * Determines whether
     * two orders can execute.
     */
    private boolean canExecute(
            Order buy,
            Order sell) {

        if (buy.getExecutionType()
                == OrderExecutionType.MARKET) {

            return true;
        }

        if (sell.getExecutionType()
                == OrderExecutionType.MARKET) {

            return true;
        }

        return buy.getPrice()
                >= sell.getPrice();
    }

    /**
     * Determines execution price.
     */
    private double determineExecutionPrice(
            Order buy,
            Order sell) {

        if (buy.getExecutionType()
                == OrderExecutionType.MARKET) {

            return sell.getPrice();
        }

        if (sell.getExecutionType()
                == OrderExecutionType.MARKET) {

            return buy.getPrice();
        }

        return sell.getPrice();
    }

    /**
     * Generates sequential trade IDs.
     */
    private String generateTradeId() {

        return String.format(
                "TRD%06d",
                tradeCounter++);
    }
    /**
     * Prints executed trade details.
     */
    private void printTrade(Trade trade) {

        System.out.println();
        System.out.println("====================================");
        System.out.println("          TRADE EXECUTED");
        System.out.println("====================================");
        System.out.println("Trade ID      : " + trade.getTradeId());
        System.out.println("Buy Order ID  : " + trade.getBuyOrderId());
        System.out.println("Sell Order ID : " + trade.getSellOrderId());
        System.out.println("Quantity      : " + trade.getQuantity());
        System.out.println("Price         : " + trade.getExecutionPrice());
        System.out.println("====================================");
    }

    /**
     * Returns the next trade number.
     */
    public long getTradeCounter() {

        return tradeCounter;
    }

    /**
     * Resets the trade counter.
     */
    public void resetCounter() {

        tradeCounter = 1;
    }
}