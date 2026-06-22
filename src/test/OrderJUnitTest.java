import model.Order;
import model.OrderExecutionType;
import model.OrderType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderJUnitTest {

    @Test
    void shouldCreateOrderCorrectly() {

        Order order = new Order(
                "ORD001",
                "AAPL",
                100,
                250.0,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        assertEquals("ORD001", order.getOrderId());
        assertEquals("AAPL", order.getStockSymbol());
        assertEquals(100, order.getQuantity());
        assertEquals(250.0, order.getPrice());
        assertEquals(OrderType.BUY, order.getOrderType());
    }

    @Test
    void shouldUpdateQuantity() {

        Order order = new Order(
                "ORD001",
                "AAPL",
                100,
                250.0,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        order.setQuantity(50);

        assertEquals(
                50,
                order.getQuantity()
        );
    }
}