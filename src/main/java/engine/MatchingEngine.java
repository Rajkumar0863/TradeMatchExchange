package engine;

import model.Order;
import model.OrderExecutionType;
import model.Trade;
import repository.TradeRepository;

import java.util.PriorityQueue;

public class MatchingEngine {

    private long tradeCounter;

    public MatchingEngine() {

        this.tradeCounter = 1;
    }

    /**
     * Core matching loop.
     *
     * Supports:
     * - LIMIT Orders
     * - MARKET Orders
     * - Price-Time Priority
     */
    private void handleSpecialExecutionTypes(

            PriorityQueue<Order> buyOrders,

            PriorityQueue<Order> sellOrders,

            Order buyOrder,

            Order sellOrder) {

        if (buyOrder.getExecutionType()
                == OrderExecutionType.IOC) {

            buyOrders.remove(buyOrder);
        }

        if (sellOrder.getExecutionType()
                == OrderExecutionType.IOC) {

            sellOrders.remove(sellOrder);
        }

        if (buyOrder.getExecutionType()
                == OrderExecutionType.FOK
                &&
                buyOrder.getQuantity() > 0) {

            buyOrders.remove(buyOrder);
        }

        if (sellOrder.getExecutionType()
                == OrderExecutionType.FOK
                &&
                sellOrder.getQuantity() > 0) {

            sellOrders.remove(sellOrder);
        }
    }

    private int totalBuyVolume(
            PriorityQueue<Order> orders) {

        int volume = 0;

        for (Order order : orders) {
            volume += order.getQuantity();
        }

        return volume;
    }

    private int totalSellVolume(
            PriorityQueue<Order> orders) {

        int volume = 0;

        for (Order order : orders) {
            volume += order.getQuantity();
        }

        return volume;
    }

    private void validateFillOrKill(

            PriorityQueue<Order> buyOrders,

            PriorityQueue<Order> sellOrders) {

        Order buy = buyOrders.peek();

        Order sell = sellOrders.peek();

        if (buy != null
                &&
                buy.getExecutionType()
                        == OrderExecutionType.FOK) {

            if (buy.getQuantity()
                    > totalSellVolume(sellOrders)) {

                buyOrders.poll();
            }
        }

        if (sell != null
                &&
                sell.getExecutionType()
                        == OrderExecutionType.FOK) {

            if (sell.getQuantity()
                    > totalBuyVolume(buyOrders)) {

                sellOrders.poll();
            }
        }
    }
    public void match(
            OrderBook orderBook,
            TradeRepository repository) {

        PriorityQueue<Order> buyOrders =
                orderBook.getBuyOrders();

        PriorityQueue<Order> sellOrders =
                orderBook.getSellOrders();

        validateFillOrKill(
                buyOrders,
                sellOrders
        );

        while (!buyOrders.isEmpty()
                && !sellOrders.isEmpty()) {

            Order buyOrder = buyOrders.peek();
            Order sellOrder = sellOrders.peek();

            if (!canExecute(buyOrder, sellOrder)) {

                System.out.println(
                        "\nNo More Matchable Orders."
                );

                break;
            }
            try {

                executeTrade(
                        buyOrder,
                        sellOrder,
                        repository
                );

            } catch (IllegalStateException exception) {

                System.out.println(
                        "\nTrade Rejected : "
                                + exception.getMessage()
                );

                buyOrders.poll();
                sellOrders.poll();

                continue;
            }


            handleSpecialExecutionTypes(
                    buyOrders,
                    sellOrders,
                    buyOrder,
                    sellOrder
            );

            removeCompletedOrders(
                    buyOrders,
                    sellOrders,
                    buyOrder,
                    sellOrder
            );
        }
        if (buyOrders.isEmpty() || sellOrders.isEmpty()) {

            System.out.println(
                    "\nMatching Completed."
            );
        }
    }

    /**
     * Determines whether
     * two orders can match.
     */
    private boolean canExecute(
            Order buyOrder,
            Order sellOrder) {

        if (buyOrder.getExecutionType() == OrderExecutionType.MARKET) {
            return true;
        }

        if (sellOrder.getExecutionType() == OrderExecutionType.MARKET) {
            return true;
        }

        return buyOrder.getPrice()
                >= sellOrder.getPrice();
    }
    private void executeTrade(
            Order buyOrder,
            Order sellOrder,
            TradeRepository repository) {

        int tradedQuantity =
                Math.min(
                        buyOrder.getQuantity(),
                        sellOrder.getQuantity()
                );
        if (tradedQuantity <= 0) {

            throw new IllegalStateException(
                    "Invalid traded quantity."
            );
        }

        double executionPrice =
                determineExecutionPrice(
                        buyOrder,
                        sellOrder
                );

        Trade trade =
                new Trade(
                        nextTradeId(),
                        buyOrder.getOrderId(),
                        sellOrder.getOrderId(),
                        tradedQuantity,
                        executionPrice
                );

        repository.addTrade(trade);

        updateOrderQuantities(
                buyOrder,
                sellOrder,
                tradedQuantity
        );

        printTrade(trade);
    }
    /**
     * Determines execution price.
     */
    private double determineExecutionPrice(
            Order buyOrder,
            Order sellOrder) {

        /*
         * MARKET vs MARKET
         * (Temporary implementation)
         */
        if (buyOrder.getExecutionType() == OrderExecutionType.MARKET
                && sellOrder.getExecutionType() == OrderExecutionType.MARKET) {

            return 0.0;
        }

        /*
         * MARKET BUY
         * Executes at SELL price.
         */
        if (buyOrder.getExecutionType() == OrderExecutionType.MARKET) {

            return sellOrder.getPrice();
        }

        /*
         * MARKET SELL
         * Executes at BUY price.
         */
        if (sellOrder.getExecutionType() == OrderExecutionType.MARKET) {

            return buyOrder.getPrice();
        }

        /*
         * LIMIT vs LIMIT
         * Executes at resting SELL price.
         */
        return sellOrder.getPrice();
    }
    private void updateOrderQuantities(
            Order buyOrder,
            Order sellOrder,
            int tradedQuantity) {

        buyOrder.setQuantity(

                Math.max(
                        0,
                        buyOrder.getQuantity()
                                - tradedQuantity
                )
        );

        sellOrder.setQuantity(

                Math.max(
                        0,
                        sellOrder.getQuantity()
                                - tradedQuantity
                )
        );
    }

    /**
     * Removes fully executed orders.
     */
    private void removeCompletedOrders(

            PriorityQueue<Order> buyOrders,

            PriorityQueue<Order> sellOrders,

            Order buyOrder,

            Order sellOrder) {

        boolean removeBuy =

                buyOrder.getQuantity() <= 0 ||

                        buyOrder.getExecutionType()
                                == OrderExecutionType.MARKET;
        boolean removeSell =

                sellOrder.getQuantity() <= 0 ||

                        sellOrder.getExecutionType()
                                == OrderExecutionType.MARKET;
        if (removeBuy) {
            buyOrders.remove(buyOrder);
        }

        if (removeSell) {
            sellOrders.remove(sellOrder);
        }
    }
    private String nextTradeId() {

        return String.format(
                "TRD%06d",
                tradeCounter++
        );
    }

    /**
     * Prints executed trade.
     */
    private void printTrade(
            Trade trade) {

        System.out.println();

        System.out.println(
                "===================================="
        );

        System.out.println(
                "          TRADE EXECUTED"
        );

        System.out.println(
                "===================================="
        );

        System.out.println(
                "Trade ID      : "
                        + trade.getTradeId()
        );

        System.out.println(
                "Buy Order ID  : "
                        + trade.getBuyOrderId()
        );

        System.out.println(
                "Sell Order ID : "
                        + trade.getSellOrderId()
        );

        System.out.println(
                "Quantity      : "
                        + trade.getQuantity()
        );

        System.out.println(
                "Price         : "
                        + trade.getExecutionPrice()
        );

        System.out.println(
                "===================================="
        );
    }
}
