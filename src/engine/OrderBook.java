package engine;

import model.Order;
import model.OrderType;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class OrderBook {

    private final PriorityQueue<Order> buyOrders;
    private final PriorityQueue<Order> sellOrders;

    public OrderBook() {

        buyOrders = new PriorityQueue<>(
                Comparator
                        .comparingDouble(Order::getPrice)
                        .reversed()
                        .thenComparing(Order::getTimestamp)
        );

        sellOrders = new PriorityQueue<>(
                Comparator
                        .comparingDouble(Order::getPrice)
                        .thenComparing(Order::getTimestamp)
        );
    }

    /**
     * Adds an order into the appropriate queue.
     */
    public void addOrder(Order order) {

        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }

        if (order.getOrderType() == OrderType.BUY) {
            buyOrders.offer(order);
        } else {
            sellOrders.offer(order);
        }
    }

    /**
     * Cancels an order by ID.
     */
    public boolean cancelOrder(String orderId) {

        return removeOrder(buyOrders, orderId)
                || removeOrder(sellOrders, orderId);
    }

    /**
     * Modifies an existing order.
     */
    public boolean modifyOrder(
            String orderId,
            int quantity,
            double price) {

        Order order = findOrder(buyOrders, orderId);

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

        order = findOrder(sellOrders, orderId);

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
     * Finds an order inside a queue.
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

        Iterator<Order> iterator = queue.iterator();

        while (iterator.hasNext()) {

            Order order = iterator.next();

            if (order.getOrderId().equals(orderId)) {

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

    public Double getBestBid() {

        return buyOrders.isEmpty()
                ? null
                : buyOrders.peek().getPrice();
    }

    public Double getBestAsk() {

        return sellOrders.isEmpty()
                ? null
                : sellOrders.peek().getPrice();
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

    public void printOrderBook() {

        System.out.println("\n========== ORDER BOOK ==========");

        System.out.println("\nBUY ORDERS");

        PriorityQueue<Order> buyCopy =
                new PriorityQueue<>(buyOrders);

        while (!buyCopy.isEmpty()) {
            System.out.println(buyCopy.poll());
        }

        System.out.println("\nSELL ORDERS");

        PriorityQueue<Order> sellCopy =
                new PriorityQueue<>(sellOrders);

        while (!sellCopy.isEmpty()) {
            System.out.println(sellCopy.poll());
        }
    }
}