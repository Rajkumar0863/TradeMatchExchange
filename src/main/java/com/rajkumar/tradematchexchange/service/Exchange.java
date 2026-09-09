package com.rajkumar.tradematchexchange.service;

import com.rajkumar.tradematchexchange.engine.MatchingEngine;
import com.rajkumar.tradematchexchange.engine.OrderBook;
import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.repository.TradeRepository;
import com.rajkumar.tradematchexchange.risk.RiskManager;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Exchange {

    private final Map<String, OrderBook> orderBooks;

    private final MatchingEngine matchingEngine;

    private final TradeRepository tradeRepository;

    private final RiskManager riskManager;

    public Exchange(
            TradeRepository tradeRepository) {

        this.orderBooks =
                new HashMap<>();

        this.matchingEngine =
                new MatchingEngine();

        this.tradeRepository =
                tradeRepository;

        this.riskManager =
                new RiskManager();
    }

    /**
     * Creates an order book for a stock
     * if one does not already exist.
     */
    public void addStock(
            String stockSymbol) {

        orderBooks.computeIfAbsent(
                stockSymbol,
                symbol -> {

                    System.out.println(
                            "Created Order Book : "
                                    + symbol
                    );

                    return new OrderBook();
                }
        );
    }

    /**
     * Places a new order into the exchange.
     *
     * The order must pass risk validation
     * before entering the in-memory book.
     */
    public void placeOrder(
            Order order) {

        if (!riskManager.validate(order)) {

            throw new IllegalArgumentException(
                    "Risk Validation Failed."
            );
        }

        OrderBook orderBook =
                orderBooks.get(
                        order.getStockSymbol()
                );

        if (orderBook == null) {

            throw new IllegalArgumentException(
                    "No Order Book for stock : "
                            + order.getStockSymbol()
            );
        }

        orderBook.addOrder(order);

        riskManager.registerOrder(
                order.getOrderId()
        );
    }

    /**
     * Restores a persisted active order
     * after application startup.
     *
     * No matching or database writing
     * takes place here.
     */
    public void restoreOrder(
            Order order) {

        if (order == null) {

            throw new IllegalArgumentException(
                    "Cannot restore a null order."
            );
        }

        if (order.getQuantity() <= 0) {

            throw new IllegalArgumentException(
                    "Cannot restore completed order: "
                            + order.getOrderId()
            );
        }

        addStock(
                order.getStockSymbol()
        );

        if (riskManager.containsOrder(
                order.getOrderId())) {

            System.out.println(
                    "Order already restored : "
                            + order.getOrderId()
            );

            return;
        }

        OrderBook orderBook =
                orderBooks.get(
                        order.getStockSymbol()
                );

        orderBook.addOrder(order);

        riskManager.registerOrder(
                order.getOrderId()
        );

        System.out.println(
                "Restored Order : "
                        + order.getOrderId()
                        + " | "
                        + order.getStockSymbol()
                        + " | "
                        + order.getOrderType()
                        + " | Qty "
                        + order.getQuantity()
                        + " | Price "
                        + order.getPrice()
        );
    }

    /**
     * Executes matching for a stock.
     *
     * Returns only orders whose state
     * changed during the matching cycle.
     */
    public List<Order> matchOrders(
            String stockSymbol) {

        OrderBook orderBook =
                orderBooks.get(
                        stockSymbol
                );

        if (orderBook == null) {

            throw new IllegalArgumentException(
                    "Unknown Stock : "
                            + stockSymbol
            );
        }

        List<Order> affectedOrders =
                matchingEngine.match(
                        orderBook,
                        tradeRepository
                );

        for (Order affectedOrder
                : affectedOrders) {

            if (!isOrderActive(
                    orderBook,
                    affectedOrder.getOrderId())) {

                riskManager.removeOrder(
                        affectedOrder.getOrderId()
                );
            }
        }

        return affectedOrders;
    }

    /**
     * Checks whether an order is still
     * active in either side of the book.
     */
    private boolean isOrderActive(
            OrderBook orderBook,
            String orderId) {

        boolean activeBuyOrder =
                orderBook.getBuyOrders()
                        .stream()
                        .anyMatch(order ->
                                order.getOrderId()
                                        .equals(orderId)
                        );

        if (activeBuyOrder) {

            return true;
        }

        return orderBook.getSellOrders()
                .stream()
                .anyMatch(order ->
                        order.getOrderId()
                                .equals(orderId)
                );
    }

    /**
     * Finds an active order inside a book.
     */
    private Order findActiveOrder(
            OrderBook orderBook,
            String orderId) {

        Order buyOrder =
                orderBook.getBuyOrders()
                        .stream()
                        .filter(order ->
                                order.getOrderId()
                                        .equals(orderId)
                        )
                        .findFirst()
                        .orElse(null);

        if (buyOrder != null) {

            return buyOrder;
        }

        return orderBook.getSellOrders()
                .stream()
                .filter(order ->
                        order.getOrderId()
                                .equals(orderId)
                )
                .findFirst()
                .orElse(null);
    }

    /**
     * Cancels an active order.
     */
    public boolean cancelOrder(
            String stockSymbol,
            String orderId) {

        OrderBook orderBook =
                orderBooks.get(
                        stockSymbol
                );

        if (orderBook == null) {

            return false;
        }

        boolean cancelled =
                orderBook.cancelOrder(
                        orderId
                );

        if (cancelled) {

            riskManager.removeOrder(
                    orderId
            );
        }

        return cancelled;
    }

    /**
     * Modifies an active order.
     *
     * IMPORTANT:
     *
     * This method now returns the actual
     * Order instance stored inside the
     * in-memory order book after modification.
     *
     * OrderBook.modifyOrder() resets the
     * timestamp because a modification loses
     * previous time priority.
     *
     * Returning that same object allows the
     * persistence layer to store exactly the
     * same quantity, price and timestamp.
     *
     * Returns null when the order cannot
     * be modified.
     */
    public Order modifyOrder(
            String stockSymbol,
            String orderId,
            int quantity,
            double price) {

        OrderBook orderBook =
                orderBooks.get(
                        stockSymbol
                );

        if (orderBook == null) {

            return null;
        }

        boolean modified =
                orderBook.modifyOrder(
                        orderId,
                        quantity,
                        price
                );

        if (!modified) {

            return null;
        }

        return findActiveOrder(
                orderBook,
                orderId
        );
    }

    /**
     * Displays aggregated market depth.
     */
    public void displayMarketDepth(
            String stockSymbol) {

        OrderBook orderBook =
                orderBooks.get(
                        stockSymbol
                );

        if (orderBook == null) {

            System.out.println(
                    "Stock Not Found."
            );

            return;
        }

        orderBook.printMarketDepth();
    }

    /**
     * Displays the current order book.
     */
    public void displayOrderBook(
            String stockSymbol) {

        OrderBook orderBook =
                orderBooks.get(
                        stockSymbol
                );

        if (orderBook == null) {

            System.out.println(
                    "Stock Not Found."
            );

            return;
        }

        orderBook.printOrderBook();
    }

    /**
     * Displays all persisted trades.
     */
    public void displayTradeHistory() {

        System.out.println(
                "\n========== TRADE HISTORY =========="
        );

        if (tradeRepository.count() == 0) {

            System.out.println(
                    "No Trades Executed."
            );

            return;
        }

        tradeRepository
                .findAll()
                .forEach(
                        System.out::println
                );
    }

    /**
     * Displays all registered stocks.
     */
    public void displayStocks() {

        System.out.println(
                "\n========== STOCKS =========="
        );

        orderBooks.keySet()
                .stream()
                .sorted()
                .forEach(
                        System.out::println
                );
    }

    public OrderBook getOrderBook(
            String stockSymbol) {

        return orderBooks.get(
                stockSymbol
        );
    }

    public TradeRepository getTradeRepository() {

        return tradeRepository;
    }

    public MatchingEngine getMatchingEngine() {

        return matchingEngine;
    }

    public RiskManager getRiskManager() {

        return riskManager;
    }

    public boolean containsStock(
            String stockSymbol) {

        return orderBooks.containsKey(
                stockSymbol
        );
    }

    public int totalStocks() {

        return orderBooks.size();
    }

    /**
     * Clears all in-memory exchange state
     * and persisted trades.
     */
    public void clearExchange() {

        orderBooks.clear();

        tradeRepository.deleteAll();

        riskManager.clear();
    }
}