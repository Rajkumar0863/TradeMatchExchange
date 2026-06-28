import model.Order;
import model.OrderExecutionType;
import model.OrderType;
import service.Exchange;

public class Main {

    public static void main(String[] args) {

        Exchange exchange = new Exchange();

        // CREATE STOCK ORDER BOOKS

        exchange.addStock("AAPL");
        exchange.addStock("MSFT");
        exchange.addStock("TSLA");
        exchange.addStock("GOOGL");

        exchange.displayStocks();

        // GET AAPL ORDER BOOK

        var aaplBook =
                exchange.getOrderBook("AAPL");

        // BUY ORDERS

        aaplBook.addOrder(
                new Order(
                        "ORD001",
                        "AAPL",
                        100,
                        250.0,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                )
        );

        aaplBook.addOrder(
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

        aaplBook.addOrder(
                new Order(
                        "ORD003",
                        "AAPL",
                        40,
                        190.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                )
        );

        aaplBook.addOrder(
                new Order(
                        "ORD004",
                        "AAPL",
                        50,
                        195.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                )
        );

        aaplBook.addOrder(
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

        aaplBook.matchOrders();

        // DISPLAY REMAINING ORDERS

        System.out.println(
                "\n===== AAPL ORDER BOOK ====="
        );

        aaplBook.printOrderBook();

        // DISPLAY TRADE HISTORY

        aaplBook.displayTradeHistory();
    }
}