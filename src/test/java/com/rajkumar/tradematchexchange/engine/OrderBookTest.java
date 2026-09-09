package com.rajkumar.tradematchexchange.engine;

import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBookTest {

    @Test
    @DisplayName("Modified BUY order receives a new timestamp")
    void modifiedBuyOrderShouldReceiveNewTimestamp() {

        OrderBook orderBook = new OrderBook();

        LocalDateTime originalTimestamp =
                LocalDateTime.of(
                        2026,
                        9,
                        9,
                        10,
                        0
                );

        Order order =
                new Order(
                        "BUY001",
                        "AAPL",
                        100,
                        200.0,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT,
                        originalTimestamp
                );

        orderBook.addOrder(order);

        boolean modified =
                orderBook.modifyOrder(
                        "BUY001",
                        150,
                        205.0
                );

        assertTrue(modified);

        Order modifiedOrder =
                orderBook
                        .getBuyOrders()
                        .peek();

        assertNotNull(modifiedOrder);

        assertSame(
                order,
                modifiedOrder
        );

        assertEquals(
                150,
                modifiedOrder.getQuantity()
        );

        assertEquals(
                205.0,
                modifiedOrder.getPrice()
        );

        assertTrue(
                modifiedOrder
                        .getTimestamp()
                        .isAfter(originalTimestamp)
        );
    }

    @Test
    @DisplayName("Modified BUY order loses time priority at same price")
    void modifiedBuyOrderShouldLoseTimePriorityAtSamePrice() {

        OrderBook orderBook = new OrderBook();

        LocalDateTime firstTimestamp =
                LocalDateTime.of(
                        2026,
                        9,
                        9,
                        10,
                        0
                );

        LocalDateTime secondTimestamp =
                LocalDateTime.of(
                        2026,
                        9,
                        9,
                        10,
                        1
                );

        Order firstOrder =
                new Order(
                        "BUY-FIRST",
                        "AAPL",
                        100,
                        200.0,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT,
                        firstTimestamp
                );

        Order secondOrder =
                new Order(
                        "BUY-SECOND",
                        "AAPL",
                        100,
                        200.0,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT,
                        secondTimestamp
                );

        orderBook.addOrder(firstOrder);

        orderBook.addOrder(secondOrder);

        assertEquals(
                "BUY-FIRST",
                orderBook
                        .getBuyOrders()
                        .peek()
                        .getOrderId()
        );

        boolean modified =
                orderBook.modifyOrder(
                        "BUY-FIRST",
                        120,
                        200.0
                );

        assertTrue(modified);

        assertEquals(
                "BUY-SECOND",
                orderBook
                        .getBuyOrders()
                        .peek()
                        .getOrderId()
        );

        assertEquals(
                120,
                firstOrder.getQuantity()
        );

        assertTrue(
                firstOrder
                        .getTimestamp()
                        .isAfter(
                                secondOrder.getTimestamp()
                        )
        );
    }

    @Test
    @DisplayName("Modified SELL order loses time priority at same price")
    void modifiedSellOrderShouldLoseTimePriorityAtSamePrice() {

        OrderBook orderBook = new OrderBook();

        LocalDateTime firstTimestamp =
                LocalDateTime.of(
                        2026,
                        9,
                        9,
                        10,
                        0
                );

        LocalDateTime secondTimestamp =
                LocalDateTime.of(
                        2026,
                        9,
                        9,
                        10,
                        1
                );

        Order firstOrder =
                new Order(
                        "SELL-FIRST",
                        "AAPL",
                        100,
                        195.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT,
                        firstTimestamp
                );

        Order secondOrder =
                new Order(
                        "SELL-SECOND",
                        "AAPL",
                        100,
                        195.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT,
                        secondTimestamp
                );

        orderBook.addOrder(firstOrder);

        orderBook.addOrder(secondOrder);

        assertEquals(
                "SELL-FIRST",
                orderBook
                        .getSellOrders()
                        .peek()
                        .getOrderId()
        );

        boolean modified =
                orderBook.modifyOrder(
                        "SELL-FIRST",
                        80,
                        195.0
                );

        assertTrue(modified);

        assertEquals(
                "SELL-SECOND",
                orderBook
                        .getSellOrders()
                        .peek()
                        .getOrderId()
        );

        assertEquals(
                80,
                firstOrder.getQuantity()
        );

        assertTrue(
                firstOrder
                        .getTimestamp()
                        .isAfter(
                                secondOrder.getTimestamp()
                        )
        );
    }

    @Test
    @DisplayName("Modified BUY price reorders the BUY queue")
    void modifiedBuyPriceShouldReorderQueue() {

        OrderBook orderBook = new OrderBook();

        Order firstOrder =
                new Order(
                        "BUY100",
                        "AAPL",
                        100,
                        200.0,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                );

        Order secondOrder =
                new Order(
                        "BUY101",
                        "AAPL",
                        100,
                        190.0,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                );

        orderBook.addOrder(firstOrder);

        orderBook.addOrder(secondOrder);

        assertEquals(
                "BUY100",
                orderBook
                        .getBuyOrders()
                        .peek()
                        .getOrderId()
        );

        boolean modified =
                orderBook.modifyOrder(
                        "BUY100",
                        100,
                        180.0
                );

        assertTrue(modified);

        assertEquals(
                "BUY101",
                orderBook
                        .getBuyOrders()
                        .peek()
                        .getOrderId()
        );

        assertEquals(
                180.0,
                firstOrder.getPrice()
        );
    }

    @Test
    @DisplayName("Modified SELL price reorders the SELL queue")
    void modifiedSellPriceShouldReorderQueue() {

        OrderBook orderBook = new OrderBook();

        Order firstOrder =
                new Order(
                        "SELL100",
                        "AAPL",
                        100,
                        190.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        Order secondOrder =
                new Order(
                        "SELL101",
                        "AAPL",
                        100,
                        200.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        orderBook.addOrder(firstOrder);

        orderBook.addOrder(secondOrder);

        assertEquals(
                "SELL100",
                orderBook
                        .getSellOrders()
                        .peek()
                        .getOrderId()
        );

        boolean modified =
                orderBook.modifyOrder(
                        "SELL100",
                        100,
                        210.0
                );

        assertTrue(modified);

        assertEquals(
                "SELL101",
                orderBook
                        .getSellOrders()
                        .peek()
                        .getOrderId()
        );

        assertEquals(
                210.0,
                firstOrder.getPrice()
        );
    }

    @Test
    @DisplayName("Unknown order modification returns false")
    void shouldReturnFalseWhenModifyingUnknownOrder() {

        OrderBook orderBook = new OrderBook();

        boolean modified =
                orderBook.modifyOrder(
                        "UNKNOWN",
                        100,
                        200.0
                );

        assertFalse(modified);
    }
}