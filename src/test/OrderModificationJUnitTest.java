import engine.OrderBook;
import model.Order;
import model.OrderExecutionType;
import model.OrderType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderModificationJUnitTest {

    @Test
    void shouldModifyOrder() {

        OrderBook orderBook =
                new OrderBook();

        Order order =
                new Order(
                        "BUY1",
                        "AAPL",
                        100,
                        200,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                );

        orderBook.addOrder(order);

        boolean modified =
                orderBook.modifyOrder(
                        "BUY1",
                        150,
                        250
                );

        assertTrue(modified);
    }
}