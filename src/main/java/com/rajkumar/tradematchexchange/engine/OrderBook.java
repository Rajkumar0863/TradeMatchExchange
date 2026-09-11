package com.rajkumar.tradematchexchange.engine;

import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class OrderBook {

    private final PriorityQueue<Order> buyOrders;

    private final PriorityQueue<Order> sellOrders;

    public OrderBook() {

        /*
         * BUY priority:
         *
         * 1. MARKET orders
         * 2. Highest price
         * 3. Earliest timestamp
         */
        buyOrders =
                new PriorityQueue<>(
                        Comparator
                                .comparing(
                                        (Order order) ->
                                                order.getExecutionType()
                                                        == OrderExecutionType.MARKET
                                )
                                .reversed()
                                .thenComparing(
                                        Order::getPrice,
                                        Comparator.reverseOrder()
                                )
                                .thenComparing(
                                        Order::getTimestamp
                                )
                );

        /*
         * SELL priority:
         *
         * 1. MARKET orders
         * 2. Lowest price
         * 3. Earliest timestamp
         */
        sellOrders =
                new PriorityQueue<>(
                        Comparator
                                .comparing(
                                        (Order order) ->
                                                order.getExecutionType()
                                                        == OrderExecutionType.MARKET
                                )
                                .reversed()
                                .thenComparing(
                                        Order::getPrice
                                )
                                .thenComparing(
                                        Order::getTimestamp
                                )
                );
    }

    /**
     * Adds an order to the appropriate side
     * of the order book.
     */
    public void addOrder(
            Order order) {

        if (order == null) {

            throw new IllegalArgumentException(
                    "Order cannot be null."
            );
        }

        if (order.getOrderType()
                == OrderType.BUY) {

            buyOrders.offer(order);

        } else {

            sellOrders.offer(order);
        }
    }

    /**
     * Cancels an active order.
     */
    public boolean cancelOrder(
            String orderId) {

        return removeOrder(
                buyOrders,
                orderId
        ) || removeOrder(
                sellOrders,
                orderId
        );
    }

    /**
     * Modifies an active order.
     *
     * A successful modification loses its
     * previous time priority.
     *
     * The existing Order object is removed from
     * the PriorityQueue before mutation because
     * quantity, price and timestamp can affect
     * queue ordering.
     *
     * After modification, the same object is
     * reinserted with a new timestamp.
     *
     * Using the same Order instance also allows
     * the service/persistence layer to observe
     * exactly the same modified state.
     */
    public boolean modifyOrder(
            String orderId,
            int quantity,
            double price) {

        Order order =
                findOrder(
                        buyOrders,
                        orderId
                );

        if (order != null) {

            return modifyOrderInQueue(
                    buyOrders,
                    order,
                    quantity,
                    price
            );
        }

        order =
                findOrder(
                        sellOrders,
                        orderId
                );

        if (order != null) {

            return modifyOrderInQueue(
                    sellOrders,
                    order,
                    quantity,
                    price
            );
        }

        return false;
    }

    /**
     * Safely modifies an order that is already
     * present inside a PriorityQueue.
     *
     * Never mutate fields used by the comparator
     * while the object remains inside the queue.
     */
    private boolean modifyOrderInQueue(
            PriorityQueue<Order> queue,
            Order order,
            int quantity,
            double price) {

        boolean removed =
                queue.remove(order);

        if (!removed) {

            return false;
        }

        order.setQuantity(quantity);

        order.setPrice(price);

        /*
         * Modification resets time priority.
         */
        order.setTimestamp(
                LocalDateTime.now()
        );

        queue.offer(order);

        return true;
    }

    /**
     * Finds an active order by ID.
     */
    private Order findOrder(
            PriorityQueue<Order> queue,
            String orderId) {

        for (Order order : queue) {

            if (order.getOrderId()
                    .equals(orderId)) {

                return order;
            }
        }

        return null;
    }

    /**
     * Removes an order safely from a queue.
     */
    private boolean removeOrder(
            PriorityQueue<Order> queue,
            String orderId) {

        Iterator<Order> iterator =
                queue.iterator();

        while (iterator.hasNext()) {

            Order order =
                    iterator.next();

            if (order.getOrderId()
                    .equals(orderId)) {

                iterator.remove();

                return true;
            }
        }

        return false;
    }

    public PriorityQueue<Order> getBuyOrders() {

        return buyOrders;
    }

    public PriorityQueue<Order> getSellOrders() {

        return sellOrders;
    }

    /**
     * Returns the best bid.
     */
    public Double getBestBid() {

        if (buyOrders.isEmpty()) {

            return null;
        }

        Order order =
                buyOrders.peek();

        if (order.getExecutionType()
                == OrderExecutionType.MARKET) {

            return Double.POSITIVE_INFINITY;
        }

        return order.getPrice();
    }

    /**
     * Returns the best ask.
     */
    public Double getBestAsk() {

        if (sellOrders.isEmpty()) {

            return null;
        }

        Order order =
                sellOrders.peek();

        if (order.getExecutionType()
                == OrderExecutionType.MARKET) {

            return 0.0;
        }

        return order.getPrice();
    }

    public int getBuyOrderCount() {

        return buyOrders.size();
    }

    public int getSellOrderCount() {

        return sellOrders.size();
    }

    public boolean isEmpty() {

        return buyOrders.isEmpty()
                && sellOrders.isEmpty();
    }

    public void clear() {

        buyOrders.clear();

        sellOrders.clear();
    }

    /**
     * Prints aggregated market depth.
     */
    public void printMarketDepth() {

        System.out.println(
                "\n========== MARKET DEPTH =========="
        );

        TreeMap<Double, Integer> buyDepth =
                new TreeMap<>(
                        Comparator.reverseOrder()
                );

        TreeMap<Double, Integer> sellDepth =
                new TreeMap<>();

        /*
         * Aggregate BUY orders.
         */
        for (Order order : buyOrders) {

            buyDepth.merge(
                    order.getPrice(),
                    order.getQuantity(),
                    Integer::sum
            );
        }

        /*
         * Aggregate SELL orders.
         */
        for (Order order : sellOrders) {

            sellDepth.merge(
                    order.getPrice(),
                    order.getQuantity(),
                    Integer::sum
            );
        }

        System.out.println("\nBUY");

        if (buyDepth.isEmpty()) {

            System.out.println(
                    "No BUY Orders"
            );

        } else {

            for (var entry
                    : buyDepth.entrySet()) {

                System.out.printf(
                        "%.2f x %d%n",
                        entry.getKey(),
                        entry.getValue()
                );
            }
        }

        System.out.println(
                "\n----------------------------"
        );

        System.out.println("\nSELL");

        if (sellDepth.isEmpty()) {

            System.out.println(
                    "No SELL Orders"
            );

        } else {

            for (var entry
                    : sellDepth.entrySet()) {

                System.out.printf(
                        "%.2f x %d%n",
                        entry.getKey(),
                        entry.getValue()
                );
            }
        }

        System.out.println(
                "\n----------------------------"
        );

        Double bestBid =
                getBestBid();

        Double bestAsk =
                getBestAsk();

        System.out.println(
                "Best Bid : " + bestBid
        );

        System.out.println(
                "Best Ask : " + bestAsk
        );

        if (bestBid != null
                && bestAsk != null
                && bestBid
                != Double.POSITIVE_INFINITY
                && bestAsk != 0.0) {

            System.out.println(
                    "Spread   : "
                            + (bestAsk - bestBid)
            );
        }
    }

    /**
     * Prints orders in matching priority order.
     */
    public void printOrderBook() {

        System.out.println(
                "\n========== ORDER BOOK =========="
        );

        System.out.println(
                "\nBUY ORDERS"
        );

        PriorityQueue<Order> buyCopy =
                new PriorityQueue<>(
                        buyOrders
                );

        while (!buyCopy.isEmpty()) {

            System.out.println(
                    buyCopy.poll()
            );
        }

        System.out.println(
                "\nSELL ORDERS"
        );

        PriorityQueue<Order> sellCopy =
                new PriorityQueue<>(
                        sellOrders
                );

        while (!sellCopy.isEmpty()) {

            System.out.println(
                    sellCopy.poll()
            );
        }

        System.out.println(
                "\n--------------------------------"
        );

        System.out.println(
                "Best Bid : "
                        + getBestBid()
        );

        System.out.println(
                "Best Ask : "
                        + getBestAsk()
        );

        if (getBestBid() != null
                && getBestAsk() != null
                && getBestBid()
                != Double.POSITIVE_INFINITY
                && getBestAsk() != 0.0) {

            System.out.println(
                    "Spread   : "
                            + (
                            getBestAsk()
                                    - getBestBid()
                    )
            );
        }
    }
}