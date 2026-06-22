import engine.OrderBook;
import model.Order;
import model.OrderExecutionType;
import model.OrderType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderBookJUnitTest {

    @Test
    void shouldExecuteTrade() {

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

        assertEquals(
                1,
                orderBook.getTradeHistory().size()
        );
    }

    @Test
    void shouldNotExecuteTradeWhenPricesDoNotMatch() {

        OrderBook orderBook = new OrderBook();

        Order buyOrder = new Order(
                "BUY1",
                "AAPL",
                100,
                150.0,
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

        assertEquals(
                0,
                orderBook.getTradeHistory().size()
        );
    }

    @Test
    void shouldHandlePartialFill() {

        OrderBook orderBook = new OrderBook();

        Order buyOrder = new Order(
                "BUY1",
                "AAPL",
                200,
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

        assertEquals(
                1,
                orderBook.getTradeHistory().size()
        );

        assertEquals(
                100,
                buyOrder.getQuantity()
        );
    }
}