import engine.OrderBook;
import model.Order;
import model.OrderExecutionType;
import model.OrderType;

public class Main {

    public static void main(String[] args)
            throws InterruptedException {

        OrderBook orderBook =
                new OrderBook();

        orderBook.addBuyOrder(
                new Order(
                        "ORD001",
                        "AAPL",
                        100,
                        250,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                )
        );

        Thread.sleep(1000);

        orderBook.addBuyOrder(
                new Order(
                        "ORD002",
                        "AAPL",
                        100,
                        250,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                )
        );

        orderBook.addSellOrder(
                new Order(
                        "ORD003",
                        "AAPL",
                        100,
                        240,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                )
        );

        orderBook.matchOrders();

        orderBook.displayTradeHistory();
    }
}