package test;

import model.Order;
import model.OrderExecutionType;
import model.OrderType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderJUnitTest {

    @Test
    void shouldCreateLimitBuyOrder() {

        Order order = new Order(
                "ORD001",
                "AAPL",
                100,
                250.00,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        assertEquals("ORD001", order.getOrderId());
        assertEquals("AAPL", order.getStockSymbol());
        assertEquals(100, order.getQuantity());
        assertEquals(250.00, order.getPrice());
        assertEquals(OrderType.BUY, order.getOrderType());
        assertEquals(OrderExecutionType.LIMIT, order.getExecutionType());

        assertNotNull(order.getTimestamp());
    }

    @Test
    void shouldUpdateQuantity() {

        Order order = new Order(
                "ORD002",
                "AAPL",
                100,
                200.00,
                OrderType.SELL,
                OrderExecutionType.LIMIT
        );

        order.setQuantity(40);

        assertEquals(40, order.getQuantity());
    }
    @Test
    void shouldIdentifyMarketOrder() {

        Order order = new Order(
                "ORD007",
                "AAPL",
                100,
                0.0,
                OrderType.BUY,
                OrderExecutionType.MARKET
        );

        assertTrue(order.isMarketOrder());
        assertFalse(order.isLimitOrder());
    }

    @Test
    void shouldIdentifyLimitOrder() {

        Order order = new Order(
                "ORD008",
                "AAPL",
                100,
                210.0,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        assertTrue(order.isLimitOrder());
        assertFalse(order.isMarketOrder());
    }
    @Test
    void shouldSupportMarketOrder() {

        Order order = new Order(
                "ORD004",
                "AAPL",
                50,
                0.0,
                OrderType.BUY,
                OrderExecutionType.MARKET
        );

        assertEquals(OrderExecutionType.MARKET, order.getExecutionType());
        assertEquals(0.0, order.getPrice());
    }

    @Test
    void shouldSupportIOCOrder() {

        Order order = new Order(
                "ORD005",
                "AAPL",
                25,
                210.00,
                OrderType.BUY,
                OrderExecutionType.IOC
        );

        assertEquals(OrderExecutionType.IOC, order.getExecutionType());
    }

    @Test
    void shouldSupportFOKOrder() {

        Order order = new Order(
                "ORD006",
                "AAPL",
                75,
                215.00,
                OrderType.SELL,
                OrderExecutionType.FOK
        );

        assertEquals(OrderExecutionType.FOK, order.getExecutionType());
    }
}