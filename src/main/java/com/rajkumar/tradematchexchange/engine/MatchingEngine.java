package com.rajkumar.tradematchexchange.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.Trade;
import com.rajkumar.tradematchexchange.repository.TradeRepository;

public class MatchingEngine {

    /**
     * Handles IOC and FOK orders after an execution.
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
                && buyOrder.getQuantity() > 0) {

            buyOrders.remove(buyOrder);
        }

        if (sellOrder.getExecutionType()
                == OrderExecutionType.FOK
                && sellOrder.getQuantity() > 0) {

            sellOrders.remove(sellOrder);
        }
    }

    /**
     * Returns total BUY volume.
     */
    private int totalBuyVolume(
            PriorityQueue<Order> orders) {

        int volume = 0;

        for (Order order : orders) {
            volume += order.getQuantity();
        }

        return volume;
    }

    /**
     * Returns total SELL volume.
     */
    private int totalSellVolume(
            PriorityQueue<Order> orders) {

        int volume = 0;

        for (Order order : orders) {
            volume += order.getQuantity();
        }

        return volume;
    }

    /**
     * Removes FOK orders that cannot be completely filled.
     *
     * Removed orders are recorded as affected so that
     * the persistence layer can remove them from
     * the active-orders table.
     */
    private void validateFillOrKill(
            PriorityQueue<Order> buyOrders,
            PriorityQueue<Order> sellOrders,
            Map<String, Order> affectedOrders) {

        Order buy = buyOrders.peek();
        Order sell = sellOrders.peek();

        if (buy != null
                && buy.getExecutionType()
                == OrderExecutionType.FOK) {

            if (buy.getQuantity()
                    > totalSellVolume(sellOrders)) {

                Order removed =
                        buyOrders.poll();

                if (removed != null) {

                    affectedOrders.put(
                            removed.getOrderId(),
                            removed
                    );
                }
            }
        }

        if (sell != null
                && sell.getExecutionType()
                == OrderExecutionType.FOK) {

            if (sell.getQuantity()
                    > totalBuyVolume(buyOrders)) {

                Order removed =
                        sellOrders.poll();

                if (removed != null) {

                    affectedOrders.put(
                            removed.getOrderId(),
                            removed
                    );
                }
            }
        }
    }

    /**
     * Core matching loop.
     *
     * Returns only orders whose state
     * was affected by matching.
     *
     * Supports:
     * - LIMIT Orders
     * - MARKET Orders
     * - IOC Orders
     * - FOK Orders
     * - Price-Time Priority
     */
    public List<Order> match(
            OrderBook orderBook,
            TradeRepository repository) {

        PriorityQueue<Order> buyOrders =
                orderBook.getBuyOrders();

        PriorityQueue<Order> sellOrders =
                orderBook.getSellOrders();

        Map<String, Order> affectedOrders =
                new LinkedHashMap<>();

        validateFillOrKill(
                buyOrders,
                sellOrders,
                affectedOrders
        );

        while (!buyOrders.isEmpty()
                && !sellOrders.isEmpty()) {

            Order buyOrder =
                    buyOrders.peek();

            Order sellOrder =
                    sellOrders.peek();

            if (!canExecute(
                    buyOrder,
                    sellOrder)) {

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

                affectedOrders.put(
                        buyOrder.getOrderId(),
                        buyOrder
                );

                affectedOrders.put(
                        sellOrder.getOrderId(),
                        sellOrder
                );

            } catch (IllegalStateException exception) {

                System.out.println(
                        "\nTrade Rejected : "
                                + exception.getMessage()
                );

                Order removedBuy =
                        buyOrders.poll();

                Order removedSell =
                        sellOrders.poll();

                if (removedBuy != null) {

                    affectedOrders.put(
                            removedBuy.getOrderId(),
                            removedBuy
                    );
                }

                if (removedSell != null) {

                    affectedOrders.put(
                            removedSell.getOrderId(),
                            removedSell
                    );
                }

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

        if (buyOrders.isEmpty()
                || sellOrders.isEmpty()) {

            System.out.println(
                    "\nMatching Completed."
            );
        }

        return new ArrayList<>(
                affectedOrders.values()
        );
    }

    /**
     * Determines whether two orders
     * can execute against each other.
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

    /**
     * Executes a trade between
     * the current best BUY and SELL.
     */
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

        repository.save(trade);

        updateOrderQuantities(
                buyOrder,
                sellOrder,
                tradedQuantity
        );

        printTrade(trade);
    }

    /**
     * Determines the execution price.
     */
    private double determineExecutionPrice(
            Order buyOrder,
            Order sellOrder) {

        /*
         * MARKET vs MARKET.
         *
         * Temporary behaviour.
         * We will improve this separately.
         */
        if (buyOrder.getExecutionType()
                == OrderExecutionType.MARKET
                && sellOrder.getExecutionType()
                == OrderExecutionType.MARKET) {

            return 0.0;
        }

        /*
         * MARKET BUY executes
         * at SELL price.
         */
        if (buyOrder.getExecutionType()
                == OrderExecutionType.MARKET) {

            return sellOrder.getPrice();
        }

        /*
         * MARKET SELL executes
         * at BUY price.
         */
        if (sellOrder.getExecutionType()
                == OrderExecutionType.MARKET) {

            return buyOrder.getPrice();
        }

        /*
         * LIMIT vs LIMIT executes
         * at resting SELL price.
         */
        return sellOrder.getPrice();
    }

    /**
     * Updates remaining quantities
     * after a successful trade.
     */
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
     * Removes fully executed or
     * non-resting MARKET orders.
     */
    private void removeCompletedOrders(
            PriorityQueue<Order> buyOrders,
            PriorityQueue<Order> sellOrders,
            Order buyOrder,
            Order sellOrder) {

        boolean removeBuy =
                buyOrder.getQuantity() <= 0
                        || buyOrder.getExecutionType()
                        == OrderExecutionType.MARKET;

        boolean removeSell =
                sellOrder.getQuantity() <= 0
                        || sellOrder.getExecutionType()
                        == OrderExecutionType.MARKET;

        if (removeBuy) {

            buyOrders.remove(
                    buyOrder
            );
        }

        if (removeSell) {

            sellOrders.remove(
                    sellOrder
            );
        }
    }

    /**
     * Generates a globally unique trade ID.
     *
     * Unlike the previous JVM-local counter,
     * UUID-based IDs remain safe across:
     *
     * - application restarts
     * - persisted database records
     * - multiple application instances
     */
    private String nextTradeId() {

        return "TRD-"
                + UUID.randomUUID()
                .toString()
                .toUpperCase();
    }

    /**
     * Prints executed trade information.
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