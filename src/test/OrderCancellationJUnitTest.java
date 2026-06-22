import engine.OrderBook;
import model.Order;
import model.OrderExecutionType;
import model.OrderType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderCancellationJUnitTest {

    @Test
    void shouldCancelOrder() {

        OrderBook orderBook =
                new OrderBook();

        Order order =
                new Order(
                        "BUY1",
                        "AAPL",
                        100,
                        250,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                );

        orderBook.addOrder(order);

        boolean cancelled =
                orderBook.cancelOrder("BUY1");

        assertTrue(cancelled);
    }

    @Test
    void shouldFailForUnknownOrder() {

        OrderBook orderBook =
                new OrderBook();

        boolean cancelled =
                orderBook.cancelOrder("UNKNOWN");

        assertFalse(cancelled);
    }
}