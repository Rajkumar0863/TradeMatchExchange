import engine.OrderBook;
import model.MarketStatistics;
import model.Order;
import model.OrderExecutionType;
import model.OrderType;
import utils.TradeExporter;

public class Main {

    public static void main(String[] args) {

        OrderBook orderBook = new OrderBook();

        // BUY ORDERS

        orderBook.addOrder(
                new Order(
                        "ORD001",
                        "AAPL",
                        100,
                        250.0,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                )
        );

        orderBook.addOrder(
                new Order(
                        "ORD002",
                        "AAPL",
                        100,
                        220.0,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                )
        );

        // SELL ORDERS

        orderBook.addOrder(
                new Order(
                        "ORD003",
                        "AAPL",
                        40,
                        190.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                )
        );

        orderBook.addOrder(
                new Order(
                        "ORD004",
                        "AAPL",
                        50,
                        195.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                )
        );

        orderBook.addOrder(
                new Order(
                        "ORD005",
                        "AAPL",
                        70,
                        210.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                )
        );

        // MATCH ORDERS

        orderBook.matchOrders();

        // REMAINING ORDERS

        System.out.println("\n===== REMAINING ORDERS =====");

        orderBook.printOrderBook();

        // TRADE HISTORY

        System.out.println("\n===== TRADE HISTORY =====");

        orderBook.getTradeHistory()
                .forEach(System.out::println);

        // MARKET STATISTICS

        MarketStatistics stats =
                new MarketStatistics(
                        orderBook.getTradeHistory()
                );

        System.out.println(stats);

        // MARKET DEPTH

        orderBook.displayMarketDepth();
        System.out.println(
                "\n===== ORDER CANCELLATION ====="
        );

        orderBook.cancelOrder("ORD002");

        System.out.println(
                "\n===== ORDER BOOK AFTER CANCELLATION ====="
        );

        orderBook.printOrderBook();

        System.out.println(
                "\n===== ORDER MODIFICATION ====="
        );

        orderBook.addOrder(
                new Order(
                        "ORD100",
                        "AAPL",
                        100,
                        220,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                )
        );

        orderBook.modifyOrder(
                "ORD100",
                150,
                240
        );

        orderBook.printOrderBook();

        // CSV EXPORT

        TradeExporter.exportTrades(
                orderBook.getTradeHistory()


        );
    }
}