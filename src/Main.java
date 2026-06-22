import engine.OrderBook;
import model.MarketStatistics;
import model.Order;
import model.OrderExecutionType;
import model.OrderType;

public class Main {

    public static void main(String[] args) {

        OrderBook orderBook = new OrderBook();

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

        orderBook.matchOrders();

        System.out.println("\n===== REMAINING ORDERS =====");

        orderBook.printOrderBook();

        System.out.println("\n===== TRADE HISTORY =====");

        orderBook.getTradeHistory()
                .forEach(System.out::println);

        MarketStatistics stats =
                new MarketStatistics(
                        orderBook.getTradeHistory()
                );

        System.out.println(stats);
    }
}