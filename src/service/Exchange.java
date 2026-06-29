package service;

import engine.MatchingEngine;
import engine.OrderBook;
import model.Order;
import repository.TradeRepository;
import risk.RiskManager;

import java.util.HashMap;
import java.util.Map;

public class Exchange {

    private final Map<String, OrderBook> orderBooks;

    private final MatchingEngine matchingEngine;

    private final TradeRepository tradeRepository;

    private final RiskManager riskManager;

    public Exchange() {

        orderBooks = new HashMap<>();

        matchingEngine = new MatchingEngine();

        tradeRepository = new TradeRepository();

        riskManager = new RiskManager();
    }

    /**
     * Creates a new Order Book for a stock.
     */
    public void addStock(String stockSymbol) {

        orderBooks.computeIfAbsent(

                stockSymbol,

                symbol -> {

                    System.out.println(
                            "Created Order Book : " + symbol);

                    return new OrderBook();
                });
    }

    /**
     * Places a new order.
     */
    public void placeOrder(Order order) {

        if (!riskManager.validate(order)) {

            throw new IllegalArgumentException(
                    "Risk Validation Failed.");
        }

        OrderBook orderBook =
                orderBooks.get(
                        order.getStockSymbol());

        if (orderBook == null) {

            throw new IllegalArgumentException(

                    "No Order Book for stock : "

                            + order.getStockSymbol());
        }

        orderBook.addOrder(order);
    }

    /**
     * Executes matching for one stock.
     */
    public void matchOrders(String stockSymbol) {

        OrderBook orderBook =
                orderBooks.get(stockSymbol);

        if (orderBook == null) {

            throw new IllegalArgumentException(
                    "Unknown Stock : "
                            + stockSymbol);
        }

        matchingEngine.match(

                orderBook,

                tradeRepository);
    }

    /**
     * Cancel an order.
     */
    public boolean cancelOrder(

            String stockSymbol,

            String orderId) {

        OrderBook orderBook =
                orderBooks.get(stockSymbol);

        if (orderBook == null) {

            return false;
        }

        return orderBook.cancelOrder(orderId);
    }

    /**
     * Modify an order.
     */
    public boolean modifyOrder(

            String stockSymbol,

            String orderId,

            int quantity,

            double price) {

        OrderBook orderBook =
                orderBooks.get(stockSymbol);

        if (orderBook == null) {

            return false;
        }

        return orderBook.modifyOrder(

                orderId,

                quantity,

                price);
    }

    /**
     * Prints one Order Book.
     */
    public void displayOrderBook(

            String stockSymbol) {

        OrderBook orderBook =
                orderBooks.get(stockSymbol);

        if (orderBook == null) {

            System.out.println(
                    "Stock Not Found.");

            return;
        }

        orderBook.printOrderBook();
    }

    /**
     * Displays all executed trades.
     */
    public void displayTradeHistory() {

        System.out.println(
                "\n========== TRADE HISTORY ==========");

        if (tradeRepository.isEmpty()) {

            System.out.println(
                    "No Trades Executed.");

            return;
        }

        tradeRepository

                .getTrades()

                .forEach(System.out::println);
    }

    /**
     * Displays all listed stocks.
     */
    public void displayStocks() {

        System.out.println(
                "\n========== STOCKS ==========");

        orderBooks.keySet()

                .stream()

                .sorted()

                .forEach(System.out::println);
    }

    /**
     * Returns Order Book.
     */
    public OrderBook getOrderBook(

            String stockSymbol) {

        return orderBooks.get(stockSymbol);
    }

    /**
     * Returns Trade Repository.
     */
    public TradeRepository getTradeRepository() {

        return tradeRepository;
    }

    /**
     * Returns Matching Engine.
     */
    public MatchingEngine getMatchingEngine() {

        return matchingEngine;
    }

    /**
     * Returns Risk Manager.
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
                stockSymbol);
    }

    /**
     * Returns total listed stocks.
     */
    public int totalStocks() {

        return orderBooks.size();
    }

    /**
     * Clears the exchange.
     */
    public void clearExchange() {

        orderBooks.clear();

        tradeRepository.clear();
    }
}