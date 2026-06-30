package test;

import engine.OrderBook;
import model.Order;
import model.OrderExecutionType;
import model.OrderType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderCancellationJUnitTest {

    @Test
    void shouldCancelExistingBuyOrder() {

        OrderBook orderBook = new OrderBook();

        Order order = new Order(
                "BUY1",
                "AAPL",
                100,
                250.00,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        orderBook.addOrder(order);

        boolean cancelled = orderBook.cancelOrder("BUY1");

        assertTrue(cancelled);
        assertTrue(orderBook.getBuyOrders().isEmpty());
    }

    @Test
    void shouldCancelExistingSellOrder() {

        OrderBook orderBook = new OrderBook();

        Order order = new Order(
                "SELL1",
                "AAPL",
                100,
                250.00,
                OrderType.SELL,
                OrderExecutionType.LIMIT
        );

        orderBook.addOrder(order);

        boolean cancelled = orderBook.cancelOrder("SELL1");

        assertTrue(cancelled);
        assertTrue(orderBook.getSellOrders().isEmpty());
    }

    @Test
    void shouldReturnFalseForUnknownOrder() {

        OrderBook orderBook = new OrderBook();

        boolean cancelled = orderBook.cancelOrder("UNKNOWN");

        assertFalse(cancelled);
    }
}