package test;

import org.junit.jupiter.api.Test;

import com.rajkumar.tradematchexchange.engine.OrderBook;
import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;

import static org.junit.jupiter.api.Assertions.*;

public class OrderModificationJUnitTest {

    @Test
    void shouldModifyExistingBuyOrder() {

        OrderBook orderBook = new OrderBook();

        Order order = new Order(
                "BUY1",
                "AAPL",
                100,
                200.00,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        orderBook.addOrder(order);

        boolean modified = orderBook.modifyOrder(
                "BUY1",
                150,
                250.00
        );

        assertTrue(modified);

        Order updatedOrder = orderBook.getBuyOrders().peek();

        assertNotNull(updatedOrder);
        assertEquals(150, updatedOrder.getQuantity());
        assertEquals(250.00, updatedOrder.getPrice());
    }

    @Test
    void shouldModifyExistingSellOrder() {

        OrderBook orderBook = new OrderBook();

        Order order = new Order(
                "SELL1",
                "AAPL",
                100,
                210.00,
                OrderType.SELL,
                OrderExecutionType.LIMIT
        );

        orderBook.addOrder(order);

        boolean modified = orderBook.modifyOrder(
                "SELL1",
                80,
                205.00
        );

        assertTrue(modified);

        Order updatedOrder = orderBook.getSellOrders().peek();

        assertNotNull(updatedOrder);
        assertEquals(80, updatedOrder.getQuantity());
        assertEquals(205.00, updatedOrder.getPrice());
    }

    @Test
    void shouldReturnFalseForUnknownOrder() {

        OrderBook orderBook = new OrderBook();

        boolean modified = orderBook.modifyOrder(
                "UNKNOWN",
                100,
                200.00
        );

        assertFalse(modified);
    }
}