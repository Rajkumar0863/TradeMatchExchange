package com.rajkumar.tradematchexchange.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;
import com.rajkumar.tradematchexchange.model.Trade;
import com.rajkumar.tradematchexchange.repository.TradeRepository;

public class MatchingEngine {

    /**
     * Core matching loop.
     *
     * Supports:
     * - LIMIT
     * - MARKET
     * - IOC
     * - FOK
     * - Partial fills
     * - Multiple counterparties
     * - Price-Time Priority
     *
     * Returns only orders whose state changed during matching.
     */
    public List<Order> match(
            OrderBook orderBook,
            TradeRepository repository) {

        if (orderBook == null) {
            throw new IllegalArgumentException(
                    "OrderBook cannot be null."
            );
        }

        if (repository == null) {
            throw new IllegalArgumentException(
                    "TradeRepository cannot be null."
            );
        }

        PriorityQueue<Order> buyOrders =
                orderBook.getBuyOrders();

        PriorityQueue<Order> sellOrders =
                orderBook.getSellOrders();

        Map<String, Order> affectedOrders =
                new LinkedHashMap<>();

        while (!buyOrders.isEmpty()
                && !sellOrders.isEmpty()) {

            Order buyOrder =
                    buyOrders.peek();

            Order sellOrder =
                    sellOrders.peek();

            /*
             * FOK must be completely executable before
             * the first unit of the order is traded.
             */
            if (buyOrder.getExecutionType()
                    == OrderExecutionType.FOK
                    && !canFullyFill(
                    buyOrder,
                    sellOrders)) {

                Order removed =
                        buyOrders.poll();

                recordAffected(
                        affectedOrders,
                        removed
                );

                continue;
            }

            if (sellOrder.getExecutionType()
                    == OrderExecutionType.FOK
                    && !canFullyFill(
                    sellOrder,
                    buyOrders)) {

                Order removed =
                        sellOrders.poll();

                recordAffected(
                        affectedOrders,
                        removed
                );

                continue;
            }

            /*
             * Best BUY and best SELL do not cross.
             *
             * Since the queues use price-time priority,
             * no lower-priority LIMIT order can cross either.
             */
            if (!canExecute(
                    buyOrder,
                    sellOrder)) {

                System.out.println(
                        "\nNo More Matchable Orders."
                );

                break;
            }

            executeTrade(
                    buyOrder,
                    sellOrder,
                    repository
            );

            recordAffected(
                    affectedOrders,
                    buyOrder
            );

            recordAffected(
                    affectedOrders,
                    sellOrder
            );

            removeCompletedOrders(
                    buyOrders,
                    sellOrders,
                    buyOrder,
                    sellOrder
            );
        }

        /*
         * MARKET, IOC and FOK orders are immediate
         * execution instructions.
         *
         * They must never remain resting in the book
         * after the matching cycle finishes.
         */
        removeRemainingImmediateOrders(
                buyOrders,
                affectedOrders
        );

        removeRemainingImmediateOrders(
                sellOrders,
                affectedOrders
        );

        System.out.println(
                "\nMatching Completed."
        );

        return new ArrayList<>(
                affectedOrders.values()
        );
    }

    /**
     * Determines whether a BUY and SELL
     * can execute against each other.
     */
    private boolean canExecute(
            Order buyOrder,
            Order sellOrder) {

        /*
         * MARKET orders accept available liquidity
         * without imposing their own price condition.
         */
        if (buyOrder.getExecutionType()
                == OrderExecutionType.MARKET) {

            return true;
        }

        if (sellOrder.getExecutionType()
                == OrderExecutionType.MARKET) {

            return true;
        }

        /*
         * LIMIT / IOC / FOK orders use their supplied price.
         */
        return buyOrder.getPrice()
                >= sellOrder.getPrice();
    }

    /**
     * Checks whether an FOK order can be completely
     * executed against currently available,
     * price-compatible opposite-side liquidity.
     */
    private boolean canFullyFill(
            Order fokOrder,
            PriorityQueue<Order> oppositeOrders) {

        long executableVolume = 0;

        for (Order oppositeOrder
                : oppositeOrders) {

            if (isExecutableAgainst(
                    fokOrder,
                    oppositeOrder)) {

                executableVolume +=
                        oppositeOrder.getQuantity();

                if (executableVolume
                        >= fokOrder.getQuantity()) {

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Determines whether one opposite-side order
     * provides executable liquidity for an FOK order.
     */
    private boolean isExecutableAgainst(
            Order fokOrder,
            Order oppositeOrder) {

        if (fokOrder.getOrderType()
                == OrderType.BUY) {

            /*
             * A BUY FOK can execute against:
             * - a SELL MARKET order
             * - a SELL order priced at or below BUY price
             */
            return oppositeOrder.getExecutionType()
                    == OrderExecutionType.MARKET
                    || oppositeOrder.getPrice()
                    <= fokOrder.getPrice();
        }

        /*
         * A SELL FOK can execute against:
         * - a BUY MARKET order
         * - a BUY order priced at or above SELL price
         */
        return oppositeOrder.getExecutionType()
                == OrderExecutionType.MARKET
                || oppositeOrder.getPrice()
                >= fokOrder.getPrice();
    }

    /**
     * Executes one trade between the current
     * best BUY and SELL orders.
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

        /*
         * Persist the trade before mutating
         * the order quantities.
         */
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
         * MARKET vs MARKET has no price reference.
         *
         * Under normal API operation this situation should
         * not occur because unmatched MARKET orders are
         * removed immediately instead of resting.
         */
        if (buyOrder.getExecutionType()
                == OrderExecutionType.MARKET
                && sellOrder.getExecutionType()
                == OrderExecutionType.MARKET) {

            throw new IllegalStateException(
                    "Cannot determine execution price "
                            + "for MARKET vs MARKET."
            );
        }

        /*
         * MARKET BUY executes against
         * the SELL order's price.
         */
        if (buyOrder.getExecutionType()
                == OrderExecutionType.MARKET) {

            return sellOrder.getPrice();
        }

        /*
         * MARKET SELL executes against
         * the BUY order's price.
         */
        if (sellOrder.getExecutionType()
                == OrderExecutionType.MARKET) {

            return buyOrder.getPrice();
        }

        /*
         * Existing simplified exchange rule:
         * LIMIT/IOC/FOK vs LIMIT/IOC/FOK
         * executes at the SELL price.
         */
        return sellOrder.getPrice();
    }

    /**
     * Reduces remaining quantities after
     * a successful execution.
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
     * Removes orders that have been completely filled.
     *
     * MARKET / IOC / FOK orders with remaining quantity
     * are deliberately kept until the matching cycle
     * finishes so they can consume multiple counterparties.
     */
    private void removeCompletedOrders(
            PriorityQueue<Order> buyOrders,
            PriorityQueue<Order> sellOrders,
            Order buyOrder,
            Order sellOrder) {

        if (buyOrder.getQuantity() <= 0) {

            buyOrders.remove(
                    buyOrder
            );
        }

        if (sellOrder.getQuantity() <= 0) {

            sellOrders.remove(
                    sellOrder
            );
        }
    }

    /**
     * Removes any unfilled remainder of immediate
     * execution instructions.
     *
     * LIMIT is the only execution type allowed
     * to remain resting in the order book.
     */
    private void removeRemainingImmediateOrders(
            PriorityQueue<Order> orders,
            Map<String, Order> affectedOrders) {

        Iterator<Order> iterator =
                orders.iterator();

        while (iterator.hasNext()) {

            Order order =
                    iterator.next();

            if (isImmediateOrder(order)) {

                iterator.remove();

                recordAffected(
                        affectedOrders,
                        order
                );
            }
        }
    }

    /**
     * MARKET, IOC and FOK are all
     * immediate execution instructions.
     */
    private boolean isImmediateOrder(
            Order order) {

        return order.getExecutionType()
                == OrderExecutionType.MARKET
                || order.getExecutionType()
                == OrderExecutionType.IOC
                || order.getExecutionType()
                == OrderExecutionType.FOK;
    }

    /**
     * Records an affected order once by order ID.
     */
    private void recordAffected(
            Map<String, Order> affectedOrders,
            Order order) {

        if (order == null) {
            return;
        }

        affectedOrders.put(
                order.getOrderId(),
                order
        );
    }

    /**
     * Generates a restart-safe trade identifier.
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