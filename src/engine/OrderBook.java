package engine;

import model.Order;
import model.OrderExecutionType;
import model.OrderType;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class OrderBook {

    private final PriorityQueue<Order> buyOrders;

    private final PriorityQueue<Order> sellOrders;

    public OrderBook() {

        /*
         * BUY Priority
         *
         * 1. MARKET Orders
         * 2. Highest Price
         * 3. Earliest Timestamp
         */
        buyOrders = new PriorityQueue<>(

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
         * SELL Priority
         *
         * 1. MARKET Orders
         * 2. Lowest Price
         * 3. Earliest Timestamp
         */
        sellOrders = new PriorityQueue<>(

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
     * Adds an order to the book.
     */
    public void addOrder(Order order) {

        if (order == null) {

            throw new IllegalArgumentException(
                    "Order cannot be null."
            );
        }

        if (order.getOrderType() == OrderType.BUY) {

            buyOrders.offer(order);

        } else {

            sellOrders.offer(order);
        }
    }

    /**
     * Cancels an order.
     */
    public boolean cancelOrder(String orderId) {

        return removeOrder(
                buyOrders,
                orderId
        ) || removeOrder(
                sellOrders,
                orderId
        );
    }

    /**
     * Modifies an order.
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

            buyOrders.remove(order);

            buyOrders.offer(

                    new Order(

                            order.getOrderId(),

                            order.getStockSymbol(),

                            quantity,

                            price,

                            order.getOrderType(),

                            order.getExecutionType()
                    )
            );

            return true;
        }

        order = findOrder(
                sellOrders,
                orderId
        );

        if (order != null) {

            sellOrders.remove(order);

            sellOrders.offer(

                    new Order(

                            order.getOrderId(),

                            order.getStockSymbol(),

                            quantity,

                            price,

                            order.getOrderType(),

                            order.getExecutionType()
                    )
            );

            return true;
        }

        return false;
    }
    /**
     * Finds an order by ID.
     */
    private Order findOrder(
            PriorityQueue<Order> queue,
            String orderId) {

        for (Order order : queue) {

            if (order.getOrderId().equals(orderId)) {
                return order;
            }
        }

        return null;
    }

    /**
     * Removes an order safely.
     */
    private boolean removeOrder(
            PriorityQueue<Order> queue,
            String orderId) {

        Iterator<Order> iterator =
                queue.iterator();

        while (iterator.hasNext()) {

            Order order =
                    iterator.next();

            if (order.getOrderId().equals(orderId)) {

                iterator.remove();

                return true;
            }
        }

        return false;
    }

    /**
     * Returns BUY queue.
     */
    public PriorityQueue<Order> getBuyOrders() {
        return buyOrders;
    }

    /**
     * Returns SELL queue.
     */
    public PriorityQueue<Order> getSellOrders() {
        return sellOrders;
    }

    /**
     * Best Bid.
     */
    public Double getBestBid() {

        if (buyOrders.isEmpty()) {
            return null;
        }

        Order order = buyOrders.peek();

        if (order.getExecutionType()
                == OrderExecutionType.MARKET) {
            return Double.POSITIVE_INFINITY;
        }

        return order.getPrice();
    }

    /**
     * Best Ask.
     */
    public Double getBestAsk() {

        if (sellOrders.isEmpty()) {
            return null;
        }

        Order order = sellOrders.peek();

        if (order.getExecutionType()
                == OrderExecutionType.MARKET) {
            return 0.0;
        }

        return order.getPrice();
    }

    /**
     * BUY order count.
     */
    public int getBuyOrderCount() {
        return buyOrders.size();
    }

    /**
     * SELL order count.
     */
    public int getSellOrderCount() {
        return sellOrders.size();
    }

    /**
     * Checks whether the book is empty.
     */
    public boolean isEmpty() {

        return buyOrders.isEmpty()
                && sellOrders.isEmpty();
    }

    /**
     * Clears the book.
     */
    public void clear() {

        buyOrders.clear();
        sellOrders.clear();
    }

    /**
     * Prints the current Order Book.
     */
    public void printOrderBook() {

        System.out.println(
                "\n========== ORDER BOOK =========="
        );

        System.out.println("\nBUY ORDERS");

        PriorityQueue<Order> buyCopy =
                new PriorityQueue<>(buyOrders);

        while (!buyCopy.isEmpty()) {

            System.out.println(
                    buyCopy.poll()
            );
        }

        System.out.println("\nSELL ORDERS");

        PriorityQueue<Order> sellCopy =
                new PriorityQueue<>(sellOrders);

        while (!sellCopy.isEmpty()) {

            System.out.println(
                    sellCopy.poll()
            );
        }

        System.out.println(
                "\n--------------------------------"
        );

        System.out.println(
                "Best Bid : " + getBestBid()
        );

        System.out.println(
                "Best Ask : " + getBestAsk()
        );

        if (getBestBid() != null
                && getBestAsk() != null
                && getBestBid() != Double.POSITIVE_INFINITY
                && getBestAsk() != 0.0) {

            System.out.println(
                    "Spread   : "
                            + (getBestAsk() - getBestBid())
            );
        }
    }
}