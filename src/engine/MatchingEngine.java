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
                break;
            }

            executeTrade(
                    buyOrder,
                    sellOrder,
                    repository
            );


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
    }

    /**
     * Determines whether
     * two orders can match.
     */
    private boolean canExecute(
            Order buyOrder,
            Order sellOrder) {

        if (buyOrder.getExecutionType()
                == OrderExecutionType.MARKET) {
            return true;
        }

        if (sellOrder.getExecutionType()
                == OrderExecutionType.MARKET) {
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

        if (buyOrder.getExecutionType()
                == OrderExecutionType.MARKET
                &&
                sellOrder.getExecutionType()
                        == OrderExecutionType.MARKET) {

            return 0.0;
        }

        if (buyOrder.getExecutionType()
                == OrderExecutionType.MARKET) {

            return sellOrder.getPrice();
        }

        if (sellOrder.getExecutionType()
                == OrderExecutionType.MARKET) {

            return buyOrder.getPrice();
        }

        return sellOrder.getPrice();
    }
    private void updateOrderQuantities(
            Order buyOrder,
            Order sellOrder,
            int tradedQuantity) {

        buyOrder.setQuantity(

                buyOrder.getQuantity()
                        - tradedQuantity
        );

        sellOrder.setQuantity(

                sellOrder.getQuantity()
                        - tradedQuantity
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

                buyOrder.getQuantity() == 0 ||

                        buyOrder.getExecutionType()
                                == OrderExecutionType.MARKET;

        boolean removeSell =

                sellOrder.getQuantity() == 0 ||

                        sellOrder.getExecutionType()
                                == OrderExecutionType.MARKET;

        if (removeBuy) {
            buyOrders.poll();
        }

        if (removeSell) {
            sellOrders.poll();
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
