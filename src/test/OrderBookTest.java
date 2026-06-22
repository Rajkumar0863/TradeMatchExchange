import engine.OrderBook;
import model.Order;
import model.OrderExecutionType;
import model.OrderType;

public class OrderBookTest {

    public static void main(String[] args) {

        OrderBook orderBook = new OrderBook();

        Order buyOrder = new Order(
                "BUY1",
                "AAPL",
                100,
                250.0,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        Order sellOrder = new Order(
                "SELL1",
                "AAPL",
                100,
                200.0,
                OrderType.SELL,
                OrderExecutionType.LIMIT
        );

        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);

        orderBook.matchOrders();

        if (orderBook.getTradeHistory().size() == 1) {
            System.out.println("TEST PASSED");
        } else {
            System.out.println("TEST FAILED");
        }
    }
}