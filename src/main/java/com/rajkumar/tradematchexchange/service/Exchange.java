package com.rajkumar.tradematchexchange.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.rajkumar.tradematchexchange.engine.MatchingEngine;
import com.rajkumar.tradematchexchange.engine.OrderBook;
import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.repository.TradeRepository;
import com.rajkumar.tradematchexchange.risk.RiskManager;

@Service
public class Exchange {

    private final Map<String, OrderBook> orderBooks;
    private final MatchingEngine matchingEngine;
    private final TradeRepository tradeRepository;
    private final RiskManager riskManager;

    public Exchange(TradeRepository tradeRepository) {

        this.orderBooks = new HashMap<>();
        this.matchingEngine = new MatchingEngine();
        this.tradeRepository = tradeRepository;
        this.riskManager = new RiskManager();
    }

    /**
     * Creates an order book for the stock
     * if one does not already exist.
     */
    public void addStock(String stockSymbol) {

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
     * Validates and places an order into
     * the in-memory exchange.
     */
    public void placeOrder(Order order) {

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

        /*
         * Add the order first.
         *
         * Only after the order is successfully
         * accepted into the book do we register
         * its ID with the RiskManager.
         */
        orderBook.addOrder(order);

        riskManager.registerOrder(
                order.getOrderId()
        );
    }

    /**
     * Runs the matching engine and returns
     * orders whose state changed.
     *
     * Orders removed from the active book
     * are also removed from the RiskManager's
     * active-order registry.
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
     * Checks whether an order still exists
     * in either side of the active order book.
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
     */
    public boolean modifyOrder(
            String stockSymbol,
            String orderId,
            int quantity,
            double price) {

        OrderBook orderBook =
                orderBooks.get(
                        stockSymbol
                );

        if (orderBook == null) {
            return false;
        }

        return orderBook.modifyOrder(
                orderId,
                quantity,
                price
        );
    }

    /**
     * Displays market depth.
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
     * Displays persisted trade history.
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
     * Displays all listed stocks.
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

    /**
     * Returns an order book.
     */
    public OrderBook getOrderBook(
            String stockSymbol) {

        return orderBooks.get(
                stockSymbol
        );
    }

    /**
     * Returns the TradeRepository.
     */
    public TradeRepository getTradeRepository() {

        return tradeRepository;
    }

    /**
     * Returns the MatchingEngine.
     */
    public MatchingEngine getMatchingEngine() {

        return matchingEngine;
    }

    /**
     * Returns the RiskManager.
     */
    public RiskManager getRiskManager() {

        return riskManager;
    }

    /**
     * Checks whether a stock exists.
     */
    public boolean containsStock(
            String stockSymbol) {

        return orderBooks.containsKey(
                stockSymbol
        );
    }

    /**
     * Returns total number of stocks.
     */
    public int totalStocks() {

        return orderBooks.size();
    }

    /**
     * Clears exchange state.
     */
    public void clearExchange() {

        orderBooks.clear();

        tradeRepository.deleteAll();

        riskManager.clear();
    }
}