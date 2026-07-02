package com.rajkumar.tradematchexchange.repository;

import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {

    private OrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new OrderRepository();
    }

    private Order createOrder(
            String orderId,
            String stockSymbol,
            int quantity,
            double price,
            OrderType orderType,
            OrderExecutionType executionType) {

        return new Order(
                orderId,
                stockSymbol,
                quantity,
                price,
                orderType,
                executionType
        );
    }

    @Test
    void testSaveOrder() {

        Order order = createOrder(
                "ORD001",
                "AAPL",
                100,
                200.0,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        repository.save(order);

        assertEquals(1, repository.count());
        assertTrue(repository.exists("ORD001"));
    }

    @Test
    void testFindById() {

        Order order = createOrder(
                "ORD002",
                "AAPL",
                50,
                210.0,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        repository.save(order);

        Optional<Order> result =
                repository.findById("ORD002");

        assertTrue(result.isPresent());
        assertEquals("ORD002", result.get().getOrderId());
        assertEquals(50, result.get().getQuantity());
    }

    @Test
    void testFindAll() {

        repository.save(createOrder(
                "ORD003",
                "AAPL",
                20,
                100,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        ));

        repository.save(createOrder(
                "ORD004",
                "AAPL",
                30,
                110,
                OrderType.SELL,
                OrderExecutionType.LIMIT
        ));

        List<Order> orders =
                repository.findAll();

        assertEquals(2, orders.size());
    }

    @Test
    void testExists() {

        repository.save(createOrder(
                "ORD005",
                "AAPL",
                10,
                120,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        ));

        assertTrue(repository.exists("ORD005"));
        assertFalse(repository.exists("UNKNOWN"));
    }

    @Test
    void testUpdate() {

        Order order = createOrder(
                "ORD006",
                "AAPL",
                100,
                150,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        repository.save(order);

        order.setQuantity(250);

        repository.update(order);

        Order updated =
                repository.findById("ORD006").orElseThrow();

        assertEquals(250, updated.getQuantity());
    }

    @Test
    void testDelete() {

        repository.save(createOrder(
                "ORD007",
                "AAPL",
                100,
                140,
                OrderType.SELL,
                OrderExecutionType.LIMIT
        ));

        repository.delete("ORD007");

        assertFalse(repository.exists("ORD007"));
        assertEquals(0, repository.count());
    }

    @Test
    void testCount() {

        repository.save(createOrder(
                "ORD008",
                "AAPL",
                1,
                100,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        ));

        repository.save(createOrder(
                "ORD009",
                "AAPL",
                1,
                101,
                OrderType.SELL,
                OrderExecutionType.LIMIT
        ));

        assertEquals(2, repository.count());
    }

    @Test
    void testClear() {

        repository.save(createOrder(
                "ORD010",
                "AAPL",
                5,
                150,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        ));

        repository.save(createOrder(
                "ORD011",
                "AAPL",
                5,
                160,
                OrderType.SELL,
                OrderExecutionType.LIMIT
        ));

        repository.clear();

        assertEquals(0, repository.count());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testFindMissingOrder() {

        Optional<Order> order =
                repository.findById("ORD999");

        assertTrue(order.isEmpty());
    }

    @Test
    void testDeleteMissingOrder() {

        repository.delete("UNKNOWN");

        assertEquals(0, repository.count());
    }
}