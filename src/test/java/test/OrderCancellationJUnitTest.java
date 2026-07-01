package test;

import org.junit.jupiter.api.Test;

import com.rajkumar.tradematchexchange.engine.OrderBook;
import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;

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