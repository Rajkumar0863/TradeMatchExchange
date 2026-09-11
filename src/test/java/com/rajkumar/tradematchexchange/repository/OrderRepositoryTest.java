package com.rajkumar.tradematchexchange.repository;

import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository repository;

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
        assertTrue(repository.existsById("ORD001"));
    }

    @Test
    void testFindById() {

        Order order = createOrder(
                "ORD002",
                "AAPL",
                50,
                180.0,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        repository.save(order);

        Order found =
                repository.findById("ORD002").orElseThrow();

        assertEquals("ORD002", found.getOrderId());
        assertEquals("AAPL", found.getStockSymbol());
        assertEquals(50, found.getQuantity());
        assertEquals(180.0, found.getPrice());
    }

    @Test
    void testFindAll() {

        repository.save(createOrder(
                "ORD003",
                "AAPL",
                100,
                100,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        ));

        repository.save(createOrder(
                "ORD004",
                "AAPL",
                100,
                110,
                OrderType.SELL,
                OrderExecutionType.LIMIT
        ));

        List<Order> orders = repository.findAll();

        assertEquals(2, orders.size());
    }

    @Test
    void testExistsById() {

        repository.save(createOrder(
                "ORD005",
                "AAPL",
                10,
                120,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        ));

        assertTrue(repository.existsById("ORD005"));
        assertFalse(repository.existsById("UNKNOWN"));
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
        repository.save(order);

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

        repository.deleteById("ORD007");

        assertFalse(repository.existsById("ORD007"));
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
    void testDeleteAll() {

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

        repository.deleteAll();

        assertEquals(0, repository.count());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testFindMissingOrder() {

        assertTrue(
                repository.findById("ORD999").isEmpty()
        );
    }
}